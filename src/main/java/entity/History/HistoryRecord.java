package entity.History;

import com.alibaba.fastjson.annotation.JSONField;

public class HistoryRecord {
    @JSONField(name = "vehicleNum", ordinal = 1)
    private int vehicleNum;
    @JSONField(name = "averageSpeed", ordinal = 2)
    private double averageSpeed;

    public HistoryRecord(int vehicleNum, double averageSpeed) {
        this.vehicleNum = vehicleNum;
        this.averageSpeed = averageSpeed;
    }

    public int getVehicleNum() {
        return vehicleNum;
    }

    public void setVehicleNum(int vehicleNum) {
        this.vehicleNum = vehicleNum;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }
}
