package entity.roadNet.roadNet;

import java.util.ArrayList;
import java.util.List;

public class LaneLink extends Drivable{
    private RoadLink roadLink;
    private Lane startLane;
    private Lane endLane;
    private List<Cross> crosses;

    public LaneLink() {
        super();
        crosses = new ArrayList<>();
        drivableType = DrivableType.LANELINK;
        maxSpeed = 1000; //TODO
    }

    @Override
    public String getId() {
        return (startLane != null ? startLane.getId() : "") + "_TO_" + (endLane != null ? endLane.getId() : "");
    }

    public RoadLink getRoadLink() {
        return roadLink;
    }

    public RoadLinkType getRoadLinkType()  {
        return roadLink.getType();
    }

    public List<Cross> getCrosses()  {
        return crosses;
    }

    public Lane getStartLane()  {
        return startLane;
    }

    public Lane getEndLane()  {
        return endLane;
    }

    public boolean isAvailable()  {
        return roadLink.isAvailable();
    }

    public boolean isTurn()  {
        return roadLink.isTurn();
    }

    public void reset() {
        super.reset();
    }

    public void setRoadLink(RoadLink roadLink) {
        this.roadLink = roadLink;
    }

    public void setStartLane(Lane startLane) {
        this.startLane = startLane;
    }

    public void setEndLane(Lane endLane) {
        this.endLane = endLane;
    }

    public void setCrosses(List<Cross> crosses) {
        this.crosses = crosses;
    }
}
