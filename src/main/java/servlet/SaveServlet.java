package servlet;

import org.apache.commons.lang3.RandomStringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

@WebServlet(
        name = "SaveServlet",
        urlPatterns = {"/save"}
    )
public class SaveServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /** get route from routes database */
        System.out.println("DO GET");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String uuid = request.getParameter("id");
        System.out.println("UUID " +  uuid);

        boolean b = Pattern.matches("([0-9BCDFGHJKLMNPQRSTVWXYZ]){8}", uuid);
        if (b == true) {
            DBUtility util = new DBUtility();
            String query = "select route from routes where uuid = '"+uuid+"';";
            System.out.println("QUERY: " +  query);
            ResultSet res = util.queryDB(query);
            ResultSet count = util.queryDB("select count(*) as total from routes");
            System.out.println("QUERIES SUCCESSFUL");

            // check if database is getting full
            try {
                count.next();
                int size = Integer.parseInt(count.getString("total"));
                System.out.println("SIZE: " + size);
                if (size > 9500) {
                    util.queryDB("DELETE FROM routes WHERE id NOT IN (SELECT TOP 500 ID FROM Table)");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // return JSON string representing route
            try {
                res.next();

                String routeJSON = res.getString("route");
                PrintWriter out = response.getWriter();
//                response.sendRedirect(response.encodeRedirectURL("/"));
                out.print(routeJSON);
                out.flush();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse
            response) throws ServletException, IOException {
        /** save route to routes database */
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String route = request.getParameter("route");
        DBUtility util = new DBUtility();
        String shortId = RandomStringUtils.random(8, "0123456789BCDFGHJKLMNPQRSTVWXYZ");
        while (1 < 2) {
            String res = util.modifyDB("INSERT INTO routes (uuid, route) VALUES ('" + shortId + "','"+route+"')");
            System.out.println("COMPLETE");
            if (res == "success") break;
            else shortId = RandomStringUtils.random(8, "0123456789BCDFGHJKLMNPQRSTVWXYZ");
        }
        PrintWriter out = response.getWriter();
        out.print("{\"id\": \""+shortId+"\"}");
        out.flush();
    }
}
