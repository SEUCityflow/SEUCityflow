package entity.roadNet.roadNet;

import util.Point;

import java.util.List;
import java.util.Map;

public class RoadNet {
    private List<Road> roads;
    private List<Intersection> intersections;
    private Map<String, Road> roadMap;
    private Map<String, Intersection> interMap;
    private Map<String, Drivable> drivableMap;
    private List<Lane> lanes;;
    private List<LaneLink> laneLinks;
    private List<Drivable> drivables;
    private Point getPoint(Point p1, Point p2, double a) {
        return null;
    }
    public boolean loadFromJson(String jsonFileName)  {
        return false;
    }
    // public rapidjson::Value convertToJson(rapidjson::Document::AllocatorType allocator)
    public List<Road> getRoads()  {
        return null;
    }
    public List<Intersection> getIntersections()  {
        return null;
    }
    public Road getRoadById(String id)  {
        return null;
    }
    public Intersection getIntersectionById(String id)  {
        return null;
    }
    public Drivable getDrivableByid(String id)  {
        return null;
    }
    public List<Lane> getLanes()  {
        return null;
    }
    public List<LaneLink> getLaneLinks()  {
        return null;
    }
    public List<Drivable> getDrivables()  {
        return null;
    }
    public void reset() {

    }
}
