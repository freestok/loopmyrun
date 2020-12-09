package servlet.loopmyrun;

import java.util.ArrayList;
import java.util.StringJoiner;

public class MultiLineString {
    private ArrayList<LineString> multilinestring;

    public MultiLineString(ArrayList<LineString> multilinestring) {
        this.multilinestring = multilinestring;
    }

    public String asGeoJSON() {
        System.out.println("Constructing GeoJSON...");
        StringJoiner geoJson = new StringJoiner("");
        geoJson.add("{\"type\": \"FeatureCollection\",");
        geoJson.add("\"features\": [");

        for (LineString line: multilinestring) {
            String feature;
            feature = "{\"type\":\"Feature\",\"properties\":{\"ln\":"+ line.getLength()+"},";
            feature += "\"geometry\":{\"type\":\"LineString\",\"coordinates\":" + line.printCoords() + "}},";
            geoJson.add(feature);
        }
        String returnString =geoJson.toString();

        returnString = returnString.substring(0,returnString.length()-1);
        returnString +="]}";
        return returnString;
    }
}
