package entity.archive;
import com.alibaba.fastjson.annotation.JSONField;
import entity.roadNet.roadNet.Intersection;
import entity.roadNet.roadNet.Lane;
import entity.roadNet.roadNet.Road;
import entity.roadNet.roadNet.RoadNet;
import util.Point;

import java.util.ArrayList;
import java.util.List;

class node {
    @JSONField(ordinal = 1)
    private String id;
    @JSONField(ordinal = 2)
    private List<Double> point;
    @JSONField(ordinal = 3)
    private boolean virtual;
    @JSONField(ordinal = 4)
    private double width;
    @JSONField(ordinal = 5)
    private List<Double> outline;

    public node() {}

    public node(Intersection intersection) {
        id = intersection.getId();
        point = new ArrayList<>();
        point.add(intersection.getPoint().x);
        point.add(intersection.getPoint().y);
        virtual = intersection.isVirtual();
        if (!virtual) {
            width = intersection.getWidth();
        }
        outline = new ArrayList<>();
        List<Point> p = intersection.getOutLine();
        for (Point value : p) {
            outline.add(value.x);
            outline.add(value.y);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public List<Double> getPoint() {
        return point;
    }

    public void setPoint(List<Double> point) {
        this.point = point;
    }

    public List<Double> getOutline() {
        return outline;
    }

    public void setOutline(List<Double> outline) {
        this.outline = outline;
    }
}

class edge {
    @JSONField(ordinal = 1)
    private String id;
    @JSONField(ordinal = 2)
    private String from;
    @JSONField(ordinal = 3)
    private String to;
    @JSONField(ordinal = 4)
    private List<List<Double>> points;
    @JSONField(ordinal = 5)
    private int nLane;
    @JSONField(ordinal = 6)
    private List<Double> laneWidths;

    public edge() {}

    public edge(Road road) {
        id = road.getId();
        from = road.getStartIntersection().getId();
        to = road.getEndIntersection().getId();
        points = new ArrayList<>();
        for (Point point: road.getPoints()) {
            List<Double> list = new ArrayList<>();
            points.add(list);
            list.add(point.x);
            list.add(point.y);
        }
        nLane = road.getLanes().size();
        laneWidths = new ArrayList<>();
        for (Lane lane: road.getLanes()) {
            laneWidths.add(lane.getWidth());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getnLane() {
        return nLane;
    }

    public void setnLane(int nLane) {
        this.nLane = nLane;
    }

    public List<Double> getLaneWidths() {
        return laneWidths;
    }

    public void setLaneWidths(List<Double> laneWidths) {
        this.laneWidths = laneWidths;
    }

    public List<List<Double>> getPoints() {
        return points;
    }

    public void setPoints(List<List<Double>> points) {
        this.points = points;
    }
}

public class RoadNetSerialization {
    @JSONField(ordinal = 1)
    private List<node> nodes;
    @JSONField(ordinal = 2)
    private List<edge> edges;

    public RoadNetSerialization() {}

    public RoadNetSerialization(RoadNet roadNet) {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        for (Intersection intersection: roadNet.getIntersections()) {
            node n = new node(intersection);
            nodes.add(n);
        }
        for (Road road: roadNet.getRoads()) {
            edge e = new edge(road);
            edges.add(e);
        }
    }

    public List<node> getNodes() {
        return nodes;
    }

    public void setNodes(List<node> nodes) {
        this.nodes = nodes;
    }

    public List<edge> getEdges() {
        return edges;
    }

    public void setEdges(List<edge> edges) {
        this.edges = edges;
    }
}
