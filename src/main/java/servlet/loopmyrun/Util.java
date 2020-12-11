package servlet.loopmyrun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class Util {
    final public static Map<String, Integer> ways = new HashMap<String, Integer>() {{
        put("primary", 6);
        put("primary_link", 5);
        put("secondary", 4);
        put("secondary_link", 4);
        put("tertiary", 3);
        put("tertiary_link", 3);
        put("residential", 3);
        put("service", 4);
        put("footway", 1);
        put("pedestrian", 1);
        put("path", 1);
        put("unclassified", 1);
    }};

    public static double haversine(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {
        lon1 = Math.toRadians(lon1);
        lat1 = Math.toRadians(lat1);
        lon2 = Math.toRadians(lon2);
        lat2 = Math.toRadians(lat2);

        double lonDiff = lon2 - lon1;
        double latDiff = lat2 - lat1;

        double a = Math.pow(Math.sin(latDiff/2),2.0) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.pow(Math.sin(lonDiff/2),2.0);
        double c = 2 * Math.asin(Math.sqrt(a));
        double km = 6371 * c;
        double meters = km * 1000;
        return meters;
    }

    public static Point getNorthmost(ArrayList<Point> points) {
        double[] max = {-90};
        Point[] maxPoint = {new Point(0,0)};
        points.forEach(e -> {
            if (e.y() < max[0]) {
                max[0] = e.x();
                maxPoint[0] = e;
            }
        });
        return maxPoint[0];
    }

    public static Point getSouthmost(ArrayList<Point> points) {
        double[] max = {90};
        Point[] maxPoint = {new Point(0,0)};
        points.forEach(e -> {
            if (e.y() < max[0]) {
                max[0] = e.x();
                maxPoint[0] = e;
            }
        });
        return maxPoint[0];
    }

    public static Point getEastmost(ArrayList<Point> points) {
        double[] max = {-180};
        Point[] maxPoint = {new Point(0,0)};
        points.forEach(e -> {
            if (e.y() > max[0]) {
                max[0] = e.x();
                maxPoint[0] = e;
            }
        });
        return maxPoint[0];
    }

    public static Point getWestmost(ArrayList<Point> points) {
        double[] max = {180};
        Point[] maxPoint = {new Point(0,0)};
        points.forEach(e -> {
            if (e.y() < max[0]) {
                max[0] = e.x();
                maxPoint[0] = e;
            }
        });
        return maxPoint[0];
    }
}
