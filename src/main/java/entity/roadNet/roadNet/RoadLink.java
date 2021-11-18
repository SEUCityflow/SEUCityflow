package entity.roadNet.roadNet;

import java.util.List;

enum RoadLinkType {
    turn_right,
    turn_left,
    go_straight
}

public class RoadLink {
    private Intersection intersection;
    private Road startRoad;
    private Road endRoad;
    private RoadLinkType type;;
    private List<LaneLink> laneLinks;
    private int index;

    public List<LaneLink> getLaneLinks() {
        return null;
    }
    public Road getStartRoad()  {
        return null;
    }
    public Road getEndRoad()  {
        return null;
    }
    public boolean isAvailable()  {
        return false;
    }
    public boolean isTurn()  {
        return false;
    }
    public void reset()  {

    }
}
