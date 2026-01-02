package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // ====== SỬA Ở ĐÂY nếu máy khác =========
    private static final String HOST = "127.0.0.1";
    private static final String PORT = "3306";
    private static final String DB_NAME = "mini_supermarket";
    private static final String USER = "root";
    private static final String PASS = "";
    // =======================================

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME +
            "?useUnicode=true&characterEncoding=utf8" +
            "&useSSL=false&allowPublicKeyRetrieval=true" +
            "&serverTimezone=Asia/Ho_Chi_Minh";

    static {
        try {
            // MySQL Connector/J 9+
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Không tìm thấy MySQL JDBC Driver. Hãy add mysql-connector-j vào project.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
