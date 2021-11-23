package util;
import entity.roadNet.roadNet.Lane;

import java.util.*;

public class Point {
    public double x;
    public double y;

    public static final double eps = 1e-8;

    public Point() {}

    public Point(Point A) {
        x = A.x;
        y = A.y;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Point plus(Point A) {
        Point ret = new Point();
        ret.x = this.x + A.x;
        ret.y = this.y + A.y;
        return ret;
    }

    public Point minus(Point A) {
        Point ret = new Point();
        ret.x = this.x - A.x;
        ret.y = this.y - A.y;
        return ret;
    }

    public Point multiply(double k) {
        Point ret = new Point();
        ret.x = this.x * k;
        ret.y = this.y * k;
        return ret;
    }

    public Point opposite() { // 向量反向
        Point ret = new Point();
        ret.x = this.x * -1;
        ret.y = this.y * -1;
        return ret;
    }

    public double len() {
        return Math.sqrt(x * x + y * y);
    }

    public Point normal() {
        return new Point(-y, x);
    }

    public Point unit() {
        double l = len();
        return new Point(x / l, y / l);
    }

    public double ang() {
        return Math.atan2(y, x);
    }

    public static List<Point> calLaneLinkPoints(Lane startLane, Lane endLane) {
        List<Point> ret = new ArrayList<>();
        Point start = getPointByDistance(startLane.getPoints(), startLane.getLength() - startLane.getEndIntersection().getWidth());
        Point end = getPointByDistance(endLane.getPoints(), endLane.getLength() - endLane.getStartIntersection().getWidth());
        double len = end.minus(start).len();
        Point startDirection = getDirectionByDistance(startLane.getPoints(), startLane.getLength() - startLane.getEndIntersection().getWidth());
        Point endDirection = getDirectionByDistance(endLane.getPoints(), endLane.getLength() - endLane.getStartIntersection().getWidth());
        double minGap = 5;
        double gap1X = startDirection.x * len * 0.5;
        double gap1Y = startDirection.y * len * 0.5;
        double gap2X = -endDirection.x * len * 0.5;
        double gap2Y = -endDirection.y * len * 0.5;
        if (gap1X * gap1X + gap1Y * gap1Y < 25 && startLane.getEndIntersection().getWidth() >= 5) {
            gap1X = minGap * startDirection.x;
            gap1Y = minGap * startDirection.y;
        }
        if (gap2X * gap2X + gap2Y * gap2Y < 25 && endLane.getStartIntersection().getWidth() >= 5) {
            gap2X = minGap * endDirection.x;
            gap2Y = minGap * endDirection.y;
        }
        Point mid1 = new Point(start.x + gap1X, start.y + gap1Y);
        Point mid2 = new Point(end.x + gap2X, end.y + gap2Y);
        int numPoints = 10;
        for (int i = 0; i <= numPoints; i++) {
            Point p1 = getPoint(start, mid1, (double)i / numPoints);
            Point p2 = getPoint(mid1, mid2, (double)i / numPoints);
            Point p3 = getPoint(mid2, end, (double)i / numPoints);
            Point p4 = getPoint(p1, p2, (double)i / numPoints);
            Point p5 = getPoint(p2, p3, (double)i / numPoints);
            Point p6 = getPoint(p4, p5, (double)i / numPoints);
            ret.add(new Point(p6.x, p6.y));
        }
        return ret;
    }

    public static Point getPoint(Point p1, Point p2, double a) {
        return new Point((p2.x - p1.x) * a + p1.x, (p2.y - p1.y) * a + p1.y);
    }

    public static List<Point> calConvexHull(List<Point> points) {
        List<Point> ret = new ArrayList<>();
        points.sort((o1, o2) -> {
            if (o1.y == o2.y) {
                return (int) (o1.x - o2.x);
            }
            return (int)(o1.y - o2.y);
        });
        Point p0 = points.get(0);
        ret.add(p0);
        points.remove(0);
        points.sort((o1, o2) -> {
            if (o1.minus(p0).ang() < o2.minus(p0).ang()) {
                return -1;
            } else if (o1.minus(p0).ang() == o2.minus(p0).ang()) {
                return 0;
            }
            return 1;
        });
        for (Point point : points) {
            Point p2 = ret.get(ret.size() - 1);
            if (ret.size() < 2) {
                if (point.x != p2.x || point.y != p2.y) {
                    ret.add(point);
                }
                continue;
            }
            Point p1 = ret.get(ret.size() - 2);
            while (ret.size() > 1 && crossMultiply(point.minus(p2), p2.minus(p1)) >= 0) {
                p2 = p1;
                ret.remove(ret.size() - 1);
                if (ret.size() > 1) {
                    p1 = ret.get(ret.size() - 2);
                }
            }
            ret.add(point);
        }
        return ret;
    }

    public static Point plus(Point A, Point B) {
        Point ret = new Point();
        ret.x = A.x + B.x;
        ret.y = A.y + B.y;
        return ret;
    }

    public static Point minus(Point A, Point B) {
        Point ret = new Point();
        ret.x = A.x - B.x;
        ret.y = A.y - B.y;
        return ret;
    }

    public static Point multiply(Point A, double k) {
        Point ret = new Point();
        ret.x = A.x * k;
        ret.y = A.y * k;
        return ret;
    }

    public static double crossMultiply(Point A, Point B) {
        return A.x * B.y - A.y * B.x;
    }

    public static double dotMultiply(Point A, Point B) {
        return A.x * B.x + A.y * B.y;
    }

    public static double calcAng(Point A, Point B) {
        double ang = A.ang() - B.ang();
        double pi = Math.acos(-1);
        while (ang >= pi / 2) {
            ang -= pi / 2;
        }
        while (ang < 0) {
            ang += pi / 2;
        }
        return Math.min(ang, pi - ang);
    }

    public static Point calcIntersectionPoint(Point A, Point B, Point C, Point D) {
        Point u = B.minus(A);
        Point v = D.minus(C);
        return A.plus(u.multiply(crossMultiply(C.minus(A), v) / crossMultiply(u, v)));
    }

    public static boolean onSegment(Point A, Point B, Point C) {
        double v1 = crossMultiply(B.minus(A), C.minus(A));
        double v2 = dotMultiply(C.minus(A), C.minus(A));
        return Util.sign(v1) == 0 && Util.sign(v2) <= 0;
    }

    public static double getLengthOfPoints(List<Point> points) {
        double length = 0;
        for (int i = 0; i + 1 < points.size(); i++) {
            length += Point.minus(points.get(i + 1), points.get(i)).len();
        }
        return length;
    }

    public static Point getPointByDistance(List<Point> points, double dis) {
        double distance = Math.min(Math.max(dis, 0), getLengthOfPoints(points));
        if (distance <= 0) {
            return points.get(0);
        }
        for (int i = 1; i < points.size(); i++) {
            double len = (points.get(i - 1).minus(points.get(i))).len();
            if (dis > len) {
                dis -= len;
            } else {
                return points.get(i - 1).plus(points.get(i).minus(points.get(i - 1)).multiply(distance / len));
            }
        }
        return points.get(points.size() - 1);
    }

    public static Point getDirectionByDistance(List<Point> points, double dis) {
        double remain = dis;
        for (int i = 0; i + 1 < points.size(); i++) {
            double len = points.get(i + 1).minus(points.get(i)).len();
            if (remain < len) {
                return points.get(i + 1).minus(points.get(i)).unit();
            } else {
                remain -= len;
            }
        }
        return points.get(points.size() - 1).minus(points.get(points.size() - 2)).unit();
    }
}
