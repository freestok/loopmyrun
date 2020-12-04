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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LoopFinder {
    private Point userLoc;
    private double userDist;
    private int divider;
    private Point closestVert;
    private DefaultUndirectedWeightedGraph<String, DefaultWeightedEdge> graph;


    public LoopFinder(Point userLoc, double userDist, int divider) {
        this.userLoc = userLoc;
        this.userDist = userDist;
        this.divider = divider;
    }

    public String findLoops() {
        // make request
        String json = requestOverpass();

        // initialize graph
        this.graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        // parse JSON
        ArrayList<Point> vertexes = processJSON(json);
        Point northMost = Util.getNorthmost(vertexes);
        Point southMost = Util.getSouthmost(vertexes);
        Point eastMost = Util.getEastmost(vertexes);
        Point westMost = Util.getWestmost(vertexes);

        //TODO create routes
        ArrayList<LineString> northRoutes = createRoute(vertexes, northMost);
        ArrayList<LineString> southRoutes = createRoute(vertexes, southMost);
        ArrayList<LineString> eastRoutes = createRoute(vertexes, eastMost);
        ArrayList<LineString> westRoutes = createRoute(vertexes, westMost);

        //TODO extract contents, convert to JSON
        ArrayList<LineString> combinedRoutes = new ArrayList<>();
        combinedRoutes.addAll(northRoutes);
        combinedRoutes.addAll(southRoutes);
        combinedRoutes.addAll(eastRoutes);
        combinedRoutes.addAll(westRoutes);

        // TODO return something else
        MultiLineString allRoutes = new MultiLineString(combinedRoutes);
        return allRoutes.asGeoJSON();
    }

    private String requestOverpass() {
        String query = "[out:json];way[highway][\"highway\"~\"primary|secondary|tertiary|" +
                "residential|unclassified|primary_link|secondary_link|tertiary_link|service|path\"]" +
                "(around:"+userDist/(divider-1)+","+userLoc.x()+","+userLoc.y()+");out geom;";
        HttpRequest response = HttpRequest
                .post("http://overpass-api.de/api/interpreter")
                .send("data="+query);
        String json = response.body();
        return json;
    }

    private ArrayList<Point> processJSON(String json) {
        ArrayList<Point> vertexWithin = new ArrayList<>();
        Point[] closestVert = {new Point(0,0)};
        double[] closestDist = {1e7};

        JSONObject jsonObj = new JSONObject(json);
        JSONArray elements = jsonObj.getJSONArray("elements");
        elements.forEach(e -> {
            JSONObject element = ((JSONObject) e); // recast object to JSON Object
            JSONObject tags = element.getJSONObject("tags");
            String tag = tags.getString("highway");
            JSONArray geom = element.getJSONArray("geometry");
            ArrayList<Point> points = new ArrayList<Point>();
            geom.forEach(g -> {
                JSONObject xy = ((JSONObject) g);
                double lon = xy.getDouble("lon");
                double lat = xy.getDouble("lat");
                Point p = new Point(lat, lon);
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
            LineString line = new LineString(points);
            line.createEdges(graph, tag);
        });
        this.closestVert = closestVert[0];
        return vertexWithin;
    }

    private ArrayList<LineString> createRoute(ArrayList<Point> vertexes, Point originPoint) {
        ClockwiseSort clockwiseSort = new ClockwiseSort(originPoint);
        Collections.sort(vertexes, clockwiseSort);
        //TODO traverse circle
        ArrayList<LineString> route = traverseCircle(vertexes);
        return route;
    }

    private ArrayList<LineString> traverseCircle(ArrayList<Point> vertexes) {
        int length = vertexes.size();
        long jumpAdd = (long) Math.floor(length/10);
        ArrayList<LineString> finishedCycles = new ArrayList<>();

        ArrayList<Point> routes = new ArrayList<>();
        for (int i = 3; i < 10; i++) { // traverse circle
            long jump = 0;
            ArrayList<Point> route = new ArrayList<>();

            for (int j = 0; j < i; j++) { // points by jump
                Point nextPoint = vertexes.get((int) jump);
                route.add(nextPoint);
                jump += jumpAdd;
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
                ArrayList<Point> cyclePoints = new ArrayList<>();
                for (DefaultEdge subEdge: subL) {
                    String[] src = subGraph.getEdgeSource(subEdge).split(",");
                    String[] end = subGraph.getEdgeTarget(subEdge).split(",");

                    double srcX = Double.parseDouble(src[0]);
                    double srcY = Double.parseDouble(src[1]);
                    double endX = Double.parseDouble(end[0]);
                    double endY = Double.parseDouble(end[1]);
                    cyclePoints.add(new Point(srcY, srcX));
                    cyclePoints.add(new Point(endY, endX));
                }
                LineString cycleLine = new LineString(cyclePoints);
                double lowerRange = userDist * .75;
                double upperRange = userDist * 1.25;
                if (cycleLine.getLength() < upperRange && cycleLine.getLength() > lowerRange) {
                    finishedCycles.add(cycleLine);
                    System.out.println("*****");
                    System.out.println(cycleLine.getLength());
                    System.out.println(cycleLine.printCoords());
                    System.out.println("*****");
                }
            }
        }
        return finishedCycles;
    }
}
