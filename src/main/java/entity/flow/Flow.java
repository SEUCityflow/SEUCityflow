package entity.flow;

import entity.engine.Engine;
import entity.vehicle.vehicle.Vehicle;
import entity.vehicle.vehicle.VehicleInfo;

public class Flow {
    private VehicleInfo vehicleTemplate; // 基本车辆信息
    private Route route; // anchor point
    private double interval; // 进入 roadnet 间隔
    private double nowTime = 0; // 当前累计时间
    private double currentTime = 0; // 当前时间
    private int startTime = 0; // 开始进入时间
    private int endTime = -1; // 结束进入时间
    private int cnt = 0; // 累计进入次数
    private Engine engine;
    private String id;
    private boolean valid = true; // route 可达

    public Flow() {
        vehicleTemplate = new VehicleInfo();
        route = new Route();
    }

    // 每个 timeInterval 对 flow 进行的操作
    public void nextStep(double timeInterval) {
        if (!valid || endTime != -1 && currentTime > endTime) {// route 不可达或已结束
            return;
        }
        if (currentTime >= startTime) {   // 可开始
            while (nowTime >= interval) { // 距此 flow 上次进入 RoadNet 已超过 interval，可根据此 flow 再初始化一辆车放入
                Vehicle vehicle = new Vehicle(vehicleTemplate, id + "_" + cnt++, engine, this);
                engine.pushVehicle(vehicle, false);
                vehicle.getFirstRoad().addPlanRouteVehicle(vehicle);
                nowTime -= interval;
            }
            nowTime += timeInterval; // 累积时间
        }
        currentTime += timeInterval; // 时间同步
    }

    public void reset() {
        nowTime = interval;
        currentTime = 0;
        cnt = 0;
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
        if (this.valid && !valid) {
            System.err.println("[warning] Invalid route '" + id + "'. Omitted by default.");
        }
        this.valid = valid;
    }
}
