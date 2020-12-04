package servlet;

import servlet.loopmyrun.LoopFinder;
import servlet.loopmyrun.Point;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
        name = "MyServlet", 
        urlPatterns = {"/getLoop"}
    )
public class LoopServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Point p1 = new Point(42.96273444046179, -85.6396280135749);
        double userDist = 1600.0;
        int divider = 3;
        LoopFinder loopFinder = new LoopFinder(p1,userDist,divider);
        String jsonObject = loopFinder.findLoops();

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print(jsonObject);
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse
            response) throws ServletException, IOException {
        Point p1 = new Point(42.96273444046179, -85.6396280135749);
        double userDist = 1600.0;
        int divider = 3;
        LoopFinder loopFinder = new LoopFinder(p1,userDist,divider);
        String jsonObject = loopFinder.findLoops();

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(jsonObject);
        out.flush();
    }
    
}
