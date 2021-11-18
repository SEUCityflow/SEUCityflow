package entity.roadNet.roadNet;

import entity.vehicle.vehicle.Vehicle;
import util.Point;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

enum DrivableType {
    LANE,
    LANELINK
}

public abstract class Drivable {
    protected double length;
    protected double width;
    protected double maxSpeed;
    protected List<Vehicle> vehicles;
    protected List<Point> points;
    protected DrivableType drivableType;

    public abstract String getId();

    public Drivable() {
        vehicles = new LinkedList<Vehicle>();
        points = new ArrayList<Point>();
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public int getVehicleCount() {
        return vehicles.size();
    }

    public DrivableType getDrivableType() {
        return drivableType;
    }

    public boolean isLane() {
        return drivableType == DrivableType.LANELINK;
    }

    public boolean isLaneLink() {
        return drivableType == DrivableType.LANE;
    }

    public Vehicle getFirstVehicle() {
        if (!vehicles.isEmpty()) {
            return vehicles.get(0);
        }
        return null;
    }

    public Vehicle getLastVehicle() {
        if (!vehicles.isEmpty()) {
            return vehicles.get(vehicles.size() - 1);
        }
        return null;
    }

    public Point getPointByDistance(double dis) { // 距 drivable 起点 dis 的点
        return Point.getPointByDistance(points, dis);
    }

    public Point getDirectionByDistance(double dis) { // 距 drivable 起点 dis 处的方向
        double remain = dis;
        for (int i = 0; i + 1 < points.size(); i++) {
            double len = points.get(i + 1).minus(points.get(i)).len();
            if (remain < len) {
                return points.get(i + 1).minus(points.get(i)).unit();
            } else {
                remain -= len;
            }
        }
        return points.get(points.size() - 1).minus(points.get(points.size() - 2)).unit();
    }

    public void pushVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public void popVehicle() {
        vehicles.remove(0);
    }

    public void setLength(double length) {
        this.length = length;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public void setDrivableType(DrivableType drivableType) {
        this.drivableType = drivableType;
    }

    public void reset() {
        vehicles.clear();
    }
}
