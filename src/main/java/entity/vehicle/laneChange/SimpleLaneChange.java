package entity.vehicle.laneChange;

import entity.roadNet.roadNet.Lane;
import entity.vehicle.router.Router;
import entity.vehicle.vehicle.Vehicle;

public class SimpleLaneChange extends LaneChange {
    private double estimateGap(Lane lane) {
        int curSegIndex = vehicle.getSegmentIndex();
        Vehicle leader = lane.getVehicleAfterDistance(vehicle.getCurDis(), curSegIndex);
        if (leader == null) {
            return lane.getLength() - vehicle.getCurDis();
        } else {
            return leader.getCurDis() - vehicle.getCurDis() - leader.getLen();
        }
    }

    public void makeSignal(double interval) {
        if (changing || vehicle.getEngine().getCurrentTime() - lastChangeTime < coolingTime) {
            return;
        }
        signalSend = new Signal();
        signalSend.setSource(vehicle);
        if (vehicle.getCurDrivable().isLane()) {
            Lane curLane = vehicle.getCurLane();
            if (curLane.getLength() - vehicle.getCurDis() < 30) {
                return;
            }
            double curEst = vehicle.getGap();
            double outerEst = 0;
            double innerEst;
            double expectGap = 2 * vehicle.getLen() + 4 * interval * vehicle.getMaxSpeed();
            if (curEst > expectGap || curEst < 1.5 * vehicle.getLen()) {
                return;
            }
            Router router = vehicle.getCurRouter();
            if (curLane.getLaneIndex() < curLane.getBeLongRoad().getLanes().size() - 1) {
                if (router.onLastRoad() || router.getNextDrivable(curLane.getOuterLane()) != null) { // 已到 route 末尾或者外侧路满足通行要求
                    outerEst = estimateGap(curLane.getOuterLane());                          // 与外侧前车间距
                    if (outerEst > curEst + vehicle.getLen()) {                           // 外侧车距要求大于当前车距 + 车厂 （不应该再加个 safeGapBefore ?）
                        signalSend.setTarget(curLane.getOuterLane());                         // signal 传向外侧
                    }
                }
            }

            if (curLane.getLaneIndex() > 0) { // 当前不在最内侧，则测试内侧 lane 是否满足 laneChange 需求
                if (router.onLastRoad() || router.getNextDrivable(curLane.getInnerLane()) != null) {
                    innerEst = estimateGap(curLane.getInnerLane());
                    if (innerEst > curEst + vehicle.getLen() && innerEst > outerEst) // 转向间距更大的一侧
                        signalSend.setTarget(curLane.getInnerLane());
                }
            }
            signalSend.setUrgency(1);
        }
        super.makeSignal(interval);
    }

    public SimpleLaneChange(Vehicle vehicle, LaneChange other) {
        super(vehicle, other);
    }

    public SimpleLaneChange(Vehicle vehicle) {
        super(vehicle);
    }

    @Override
    public double yieldSpeed(double interval) {
        if (planChange()) {// 在此更新 waitingTime
            waitingTime += interval;
        }
        if (signalRecv != null) {
            if (vehicle == signalRecv.getSource().getTargetLeader()) { // 自己是某个将 laneChange 的车的前车
                return 100;                                         // 大速度用于后续 min2double
            } else {                                                // 作为后车
                Vehicle source = signalRecv.getSource();
                double srcSpeed = source.getSpeed();
                double gap = source.getLaneChange().getGapBefore() - source.getLaneChange().getSafeGapBefore();

                double v = vehicle.getNoCollisionSpeed(srcSpeed, source.getMaxNegAcc(), vehicle.getSpeed(), vehicle.getMaxNegAcc(), gap, interval, 0);

                if (v < 0) {// 无法满足 yieldSpeed 要求就算了
                    v = 100;
                }
                // If the follower is too fast, let it go.
                return v;
            }
        }
        return 100;
    }

    @Override
    public void sendSignal() {
        if (targetLeader != null) {
            targetLeader.receiveSignal(vehicle);
        }
        if (targetFollower != null) {
            targetFollower.receiveSignal(vehicle);
        }
    }

    @Override
    public double getSafeGapBefore() {
        return targetFollower != null ? targetFollower.getMinBrakeDistance() : 0;
    }

    @Override
    public double getSafeGapAfter() {
        return vehicle.getMinBrakeDistance();
    }
}
