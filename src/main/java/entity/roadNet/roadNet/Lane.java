package entity.roadNet.roadNet;

import entity.vehicle.vehicle.Vehicle;

import java.util.List;

class HistoryRecord {
    public int vehicleNum;
    public double averageSpeed;
    public HistoryRecord(int vehicleNum, double averageSpeed) {

    }
}

public class Lane extends  Drivable{
    private int laneIndex;
    private List<Segment> segments;
    private List<LaneLink> LaneLinks;
    private Road belongRoad;
    private List<Vehicle> waitingBuffer;
    private List<HistoryRecord> history;
    private int historyVehicleNum;
    private int historyAverageSpeed;
    private static final int historyLen = 240;
    public Lane() {

    }
    public Lane(double width, double maxSpeed, int laneIndex, Road belongRoad) {

    }
    public String getId() {
        return null;
    }
    public Road getBeLongRoad()  {
        return null;
    }
    public boolean available(Vehicle vehicle)  {
        return false;
    }
    public boolean canEnter(Vehicle vehicle)  {
        return false;
    }
    public int getLaneIndex()  {
        return 0;
    }
    public Lane getInnerLane()  {
        return null;
    }
    public Lane getOuterLane() {
        return null;
    }
    public List<LaneLink> getLaneLinks() {
        return null;
    }
    public Intersection getStartIntersection() {
        return null;
    }
    public Intersection getEndIntersection() {
        return null;
    }
    public List<LaneLink> getLaneLinksToRoad(Road road) {
        return null;
    }
    public void reset() {

    }
    public List<Vehicle> getWaitingBuffer()  {
        return null;
    }
    public void pushWaitingVehicle(Vehicle vehicle)  {

    }
    public void buildSegmentation(int numSegments)  {

    }
    public void initSegments()  {

    }
    public Segment getSegment(int index)  {
        return null;
    }
    public List<Segment> getSegments()  {
        return null;
    }
    public int getSegmentNum()  {
        return 0;
    }
    public List<Vehicle> getVehiclesBeforeDistance(double dis, int segmentIndex, double deltaDis)  {
        return null;
    }
    public void updateHistory()  {

    }
    public int getHistoryVehicleNum()  {
        return 0;
    }
    public double getHistoryAverageSpeed()  {
        return 0;
    }
    public Vehicle getVehicleBeforeDistance(double dis, int segmentIndex)  {
        return null;
    }
    public Vehicle getVehicleAfterDistance(double dis, int segmentIndex)  {
        return null;
    }
}
