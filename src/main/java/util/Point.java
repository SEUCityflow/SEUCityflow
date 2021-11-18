package util;
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

    public double len() { // 向量长度
        return Math.sqrt(x * x + y * y);
    }

    public Point normal() { // 垂直向
        return new Point(-y, x);
    }

    public Point unit() { // 单位化
        double l = len();
        return new Point(x / l, y / l);
    }

    public double ang() { // 向量倾角
        return Math.atan2(y, x);
    }

    public static Point getPoint(Point p1, Point p2, double a) { // p1, p2 之间占比 a 处的点坐标
        return new Point((p2.x - p1.x) * a + p1.x, (p2.y - p1.y) * a + p1.y);
    }

    public static List<Point> calConvexHull(List<Point> points) { // 凸包
        List<Point> ret = new ArrayList<Point>();
        List<Point> temp = new ArrayList<Point>(points);
        temp.sort(Comparator.comparing(Point::getX));
        Point p0 = temp.get(0);
        ret.add(p0);
        temp.remove(p0);
        temp.sort(Comparator.comparing(Point::ang));
        for (int i = 0; i < temp.size(); i++) {
            Point point = points.get(i);
            Point p2 = ret.get(ret.size() - 1);
            if (ret.size() < 2) {
                if (point.x != p2.x || point.y != p2.y) {
                    ret.add(point);
                } else {
                    continue;
                }
            }
            Point p1 = ret.get(ret.size() - 2);
            while (ret.size() > 1 && crossMultiply(point.minus(p2), p2.minus(p1)) >= 0) {
                p2 = p1;
                ret.remove(ret.size() - 1);
                if (ret.size() > 1) {
                    p1 = ret.get(ret.size() -2);
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

    public static double crossMultiply(Point A, Point B) { // 叉乘
        return A.x * B.y - A.y * B.x;
    }

    public static double dotMultiply(Point A, Point B) { // 点乘
        return A.x * B.x + A.y * B.y;
    }

    public static double calcAng(Point A, Point B) { // 向量夹角
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

    public static Point calcIntersectionPoint(Point A, Point B, Point C, Point D) { // 向量交点
        Point u = B.minus(A);
        Point v = D.minus(C);
        return A.plus(u.multiply(crossMultiply(C.minus(A), v) / crossMultiply(u, v)));
    }

    public static boolean onSegment(Point A, Point B, Point C) { // C 是否在线段 AB 上
        double v1 = crossMultiply(B.minus(A), C.minus(A));
        double v2 = dotMultiply(C.minus(A), C.minus(A));
        return Util.sign(v1) == 0 && Util.sign(v2) <= 0;
    }

    public static double getLengthOfPoints(List<Point> points) { // 线段长度
        double length = 0;
        for (int i = 0; i + 1 < points.size(); i++) {
            length += Point.minus(points.get(i + 1), points.get(i)).len();
        }
        return length;
    }

    public static Point getPointByDistance(List<Point> points, double dis) { // 距线段起点 dis 处的点
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
}
