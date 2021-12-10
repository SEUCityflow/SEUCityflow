package entity.vehicle.vehicle;

import entity.flow.Route;
import entity.roadNet.roadNet.Drivable;
import entity.vehicle.router.Router;

import java.util.Random;

public class ControllerInfo {
    private double dis;
    private Drivable drivable;
    private Drivable prevDrivable;
    private double approachingIntersectionDistance;
    private double gap;
    private int enterLaneLinkTime;
    private Vehicle leader;
    private Vehicle blocker;
    private boolean end;
    private boolean running;
    private Router router;

    public ControllerInfo(Vehicle vehicle) {
        router = new Router(vehicle);
    }

    public ControllerInfo(ControllerInfo other) {
        this.dis = other.dis;
        this.drivable = other.drivable;
        this.prevDrivable = other.prevDrivable;
        this.approachingIntersectionDistance = other.approachingIntersectionDistance;
        this.gap = other.gap;
        this.enterLaneLinkTime = other.enterLaneLinkTime;
        this.leader = other.leader;
        this.blocker = other.blocker;
        this.end = other.end;
        this.running = other.running;
        this.router = new Router(other.router);
    }

    public ControllerInfo(Vehicle vehicle, Route route, Random rnd) {
        router = new Router(vehicle, route, rnd);
        enterLaneLinkTime = Integer.MAX_VALUE;
    }

    // redo
    ControllerInfo(Vehicle vehicle, ControllerInfo other) {
        this.dis = other.dis;
        this.drivable = other.drivable;
        this.prevDrivable = other.prevDrivable;
        this.approachingIntersectionDistance = other.approachingIntersectionDistance;
        this.gap = other.gap;
        this.enterLaneLinkTime = other.enterLaneLinkTime;
        this.leader = other.leader;
        this.blocker = other.blocker;
        this.end = other.end;
        this.running = other.running;
        this.router = new Router(other.router);
        router.setVehicle(vehicle);
    }

    public double getDis() {
        return dis;
    }

    public void setDis(double dis) {
        this.dis = dis;
    }

    public Drivable getDrivable() {
        return drivable;
    }

    public void setDrivable(Drivable drivable) {
        this.drivable = drivable;
    }

    public Drivable getPrevDrivable() {
        return prevDrivable;
    }

    public void setPrevDrivable(Drivable prevDrivable) {
        this.prevDrivable = prevDrivable;
    }

    public double getApproachingIntersectionDistance() {
        return approachingIntersectionDistance;
    }

    public void setApproachingIntersectionDistance(double approachingIntersectionDistance) {
        this.approachingIntersectionDistance = approachingIntersectionDistance;
    }

    public double getGap() {
        return gap;
    }

    public void setGap(double gap) {
        this.gap = gap;
    }

    public int getEnterLaneLinkTime() {
        return enterLaneLinkTime;
    }

    public void setEnterLaneLinkTime(int enterLaneLinkTime) {
        this.enterLaneLinkTime = enterLaneLinkTime;
    }

    public Vehicle getLeader() {
        return leader;
    }

    public void setLeader(Vehicle leader) {
        this.leader = leader;
    }

    public Vehicle getBlocker() {
        return blocker;
    }

    public void setBlocker(Vehicle blocker) {
        this.blocker = blocker;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Router getRouter() {
        return router;
    }

    public void setRouter(Router router) {
        this.router = router;
    }
}
