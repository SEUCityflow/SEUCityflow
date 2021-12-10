package entity.archive;

import com.alibaba.fastjson.annotation.JSONField;

class TrafficLightArchive {
    @JSONField(name = "trafficId", ordinal = 1)
    private String trafficId;
    @JSONField(name = "remainDuration", ordinal = 2)
    private double remainDuration;
    @JSONField(name = "curPhaseIndex", ordinal = 3)
    private int curPhaseIndex;

    public String getTrafficId() {
        return trafficId;
    }

    public void setTrafficId(String trafficId) {
        this.trafficId = trafficId;
    }

    public double getRemainDuration() {
        return remainDuration;
    }

    public void setRemainDuration(double remainDuration) {
        this.remainDuration = remainDuration;
    }

    public int getCurPhaseIndex() {
        return curPhaseIndex;
    }

    public void setCurPhaseIndex(int curPhaseIndex) {
        this.curPhaseIndex = curPhaseIndex;
    }
}
