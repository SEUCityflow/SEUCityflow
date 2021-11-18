package entity.flow;

import entity.engine.Engine;
import entity.vehicle.vehicle.VehicleInfo;

public class Flow {
    private VehicleInfo vehicleTemplate;
    private Route route;
    private double interval;
    private double nowTime;
    private double currentTime;
    private int startTime;
    private int endTime;
    private int cnt;
    private Engine engine;
    private String id;
    private boolean valid;

    public Flow(VehicleInfo vehicleTemplate, double timeInterval, Engine engine, int startTime, int endTime, String id) {

    }

    public void nextStep() {

    }

    public void reset() {

    }

    public boolean isValid() {
        return valid;
    }

    // set / get
    public VehicleInfo getVehicleTemplate() {
        return vehicleTemplate;
    }

    public void setVehicleTemplate(VehicleInfo vehicleTemplate) {
        this.vehicleTemplate = vehicleTemplate;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public double getInterval() {
        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }

    public double getNowTime() {
        return nowTime;
    }

    public void setNowTime(double nowTime) {
        this.nowTime = nowTime;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
