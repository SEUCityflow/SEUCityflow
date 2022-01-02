package entity.vehicle.vehicle;

import entity.engine.Engine;
import entity.flow.Flow;
import entity.flow.Route;
import entity.roadNet.roadNet.*;
import entity.vehicle.router.Router;
import entity.vehicle.laneChange.LaneChange;
import entity.vehicle.laneChange.Signal;
import entity.vehicle.laneChange.SimpleLaneChange;
import javafx.util.Pair;
import util.ControlInfo;
import util.Point;

import static util.Point.*;

import java.util.*;

public class Vehicle {
    private VehicleInfo vehicleInfo; // 车辆基本数据
    private Buffer buffer; // 完成 step 后生成的新数据，待更新到 controllerInfo
    private ControllerInfo controllerInfo; // 当前数据
    private LaneChangeInfo laneChangeInfo; // 变道数据
    private LaneChange laneChange; // 变道
    private int priority; // 优先级
    private String id;
    private double enterTime;
    private Engine engine;
    private boolean routeValid = true;
    private Flow flow;

    public Vehicle() {
        vehicleInfo = new VehicleInfo();
        controllerInfo = new ControllerInfo(this);
        buffer = new Buffer();
        laneChangeInfo = new LaneChangeInfo();
        laneChange = new SimpleLaneChange(this);
    }

    // 用于 archive
    public Vehicle(Vehicle vehicle) {
        vehicleInfo = new VehicleInfo(vehicle.vehicleInfo);
        controllerInfo = new ControllerInfo(this, vehicle.controllerInfo);
        buffer = new Buffer(vehicle.buffer);
        priority = vehicle.priority;
        id = vehicle.id;
        engine = vehicle.engine;
        flow = vehicle.flow;
        enterTime = vehicle.enterTime;
        laneChangeInfo = new LaneChangeInfo(vehicle.laneChangeInfo);
        laneChange = new SimpleLaneChange(this, vehicle.laneChange);
        routeValid = vehicle.routeValid;
    }

    // shadow 创建，除 laneChange（新建）和 flow（nullptr）与 router.vehicle 外全部一致
    public Vehicle(Vehicle vehicle, String id, Engine engine, Flow flow) {
        vehicleInfo = vehicle.vehicleInfo;
        controllerInfo = new ControllerInfo(this, vehicle.getControllerInfo());
        buffer = new Buffer(vehicle.buffer);
        this.id = id;
        this.engine = vehicle.engine;
        this.flow = flow;
        while (engine.checkPriority(priority = engine.getRnd().nextInt())) ;
        enterTime = vehicle.enterTime;
        laneChangeInfo = new LaneChangeInfo(vehicle.laneChangeInfo);
        laneChange = new SimpleLaneChange(this);
    }

    // 用于 flow
    public Vehicle(VehicleInfo init, String id, Engine engine, Flow flow) {
        vehicleInfo = new VehicleInfo(init);
        controllerInfo = new ControllerInfo(this, vehicleInfo.route, engine.getRnd());
        buffer = new Buffer();
        this.id = id;
        this.engine = engine;
        this.flow = flow;
        controllerInfo.setApproachingIntersectionDistance(vehicleInfo.maxSpeed * vehicleInfo.maxSpeed / vehicleInfo.usualNegAcc / 2 + vehicleInfo.maxSpeed * engine.getInterval() * 2);
        while (engine.checkPriority(priority = engine.getRnd().nextInt())) ;
        enterTime = engine.getCurrentTime();
        laneChangeInfo = new LaneChangeInfo();
        laneChange = new SimpleLaneChange(this);
    }

    // 由 dis 算出当前在哪条 drivable 上并更新 buffer
    public void setDeltaDistance(double dis) {
        if (!buffer.isDisSet || dis < buffer.deltaDis) {  // 条件二为何会出现？
            unSetBufferEnd();   // 用于条件二
            unSetBufferDrivable();  // 用于条件二
            buffer.deltaDis = dis;
            dis += controllerInfo.getDis();  // 到目前所在 drivable 起点的总距离
            Drivable drivable = getCurDrivable();
            for (int i = 0; drivable != null && dis > drivable.getLength(); i++) {
                dis -= drivable.getLength();
                Drivable nextDrivable = controllerInfo.getRouter().getNextDrivable(i);
                if (nextDrivable == null) {  // 没有下一 drivable 且 dis 大于当前 drivable 长度，即此时已到达末尾
                    assert (controllerInfo.getRouter().isLastRoad(drivable));
                    setBufferEnd(true);
                }
                drivable = nextDrivable;
                setBufferDrivable(drivable);  // 新 drivable 存入 buffer
            }
            setBufferDis(dis);
        }

    }

    // 如 drivable 改变则返回新的 drivable
    public boolean isDrivableSet() {
        return buffer.isDrivableSet;
    }

    public Drivable getChangedDrivable() {
        if (!buffer.isDrivableSet) {
            return null;
        }
        return buffer.drivable;
    }

    // 获取 vehicle 当前坐标
    public Point getPoint() {
        if (Math.abs(getOffSet()) < eps || controllerInfo.getDrivable().isLaneLink()) { // 未 laneChange 时位置
            return getPointByDistance(controllerInfo.getDrivable().getPoints(), controllerInfo.getDis());
        } else {
            Lane lane = (Lane) controllerInfo.getDrivable();
            Point origin = getPointByDistance(lane.getPoints(), controllerInfo.getDis());
            Point next;
            double percentage;
            List<Lane> lanes = lane.getBelongRoad().getLanes();
            if (getOffSet() > 0) {  // 向外侧偏移
                next = getPointByDistance(lanes.get(lane.getLaneIndex() + 1).getPoints(), controllerInfo.getDis()); // 外侧同距离位置
                percentage = 2 * getOffSet() / (lane.getWidth() + lanes.get(lane.getLaneIndex() + 1).getWidth()); // 横向所占比例
            } else {
                next = getPointByDistance(lanes.get(lane.getLaneIndex() - 1).getPoints(), controllerInfo.getDis());
                percentage = -2 * getOffSet() / (lane.getWidth() + lanes.get(lane.getLaneIndex() - 1).getWidth());
            }
            return new Point(next.x * percentage + origin.x * (1 - percentage), next.y * percentage + origin.y * (1 - percentage));
        }
    }

    // TODO: use something like reflection?    buffer 信息导入 controllerInfo
    public void update() {
        if (buffer.isEndSet) {
            controllerInfo.setEnd(buffer.end);
            buffer.isEndSet = false;
        }
        if (buffer.isDisSet) {
            controllerInfo.setDis(buffer.dis);
            buffer.isDisSet = false;
        }
        if (buffer.isSpeedSet) {
            vehicleInfo.speed = buffer.speed;
            buffer.isSpeedSet = false;
        }
        if (buffer.isCustomSpeedSet) {
            buffer.isCustomSpeedSet = false;
        }
        if (buffer.isDrivableSet) {
            controllerInfo.setPrevDrivable(controllerInfo.getDrivable());
            controllerInfo.setDrivable(buffer.drivable);
            buffer.isDrivableSet = false;
            controllerInfo.getRouter().update();
        }
        if (buffer.isEnterLaneLinkTimeSet) {
            controllerInfo.setEnterLaneLinkTime(buffer.enterLaneLinkTime);
            buffer.isEnterLaneLinkTimeSet = false;
        }
        if (buffer.isBlockerSet) {
            controllerInfo.setBlocker(buffer.blocker);
            buffer.isBlockerSet = false;
        } else {
            controllerInfo.setBlocker(null);
        }
        if (buffer.isNotifiedVehicles) {
            buffer.notifiedVehicles.clear();
            buffer.isNotifiedVehicles = false;
        }
    }

    public void updateRouter() {
        controllerInfo.getRouter().update();
    }

    // 获取 vehicle 头尾坐标
    public Pair<Point, Point> getCurPos() {
        // 车头坐标
        Point first = getPointByDistance(controllerInfo.getDrivable().getPoints(), controllerInfo.getDis());
        // 行驶方向
        Point direction = getDirectionByDistance(controllerInfo.getDrivable().getPoints(), controllerInfo.getDis());
        Point tail = new Point(first);
        tail.x -= direction.x * vehicleInfo.len;
        tail.y -= direction.y * vehicleInfo.len;
        // 头尾坐标
        return new Pair<>(first, tail);
    }

    // 更新 leader 与 gap
    public void updateLeaderAndGap(Vehicle leader) {
        if (leader != null && leader.getCurDrivable() == getCurDrivable()) {  // 传入 leader 且和当前车在同一 lane
            controllerInfo.setLeader(leader);
            controllerInfo.setGap(leader.getCurDis() - leader.getLen() - controllerInfo.getDis());
        } else {
            controllerInfo.setLeader(null);
            Drivable drivable;
            Vehicle candidateLeader;
            double candidateGap;
            double dis = controllerInfo.getDrivable().getLength() - controllerInfo.getDis();  // 距 lane 首距离
            int cnt = 0;
            while (true) {  // 在未来将驶向的 drivable 内搜寻 leader
                drivable = getNextDrivable(cnt++);
                if (drivable == null) { // 已到 route 末尾，则无 leader
                    return;
                }
                if (drivable.isLaneLink()) {  // if laneLink, check all laneLink start from previous lane, because lanelinks may overlap
                    for (LaneLink laneLink : ((LaneLink) drivable).getStartLane().getLaneLinks()) {
                        if ((candidateLeader = laneLink.getLastVehicle()) != null) {  // drivable 中的 lastVehicle 为 leader
                            candidateGap = dis + candidateLeader.getCurDis() - candidateLeader.getLen();
                            if (controllerInfo.getLeader() == null || candidateGap < controllerInfo.getGap()) {
                                controllerInfo.setLeader(candidateLeader);
                                controllerInfo.setGap(candidateGap);
                            }
                        }
                    }
                    if (controllerInfo.getLeader() != null) {
                        return;
                    }
                } else {
                    if (drivable.getLastVehicle() != null) {
                        controllerInfo.setLeader(drivable.getLastVehicle());
                        controllerInfo.setGap(dis + controllerInfo.getLeader().getCurDis() - controllerInfo.getLeader().getLen());
                    }
                }
                // 当前 drivable 中无车，再向前找
                dis += drivable.getLength();
                // ？多次寻找后 dis 距离过大，停止寻找
                if (dis > vehicleInfo.maxSpeed * vehicleInfo.maxSpeed / vehicleInfo.usualNegAcc / 2 + vehicleInfo.maxSpeed * engine.getInterval() * 2) {
                    return;
                }
            }
        }
    }

    // 在给定数据下减速使最终距离为 targetGap，减速 interval 时间后的速度
    public double getNoCollisionSpeed(double vL, double dL, double vF, double dF, double gap, double interval, double targetGap) {
        double c = vF * interval / 2 + targetGap - 0.5 * vL * vL / dL - gap;
        double a = 0.5 / dF;
        double b = 0.5 * interval;
        if (b * b < 4 * a * c) {
            return -100;
        }
        double v1 = 0.5 / a * (Math.sqrt(b * b - 4 * a * c) - b);
        double v2 = 2 * vL - dL * interval + 2 * (gap - targetGap) / interval;
        return Math.min(v1, v2);
    }

    // 跟随速度
    public double getCarFollowSpeed(double interval) {
        Vehicle leader = getCurLeader();
        if (leader == null) {  // 没有前车
            return hasSetCustomSpeed() ? buffer.customSpeed : vehicleInfo.maxSpeed;  // 习惯速度/上限速度
        }

        double v = getNoCollisionSpeed(leader.getSpeed(), leader.getMaxNegAcc(), vehicleInfo.speed, vehicleInfo.maxNegAcc, controllerInfo.getGap(), interval, 0); // 极端情况下制动无碰撞

        if (hasSetCustomSpeed())
            return Math.min(buffer.customSpeed, v); // 有习惯速度则以习惯速度

        double assumeDecel = 0, leaderSpeed = leader.getSpeed(); // 速度差
        if (vehicleInfo.speed > leaderSpeed) {
            assumeDecel = vehicleInfo.speed - leaderSpeed;
        }
        v = Math.min(v, getNoCollisionSpeed(leader.getSpeed(), leader.getUsualNegAcc(), vehicleInfo.speed, vehicleInfo.usualNegAcc, controllerInfo.getGap(), interval,
                vehicleInfo.minGap)); // 常规情况下制动保持 minGap
        v = Math.min(v, (controllerInfo.getGap() + (leaderSpeed + assumeDecel / 2) * interval - vehicleInfo.speed * interval / 2) / (vehicleInfo.headwayTime + interval / 2)); // ?
        return v;
    }

    public double getStopBeforeSpeed(double distance, double interval) {  // 能在 distance 内停下时经过 interval 时间后的速度
        assert (distance >= 0);
        if (getBrakeDistanceAfterAccel(vehicleInfo.usualPosAcc, vehicleInfo.usualNegAcc, interval) < distance) {// 如果加速 interval 时间后再减速距离依旧满足，那就加速
            return vehicleInfo.speed + vehicleInfo.usualPosAcc * interval;
        }
        double takeInterval = 2 * distance / (vehicleInfo.speed + eps) / interval; // 在 distance 距离内减速到 0 需要几个 interval
        if (takeInterval >= 1) {
            return vehicleInfo.speed - vehicleInfo.speed / (int) takeInterval;
        } else {
            return vehicleInfo.speed - vehicleInfo.speed / takeInterval; // <0 ? (后续会判是否小于0)
        }
    }

    public int getReachSteps(double distance, double targetSpeed, double acc) {
        if (distance <= 0) {                                                            // 已到
            return 0;
        }
        double interval = engine.getInterval();
        if (vehicleInfo.speed > targetSpeed) {// 当前速度大于目标速度
            return (int) Math.ceil(distance / vehicleInfo.speed / interval);
        }
        double distanceUntilTargetSpeed = getDistanceUntilSpeed(targetSpeed, acc); // 加速到 targetSpeed 距离
        if (distanceUntilTargetSpeed > distance) {   // distance 内加速不到 targetSpeed
            return (int) Math.ceil((Math.sqrt(vehicleInfo.speed * vehicleInfo.speed + 2 * acc * distance) - vehicleInfo.speed) / acc / interval); // 在 distance 内加到最终速所用时间段
        } else {
            return (int) Math.ceil((targetSpeed - vehicleInfo.speed) / acc / interval) + (int) Math.ceil((distance - distanceUntilTargetSpeed) / targetSpeed / interval);  // distance 内加速并匀速所用时间段
        }
    }

    // 在 laneLink 上以最大可能行驶 distance 所用时间段
    public int getReachStepsOnLaneLink(double distance, LaneLink laneLink) {
        return getReachSteps(distance, laneLink.isTurn() ? vehicleInfo.turnSpeed : vehicleInfo.maxSpeed, vehicleInfo.usualPosAcc);
    }

    // 以 acc 加速度加速到 speed 所需距离
    public double getDistanceUntilSpeed(double speed, double acc) {
        if (speed <= vehicleInfo.speed) { // 已到
            return 0;
        }

        double interval = engine.getInterval();
        int stage1Steps = (int) Math.floor((speed - vehicleInfo.speed) / acc / interval);  // 加速到 speed 需要的 step 数
        double stage1Speed = vehicleInfo.speed + stage1Steps * acc / interval;  // 最终速度
        double stage1Dis = (vehicleInfo.speed + stage1Speed) * (stage1Steps * interval) / 2;  // 初加速阶段行驶距离
        return stage1Dis + (stage1Speed < speed ? ((stage1Speed + speed) * interval / 2) : 0);  // 总距离
    }

    // 未到 cross 且能在 yield 范围前停住或已过 cross 且不覆盖 cross
    public boolean canYield(double dist) {
        return (dist > 0 && getMinBrakeDistance() < dist - vehicleInfo.yieldDistance) || (dist < 0 && dist + vehicleInfo.len < 0);
    }

    // 是否已在 intersection 或将进入 intersection
    public boolean isIntersectionRelated() {
        if (controllerInfo.getDrivable().isLaneLink()) {
            return true;
        }
        if (controllerInfo.getDrivable().isLane()) {
            Drivable drivable = getNextDrivable();
            return drivable != null && drivable.isLaneLink() && controllerInfo.getDrivable().getLength() - controllerInfo.getDis() <= controllerInfo.getApproachingIntersectionDistance();
        }
        return false;
    }

    // 在加速 interval 时间后减速到 0 需要的距离
    public double getBrakeDistanceAfterAccel(double acc, double dec, double interval) {
        double currentSpeed = vehicleInfo.speed;
        double nextSpeed = currentSpeed + acc * interval;
        return (currentSpeed + nextSpeed) * interval / 2 + (nextSpeed * nextSpeed / dec / 2);
    }

    // 求解 interval 后的速度
    public ControlInfo getNextSpeed(double interval) {
        ControlInfo controlInfo = new ControlInfo();
        Drivable drivable = controllerInfo.getDrivable();
        double v = vehicleInfo.maxSpeed;                                    // 上限速度
        v = Math.min(v, vehicleInfo.speed + vehicleInfo.maxPosAcc * interval); // 当前速度能加到的最快速度
        v = Math.min(v, drivable.getMaxSpeed()); // 道路限速
        // car follow
        v = Math.min(v, getCarFollowSpeed(interval)); // 跟随速度
        if (isIntersectionRelated()) {
            v = Math.min(v, getIntersectionRelatedSpeed(interval)); // 过 intersection 速度
        }
        v = Math.max(v, vehicleInfo.speed - vehicleInfo.maxNegAcc * interval); // 能减到的最小速度
        controlInfo.speed = v;
        return controlInfo;
    }

    // 将进入或已在 intersection 时的速度计算
    public double getIntersectionRelatedSpeed(double interval) {
        double v = vehicleInfo.maxSpeed;                           // 最大速度
        Drivable nextDrivable = getNextDrivable();
        LaneLink laneLink = null;
        if (nextDrivable != null && nextDrivable.isLaneLink()) { // 即将进入 intersection
            laneLink = (LaneLink) nextDrivable;
            if (!laneLink.isAvailable() || !laneLink.getEndLane().canEnter(this)) { // not only the first vehicle should follow intersection logic  由于红灯或 endLane 车辆过多而不可通
                if (getMinBrakeDistance() > controllerInfo.getDrivable().getLength() - controllerInfo.getDis()) { // 无法在线前刹车
                    // TODO: 暂时不会出现此情况
                } else {
                    v = Math.min(v, getStopBeforeSpeed(controllerInfo.getDrivable().getLength() - controllerInfo.getDis(), interval)); // 能停下的话经过 interval 时间的速度
                    return v;
                }
            }
            if (laneLink.isTurn()) { // 绿灯转弯限速
                v = Math.min(v, vehicleInfo.turnSpeed);
            }
        }
        if (laneLink == null && controllerInfo.getDrivable().isLaneLink()) {// 已在 intersection
            laneLink = (LaneLink) (controllerInfo.getDrivable()); // 获取当前 laneLink
        }
        double distanceToLaneLinkStart = controllerInfo.getDrivable().isLane() ? -(controllerInfo.getDrivable().getLength() - controllerInfo.getDis())
                : controllerInfo.getDis(); // vehicle 距离 laneLink start 的 距离 <0 表示在 laneLink 前，>0 在 laneLink 后
        double distanceOnLaneLink;
        assert laneLink != null;
        for (Cross cross : laneLink.getCrosses()) {                 // 对当前 laneLink 上每个 cross
            distanceOnLaneLink = cross.getDistanceByLane(laneLink); // cross 距 laneLink 起点距离
            if (distanceOnLaneLink < distanceToLaneLinkStart)        // 车头已过此 cross，说明先前已对当前 cross 进行了 canPass 判断，无需再考虑
                continue;
            if (!cross.canPass(this, laneLink, distanceToLaneLinkStart)) { // 当前不可通过
                v = Math.min(v, getStopBeforeSpeed(distanceOnLaneLink - distanceToLaneLinkStart - vehicleInfo.yieldDistance, interval)); // TODO: headway distance  能停下的话经过 interval 时间的速度
                setBlocker(cross.getFoeVehicle(laneLink));      // 被 block
                break;
            }
        }
        return v;
    }

    public Road getFirstRoad() {
        return controllerInfo.getRouter().getFirstRoad();
    }

    // controllerInfo.drivable 初次设置
    public void setFirstDrivable() {
        controllerInfo.setDrivable(controllerInfo.getRouter().getFirstDrivable());
    }

    // 用 updateShortestPath 更新 route
    public void updateRoute() {
        routeValid = controllerInfo.getRouter().updateShortestPath();
    }

    public boolean changeRoute(List<Road> anchor) {
        return controllerInfo.getRouter().changeRoute(anchor);
    }

    public Drivable getNextDrivable(int i) {
        return controllerInfo.getRouter().getNextDrivable(i);
    }

    // java不支持默认参数，故使用方法重载
    public Drivable getNextDrivable() {
        return controllerInfo.getRouter().getNextDrivable(0);
    }

    // 减速到 0 最短距离
    public double getMinBrakeDistance() {
        return 0.5 * vehicleInfo.speed * vehicleInfo.speed / vehicleInfo.maxNegAcc;
    }

    // 常规减速到 0 所需距离
    public double getUsualBrakeDistance() {
        return 0.5 * vehicleInfo.speed * vehicleInfo.speed / vehicleInfo.usualNegAcc;
    }

    // for laneChange
    public void makeLaneChangeSignal(double interval) { // 交由 laneChange 创建 signalSend 并设置 signal 内各值并寻找目标 lane
        laneChange.makeSignal(interval);
    }

    public boolean planLaneChange() { // 交由 laneChange 判断是否满足 laneChange 条件
        return laneChange.planChange();
    }

    public void receiveSignal(Vehicle sender) {// laneChange signal 接收，按 priority 判断
        if (laneChange.isChanging()) {// 当前车正在 langChange，无视
            return;
        }

        Signal signal_recv = laneChange.getSignalRecv();                              // 之前接收的 signal
        Signal signal_send = laneChange.getSignalSend();                               // 自己发送的 signal
        int curPriority = signal_recv != null ? signal_recv.getSource().getPriority() : -1; // 获取之前 receiveSignal 来源 vehicle 的 priority
        int newPriority = sender.getPriority();                                 // 当前 receiveSignal 来源 vehicle 的 priority

        if ((signal_recv == null || curPriority < newPriority) && (signal_send == null || priority < newPriority)) { // （尚未接收 || sender 的优先级更高更高） && (自己未发 || sender 优先级更高)
            laneChange.setSignalRecv(sender.laneChange.getSignalSend());
        }
    }

    public void sendSignal() { // 交由 laneChange 向 targetLeader 和 targetFollower 传递信号
        laneChange.sendSignal();
    }

    public void clearSignal() {
        laneChange.clearSignal();
    }

    public void updateLaneChangeNeighbor() { // 交由 laneChange 寻找 laneChange 后的 leader 与 follower
        laneChange.updateLeaderAndFollower();
    }

    public void insertShadow(Vehicle shadow) { // 交由 laneChange 将 shadow 插入 targetLane
        laneChange.insertShadow(shadow);
    }

    public boolean onValidLane() { // 交由 router，当无下一条路且 route 未到末尾说明有误
        return controllerInfo.getRouter().onValidLane();
    }

    public Lane getValidLane() { // nextLane
        assert (getCurDrivable().isLane());
        return controllerInfo.getRouter().getValidLane((Lane) (getCurDrivable()));
    }

    public boolean canChange() { // 交由 laneChange，自己发送了 signal 且未 receive 信号，如receive 说明 receive 信号优先级更高
        return laneChange.canChange();
    }

    public double getGap() {
        return controllerInfo.getGap();
    }

    public int getLaneChangeUrgency() {
        return laneChange.getSignalSendUrgency();
    }

    public Vehicle getTargetLeader() {
        return laneChange.getTargetLeader();
    }

    public int getLastLaneChangeDirection() {
        return laneChange.getLastDir();
    }

    public int getLaneChangeDirection() {
        if (laneChange.getSignalSend() == null) {
            return 0;
        }
        return laneChange.getSignalSend().getDirection();
    }

    public boolean isChanging() {
        return laneChange.isChanging();
    }

    public double getMaxOffSet() { // 最大偏移量，大于此量表示完成 laneChange
        Lane target = laneChange.getSignalSend().getTarget();
        return (target.getWidth() + getCurLane().getWidth()) / 2;
    }

    public void abortLaneChange() {// 由 shadow 调用，终止 laneChange
        setBufferEnd(true);
        laneChange.abortChanging();
    }

    public void finishChanging() {
        laneChange.finishChanging();
        setBufferEnd(true);
    }


    // 自身 set / get
    public VehicleInfo getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(VehicleInfo vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }

    public ControllerInfo getControllerInfo() {
        return controllerInfo;
    }

    public void setControllerInfo(ControllerInfo controllerInfo) {
        this.controllerInfo = controllerInfo;
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

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public boolean isRouteValid() {
        return routeValid;
    }

    public void setRouteValid(boolean routeValid) {
        this.routeValid = routeValid;
    }

    public Flow getFlow() {
        return flow;
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    public LaneChangeInfo getLaneChangeInfo() {
        return laneChangeInfo;
    }

    public void setLaneChangeInfo(LaneChangeInfo laneChangeInfo) {
        this.laneChangeInfo = laneChangeInfo;
    }

    public void setLaneChange(LaneChange laneChange) {
        this.laneChange = laneChange;
    }

    public LaneChange getLaneChange() {
        return laneChange;
    }

    // buffer set / get
    public double getBufferDis() {
        return buffer.dis;
    }

    public void setBufferDis(double dis) {
        buffer.dis = dis;
        buffer.isDisSet = true;
    }

    public double getBufferSpeed() {
        return buffer.speed;
    }

    public void setBufferSpeed(double speed) {
        buffer.speed = speed;
        buffer.isSpeedSet = true;
    }

    public Drivable getBufferDrivable() {
        return buffer.drivable;
    }

    public void setBufferDrivable(Drivable drivable) {
        buffer.drivable = drivable;
        buffer.isDrivableSet = true;
    }

    public void unSetBufferDrivable() {
        buffer.isDrivableSet = false;
    }

    public List<Vehicle> getBufferNotifiedVehicles() {
        return buffer.notifiedVehicles;
    }

    public void setBufferNotifiedVehicles(List<Vehicle> notifiedVehicles) {
        buffer.notifiedVehicles = notifiedVehicles;
        buffer.isNotifiedVehicles = true;
    }

    public boolean isBufferEnd() {
        return buffer.end;
    }

    public void setBufferEnd(boolean end) {
        buffer.end = end;
        buffer.isEndSet = true;
    }

    public void unSetBufferEnd() {
        buffer.isEndSet = false;
    }

    public int getBufferEnterLaneLinkTime() {
        return buffer.enterLaneLinkTime;
    }

    public void setBufferEnterLaneLinkTime(int enterLaneLinkTime) {
        buffer.enterLaneLinkTime = enterLaneLinkTime;
        buffer.isEnterLaneLinkTimeSet = true;
    }

    public Vehicle getBufferBlocker() {
        return buffer.blocker;
    }

    public void setBufferBlocker(Vehicle blocker) {
        buffer.blocker = blocker;
        buffer.isBlockerSet = true;
    }

    public double getBufferCustomSpeed() {
        return buffer.customSpeed;
    }

    public void setBufferCustomSpeed(double customSpeed) {
        buffer.customSpeed = customSpeed;
        buffer.isCustomSpeedSet = true;
    }

    public double getBufferDeltaDis() {
        return buffer.deltaDis;
    }

    public void setBufferDeltaDis(double deltaDis) {
        buffer.deltaDis = deltaDis;
    }

    public boolean hasSetDis() {
        return buffer.isDisSet;
    }

    public boolean hasSetSpeed() {
        return buffer.isSpeedSet;
    }

    public boolean hasSetDrivable() {
        return buffer.isDrivableSet;
    }

    public boolean hasNotifiedVehicles() {
        return buffer.isNotifiedVehicles;
    }

    public boolean hasSetEnd() {
        return buffer.isEndSet;
    }

    public boolean hasSetEnterLaneLinkTime() {
        return buffer.isEnterLaneLinkTimeSet;
    }

    public boolean hasSetBlocker() {
        return buffer.isBlockerSet;
    }

    public boolean hasSetCustomSpeed() {
        return buffer.isCustomSpeedSet;
    }

    // ControllerInfo set / get
    public double getCurDis() {
        return controllerInfo.getDis();
    }

    public void setCurDis(double dis) {
        controllerInfo.setDis(dis);
    }

    public Drivable getCurDrivable() {
        return controllerInfo.getDrivable();
    }

    public void setCurDrivable(Drivable drivable) {
        controllerInfo.setDrivable(drivable);
    }

    public Drivable getPrevDrivable() {
        return controllerInfo.getPrevDrivable();
    }

    public void setPrevDrivable(Drivable prevDrivable) {
        controllerInfo.setPrevDrivable(prevDrivable);
    }

    public double getApproachingIntersectionDistance() {
        return controllerInfo.getApproachingIntersectionDistance();
    }

    public void setApproachingIntersectionDistance(double approachingIntersectionDistance) {
        controllerInfo.setApproachingIntersectionDistance(approachingIntersectionDistance);
    }

    public double getCurGap() {
        return controllerInfo.getGap();
    }

    public void setCurGap(double gap) {
        controllerInfo.setGap(gap);
    }

    public int getEnterLaneLinkTime() {
        return controllerInfo.getEnterLaneLinkTime();
    }

    public void setEnterLaneLinkTime(int enterLaneLinkTime) {
        controllerInfo.setEnterLaneLinkTime(enterLaneLinkTime);
    }

    public Vehicle getCurLeader() {
        return controllerInfo.getLeader();
    }

    public void setCurLeader(Vehicle leader) {
        controllerInfo.setLeader(leader);
    }

    public Vehicle getCurBlocker() {
        return controllerInfo.getBlocker();
    }

    public void setCurBlocker(Vehicle blocker) {
        controllerInfo.setBlocker(blocker);
    }

    public boolean isCurEnd() {
        return controllerInfo.isEnd();
    }

    public void setCurEnd(boolean end) {
        controllerInfo.setEnd(end);
    }

    public boolean isCurRunning() {
        return controllerInfo.isRunning();
    }

    public void setCurRunning(boolean running) {
        controllerInfo.setRunning(running);
    }

    public Router getCurRouter() {
        return controllerInfo.getRouter();
    }

    public void setCurRouter(Router router) {
        controllerInfo.setRouter(router);
    }

    // VehicleInfo set / get
    public double getSpeed() {
        return vehicleInfo.speed;
    }

    public void setSpeed(double speed) {
        vehicleInfo.speed = speed;
    }

    public double getLen() {
        return vehicleInfo.len;
    }

    public void setLen(double len) {
        vehicleInfo.len = len;
    }

    public double getWidth() {
        return vehicleInfo.width;
    }

    public void setWidth(double width) {
        vehicleInfo.width = width;
    }

    public double getMaxPosAcc() {
        return vehicleInfo.maxPosAcc;
    }

    public void setMaxPosAcc(double maxPosAcc) {
        vehicleInfo.maxPosAcc = maxPosAcc;
    }

    public double getMaxNegAcc() {
        return vehicleInfo.maxNegAcc;
    }

    public void setMaxNegAcc(double maxNegAcc) {
        vehicleInfo.maxNegAcc = maxNegAcc;
    }

    public double getUsualPosAcc() {
        return vehicleInfo.usualPosAcc;
    }

    public void setUsualPosAcc(double usualPosAcc) {
        vehicleInfo.usualPosAcc = usualPosAcc;
    }

    public double getUsualNegAcc() {
        return vehicleInfo.usualNegAcc;
    }

    public void setUsualNegAcc(double usualNegAcc) {
        vehicleInfo.usualNegAcc = usualNegAcc;
    }

    public double getMinGap() {
        return vehicleInfo.minGap;
    }

    public void setMinGap(double minGap) {
        vehicleInfo.minGap = minGap;
    }

    public double getMaxSpeed() {
        return vehicleInfo.maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        vehicleInfo.maxSpeed = maxSpeed;
    }

    public double getHeadwayTime() {
        return vehicleInfo.headwayTime;
    }

    public void setHeadwayTime(double headwayTime) {
        vehicleInfo.headwayTime = headwayTime;
    }

    public double getYieldDistance() {
        return vehicleInfo.yieldDistance;
    }

    public void setYieldDistance(double yieldDistance) {
        vehicleInfo.yieldDistance = yieldDistance;
    }

    public double getTurnSpeed() {
        return vehicleInfo.turnSpeed;
    }

    public void setTurnSpeed(double turnSpeed) {
        vehicleInfo.turnSpeed = turnSpeed;
    }

    public Route getRoute() {
        return vehicleInfo.route;
    }

    public void setRoute(Route route) {
        vehicleInfo.route = route;
    }

    public void setBlocker(Vehicle blocker) {
        buffer.blocker = blocker;
        buffer.isBlockerSet = true;
    }

    // laneChangeInfo set/get
    public boolean isReal() {
        return laneChangeInfo.getPartnerType() != 2;
    }

    public void setPartnerType(int partnerType) {
        laneChangeInfo.setPartnerType(partnerType);
    }

    public int getPartnerType() {
        return laneChangeInfo.getPartnerType();
    }

    public boolean hasPartner() {
        return laneChangeInfo.getPartnerType() > 0;
    }

    public void setShadow(Vehicle vehicle) { // 自己是原 vehicle
        laneChangeInfo.setPartnerType(1);
        laneChangeInfo.setPartner(vehicle);
    }

    public void setPartner(Vehicle vehicle) { // 自己是 shadow
        laneChangeInfo.setPartner(vehicle);
        if (vehicle != null) {
            laneChangeInfo.setPartnerType(2);
        } else {
            laneChangeInfo.setPartnerType(0);
        }
    }

    public void setLastChangeTime(double time) {
        laneChange.setLastChangeTime(time);
    }

    public Vehicle getPartner() {
        return laneChangeInfo.getPartner();
    }

    public int getSegmentIndex() {
        return laneChangeInfo.getSegmentIndex();
    }

    public void setSegmentIndex(int i) {
        laneChangeInfo.setSegmentIndex(i);
    }

    public void setOffSet(double offSet) {
        laneChangeInfo.setOffSet(offSet);
    }

    public double getOffSet() {
        return laneChangeInfo.getOffSet();
    }

    // 对应 id 车辆信息获取 <title, info>
    public Map<String, String> getInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("running", Boolean.toString(isCurRunning()));
        if (!isCurRunning()) {
            return info;
        }
        info.put("distance", Double.toString(getCurDis()));
        info.put("speed", Double.toString(getSpeed()));
        Drivable drivable = getCurDrivable();
        info.put("drivable", drivable.getId());
        Road road = drivable.isLane() ? getCurLane().getBeLongRoad() : null;
        if (road != null) {
            info.put("road", road.getId());
            info.put("intersection", road.getEndIntersection().getId());
        }
        // add routing info
        StringBuilder route = new StringBuilder();
        for (Road r : controllerInfo.getRouter().getFollowingRoads()) {
            route.append(r.getId()).append(" ");
        }
        info.put("route", route.toString());

        return info;
    }

    public Lane getCurLane() {
        if (getCurDrivable().isLane())
            return (Lane) getCurDrivable();
        else
            return null;
    }

    public boolean hasDeadlock() {
        Vehicle fastPointer = this;
        Vehicle slowPointer = this;
        while (fastPointer != null && fastPointer.getCurBlocker() != null) {
            slowPointer = slowPointer.getCurBlocker();
            fastPointer = fastPointer.getCurBlocker().getCurBlocker();
            if (slowPointer == fastPointer) { // foeVehicle 存在死锁
                // deadlock detected
                return true; // foeVehicle 死锁不可动，当前 Vehicle 通行
            }
        }
        return false;
    }
}
