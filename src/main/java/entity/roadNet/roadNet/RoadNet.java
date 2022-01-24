package entity.roadNet.roadNet;

import com.alibaba.fastjson.*;
import entity.roadNet.trafficLight.LightPhase;
import static util.JsonRelate.*;

import entity.roadNet.trafficLight.TrafficLight;

import static util.Point.*;

import java.util.*;

import entity.vehicle.vehicle.VehicleInfo;
import util.Point;
import entity.archive.RoadNetSerialization;


public class RoadNet {
    private List<Road> roads;
    private List<Intersection> intersections;
    private Map<String, Road> roadMap;
    private Map<String, Intersection> interMap;
    private Map<String, Drivable> drivableMap;
    private List<Lane> lanes;
    private List<LaneLink> laneLinks;
    private List<Drivable> drivables;
    private static final Map<String, RoadLinkType> typeMap = new HashMap<>();

    static {
        typeMap.put("turn_left", RoadLinkType.turn_left);
        typeMap.put("turn_right", RoadLinkType.turn_right);
        typeMap.put("go_straight", RoadLinkType.go_straight);
    }


    public RoadNet() {
        roads = new ArrayList<>();
        intersections = new ArrayList<>();
        roadMap = new HashMap<>();
        interMap = new HashMap<>();
        drivableMap = new HashMap<>();
        lanes = new ArrayList<>();
        laneLinks = new ArrayList<>();
        drivables = new ArrayList<>();
    }

    private void loadPoints(JSONArray pointValues, List<Point> points) {
        for (int i = 0; i < pointValues.size(); i++) {
            JSONObject curPoint = pointValues.getJSONObject(i);
            double x = getDoubleFromJsonObject(curPoint, "x");
            double y = getDoubleFromJsonObject(curPoint, "y");
            points.add(new Point(x, y));
        }
    }

    private void loadLaneLinks(JSONObject object, LaneLink laneLink) {
        JSONArray pointValues = getJsonMemberArray(object, "points");
        if (pointValues.size() != 0) {
            loadPoints(pointValues, laneLink.getPoints());
        } else {
            laneLink.setPoints(calLaneLinkPoints(laneLink.getStartLane(), laneLink.getEndLane()));
        }
        laneLink.setLength(getLengthOfPoints(laneLink.getPoints()));
        drivableMap.put(laneLink.getId(), laneLink);
    }

    private void loadRoadLinks(JSONObject object, RoadLink roadLink) {
        roadLink.setType(typeMap.get(getStringFromJsonObject(object, "type")));
        roadLink.setStartRoad(roadMap.get(getStringFromJsonObject(object, "startRoad")));
        roadLink.setEndRoad(roadMap.get(getStringFromJsonObject(object, "endRoad")));
        JSONArray laneLinkValues = getJsonMemberArray(object, "laneLinks");
        for (int i = 0; i < laneLinkValues.size(); i++) {
            LaneLink laneLink = new LaneLink();
            roadLink.getLaneLinks().add(laneLink);
            JSONObject curLaneLink = laneLinkValues.getJSONObject(i);
            Lane startLane = roadLink.getStartRoad().getLanes().get(getIntFromJsonObject(curLaneLink, "startLaneIndex"));
            Lane endLane = roadLink.getEndRoad().getLanes().get(getIntFromJsonObject(curLaneLink, "endLaneIndex"));
            laneLink.setStartLane(startLane);
            laneLink.setEndLane(endLane);
            laneLink.setRoadLink(roadLink);
            startLane.getLaneLinks().add(laneLink);
            loadLaneLinks(curLaneLink, laneLink);
        }
    }

    private void loadLightPhase(JSONObject object, LightPhase lightPhase) {
        lightPhase.setTime(getDoubleFromJsonObject(object, "time"));
        JSONArray availableRoadLinkValues = getJsonMemberArray(object, "availableRoadLinks");
        for (int i = 0; i < availableRoadLinkValues.size(); i++) {
            int indexInRoadLinks = getIntFromJsonArray(availableRoadLinkValues, i);
            lightPhase.getRoadLinkAvailable().set(indexInRoadLinks, true);
        }
    }

    private void loadTrafficLight(JSONObject object, TrafficLight trafficLight) {
        JSONArray lightPhaseValues = getJsonMemberArray(object, "lightphases");
        Intersection intersection = trafficLight.getIntersection();
        for (int i = 0; i < lightPhaseValues.size(); i++) {
            JSONObject curLightPhase = lightPhaseValues.getJSONObject(i);
            LightPhase lightPhase = new LightPhase();
            trafficLight.getPhases().add(lightPhase);
            for (int j = 0; j < trafficLight.getIntersection().getRoadLinks().size(); j++) {
                lightPhase.getRoadLinkAvailable().add(false);
                trafficLight.getCumulateTimes().add((double) 0);
            }
            loadLightPhase(curLightPhase, lightPhase);
            int pos = 0;
            for (boolean flag : lightPhase.getRoadLinkAvailable()) {
                trafficLight.setPeriod(trafficLight.getPeriod() + lightPhase.getTime());
                if (flag) {
                    switch (intersection.getRoadLinks().get(pos).getType()) {
                        case turn_right:
                            (trafficLight.getTime())[0] += lightPhase.getTime();
                            break;
                        case turn_left:
                            (trafficLight.getTime())[1] += lightPhase.getTime();
                            break;
                        case go_straight:
                            (trafficLight.getTime())[2] += lightPhase.getTime();
                            break;
                    }
                    pos++;
                }
            }
        }
        trafficLight.init(0);
    }

    private void loadIntersection(JSONObject object, Intersection intersection) {
        // point
        JSONObject curPoint = getJsonMemberObject(object, "point");
        double x = getDoubleFromJsonObject(curPoint, "x");
        double y = getDoubleFromJsonObject(curPoint, "y");
        intersection.setPoint(new Point(x, y));
        // roads
        JSONArray curRoadValues = getJsonMemberArray(object, "roads");
        for (int i = 0; i < curRoadValues.size(); i++) {
            String roadName = getStringFromJsonArray(curRoadValues, i);
            intersection.getRoads().add(roadMap.get(roadName));
        }
        // isVirtual
        intersection.setVirtual(getBooleanFromJsonObject(object, "virtual"));
        if (intersection.isVirtual()) {
            return;
        }
        // read width
        intersection.setWidth(getDoubleFromJsonObject(object, "width"));
        // roadLinks
        JSONArray roadLinkValues = getJsonMemberArray(object, "roadLinks");
        for (int i = 0; i < roadLinkValues.size(); i++) {
            JSONObject curRoadLink = roadLinkValues.getJSONObject(i);
            RoadLink roadLink = new RoadLink();
            intersection.getRoadLinks().add(roadLink);
            roadLink.setIndex(i);
            roadLink.setIntersection(intersection);
            loadRoadLinks(curRoadLink, roadLink);
            intersection.getLaneLinks().addAll(roadLink.getLaneLinks());
        }
        // trafficLight
        TrafficLight trafficLight = new TrafficLight();
        intersection.setTrafficLight(trafficLight);
        trafficLight.setIntersection(intersection);
        JSONObject trafficLightValue = getJsonMemberObject(object, "trafficLight");
        loadTrafficLight(trafficLightValue, trafficLight);
        // traffic period time
        for (int j = 0; j < trafficLight.getPhases().size(); j++) {
            int last = (j - 1 + trafficLight.getPhases().size()) % trafficLight.getPhases().size();
            trafficLight.updateCumulateTime(j, last, trafficLight.getPhases().get(last).getTime());
        }
    }

    private void loadRoads(JSONObject object, Road road) {
        road.setStartIntersection(interMap.get(getStringFromJsonObject(object, "startIntersection")));
        road.setEndIntersection(interMap.get(getStringFromJsonObject(object, "endIntersection")));
        JSONArray laneValues = getJsonMemberArray(object, "lanes");
        // lanes
        for (int i = 0; i < laneValues.size(); i++) {
            JSONObject curLaneValue = laneValues.getJSONObject(i);
            double width = getDoubleFromJsonObject(curLaneValue, "width");
            double maxSpeed = getDoubleFromJsonObject(curLaneValue, "maxSpeed");
            road.getLanes().add(new Lane(width, maxSpeed, i, road));
        }
        for (Lane lane : road.getLanes()) {
            drivableMap.put(lane.getId(), lane);
        }
        // points
        JSONArray pointValues = getJsonMemberArray(object, "points");
        loadPoints(pointValues, road.getPoints());
    }

    public void buildMapping(JSONArray intersectionValues, JSONArray roadValues) {
        for (int i = 0; i < intersectionValues.size(); i++) {
            JSONObject curInterValue = intersectionValues.getJSONObject(i);
            Intersection intersection = new Intersection();
            intersections.add(intersection);
            String id = getStringFromJsonObject(curInterValue, "id");
            interMap.put(id, intersection);
            intersection.setId(id);
        }
        for (int i = 0; i < roadValues.size(); i++) {
            JSONObject curRoadValue = roadValues.getJSONObject(i);
            Road road = new Road();
            roads.add(road);
            String id = getStringFromJsonObject(curRoadValue, "id");
            roadMap.put(id, road);
            road.setId(id);
        }
    }

    // TODO: 配置 fastJSON 序列化格式
    public void loadFromJson(String jsonFileName) {
        String json = readJsonData(jsonFileName);
        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONArray intersectionValues = getJsonMemberArray(jsonObject, "intersections");
        JSONArray roadValues = getJsonMemberArray(jsonObject, "roads");
        buildMapping(intersectionValues, roadValues);
        //roads
        for (int i = 0; i < roadValues.size(); i++) {
            JSONObject curRoadValue = roadValues.getJSONObject(i);
            Road road = roads.get(i);
            loadRoads(curRoadValue, road);
        }
        // intersections
        for (int i = 0; i < intersectionValues.size(); i++) {
            JSONObject curInterValue = intersectionValues.getJSONObject(i);
            Intersection intersection = intersections.get(i);
            loadIntersection(curInterValue, intersection);
        }

        for (Intersection intersection : intersections) {
            intersection.initCrosses();
            laneLinks.addAll(intersection.getLaneLinks());
            drivables.addAll(intersection.getLaneLinks());
        }

        VehicleInfo vehicleInfo = new VehicleInfo();

        for (Road road : roads) {
            lanes.addAll(road.getLanes());
            drivables.addAll(road.getLanes());
            road.initLanePoints();
            road.buildSegmentationByInterval((vehicleInfo.len + vehicleInfo.minGap) * 10);
        }
    }

    public String convertToJson() {
        try {
            RoadNetSerialization roadNetSerialization = new RoadNetSerialization(this);
            return JSON.toJSONString(roadNetSerialization);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

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

    public void reset() {
        for (Road road : roads) {
            road.reset();
        }
        for (Intersection intersection : intersections) {
            intersection.reset();
        }
    }

    public List<Drivable> getDrivables() {
        return drivables;
    }

    public void setDrivables(List<Drivable> drivables) {
        this.drivables = drivables;
    }

    // set / get
    public void setRoads(List<Road> roads) {
        this.roads = roads;
    }

    public void setIntersections(List<Intersection> intersections) {
        this.intersections = intersections;
    }

    public Map<String, Road> getRoadMap() {
        return roadMap;
    }

    public void setRoadMap(Map<String, Road> roadMap) {
        this.roadMap = roadMap;
    }

    public Map<String, Intersection> getInterMap() {
        return interMap;
    }

    public void setInterMap(Map<String, Intersection> interMap) {
        this.interMap = interMap;
    }

    public Map<String, Drivable> getDrivableMap() {
        return drivableMap;
    }

    public void setDrivableMap(Map<String, Drivable> drivableMap) {
        this.drivableMap = drivableMap;
    }

    public void setLanes(List<Lane> lanes) {
        this.lanes = lanes;
    }

    public void setLaneLinks(List<LaneLink> laneLinks) {
        this.laneLinks = laneLinks;
    }
}
