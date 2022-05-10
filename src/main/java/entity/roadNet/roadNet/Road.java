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
    private List<Point> points; // 起点为 startIntersection 中心，终点为 endIntersection 中心
    private List<Vehicle> planRouteBuffer;
    public static final double congestionIndex = 1.5;

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
        planRouteBuffer = new ArrayList<>();
    }

    public void buildSegmentationByInterval(double interval) {
        int numSegments = (int) Math.max(Math.ceil(Point.getLengthOfPoints(lanes.get(0).getPoints()) / interval), 1);
        for (Lane lane : lanes) {
            lane.buildSegmentation(numSegments);
        }
    }

    public RoadLink connectedToRoad(Road road) {
        for (Lane lane : lanes) {
            List<LaneLink> list = lane.getLaneLinksToRoad(road);
            if (list.size() != 0) {
                return list.get(0).getRoadLink();
            }
        }
        return null;
    }

    public void reset() {
        for (Lane lane : lanes) {
            lane.reset();
        }
    }

    public boolean isAnchorPoint(Road nowAnchorPoint) {
        return this == nowAnchorPoint;
    }

    public boolean isCongestion() {
        double qtiMax = 0;
        for (Lane lane : lanes) {
            qtiMax = Math.max(qtiMax, lane.getQueueingTimeIndex());
        }
        return qtiMax >= congestionIndex;
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

    public double getHistoryAverageSpeed() {
        int vehicleNum = 0;
        double speedSum = 0;
        for (Lane lane : lanes) {
            vehicleNum += lane.getHistoryVehicleNum();
            speedSum += lane.getHistoryAverageSpeed() * lane.getHistoryVehicleNum();
        }
        return vehicleNum != 0 ? speedSum / vehicleNum : -1;
    }

    public double getAverageDuration() {
        double averageSpeed = getHistoryAverageSpeed();
        return averageSpeed < 0 ? -1 : getAverageLength() / averageSpeed;
    }

    public void addPlanRouteVehicle(Vehicle vehicle) {
        planRouteBuffer.add(vehicle);
    }

    public void clearPlanRouteBuffer() {
        planRouteBuffer.clear();
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

    public List<Vehicle> getPlanRouteBuffer() {
        return planRouteBuffer;
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

    public void setPlanRouteBuffer(List<Vehicle> planRouteBuffer) {
        this.planRouteBuffer = planRouteBuffer;
    }

}
