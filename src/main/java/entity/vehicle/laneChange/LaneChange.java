package entity.vehicle.laneChange;

import entity.roadNet.roadNet.Lane;
import entity.roadNet.roadNet.LaneLink;
import entity.roadNet.roadNet.Segment;
import entity.vehicle.vehicle.Vehicle;

public abstract class LaneChange {

    protected int lastDir;
    protected Signal signalRecv;
    protected Signal signalSend;

    protected Vehicle vehicle;
    protected Vehicle targetLeader;
    protected Vehicle targetFollower;

    protected double leaderGap;
    protected double followerGap;
    protected double waitingTime;
    protected boolean changing;
    protected boolean finished;
    protected double lastChangeTime;
    protected static final double coolingTime = 3;

    public LaneChange(Vehicle vehicle, LaneChange other) {
        lastDir = other.lastDir;
        this.vehicle = vehicle;
        targetLeader = other.targetLeader;
        targetFollower = other.targetFollower;
        leaderGap = other.leaderGap;
        followerGap = other.followerGap;
        waitingTime = other.waitingTime;
        changing = other.changing;
        finished = other.finished;
        lastChangeTime = other.lastChangeTime;
        if (other.signalSend != null) {
            signalSend = new Signal(other.signalSend);
            signalSend.setSource(vehicle);
        }
    }

    public LaneChange(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void updateLeaderAndFollower() {
        targetLeader = targetFollower = null;
        Lane target = signalSend.getTarget();
        targetLeader = target.getVehicleAfterDistance(vehicle.getCurDis(), vehicle.getSegmentIndex()); // 换行后前面的车
        Lane curLane = (Lane) vehicle.getCurDrivable();
        leaderGap = followerGap = Integer.MAX_VALUE;
        if (targetLeader == null) {
            // Find target leader in following lanelinks
            double rest = curLane.getLength() - vehicle.getCurDis();
            leaderGap = rest;
            double gap = Integer.MAX_VALUE;
            for (LaneLink lanelink : signalSend.getTarget().getLaneLinks()) { // targetLane 上所有可通行的 laneLink
                Vehicle leader = lanelink.getLastVehicle();
                if (leader != null && leader.getCurDis() + rest < gap) {
                    gap = leader.getCurDis() + rest;
                    if (gap < leader.getLen()) { // 找到最近的车
                        targetLeader = leader;
                        leaderGap = rest - (leader.getLen() - gap);
                    }
                }
            }
        } else {
            leaderGap = targetLeader.getCurDis() - vehicle.getCurDis() - targetLeader.getLen();
        }

        targetFollower = target.getVehicleBeforeDistance(vehicle.getCurDis(), vehicle.getSegmentIndex());

        if (targetFollower != null) {
            followerGap = vehicle.getCurDis() - targetFollower.getCurDis() - vehicle.getLen();
        } else {
            followerGap = Integer.MAX_VALUE;
        }

    }

    public Lane getTarget() {
        return signalSend != null ? signalSend.getTarget() : (Lane) vehicle.getCurDrivable();
    }

    public Vehicle getTargetLeader() {
        return targetLeader;
    }

    public Vehicle getTargetFollower() {
        return targetFollower;
    }

    public double getGapBefore() {
        return followerGap;
    }

    public double getGapAfter() {
        return leaderGap;
    }

    public void insertShadow(Vehicle shadow) {
        changing = true;
        waitingTime = 0;

        Lane targetLane = signalSend.getTarget();
        int segId = vehicle.getSegmentIndex();
        Segment targetSeg = targetLane.getSegment(segId);

        shadow.setPartner(vehicle); // 相互绑定
        vehicle.setShadow(shadow); // 相互绑定
        shadow.setCurBlocker(null);
        shadow.setCurDrivable(targetLane); // 修改 shadow drivable
        shadow.updateRouter();       // 更新 shadow iCurRoad 与 清除 planned

        targetSeg.insertVehicle(shadow); // 更新 targetLane 对应 segment

        shadow.updateLeaderAndGap(targetLeader); // 更新 shadow 与前车距离
        if (targetFollower != null) {// 更新后车 与 shadow 距离
            targetFollower.updateLeaderAndGap(shadow);
        }
    }

    public void makeSignal(double interval) {
        if (signalSend != null) {
            signalSend.setDirection(getDirection());
        }
    }

    public boolean planChange() {
        return (signalSend != null && signalSend.getTarget() != null && signalSend.getTarget() != vehicle.getCurDrivable()) || changing;
    }

    public boolean canChange() {
        return signalSend != null && signalRecv == null;
    }

    public boolean isGapValid() {
        return getGapAfter() >= getSafeGapAfter() && getGapBefore() >= getSafeGapBefore();
    }

    public void finishChanging() {
        changing = false;
        finished = true;

        Vehicle partner = vehicle.getPartner();
        partner.setLastChangeTime(vehicle.getEngine().getCurrentTime());
        if (!partner.isReal()) {// partner id 修改
            partner.setId(vehicle.getId());
        }
        partner.setPartnerType(0); // 不再是 shadow
        partner.setOffSet(0);
        partner.setPartner(null);
        vehicle.setPartner(null);
        clearSignal();
    }

    public void abortChanging() {
        Vehicle partner = vehicle.getPartner();
        partner.getLaneChange().changing = false;
        partner.setPartnerType(0);
        partner.setOffSet(0);
        partner.setPartner(null);
        clearSignal();
    }

    public int getDirection() {
        if (vehicle.getCurDrivable().isLaneLink() || signalSend == null || signalSend.getTarget() == null) {
            return 0;
        }
        Lane curLane = (Lane) vehicle.getCurDrivable();
        if (signalSend.getTarget() == curLane.getInnerLane()) {
            return -1;
        }
        if (signalSend.getTarget() == curLane.getOuterLane()) {
            return 1;
        }
        return 0;
    }

    public void clearSignal() {
        targetLeader = null;
        targetFollower = null;
        if (signalSend != null) {
            lastDir = signalSend.getDirection();
        } else {
            lastDir = 0;
        }
        if (changing) {
            return;
        }
        signalSend = null;
        signalRecv = null;
    }

    public abstract double yieldSpeed(double interval);

    public abstract void sendSignal();

    public abstract double getSafeGapBefore();

    public abstract double getSafeGapAfter();

    public int getSignalSendUrgency() {
        return signalSend.getUrgency();
    }

    public int getLastDir() {
        return lastDir;
    }

    public void setLastDir(int lastDir) {
        this.lastDir = lastDir;
    }

    public Signal getSignalRecv() {
        return signalRecv;
    }

    public void setSignalRecv(Signal signalRecv) {
        this.signalRecv = signalRecv;
    }

    public Signal getSignalSend() {
        return signalSend;
    }

    public void setSignalSend(Signal signalSend) {
        this.signalSend = signalSend;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void setTargetLeader(Vehicle targetLeader) {
        this.targetLeader = targetLeader;
    }

    public void setTargetFollower(Vehicle targetFollower) {
        this.targetFollower = targetFollower;
    }

    public double getLeaderGap() {
        return leaderGap;
    }

    public void setLeaderGap(double leaderGap) {
        this.leaderGap = leaderGap;
    }

    public double getFollowerGap() {
        return followerGap;
    }

    public void setFollowerGap(double followerGap) {
        this.followerGap = followerGap;
    }

    public double getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(double waitingTime) {
        this.waitingTime = waitingTime;
    }

    public boolean isChanging() {
        return changing;
    }

    public void setChanging(boolean changing) {
        this.changing = changing;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public double getLastChangeTime() {
        return lastChangeTime;
    }

    public void setLastChangeTime(double lastChangeTime) {
        this.lastChangeTime = lastChangeTime;
    }
}
