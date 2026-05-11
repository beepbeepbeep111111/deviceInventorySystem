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


    // Print last N audit log entries (admin only)
    static void showLog(int limit) {
        System.out.println("\n== AUDIT LOG (Last " + limit + " Actions) ==");
        String sql = "SELECT id, username, action, details, logged_at " +
                     "FROM audit_log ORDER BY logged_at DESC LIMIT ?";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            System.out.println("+------+------------------+------------------+--------------------------------+---------------------+");
            System.out.printf( "| %-4s | %-16s | %-16s | %-30s | %-19s |%n",
                    "ID","Username","Action","Details","Timestamp");
            System.out.println("+------+------------------+------------------+--------------------------------+---------------------+");
            while (rs.next()) {
                System.out.printf("| %-4d | %-16s | %-16s | %-30s | %-19s |%n",
                        rs.getInt("id"),
                        DeviceCRUD.trunc(rs.getString("username"), 16),
                        DeviceCRUD.trunc(rs.getString("action"),   16),
                        DeviceCRUD.trunc(rs.getString("details"),  30),
                        rs.getString("logged_at"));
            }
            System.out.println("+------+------------------+------------------+--------------------------------+---------------------+");
        } catch (SQLException e) {
            System.out.println("  Error reading log: " + e.getMessage());
        }
    }
}
  







    
}
