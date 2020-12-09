package servlet;

import org.apache.commons.lang3.RandomStringUtils;

import java.net.URI;
import java.sql.*;

public class DBUtility {

    private static final String Driver = "org.postgresql.Driver";
    private static final String ConnUrl = System.getenv("DATABASE_URL");

    // This is a constructor
    public DBUtility() {}

    // create a Connection to the database
    private Connection connectDB() {
//        URI dbUri = new URI(System.getenv("DATABASE_URL"));


        Connection conn = null;
        try {
            URI dbUri = new URI(ConnUrl);
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
            return DriverManager.getConnection(dbUrl, username, password);
//            Class.forName(Driver);
//            conn = DriverManager.getConnection(ConnUrl, username, password);
//            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    // execute a sql query (e.g. SELECT) and return a ResultSet
    public ResultSet queryDB(String sql) {
        Connection conn = connectDB();
        ResultSet res = null;
        try {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                res = stmt.executeQuery(sql);
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    // execute a sql query (e.g. INSERT) to modify the database;
    // no return value needed
    public String modifyDB(String sql) {
        Connection conn = connectDB();
        String msg = "";
        try {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
                stmt.close();
                conn.close();
                msg = "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = "failure";
        }
        return msg;
    }
}