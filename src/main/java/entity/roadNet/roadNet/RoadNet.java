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

    public boolean loadFromJson(String jsonFileName)  {
        return false;
    }

    // public rapidjson::Value convertToJson(rapidjson::Document::AllocatorType allocator)

    public List<Road> getRoads()  {
        return roads;
    }

    public List<Intersection> getIntersections()  {
        return intersections;
    }

    public Road getRoadById(String id)  {
        return roadMap.getOrDefault(id, null);
    }

    public Intersection getIntersectionById(String id)  {
        return interMap.getOrDefault(id, null);
    }

    public Drivable getDrivableById(String id)  {
        return drivableMap.getOrDefault(id, null);
    }

    public List<Lane> getLanes()  {
        return lanes;
    }

    public List<LaneLink> getLaneLinks()  {
        return laneLinks;
    }

    public List<Drivable> getDrivables()  {
        return drivables;
    }

    public void reset() {
        for (Road road : roads) {
            road.reset();
        }
        for (Intersection intersection : intersections) {
            intersection.reset();
        }
    }
}
