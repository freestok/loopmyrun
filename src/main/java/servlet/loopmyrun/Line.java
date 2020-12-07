package servlet.loopmyrun;

import org.gavaghan.geodesy.GlobalPosition;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;

import static servlet.loopmyrun.Util.geoCalc;
import static servlet.loopmyrun.Util.reference;
import static servlet.loopmyrun.Util.ways;

public class Line {
    private ArrayList<LatLng> linestring;
    private double length;


    public Line(ArrayList<LatLng> linestring){
        this.linestring = linestring;
        this.length = calcLength();
    }

    public void createEdges(DefaultUndirectedWeightedGraph<String,DefaultWeightedEdge> g, String tag) {
        ArrayList<LatLng> line = this.linestring;
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
        for (LatLng point: this.linestring) {
            toPrint += "["+point.coords()+"], ";
        }
        toPrint = toPrint.substring(0,toPrint.length()-2);
        toPrint = "[" + toPrint + "]";
        return toPrint;
    }

    private double calcLength() {
        double length, totalLength;
        double lat1, lon1, lat2, lon2;
        LatLng pt1, pt2;

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

    private double calcGeodeticLength() {
        double totalLength;
        LatLng pt1, pt2;

        totalLength = 0.0;
        for (int i = 0; i < this.linestring.size(); i++) {
            if (i + 1 < this.linestring.size()) {
                pt1 = this.linestring.get(i);
                pt2 = this.linestring.get(i+1);
                GlobalPosition pointA = new GlobalPosition(pt1.x(), pt1.y(), 0.0); // LatLng A
                GlobalPosition pointB = new GlobalPosition(pt2.x(), pt2.y(), 0.0); // LatLng B

                double distance = geoCalc.calculateGeodeticCurve(reference, pointB, pointA).getEllipsoidalDistance();
                totalLength += distance;
            }
        }
        return totalLength;
    }

    private double calculateLengthAlongLineString(LineString theLine, Coordinate coordinateOnTheLine){
        GeometryFactory factory = new GeometryFactory();
        double length = 0;
        // create point to check for intersection with line
        Point pointOnTheLine = factory.createPoint(coordinateOnTheLine);
        Coordinate[] theLineCoordinates = theLine.getCoordinates();
        // iterate over linestring and create sub-lines for each coordinate pair
        for(int i = 1; i < theLineCoordinates.length; i++){
            LineString currentLine = factory.createLineString(new Coordinate[]{theLineCoordinates[i-1], theLineCoordinates[i]});
            // check if coordinateOnTheLine is on currentLine
            if(currentLine.intersects(pointOnTheLine)){
                // create new currentLine with coordinateOnTheLine as endpoint and calculate length
                currentLine = factory.createLineString(new Coordinate[]{theLineCoordinates[i-1], coordinateOnTheLine});
                length += currentLine.getLength();
                // return result length
                return length;
            }
            length += currentLine.getLength();
        }
        // coordinate was not on the line -> return length of complete linestring...
        return length;
    }


}
