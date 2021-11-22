package entity.flow;

import entity.engine.Engine;
import entity.vehicle.vehicle.Vehicle;
import entity.vehicle.vehicle.VehicleInfo;

public class Flow {
    private VehicleInfo vehicleTemplate;
    private Route route;
    private double interval;
    private double nowTime = 0;
    private double currentTime = 0;
    private int startTime = 0;
    private int endTime = -1;
    private int cnt = 0;
    private Engine engine;
    private String id;
    private boolean valid;

    public Flow(VehicleInfo vehicleTemplate, double timeInterval, Engine engine, int startTime, int endTime, String id) {
        this.vehicleTemplate = vehicleTemplate;
        interval = timeInterval;
        this.engine = engine;
        this.startTime = startTime;
        this.endTime = endTime;
        this.id = id;
        assert (timeInterval >= 1 || (startTime == endTime));
        nowTime = interval;
    }

    public Flow() {
        vehicleTemplate = new VehicleInfo();
        route = new Route();
    }

    // 每个 timeInterval 对 flow 进行的操作
    public void nextStep(double timeInterval) {
        if (!valid)                            // route 不可达
            return;
        if (endTime != -1 && currentTime > endTime) // 未结束
            return;
        if (currentTime >= startTime) {   // 可开始
            while (nowTime >= interval) { // 距此 flow 上次进入 RoadNet 已超过 interval，可根据此 flow 再初始化一辆车放入
                Vehicle vehicle = new Vehicle(vehicleTemplate, id + "_" + Integer.toString(cnt++), engine, this);
                // priority has been set correctlly before?
                int priority = vehicle.getPriority();
                while (engine.checkPriority(priority))
                    priority = engine.getRnd().nextInt();
                vehicle.setPriority(priority);
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
        if (this.valid && !valid)
            System.err.println("[warning] Invalid route '" + id + "'. Omitted by default.");
        this.valid = valid;
    }
}
