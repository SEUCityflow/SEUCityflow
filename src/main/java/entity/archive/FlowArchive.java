package entity.archive;

import com.alibaba.fastjson.annotation.JSONField;

class FlowArchive {
    @JSONField(name = "flowId", ordinal = 1)
    private String flowId;
    @JSONField(name = "nowTime", ordinal = 2)
    private double nowTime;
    @JSONField(name = "currentTime", ordinal = 3)
    private double currentTime;
    @JSONField(name = "cnt", ordinal = 4)
    private int cnt;

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
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

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }
}
