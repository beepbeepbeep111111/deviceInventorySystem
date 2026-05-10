import java.sql.*;
import java.util.Scanner;

// No OOP: no model class, no encapsulation — all data read/printed directly from ResultSet
public class DeviceCRUD {

    // ── CREATE ────────────────────────────────────────────────────────────────
    static void addDevice(Scanner sc) {
        System.out.println("\n== ADD NEW DEVICE ==");
        System.out.print("  Device Name   : "); String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("  Name is required."); return; }
        System.out.print("  Device Type   : "); String type  = sc.nextLine().trim();
        System.out.print("  Brand         : "); String brand = sc.nextLine().trim();
        System.out.print("  Model         : "); String model = sc.nextLine().trim();
        System.out.print("  Serial Number : "); String serial = sc.nextLine().trim();
        System.out.print("  Status [1]Active [2]Inactive [3]Under Repair [4]Retired : ");
        String status = parseStatus(sc.nextLine().trim(), "Active");
        System.out.print("  Location      : "); String location    = sc.nextLine().trim();
        System.out.print("  Assigned To   : "); String assignedTo  = sc.nextLine().trim();
        System.out.print("  Purchase Date : "); String purchDate   = sc.nextLine().trim();
        System.out.print("  Notes         : "); String notes       = sc.nextLine().trim();

        String sql = "INSERT INTO devices " +
                "(device_name,device_type,brand,model,serial_number,status," +
                "location,assigned_to,purchase_date,notes,created_by,updated_by) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(sql);
            ps.setString(1, name);      ps.setString(2, type);
            ps.setString(3, brand);     ps.setString(4, model);
            ps.setString(5, serial);    ps.setString(6, status);
            ps.setString(7, location);  ps.setString(8, assignedTo);
            ps.setString(9, purchDate); ps.setString(10, notes);
            ps.setInt(11, Auth.currentId);
            ps.setInt(12, Auth.currentId);
            ps.executeUpdate();
            AuditLog.write("ADD_DEVICE", "Added: " + name + " SN:" + serial);
            System.out.println("  Device added successfully!");
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate"))
                System.out.println("  Error: Serial number already exists.");
            else
                System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── READ ALL ─────────────────────────────────────────────────────────────
    static void listDevices() {
        System.out.println("\n== ALL DEVICES ==");
        String sql = "SELECT id,device_name,brand,model,serial_number,status,location FROM devices ORDER BY id";
        try {
            Statement st  = Database.connect().createStatement();
            ResultSet rs  = st.executeQuery(sql);
            int count = 0;
            System.out.println("+------+------------------------+--------------+--------------+---------------------+---------------+--------------+");
            System.out.printf( "| %-4s | %-22s | %-12s | %-12s | %-19s | %-13s | %-12s |%n",
                    "ID","Device Name","Brand","Model","Serial Number","Status","Location");
            System.out.println("+------+------------------------+--------------+--------------+---------------------+---------------+--------------+");
            while (rs.next()) {
                count++;
                System.out.printf("| %-4d | %-22s | %-12s | %-12s | %-19s | %-13s | %-12s |%n",
                        rs.getInt("id"),
                        trunc(rs.getString("device_name"),  22),
                        trunc(rs.getString("brand"),        12),
                        trunc(rs.getString("model"),        12),
                        trunc(rs.getString("serial_number"),19),
                        trunc(rs.getString("status"),       13),
                        trunc(rs.getString("location"),     12));
            }
            System.out.println("+------+------------------------+--------------+--------------+---------------------+---------------+--------------+");
            System.out.println("  Total: " + count + " device(s)");
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── READ ONE (full detail) ────────────────────────────────────────────────
    static void viewDevice(Scanner sc) {
        System.out.println("\n== VIEW DEVICE DETAILS ==");
        System.out.print("  Enter Device ID: ");
        int id = parseInt(sc.nextLine().trim());
        if (id < 0) { System.out.println("  Invalid ID."); return; }

        String sql = "SELECT * FROM devices WHERE id = ?";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { System.out.println("  Device not found."); return; }
            System.out.println("  +------------------------------------------+");
            System.out.printf( "  | ID            : %-26d|%n", rs.getInt("id"));
            System.out.printf( "  | Device Name   : %-26s|%n", rs.getString("device_name"));
            System.out.printf( "  | Type          : %-26s|%n", rs.getString("device_type"));
            System.out.printf( "  | Brand         : %-26s|%n", rs.getString("brand"));
            System.out.printf( "  | Model         : %-26s|%n", rs.getString("model"));
            System.out.printf( "  | Serial Number : %-26s|%n", rs.getString("serial_number"));
            System.out.printf( "  | Status        : %-26s|%n", rs.getString("status"));
            System.out.printf( "  | Location      : %-26s|%n", rs.getString("location"));
            System.out.printf( "  | Assigned To   : %-26s|%n", rs.getString("assigned_to"));
            System.out.printf( "  | Purchase Date : %-26s|%n", rs.getString("purchase_date"));
            System.out.printf( "  | Notes         : %-26s|%n", rs.getString("notes"));
            System.out.printf( "  | Created At    : %-26s|%n", rs.getString("created_at"));
            System.out.printf( "  | Updated At    : %-26s|%n", rs.getString("updated_at"));
            System.out.println("  +------------------------------------------+");
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    static void updateDevice(Scanner sc) {
        System.out.println("\n== EDIT DEVICE ==");
        System.out.print("  Enter Device ID to edit: ");
        int id = parseInt(sc.nextLine().trim());
        if (id < 0) { System.out.println("  Invalid ID."); return; }

        // Load current values first
        String sql = "SELECT * FROM devices WHERE id = ?";
        String curName="",curType="",curBrand="",curModel="",curSerial="",
               curStatus="",curLocation="",curAssigned="",curDate="",curNotes="";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { System.out.println("  Device not found."); return; }
            curName     = str(rs.getString("device_name"));
            curType     = str(rs.getString("device_type"));
            curBrand    = str(rs.getString("brand"));
            curModel    = str(rs.getString("model"));
            curSerial   = str(rs.getString("serial_number"));
            curStatus   = str(rs.getString("status"));
            curLocation = str(rs.getString("location"));
            curAssigned = str(rs.getString("assigned_to"));
            curDate     = str(rs.getString("purchase_date"));
            curNotes    = str(rs.getString("notes"));
        } catch (SQLException e) {
            System.out.println("  Error loading device: " + e.getMessage()); return;
        }

        System.out.println("  (Press Enter to keep current value)\n");
        System.out.printf("  Device Name   [%s]: ", curName);    String name  = defVal(sc.nextLine().trim(), curName);
        System.out.printf("  Device Type   [%s]: ", curType);    String type  = defVal(sc.nextLine().trim(), curType);
        System.out.printf("  Brand         [%s]: ", curBrand);   String brand = defVal(sc.nextLine().trim(), curBrand);
        System.out.printf("  Model         [%s]: ", curModel);   String model = defVal(sc.nextLine().trim(), curModel);
        System.out.printf("  Serial Number [%s]: ", curSerial);  String serial = defVal(sc.nextLine().trim(), curSerial);
        System.out.printf("  Status [1]Active [2]Inactive [3]Under Repair [4]Retired [Enter=keep %s]: ", curStatus);
        String status = parseStatus(sc.nextLine().trim(), curStatus);
        System.out.printf("  Location      [%s]: ", curLocation); String location  = defVal(sc.nextLine().trim(), curLocation);
        System.out.printf("  Assigned To   [%s]: ", curAssigned); String assignedTo = defVal(sc.nextLine().trim(), curAssigned);
        System.out.printf("  Purchase Date [%s]: ", curDate);     String purchDate  = defVal(sc.nextLine().trim(), curDate);
        System.out.printf("  Notes         [%s]: ", curNotes);    String notes      = defVal(sc.nextLine().trim(), curNotes);

        String upd = "UPDATE devices SET device_name=?,device_type=?,brand=?,model=?," +
                     "serial_number=?,status=?,location=?,assigned_to=?,purchase_date=?," +
                     "notes=?,updated_by=? WHERE id=?";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(upd);
            ps.setString(1, name);       ps.setString(2, type);
            ps.setString(3, brand);      ps.setString(4, model);
            ps.setString(5, serial);     ps.setString(6, status);
            ps.setString(7, location);   ps.setString(8, assignedTo);
            ps.setString(9, purchDate);  ps.setString(10, notes);
            ps.setInt(11, Auth.currentId);
            ps.setInt(12, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                AuditLog.write("UPDATE_DEVICE", "Updated device ID: " + id);
                System.out.println("  Device updated successfully!");
            } else {
                System.out.println("  No device updated.");
            }
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    static void deleteDevice(Scanner sc) {
        System.out.println("\n== DELETE DEVICE ==");
        System.out.print("  Enter Device ID to delete: ");
        int id = parseInt(sc.nextLine().trim());
        if (id < 0) { System.out.println("  Invalid ID."); return; }

        // Show the record first
        String sel = "SELECT device_name, serial_number FROM devices WHERE id = ?";
        String dName = "", dSerial = "";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(sel);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { System.out.println("  Device not found."); return; }
            dName   = rs.getString("device_name");
            dSerial = rs.getString("serial_number");
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage()); return;
        }

        System.out.println("  Device: " + dName + " | Serial: " + dSerial);
        System.out.print("  Type YES to confirm deletion: ");
        if (!"YES".equals(sc.nextLine().trim())) {
            System.out.println("  Deletion cancelled."); return;
        }

        String del = "DELETE FROM devices WHERE id = ?";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(del);
            ps.setInt(1, id);
            ps.executeUpdate();
            AuditLog.write("DELETE_DEVICE", "Deleted device: " + dName + " SN:" + dSerial);
            System.out.println("  Device deleted.");
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── SEARCH ────────────────────────────────────────────────────────────────
    static void searchDevices(Scanner sc) {
        System.out.println("\n== SEARCH DEVICES ==");
        System.out.print("  Keyword: ");
        String kw = "%" + sc.nextLine().trim() + "%";
        String sql = "SELECT id,device_name,brand,model,serial_number,status,location " +
                     "FROM devices WHERE device_name LIKE ? OR brand LIKE ? OR model LIKE ? " +
                     "OR serial_number LIKE ? OR location LIKE ? OR assigned_to LIKE ? ORDER BY id";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(sql);
            for (int i = 1; i <= 6; i++) ps.setString(i, kw);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            System.out.println("+------+------------------------+--------------+--------------+---------------------+---------------+--------------+");
            System.out.printf( "| %-4s | %-22s | %-12s | %-12s | %-19s | %-13s | %-12s |%n",
                    "ID","Device Name","Brand","Model","Serial Number","Status","Location");
            System.out.println("+------+------------------------+--------------+--------------+---------------------+---------------+--------------+");
            while (rs.next()) {
                count++;
                System.out.printf("| %-4d | %-22s | %-12s | %-12s | %-19s | %-13s | %-12s |%n",
                        rs.getInt("id"),
                        trunc(rs.getString("device_name"),  22),
                        trunc(rs.getString("brand"),        12),
                        trunc(rs.getString("model"),        12),
                        trunc(rs.getString("serial_number"),19),
                        trunc(rs.getString("status"),       13),
                        trunc(rs.getString("location"),     12));
            }
            System.out.println("+------+------------------------+--------------+--------------+---------------------+---------------+--------------+");
            System.out.println("  Found: " + count + " result(s)");
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ── FILTER BY STATUS ──────────────────────────────────────────────────────
    static void filterByStatus(Scanner sc) {
        System.out.println("\n== FILTER BY STATUS ==");
        System.out.println("  [1] Active  [2] Inactive  [3] Under Repair  [4] Retired");
        System.out.print("  Choose: ");
        String status = parseStatus(sc.nextLine().trim(), null);
        if (status == null) { System.out.println("  Invalid option."); return; }
        String sql = "SELECT id,device_name,brand,model,serial_number,status,location " +
                     "FROM devices WHERE status = ? ORDER BY id";
        try {
            PreparedStatement ps = Database.connect().prepareStatement(sql);
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            System.out.println("\n  Status: " + status);
            System.out.println("+------+------------------------+--------------+--------------+---------------------+--------------+");
            System.out.printf( "| %-4s | %-22s | %-12s | %-12s | %-19s | %-12s |%n",
                    "ID","Device Name","Brand","Model","Serial Number","Location");
            System.out.println("+------+------------------------+--------------+--------------+---------------------+--------------+");
            while (rs.next()) {
                count++;
                System.out.printf("| %-4d | %-22s | %-12s | %-12s | %-19s | %-12s |%n",
                        rs.getInt("id"),
                        trunc(rs.getString("device_name"),  22),
                        trunc(rs.getString("brand"),        12),
                        trunc(rs.getString("model"),        12),
                        trunc(rs.getString("serial_number"),19),
                        trunc(rs.getString("location"),     12));
            }
            System.out.println("+------+------------------------+--------------+--------------+---------------------+--------------+");
            System.out.println("  Total: " + count + " device(s)");
        } catch (SQLException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    




    
