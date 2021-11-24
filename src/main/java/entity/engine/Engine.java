package entity.engine;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import entity.flow.Flow;
import entity.flow.Route;
import entity.roadNet.roadNet.*;
import entity.vehicle.vehicle.Vehicle;
import entity.vehicle.vehicle.VehicleInfo;
import javafx.util.Pair;
import util.Barrier;
import util.Point;
import static util.Point.*;

import javax.swing.text.StyledEditorKit;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static util.JsonRelate.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


class ThreadControl implements Runnable {
    private Engine engine;
    private Barrier startBarrier;
    private Barrier endBarrier;
    private Set<Vehicle> vehicles;
    private List<Road> roads;
    private List<Intersection> intersections;
    private List<Drivable> drivables;
    public ThreadControl(Engine engine, Barrier startBarrier,Barrier endBarrier, Set<Vehicle> vehicles, List<Road> roads, List<Intersection> intersections, List<Drivable> drivables){
        this.engine = engine;
        this.startBarrier = startBarrier;
        this.endBarrier = endBarrier;
        this.vehicles = vehicles;
        this.roads = roads;
        this.drivables = drivables;
        this.intersections = intersections;
    }

    private void threadPlanRoute(List<Road> roads) {
        startBarrier.Wait();
        for(Road road : roads){
            for(Vehicle vehicle : road.getPlaneRouteBuffer())
                vehicle.updateRoute();
        }
        endBarrier.Wait();
    }

    private void threadUpdateLeaderAndGap(List<Drivable> drivables) {
        startBarrier.Wait();
        for(Drivable drivable : drivables){
            Vehicle leader = null;
            for(Vehicle vehicle : drivable.getVehicles()){
                //每辆车与前车距离更新
                vehicle.updateLeaderAndGap(leader);
                leader = vehicle;
            }
            if(drivable.isLane()){
                ((Lane)drivable).updateHistory();
            }
        }
        endBarrier.Wait();
    }

    private void threadNotifyCross(List<Intersection> intersections) {
        startBarrier.Wait();
        for(Intersection intersection : intersections){
            for(Cross cross : intersection.getCrosses())
                cross.clearNotify();
        }

        for(Intersection intersection : intersections)
            for(LaneLink laneLink : intersection.getLaneLinks()){
                List<Cross> crosses = laneLink.getCrosses();
                int index = crosses.size() - 1;//java的逆迭代器不知道怎么变位置,从末尾的cross开始

                // first check the vehicle on the end lane
                Vehicle vehicle = laneLink.getEndLane().getLastVehicle();
                if(vehicle != null && ((LaneLink)(vehicle.getPrevDrivable())).equals(laneLink)){
                    double vehDistance = vehicle.getCurDis() - vehicle.getLen();//problem vehicle距离此endlane起点的距离 C++里为getDistance()函数名未找到
                    while(index >= 0){
                        Cross cross_now = crosses.get(index);
                        double crossDistance = laneLink.getLength() - cross_now.getDistanceByLane(laneLink);
                        // cross 距 laneLink 终点
                        if (crossDistance + vehDistance < cross_now.getLeaveDistance()) {                     //problem  vehicle 距 cross 的距离小于 leaveDistance
                            cross_now.notify(laneLink, vehicle, -(vehicle.getCurDis() + crossDistance));   //problem  信息填入此 cross
                            index --;
                        } else
                            break;
                    }
                }

                // check each vehicle on laneLink
                for(Vehicle linkVehicle : laneLink.getVehicles()){
                    double vehDistance = linkVehicle.getCurDis();//problem

                    while(index >= 0){
                        Cross cross_now = crosses.get(index);
                        double crossDistance = cross_now.getDistanceByLane(laneLink);
                        if(vehDistance > crossDistance){ // vehicle 已过 cross
                            if(vehDistance - crossDistance - linkVehicle.getLen() <= cross_now.getLeaveDistance()){//problem
                                cross_now.notify(laneLink, linkVehicle, crossDistance - vehDistance);
                            }else
                                break;
                        }else{ // vehicle未过cross
                            cross_now.notify(laneLink, linkVehicle, crossDistance - vehDistance);
                        }
                        index --;
                    }
                }

                // check vehicle on the incoming lane（laneLink 上车已经检查完成但仍有 cross 未 notify）
                vehicle = laneLink.getStartLane().getFirstVehicle();
                if(vehicle != null && ((LaneLink)(vehicle.getNextDrivable())).equals(laneLink) && laneLink.isAvailable()){
                    double vehDistance = laneLink.getStartLane().getLength() - vehicle.getCurDis();
                    while(index >= 0){
                        crosses.get(index).notify(laneLink, vehicle, vehDistance);
                        index --;
                    }
                }
            }
        endBarrier.Wait();
    }

    private void threadGetAction(Set<Vehicle> vehicles) {
        startBarrier.Wait();
        List<Pair<Vehicle, Double>> buffer = new LinkedList<>();
        for(Vehicle vehicle : vehicles)
            if(vehicle.isRunning())
                engine.vehicleControl(vehicle, buffer); //计算speed、dis等信息
        synchronized (this){
            engine.getPushBuffer().addAll(buffer);
        }
        endBarrier.Wait();
    }

    private void threadUpdateLocation(List<Drivable> drivables) {
        startBarrier.Wait();
        for(Drivable drivable : drivables){
            List<Vehicle> vehicles = drivable.getVehicles();
            Iterator<Vehicle> vehicleItr = vehicles.iterator();
            while(vehicleItr.hasNext()){
                Vehicle vehicle= vehicleItr.next();
                if(vehicle.getChangedDrivable() != null || vehicle.hasSetEnd()) { // 该车已移动到下一个 drivable 或 finishChange 或 abortChange
                    vehicles.remove(vehicle);
                }

                if(vehicle.hasSetEnd()){ // 已跑完 route 或 vehicle.finishChange 或 shadow.abortChange，此时 vehicle 将被 delete
                    synchronized (this){
                        engine.getVehicleRemoveBuffer().add(vehicle);
                        /*if(!vehicle.getLaneChange().hasFinished){
                            vehicleMap.erase(vehicle->getId());
                            finishedVehicleCnt += 1; // ? 为啥 shadow 要被记录
                            cumulativeTravelTime += getCurrentTime() - vehicle->getEnterTime();
                        }*/
                        Pair<Vehicle, Integer> pair = engine.getVehiclePool().get(vehicle.getPriority());
                        engine.getThreadVehiclePool().get(pair.getValue()).remove(vehicle);
                        engine.getVehiclePool().remove(vehicle.getPriority());
                        engine.setActiveVehicleCount(engine.getActiveVehicleCount() - 1);
                    }
                }
            }
        }
        endBarrier.Wait();
    }

    private void threadUpdateAction(Set<Vehicle> vehicles) { // vehicle 信息更新
        for(Vehicle vehicle : vehicles){
            if(vehicle.isRunning()){
                if(engine.getVehicleRemoveBuffer().contains(vehicle.getBufferBlocker())){
                    vehicle.setBufferBlocker(null); //problem
                }

                vehicle.update();    // vehicle.buffer 信息移入 vehicle.controllerInfo
                //vehicle.clearSignal(); //problem 清空信号
            }
        }
        endBarrier.Wait();
    }

    public void run() {
        while(!engine.getFinished()){
            threadPlanRoute(roads);
            //laneChange...
            threadNotifyCross(intersections);
            threadGetAction(vehicles);
            threadUpdateLocation(drivables);
            threadUpdateAction(vehicles);
            threadUpdateLeaderAndGap(drivables);
        }
    }

    public Barrier getStartBarrier() {
        return startBarrier;
    }

    public void setStartBarrier(Barrier startBarrier) {
        this.startBarrier = startBarrier;
    }

    public Barrier getEndBarrier() {
        return endBarrier;
    }

    public void setEndBarrier(Barrier endBarrier) {
        this.endBarrier = endBarrier;
    }
}

public class Engine {

    private Map<Integer, Pair<Vehicle, Integer>> vehiclePool;
    private Map<String, Vehicle> vehicleMap;
    private List<Set<Vehicle>> threadVehiclePool;
    private List<List<Road>> threadRoadPool;
    private List<List<Intersection>> threadIntersectionPool;
    private List<List<Drivable>> threadDrivablePool;
    private List<Flow> flows;
    private List<Pair<Vehicle, Double>> pushBuffer;
    private List<Vehicle> VehicleRemoveBuffer;
    private List<Runnable> threadPool;

    private RoadNet roadNet;
    private int threadNum;

    private double interval;
    private boolean saveReplay;
    private boolean isSaveReplayInConfig;
    private boolean warnings;
    // private rapidjson::Document jsonRoot; java 读取 json 的类暂时未找
    private String stepLog;
    private int step;
    private int activeVehicleCount;
    private int seed;
    private Barrier startBarrier;
    private Barrier endBarrier;
    private boolean finished;
    private String dir;
    private BufferedWriter logOut;

    private boolean rlTrafficLight;
    private boolean laneChange;
    private int finishedVehicleCnt;
    private double cumulativeTravelTime;
    private Random rnd;

    private boolean loadRoadNet(String jsonFile) throws Exception {
        roadNet.loadFromJson(jsonFile);
        return true;
    }

    private boolean loadFlow(String jsonFileName) throws Exception {
        String json = readJsonData(jsonFileName);
        JSONArray flowValues = JSONObject.parseArray(json);
        for (int i = 0; i < flowValues.size(); i++) {
            JSONObject curFlowValue = flowValues.getJSONObject(i);
            Flow flow = new Flow();
            flows.add(flow);
            flow.setId("flow_" + i);
            // vehicle
            JSONObject curVehicle = getJsonMemberObject(curFlowValue, "vehicle");
            VehicleInfo vehicleInfo = flow.getVehicleTemplate();
            vehicleInfo.len = getDoubleFromJsonObject(curVehicle, "length");
            vehicleInfo.width = getDoubleFromJsonObject(curVehicle, "width");
            vehicleInfo.maxPosAcc = getDoubleFromJsonObject(curVehicle, "maxPosAcc");
            vehicleInfo.maxNegAcc = getDoubleFromJsonObject(curVehicle, "maxNegAcc");
            vehicleInfo.usualPosAcc = getDoubleFromJsonObject(curVehicle, "usualPosAcc");
            vehicleInfo.usualNegAcc = getDoubleFromJsonObject(curVehicle, "usualNegAcc");
            vehicleInfo.minGap = getDoubleFromJsonObject(curVehicle, "minGap");
            vehicleInfo.maxSpeed = getDoubleFromJsonObject(curVehicle, "maxSpeed");
            vehicleInfo.headwayTime = getDoubleFromJsonObject(curVehicle, "headwayTime");
            // route
            JSONArray curRoute = getJsonMemberArray(curFlowValue, "route");
            Route route = flow.getRoute();
            vehicleInfo.route = route;
            for (int j = 0; j < curRoute.size(); j++) {
                Road road = roadNet.getRoadById(getStringFromJsonArray(curRoute, j));
                route.getRoute().add(road);
            }
            // interval
            flow.setInterval(getDoubleFromJsonObject(curFlowValue, "interval"));
            // startTime
            flow.setStartTime(getIntFromJsonObject(curFlowValue, "startTime"));
            // endTime
            flow.setEndTime(getIntFromJsonObject(curFlowValue, "endTime"));
            flow.setEngine(this);
        }
        return true;
    }

    public boolean loadConfig(String configFile) {
        String json = readJsonData(configFile);
        JSONObject configValues = JSONObject.parseObject(json);
        try {
            interval = getDoubleFromJsonObject(configValues, "interval");
            warnings = false;
            rlTrafficLight = getBooleanFromJsonObject(configValues, "rlTrafficLight");
            laneChange = getBooleanFromJsonObject(configValues, "laneChange");
            seed = getIntFromJsonObject(configValues, "seed");
            rnd.setSeed(seed);
            dir = getStringFromJsonObject(configValues, "dir");
            String roadNetFile = getStringFromJsonObject(configValues, "roadnetFile");
            String flowFile = getStringFromJsonObject(configValues, "flowFile");
            loadRoadNet(dir + roadNetFile);
            loadFlow(dir + flowFile);
            if (warnings) {
                checkWarning();
            }
            saveReplay = isSaveReplayInConfig = getBooleanFromJsonObject(configValues, "saveReplay");
            if (saveReplay) {
                String roadNetLogFile = getStringFromJsonObject(configValues, "roadnetLogFile");
                String replayLogFile = getStringFromJsonObject(configValues, "replayLogFile");
                setLogFile(dir + roadNetLogFile, dir + replayLogFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void setLogFile(String jsonFile, String logFile) throws IOException {
        writeJsonToFile(jsonFile, "{\"static\":" + roadNet.convertToJson() + "}");
        logOut = new BufferedWriter(new FileWriter(logFile));
    }

    protected void vehicleControl(Vehicle vehicle, List<Pair<Vehicle, Double>> buffer) {
        double nextSpeed;
        if(vehicle.hasSetSpeed()) //已作为 partner 被设定过速度
            nextSpeed = vehicle.getBufferSpeed();
        else
            nextSpeed = vehicle.getNextSpeed(interval).speed; //在多条件下计算速度

        /*if(laneChange){

        }

        if (vehicle.getPartner()){

        }*/

        double deltaDis, speed = vehicle.getSpeed();
        if(nextSpeed < 0){  // 存在先前计算导致速度为负时实际情况为停车后倒车
            deltaDis = 0.5 * speed * speed / vehicle.getMaxNegAcc(); // 到停车为止
            nextSpeed = 0;
        }else{
            deltaDis = (speed + nextSpeed) * interval / 2;
        }
        vehicle.setSpeed(nextSpeed);    // speed 设置
        vehicle.setDeltaDistance(deltaDis); // drivable、dis 设置
        /*if(laneChange){

        }*/

        if(!vehicle.hasSetEnd() && vehicle.hasSetDrivable()){// 发生 drivable 变动且此时尚未到达 end
            Pair<Vehicle, Double> pair = new Pair<>(vehicle, vehicle.getBufferDis());
            buffer.add(pair);
        }
    }

    private void planRoute() {
        startBarrier.Wait();
        endBarrier.Wait();
        for(Road road : roadNet.getRoads()){
            for(Vehicle vehicle : road.getPlaneRouteBuffer())
                if(vehicle.isRouteValid()){ // vehicle.routeValid = true
                    vehicle.setFirstDrivable(); // vehicle controllerInfo.drivable 设置
                    vehicle.getCurLane().pushWaitingVehicle(vehicle);// problem
                }else{
                    Flow flow = vehicle.getFlow();
                    if(flow != null)
                        flow.setValid(true);

                    //remove this vehicle
                    Pair<Vehicle, Integer> pair = vehiclePool.get(vehicle.getPriority());
                    threadVehiclePool.get(pair.getValue()).remove(vehicle);
                    vehiclePool.remove(vehicle.getPriority());
                }
            road.clearPlanRouteBuffer();

        }
    }

    private void getAction() {
        startBarrier.Wait();
        endBarrier.Wait();
    }

    private void updateAction() {
        startBarrier.Wait();
        endBarrier.Wait();
        getVehicleRemoveBuffer().clear();
    }


    private void updateLocation() {
        startBarrier.Wait();
        endBarrier.Wait();
        pushBuffer.sort(new Comparator<Pair<Vehicle, Double>>() {
            @Override
            public int compare(Pair<Vehicle, Double> o1, Pair<Vehicle, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        for(Pair<Vehicle, Double> vehiclePair : pushBuffer){
            Vehicle vehicle = vehiclePair.getKey();
            Drivable drivable = vehicle.getChangedDrivable();
            if(drivable != null){
                drivable.pushVehicle(vehicle);
                if(drivable.isLaneLink()){
                    vehicle.setEnterLaneLinkTime(step);
                }else{
                    vehicle.setEnterLaneLinkTime(Integer.MAX_VALUE);
                }
            }
        }
        pushBuffer.clear();
    }

    private void updateLeaderAndGap() {
        startBarrier.Wait();
        endBarrier.Wait();
    }

    private void handleWaiting() { // 对每个 lane 的 waitingBuffer 的首车，判断其是否可入 lane。如可则进入并更新 leader 与 gap；如不可，则等下一个
        for(Lane lane : roadNet.getLanes()){
            List<Vehicle> buffer = lane.getWaitingBuffer();
            if(buffer.isEmpty())
                continue;
            Vehicle vehicle = buffer.get(0);
            if(lane.available(vehicle)){ //车可进入
                vehicle.setCurRunning(true); //车启动
                activeVehicleCount ++;
                Vehicle tail = lane.getLastVehicle();
                lane.pushVehicle(vehicle);
                vehicle.updateLeaderAndGap(tail); // 更新这个新进入 lane 的 vehicle 与前车的距离
                buffer.remove(0);
            }
        }
    }

    private void updateLog() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (Vehicle vehicle: getRunningVehicle()) {
            Point pos = vehicle.getPoint();
            Point dir = getDirectionByDistance(vehicle.getCurDrivable().getPoints(), vehicle.getCurDis());
            int lc = 0;
            //          int lc = vehicle.lastLaneChangeDirection();
            stringBuilder.append(pos.x).append(" ").append(pos.y).append(Math.atan2(dir.y, dir.x)).append(" ").append(vehicle.getId()).append(" ").append(lc).append(" ").append(vehicle.getLen()).append(" ").append(vehicle.getWidth()).append(",");
        }
        stringBuilder.append(";");
        for (Road road: roadNet.getRoads()) {
            if (road.getEndIntersection().isVirtual()) {
                continue;
            }
            stringBuilder.append(road.getId());
            for (Lane lane: road.getLanes()) {
                if (!lane.getEndIntersection().isNotImplicitIntersection()) {
                    stringBuilder.append(" i");
                    continue;
                }
                boolean can_go = true;
                for (LaneLink laneLink: lane.getLaneLinks()) {
                    if (!laneLink.isAvailable()) {
                        can_go = false;
                        break;
                    }
                }
                stringBuilder.append(can_go ? " g" : " r");
            }
            stringBuilder.append(";");
        }
        logOut.write(String.valueOf(stringBuilder));
    }

    private void checkWarning() {

    }

    private void notifyCross() {
        startBarrier.Wait();
        endBarrier.Wait();
    }

    public Engine(String configFile, int threadNum) {
        this.threadNum = threadNum;
        startBarrier = new Barrier(this.threadNum + 1);
        endBarrier = new Barrier(this.threadNum + 1);
        for (int i = 0; i < threadNum; i++) {
            threadVehiclePool.add(new HashSet<>());
            threadRoadPool.add(new LinkedList<>());
            threadIntersectionPool.add(new LinkedList<>());
            threadDrivablePool.add(new LinkedList<>());
        }
        boolean success = loadConfig(configFile);
        if (!success) {
            System.out.println("load config failed!");
        }
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            fixedThreadPool.execute(new ThreadControl(this,
                    startBarrier,
                    endBarrier,
                    threadVehiclePool.get(i),
                    threadRoadPool.get(i),
                    threadIntersectionPool.get(i),
                    threadDrivablePool.get(i))
            );
        }
    }

    public boolean checkPriority(int priority) {
        return true;
    }

    public void nextStep() {
        startBarrier.Wait();
        endBarrier.Wait();
    }

    public void reset(boolean resetRnd) {

    }

    public double getCurrentTime() { // 当前时间
        return step * interval;
    }

    public void pushVehicle(Vehicle vehicle, boolean pushToDrivable) {

    }

    private List<Vehicle> getRunningVehicle() {
        return null;
    }

    // set / get
    public Map<Integer, Pair<Vehicle, Integer>> getVehiclePool() {
        return vehiclePool;
    }

    public void setVehiclePool(Map<Integer, Pair<Vehicle, Integer>> vehiclePool) {
        this.vehiclePool = vehiclePool;
    }

    public Map<String, Vehicle> getVehicleMap() {
        return vehicleMap;
    }

    public void setVehicleMap(Map<String, Vehicle> vehicleMap) {
        this.vehicleMap = vehicleMap;
    }

    public List<Set<Vehicle>> getThreadVehiclePool() {
        return threadVehiclePool;
    }

    public void setThreadVehiclePool(List<Set<Vehicle>> threadVehiclePool) {
        this.threadVehiclePool = threadVehiclePool;
    }

    public List<List<Road>> getThreadRoadPool() {
        return threadRoadPool;
    }

    public void setThreadRoadPool(List<List<Road>> threadRoadPool) {
        this.threadRoadPool = threadRoadPool;
    }

    public List<List<Intersection>> getThreadIntersectionPool() {
        return threadIntersectionPool;
    }

    public void setThreadIntersectionPool(List<List<Intersection>> threadIntersectionPool) {
        this.threadIntersectionPool = threadIntersectionPool;
    }

    public List<List<Drivable>> getThreadDrivablePool() {
        return threadDrivablePool;
    }

    public void setThreadDrivablePool(List<List<Drivable>> threadDrivablePool) {
        this.threadDrivablePool = threadDrivablePool;
    }

    public List<Flow> getFlows() {
        return flows;
    }

    public void setFlows(List<Flow> flows) {
        this.flows = flows;
    }

    public RoadNet getRoadNet() {
        return roadNet;
    }

    public void setRoadNet(RoadNet roadNet_new) {
        roadNet = roadNet_new;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public double getInterval() {
        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }

    public boolean isSaveReplay() {
        return saveReplay;
    }

    public void setSaveReplay(boolean saveReplay) {
        this.saveReplay = saveReplay;
    }

    public boolean isSaveReplayInConfig() {
        return isSaveReplayInConfig;
    }

    public void setSaveReplayInConfig(boolean saveReplayInConfig) {
        isSaveReplayInConfig = saveReplayInConfig;
    }

    public boolean isWarnings() {
        return warnings;
    }

    public void setWarnings(boolean warnings) {
        this.warnings = warnings;
    }

    public List<Pair<Vehicle, Double>> getPushBuffer() {
        return pushBuffer;
    }

    public void setPushBuffer(List<Pair<Vehicle, Double>> pushBuffer) {
        this.pushBuffer = pushBuffer;
    }

    public List<Vehicle> getVehicleRemoveBuffer() {
        return VehicleRemoveBuffer;
    }

    public void setVehicleRemoveBuffer(List<Vehicle> vehicleRemoveBuffer) {
        VehicleRemoveBuffer = vehicleRemoveBuffer;
    }

    public String getStepLog() {
        return stepLog;
    }

    public void setStepLog(String stepLog) {
        this.stepLog = stepLog;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getActiveVehicleCount() {
        return activeVehicleCount;
    }

    public void setActiveVehicleCount(int activeVehicleCount) {
        this.activeVehicleCount = activeVehicleCount;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public Barrier getStartBarrier() {
        return startBarrier;
    }

    public void setStartBarrier(Barrier startBarrier) {
        this.startBarrier = startBarrier;
    }

    public Barrier getEndBarrier() {
        return endBarrier;
    }

    public void setEndBarrier(Barrier endBarrier) {
        this.endBarrier = endBarrier;
    }

    public List<Runnable> getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(List<Runnable> threadPool) {
        this.threadPool = threadPool;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public boolean isRlTrafficLight() {
        return rlTrafficLight;
    }

    public void setRlTrafficLight(boolean rlTrafficLight) {
        this.rlTrafficLight = rlTrafficLight;
    }

    public boolean isLaneChange() {
        return laneChange;
    }

    public void setLaneChange(boolean laneChange) {
        this.laneChange = laneChange;
    }

    public int getFinishedVehicleCnt() {
        return finishedVehicleCnt;
    }

    public void setFinishedVehicleCnt(int finishedVehicleCnt) {
        this.finishedVehicleCnt = finishedVehicleCnt;
    }

    public double getCumulativeTravelTime() {
        return cumulativeTravelTime;
    }

    public void setCumulativeTravelTime(double cumulativeTravelTime) {
        this.cumulativeTravelTime = cumulativeTravelTime;
    }

    public boolean getFinished(){
        return finished;
    }
    public Random getRnd() {
        return rnd;
    }

    public void setRnd(Random rnd) {
        this.rnd = rnd;
    }

    public static void main(String[] args) {
        String configFile = "configFile.json";
        Engine engine = new Engine(configFile, 10);
    }
}
