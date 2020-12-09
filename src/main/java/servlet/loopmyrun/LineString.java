package servlet.loopmyrun;

import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;

import static servlet.loopmyrun.Util.ways;

public class LineString {
    private ArrayList<Point> linestring;
    private double length;


    public LineString(ArrayList<Point> linestring){
        this.linestring = linestring;
//        this.length = calcLength();
    }

    public void createEdges(DefaultUndirectedWeightedGraph<String,DefaultWeightedEdge> g, String tag) {
        ArrayList<Point> line = this.linestring;
        for (int i = 0; i < line.size(); i++) {
            if (i + 1 < line.size()) {
                String p1 = line.get(i).coords();
                String p2 = line.get(i+1).coords();
                DefaultWeightedEdge edge = g.addEdge(p1, p2);
                Integer weight = ways.get(tag);
                try {
                    g.setEdgeWeight(edge,weight);
                }
                catch(Exception e) {
                    System.out.println(e);
                }
            }
        }
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

//    private double calcLength() {
//        double length, totalLength;
//        double lat1, lon1, lat2, lon2;
//        Point pt1, pt2;
//
//        totalLength = 0.0;
//        for (int i = 0; i < this.linestring.size(); i++) {
//            if (i + 1 < this.linestring.size()) {
//                pt1 = this.linestring.get(i);
//                pt2 = this.linestring.get(i+1);
//
//                lat1 = pt1.x();
//                lon1 = pt1.y();
//                lat2 = pt2.x();
//                lon2 = pt2.y();
//
//                length = Util.haversine(lat1, lon1 , lat2, lon2);
//                totalLength += length;
//            }
//        }
//        return totalLength;
//    }




}
