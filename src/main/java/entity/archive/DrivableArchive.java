package entity.archive;

import com.alibaba.fastjson.annotation.JSONField;
import entity.History.HistoryRecord;
import entity.vehicle.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;

class DrivableArchive {
    @JSONField(name = "drivableId", ordinal = 1)
    private String drivableId;
    @JSONField(name = "vehicles", ordinal = 2)
    private List<String> vehicles;
    @JSONField(name = "waitingBuffer", ordinal = 3)
    private List<String> waitingBuffer;
    @JSONField(name = "history", ordinal = 4)
    private List<HistoryRecord> history;
    @JSONField(name = "historyVehicleNum", ordinal = 5)
    private int historyVehicleNum;
    @JSONField(name = "historyAverageSpeed", ordinal = 6)
    private double historyAverageSpeed;
    @JSONField(name = "queueingTimeList", ordinal = 7)
    private List<Double> queueingTimeList;
    @JSONField(name = "periodTimeList", ordinal = 8)
    private List<Double> periodTimeList;
    @JSONField(name = "sumQueueingTime", ordinal = 9)
    private double sumQueueingTime;
    @JSONField(name = "sumPeriodTime", ordinal = 10)
    private double sumPeriodTime;

    public DrivableArchive() {
        vehicles = new ArrayList<>();
        waitingBuffer = new ArrayList<>();
        history = new ArrayList<>();
    }

    public String getDrivableId() {
        return drivableId;
    }

    public void setDrivableId(String drivableId) {
        this.drivableId = drivableId;
    }

    public List<String> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<String> vehicles) {
        this.vehicles = vehicles;
    }

    public List<String> getWaitingBuffer() {
        return waitingBuffer;
    }

    public void setWaitingBuffer(List<String> waitingBuffer) {
        this.waitingBuffer = waitingBuffer;
    }

    public List<HistoryRecord> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryRecord> history) {
        this.history = history;
    }

    public int getHistoryVehicleNum() {
        return historyVehicleNum;
    }

    public void setHistoryVehicleNum(int historyVehicleNum) {
        this.historyVehicleNum = historyVehicleNum;
    }

    public double getHistoryAverageSpeed() {
        return historyAverageSpeed;
    }

    public void setHistoryAverageSpeed(double historyAverageSpeed) {
        this.historyAverageSpeed = historyAverageSpeed;
    }

    public List<Double> getQueueingTimeList() {
        return queueingTimeList;
    }

    public void setQueueingTimeList(List<Double> queueingTimeList) {
        this.queueingTimeList = queueingTimeList;
    }

    public List<Double> getPeriodTimeList() {
        return periodTimeList;
    }

    public void setPeriodTimeList(List<Double> periodTimeList) {
        this.periodTimeList = periodTimeList;
    }

    public double getSumQueueingTime() {
        return sumQueueingTime;
    }

    public void setSumQueueingTime(double sumQueueingTime) {
        this.sumQueueingTime = sumQueueingTime;
    }

    public double getSumPeriodTime() {
        return sumPeriodTime;
    }

    public void setSumPeriodTime(double sumPeriodTime) {
        this.sumPeriodTime = sumPeriodTime;
    }
}
