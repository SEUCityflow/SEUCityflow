package entity.roadNet.roadNet;

import entity.roadNet.trafficLight.TrafficLight;
import util.Point;

import java.util.List;

public class Intersection {
    private String id;
    private boolean isVirtual;
    private double width;
    private Point point;
    private TrafficLight trafficLight;
    private List<Road> roads;
    private List<RoadLink> roadLinks;
    private List<Cross> crosses;
    private List<LaneLink> laneLinks;
    private void initCrosses() {

    }

    public String getId() {
        return null;
    }
    public TrafficLight getTrafficLight() {
        return null;
    }
    public List<Road> getRoads() {
        return null;
    }
    public List<RoadLink> getRoadLinks() {
        return null;
    }
    public List<Cross> getCross() {
        return null;
    }
    public boolean isVirtualIntersection() {
        return false;
    }
    public List<LaneLink> getLaneLinks() {
        return null;
    }
    public void reset() {

    }
    public List<Point> getOutLine() {
        return null;
    }
    public boolean isImplicitIntersection()  {
        return false;
    }
    public Point getPosition()  {
        return null;
    }
}
