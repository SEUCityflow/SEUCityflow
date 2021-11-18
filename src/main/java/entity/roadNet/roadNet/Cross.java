package entity.roadNet.roadNet;

import entity.vehicle.vehicle.Vehicle;

public class Cross {
    private LaneLink[] laneLinks;
    private Vehicle[] notifyVehicles;
    private double[] notifyDistances;
    private double leaveDistance;
    private double arriveDistance;
    private double ang;
    private double[] safeDistances;

    public Cross() {

    }

    public void notify(LaneLink laneLink, Vehicle vehicle, double notifyDistance) {

    }
    public boolean canPass(Vehicle vehicle, LaneLink laneLink, double distanceToLaneLinkStart) {
        return false;
    }
    public void clearNotify() {

    }
    public Vehicle getFoeVehicle(LaneLink laneLink) {
        return null;
    }
    public double getDistanceByLane(LaneLink laneLink) {
        return 0;
    }
    public double getNotifyDistanceByLane(LaneLink laneLink) {
        return 0;
    }
    public double getSafeDistanceByLane(LaneLink laneLink) {
        return 0;
    }

    public double getAng() {
        return 0;
    }
    public LaneLink getLaneLink(int i) {
        return null;
    }
    public void reset() {

    }
}
