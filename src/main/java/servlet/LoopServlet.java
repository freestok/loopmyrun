package servlet;

import servlet.loopmyrun.LatLng;
import servlet.loopmyrun.LoopFinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringJoiner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
        name = "LoopServlet",
        urlPatterns = {"/getLoop"}
    )
public class LoopServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse
            response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        System.out.println();
        System.out.println("-------------------");

        double lat = Double.parseDouble(request.getParameter("lat"));
        double lng = Double.parseDouble(request.getParameter("lng"));
        double userDist = Double.parseDouble(request.getParameter("distance"));
        LatLng p1 = new LatLng(lat, lng);


        LoopFinder loopFinder = new LoopFinder(p1,userDist);
        String jsonObject = loopFinder.findLoops();
//        for (int divider = 4; divider < 6; divider++) {
//            System.out.println("***DIVIDER: "+divider);
//            String jsonObject = loopFinder.findLoops();
//            if (jsonObject != null) geoJson.add(jsonObject);
//            loopFinder.setDivider(divider);
////            if (jsonObject != null) {
////                break;
////            }
////            else loopFinder.setDivider(divider);
//        }
//
//        String returnString = geoJson.toString();
//        returnString +="]}";
//        System.out.println(geoJson);

        if (jsonObject == null) jsonObject = "{\"message\": \"no loops\"}";

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(jsonObject);
        out.flush();
    }
    
}
