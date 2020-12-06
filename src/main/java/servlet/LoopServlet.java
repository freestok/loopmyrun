package servlet;

import servlet.loopmyrun.LoopFinder;
import servlet.loopmyrun.Point;
import servlet.loopmyrun.Util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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
        Point p1 = new Point(lat, lng);

        String jsonObject = "";
        LoopFinder loopFinder = new LoopFinder(p1,userDist,3);
        for (int divider = 4; divider < 8; divider++) {
            System.out.println("***DIVIDER: "+divider);
            jsonObject = loopFinder.findLoops();
            if (jsonObject != null) {
                break;
            }
            else loopFinder.setDivider(divider);
        }

        if (jsonObject == null) jsonObject = "{\"message\": \"no loops\"}";

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(jsonObject);
        out.flush();
    }
    
}
