package entity.roadNet.roadNet;

import entity.vehicle.vehicle.Vehicle;

public class Cross {
    private final LaneLink[] laneLinks;
    private final Vehicle[] notifyVehicles;
    private final double[] notifyDistances;
    private final double[] distanceOnLane;
    private double leaveDistance;
    //    private double arriveDistance;
    private double ang;
    private final double[] safeDistances;

    public Cross() {
        laneLinks = new LaneLink[2];
        notifyVehicles = new Vehicle[2];
        notifyDistances = new double[2];
        safeDistances = new double[2];
        distanceOnLane = new double[2];
    }

    public void notify(LaneLink laneLink, Vehicle vehicle, double notifyDistance) {
        int i = (laneLink == laneLinks[0]) ? 0 : 1;
        notifyVehicles[i] = vehicle;
        notifyDistances[i] = notifyDistance;
    }

    // TODO: 考虑体积碰撞
    public boolean canPass(Vehicle vehicle, LaneLink laneLink, double distanceToLaneLinkStart) {
        int i = (laneLink == laneLinks[0]) ? 0 : 1;
        Vehicle foeVehicle = notifyVehicles[1 - i];
        RoadLinkType t1 = laneLinks[i].getRoadLinkType();
        RoadLinkType t2 = laneLinks[1 - i].getRoadLinkType();
        double d1 = distanceOnLane[i] - distanceToLaneLinkStart;
        double d2 = notifyDistances[1 - i];

        if (foeVehicle == null) return true;

        if (!vehicle.canYield(d1)) return true;

        int yield = 0;
        if (!foeVehicle.canYield(d2)) yield = 1;
        if (yield == 0) {
            if (t1.ordinal() > t2.ordinal()) {
                yield = -1;
            } else if (t1.ordinal() < t2.ordinal()) {
                if (d2 > 0) {
                    // todo: can be improved, check if higher priority vehicle is blocked by other vehicles, hard!
                    int foeVehicleReachSteps = foeVehicle.getReachStepsOnLaneLink(d2, laneLinks[1 - i]);
                    int reachSteps = vehicle.getReachStepsOnLaneLink(d1, laneLinks[i]);
                    if (foeVehicleReachSteps > reachSteps) {
                        yield = -1;
                    }
                } else {
                    if (d2 + foeVehicle.getLen() < 0) {
                        yield = -1;
                    }
                }
                if (yield == 0) yield = 1;
            } else {
                if (d2 > 0) {
                    int foeVehicleReachSteps = foeVehicle.getReachStepsOnLaneLink(d2, laneLinks[1 - i]);
                    int reachSteps = vehicle.getReachStepsOnLaneLink(d1, laneLinks[i]);
                    if (foeVehicleReachSteps > reachSteps) {
                        yield = -1;
                    } else if (foeVehicleReachSteps < reachSteps) {
                        yield = 1;
                    } else {
                        if (vehicle.getEnterLaneLinkTime() == foeVehicle.getEnterLaneLinkTime()) {
                            if (d1 == d2) {
                                yield = vehicle.getPriority() > foeVehicle.getPriority() ? -1 : 1;
                            } else {
                                yield = d1 < d2 ? -1 : 1;
                            }
                        } else {
                            yield = vehicle.getEnterLaneLinkTime() < foeVehicle.getEnterLaneLinkTime() ? -1 : 1;
                        }
                    }
                } else {
                    yield = d2 + foeVehicle.getLen() < 0 ? -1 : 1;
                }
            }
        }
        if (yield == 1) {
            Vehicle fastPointer = foeVehicle;
            Vehicle slowPointer = foeVehicle;
            while (fastPointer != null && fastPointer.getCurBlocker() != null) {
                slowPointer = slowPointer.getCurBlocker();
                fastPointer = fastPointer.getCurBlocker().getCurBlocker();
                if (slowPointer == fastPointer) {
                    // deadlock detected
                    yield = -1;
                    break;
                }
            }
        }
        return yield == -1;
    }

    public double getYieldDistance(Vehicle vehicle, Vehicle foeVehicle) {
        return Math.min(vehicle.getYieldDistance(), (vehicle.getWidth() + foeVehicle.getWidth() * Math.cos(ang)) / 2 / Math.sin(ang));
    }

    public double getYieldDistance(Vehicle vehicle, LaneLink laneLink) {
        int i = (laneLink == laneLinks[0]) ? 0 : 1;
        Vehicle foeVehicle = notifyVehicles[1 - i];
        return Math.min(vehicle.getYieldDistance(), (vehicle.getWidth() + foeVehicle.getWidth() * Math.cos(ang)) / 2 / Math.sin(ang));
    }

    public void clearNotify() {
        notifyVehicles[0] = notifyVehicles[1] = null;
    }

    public Vehicle getFoeVehicle(LaneLink laneLink) {
        return laneLink == laneLinks[0] ? notifyVehicles[1] : notifyVehicles[0];
    }

    public double getDistanceByLane(LaneLink laneLink) {
        return laneLink == laneLinks[0] ? distanceOnLane[0] : distanceOnLane[1];
    }

    public double getNotifyDistanceByLane(LaneLink laneLink) {
        return laneLink == laneLinks[0] ? notifyDistances[0] : notifyDistances[1];
    }

    public double getSafeDistanceByLane(LaneLink laneLink) {
        return laneLink == laneLinks[0] ? safeDistances[0] : safeDistances[1];
    }

    public double getAng() {
        return ang;
    }

    public LaneLink getLaneLink(int i) {
        return laneLinks[i];
    }

    public void reset() {}

    public void setLaneLinks(LaneLink l1, LaneLink l2) {
        laneLinks[0] = l1;
        laneLinks[1] = l2;
    }

    public void setNotifyVehicles(Vehicle v1, Vehicle v2) {
        notifyVehicles[0] = v1;
        notifyVehicles[1] = v2;
    }

    public void setNotifyDistances(double d1, double d2) {
        notifyDistances[0] = d1;
        notifyDistances[1] = d2;
    }

    public void setDistanceOnLane(double d1, double d2) {
        distanceOnLane[0] = d1;
        distanceOnLane[1] = d2;
    }

    public void setLeaveDistance(double leaveDistance) {
        this.leaveDistance = leaveDistance;
    }

    public void setAng(double ang) {
        this.ang = ang;
    }

    public void setSafeDistances(double d1, double d2) {
        safeDistances[0] = d1;
        safeDistances[1] = d2;
    }

    public double getLeaveDistance() {
        return leaveDistance;
    }
}
