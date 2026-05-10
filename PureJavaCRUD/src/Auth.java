import java.security.MessageDigest;
import java.sql.*;

// No OOP: pure static utility methods only
public class Auth {

    // Currently logged-in user data stored as plain static variables
    static int    currentId       = -1;
    static String currentUsername = "";
    static String currentRole     = "";

    // SHA-256 hash using standard Java library
    static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(text.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 unavailable");
        }
    }

    // Returns true if login succeeds; sets current* variables
    static boolean login(String username, String password) {
        String sql = "SELECT id, username, role FROM users WHERE username = ? AND password = ?";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, hash(password));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentId       = rs.getInt("id");
                currentUsername = rs.getString("username");
                currentRole     = rs.getString("role");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return false;
    }

    static boolean isAdmin() {
        return "admin".equalsIgnoreCase(currentRole);
    }

    // Seed default admin if none exists
    static void seedAdmin() {
        try {
            Statement st = Database.connect().createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE role='admin'");
            rs.next();
            if (rs.getInt(1) == 0) {
                PreparedStatement ps = Database.connect()
                        .prepareStatement("INSERT INTO users (username,password,role) VALUES ('admin',?,'admin')");
                ps.setString(1, hash("Admin@123"));
                ps.executeUpdate();
                System.out.println("Default admin created → username: admin | password: Admin@123");
            }
        } catch (SQLException e) {
            System.out.println("Admin seed error: " + e.getMessage());
        }
    }
}
