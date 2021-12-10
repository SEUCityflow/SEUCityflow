package entity.archive;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

class VehicleInfoArchive {
    @JSONField(name = "speed", ordinal = 1)
    private double speed = 0;
    @JSONField(name = "len", ordinal = 2)
    private double len = 5;
    @JSONField(name = "width", ordinal = 3)
    private double width = 2;
    @JSONField(name = "maxPosAcc", ordinal = 4)
    private double maxPosAcc = 4.5;
    @JSONField(name = "maxNegAcc", ordinal = 5)
    private double maxNegAcc = 4.5;
    @JSONField(name = "usualPosAcc", ordinal = 6)
    private double usualPosAcc = 2.5;
    @JSONField(name = "usualNegAcc", ordinal = 7)
    private double usualNegAcc = 2.5;
    @JSONField(name = "minGap", ordinal = 8)
    private double minGap = 2;
    @JSONField(name = "maxSpeed", ordinal = 9)
    private double maxSpeed = 16.66667;
    @JSONField(name = "headwayTime", ordinal = 10)
    private double headwayTime = 1;
    @JSONField(name = "yieldDistance", ordinal = 11)
    private double yieldDistance = 5;
    @JSONField(name = "turnSpeed", ordinal = 12)
    private double turnSpeed = 8.3333;
    @JSONField(name = "routeIds", ordinal = 13)
    private List<String> routeIds;

    public VehicleInfoArchive() {
        routeIds = new ArrayList<>();
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getLen() {
        return len;
    }

    public void setLen(double len) {
        this.len = len;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getMaxPosAcc() {
        return maxPosAcc;
    }

    public void setMaxPosAcc(double maxPosAcc) {
        this.maxPosAcc = maxPosAcc;
    }

    public double getMaxNegAcc() {
        return maxNegAcc;
    }

    public void setMaxNegAcc(double maxNegAcc) {
        this.maxNegAcc = maxNegAcc;
    }

    public double getUsualPosAcc() {
        return usualPosAcc;
    }

    public void setUsualPosAcc(double usualPosAcc) {
        this.usualPosAcc = usualPosAcc;
    }

    public double getUsualNegAcc() {
        return usualNegAcc;
    }

    public void setUsualNegAcc(double usualNegAcc) {
        this.usualNegAcc = usualNegAcc;
    }

    public double getMinGap() {
        return minGap;
    }

    public void setMinGap(double minGap) {
        this.minGap = minGap;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getHeadwayTime() {
        return headwayTime;
    }

    public void setHeadwayTime(double headwayTime) {
        this.headwayTime = headwayTime;
    }

    public double getYieldDistance() {
        return yieldDistance;
    }

    public void setYieldDistance(double yieldDistance) {
        this.yieldDistance = yieldDistance;
    }

    public double getTurnSpeed() {
        return turnSpeed;
    }

    public void setTurnSpeed(double turnSpeed) {
        this.turnSpeed = turnSpeed;
    }

    public List<String> getRouteIds() {
        return routeIds;
    }

    public void setRouteIds(List<String> routeIds) {
        this.routeIds = routeIds;
    }
}

class ControllerInfoArchive {
    @JSONField(name = "dis", ordinal = 1)
    private double dis;
    @JSONField(name = "drivableId", ordinal = 2)
    private String drivableId;
    @JSONField(name = "prevDrivableId", ordinal = 3)
    private String prevDrivableId;
    @JSONField(name = "approachingIntersectionDistance", ordinal = 4)
    private double approachingIntersectionDistance;
    @JSONField(name = "gap", ordinal = 5)
    private double gap;
    @JSONField(name = "enterLaneLinkTime", ordinal = 6)
    private int enterLaneLinkTime;
    @JSONField(name = "leaderId", ordinal = 7)
    private String leaderId;
    @JSONField(name = "blockerId", ordinal = 8)
    private String blockerId;
    @JSONField(name = "end", ordinal = 9)
    private boolean end;
    @JSONField(name = "running", ordinal = 10)
    private boolean running;

    public ControllerInfoArchive() {

    }

    public double getDis() {
        return dis;
    }

    public void setDis(double dis) {
        this.dis = dis;
    }

    public String getDrivableId() {
        return drivableId;
    }

    public void setDrivableId(String drivableId) {
        this.drivableId = drivableId;
    }

    public String getPrevDrivableId() {
        return prevDrivableId;
    }

    public void setPrevDrivableId(String prevDrivableId) {
        this.prevDrivableId = prevDrivableId;
    }

    public double getApproachingIntersectionDistance() {
        return approachingIntersectionDistance;
    }

    public void setApproachingIntersectionDistance(double approachingIntersectionDistance) {
        this.approachingIntersectionDistance = approachingIntersectionDistance;
    }

    public double getGap() {
        return gap;
    }

    public void setGap(double gap) {
        this.gap = gap;
    }

    public int getEnterLaneLinkTime() {
        return enterLaneLinkTime;
    }

    public void setEnterLaneLinkTime(int enterLaneLinkTime) {
        this.enterLaneLinkTime = enterLaneLinkTime;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public String getBlockerId() {
        return blockerId;
    }

    public void setBlockerId(String blockerId) {
        this.blockerId = blockerId;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}

class LaneChangeInfoArchive {
    @JSONField(name = "partnerType", ordinal = 1)
    private int partnerType;
    @JSONField(name = "partnerId", ordinal = 2)
    private String partnerId;
    @JSONField(name = "offSet", ordinal = 3)
    private double offSet;
    @JSONField(name = "segmentIndex", ordinal = 4)
    private int segmentIndex;

    public LaneChangeInfoArchive() {

    }

    public int getPartnerType() {
        return partnerType;
    }

    public void setPartnerType(int partnerType) {
        this.partnerType = partnerType;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public double getOffSet() {
        return offSet;
    }

    public void setOffSet(double offSet) {
        this.offSet = offSet;
    }

    public int getSegmentIndex() {
        return segmentIndex;
    }

    public void setSegmentIndex(int segmentIndex) {
        this.segmentIndex = segmentIndex;
    }
}

class LaneChangeArchive {
    @JSONField(name = "laneChangeUrgency", ordinal = 1)
    private int laneChangeUrgency = -Integer.MAX_VALUE;
    @JSONField(name = "laneChangeDirection", ordinal = 2)
    private int laneChangeDirection;
    @JSONField(name = "laneChangeTargetId", ordinal = 3)
    private String laneChangeTargetId;
    @JSONField(name = "laneChangeRecvId", ordinal = 4)
    private String laneChangeRecvId;
    @JSONField(name = "laneChangeLeaderId", ordinal = 5)
    private String laneChangeLeaderId;
    @JSONField(name = "laneChangeFollowerId", ordinal = 6)
    private String laneChangeFollowerId;
    @JSONField(name = "laneChangeWaitingTime", ordinal = 7)
    private double laneChangeWaitingTime;
    @JSONField(name = "laneChanging", ordinal = 8)
    private boolean laneChanging;
    @JSONField(name = "laneChangeLastTime", ordinal = 9)
    private double laneChangeLastTime;

    public LaneChangeArchive() {

    }

    public int getLaneChangeUrgency() {
        return laneChangeUrgency;
    }

    public void setLaneChangeUrgency(int laneChangeUrgency) {
        this.laneChangeUrgency = laneChangeUrgency;
    }

    public int getLaneChangeDirection() {
        return laneChangeDirection;
    }

    public void setLaneChangeDirection(int laneChangeDirection) {
        this.laneChangeDirection = laneChangeDirection;
    }

    public String getLaneChangeTargetId() {
        return laneChangeTargetId;
    }

    public void setLaneChangeTargetId(String laneChangeTargetId) {
        this.laneChangeTargetId = laneChangeTargetId;
    }

    public String getLaneChangeRecvId() {
        return laneChangeRecvId;
    }

    public void setLaneChangeRecvId(String laneChangeRecvId) {
        this.laneChangeRecvId = laneChangeRecvId;
    }

    public String getLaneChangeLeaderId() {
        return laneChangeLeaderId;
    }

    public void setLaneChangeLeaderId(String laneChangeLeaderId) {
        this.laneChangeLeaderId = laneChangeLeaderId;
    }

    public String getLaneChangeFollowerId() {
        return laneChangeFollowerId;
    }

    public void setLaneChangeFollowerId(String laneChangeFollowerId) {
        this.laneChangeFollowerId = laneChangeFollowerId;
    }

    public double getLaneChangeWaitingTime() {
        return laneChangeWaitingTime;
    }

    public void setLaneChangeWaitingTime(double laneChangeWaitingTime) {
        this.laneChangeWaitingTime = laneChangeWaitingTime;
    }

    public boolean isLaneChanging() {
        return laneChanging;
    }

    public void setLaneChanging(boolean laneChanging) {
        this.laneChanging = laneChanging;
    }

    public double getLaneChangeLastTime() {
        return laneChangeLastTime;
    }

    public void setLaneChangeLastTime(double laneChangeLastTime) {
        this.laneChangeLastTime = laneChangeLastTime;
    }
}

public class VehicleArchive {
    @JSONField(name = "vehicleInfoArchive", ordinal = 4)
    private VehicleInfoArchive vehicleInfoArchive;
    @JSONField(name = "controllerInfoArchive", ordinal = 5)
    private ControllerInfoArchive controllerInfoArchive;
    @JSONField(name = "laneChangeInfoArchive", ordinal = 6)
    private LaneChangeInfoArchive laneChangeInfoArchive;
    @JSONField(name = "laneChangeArchive", ordinal = 7)
    private LaneChangeArchive laneChangeArchive;
    @JSONField(name = "priority", ordinal = 1)
    private int priority;
    @JSONField(name = "id", ordinal = 2)
    private String id;
    @JSONField(name = "enterTime", ordinal = 3)
    private double enterTime;

    public VehicleArchive() {
        vehicleInfoArchive = new VehicleInfoArchive();
        controllerInfoArchive = new ControllerInfoArchive();
        laneChangeArchive = new LaneChangeArchive();
        laneChangeInfoArchive = new LaneChangeInfoArchive();
    }

    public VehicleInfoArchive getVehicleInfoArchive() {
        return vehicleInfoArchive;
    }

    public void setVehicleInfoArchive(VehicleInfoArchive vehicleInfoArchive) {
        this.vehicleInfoArchive = vehicleInfoArchive;
    }

    public ControllerInfoArchive getControllerInfoArchive() {
        return controllerInfoArchive;
    }

    public void setControllerInfoArchive(ControllerInfoArchive controllerInfoArchive) {
        this.controllerInfoArchive = controllerInfoArchive;
    }

    public LaneChangeInfoArchive getLaneChangeInfoArchive() {
        return laneChangeInfoArchive;
    }

    public void setLaneChangeInfoArchive(LaneChangeInfoArchive laneChangeInfoArchive) {
        this.laneChangeInfoArchive = laneChangeInfoArchive;
    }

    public LaneChangeArchive getLaneChangeArchive() {
        return laneChangeArchive;
    }

    public void setLaneChangeArchive(LaneChangeArchive laneChangeArchive) {
        this.laneChangeArchive = laneChangeArchive;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(double enterTime) {
        this.enterTime = enterTime;
    }
}
