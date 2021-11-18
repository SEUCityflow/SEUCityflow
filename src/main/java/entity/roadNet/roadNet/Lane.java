package entity.roadNet.roadNet;

import entity.vehicle.vehicle.Vehicle;

import java.util.*;

class HistoryRecord {
    public int vehicleNum;
    public double averageSpeed;
    public HistoryRecord(int vehicleNum, double averageSpeed) {
        this.vehicleNum = vehicleNum;
        this.averageSpeed = averageSpeed;
    }
}

public class Lane extends  Drivable{
    private int laneIndex;
    private List<Segment> segments;
    private List<LaneLink> laneLinks;
    private Road belongRoad;
    private List<Vehicle> waitingBuffer;
    private List<HistoryRecord> history;
    private int historyVehicleNum;
    private double historyAverageSpeed;
    private static final int historyLen = 240;

    public Lane() {
        super();
        drivableType = DrivableType.LANE;
        segments = new ArrayList<Segment>();
        laneLinks = new ArrayList<LaneLink>();
        waitingBuffer = new LinkedList<Vehicle>();
        history = new LinkedList<HistoryRecord>();
    }

    public Lane(double width, double maxSpeed, int laneIndex, Road belongRoad) {
        super();
        this.width = width;
        this.maxSpeed = maxSpeed;
        this.laneIndex = laneIndex;
        this.belongRoad = belongRoad;
        drivableType = DrivableType.LANE;
        segments = new ArrayList<Segment>();
        laneLinks = new ArrayList<LaneLink>();
        waitingBuffer = new LinkedList<Vehicle>();
        history = new LinkedList<HistoryRecord>();
    }

    @Override
    public String getId() {
        return belongRoad.getId() + "_" + getLaneIndex();
    }

    public Road getBeLongRoad()  {
        return belongRoad;
    }

    public boolean available(Vehicle vehicle)  { // 当前 lane 是否有足够空间，用于 vehicle 初始 lane 的选择
        if (!vehicles.isEmpty()) {
            Vehicle tail = vehicles.get(vehicles.size() - 1);
            return tail.getCurDis() > tail.getLen() + vehicle.getMinGap();
        } else {
            return true;
        }
    }

    public boolean canEnter(Vehicle vehicle)  { // 当前车辆是否可进入，用于 drivable 的变换
        if (!vehicles.isEmpty()) {
            Vehicle tail = vehicles.get(vehicles.size() - 1);
            return tail.getCurDis() > tail.getLen() + vehicle.getLen() || tail.getSpeed() >= 2;
        } else {
            return true;
        }
    }

    public int getLaneIndex()  {
        return laneIndex;
    }

    public Lane getInnerLane()  {
        return laneIndex > 0 ? belongRoad.getLanes().get(laneIndex - 1) : null;
    }

    public Lane getOuterLane() {
        int laneNum = belongRoad.getLanes().size();
        return laneIndex < laneNum - 1 ? belongRoad.getLanes().get(laneIndex + 1) : null;
    }

    public List<LaneLink> getLaneLinks() {
        return laneLinks;
    }

    public Intersection getStartIntersection() {
        return belongRoad.getStartIntersection();
    }

    public Intersection getEndIntersection() {
        return belongRoad.getEndIntersection();
    }

    public List<LaneLink> getLaneLinksToRoad(Road road) {
        List<LaneLink> ret = new ArrayList<LaneLink>();
        for(LaneLink laneLink : laneLinks) {
            if (laneLink.getEndLane().getBeLongRoad() == road) {
                ret.add(laneLink);
            }
        }
        return ret;
    }

    public void reset() {
        waitingBuffer.clear();
        super.reset();
    }

    public List<Vehicle> getWaitingBuffer()  {
        return waitingBuffer;
    }

    public void pushWaitingVehicle(Vehicle vehicle)  {
        waitingBuffer.add(vehicle);
    }

    public void buildSegmentation(int numSegments)  {
        for (int i = 0; i < numSegments; i++) {
            Segment segment = new Segment();
            segment.setIndex(i);
            segment.setBelongLane(this);
            segment.setStartPos(i * length / numSegments);
            segment.setEndPos((i + 1) * length / numSegments);
            segments.add(i, segment);
        }
    }

    public void initSegments()  {
//        Iterator<Vehicle> iter = vehicles.iterator();
//        for (int i = segments.size() - 1; i >= 0; i--) {
//            Segment segment = segments.get(i);
//            segment.setVehicles(new ArrayList<Vehicle>());
//            while (iter.hasNext()) {
//                Vehicle vehicle = iter.next();
//                if (vehicle.getCurDis() >= segment.getStartPos()) {
//                    segment.getVehicles().add(vehicle);
//                    //vehicle.setSegmentIndex(segment.getIndex());
//                    break;
//                }
//            }
//        }
    }

    public Segment getSegment(int index)  {
        return segments.get(index);
    }

    public List<Segment> getSegments()  {
        return segments;
    }

    public int getSegmentNum()  {
        return segments.size();
    }

    public List<Vehicle> getVehiclesBeforeDistance(double dis, int segmentIndex, double deltaDis)  {
        List<Vehicle> ret = new ArrayList<Vehicle>();
        for (int i = segmentIndex; i >= 0; i--) {
            Segment segment = getSegment(i);
            List<Vehicle> vehicles = segment.getVehicles();
            for (Vehicle vehicle : vehicles) {
                if (vehicle.getCurDis() < dis - deltaDis) {
                    return ret;
                }
                if (vehicle.getCurDis() < dis) {
                    ret.add(vehicle);
                }
            }
        }
        return ret;
    }

    public Vehicle getVehicleBeforeDistance(double dis, int segmentIndex)  {
        for (int i = segmentIndex; i>= 0; i--) {
            List<Vehicle> vehicles = getSegment(i).getVehicles();
            for (Vehicle vehicle : vehicles) {
                if (vehicle.getCurDis() < dis) {
                    return vehicle;
                }
            }
        }
        return null;
    }

    public Vehicle getVehicleAfterDistance(double dis, int segmentIndex)  {
        for (int i = segmentIndex; i < getSegmentNum(); i++) {
            List<Vehicle> vehicles = getSegment(i).getVehicles();
            for (Vehicle vehicle : vehicles) {
                if (vehicle.getCurDis() >= dis) {
                    return vehicle;
                }
            }
        }
        return null;
    }

    public void updateHistory()  {
        double speedSum = historyVehicleNum * historyAverageSpeed;
        while (history.size() > historyLen) {
            historyVehicleNum -= history.get(0).vehicleNum;
            speedSum -= history.get(0).vehicleNum * history.get(0).averageSpeed;
            history.remove(0);
        }
        double curSpeedSum = 0;
        int vehicleNum = getVehicles().size();
        historyVehicleNum += vehicleNum;
        for (Vehicle vehicle : getVehicles()) {
            curSpeedSum += vehicle.getSpeed();
        }
        history.add(new HistoryRecord(vehicleNum, vehicleNum != 0 ? curSpeedSum / vehicleNum : 0));
        historyAverageSpeed = historyVehicleNum != 0 ? speedSum / historyVehicleNum : 0;
    }

    public int getHistoryVehicleNum()  {
        return historyVehicleNum;
    }

    public double getHistoryAverageSpeed()  {
        return historyAverageSpeed;
    }
}
