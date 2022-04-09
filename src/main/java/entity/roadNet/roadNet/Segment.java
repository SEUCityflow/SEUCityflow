package entity.roadNet.roadNet;

import entity.vehicle.vehicle.Vehicle;

import java.util.*;

public class Segment {
    private int index;
    private Lane belongLane;
    private double startPos;
    private double endPos;
    private List<Vehicle> vehicles;
    private boolean isGrouped;

    public Segment() {
        vehicles = new LinkedList<>();
    }

    public Segment(int index, Lane belongLane, double startPos, double endPos) {
        this.index = index;
        this.belongLane = belongLane;
        this.startPos = startPos;
        this.endPos = endPos;
        vehicles = new LinkedList<>();
    }

    public double getAverageSpeed() {
        double sum = 0;
        for (Vehicle vehicle : vehicles) {
            sum += vehicle.getSpeed();
        }
        return vehicles.size() == 0 ? -1 : sum / vehicles.size();
    }

    public void disband() {
        if (!isGrouped) {
            return;
        }
        isGrouped = false;
        for (Vehicle vehicle : vehicles) {
            vehicle.setGroupLeader(null);
        }
    }

    public void buildGroup() {
        isGrouped = true;
        Iterator<Vehicle> iterator = vehicles.iterator();
        Vehicle leader = iterator.next();
        leader.setGroupLeader(null);
        while (iterator.hasNext()) {
            Vehicle vehicle = iterator.next();
            vehicle.setGroupLeader(leader);
        }
    }

    public boolean canGroup() {
        double lengthToEnd = belongLane.length - endPos;
        double averageSpeed = getAverageSpeed();
        return index != 0 && lengthToEnd >= 30 && vehicles.size() != 0 && averageSpeed <= belongLane.getMaxSpeed() * Vehicle.Alpha;
    }

    public double getStartPos() {
        return startPos;
    }

    public double getEndPos() {
        return endPos;
    }

    public int getIndex() {
        return index;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
    }

    public void insertVehicle(Vehicle vehicle) {
        ListIterator<Vehicle> iterator = vehicles.listIterator();
        while (iterator.hasNext()) {
            Vehicle nowVehicle = iterator.next();
            if (vehicle.getCurDis() > nowVehicle.getCurDis()) {
                iterator.previous();
                iterator.add(vehicle);
                return;
            }
        }
        iterator.add(vehicle);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Lane getBelongLane() {
        return belongLane;
    }

    public void setBelongLane(Lane belongLane) {
        this.belongLane = belongLane;
    }

    public void setStartPos(double startPos) {
        this.startPos = startPos;
    }

    public void setEndPos(double endPos) {
        this.endPos = endPos;
    }

    public boolean isGrouped() {
        return isGrouped;
    }

    public void setGrouped(boolean grouped) {
        isGrouped = grouped;
    }
}
