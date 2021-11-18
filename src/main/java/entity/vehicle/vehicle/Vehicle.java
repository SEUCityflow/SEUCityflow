package entity.vehicle.vehicle;

import entity.engine.Engine;
import entity.flow.Flow;
import entity.flow.Route;
import entity.roadNet.roadNet.Drivable;
import entity.roadNet.roadNet.LaneLink;
import entity.roadNet.roadNet.Road;
import entity.vehicle.Router.Router;
import javafx.util.Pair;
import util.ControlInfo;
import util.Point;

import java.util.List;
import java.util.Random;

class Buffer {
    boolean isDisSet;
    boolean isSpeedSet;
    boolean isDrivableSet;
    boolean isNotifiedVehicles;
    boolean isEndSet;
    boolean isEnterLaneLinkTimeSet;
    boolean isBlockerSet;
    boolean isCustomSpeedSet;

    double dis;
    double speed;
    Drivable drivable;
    List<Vehicle> notifiedVehicles;
    boolean end;
    int enterLaneLinkTime;
    Vehicle blocker;
    double customSpeed;
    double deltaDis;
}

class ControllerInfo {
    double dis;
    Drivable drivable;
    Drivable prevDrivable;
    double approachingIntersectionDistance;
    double gap;
    int enterLaneLinkTime;
    Vehicle leader;
    Vehicle blocker;
    boolean end;
    boolean running;
    Router router;
    ControllerInfo(Vehicle vehicle, Route route, Random rnd) {

    }
    ControllerInfo(Vehicle vehicle, ControllerInfo other) {

    }
}

public class Vehicle {
    private VehicleInfo vehicleInfo;
    private Buffer buffer;
    private ControllerInfo controllerInfo;
    private int priority;
    private String id;
    private double enterTime;
    private Engine engine;
    private boolean routeValid;
    private Flow flow;

    public Vehicle(Vehicle vehicle, Flow flow) {

    }

    public Vehicle (Vehicle vehicle, String id, Engine engine, Flow flow) {

    }

    public Vehicle (VehicleInfo init, String id, Engine engine, Flow flow) {

    }

    public void setDeltaDistance(double dis) {

    }

    public Drivable getChangeDrivable() {
        return null;
    }

    public Point getPoint() {
        return null;
    }

    public void update() {

    }

    public Pair<Point, Point> getCurPos() {
        return null;
    }

    public void updateLeaderAndGap(Vehicle leader) {

    }

    public double getNoCollisionSpeed(double vL, double dL, double vF, double dF, double gap, double interval, double targetGap) {
        return 0;
    }

    public double getCarFollowSpeed(double interval) {
        return 0;
    }

    public double getStopBeforeSpeed(double distance, double interval){
        return 0;
    }

    public int getReachSteps(double distance, double targetSpeed, double acc) {
        return 0;
    }

    public int getReachStepsOnLaneLink(double distance, LaneLink laneLink) {
        return 0;
    }

    public double getDistanceUntilSpeed(double speed, double acc) {
        return 0;
    }

    public boolean canYield(double dist) {
        return false;
    }

    public double getBrakeDistanceAfterAccel(double acc, double dec, double interval) {
        return 0;
    }

    public ControlInfo getNextSpeed(double interval) {
        return null;
    }

    public double getIntersectionRelatedSpeed(double interval) {
        return 0;
    }

    public Road getFirstRoad() {
        return null;
    }

    public void setFirstDrivable() {

    }

    public void updateRoute() {

    }

    public void changeRoute(List<Road> anchor) {

    }

    public Drivable getNextDrivable(int i) {
        return null;
    }

    public double getMinBrakeDistance() {
        return 0;
    }

    public double getUsualBrakeDistance() {
        return 0;
    }

    public boolean isRunning() {
        return false;
    }

    public boolean hasDeadlock() {
        Vehicle fastPointer = this;
        Vehicle slowPointer = this;
        while (fastPointer != null && fastPointer.getCurBlocker() != null) {
            slowPointer = slowPointer.getCurBlocker();
            fastPointer = fastPointer.getCurBlocker().getCurBlocker();
            if (slowPointer == fastPointer) { // foeVehicle 存在死锁
                // deadlock detected
                return true; // foeVehicle 死锁不可动，当前 Vehicle 通行
            }
        }
        return false;
    }

    // 自身 set / get
    public VehicleInfo getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(VehicleInfo vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }

    public ControllerInfo getControllerInfo() {
        return controllerInfo;
    }

    public void setControllerInfo(ControllerInfo controllerInfo) {
        this.controllerInfo = controllerInfo;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(double enterTime) {
        this.enterTime = enterTime;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public boolean isRouteValid() {
        return routeValid;
    }

    public void setRouteValid(boolean routeValid) {
        this.routeValid = routeValid;
    }

    public Flow getFlow() {
        return flow;
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    // buffer set / get
    public double getBufferDis() {
        return buffer.dis;
    }

    public void setBufferDis(double dis) {
        buffer.dis = dis;
        buffer.isDisSet = true;
    }

    public double getBufferSpeed() {
        return buffer.speed;
    }

    public void setBufferSpeed(double speed) {
        buffer.speed = speed;
        buffer.isSpeedSet = true;
    }

    public Drivable getBufferDrivable() {
        return buffer.drivable;
    }

    public void setBufferDrivable(Drivable drivable) {
        buffer.drivable = drivable;
        buffer.isDrivableSet = true;
    }

    public void unSetBufferDrivable() {
        buffer.isDrivableSet = false;
    }

    public List<Vehicle> getBufferNotifiedVehicles() {
        return buffer.notifiedVehicles;
    }

    public void setBufferNotifiedVehicles(List<Vehicle> notifiedVehicles) {
        buffer.notifiedVehicles = notifiedVehicles;
        buffer.isNotifiedVehicles= true;
    }

    public boolean isBufferEnd() {
        return buffer.end;
    }

    public void setBufferEnd(boolean end) {
        buffer.end = end;
        buffer.isEndSet = true;
    }

    public void unSetBufferEnd() {
        buffer.isEndSet = false;
    }

    public int getBufferEnterLaneLinkTime() {
        return buffer.enterLaneLinkTime;
    }

    public void setBufferEnterLaneLinkTime(int enterLaneLinkTime) {
        buffer.enterLaneLinkTime = enterLaneLinkTime;
        buffer.isEnterLaneLinkTimeSet = true;
    }

    public Vehicle getBufferBlocker() {
        return buffer.blocker;
    }

    public void setBufferBlocker(Vehicle blocker) {
        buffer.blocker = blocker;
        buffer.isBlockerSet = true;
    }

    public double getBufferCustomSpeed() {
        return buffer.customSpeed;
    }

    public void setBufferCustomSpeed(double customSpeed) {
        buffer.customSpeed = customSpeed;
        buffer.isCustomSpeedSet = true;
    }

    public double getBufferDeltaDis() {
        return buffer.deltaDis;
    }

    public void setBufferDeltaDis(double deltaDis) {
        buffer.deltaDis = deltaDis;
    }

    public boolean hasSetDis() {
        return buffer.isDisSet;
    }

    public boolean hasSetSpeed() {
        return buffer.isSpeedSet;
    }

    public boolean hasSetDrivable() {
        return buffer.isDrivableSet;
    }

    public boolean hasNotifiedVehicles() {
        return buffer.isNotifiedVehicles;
    }

    public boolean hasSetEnd() {
        return buffer.isEndSet;
    }

    public boolean hasSetEnterLaneLinkTime() {
        return buffer.isEnterLaneLinkTimeSet;
    }

    public boolean hasSetBlocker() {
        return buffer.isBlockerSet;
    }

    public boolean hasSetCustomSpeed() {
        return buffer.isCustomSpeedSet;
    }

    // ControllerInfo set / get
    public double getCurDis() {
        return controllerInfo.dis;
    }

    public void setCurDis(double dis) {
        controllerInfo.dis = dis;
    }

    public Drivable getCurDrivable() {
        return controllerInfo.drivable;
    }

    public void setCurDrivable(Drivable drivable) {
        controllerInfo.drivable = drivable;
    }

    public Drivable getPrevDrivable() {
        return controllerInfo.prevDrivable;
    }

    public void setPrevDrivable(Drivable prevDrivable) {
        controllerInfo.prevDrivable = prevDrivable;
    }

    public double getApproachingIntersectionDistance() {
        return controllerInfo.approachingIntersectionDistance;
    }

    public void setApproachingIntersectionDistance(double approachingIntersectionDistance) {
        controllerInfo.approachingIntersectionDistance = approachingIntersectionDistance;
    }

    public double getCurGap() {
        return controllerInfo.gap;
    }

    public void setCurGap(double gap) {
        controllerInfo.gap = gap;
    }

    public int getEnterLaneLinkTime() {
        return controllerInfo.enterLaneLinkTime;
    }

    public void setEnterLaneLinkTime(int enterLaneLinkTime) {
        controllerInfo.enterLaneLinkTime = enterLaneLinkTime;
    }

    public Vehicle getCurLeader() {
        return controllerInfo.leader;
    }

    public void setCurLeader(Vehicle leader) {
        controllerInfo.leader = leader;
    }

    public Vehicle getCurBlocker() {
        return controllerInfo.blocker;
    }

    public void setCurBlocker(Vehicle blocker) {
        controllerInfo.blocker = blocker;
    }

    public boolean isCurEnd() {
        return controllerInfo.end;
    }

    public void setCurEnd(boolean end) {
        controllerInfo.end = end;
    }

    public boolean isCurRunning() {
        return controllerInfo.running;
    }

    public void setCurRunning(boolean running) {
        controllerInfo.running = running;
    }

    public Router getCurRouter() {
        return controllerInfo.router;
    }

    public void setCurRouter(Router router) {
        controllerInfo.router = router;
    }

    // VehicleInfo set / get
    public double getSpeed() {
        return vehicleInfo.speed;
    }

    public void setSpeed(double speed) {
        vehicleInfo.speed = speed;
    }

    public double getLen() {
        return vehicleInfo.len;
    }

    public void setLen(double len) {
        vehicleInfo.len = len;
    }

    public double getWidth() {
        return vehicleInfo.width;
    }

    public void setWidth(double width) {
        vehicleInfo.width = width;
    }

    public double getMaxPosAcc() {
        return vehicleInfo.maxPosAcc;
    }

    public void setMaxPosAcc(double maxPosAcc) {
        vehicleInfo.maxPosAcc = maxPosAcc;
    }

    public double getMaxNegAcc() {
        return vehicleInfo.maxNegAcc;
    }

    public void setMaxNegAcc(double maxNegAcc) {
        vehicleInfo.maxNegAcc = maxNegAcc;
    }

    public double getUsualPosAcc() {
        return vehicleInfo.usualPosAcc;
    }

    public void setUsualPosAcc(double usualPosAcc) {
        vehicleInfo.usualPosAcc = usualPosAcc;
    }

    public double getUsualNegAcc() {
        return vehicleInfo.usualNegAcc;
    }

    public void setUsualNegAcc(double usualNegAcc) {
        vehicleInfo.usualNegAcc = usualNegAcc;
    }

    public double getMinGap() {
        return vehicleInfo.minGap;
    }

    public void setMinGap(double minGap) {
        vehicleInfo.minGap = minGap;
    }

    public double getMaxSpeed() {
        return vehicleInfo.maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        vehicleInfo.maxSpeed = maxSpeed;
    }

    public double getHeadwayTime() {
        return vehicleInfo.headwayTime;
    }

    public void setHeadwayTime(double headwayTime) {
        vehicleInfo.headwayTime = headwayTime;
    }

    public double getYieldDistance() {
        return vehicleInfo.yieldDistance;
    }

    public void setYieldDistance(double yieldDistance) {
        vehicleInfo.yieldDistance = yieldDistance;
    }

    public double getTurnSpeed() {
        return vehicleInfo.turnSpeed;
    }

    public void setTurnSpeed(double turnSpeed) {
        vehicleInfo.turnSpeed = turnSpeed;
    }

    public Route getRoute() {
        return vehicleInfo.route;
    }

    public void setRoute(Route route) {
        vehicleInfo.route = route;
    }
}
