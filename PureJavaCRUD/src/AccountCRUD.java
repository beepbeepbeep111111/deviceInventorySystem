import java.sql.*;
import java.util.Scanner;

// No OOP: pure static methods, no model objects, data printed directly from ResultSet
public class AccountCRUD {

    // ── CREATE ACCOUNT (Admin Only) ───────────────────────────────────────────
    static void addAccount(Scanner sc) {
        System.out.println("\n== ADD NEW ACCOUNT ==");
        System.out.print("  Username : "); String username = sc.nextLine().trim();
        if (username.isEmpty()) { System.out.println("  Username required."); return; }

        // Check duplicate
        try {
            PreparedStatement chk = Database.connect()
                    .prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
            chk.setString(1, username);
            ResultSet rc = chk.executeQuery();
            rc.next();
            if (rc.getInt(1) > 0) { System.out.println("  Username already exists."); return; }
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage()); return;
        }

        System.out.print("  Password : "); String password = sc.nextLine().trim();
        if (password.length() < 6) { System.out.println("  Password must be at least 6 characters."); return; }

        System.out.print("  Role [1] User  [2] Admin : ");
        String role = sc.nextLine().trim().equals("2") ? "admin" : "user";

        String sql = "INSERT INTO users (username, password, role, created_by) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, Auth.hash(password));
            ps.setString(3, role);
            ps.setInt(4, Auth.currentId);
            ps.executeUpdate();
            AuditLog.write("ADD_ACCOUNT", "Created " + role + " account: " + username);
            System.out.println("  Account created: " + username + " [" + role + "]");
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── READ ALL ACCOUNTS ─────────────────────────────────────────────────────
    static void listAccounts() {
        System.out.println("\n== ALL ACCOUNTS ==");
        String sql = "SELECT id, username, role, created_at FROM users ORDER BY id";
        try {
            Statement st = Database.connect().createStatement();
            ResultSet rs = st.executeQuery(sql);
            System.out.println("+------+----------------------+----------+---------------------+");
            System.out.printf( "| %-4s | %-20s | %-8s | %-19s |%n","ID","Username","Role","Created At");
            System.out.println("+------+----------------------+----------+---------------------+");
            while (rs.next()) {
                System.out.printf("| %-4d | %-20s | %-8s | %-19s |%n",
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("created_at"));
            }
            System.out.println("+------+----------------------+----------+---------------------+");
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── DELETE ACCOUNT (Admin only, cannot delete admins or self) ────────────
    static void deleteAccount(Scanner sc) {
        System.out.println("\n== DELETE ACCOUNT ==");
        listAccounts();
        System.out.print("\n  Enter User ID to delete: ");
        int id = DeviceCRUD.parseInt(sc.nextLine().trim());
        if (id < 0) { System.out.println("  Invalid ID."); return; }
        if (id == Auth.currentId) { System.out.println("  Cannot delete your own account."); return; }

        // Check role
        try {
            PreparedStatement chk = Database.connect()
                    .prepareStatement("SELECT username, role FROM users WHERE id = ?");
            chk.setInt(1, id);
            ResultSet rc = chk.executeQuery();
            if (!rc.next()) { System.out.println("  User not found."); return; }
            if ("admin".equals(rc.getString("role"))) {
                System.out.println("  Cannot delete admin accounts."); return;
            }
            String targetName = rc.getString("username");
            System.out.print("  Type YES to confirm deleting '" + targetName + "': ");
            if (!"YES".equals(sc.nextLine().trim())) {
                System.out.println("  Cancelled."); return;
            }
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage()); return;
        }

        String sql = "DELETE FROM users WHERE id = ?";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            AuditLog.write("DELETE_ACCOUNT", "Deleted user ID: " + id);
            System.out.println("  Account deleted.");
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }



    // ── CHANGE PASSWORD ───────────────────────────────────────────────────────
    static void changePassword(Scanner sc, boolean isOwnPassword) {
        System.out.println("\n== CHANGE PASSWORD ==");
        int targetId = Auth.currentId;

        if (!isOwnPassword) {
            // Admin changing someone else's password
            listAccounts();
            System.out.print("\n  Enter User ID: ");
            targetId = DeviceCRUD.parseInt(sc.nextLine().trim());
            if (targetId < 0) { System.out.println("  Invalid ID."); return; }
        } else {
            // User changing own — verify current password first
            System.out.print("  Current Password : ");
            String cur = sc.nextLine().trim();
            if (!Auth.login(Auth.currentUsername, cur)) {
                System.out.println("  Incorrect current password."); return;
            }
        }

        System.out.print("  New Password     : "); String newPass = sc.nextLine().trim();
        if (newPass.length() < 6) { System.out.println("  Min 6 characters."); return; }
        System.out.print("  Confirm Password : "); String confirm = sc.nextLine().trim();
        if (!newPass.equals(confirm)) { System.out.println("  Passwords do not match."); return; }

        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(sql);
            ps.setString(1, Auth.hash(newPass));
            ps.setInt(2, targetId);
            ps.executeUpdate();
            AuditLog.write("CHANGE_PASSWORD", "Changed password for user ID: " + targetId);
            System.out.println("  Password changed successfully!");
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }

