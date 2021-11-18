package util;

public class Util {
    public static int sign(double x) {
        return (x + Point.eps > 0 ? 1 : 0) - (x < Point.eps ? 1 : 0);
    }
}
