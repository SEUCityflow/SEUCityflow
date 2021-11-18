package entity.roadNet.roadNet;

import entity.vehicle.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Segment { // 暂时不动，数据结构需修改
    private int index;
    private Lane belongLane;
    private double startPos;
    private double endPos;
    private List<Vehicle> vehicles;
    private int curPos;

    public Segment() {
        vehicles = new ArrayList<Vehicle>();
    }

    public Segment(int index, Lane belongLane, double startPos, double endPos) {
        this.index = index;
        this.belongLane = belongLane;
        this.startPos = startPos;
        this.endPos = endPos;
        vehicles = new ArrayList<Vehicle>();
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

    public int findVehicle(Vehicle vehicle) {
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i) == vehicle) {
                return i;
            }
        }
        return -1;
    }

    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
    }

    public void insertVehicle(Vehicle vehicle) {
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getCurDis() <= vehicle.getCurDis()) {
                vehicles.add(i, vehicle);
                return;
            }
        }
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

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public int getCurPos() {
        return curPos;
    }

    public void setCurPos(int curPos) {
        this.curPos = curPos;
    }
}
