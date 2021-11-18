package entity.vehicle.Router;

import entity.flow.Route;
import entity.roadNet.roadNet.Drivable;
import entity.roadNet.roadNet.Lane;
import entity.roadNet.roadNet.LaneLink;
import entity.roadNet.roadNet.Road;
import entity.vehicle.vehicle.Vehicle;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

enum RouterType {
    LENGTH,
    DURATION,
    DYNAMIC
}


public class Router {
    private Vehicle vehicle;
    private List<Road> route;
    private List<Road> anchorPoints;
    private Iterator<List<Road>> iCurRoad;
    private Random rnd;
    private List<Drivable> planned;
    private RouterType type;

    private int selectLaneIndex(Lane curLane, List<Lane> lanes) {
        return 0;
    }

    private LaneLink selectLaneLink(Lane curLane, List<LaneLink> laneLinks) {
        return null;
    }

    private Lane selectLane(Lane curLane, List<Lane> lanes) {
        return null;
    }

    private boolean dijkstra(Road start, Road end, List<Road> buffer) {
        return false;
    }

    public Router(Router router) {

    }

    public Router(Vehicle vehicle, Route route, Random rnd) {

    }

    public Road getFirstRoad() {
        return null;
    }
    public Drivable getFirstDrivable() {
        return null;
    }
    public Drivable getNextDrivable(int i) {
        return null;
    }
    public Drivable getNextDrivable(Drivable curDrivable) {
        return null;
    }
    public void update() {

    }
    public boolean isLastRoad(Drivable drivable) {
        return false;
    }
    public boolean onLastRoad() {
        return false;
    }
    public boolean onValidRoad() {
        return false;
    }
    public Lane getValidLane(Lane curLane) {
        return null;
    }

    public boolean updateShortestPath() {
        return false;
    }

    public boolean changeRoute(List<Road> anchor) {
        return false;
    }

    // set / get
    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public List<Road> getRoute() {
        return route;
    }

    public void setRoute(List<Road> route) {
        this.route = route;
    }

    public List<Road> getAnchorPoints() {
        return anchorPoints;
    }

    public void setAnchorPoints(List<Road> anchorPoints) {
        this.anchorPoints = anchorPoints;
    }

    public Iterator<List<Road>> getiCurRoad() {
        return iCurRoad;
    }

    public void setiCurRoad(Iterator<List<Road>> iCurRoad) {
        this.iCurRoad = iCurRoad;
    }

    public Random getRnd() {
        return rnd;
    }

    public void setRnd(Random rnd) {
        this.rnd = rnd;
    }

    public List<Drivable> getPlanned() {
        return planned;
    }

    public void setPlanned(List<Drivable> planned) {
        this.planned = planned;
    }

    public RouterType getType() {
        return type;
    }

    public void setType(RouterType type) {
        this.type = type;
    }
}
