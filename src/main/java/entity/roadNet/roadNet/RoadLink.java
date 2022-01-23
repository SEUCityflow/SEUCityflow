package entity.roadNet.roadNet;

import java.util.ArrayList;
import java.util.List;

public class RoadLink {
    private Intersection intersection;
    private Road startRoad;
    private Road endRoad;
    private RoadLinkType type;
    private List<LaneLink> laneLinks;
    private int index;

    public RoadLink() {
        laneLinks = new ArrayList<>();
    }

    public List<LaneLink> getLaneLinks() {
        return laneLinks;
    }

    public Road getStartRoad()  {
        return startRoad;
    }

    public Road getEndRoad()  {
        return endRoad;
    }

    public boolean isAvailable()  {
        return intersection.getTrafficLight().getCurrentPhase().getRoadLinkAvailable().get(index);
    }

    public boolean isTurn()  {
        return type == RoadLinkType.turn_left || type == RoadLinkType.turn_right;
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public void setIntersection(Intersection intersection) {
        this.intersection = intersection;
    }

    public void setStartRoad(Road startRoad) {
        this.startRoad = startRoad;
    }

    public void setEndRoad(Road endRoad) {
        this.endRoad = endRoad;
    }

    public RoadLinkType getType() {
        return type;
    }

    public void setType(RoadLinkType type) {
        this.type = type;
    }

    public void setLaneLinks(List<LaneLink> laneLinks) {
        this.laneLinks = laneLinks;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void reset()  {
        for (LaneLink laneLink : laneLinks) {
            laneLink.reset();
        }
    }
}
