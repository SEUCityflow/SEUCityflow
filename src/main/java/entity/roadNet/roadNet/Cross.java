package entity.roadNet.roadNet;

import entity.vehicle.vehicle.Vehicle;

import java.util.Arrays;

public class Cross {
    private final LaneLink[] laneLinks;
    private final Vehicle[] notifyVehicles;
    private final double[] notifyDistances;
    private final double[] distanceOnLane;
    private double leaveDistance;
    //    private double arriveDistance;
    private double ang;
    private final double[] safeDistances;

    public Cross() {
        laneLinks = new LaneLink[2];
        notifyVehicles = new Vehicle[2];
        notifyDistances = new double[2];
        safeDistances = new double[2];
        distanceOnLane = new double[2];
    }

    public void notify(LaneLink laneLink, Vehicle vehicle, double notifyDistance) {
        int i = (laneLink == laneLinks[0]) ? 0 : 1;
        notifyVehicles[i] = vehicle;
        notifyDistances[i] = notifyDistance;
    }

    // TODO: 考虑体积碰撞
    public boolean canPass(Vehicle vehicle, LaneLink laneLink, double distanceToLaneLinkStart) {
        int i = (laneLink == laneLinks[0]) ? 0 : 1;
        Vehicle foeVehicle = notifyVehicles[1 - i];
        RoadLinkType t1 = laneLinks[i].getRoadLinkType();
        RoadLinkType t2 = laneLinks[1 - i].getRoadLinkType();
        double d1 = distanceOnLane[i] - distanceToLaneLinkStart;
        double d2 = distanceOnLane[1 - i];

        // 无冲突车辆或当前车辆无法停下或冲突车辆已完全通过或冲突车辆死锁无法移动
        if (foeVehicle == null || !vehicle.canYield(d1) || d2 + foeVehicle.getLen() < 0 || foeVehicle.hasDeadlock()) {
            return true;
        }
        boolean canPass = false;
        if (foeVehicle.canYield(d2)) { // 冲突车辆可停
            int foeVehicleReachSteps = foeVehicle.getReachStepsOnLaneLink(d2, laneLinks[1 - i]);
            int reachSteps = vehicle.getReachStepsOnLaneLink(d1, laneLinks[i]);
            if (reachSteps < foeVehicleReachSteps) { // 自己早到
                canPass = true;
            } else if (reachSteps > foeVehicleReachSteps) { // 自己晚到
                canPass = t1.ordinal() > t2.ordinal(); // 交通规则优势
            } else { // 同时到
                if (t1.ordinal() > t2.ordinal()) { // 交通规则优势
                    canPass = true;
                } else if (t1.ordinal() == t2.ordinal()) { // 同优势
                    if (vehicle.getEnterLaneLinkTime() == foeVehicle.getBufferEnterLaneLinkTime()) { // 同时进路口
                        if (d1 == d2) { // 同距离
                            canPass = vehicle.getPriority() > foeVehicle.getPriority(); // 优先级
                        } else {
                            canPass = d1 < d2;
                        }
                    } else {
                        canPass = vehicle.getBufferEnterLaneLinkTime() < foeVehicle.getEnterLaneLinkTime();
                    }
                }
            }
        }
        return canPass;
    }

    public void clearNotify() {
        notifyVehicles[0] = notifyVehicles[1] = null;
    }

    public Vehicle getFoeVehicle(LaneLink laneLink) {
        return laneLink == laneLinks[0] ? notifyVehicles[1] : notifyVehicles[0];
    }

    public double getDistanceByLane(LaneLink laneLink) {
        return laneLink == laneLinks[0] ? distanceOnLane[0] : distanceOnLane[1];
    }

    public double getNotifyDistanceByLane(LaneLink laneLink) {
        return laneLink == laneLinks[0] ? notifyDistances[0] : notifyDistances[1];
    }

    public double getSafeDistanceByLane(LaneLink laneLink) {
        return laneLink == laneLinks[0] ? safeDistances[0] : safeDistances[1];
    }

    public double getAng() {
        return ang;
    }

    public LaneLink getLaneLink(int i) {
        return laneLinks[i];
    }

    public void reset() {}

    public void setLaneLinks(LaneLink l1, LaneLink l2) {
        laneLinks[0] = l1;
        laneLinks[1] = l2;
    }

    public void setNotifyVehicles(Vehicle v1, Vehicle v2) {
        notifyVehicles[0] = v1;
        notifyVehicles[1] = v2;
    }

    public void setNotifyDistances(double d1, double d2) {
        notifyDistances[0] = d1;
        notifyDistances[1] = d2;
    }

    public void setDistanceOnLane(double d1, double d2) {
        distanceOnLane[0] = d2;
        distanceOnLane[1] = d2;
    }

    public void setLeaveDistance(double leaveDistance) {
        this.leaveDistance = leaveDistance;
    }

//    public void setArriveDistance(double arriveDistance) {
//        this.arriveDistance = arriveDistance;
//    }

    public void setAng(double ang) {
        this.ang = ang;
    }

    public void setSafeDistances(double d1, double d2) {
        safeDistances[0] = d1;
        safeDistances[1] = d2;
    }

    public double getDistanceOnLane0() {
        return distanceOnLane[0];
    }

    public double getLeaveDistance() {
        return leaveDistance;
    }
}
