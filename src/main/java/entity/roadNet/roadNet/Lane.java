package entity.roadNet.roadNet;

import entity.History.HistoryRecord;
import entity.vehicle.vehicle.Vehicle;

import java.util.*;

public class Lane extends Drivable {
    private int laneIndex;
    private List<Segment> segments;
    private List<LaneLink> laneLinks;
    private Road belongRoad;
    private List<Vehicle> waitingBuffer;
    private List<HistoryRecord> history;
    private List<Double> queueingTimeList;
    private List<Double> periodTimeList;
    private int historyVehicleNum;
    private double historyAverageSpeed;
    private double sumQueueingTime;
    private double sumPeriodTime;
    private static final int historyLen = 240;
    private static final int carToBeCalculate = 120;

    public Lane() {
        super();
        drivableType = DrivableType.LANE;
        segments = new ArrayList<>();
        laneLinks = new ArrayList<>();
        waitingBuffer = new LinkedList<>();
        history = new LinkedList<>();
        queueingTimeList = new LinkedList<>();
        periodTimeList = new LinkedList<>();
    }

    public Lane(double width, double maxSpeed, int laneIndex, Road belongRoad) {
        super();
        this.width = width;
        this.maxSpeed = maxSpeed;
        this.laneIndex = laneIndex;
        this.belongRoad = belongRoad;
        drivableType = DrivableType.LANE;
        segments = new ArrayList<>();
        laneLinks = new ArrayList<>();
        waitingBuffer = new LinkedList<>();
        history = new LinkedList<>();
        queueingTimeList = new LinkedList<>();
        periodTimeList = new LinkedList<>();
    }

    @Override
    public String getId() {
        return belongRoad.getId() + "_" + getLaneIndex();
    }

    public Road getBeLongRoad() {
        return belongRoad;
    }

    // 用于 waitingBuffer 进入 lane
    public boolean available(Vehicle vehicle) {
        if (!vehicles.isEmpty()) {
            Vehicle tail = getLastVehicle();
            return tail.getCurDis() > tail.getLen() + vehicle.getMinGap();
        } else {
            return true;
        }
    }

    // 用于 laneLink 进入 lane
    public boolean canEnter(Vehicle vehicle) {
        if (!vehicles.isEmpty()) {
            Vehicle tail = getLastVehicle();
            return tail.getCurDis() > tail.getLen() + vehicle.getLen() || tail.getSpeed() >= 2;
        } else {
            return true;
        }
    }

    public int getLaneIndex() {
        return laneIndex;
    }

    public Lane getInnerLane() {
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
        List<LaneLink> ret = new ArrayList<>();
        for (LaneLink laneLink : laneLinks) {
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

    public List<Vehicle> getWaitingBuffer() {
        return waitingBuffer;
    }

    public void pushWaitingVehicle(Vehicle vehicle) {
        waitingBuffer.add(vehicle);
    }

    public void buildSegmentation(int numSegments) {
        for (int i = 0; i < numSegments; i++) {
            Segment segment = new Segment(i, this, i * length / numSegments, (i + 1) * length / numSegments);
            segments.add(i, segment);
        }
    }

    public void initSegments() {
        ListIterator<Vehicle> iter = vehicles.listIterator();
        int start = 0;
        int end = 0;
        for (int i = segments.size() - 1; i >= 0; i--) {
            Segment segment = segments.get(i);
            segment.disband();
            while (iter.hasNext()) {
                Vehicle vehicle = iter.next();
                if (vehicle.getCurDis() >= segment.getStartPos()) {
                    end++;
                    vehicle.setSegmentIndex(i);
                } else {
                    iter.previous();
                    break;
                }
            }
            segment.setVehicles(new LinkedList<>(vehicles.subList(start, end)));
            if (segment.canGroup()) {
                segment.buildGroup();
            }
            start = end;
        }
    }

    public Segment getSegment(int index) {
        return segments.get(index);
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public int getSegmentNum() {
        return segments.size();
    }

    public List<Vehicle> getVehiclesBeforeDistance(double dis, int segmentIndex, double deltaDis) {
        List<Vehicle> ret = new ArrayList<>();
        for (int i = segmentIndex; i >= 0; i--) {
            Segment segment = getSegment(i);
            List<Vehicle> list = segment.getVehicles();
            for (Vehicle vehicle : list) {
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

    public Vehicle getVehicleBeforeDistance(double dis, int segmentIndex) {
        for (int i = segmentIndex; i >= 0; i--) {
            Segment segment = getSegment(i);
            List<Vehicle> list = segment.getVehicles();
            for (Vehicle vehicle : list) {
                if (vehicle.getCurDis() < dis) {
                    return vehicle;
                }
            }
        }
        return null;
    }

    public Vehicle getVehicleAfterDistance(double dis, int segmentIndex) {
        for (int i = segmentIndex; i < getSegmentNum(); i++) {
            Segment segment = getSegment(i);
            List<Vehicle> list = segment.getVehicles();
            ListIterator<Vehicle> iter = list.listIterator(list.size());
            while (iter.hasPrevious()) {
                Vehicle vehicle = iter.previous();
                if (vehicle.getCurDis() >= dis) {
                    return vehicle;
                }
            }
        }
        return null;
    }

    public void updateHistory() {
        double speedSum = historyVehicleNum * historyAverageSpeed;
        while (history.size() >= historyLen) {
            historyVehicleNum -= history.get(0).getVehicleNum();
            speedSum -= history.get(0).getVehicleNum() * history.get(0).getAverageSpeed();
            history.remove(0);
        }
        double curSpeedSum = 0;
        int vehicleNum = getVehicles().size();
        for (Vehicle vehicle : getVehicles()) {
            curSpeedSum += vehicle.getSpeed();
        }
        historyVehicleNum += vehicleNum;
        speedSum += curSpeedSum;
        history.add(new HistoryRecord(vehicleNum, vehicleNum != 0 ? curSpeedSum / vehicleNum : 0));
        historyAverageSpeed = historyVehicleNum != 0 ? speedSum / historyVehicleNum : 0;
    }

    public void addQueueingTime(double time) {
        while (queueingTimeList.size() >= carToBeCalculate) {
            sumQueueingTime -= queueingTimeList.get(0);
            queueingTimeList.remove(0);
        }
        queueingTimeList.add(time);
        sumQueueingTime += time;
    }

    public void addPeriodTime(double time) {
        while (periodTimeList.size() >= 6) {
            sumPeriodTime -= periodTimeList.get(0);
            periodTimeList.remove(0);
        }
        periodTimeList.add(time);
        sumPeriodTime += time;
    }

    public double getPeriod() {
        return periodTimeList.size() == 0 ? 0 : sumPeriodTime / periodTimeList.size() / 2;
    }

    private double getAverageQueueTime() {
        return queueingTimeList.size() == 0 ? 0 : sumQueueingTime / queueingTimeList.size();
    }

    public double getQueueingTimeIndex() {
        return getPeriod() == 0 ? 0 : getAverageQueueTime() / getPeriod();
    }

    public int getHistoryVehicleNum() {
        return historyVehicleNum;
    }

    public double getHistoryAverageSpeed() {
        return historyAverageSpeed;
    }

    public void setLaneIndex(int laneIndex) {
        this.laneIndex = laneIndex;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }

    public void setLaneLinks(List<LaneLink> laneLinks) {
        this.laneLinks = laneLinks;
    }

    public Road getBelongRoad() {
        return belongRoad;
    }

    public void setBelongRoad(Road belongRoad) {
        this.belongRoad = belongRoad;
    }

    public void setWaitingBuffer(List<Vehicle> waitingBuffer) {
        this.waitingBuffer = waitingBuffer;
    }

    public List<HistoryRecord> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryRecord> history) {
        this.history = history;
    }

    public void setHistoryVehicleNum(int historyVehicleNum) {
        this.historyVehicleNum = historyVehicleNum;
    }

    public void setHistoryAverageSpeed(double historyAverageSpeed) {
        this.historyAverageSpeed = historyAverageSpeed;
    }

    public List<Double> getQueueingTimeList() {
        return queueingTimeList;
    }

    public List<Double> getPeriodTimeList() {
        return periodTimeList;
    }

    public void setQueueingTimeList(List<Double> queueingTimeList) {
        this.queueingTimeList = queueingTimeList;
    }

    public void setPeriodTimeList(List<Double> periodTimeList) {
        this.periodTimeList = periodTimeList;
    }

    public double getSumQueueingTime() {
        return sumQueueingTime;
    }

    public void setSumQueueingTime(double sumQueueingTime) {
        this.sumQueueingTime = sumQueueingTime;
    }

    public double getSumPeriodTime() {
        return sumPeriodTime;
    }

    public void setSumPeriodTime(double sumPeriodTime) {
        this.sumPeriodTime = sumPeriodTime;
    }
}
