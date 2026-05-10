import java.util.Scanner;

// No OOP: main class with only static methods — no objects instantiated for logic
public class Main {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        clearScreen();
        printBanner();

        // Test DB connection
        try {
            Database.connect();
            Auth.seedAdmin();
        } catch (Exception e) {
            System.out.println("\n  DATABASE CONNECTION FAILED!");
            System.out.println("  Make sure XAMPP MySQL is running.");
            System.out.println("  Error: " + e.getMessage());
            System.out.println("\n  Fix:");
            System.out.println("  1. Open XAMPP Control Panel -> Start MySQL");
            System.out.println("  2. Import schema.sql in phpMyAdmin");
            System.exit(1);
        }

        // Login loop
        boolean loggedIn = false;
        while (!loggedIn) {
            loggedIn = loginScreen();
        }

        // Route to correct menu
        if (Auth.isAdmin()) {
            adminMenu();
        } else {
            userMenu();
        }

        Database.close();
        System.out.println("\n  Goodbye, " + Auth.currentUsername + "!");
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────
    static boolean loginScreen() {
        System.out.println("\n+--------------------------------+");
        System.out.println("|            LOGIN               |");
        System.out.println("+--------------------------------+");
        System.out.print("  Username : "); String username = sc.nextLine().trim();
        System.out.print("  Password : "); String password = sc.nextLine().trim();

        if (Auth.login(username, password)) {
            AuditLog.write("LOGIN", "User logged in");
            System.out.println("\n  Welcome, " + Auth.currentUsername +
                    "! [" + Auth.currentRole.toUpperCase() + "]");
            pause(800);
            return true;
        } else {
            System.out.println("\n  Invalid username or password. Try again.");
            pause(1000);
            return false;
        }
    }

    // ── ADMIN MENU ────────────────────────────────────────────────────────────
    static void adminMenu() {
        String choice;
        do {
            clearScreen();
            System.out.println("+==========================================+");
            System.out.println("|    DEVICE INVENTORY  [ADMIN]            |");
            System.out.println("+==========================================+");
            System.out.println("|  -- DEVICE CRUD --                      |");
            System.out.println("|  [1] List All Devices                   |");
            System.out.println("|  [2] View Device Details                |");
            System.out.println("|  [3] Add Device                         |");
            System.out.println("|  [4] Edit Device                        |");
            System.out.println("|  [5] Delete Device                      |");
            System.out.println("|  [6] Search Devices                     |");
            System.out.println("|  [7] Filter by Status                   |");
            System.out.println("|  -- ACCOUNT MANAGEMENT (Admin Only) --  |");
            System.out.println("|  [8] List All Accounts                  |");
            System.out.println("|  [9] Add Account                        |");
            System.out.println("|  [10] Delete Account                    |");
            System.out.println("|  [11] Change Any Account Password       |");
            System.out.println("|  -- OTHER --                            |");
            System.out.println("|  [12] View Audit Log                    |");
            System.out.println("|  [13] Change My Password                |");
            System.out.println("|  [0]  Logout                            |");
            System.out.println("+==========================================+");
            System.out.print("  Logged in as: " + Auth.currentUsername + " | Choice: ");
            choice = sc.nextLine().trim();

            switch (choice) {
                case "1"  -> { listAndPause(); }
                case "2"  -> { DeviceCRUD.viewDevice(sc);    pause(0); promptEnter(); }
                case "3"  -> { DeviceCRUD.addDevice(sc);     promptEnter(); }
                case "4"  -> { DeviceCRUD.updateDevice(sc);  promptEnter(); }
                case "5"  -> { DeviceCRUD.deleteDevice(sc);  promptEnter(); }
                case "6"  -> { DeviceCRUD.searchDevices(sc); promptEnter(); }
                case "7"  -> { DeviceCRUD.filterByStatus(sc);promptEnter(); }
                case "8"  -> { AccountCRUD.listAccounts();   promptEnter(); }
                case "9"  -> { AccountCRUD.addAccount(sc);   promptEnter(); }
                case "10" -> { AccountCRUD.deleteAccount(sc);promptEnter(); }
                case "11" -> { AccountCRUD.changePassword(sc, false); promptEnter(); }
                case "12" -> { AuditLog.showLog(50);         promptEnter(); }
                case "13" -> { AccountCRUD.changePassword(sc, true);  promptEnter(); }
                case "0"  -> { AuditLog.write("LOGOUT","User logged out"); }
                default   -> { System.out.println("  Invalid option."); pause(700); }
            }
        } while (!"0".equals(choice));
    }

    // ── USER MENU ─────────────────────────────────────────────────────────────
    static void userMenu() {
        String choice;
        do {
            clearScreen();
            System.out.println("+==========================================+");
            System.out.println("|    DEVICE INVENTORY  [USER]             |");
            System.out.println("+==========================================+");
            System.out.println("|  [1] List All Devices                   |");
            System.out.println("|  [2] View Device Details                |");
            System.out.println("|  [3] Add Device                         |");
            System.out.println("|  [4] Edit Device                        |");
            System.out.println("|  [5] Search Devices                     |");
            System.out.println("|  [6] Filter by Status                   |");
            System.out.println("|  [7] Change My Password                 |");
            System.out.println("|  [0] Logout                             |");
            System.out.println("+==========================================+");
            System.out.print("  Logged in as: " + Auth.currentUsername + " | Choice: ");
            choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> { listAndPause(); }
                case "2" -> { DeviceCRUD.viewDevice(sc);    pause(0); promptEnter(); }
                case "3" -> { DeviceCRUD.addDevice(sc);     promptEnter(); }
                case "4" -> { DeviceCRUD.updateDevice(sc);  promptEnter(); }
                case "5" -> { DeviceCRUD.searchDevices(sc); promptEnter(); }
                case "6" -> { DeviceCRUD.filterByStatus(sc);promptEnter(); }
                case "7" -> { AccountCRUD.changePassword(sc, true); promptEnter(); }
                case "0" -> { AuditLog.write("LOGOUT","User logged out"); }
                default  -> { System.out.println("  Invalid option."); pause(700); }
            }
        } while (!"0".equals(choice));
    }

    // ── UTILITIES ─────────────────────────────────────────────────────────────
    static void listAndPause() {
        DeviceCRUD.listDevices();
        promptEnter();
    }

    static void promptEnter() {
        System.out.print("\n  Press Enter to continue...");
        sc.nextLine();
    }

    static void pause(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    static void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win"))
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            else { System.out.print("\033[H\033[2J"); System.out.flush(); }
        } catch (Exception ignored) { System.out.println("\n\n\n"); }
    }






    
}
