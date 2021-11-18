package entity.roadNet.roadNet;

import entity.vehicle.vehicle.Vehicle;

import java.util.Iterator;
import java.util.List;

public class Segment {
    private int index;
    private Lane belongLane;
    private double startPos;
    private double endPos;
    private List<Iterator<List<Vehicle>>> vehicles;
    private Iterator<List<Vehicle>> prev_vehicle_iter;
    public Segment() {

    }
    public Segment(int index, Lane belongLane, double startPos, double endPos) {

    }
    public double getStartPos() {
        return 0;
    }
    public double getEndPos() {
        return 0;
    }
    public int getIndex() {
        return 0;
    }
    public List<Iterator<List<Vehicle>>> getVehicles() {
        return null;
    }
    public Iterator<List<Vehicle>> findVehicle(Vehicle vehicle) {
        return null;
    }
    public void removeVehicle(Vehicle vehicle) {

    }
    public void insertVehicle(List<Iterator<List<Vehicle>>> vehicle) {

    }
}
