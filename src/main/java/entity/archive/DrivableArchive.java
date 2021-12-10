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
}
