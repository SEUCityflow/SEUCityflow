package entity.archive;

import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;
import com.alibaba.fastjson.annotation.JSONField;
import entity.engine.Engine;
import entity.flow.Flow;
import entity.roadNet.roadNet.Drivable;
import entity.roadNet.roadNet.Intersection;
import entity.roadNet.roadNet.Lane;
import entity.roadNet.roadNet.Road;
import entity.roadNet.trafficLight.TrafficLight;
import entity.vehicle.laneChange.LaneChange;
import entity.vehicle.laneChange.Signal;
import entity.vehicle.router.Router;
import entity.vehicle.router.RouterType;
import entity.vehicle.vehicle.ControllerInfo;
import entity.vehicle.vehicle.LaneChangeInfo;
import entity.vehicle.vehicle.Vehicle;
import entity.vehicle.vehicle.VehicleInfo;
import util.Pair;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Archive {
    @JSONField(name = "trafficLightArchives", ordinal = 1)
    private List<TrafficLightArchive> trafficLightArchives;
    @JSONField(name = "flowArchives", ordinal = 2)
    private List<FlowArchive> flowArchives;
    @JSONField(name = "drivableArchives", ordinal = 3)
    private List<DrivableArchive> drivableArchives;
    @JSONField(name = "vehicleArchives", ordinal = 4)
    private List<VehicleArchive> vehicleArchives;
    @JSONField(name = "step", ordinal = 5)
    private int step;
    @JSONField(name = "activeVehicleCount", ordinal = 6)
    private int activeVehicleCount;
    @JSONField(name = "rnd", ordinal = 7)
    private Random rnd;
    @JSONField(name = "finishedVehicleCnt", ordinal = 8)
    private int finishedVehicleCnt;
    @JSONField(name = "cumulativeTravelTime", ordinal = 9)
    private double cumulativeTravelTime;
    @JSONField(name = "routeType", ordinal = 10)
    private String routeType;

    @JSONField(serialize = false, deserialize = false)
    private final Map<Integer, Pair<Vehicle, Integer>> vehiclePool;
    @JSONField(serialize = false, deserialize = false)
    private final Map<String, Vehicle> vehicleMap;
    @JSONField(serialize = false, deserialize = false)
    private static final Map<String, RouterType> typeNameMap = new HashMap<>();
    @JSONField(serialize = false, deserialize = false)
    private static final Map<RouterType, String> typeMap = new HashMap<>();

    static {
        typeNameMap.put("LENGTH", RouterType.LENGTH);
        typeNameMap.put("DURATION", RouterType.DURATION);
        typeNameMap.put("DYNAMIC", RouterType.DYNAMIC);
        typeNameMap.put("RANDOM", RouterType.RANDOM);
        typeMap.put(RouterType.LENGTH, "LENGTH");
        typeMap.put(RouterType.DURATION, "DURATION");
        typeMap.put(RouterType.DYNAMIC, "DYNAMIC");
        typeMap.put(RouterType.RANDOM, "RANDOM");
    }

    // engine -> archive
    private static Map<Integer, Pair<Vehicle, Integer>> copyVehiclePool(Map<Integer, Pair<Vehicle, Integer>> pool) {
        Map<Integer, Pair<Vehicle, Integer>> newPool = new HashMap<>();
        for (Map.Entry<Integer, Pair<Vehicle, Integer>> entry : pool.entrySet()) {
            Vehicle oldVehicle = entry.getValue().getKey();
            Vehicle newVehicle = new Vehicle(oldVehicle);
            newPool.put(newVehicle.getPriority(), new Pair<>(newVehicle, entry.getValue().getValue()));
        }
        for (Map.Entry<Integer, Pair<Vehicle, Integer>> entry : newPool.entrySet()) {
            Vehicle vehicle = entry.getValue().getKey();
            if (vehicle.hasPartner()) {
                if (vehicle.isReal()) {
                    vehicle.setShadow(getNewVehicle(newPool, vehicle.getPartner()));
                } else {
                    vehicle.setPartner(getNewVehicle(newPool, vehicle.getPartner()));
                }
            }
            vehicle.setCurLeader(getNewVehicle(newPool, vehicle.getCurLeader()));
            vehicle.setCurBlocker(getNewVehicle(newPool, vehicle.getCurBlocker()));

            LaneChange laneChange = vehicle.getLaneChange();
            laneChange.setTargetLeader(getNewVehicle(newPool, laneChange.getTargetLeader()));
            laneChange.setTargetFollower(getNewVehicle(newPool, laneChange.getTargetFollower()));
            if (laneChange.getSignalRecv() != null) {
                laneChange.setSignalRecv(getNewVehicle(newPool, laneChange.getSignalRecv().getSource()).getLaneChange().getSignalSend());
            }
        }
        return newPool;
    }

    private void loadVehicleInfo(VehicleInfoArchive vehicleInfoArchive, VehicleInfo vehicleInfo, Engine engine) {
        vehicleInfo.setSpeed(vehicleInfoArchive.getSpeed());
        vehicleInfo.setLen(vehicleInfoArchive.getLen());
        vehicleInfo.setWidth(vehicleInfoArchive.getWidth());
        vehicleInfo.setMaxPosAcc(vehicleInfoArchive.getMaxPosAcc());
        vehicleInfo.setMaxNegAcc(vehicleInfoArchive.getMaxNegAcc());
        vehicleInfo.setUsualPosAcc(vehicleInfoArchive.getUsualPosAcc());
        vehicleInfo.setUsualNegAcc(vehicleInfoArchive.getUsualNegAcc());
        vehicleInfo.setMinGap(vehicleInfoArchive.getMinGap());
        vehicleInfo.setMaxSpeed(vehicleInfoArchive.getMaxSpeed());
        vehicleInfo.setHeadwayTime(vehicleInfoArchive.getHeadwayTime());
        vehicleInfo.setYieldDistance(vehicleInfoArchive.getYieldDistance());
        vehicleInfo.setTurnSpeed(vehicleInfoArchive.getTurnSpeed());
        // route
        for (String string : vehicleInfoArchive.getRouteIds()) {
            vehicleInfo.getRoute().getRoute().add(engine.getRoadNet().getRoadById(string));
        }
    }

    private void loadControllerInfo(ControllerInfoArchive controllerInfoArchive, ControllerInfo controllerInfo, Engine engine) {
        controllerInfo.setDis(controllerInfoArchive.getDis());
        controllerInfo.setApproachingIntersectionDistance(controllerInfoArchive.getApproachingIntersectionDistance());
        controllerInfo.setGap(controllerInfoArchive.getGap());
        controllerInfo.setEnterLaneLinkTime(controllerInfoArchive.getEnterLaneLinkTime());
        controllerInfo.setEnd(controllerInfoArchive.isEnd());
        controllerInfo.setRunning(controllerInfoArchive.isRunning());
        controllerInfo.setDrivable(engine.getRoadNet().getDrivableById(controllerInfoArchive.getDrivableId()));
        controllerInfo.setPrevDrivable(engine.getRoadNet().getDrivableById(controllerInfoArchive.getPrevDrivableId()));
        controllerInfo.setLeader(vehicleMap.get(controllerInfoArchive.getLeaderId()));
        controllerInfo.setBlocker(vehicleMap.get(controllerInfoArchive.getBlockerId()));
        // router
        Router router = controllerInfo.getRouter();
        router.setRnd(engine.getRnd());
        List<Pair<String, Integer>> routeIdsArchive = controllerInfoArchive.getRouteIds();
        List<String> anchorPointsIdsArchive = controllerInfoArchive.getAnchorPointsIds();
        Pair<String, Integer> anchorPointArchive = controllerInfoArchive.getNowAnchorPoint();
        List<Pair<Road, Integer>> route = new ArrayList<>();
        List<Road> anchorPoints = new ArrayList<>();
        // route
        for (Pair<String, Integer> road : routeIdsArchive) {
            route.add(new Pair<>(engine.getRoadNet().getRoadById(road.getKey()), road.getValue()));
        }
        router.setRoute(route);
        // anchorPoints
        for (String road : anchorPointsIdsArchive) {
            anchorPoints.add(engine.getRoadNet().getRoadById(road));
        }
        router.setAnchorPoints(anchorPoints);
        router.setNowAnchorPoint(new Pair<>(engine.getRoadNet().getRoadById(anchorPointArchive.getKey()), anchorPointArchive.getValue()));
        router.setiCurRoad(route.listIterator());
        router.setType(typeNameMap.get(controllerInfoArchive.getType()));
    }

    private void loadLaneChangeInfo(LaneChangeInfoArchive laneChangeInfoArchive, LaneChangeInfo laneChangeInfo) {
        laneChangeInfo.setPartner(vehicleMap.get(laneChangeInfoArchive.getPartnerId()));
        laneChangeInfo.setPartnerType(laneChangeInfoArchive.getPartnerType());
        laneChangeInfo.setOffSet(laneChangeInfoArchive.getOffSet());
        laneChangeInfo.setSegmentIndex(laneChangeInfoArchive.getSegmentIndex());
    }

    private void loadLaneChange(LaneChangeArchive laneChangeArchive, LaneChange laneChange, Engine engine) {
        laneChange.setTargetLeader(vehicleMap.get(laneChangeArchive.getLaneChangeLeaderId()));
        laneChange.setTargetFollower(vehicleMap.get(laneChangeArchive.getLaneChangeFollowerId()));
        laneChange.setChanging(laneChangeArchive.isLaneChanging());
        laneChange.setLastChangeTime(laneChangeArchive.getLaneChangeLastTime());
        if (laneChangeArchive.getLaneChangeUrgency() != -Integer.MAX_VALUE) {
            Signal send = new Signal();
            laneChange.setSignalSend(send);
            send.setUrgency(laneChangeArchive.getLaneChangeUrgency());
            send.setTarget((Lane) engine.getRoadNet().getDrivableById(laneChangeArchive.getLaneChangeTargetId()));
            send.setDirection(laneChangeArchive.getLaneChangeDirection());
        }
    }

    private void buildVehiclePool(Engine engine) {
        for (VehicleArchive archive : vehicleArchives) {
            Vehicle vehicle = new Vehicle();
            vehicle.setPriority(archive.getPriority());
            vehicle.setId(archive.getId());
            vehicle.setEnterTime(archive.getEnterTime());
            vehicle.setEngine(engine);
            vehiclePool.put(vehicle.getPriority(), new Pair<>(vehicle, rnd.nextInt(engine.getThreadNum())));
            vehicleMap.put(vehicle.getId(), vehicle);
        }
        for (VehicleArchive archive : vehicleArchives) {
            Vehicle vehicle = vehiclePool.get(archive.getPriority()).getKey();
            loadVehicleInfo(archive.getVehicleInfoArchive(), vehicle.getVehicleInfo(), engine);
            vehicle.setControllerInfo(vehicle.getControllerInfo());
            loadControllerInfo(archive.getControllerInfoArchive(), vehicle.getControllerInfo(), engine);
            loadLaneChangeInfo(archive.getLaneChangeInfoArchive(), vehicle.getLaneChangeInfo());
            loadLaneChange(archive.getLaneChangeArchive(), vehicle.getLaneChange(), engine);
            if (vehicle.getLaneChange().getSignalSend() != null) {
                vehicle.getLaneChange().getSignalSend().setSource(vehicle);
            }
        }
        for (VehicleArchive archive : vehicleArchives) {
            if (archive.getLaneChangeArchive().getLaneChangeRecvId() != null) {
                Vehicle vehicle = vehiclePool.get(archive.getPriority()).getKey();
                Vehicle from = vehicleMap.get(archive.getLaneChangeArchive().getLaneChangeRecvId());
                vehicle.getLaneChange().setSignalRecv(from.getLaneChange().getSignalRecv());
            }
        }
    }

    private static Vehicle getNewVehicle(Map<Integer, Pair<Vehicle, Integer>> vehiclePool, Vehicle old) {
        if (old == null) {
            return null;
        }
        int priority = old.getPriority();
        return vehiclePool.get(priority).getKey();
    }

    private void archiveDrivable(Drivable drivable, DrivableArchive drivableArchive) {
        drivableArchive.setDrivableId(drivable.getId());
        for (Vehicle vehicle : drivable.getVehicles()) {
            drivableArchive.getVehicles().add(getNewVehicle(vehiclePool, vehicle).getId());
        }
        if (drivable.isLane()) {
            Lane lane = (Lane) drivable;
            for (Vehicle vehicle : lane.getWaitingBuffer()) {
                drivableArchive.getWaitingBuffer().add(getNewVehicle(vehiclePool, vehicle).getId());
            }
            drivableArchive.setHistory(lane.getHistory());
            drivableArchive.setHistoryVehicleNum(lane.getHistoryVehicleNum());
            drivableArchive.setHistoryAverageSpeed(lane.getHistoryAverageSpeed());
        }
    }

    private void archiveFlow(Flow flow, FlowArchive flowArchive) {
        flowArchive.setFlowId(flow.getId());
        flowArchive.setCurrentTime(flow.getCurrentTime());
        flowArchive.setNowTime(flow.getNowTime());
        flowArchive.setCnt(flow.getCnt());
    }

    private void archiveTrafficLight(TrafficLight light, TrafficLightArchive trafficLightArchive) {
        if (light == null) {
            return;
        }
        trafficLightArchive.setTrafficId(light.getIntersection().getId());
        trafficLightArchive.setCurPhaseIndex(light.getCurPhaseIndex());
        trafficLightArchive.setRemainDuration(light.getRemainDuration());
    }

    private void archiveVehicleInfo(VehicleInfo vehicleInfo, VehicleInfoArchive vehicleInfoArchive) {
        vehicleInfoArchive.setSpeed(vehicleInfo.getSpeed());
        vehicleInfoArchive.setLen(vehicleInfo.getLen());
        vehicleInfoArchive.setWidth(vehicleInfo.getWidth());
        vehicleInfoArchive.setMaxPosAcc(vehicleInfo.getMaxPosAcc());
        vehicleInfoArchive.setMaxNegAcc(vehicleInfo.getMaxNegAcc());
        vehicleInfoArchive.setUsualPosAcc(vehicleInfo.getMaxPosAcc());
        vehicleInfoArchive.setUsualNegAcc(vehicleInfo.getMaxNegAcc());
        vehicleInfoArchive.setMinGap(vehicleInfo.getMinGap());
        vehicleInfoArchive.setMaxSpeed(vehicleInfo.getMaxSpeed());
        vehicleInfoArchive.setHeadwayTime(vehicleInfo.getHeadwayTime());
        vehicleInfoArchive.setYieldDistance(vehicleInfo.getYieldDistance());
        vehicleInfoArchive.setTurnSpeed(vehicleInfo.getTurnSpeed());
        // route
        List<String> routeIds = vehicleInfoArchive.getRouteIds();
        for (Road road : vehicleInfo.getRoute().getRoute()) {
            routeIds.add(road.getId());
        }
    }

    private void archiveControllerInfo(ControllerInfo controllerInfo, ControllerInfoArchive controllerInfoArchive) {
        controllerInfoArchive.setDis(controllerInfo.getDis());
        controllerInfoArchive.setDrivableId(controllerInfo.getDrivable() != null ? controllerInfo.getDrivable().getId() : null);
        controllerInfoArchive.setPrevDrivableId(controllerInfo.getPrevDrivable() != null ? controllerInfo.getPrevDrivable().getId() : null);
        controllerInfoArchive.setApproachingIntersectionDistance(controllerInfo.getApproachingIntersectionDistance());
        controllerInfoArchive.setGap(controllerInfo.getGap());
        controllerInfoArchive.setEnterLaneLinkTime(controllerInfo.getEnterLaneLinkTime());
        controllerInfoArchive.setLeaderId(controllerInfo.getLeader() != null ? controllerInfo.getLeader().getId() : null);
        controllerInfoArchive.setBlockerId(controllerInfo.getBlocker() != null ? controllerInfo.getBlocker().getId() : null);
        controllerInfoArchive.setEnd(controllerInfo.isEnd());
        controllerInfoArchive.setRunning(controllerInfo.isRunning());
        // router
        List<Pair<String, Integer>> routeIds = controllerInfoArchive.getRouteIds();
        for (Pair<Road, Integer> road : controllerInfo.getRouter().getRoute()) {
            routeIds.add(new Pair<>(road.getKey().getId(), road.getValue()));
        }
        List<String> anchorPointsIds = controllerInfoArchive.getAnchorPointsIds();
        for (Road road : controllerInfo.getRouter().getAnchorPoints()) {
            anchorPointsIds.add(road.getId());
        }
        Pair<Road, Integer> anchorPoint = controllerInfo.getRouter().getNowAnchorPoint();
        controllerInfoArchive.setNowAnchorPoint(new Pair<>(anchorPoint.getKey().getId(), anchorPoint.getValue()));
        controllerInfoArchive.setType(typeMap.get(controllerInfo.getRouter().getType()));
    }

    private void archiveLaneChangeInfo(LaneChangeInfo laneChangeInfo, LaneChangeInfoArchive laneChangeInfoArchive) {
        laneChangeInfoArchive.setPartnerType(laneChangeInfo.getPartnerType());
        laneChangeInfoArchive.setPartnerId(laneChangeInfo.getPartner() != null ? laneChangeInfo.getPartner().getId() : null);
        laneChangeInfoArchive.setOffSet(laneChangeInfo.getOffSet());
        laneChangeInfoArchive.setSegmentIndex(laneChangeInfo.getSegmentIndex());
    }

    private void archiveLaneChange(LaneChange laneChange, LaneChangeArchive laneChangeArchive) {
        laneChangeArchive.setLaneChangeLeaderId(laneChange.getTargetLeader() != null ? laneChange.getTargetLeader().getId() : null);
        laneChangeArchive.setLaneChangeFollowerId(laneChange.getTargetFollower() != null ? laneChange.getTargetFollower().getId() : null);
        laneChangeArchive.setLaneChangeWaitingTime(laneChange.getWaitingTime());
        laneChangeArchive.setLaneChanging(laneChange.isChanging());
        laneChangeArchive.setLaneChangeLastTime(laneChange.getLastChangeTime());
        if (laneChange.getSignalSend() != null) {
            laneChangeArchive.setLaneChangeUrgency(laneChange.getSignalSendUrgency());
            laneChangeArchive.setLaneChangeDirection(laneChange.getDirection());
            laneChangeArchive.setLaneChangeTargetId(laneChange.getTarget() != null ? laneChange.getTarget().getId() : null);
        }
        if (laneChange.getSignalRecv() != null) {
            laneChangeArchive.setLaneChangeRecvId(laneChange.getSignalRecv().getSource().getId());
        }
    }

    private void archiveVehicle(Vehicle vehicle, VehicleArchive vehicleArchive) {
        vehicleArchive.setPriority(vehicle.getPriority());
        vehicleArchive.setId(vehicle.getId());
        vehicleArchive.setEnterTime(vehicle.getEnterTime());
        // vehicleInfo
        VehicleInfoArchive vehicleInfoArchive = vehicleArchive.getVehicleInfoArchive();
        VehicleInfo vehicleInfo = vehicle.getVehicleInfo();
        archiveVehicleInfo(vehicleInfo, vehicleInfoArchive);
        // controllerInfo
        ControllerInfoArchive controllerInfoArchive = vehicleArchive.getControllerInfoArchive();
        ControllerInfo controllerInfo = vehicle.getControllerInfo();
        archiveControllerInfo(controllerInfo, controllerInfoArchive);
        // laneChangeInfo
        LaneChangeInfoArchive laneChangeInfoArchive = vehicleArchive.getLaneChangeInfoArchive();
        LaneChangeInfo laneChangeInfo = vehicle.getLaneChangeInfo();
        archiveLaneChangeInfo(laneChangeInfo, laneChangeInfoArchive);
        // laneChange
        LaneChange laneChange = vehicle.getLaneChange();
        LaneChangeArchive laneChangeArchive = vehicleArchive.getLaneChangeArchive();
        archiveLaneChange(laneChange, laneChangeArchive);
    }

    public Archive() {
        vehiclePool = new HashMap<>();
        vehicleMap = new HashMap<>();
        trafficLightArchives = new ArrayList<>();
        flowArchives = new ArrayList<>();
        drivableArchives = new ArrayList<>();
        vehicleArchives = new ArrayList<>();
    }

    // engine -> archive
    public Archive(Engine engine) {
        trafficLightArchives = new ArrayList<>();
        flowArchives = new ArrayList<>();
        drivableArchives = new ArrayList<>();
        vehicleArchives = new ArrayList<>();
        vehicleMap = new HashMap<>();

        step = engine.getStep();
        activeVehicleCount = engine.getActiveVehicleCount();
        rnd = new Random(engine.getSeed());
        finishedVehicleCnt = engine.getFinishedVehicleCnt();
        cumulativeTravelTime = engine.getCumulativeTravelTime();
        routeType = typeMap.get(engine.getRouterType());

        vehiclePool = copyVehiclePool(engine.getVehiclePool());
        for (Map.Entry<Integer, Pair<Vehicle, Integer>> entry : vehiclePool.entrySet()) {
            Vehicle vehicle = entry.getValue().getKey();
            vehicleMap.put(vehicle.getId(), vehicle);
            VehicleArchive vehicleArchive = new VehicleArchive();
            vehicleArchives.add(vehicleArchive);
            archiveVehicle(vehicle, vehicleArchive);
        }
        for (Drivable drivable : engine.getRoadNet().getDrivables()) {
            DrivableArchive drivableArchive = new DrivableArchive();
            drivableArchives.add(drivableArchive);
            archiveDrivable(drivable, drivableArchive);
        }
        for (Flow flow : engine.getFlows()) {
            FlowArchive flowArchive = new FlowArchive();
            flowArchives.add(flowArchive);
            archiveFlow(flow, flowArchive);
        }
        for (Intersection intersection : engine.getRoadNet().getIntersections()) {
            TrafficLight light = intersection.getTrafficLight();
            TrafficLightArchive trafficLightArchive = new TrafficLightArchive();
            trafficLightArchives.add(trafficLightArchive);
            archiveTrafficLight(light, trafficLightArchive);
        }
    }

    // archive -> engine
    public void resume(Engine engine) {
        engine.setStep(step);
        engine.setActiveVehicleCount(activeVehicleCount);
        engine.setVehiclePool(vehiclePool);
        engine.setVehicleMap(vehicleMap);
        engine.setRnd(rnd);
        engine.setFinishedVehicleCnt(finishedVehicleCnt);
        engine.setCumulativeTravelTime(cumulativeTravelTime);
        engine.setRouterType(typeNameMap.get(routeType));
        // vehicle
        for (Set<Vehicle> vehicleSet : engine.getThreadVehiclePool()) {
            vehicleSet.clear();
        }
        for (Map.Entry<Integer, Pair<Vehicle, Integer>> entry : engine.getVehiclePool().entrySet()) {
            Vehicle vehicle = entry.getValue().getKey();
            int threadIndex = entry.getValue().getValue();
            engine.getThreadVehiclePool().get(threadIndex).add(vehicle);
        }
        // drivable
        for (DrivableArchive archive : drivableArchives) {
            Drivable drivable = engine.getRoadNet().getDrivableById(archive.getDrivableId());
            drivable.getVehicles().clear();
            for (String vehicleId : archive.getVehicles()) {
                drivable.getVehicles().add(vehicleMap.get(vehicleId));
            }
            if (drivable.isLane()) {
                Lane lane = (Lane) drivable;
                lane.getWaitingBuffer().clear();
                for (String vehicleId : archive.getWaitingBuffer()) {
                    lane.getWaitingBuffer().add(vehicleMap.get(vehicleId));
                }
                lane.setHistory(archive.getHistory());
                lane.setHistoryVehicleNum(archive.getHistoryVehicleNum());
                lane.setHistoryAverageSpeed(archive.getHistoryAverageSpeed());
            }
        }
        // flow
        for (FlowArchive archive : flowArchives) {
            Flow flow = engine.getFlowMap().get(archive.getFlowId());
            flow.setCurrentTime(archive.getCurrentTime());
            flow.setNowTime(archive.getNowTime());
            flow.setCnt(archive.getCnt());
        }
        // traffic
        for (TrafficLightArchive archive : trafficLightArchives) {
            if (archive.getTrafficId() == null) {
                return;
            }
            TrafficLight light = engine.getRoadNet().getInterMap().get(archive.getTrafficId()).getTrafficLight();
            light.setRemainDuration(archive.getRemainDuration());
            light.setCurPhaseIndex(archive.getCurPhaseIndex());
        }
    }

    // archive -> file
    public void dump(String fileName) {
        try {
            JSONWriter writer = new JSONWriter(new FileWriter(fileName));
            writer.startObject();
            // write trafficLight
            writer.writeKey("trafficLightArchives");
            writer.startArray();
            for (TrafficLightArchive trafficLightArchive : trafficLightArchives) {
                writer.writeValue(trafficLightArchive);
            }
            writer.endArray();
            // write flow
            writer.writeKey("flowArchives");
            writer.startArray();
            for (FlowArchive flowArchive : flowArchives) {
                writer.writeValue(flowArchive);
            }
            writer.endArray();
            // write drivable
            writer.writeKey("drivableArchives");
            writer.startArray();
            for (DrivableArchive drivableArchive : drivableArchives) {
                writer.writeValue(drivableArchive);
            }
            writer.endArray();
            // write vehicle
            writer.writeKey("vehicleArchives");
            writer.startArray();
            for (VehicleArchive vehicleArchive : vehicleArchives) {
                writer.writeValue(vehicleArchive);
            }
            writer.endArray();
            // write engine
            writer.writeKey("step");
            writer.writeValue(step);
            writer.writeKey("activeVehicleCount");
            writer.writeValue(activeVehicleCount);
            writer.writeKey("rnd");
            writer.writeValue(rnd);
            writer.writeKey("finishedVehicleCnt");
            writer.writeValue(finishedVehicleCnt);
            writer.writeKey("cumulativeTravelTime");
            writer.writeValue(cumulativeTravelTime);
            writer.writeKey("routeType");
            writer.writeValue(routeType);
            writer.endObject();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // file -> archive
    public static Archive load(Engine engine, String fileName) {
        Archive archive = new Archive();
        try {
            JSONReader reader = new JSONReader(new FileReader(fileName));
            reader.startObject();
            // read traffic
            reader.readString();
            reader.startArray();
            while (reader.hasNext()) {
                archive.getTrafficLightArchives().add(reader.readObject(TrafficLightArchive.class));
            }
            reader.endArray();
            // read flow
            reader.readString();
            reader.startArray();
            while (reader.hasNext()) {
                archive.getFlowArchives().add(reader.readObject(FlowArchive.class));
            }
            reader.endArray();
            // read drivable
            reader.readString();
            reader.startArray();
            while (reader.hasNext()) {
                archive.getDrivableArchives().add(reader.readObject(DrivableArchive.class));
            }
            reader.endArray();
            // read vehicle
            reader.readString();
            reader.startArray();
            while (reader.hasNext()) {
                archive.getVehicleArchives().add(reader.readObject(VehicleArchive.class));
            }
            reader.endArray();
            // read engine
            reader.readString();
            archive.setStep(reader.readInteger());
            reader.readString();
            archive.setActiveVehicleCount(reader.readInteger());
            reader.readString();
            archive.setRnd(reader.readObject(Random.class));
            reader.readString();
            archive.setFinishedVehicleCnt(reader.readInteger());
            reader.readString();
            archive.setCumulativeTravelTime(reader.readInteger());
            reader.readString();
            archive.setRouteType(reader.readString());
            reader.endObject();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        archive.buildVehiclePool(engine);
        return archive;
    }

    // set / get
    public List<TrafficLightArchive> getTrafficLightArchives() {
        return trafficLightArchives;
    }

    public void setTrafficLightArchives(List<TrafficLightArchive> trafficLightArchives) {
        this.trafficLightArchives = trafficLightArchives;
    }

    public List<FlowArchive> getFlowArchives() {
        return flowArchives;
    }

    public void setFlowArchives(List<FlowArchive> flowArchives) {
        this.flowArchives = flowArchives;
    }

    public List<DrivableArchive> getDrivableArchives() {
        return drivableArchives;
    }

    public void setDrivableArchives(List<DrivableArchive> drivableArchives) {
        this.drivableArchives = drivableArchives;
    }

    public List<VehicleArchive> getVehicleArchives() {
        return vehicleArchives;
    }

    public void setVehicleArchives(List<VehicleArchive> vehicleArchives) {
        this.vehicleArchives = vehicleArchives;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getActiveVehicleCount() {
        return activeVehicleCount;
    }

    public void setActiveVehicleCount(int activeVehicleCount) {
        this.activeVehicleCount = activeVehicleCount;
    }

    public Random getRnd() {
        return rnd;
    }

    public void setRnd(Random rnd) {
        this.rnd = rnd;
    }

    public int getFinishedVehicleCnt() {
        return finishedVehicleCnt;
    }

    public void setFinishedVehicleCnt(int finishedVehicleCnt) {
        this.finishedVehicleCnt = finishedVehicleCnt;
    }

    public double getCumulativeTravelTime() {
        return cumulativeTravelTime;
    }

    public void setCumulativeTravelTime(double cumulativeTravelTime) {
        this.cumulativeTravelTime = cumulativeTravelTime;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }
}
