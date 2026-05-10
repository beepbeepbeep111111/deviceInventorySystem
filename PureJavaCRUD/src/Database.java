import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// No OOP: no constructor, no instance, only static members
public class Database {

    static final String URL  = "jdbc:mysql://localhost:3306/device_inventory" +
                               "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    static final String USER = "root";
    static final String PASS = "";          // XAMPP default: blank

    static Connection conn = null;

    static Connection connect() throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found. Place the .jar in /lib folder.");
        }
        return conn;
    }

    static void close() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            System.out.println("Warning: could not close connection - " + e.getMessage());
        }
    }
}
