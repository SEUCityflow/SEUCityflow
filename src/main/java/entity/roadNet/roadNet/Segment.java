package entity.roadNet.roadNet;

import entity.vehicle.vehicle.Vehicle;

import java.util.*;

public class Segment {
    private int index;
    private Lane belongLane;
    private double startPos;
    private double endPos;
    private List<Vehicle> vehicles;

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

}
