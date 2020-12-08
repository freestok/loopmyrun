package servlet.loopmyrun;

import com.github.kevinsawicki.http.HttpRequest;
//import com.sun.org.apache.xpath.internal.operations.Mult;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.cycle.StackBFSFundamentalCycleBasis;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class LoopFinder {
    private LatLng userLoc;
    private double userDist;
    private int divider;
    private LatLng closestVert;
    private String json;
    private DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge> graph;


    public LoopFinder(LatLng userLoc, double userDist) {
        this.userLoc = userLoc;
        this.userDist = userDist;
        this.divider = divider;
        this.json = requestOverpass(userDist,userLoc);
    }

    private void setDivider(int divider) {
        this.divider = divider;
    }

    public String findLoops() {
        ArrayList<Line> masterRoutes = new ArrayList<>();
        for (int divider = 3; divider < 7; divider++) {
            ArrayList<Line> loops = getLoops(divider);
            if (loops != null) masterRoutes.addAll(loops);

            setDivider(divider);

//            if (userDist >= 2) break; // don't do this multiple times if larger than 10 miles
        }

        if (masterRoutes == null) return null;

        // return as GeoJson
        MultiLineString allRoutes = new MultiLineString(masterRoutes);
        return allRoutes.asGeoJSON();
    }

    private ArrayList<Line> getLoops(int divider) {
        setDivider(divider);
        System.out.println("DIVIDER: " + divider);

        // initialize graph
        this.graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        // parse JSON
        ArrayList<LatLng> vertexes = processJSON(json);
        LatLng northMost = Util.getNorthmost(vertexes);
        LatLng southMost = Util.getSouthmost(vertexes);
        LatLng eastMost = Util.getEastmost(vertexes);
        LatLng westMost = Util.getWestmost(vertexes);

        // create routes
        ArrayList<Line> northRoutes = createRoute(vertexes, northMost);
        ArrayList<Line> southRoutes = createRoute(vertexes, southMost);
        ArrayList<Line> eastRoutes = createRoute(vertexes, eastMost);
        ArrayList<Line> westRoutes = createRoute(vertexes, westMost);

        // combine routes into one array
        ArrayList<Line> combinedRoutes = new ArrayList<>();
        if (northRoutes != null) combinedRoutes.addAll(northRoutes);
        if (southRoutes != null) combinedRoutes.addAll(southRoutes);
        if (eastRoutes != null) combinedRoutes.addAll(eastRoutes);
        if (westRoutes != null) combinedRoutes.addAll(westRoutes);

        if (combinedRoutes.isEmpty()) return null;

        // remove duplicate routes
        Set<Line> s = new LinkedHashSet<>(combinedRoutes);
        combinedRoutes.clear();
        combinedRoutes.addAll(s);
        return combinedRoutes;
    }

    private String requestOverpass(double userDist, LatLng userLoc) {
        int div;
        if (userDist <= 10000) div = 2;
        else if (userDist <= 20000) div = 3;
        else if (userDist <= 30000) div = 4;
        else if (userDist <= 40000) div = 5;
        else div = 6;
        String query = "[out:json];way[highway][\"highway\"~\"primary|secondary|tertiary|" +
                "residential|unclassified|primary_link|secondary_link|tertiary_link|service|path\"]" +
                "(around:"+userDist/div+","+userLoc.x()+","+userLoc.y()+");out geom;";
        HttpRequest response = HttpRequest
                .post("http://overpass-api.de/api/interpreter")
                .send("data="+query);
        String json = response.body();
        return json;
    }

    private ArrayList<LatLng> processJSON(String json) {
        ArrayList<LatLng> vertexWithin = new ArrayList<>();
        LatLng[] closestVert = {new LatLng(0,0)};
        double[] closestDist = {1e7};

        JSONObject jsonObj = new JSONObject(json);
        JSONArray elements = jsonObj.getJSONArray("elements");
        elements.forEach(e -> {
//            System.out.println(e);
            JSONObject element = ((JSONObject) e); // recast object to JSON Object
            JSONObject tags = element.getJSONObject("tags");
            String tag = tags.getString("highway");
            JSONArray geom = element.getJSONArray("geometry");
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            geom.forEach(g -> {
                JSONObject xy = ((JSONObject) g);
                double lon = xy.getDouble("lon");
                double lat = xy.getDouble("lat");
                LatLng p = new LatLng(lat, lon);
                points.add(p);
                graph.addVertex(p.coords());
                double dist = Util.haversine(userLoc.x(), userLoc.y(), lat, lon);

                // check if it's the closest point
                if (dist < closestDist[0]) {
                    closestDist[0] = dist;
                    closestVert[0] = p;
                }

                // check if point is within certain distance
                double lowerRange = (userDist/divider) * .95;
                double upperRange = (userDist/divider) * 1.05;
                if (dist >= lowerRange && dist <= upperRange) {
                    vertexWithin.add(p);
                }
            });
            Line line = new Line(points);
            line.createEdges(graph, tag);
        });
        System.out.println("Done going through elements");
        this.closestVert = closestVert[0];
        return vertexWithin;
    }

    private ArrayList<Line> createRoute(ArrayList<LatLng> vertexes, LatLng originPoint) {
        ClockwiseSort clockwiseSort = new ClockwiseSort(originPoint);
        Collections.sort(vertexes, clockwiseSort);
        ArrayList<Line> route = traverseCircle(vertexes);
        return route;
    }

    private ArrayList<Line> traverseCircle(ArrayList<LatLng> vertexes) {
        int length = vertexes.size();
        long jumpAdd = (long) Math.floor(length/10);
        ArrayList<Line> finishedCycles = new ArrayList<>();
        ArrayList<Double> finishedLengths = new ArrayList<>();
        for (int i = 3; i < 10; i++) { // traverse circle
            long jump = 0;
            ArrayList<LatLng> route = new ArrayList<>();

            try {
                for (int j = 0; j < i; j++) { // points by jump
                    LatLng nextPoint = vertexes.get((int) jump);
                    route.add(nextPoint);
                    jump += jumpAdd;
                }
            } catch(Exception e) {
                return null;
            }


            route.add(0,closestVert);
            route.add(route.size(),closestVert);

            ArrayList<GraphPath<String, DefaultWeightedEdge>> paths = new ArrayList<>();

            // get shortest distance paths and append to paths
            for (int p = 0; p < route.size(); p++) {
                if (p+1 < route.size()) {
                    DijkstraShortestPath<String, DefaultWeightedEdge> dijkstraAlg;
                    dijkstraAlg = new DijkstraShortestPath<>(graph);
                    ShortestPathAlgorithm.SingleSourcePaths<String, DefaultWeightedEdge> pthSrc =
                            dijkstraAlg.getPaths(route.get(p).coords());

                    GraphPath<String, DefaultWeightedEdge> pth = pthSrc.getPath(route.get(p+1).coords());
                    if (pth == null) continue;
                    paths.add(pth);
                }
            }
            // initialize subgraph AND add vertex & edges
            DefaultUndirectedGraph<String, DefaultEdge> subGraph;
            subGraph = new DefaultUndirectedGraph<>(DefaultEdge.class);
            for (GraphPath<String, DefaultWeightedEdge> path: paths) {
                List<String> vertList = path.getVertexList();
                for (String vertex: vertList) {
                    subGraph.addVertex(vertex);
                }
                for (int v=0; v < vertList.size() -1; v++) {
                    subGraph.addEdge(vertList.get(v),vertList.get(v+1));
                }
            }

            // -----------------------------------------------------
            // -----------------------------------------------------
            // get cycle from subGraph
            StackBFSFundamentalCycleBasis<String, DefaultEdge> cycleDetector = new StackBFSFundamentalCycleBasis<>(subGraph);
            Set<List<DefaultEdge>> cycles = cycleDetector.getCycleBasis().getCycles();

            for (List<DefaultEdge> subL: cycles) {
                // deconstruct cycle and recast to a linestring
                ArrayList<LatLng> cyclePoints = new ArrayList<>();
                for (DefaultEdge subEdge: subL) {
                    String[] src = subGraph.getEdgeSource(subEdge).split(",");
                    String[] end = subGraph.getEdgeTarget(subEdge).split(",");

                    double srcX = Double.parseDouble(src[0]);
                    double srcY = Double.parseDouble(src[1]);
                    double endX = Double.parseDouble(end[0]);
                    double endY = Double.parseDouble(end[1]);
                    cyclePoints.add(new LatLng(srcY, srcX));
                    cyclePoints.add(new LatLng(endY, endX));
                }
                Line cycleLine = new Line(cyclePoints);
//                double lowerRange = userDist * .75;
//                double upperRange = userDist * 1.25;
                finishedCycles.add(cycleLine);
//                if (cycleLine.getLength() < upperRange && cycleLine.getLength() > lowerRange) {
//
//                    // check to see if duplicate
//                    if (finishedLengths.contains(cycleLine.getLength())) {
//                        continue;
//                    }
//                    finishedCycles.add(cycleLine);
//                    System.out.println("*****************");
//                    System.out.println(cycleLine.getLength());
//                    System.out.println(cycleLine.printCoords());
//                    finishedLengths.add(cycleLine.getLength());
//                }
            }
        }
        System.out.println("Loop Finder done");
        return finishedCycles;
    }
}
