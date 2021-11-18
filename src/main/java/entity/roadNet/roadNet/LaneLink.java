package entity.roadNet.roadNet;

import java.util.List;

public class LaneLink extends Drivable{
    private RoadLink roadLink;
    private Lane startLane;
    private Lane endLane;
    private List<Cross> crosses;
    public LaneLink() {

    }
    public Road getRoadLink() {
        return null;
    }
    public RoadLinkType getRoadLinkType()  {
        return null;
    }
    public List<Cross> getCrosses()  {
        return null;
    }
    public Lane getStartLane()  {
        return null;
    }
    public Lane getEndLane()  {
        return null;
    }
    public boolean isAvailable()  {
        return false;
    }
    public boolean isTurn()  {
        return false;
    }
    public void reset() {

    }

    @Override
    public String getId() {
        return null;
    }
}
