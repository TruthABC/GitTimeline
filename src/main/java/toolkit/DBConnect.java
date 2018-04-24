package toolkit;

import java.sql.*;

/**
 * Data Base connector 第二代
 *  用于 package projecthandler
 */
public class DBConnect {
    private String url = "jdbc:mysql://localhost:3306/android_api?useSSL=false&serverTimezone=UTC";
    private String name = "com.mysql.cj.jdbc.Driver";
    private String user = "root";
    private String password = "root";

    private Connection conn = null;
    private Statement stmt;
    private PreparedStatement pst;
    private ResultSet rs;

    public DBConnect(String url, String user, String password) {
        this.url=url;
        this.user=user;
        this.password=password;
        try {
            Class.forName(name);//指定连接类型
            conn =  DriverManager.getConnection(url, user, password);//获取连接
            stmt =  conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PreparedStatement prepareStatement(String sql) {
        try {
            pst = conn.prepareStatement(sql);
            return pst;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void executeUpdate(String sql) {
        try{
            stmt.executeUpdate(sql);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void executeUpdate(PreparedStatement pst) {
        try{
            pst.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String sql) {
        try {
            rs = stmt.executeQuery(sql);
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet executeQuery(PreparedStatement pst) {
        try {
            rs = pst.executeQuery();
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
                rs = null;
            }
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
                stmt = null;
            }
            if (pst != null && !pst.isClosed()) {
                pst.close();
                pst = null;
            }
            if (conn != null && !conn.isClosed()) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
