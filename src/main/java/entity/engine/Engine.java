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

import static util.JsonRelate.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static util.JsonRelate.readJsonData;

class ThreadControl implements Runnable {
    private Barrier startBarrier;
    private Barrier endBarrier;

    private void threadPlanRoute(List<Road> roads) {

    }

    private void threadUpdateLeaderAndGap(List<Drivable> drivables) {

    }

    private void threadNotifyCross(List<Intersection> intersections) {

    }

    private void threadGetAction(Set<Vehicle> vehicles) {

    }

    private void threadUpdateLocation(List<Drivable> drivables) {

    }

    private void threadUpdateAction(Set<Vehicle> vehicles) {

    }

    public void run() {

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
    private static boolean vehicleCmp(Pair<Vehicle, Double> a, Pair<Vehicle, Double> b) {
        return a.getValue() > b.getValue();
    }

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

    private void vehicleControl(Vehicle vehicle, List<Pair<Vehicle, Double>> buffer) {

    }

    private void planRoute() {

    }

    private void getAction() {

    }

    private void updateAction() {

    }

    private void updateLocation() {

    }

    private void updateLeaderAndGap() {

    }

    private void handleWaiting() {

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

    }

    private List<Vehicle> getRunningVehicle() {
        return null;
    }

    public Engine() {
        rnd = new Random();
        roadNet = new RoadNet();
        flows = new ArrayList<>();
    }

    public Engine(String configFile, int threadNum) {
        roadNet = new RoadNet();
        flows = new ArrayList<>();
    }

    public boolean checkPriority(int priority) {
        return true;
    }

    public void nextStep() {

    }

    public void reset(boolean resetRnd) {

    }

    public double getCurrentTime() { // 当前时间
        return step * interval;
    }

    public void pushVehicle(Vehicle vehicle, boolean pushToDrivable) {

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

    public void setRoadNet(RoadNet roadNet) {
        this.roadNet = roadNet;
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

    public Random getRnd() {
        return rnd;
    }

    public void setRnd(Random rnd) {
        this.rnd = rnd;
    }

}
