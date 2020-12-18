package servlet;

import org.apache.commons.lang3.RandomStringUtils;
import servlet.loopmyrun.Point;
import servlet.loopmyrun.LoopFinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
        name = "GetLoopServlet",
        urlPatterns = {"/getLoop"}
    )
public class GetLoopServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /** get route when ready and return to front-end, delete route when returned */
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String uuid = request.getParameter("uuid");
        System.out.println("UUID " + uuid);

        boolean b = Pattern.matches("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b", uuid);
        if (b == true) {
            DBUtility util = new DBUtility();
            ResultSet res = util.queryDB("select route from temp where uuid = '"+uuid+"'");
            try {
                String routeJSON = "";
                while (res.next()) {
                    routeJSON = res.getString("route");
                }

                PrintWriter out = response.getWriter();
                System.out.println(routeJSON.length());
                if (routeJSON.length() == 10) out.print("{\"message\": \"processing\"}");
                else if (routeJSON.length() == 5) out.print("{\"message\": \"error\"}");
                else if (routeJSON.length() > 10) {
                    // delete from temp
                    util.modifyDB("DELETE FROM temp WHERE 1=1");
                    out.print(routeJSON);
                }
                out.flush();
            } catch (SQLException throwables) {
                System.out.println("ERROR");
            }
        }
        else System.out.println("invalid value");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse
            response) throws ServletException, IOException {

        System.out.println();
        System.out.println("-------------------");

        String uuid = request.getParameter("uuid");
        double lat = Double.parseDouble(request.getParameter("lat"));
        double lng = Double.parseDouble(request.getParameter("lng"));
        double userDist = Double.parseDouble(request.getParameter("distance"));

        DBUtility util = new DBUtility();

        // check to see if valid uuid
        boolean b = Pattern.matches("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b", uuid);
        if (b == false) {
            System.out.println("bad value");
            util.modifyDB("INSERT INTO routes VALUES ('------------------------------------','BAD VALUE')");
        } else {
            try {
                System.out.println("good value");
                util.modifyDB("INSERT INTO temp (uuid, route) VALUES ('" + uuid + "','processing')");
                Point p1 = new Point(lat, lng);
                LoopFinder loopFinder = new LoopFinder(p1,userDist);
                String jsonObject = loopFinder.findLoops();

                if (jsonObject == null) jsonObject = "{\"message\": \"no loops\"}";

                // UPDATE films SET kind = 'Dramatic' WHERE kind = 'Drama';
                util.modifyDB("UPDATE temp SET route = '"+jsonObject+"' WHERE uuid = '"+uuid+"';");
                System.out.println("COMPLETE");
            }
            catch(Exception e) {
                util.modifyDB("UPDATE temp SET route = 'error' WHERE uuid = '"+uuid+"';");
            }
        }
    }
}
