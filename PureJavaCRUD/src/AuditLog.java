import java.sql.*;

// No OOP: pure static utility for recording all actions in the database
public class AuditLog {

    // Write a log entry to the audit_log table
    static void write(String action, String details) {
        String sql = "INSERT INTO audit_log (user_id, username, action, details) VALUES (?,?,?,?)";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(sql);
            ps.setInt(1, Auth.currentId);
            ps.setString(2, Auth.currentUsername);
            ps.setString(3, action);
            ps.setString(4, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Don't crash main flow for a log error
            System.out.println("[Audit Error] " + e.getMessage());
        }
    }

  







    
}
