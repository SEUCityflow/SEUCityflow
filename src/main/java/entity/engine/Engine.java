package entity.engine;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import entity.archive.Archive;
import entity.flow.Flow;
import entity.flow.Route;
import entity.roadNet.roadNet.*;
import entity.roadNet.trafficLight.LightPhase;
import entity.roadNet.trafficLight.TrafficLight;
import entity.vehicle.laneChange.LaneChange;
import entity.vehicle.router.RouterType;
import entity.vehicle.vehicle.Vehicle;
import entity.vehicle.vehicle.VehicleInfo;
import util.JsonMemberMiss;
import util.Pair;
import util.Barrier;
import util.Point;

import static util.Point.*;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static util.JsonRelate.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Engine {
    private Map<Integer, Pair<Vehicle, Integer>> vehiclePool;
    private Map<String, Vehicle> vehicleMap;
    private Map<String, Flow> flowMap;
    private List<Set<Vehicle>> threadVehiclePool;
    private List<List<Road>> threadRoadPool;
    private List<List<Intersection>> threadIntersectionPool;
    private List<List<Drivable>> threadDrivablePool;
    private List<Flow> flows;
    private List<Pair<Vehicle, Double>> pushBuffer;
    private List<Vehicle> VehicleRemoveBuffer;
    private List<Runnable> threadPool;
    private List<Vehicle> laneChangeNotifyBuffer;
    private final ExecutorService threadExecutor;

    private RoadNet roadNet;
    private int threadNum;

    private double interval;
    private boolean saveReplay;
    private boolean isSaveReplayInConfig;
    private boolean warnings;
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
    private int manuallyPushCnt;
    private Random rnd;
    private RouterType routerType = RouterType.LENGTH;

    private static final Map<String, RouterType> typeNameMap = new HashMap<>();

    static {
        typeNameMap.put("LENGTH", RouterType.LENGTH);
        typeNameMap.put("DURATION", RouterType.DURATION);
        typeNameMap.put("DYNAMIC", RouterType.DYNAMIC);
        typeNameMap.put("RANDOM", RouterType.RANDOM);
    }

    // load
    private boolean loadRoadNet(String jsonFile) throws IOException {
        roadNet.loadFromJson(jsonFile);
        int cnt = 0;
        for (Road road : roadNet.getRoads()) {
            threadRoadPool.get(cnt).add(road);
            cnt = (cnt + 1) % threadNum;
        }
        for (Intersection intersection : roadNet.getIntersections()) {
            threadIntersectionPool.get(cnt).add(intersection);
            cnt = (cnt + 1) % threadNum;
        }
        for (Drivable drivable : roadNet.getDrivables()) {
            threadDrivablePool.get(cnt).add(drivable);
            cnt = (cnt + 1) % threadNum;
        }
        return true;
    }

    private boolean loadFlow(String jsonFileName) throws IOException {
        String json = readJsonData(jsonFileName);
        JSONArray flowValues = JSONObject.parseArray(json);
        for (int i = 0; i < flowValues.size(); i++) {
            JSONObject curFlowValue = flowValues.getJSONObject(i);
            Flow flow = new Flow();
            flows.add(flow);
            flow.setId("flow_" + i);
            flowMap.put(flow.getId(), flow);
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
            flow.setNowTime(flow.getInterval());
            // startTime
            flow.setStartTime(getIntFromJsonObject(curFlowValue, "startTime"));
            // endTime
            flow.setEndTime(getIntFromJsonObject(curFlowValue, "endTime"));
            flow.setEngine(this);
        }
        return true;
    }

    private boolean loadConfig(String configFile) throws IOException {
        String json = readJsonData(configFile);
        JSONObject configValues = JSONObject.parseObject(json);
        interval = getDoubleFromJsonObject(configValues, "interval");
        warnings = false;
        rlTrafficLight = getBooleanFromJsonObject(configValues, "rlTrafficLight");
        laneChange = getBooleanFromJsonObject(configValues, "laneChange");
        seed = getIntFromJsonObject(configValues, "seed");
        rnd.setSeed(seed);
        dir = getStringFromJsonObject(configValues, "dir");
        try {
            String jsonRouteType = getStringFromJsonObject(configValues, "routeType");
            routerType = typeNameMap.get(jsonRouteType);
        } catch (JsonMemberMiss jsonMemberMiss) {
            routerType = RouterType.LENGTH;
        }
        String roadNetFile = getStringFromJsonObject(configValues, "roadnetFile");
        String flowFile = getStringFromJsonObject(configValues, "flowFile");
        if (!loadRoadNet(dir + roadNetFile)) {
            System.err.println("load roadNet error");
            return false;
        }
        if (!loadFlow(dir + flowFile)) {
            System.err.println("load flow error");
            return false;
        }
        saveReplay = isSaveReplayInConfig = getBooleanFromJsonObject(configValues, "saveReplay");
        if (saveReplay) {
            String roadNetLogFile = getStringFromJsonObject(configValues, "roadnetLogFile");
            String replayLogFile = getStringFromJsonObject(configValues, "replayLogFile");
            setLogFile(dir + roadNetLogFile, dir + replayLogFile);
        }
        if (warnings) {
            checkWarning();
        }
        return true;
    }

    public void setLogFile(String jsonFile, String logFile) throws IOException {
        writeJsonToFile(jsonFile, "{\"static\":" + roadNet.convertToJson() + "}");
        logOut = new BufferedWriter(new FileWriter(logFile));
    }

    private void checkWarning() {
        if (interval < 0.2 || interval > 1.5) {
            System.err.println("Deprecated time interval, recommended interval between 0.2 and 1.5");
        }
        for (Lane lane : roadNet.getLanes()) {
            if (lane.getLength() < 50) {
                System.err.println("Deprecated road length, recommended road length at least 50 meters");
            }
            if (lane.getMaxSpeed() > 30) {
                System.err.println("Deprecated road max speed, recommended max speed at most 30 meters/s");
            }
        }
    }

    // nextStep
    private double calculateSpeed(Vehicle vehicle) {
        double nextSpeed;
        if (vehicle.hasSetSpeed()) {//已作为 partner 被设定过速度
            nextSpeed = vehicle.getBufferSpeed();
        } else {
            nextSpeed = vehicle.getNextSpeed(interval);//在多条件下计算速度
        }
        if (laneChange) {
            Vehicle partner = vehicle.getPartner();
            if (partner != null && !partner.hasSetSpeed()) { // 有 partner 且尚未进行 vehicleControl，在此同步速度
                double partnerSpeed = partner.getNextSpeed(interval);
                nextSpeed = Math.min(nextSpeed, partnerSpeed);
                partner.setBufferSpeed(nextSpeed);
                if (partner.hasSetEnd()) {
                    vehicle.setBufferEnd(true);
                }
            }
        }
        if (nextSpeed < 0) {
            nextSpeed = 0;
        }
        vehicle.setBufferSpeed(nextSpeed);
        return nextSpeed;
    }

    private double calculateDistance(Vehicle vehicle, double nextSpeed) {
        double speed = vehicle.getSpeed();
        double deltaDis;
        if (nextSpeed == 0) {
            deltaDis = 0.5 * speed * speed / vehicle.getMaxNegAcc(); // 到停车为止
        } else {
            deltaDis = (speed + nextSpeed) * interval / 2;
        }
        vehicle.setDeltaDistance(deltaDis);
        return deltaDis;
    }

    private void calculateOffset(Vehicle vehicle, double nextSpeed) {
        if (laneChange) {
            if (!vehicle.isReal() && vehicle.getChangedDrivable() != null) { // 当前为 shadow 在此阶段行驶 deltaDis 后发生 drivable 变化
                vehicle.abortLaneChange();                                      // 放弃此次 laneChange 并清除 partner laneChange 状态
            }
            if (vehicle.isChanging()) {
                int dir = vehicle.getLaneChangeDirection();                                                     // 0:直行，1:outerLane，-1:innerLane
                double laneChangeSpeed = Math.max(0.2 * nextSpeed, 1);
                double newOffSet = Math.abs(vehicle.getOffSet() + laneChangeSpeed * interval * dir); // 横向偏移量计算
                newOffSet = Math.min(newOffSet, vehicle.getMaxOffSet());
                vehicle.setOffSet(newOffSet * dir); // 更新偏移量
                if (newOffSet >= vehicle.getMaxOffSet()) { // laneChange 完成
                    synchronized (this) {
                        vehicleMap.remove(vehicle.getPartner().getId());    // 清除 shadow 的映射
                        vehicleMap.put(vehicle.getId(), vehicle.getPartner()); // 完成 laneChange，自己成为 shadow
                        vehicle.finishChanging();
                    }
                }
            }
        }
    }

    private void checkDrivableChange(Vehicle vehicle, List<Pair<Vehicle, Double>> buffer, List<Vehicle> buffer2) {
        if (!vehicle.hasSetEnd() && vehicle.hasSetDrivable()) {// 发生 drivable 变动且此时尚未到达 end
            Pair<Vehicle, Double> pair = new Pair<>(vehicle, vehicle.getBufferDis());
            buffer.add(pair);
            if (vehicle.getBufferDrivable().isLane() && vehicle.getCurRouter().getType() == RouterType.DYNAMIC && vehicle.isRouteValid()) {
                buffer2.add(vehicle);
            }
        }
    }

    public void vehicleControl(Vehicle vehicle, List<Pair<Vehicle, Double>> buffer1, List<Vehicle> buffer2) {
        double nextSpeed = calculateSpeed(vehicle);
        double deltaDis = calculateDistance(vehicle, nextSpeed);
        calculateOffset(vehicle, nextSpeed);
        checkDrivableChange(vehicle, buffer1, buffer2);
        if (vehicle.isGrouped()) {
            if (nextSpeed > vehicle.getCurDrivable().getMaxSpeed() * Vehicle.Alpha) {
                for (Vehicle vehicle1 : vehicle.getSegment().getVehicles()) {
                    if (vehicle == vehicle1) {
                        continue;
                    }
                    calculateSpeed(vehicle1);
                    calculateDistance(vehicle1, nextSpeed);
                    calculateOffset(vehicle1, nextSpeed);
                    checkDrivableChange(vehicle1, buffer1, buffer2);
                }
            } else {
                for (Vehicle vehicle1 : vehicle.getSegment().getVehicles()) {
                    if (vehicle == vehicle1) {
                        continue;
                    }
                    vehicle1.setBufferSpeed(nextSpeed);
                    vehicle1.setDeltaDistance(deltaDis);
                    checkDrivableChange(vehicle1, buffer1, buffer2);
                }
            }
        }
    }

    private void planRoute() {
        startBarrier.Wait();
        endBarrier.Wait();
        for (Road road : roadNet.getRoads()) {
            for (Vehicle vehicle : road.getPlanRouteBuffer())
                if (vehicle.isRouteValid()) { // vehicle.routeValid = true
                    vehicle.setFirstDrivable(); // vehicle controllerInfo.drivable 设置
                    vehicle.getCurLane().pushWaitingVehicle(vehicle);// problem
                } else {
                    Flow flow = vehicle.getFlow();
                    if (flow != null) {
                        flow.setValid(false);
                    }
                    //remove this vehicle
                    Pair<Vehicle, Integer> pair = vehiclePool.get(vehicle.getPriority());
                    threadVehiclePool.get(pair.getValue()).remove(vehicle);
                    vehiclePool.remove(vehicle.getPriority());
                }
            road.clearPlanRouteBuffer();
        }
    }

    private void handleWaiting() { // 对每个 lane 的 waitingBuffer 的首车，判断其是否可入 lane。如可则进入并更新 leader 与 gap；如不可，则等下一个
        for (Lane lane : roadNet.getLanes()) {
            List<Vehicle> buffer = lane.getWaitingBuffer();
            if (buffer.isEmpty()) {
                continue;
            }
            Vehicle vehicle = buffer.get(0);
            if (lane.available(vehicle)) { //车可进入
                vehicle.setCurRunning(true); //车启动
                activeVehicleCount++;
                Vehicle tail = lane.getLastVehicle();
                lane.pushVehicle(vehicle);
                vehicle.updateLeaderAndGap(tail); // 更新这个新进入 lane 的 vehicle 与前车的距离
                buffer.remove(0);
            }
        }
    }

    private void initSegments() {
        startBarrier.Wait();
        endBarrier.Wait();
    }

    private void scheduleLaneChange() {
        laneChangeNotifyBuffer.sort(Comparator.comparing(Vehicle::getLaneChangeUrgency));
        Set<Lane> updateSet = new HashSet<>();
        for (Vehicle vehicle : laneChangeNotifyBuffer) {
            vehicle.updateLaneChangeNeighbor();
            vehicle.sendSignal();
            if (!vehicle.isChanging() && vehicle.planLaneChange() && vehicle.canChange()) {
                LaneChange laneChange = vehicle.getLaneChange();
                if (laneChange.isGapValid() && vehicle.getCurDrivable().isLane()) {
                    insertShadow(vehicle);
                    updateSet.add(laneChange.getTarget());
                }
            }
        }
        for (Lane lane : updateSet) {
            lane.getVehicles().clear();
            for (int i = lane.getSegmentNum() - 1; i >= 0; i--) {
                Segment segment = lane.getSegment(i);
                lane.getVehicles().addAll(segment.getVehicles());
            }
        }
        for (Drivable drivable : roadNet.getDrivables()) { // can be improved
            if (drivable.getVehicles().size() != 0) {
                drivable.getVehicles().get(0).updateLeaderAndGap(null);
            }
        }
        laneChangeNotifyBuffer.clear();
    }

    private void planLaneChange() {
        startBarrier.Wait();
        endBarrier.Wait();
        scheduleLaneChange();
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
        pushBuffer.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        for (Pair<Vehicle, Double> vehiclePair : pushBuffer) {
            Vehicle vehicle = vehiclePair.getKey();
            Drivable drivable = vehicle.getChangedDrivable();
            if (drivable != null) {
                drivable.pushVehicle(vehicle);
                if (drivable.isLaneLink()) {
                    vehicle.setEnterLaneLinkTime(step);
                } else {
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

    private void updateShorterRoute() {
        startBarrier.Wait();
        endBarrier.Wait();
    }

    private void updateLog() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (Vehicle vehicle : getRunningVehicle()) {
            Point pos = vehicle.getPoint();
            Point dir = getDirectionByDistance(vehicle.getCurDrivable().getPoints(), vehicle.getCurDis());
            int lc = 0;
            stringBuilder.append(pos.x).append(" ").append(pos.y).append(" ").append(Math.atan2(dir.y, dir.x)).append(" ").append(vehicle.getId()).append(" ").append(lc).append(" ").append(vehicle.getLen()).append(" ").append(vehicle.getWidth()).append(",");
        }
        stringBuilder.append(";");
        for (Road road : roadNet.getRoads()) {
            if (road.getEndIntersection().isVirtual()) {
                continue;
            }
            stringBuilder.append(road.getId());
            for (Lane lane : road.getLanes()) {
                if (!lane.getEndIntersection().isNotImplicitIntersection()) {
                    stringBuilder.append(" i");
                    continue;
                }
                boolean can_go = true;
                for (LaneLink laneLink : lane.getLaneLinks()) {
                    if (!laneLink.isAvailable()) {
                        can_go = false;
                        break;
                    }
                }
                stringBuilder.append(can_go ? " g" : " r");
            }
            stringBuilder.append(",");
        }
        stringBuilder.append("\n");
        logOut.write(String.valueOf(stringBuilder));
    }

    private void notifyCross() {
        startBarrier.Wait();
        endBarrier.Wait();
    }

    private void insertShadow(Vehicle vehicle) {
        int threadIndex = vehiclePool.get(vehicle.getPriority()).getValue();
        Vehicle shadow = new Vehicle(vehicle, vehicle.getId() + "_shadow", this, vehicle.getFlow());
        vehicleMap.put(shadow.getId(), shadow);
        vehiclePool.put(shadow.getPriority(), new Pair<>(shadow, threadIndex));
        threadVehiclePool.get(threadIndex).add(shadow);
        vehicle.insertShadow(shadow);
        activeVehicleCount++;
    }

    public void nextStep() {
        for (Flow flow : flows) { // O(n), n = flow.size()
            flow.nextStep(interval);
        }
        planRoute(); // O(n), n = vehicle.size() in planRouteBuffer
        handleWaiting(); // O(n), n = lane.size()
        initSegments();
        if (laneChange) {
            planLaneChange(); // O(nlogn + m), n = vehicle.size() in laneChangeNotifyBuffer, m = drivable.size()
        }
        notifyCross();
        getAction();
        updateLocation(); // 0(nlogn), n = vehicle.size() in pushBuffer
        updateAction();
        updateLeaderAndGap();
        updateShorterRoute();
        if (!rlTrafficLight) {
            for (Intersection intersection : roadNet.getIntersections()) {
                if (intersection.isVirtual()) {
                    continue;
                }
                intersection.getTrafficLight().passTime(interval);
            }
        }
        if (saveReplay) {
            try {
                updateLog(); // O(n), n = runningVehicle.size()
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        step += 1;
    }

    public void pushVehicle(Vehicle vehicle, boolean pushToDrivable) {
        int threadIndex = getRnd().nextInt(threadNum);
        vehiclePool.put(vehicle.getPriority(), new Pair<>(vehicle, threadIndex));
        vehicleMap.put(vehicle.getId(), vehicle);
        threadVehiclePool.get(threadIndex).add(vehicle);

        if (pushToDrivable) {//放入Drivable
            ((Lane) vehicle.getCurDrivable()).pushWaitingVehicle(vehicle);
        }
    }

    public boolean checkPriority(int priority) {
        return vehiclePool.get(priority) != null;
    }

    // 构造
    public Engine(String configFile, int threadNum) throws IOException {
        vehiclePool = new HashMap<>();
        vehicleMap = new HashMap<>();
        flowMap = new HashMap<>();
        threadVehiclePool = new ArrayList<>();
        threadRoadPool = new ArrayList<>();
        threadIntersectionPool = new ArrayList<>();
        threadDrivablePool = new ArrayList<>();
        pushBuffer = new ArrayList<>();
        VehicleRemoveBuffer = new ArrayList<>();
        laneChangeNotifyBuffer = new ArrayList<>();
        threadPool = new ArrayList<>();
        rnd = new Random();
        roadNet = new RoadNet();
        flows = new ArrayList<>();
        this.threadNum = threadNum;
        startBarrier = new Barrier(this.threadNum + 1);
        endBarrier = new Barrier(this.threadNum + 1);
        for (int i = 0; i < threadNum; i++) {
            threadVehiclePool.add(new HashSet<>());
            threadRoadPool.add(new ArrayList<>());
            threadIntersectionPool.add(new ArrayList<>());
            threadDrivablePool.add(new ArrayList<>());
            threadPool.add(new ThreadControl(this,
                    startBarrier,
                    endBarrier,
                    threadVehiclePool.get(i),
                    threadRoadPool.get(i),
                    threadIntersectionPool.get(i),
                    threadDrivablePool.get(i))
            );
        }
        boolean success = loadConfig(configFile);
        if (!success) {
            System.out.println("load config failed!");
        }
        threadExecutor = Executors.newCachedThreadPool();
        for (int i = 0; i < threadNum; i++) {
            threadExecutor.execute(threadPool.get(i));
        }
    }

    public void close() {
        try {
            if (saveReplay) {
                logOut.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(100);
            finished = true;
            int cnt = 8;
            if (isLaneChange()) {
                cnt += 1;
            }
            for (int i = 0; i < cnt; i++) {
                startBarrier.Wait();
                endBarrier.Wait();
            }
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
        threadExecutor.shutdown();
    }

    public void reset(boolean resetRnd) {
        vehiclePool.clear();
        for (Set<Vehicle> pool : threadVehiclePool) {
            pool.clear();
        }

        vehicleMap.clear();
        roadNet.reset();

        finishedVehicleCnt = 0;
        cumulativeTravelTime = 0;

        for (Flow flow : flows) {
            flow.reset();
        }

        step = 0;
        activeVehicleCount = 0;
        if (resetRnd) {
            rnd.setSeed(seed);
        }
    }

    //archive
    public void load(Archive archive) {
        archive.resume(this);
    }

    public Archive snapshot() {
        return new Archive(this);
    }

    public void loadFromFile(String fileName) throws FileNotFoundException {
        Archive archive = Archive.load(this, fileName);
        archive.resume(this);
    }

    public Archive loadArchiveFromFile(String fileName) throws FileNotFoundException {
        return Archive.load(this, fileName);
    }

    public void saveArchiveToFile(Archive archive, String fileName) throws IOException {
        archive.dump(fileName);
    }

    public void saveToFile(String fileName) throws IOException {
        Archive archive = snapshot();
        archive.dump(fileName);
    }

    // RL relate api
    public void pushVehicle(Map<String, Double> info, List<String> roads) {
        VehicleInfo vehicleInfo = new VehicleInfo();
        if (info.get("speed") != null) {
            vehicleInfo.setSpeed(info.get("speed"));
        }
        if (info.get("length") != null) {
            vehicleInfo.setLen(info.get("length"));
        }
        if (info.get("width") != null) {
            vehicleInfo.setWidth(info.get("width"));
        }
        if (info.get("maxPosAcc") != null) {
            vehicleInfo.setMaxPosAcc(info.get("maxPosAcc"));
        }
        if (info.get("maxNegAcc") != null) {
            vehicleInfo.setMaxNegAcc(info.get("maxNegAcc"));
        }
        if (info.get("usualPosAcc") != null) {
            vehicleInfo.setUsualPosAcc(info.get("usualPosAcc"));
        }
        if (info.get("usualNegAcc") != null) {
            vehicleInfo.setUsualNegAcc(info.get("usualNegAcc"));
        }
        if (info.get("minGap") != null) {
            vehicleInfo.setMinGap(info.get("minGap"));
        }
        if (info.get("maxSpeed") != null) {
            vehicleInfo.setMaxSpeed(info.get("maxSpeed"));
        }
        if (info.get("headwayTime") != null) {
            vehicleInfo.setHeadwayTime(info.get("headwayTime"));
        }
        List<Road> routes = vehicleInfo.getRoute().getRoute();
        for (String roadId : roads) {
            routes.add(roadNet.getRoadById(roadId));
        }
        Vehicle vehicle = new Vehicle(vehicleInfo, "manually_pushed_" + manuallyPushCnt, this, null);
        pushVehicle(vehicle, false);
        vehicle.getFirstRoad().addPlanRouteVehicle(vehicle);
    }

    public void setRandomSeed(int seed) {
        rnd.setSeed(seed);
    }

    public int getVehicleCount() {
        return activeVehicleCount;
    }

    public List<String> getVehicles(boolean includeWaiting) {
        List<String> ret = new ArrayList<>();
        for (Vehicle vehicle : getRunningVehicle(includeWaiting)) {
            ret.add(vehicle.getId());
        }
        return ret;
    }

    public Map<String, Integer> getLaneVehicleCount() {
        Map<String, Integer> ret = new HashMap<>();
        for (Lane lane : roadNet.getLanes()) {
            ret.put(lane.getId(), lane.getVehicleCount());
        }
        return ret;
    }

    public Map<String, Integer> getLaneWaitingVehicleCount() {
        Map<String, Integer> ret = new HashMap<>();
        for (Lane lane : roadNet.getLanes()) {
            int cnt = 0;
            for (Vehicle vehicle : lane.getVehicles()) {
                if (vehicle.getSpeed() < 0.1) {
                    cnt++;
                }
            }
            ret.put(lane.getId(), cnt);
        }
        return ret;
    }

    public Map<String, List<String>> getLaneVehicles() {
        Map<String, List<String>> ret = new HashMap<>();
        for (Lane lane : roadNet.getLanes()) {
            List<String> vehicles = new ArrayList<>();
            for (Vehicle vehicle : lane.getVehicles()) {
                vehicles.add(vehicle.getId());
            }
            ret.put(lane.getId(), vehicles);
        }
        return ret;
    }

    public Map<String, Double> getVehicleSpeed() {
        Map<String, Double> ret = new HashMap<>();
        for (Vehicle vehicle : getRunningVehicle()) {
            ret.put(vehicle.getId(), vehicle.getSpeed());
        }
        return ret;
    }

    public Map<String, Double> getVehicleDistance() {
        Map<String, Double> ret = new HashMap<>();
        for (Vehicle vehicle : getRunningVehicle()) {
            ret.put(vehicle.getId(), vehicle.getCurDis());
        }
        return ret;
    }

    public Map<String, String> getVehicleInfo(String id) throws RuntimeException {
        Vehicle vehicle = vehicleMap.get(id);
        if (vehicle == null) {
            throw new RuntimeException("Vehicle " + id + " not found");
        } else {
            return vehicle.getInfo();
        }
    }

    public String getLeader(String vehicleId) throws RuntimeException {
        Vehicle vehicle = vehicleMap.get(vehicleId);
        if (vehicle == null) {
            throw new RuntimeException("Vehicle '" + vehicleId + "' not found");
        } else {
            if (laneChange) {
                if (!vehicle.isReal()) {
                    vehicle = vehicle.getPartner();
                }
            }
            Vehicle leader = vehicle.getCurLeader();
            if (leader != null) {
                return leader.getId();
            } else {
                return "";
            }
        }
    }

    public double getCurrentTime() { // 当前时间
        return step * interval;
    }

    public double getAverageTravelTime() {
        double tt = cumulativeTravelTime;
        int n = finishedVehicleCnt;
        return n == 0 ? 0 : tt / n;
    }

    public void setTrafficLightPhase(String id, int phaseIndex) { // 设置某 intersection 当前信号灯阶段
        if (!rlTrafficLight) {
            System.out.println("please set rlTrafficLight to true to enable traffic light control");
            return;
        }
        TrafficLight trafficLight = roadNet.getIntersectionById(id).getTrafficLight();
        LightPhase lastPhase = trafficLight.getPhases().get(trafficLight.getCurPhaseIndex());
        trafficLight.updateCumulateTime(phaseIndex, trafficLight.getCurPhaseIndex(), lastPhase.getTime() - trafficLight.getRemainDuration());
        trafficLight.setCurPhaseIndex(phaseIndex);
    }

    public void setReplayLogFile(String logFile) throws IOException {
        if (!isSaveReplayInConfig) {
            System.out.println("saveReplay is not set to true in config file!");
            return;
        }
        if (logOut != null) {
            logOut.close();
        }
        logOut = new BufferedWriter(new FileWriter(logFile));
    }

    public void setSaveReplay(boolean open) {
        if (!isSaveReplayInConfig) {
            System.out.println("saveReplay is not set to true in config file!");
            return;
        }
        saveReplay = open;
    }

    public void setVehicleSpeed(String id, double speed) throws Exception { //设置某车速度
        Vehicle vehicle = vehicleMap.get(id);
        if (vehicle == null) {
            throw new Exception("Vehicle '" + id + "' not found");
        } else {
            vehicle.setBufferCustomSpeed(speed);
        }
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public boolean setRoute(String vehicleId, List<String> anchorId) {
        Vehicle vehicle = vehicleMap.get(vehicleId);
        if (vehicle == null) {
            return false;
        }
        List<Road> anchors = new ArrayList<>();
        for (String roadId : anchorId) {
            Road road = roadNet.getRoadById(roadId);
            if (road == null) {
                return false;
            }
            anchors.add(road);
        }
        return vehicle.changeRoute(anchors);
    }

    private List<Vehicle> getRunningVehicle() {
        return getRunningVehicle(false);
    }

    private List<Vehicle> getRunningVehicle(boolean includeWaiting) {
        List<Vehicle> ret = new ArrayList<>();
        for (int key : vehiclePool.keySet()) {
            Vehicle vehicle = vehiclePool.get(key).getKey();
            if ((includeWaiting || vehicle.isCurRunning()) && vehicle.isReal()) {
                ret.add(vehicle);
            }
        }
        return ret;
    }

    // set / get
    public Map<String, Flow> getFlowMap() {
        return flowMap;
    }

    public void setFlowMap(Map<String, Flow> flowMap) {
        this.flowMap = flowMap;
    }

    public ExecutorService getThreadExecutor() {
        return threadExecutor;
    }

    public BufferedWriter getLogOut() {
        return logOut;
    }

    public void setLogOut(BufferedWriter logOut) {
        this.logOut = logOut;
    }

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

    public void addCumulativeTravelTime(double cumulativeTravelTime) {
        this.cumulativeTravelTime += cumulativeTravelTime;
    }

    public void setCumulativeTravelTime(double cumulativeTravelTime) {
        this.cumulativeTravelTime = cumulativeTravelTime;
    }

    public boolean getFinished() {
        return finished;
    }

    public Random getRnd() {
        return rnd;
    }

    public void setRnd(Random rnd) {
        this.rnd = rnd;
    }

    public int getManuallyPushCnt() {
        return manuallyPushCnt;
    }

    public void setManuallyPushCnt(int manuallyPushCnt) {
        this.manuallyPushCnt = manuallyPushCnt;
    }

    public List<Vehicle> getLaneChangeNotifyBuffer() {
        return laneChangeNotifyBuffer;
    }

    public void setLaneChangeNotifyBuffer(List<Vehicle> laneChangeNotifyBuffer) {
        this.laneChangeNotifyBuffer = laneChangeNotifyBuffer;
    }

    public RouterType getRouterType() {
        return routerType;
    }

    public void setRouterType(RouterType routerType) {
        this.routerType = routerType;
    }
}
