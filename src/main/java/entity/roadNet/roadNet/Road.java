package entity.roadNet.roadNet;

import entity.vehicle.vehicle.Vehicle;
import util.Point;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Road {
    private String id;
    private Intersection startIntersection;
    private Intersection endIntersection;
    private List<Lane> lanes;
    private List<Point> points;
    private List<Vehicle> planeRouteBuffer;

    public void initLanePoints() {
        double dsum = 0;
        List<Point> roadPoints = new ArrayList<>(points);
        if (! startIntersection.isVirtual() && startIntersection.isNotImplicitIntersection()) {
            double width = startIntersection.getWidth();
            Point p1 = roadPoints.get(0);
            Point p2 = roadPoints.get(1);
            roadPoints.set(0, p1.plus(p2.minus(p1).unit().multiply(width)));
        }
        if (! endIntersection.isVirtual() && endIntersection.isNotImplicitIntersection()) {
            double width = endIntersection.getWidth();
            Point p1 = roadPoints.get(roadPoints.size() - 2);
            Point p2 = roadPoints.get(roadPoints.size() - 1);
            roadPoints.set(roadPoints.size() - 1, p2.minus(p2.minus(p1).unit().multiply(width)));
        }
        for (Lane lane : lanes) {
            double dmin = dsum;
            double dmax = dsum + lane.getWidth();
            List<Point> lanePoints = new ArrayList<>();
            for (int i = 0; i < roadPoints.size(); i++ ) {
                Point u;
                if (i == 0) {
                    u = roadPoints.get(1).minus(roadPoints.get(0)).unit();
                } else if (i + 1 == roadPoints.size()) {
                    u = roadPoints.get(i).minus(roadPoints.get(i - 1)).unit();
                } else {
                    Point u1 = roadPoints.get(i + 1).minus(roadPoints.get(i)).unit();
                    Point u2 = roadPoints.get(i).minus(roadPoints.get(i - 1)).unit();
                    u = u1.plus(u2).unit();
                }
                Point v = u.normal().opposite();
                Point point = roadPoints.get(i).plus(v.multiply((dmin + dmax) / 2));
                lanePoints.add(point);
            }
            lane.setPoints(lanePoints);
            lane.setLength(Point.getLengthOfPoints(lanePoints));
            dsum += lane.getWidth();
        }
    }

    public Road() {
        lanes = new ArrayList<>();
        points = new ArrayList<>();
        planeRouteBuffer = new LinkedList<>();
    }

    public void buildSegmentationByInterval(double interval) { // TODO: segment 段数存在问题，当前为修改方案
        for (Lane lane : lanes) {
            lane.buildSegmentation((int) Math.max(Math.ceil(Point.getLengthOfPoints(lane.getPoints()) / interval), 1));
        }
    }

    public boolean connectedToRoad(Road road) {
        for (Lane lane : lanes) {
            if (lane.getLaneLinksToRoad(road).size() != 0) {
                return true;
            }
        }
        return false;
    }

    public void reset() {
        for (Lane lane : lanes) {
            lane.reset();
        }
    }

    public double getWidth() {
        double width = 0;
        for (Lane lane : lanes) {
            width += lane.getWidth();
        }
        return width;
    }

    public double getAverageLength() {
        double sum = 0;
        for (Lane lane : lanes) {
            sum += lane.getLength();
        }
        return lanes.size() == 0 ? 0 : sum / lanes.size();
    }

    public double getAverageSpeed() {
        int vehicleNum = 0;
        double speedSum = 0;
        for (Lane lane : lanes) {
            vehicleNum += lane.getHistoryVehicleNum();
            speedSum += lane.getHistoryAverageSpeed() * lane.getHistoryVehicleNum();
        }
        return vehicleNum != 0 ? speedSum / vehicleNum : -1;
    }

    public double getAverageDuration() {
        double averageSpeed = getAverageSpeed();
        return averageSpeed < 0 ? -1 : getAverageLength() / averageSpeed;
    }

    public void addPlanRouteVehicle(Vehicle vehicle) {
        planeRouteBuffer.add(vehicle);
    }

    public void clearPlanRouteBuffer() {
        planeRouteBuffer.clear();
    }

    public String getId() {
        return id;
    }

    public Intersection getStartIntersection() {
        return startIntersection;
    }

    public Intersection getEndIntersection() {
        return endIntersection;
    }

    public List<Lane> getLanes() {
        return lanes;
    }

    public List<Point> getPoints() {
        return points;
    }

    public List<Vehicle> getPlaneRouteBuffer() {
        return planeRouteBuffer;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStartIntersection(Intersection startIntersection) {
        this.startIntersection = startIntersection;
    }

    public void setEndIntersection(Intersection endIntersection) {
        this.endIntersection = endIntersection;
    }

    public void setLanes(List<Lane> lanes) {
        this.lanes = lanes;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public void setPlaneRouteBuffer(List<Vehicle> planeRouteBuffer) {
        this.planeRouteBuffer = planeRouteBuffer;
    }
}
