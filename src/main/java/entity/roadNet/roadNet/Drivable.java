package entity.roadNet.roadNet;

import entity.vehicle.vehicle.Vehicle;
import util.Point;

import java.util.List;

enum DrivableType {
    LANE,
    LANELINK
}

public abstract class Drivable {
    protected double averageLength;
    protected double width;
    protected double maxSpeed;
    protected List<Vehicle> vehicles;
    protected List<Point> points;
    protected DrivableType drivableType;

    public List<Vehicle> getVehicles() {
        return null;
    }
    public double getLength() {
        return 0;
    }

    public double getWidth() {
        return 0;
    }
    public double getMaxSpeed() {
        return 0;
    }
    public int getVehicleCount() {
        return 0;
    }
    public DrivableType getDrivableType() {
        return DrivableType.LANE;
    }
    public boolean isLane() {
        return false;
    }
    public boolean isLaneLink() {
        return false;
    }
    public Vehicle getFirstVehicle() {
        return null;
    }
    public Vehicle getLastVehicle() {
        return null;
    }
    public Point getPointByDistance(double dis) {
        return null;
    }
    public Point getDirectionByDistance(double dis) {
        return null;
    }
    public void pushVehicle(Vehicle vehicle) {

    }
    public void popVehicle() {

    }
    public abstract String getId();
}
