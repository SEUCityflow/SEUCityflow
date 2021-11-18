package entity.roadNet.roadNet;

import entity.roadNet.trafficLight.TrafficLight;
import util.Point;
import util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Intersection {
    private String id;
    private boolean isVirtual;
    private double width;
    private Point point;
    private TrafficLight trafficLight;
    private List<Road> roads;
    private List<RoadLink> roadLinks;
    private List<Cross> crosses;
    private List<LaneLink> laneLinks;

    private Cross calCross(LaneLink laneLink1, LaneLink laneLink2) { // 计算两 laneLink 的 cross
        List<Point> p1 = laneLink1.getPoints();
        List<Point> p2 = laneLink2.getPoints();
        double disa = 0;
        for (int i = 0; i + 1 < p1.size(); i++) {
            double disb = 0;
            for (int j = 0; j + 1 < p2.size(); j++) {
                Point A1 = p1.get(i);
                Point A2 = p1.get(i + 1);
                Point B1 = p2.get(j);
                Point B2 = p2.get(j + 1);
                if (Util.sign(Point.crossMultiply(A2.minus(A1), B2.minus(B1))) == 0) { // 平行
                    continue;
                }
                Point p = Point.calcIntersectionPoint(A1, A2, B1, B2); // 向量交点
                if (Point.onSegment(A1, A2, p) && Point.onSegment(B1, B2, p)) { // 是线段交点
                    Cross cross = new Cross();
                    cross.setLaneLinks(laneLink1, laneLink2);
                    cross.setDistanceOnLane(disa + p.minus(A1).len(), disb + p.minus(B1).len());
                    cross.setAng(Point.calcAng(A2.minus(A1), B2.minus(B1)));
                    double w1 = laneLink1.getWidth();
                    double w2 = laneLink2.getWidth();
                    double c1 = w1 / Math.sin(cross.getAng());
                    double c2 = w2 / Math.sin(cross.getAng());
                    double diag = (c1 * c1 + c2 * c2 + 2 * c1 * c2 * Math.cos(cross.getAng())) / 4; //?
                    cross.setSafeDistances(Math.sqrt(diag - w2 * w2 / 4), Math.sqrt(diag - w1 * w1 / 4));
                    return cross;
                }
            }
        }
        return null;
    }

    private void initCrosses() { // 计算 intersection 内 cross
        List<LaneLink> allLaneLink = new ArrayList<LaneLink>();
        for (RoadLink roadLink : roadLinks) {
            allLaneLink.addAll(roadLink.getLaneLinks());
        }
        int n = allLaneLink.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Cross p = calCross(allLaneLink.get(i), allLaneLink.get(j));
                crosses.add(p);
            }
        }
        for (Cross cross : crosses) {
            cross.getLaneLink(0).getCrosses().add(cross);
            cross.getLaneLink(1).getCrosses().add(cross);
        }
        for (LaneLink laneLink : allLaneLink) {
            List<Cross> crosses = laneLink.getCrosses();
            crosses.sort(Comparator.comparing(Cross::getDistanceOnLane0));
        }
    }

    public List<Point> getOutLine() { // 计算 intersection 凸包
        List<Point> ret = new ArrayList<Point>();
        ret.add(point);
        for (Road road : roads) {
            Point roadDirect = road.getEndIntersection().getPoint().minus(road.getStartIntersection().getPoint()).unit();
            Point pDirect = roadDirect.normal();
            if (road.getStartIntersection() == this) {
                roadDirect = roadDirect.opposite();
            }
            double roadWidth = road.getWidth();
            double deltaWidth = Math.max(5, 0.5 * Math.min(width, roadWidth));
            Point pointA = point.minus(roadDirect.multiply(width));
            Point pointB = point.minus(pDirect.multiply(roadWidth));
            ret.add(pointA);
            ret.add(pointB);
            if (deltaWidth < road.getAverageLength()) {
                Point pointA1 = pointA.minus(roadDirect.multiply(deltaWidth));
                Point pointB1 = pointB.minus(roadDirect.multiply(deltaWidth));
                ret.add(pointA1);
                ret.add(pointB1);
            }
        }
        return Point.calConvexHull(ret);
    }

    public String getId() {
        return id;
    }

    public TrafficLight getTrafficLight() {
        return trafficLight;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public List<RoadLink> getRoadLinks() {
        return roadLinks;
    }

    public List<Cross> getCross() {
        return crosses;
    }

    public boolean isVirtualIntersection() {
        return isVirtual;
    }

    public List<LaneLink> getLaneLinks() {
        return laneLinks;
    }

    public void reset() {
        trafficLight.reset();
        for (RoadLink roadLink : roadLinks) {
            roadLink.reset();
        }
        for (Cross cross : crosses) {
            cross.reset();
        }
    }

    public boolean isImplicitIntersection()  {
        return trafficLight.getPhases().size() <= 1;
    }

    public Point getPosition()  {
        return point;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isVirtual() {
        return isVirtual;
    }

    public void setVirtual(boolean virtual) {
        isVirtual = virtual;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public void setTrafficLight(TrafficLight trafficLight) {
        this.trafficLight = trafficLight;
    }

    public void setRoads(List<Road> roads) {
        this.roads = roads;
    }

    public void setRoadLinks(List<RoadLink> roadLinks) {
        this.roadLinks = roadLinks;
    }

    public List<Cross> getCrosses() {
        return crosses;
    }

    public void setCrosses(List<Cross> crosses) {
        this.crosses = crosses;
    }

    public void setLaneLinks(List<LaneLink> laneLinks) {
        this.laneLinks = laneLinks;
    }
}
