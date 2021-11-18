package entity.engine;

import entity.flow.Flow;
import entity.roadNet.roadNet.Drivable;
import entity.roadNet.roadNet.Intersection;
import entity.roadNet.roadNet.Road;
import entity.roadNet.roadNet.RoadNet;
import entity.vehicle.vehicle.Vehicle;
import javafx.util.Pair;
import util.Barrier;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
    // private rapidjson::Document jsonRoot; java 读取 json 的类暂时未找
    private String stepLog;
    private int step;
    private int activeVehicleCount;
    private int seed;
    private Barrier startBarrier;
    private Barrier endBarrier;
    private boolean finished;
    private String dir;
    // private ofStream logOut; java 输出流暂未看

    private boolean rlTrafficLight;
    private boolean laneChange;
    private int finishedVehicleCnt;
    private double cumulativeTravelTime;
    private Random rnd;

    private boolean loadRoadNet(String jsonFile) {
        return true;
    }

    private boolean loadFlow(String jsonFilename) {
        return true;
    }

    private boolean loadConfig(Engine engine, String configFile) {
        return true;
    }

    private void setLogFile(String jsonFile, String logFile) {

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

    private void updateLog() {

    }

    private void checkWarning() {

    }

    private void notifyCross() {

    }

    public Engine(String configFile, int threadNum) {

    }

    public boolean checkPriority(int priority) {
        return true;
    }

    public void nextStep() {

    }

    public void reset(boolean resetRnd) {

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
