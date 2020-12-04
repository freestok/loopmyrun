package servlet.loopmyrun;

import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LineString {
    private ArrayList<Point> linestring;
    private double length;
    Map<String, Integer> ways = new HashMap<String, Integer>() {{
        put("primary", 6);
        put("primary_link", 5);
        put("secondary", 4);
        put("secondary_link", 4);
        put("tertiary", 3);
        put("tertiary_link", 3);
        put("residential", 2);
        put("service", 2);
        put("pedestrian", 1);
        put("footway", 1);
        put("pedestrian", 1);
        put("path", 1);
    }};

    public LineString (ArrayList<Point> linestring){
        this.linestring = linestring;
        this.length = calcLength();
    }

    public void createEdges(DefaultUndirectedWeightedGraph<String,DefaultWeightedEdge> g, String tag) {
        ArrayList<Point> line = this.linestring;
        for (int i = 0; i < line.size(); i++) {
            if (i + 1 < line.size()) {
                String p1 = line.get(i).coords();
                String p2 = line.get(i+1).coords();
                DefaultWeightedEdge edge = g.addEdge(p1, p2);
                Integer weight = ways.get(tag);
                g.setEdgeWeight(edge,weight);
            }
        }
//        return g;
    }

    public String getPoint(int i) { return this.linestring.get(i).coords(); }
    public double getLength() { return this.length; }

    public String printCoords() {
        String toPrint = "";
        for (Point point: this.linestring) {
            toPrint += "["+point.coords()+"], ";
        }
        toPrint = toPrint.substring(0,toPrint.length()-2);
        toPrint = "[" + toPrint + "]";
        return toPrint;
    }

    private double calcLength() {
        double length, totalLength;
        double lat1, lon1, lat2, lon2;
        Point pt1, pt2;

        totalLength = 0.0;
        for (int i = 0; i < this.linestring.size(); i++) {
            if (i + 1 < this.linestring.size()) {
                pt1 = this.linestring.get(i);
                pt2 = this.linestring.get(i+1);

                lat1 = pt1.x();
                lon1 = pt1.y();
                lat2 = pt2.x();
                lon2 = pt2.y();

//                length = Dist.distVincenty(lat1, lon1 , lat2, lon2);
                length = Util.haversine(lat1, lon1 , lat2, lon2);
                totalLength += length;
            }
        }
        return totalLength;
    }


}