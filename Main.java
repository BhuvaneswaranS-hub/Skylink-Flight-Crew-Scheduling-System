import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import model.*;
import service.*;
import util.OperationCancelled;
import util.TimeZoneCatalog;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static final CrewService crews = new CrewService();
    private static final FlightService flights = new FlightService();
    private static final AssignmentService assignments = new AssignmentService();
    private static final LeaveService leaves = new LeaveService();

    public static void main(String[] args) {
        if (args.length == 0 || !"--console".equals(args[0])) {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                javax.swing.SwingUtilities.invokeLater(() -> new DashboardWindow().setVisible(true));
                return;
            }
        }
        runConsole();
    }

    private static void runConsole() {
        while (true) {
            System.out.println("\n========================================\n       SKYLINK-FLIGHT CREW SCHEDULING SYSTEM\n========================================");
            System.out.println("1. Crew Management\n2. Flight Management\n3. Crew Assignment\n4. Leave Management\n5. Reports\n6. Exit");
            try {
                switch (integer("Enter choice: ")) {
                    case 1: crewMenu(); break;
                    case 2: flightMenu(); break;
                    case 3: assignmentMenu(); break;
                    case 4: leaveMenu(); break;
                    case 5: reportMenu(); break;
                    case 6: System.out.println("Goodbye."); return;
                    default: System.out.println("Choose a menu option from the list.");
                }
            } catch (EndOfInput e) { System.out.println("Goodbye."); return; }
            catch (OperationCancelled e) { System.out.println(e.getMessage()); }
        }
    }

    private static void crewMenu() {
        menu("CREW MANAGEMENT", "1. Add Crew\n2. View All Crew\n3. Search Crew by ID\n4. Update Crew\n5. Delete Crew\n6. Back", 6, c -> {
            switch (c) {
                case 1: crewAdd(); break;
                case 2: action(() -> printCrews(crews.getAllCrew())); break;
                case 3: action(() -> printCrew(crews.getCrewById(positive("Crew ID: ")))); break;
                case 4: crewUpdate(); break;
                case 5: action(() -> { int id = positive("Crew ID: "); if (confirm("Are you sure you want to delete this crew member? (Y/N): ")) deleted(crews.deleteCrew(id), "Crew member not found."); }); break;
            }
        });
    }

    private static void crewAdd() {
        CrewMember c = crewInput();
        action(() -> System.out.println(crews.addCrew(c) ? "Crew member added successfully." : "Crew member was not added."));
    }

    private static void crewUpdate() {
        int id = positive("Crew ID: ");
        action(() -> {
            CrewMember c = crews.getCrewById(id);
            if (c == null) {
                System.out.println("Crew member not found.");
                return;
            }
            System.out.println("Updating Crew ID: " + id + " (" + c.getName() + " - " + c.getCrewType() + ")");
            CrewMember n = crewInput();
            n.setCrewId(id);
            String avail = optional("Availability [Available / Unavailable / On Leave] (Press Enter to keep '" + c.getAvailability() + "'): ");
            if (avail.trim().isEmpty()) {
                n.setAvailability(c.getAvailability());
            } else {
                n.setAvailability(avail.trim());
            }
            System.out.println(crews.updateCrew(n) ? "Crew member updated successfully." : "Crew member not found.");
        });
    }

    private static CrewMember crewInput() {
        String name = required("Name: ");
        String gender = optional("Gender (optional): ");
        String role;
        do {
            role = required("Role (Pilot / Co-Pilot / Cabin Crew): ");
            if (role.equalsIgnoreCase("pilot") || role.equalsIgnoreCase("co-pilot") || role.equalsIgnoreCase("copilot") || role.equalsIgnoreCase("cabin crew")) {
                break;
            }
            System.out.println("Invalid role. Please enter Pilot, Co-Pilot, or Cabin Crew.");
        } while (true);

        role = role.equalsIgnoreCase("pilot") ? "Pilot" : (role.equalsIgnoreCase("cabin crew") ? "Cabin Crew" : "Co-Pilot");
        CrewMember c = role.equals("Pilot") ? new Pilot() : role.equals("Co-Pilot") ? new CoPilot() : new CabinCrew();
        c.setName(name);
        c.setGender(gender.trim().isEmpty() ? null : gender.trim());
        c.setRole(role);
        c.setPhone(required("Phone: "));
        c.setEmail(required("Email: "));
        c.setAvailability("Available");
        return c;
    }

    private static void flightMenu() {
        menu("FLIGHT MANAGEMENT", "1. Add Flight\n2. View All Flights\n3. Search Flight by ID\n4. Update Flight\n5. Delete Flight\n6. Back", 6, c -> {
            switch (c) {
                case 1: flightAdd(); break;
                case 2: action(() -> printFlights(flights.listFlights())); break;
                case 3: action(() -> printFlight(flights.searchFlight(positive("Flight ID: ")))); break;
                case 4: flightUpdate(); break;
                case 5: action(() -> { int id = positive("Flight ID: "); if (confirm("Are you sure you want to delete this flight? (Y/N): ")) deleted(flights.deleteFlight(id), "Flight not found."); }); break;
            }
        });
    }

    private static void flightAdd() {
        Flight f = flightInput();
        action(() -> System.out.println(flights.addFlight(f) ? "Flight added successfully." : "Flight was not added."));
    }

    private static void flightUpdate() {
        int id = positive("Flight ID: ");
        action(() -> {
            Flight existing = flights.searchFlight(id);
            if (existing == null) {
                System.out.println("Flight not found.");
                return;
            }
            System.out.println("Updating Flight ID: " + id + " (" + existing.getFlightNumber() + ")");
            Flight f = flightInput();
            f.setFlightId(id);
            System.out.println(flights.updateFlight(f) ? "Flight updated successfully." : "Flight not found.");
        });
    }

    private static Flight flightInput() {
        Flight f = new Flight();
        f.setFlightNumber(required("Flight number: "));
        f.setSource(required("Source: "));
        f.setDestination(required("Destination: "));
        f.setSourceZoneId(zoneId("Source IANA time zone (e.g. Asia/Kolkata): "));
        f.setDestinationZoneId(zoneId("Destination IANA time zone (e.g. Europe/London): "));
        while (true) {
            LocalDateTime dep = dateTime("Departure (yyyy-MM-dd HH:mm): ");
            if (dep.isBefore(LocalDateTime.now())) {
                System.out.println("Invalid departure: Date and time cannot be in the past. Please enter a current or future date/time.");
                continue;
            }
            f.setDepartureTime(dep);
            break;
        }
        while (true) {
            LocalDateTime arr = dateTime("Arrival (yyyy-MM-dd HH:mm): ");
            if (arr.isBefore(LocalDateTime.now())) {
                System.out.println("Invalid arrival: Date and time cannot be in the past.");
                continue;
            }
            if (!arr.isAfter(f.getDepartureTime())) {
                System.out.println("Invalid arrival: Arrival time must be later than departure time.");
                continue;
            }
            f.setArrivalTime(arr);
            break;
        }
        return f;
    }

    private static void assignmentMenu() {
        menu("CREW ASSIGNMENT", "1. Assign Crew to Flight\n2. View All Assignments\n3. Search Assignment by ID\n4. Update Assignment\n5. Delete Assignment\n6. Back", 6, c -> {
            switch (c) {
                case 1: assignmentAdd(); break;
                case 2: action(() -> printTextRows("ASSIGNMENTS", "Assignment ID | Crew Member (Role) | Flight | Route | Date", assignments.joinedAssignments())); break;
                case 3: action(() -> printAssignment(assignments.assignmentById(positive("Assignment ID: ")))); break;
                case 4: assignmentUpdate(); break;
                case 5: action(() -> { int id = positive("Assignment ID: "); if (confirm("Are you sure you want to delete this assignment? (Y/N): ")) deleted(assignments.removeAssignment(id), "Assignment not found."); }); break;
            }
        });
    }

    private static void assignmentAdd() {
        int crew = positive("Crew ID: ");
        int flight = positive("Flight ID: ");
        LocalDate date = localDate("Assignment date (yyyy-MM-dd): ");
        action(() -> System.out.println(assignments.assignCrew(new Assignment(0, crew, flight, date)) ? "Assignment created successfully." : "Assignment was not created."));
    }

    private static void assignmentUpdate() {
        int id = positive("Assignment ID: ");
        action(() -> {
            Assignment existing = assignments.assignmentById(id);
            if (existing == null) {
                System.out.println("Assignment not found.");
                return;
            }
            System.out.println("Current details -> Crew ID: " + existing.getCrewId() + ", Flight ID: " + existing.getFlightId() + ", Date: " + existing.getAssignmentDate());
            int crew = positive("New Crew ID: ");
            int flight = positive("New Flight ID: ");
            LocalDate date = localDate("New Assignment date (yyyy-MM-dd): ");
            Assignment a = new Assignment(id, crew, flight, date);
            System.out.println(assignments.updateAssignment(a) ? "Assignment updated successfully." : "Assignment not found.");
        });
    }

    private static void leaveMenu() {
        menu("LEAVE MANAGEMENT", "1. Apply Leave\n2. View All Leave Requests\n3. Search Leave by ID\n4. Update Leave Status\n5. Update Leave Details\n6. Delete Leave\n7. Back", 7, c -> {
            switch (c) {
                case 1: leaveAdd(); break;
                case 2: action(() -> printLeaves(leaves.viewLeaves())); break;
                case 3: action(() -> printLeave(leaves.leaveById(positive("Leave ID: ")))); break;
                case 4: leaveUpdateStatus(); break;
                case 5: leaveUpdateDetails(); break;
                case 6: action(() -> { int id = positive("Leave ID: "); if (confirm("Are you sure you want to delete this leave request? (Y/N): ")) deleted(leaves.deleteLeave(id), "Leave request not found."); }); break;
            }
        });
    }

    private static void leaveAdd() {
        int id = positive("Crew ID: ");
        LocalDate from = localDate("From date (yyyy-MM-dd): ");
        LocalDate to = localDate("To date (yyyy-MM-dd): ");
        String reason = required("Reason: ");
        action(() -> System.out.println(leaves.applyLeave(new Leave(0, id, from, to, reason, "Pending")) ? "Leave application submitted successfully." : "Leave application was not submitted."));
    }

    private static void leaveUpdateStatus() {
        int id = positive("Leave ID: ");
        action(() -> {
            String s = required("Status (Pending / Approved / Rejected): ");
            System.out.println(leaves.updateLeaveStatus(id, s) ? "Leave status updated." : "Leave request not found.");
        });
    }

    private static void leaveUpdateDetails() {
        int id = positive("Leave ID: ");
        action(() -> {
            Leave existing = leaves.leaveById(id);
            if (existing == null) {
                System.out.println("Leave request not found.");
                return;
            }
            System.out.println("Current details -> Crew ID: " + existing.getCrewId() + ", From: " + existing.getFromDate() + ", To: " + existing.getToDate() + ", Status: " + existing.getStatus() + ", Reason: " + existing.getReason());
            int crewId = positive("Crew ID: ");
            LocalDate from = localDate("From date (yyyy-MM-dd): ");
            LocalDate to = localDate("To date (yyyy-MM-dd): ");
            String reason = required("Reason: ");
            String status = optional("Status [Pending / Approved / Rejected] (Press Enter to keep '" + existing.getStatus() + "'): ");
            if (status.trim().isEmpty()) {
                status = existing.getStatus();
            }
            Leave updated = new Leave(id, crewId, from, to, reason, status.trim());
            System.out.println(leaves.updateLeave(updated) ? "Leave details updated successfully." : "Leave request not found.");
        });
    }

    private static void reportMenu() {
        menu("REPORTS", "1. View All Crew\n2. View All Flights\n3. View Flight Assignments\n4. View Approved Leaves\n5. View Crew Assigned to a Specific Flight\n6. Back", 6, c -> {
            switch (c) {
                case 1: action(() -> printCrews(crews.getAllCrew())); break;
                case 2: action(() -> printFlights(flights.listFlights())); break;
                case 3: action(() -> printTextRows("ASSIGNMENTS", "Assignment ID | Crew Member (Role) | Flight | Route | Date", assignments.joinedAssignments())); break;
                case 4: action(() -> printTextRows("APPROVED LEAVE REQUESTS", "Leave ID | Crew Member | Role | From | To | Reason", leaves.approvedLeaves())); break;
                case 5: action(() -> printCrewForFlight(assignments.crewDetailsForFlight(positive("Flight ID: ")))); break;
            }
        });
    }

    private static void printCrews(List<CrewMember> list) {
        if (list.isEmpty()) {
            System.out.println("No crew members found.");
            return;
        }
        System.out.printf("%-4s  %-18s  %-8s  %-12s  %-13s  %-22s  %-12s%n", "ID", "Name", "Gender", "Role", "Phone", "Email", "Availability");
        System.out.println("-".repeat(97));
        for (CrewMember c : list) {
            System.out.printf("%-4d  %-18s  %-8s  %-12s  %-13s  %-22s  %-12s%n",
                    c.getCrewId(), safe(c.getName()), safe(c.getGender()), c.getCrewType(),
                    safe(c.getPhone()), safe(c.getEmail()), safe(c.getAvailability()));
        }
    }

    private static void printCrew(CrewMember c) {
        if (c == null) {
            System.out.println("Crew member not found.");
            return;
        }
        System.out.println("\nCrew Member Details\n-------------------");
        System.out.println("Crew ID: " + c.getCrewId());
        System.out.println("Name: " + safe(c.getName()));
        System.out.println("Gender: " + safe(c.getGender()));
        System.out.println("Role: " + c.getCrewType());
        System.out.println("Phone: " + safe(c.getPhone()));
        System.out.println("Email: " + safe(c.getEmail()));
        System.out.println("Availability: " + safe(c.getAvailability()));
    }

    private static void printFlights(List<Flight> list) {
        if (list.isEmpty()) {
            System.out.println("No flights found.");
            return;
        }
        System.out.printf("%-4s  %-12s  %-16s  %-16s  %-18s  %-18s%n", "ID", "Flight No", "Source", "Destination", "Departure", "Arrival");
        System.out.println("-".repeat(92));
        for (Flight f : list) {
            System.out.printf("%-4d  %-12s  %-16s  %-16s  %-18s  %-18s%n",
                    f.getFlightId(), f.getFlightNumber(), f.getSource(), f.getDestination(),
                    f.getDepartureTime().toString().replace('T', ' '),
                    f.getArrivalTime().toString().replace('T', ' '));
        }
    }

    private static void printFlight(Flight f) {
        if (f == null) {
            System.out.println("Flight not found.");
            return;
        }
        System.out.println("\nFlight Details\n--------------");
        System.out.println("Flight ID: " + f.getFlightId());
        System.out.println("Flight Number: " + f.getFlightNumber());
        System.out.println("Source: " + f.getSource());
        System.out.println("Destination: " + f.getDestination());
        System.out.println("Departure: " + (f.getDepartureTime() == null ? "" : f.getDepartureTime().toString().replace('T', ' ')));
        System.out.println("Arrival: " + (f.getArrivalTime() == null ? "" : f.getArrivalTime().toString().replace('T', ' ')));
    }

    private static void printTextRows(String title, String heading, List<String> rows) {
        System.out.println("\n" + title);
        if (rows.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        System.out.println(heading);
        System.out.println("-".repeat(heading.length()));
        for (String row : rows) {
            System.out.println(row);
        }
    }

    private static void printAssignment(Assignment a) {
        if (a == null) {
            System.out.println("Assignment not found.");
            return;
        }
        System.out.println("\nAssignment Details\n------------------");
        System.out.println("Assignment ID: " + a.getAssignmentId());
        System.out.println("Crew ID: " + a.getCrewId());
        System.out.println("Flight ID: " + a.getFlightId());
        System.out.println("Assignment Date: " + a.getAssignmentDate());
    }

    private static void printLeaves(List<Leave> list) {
        if (list.isEmpty()) {
            System.out.println("No leave requests found.");
            return;
        }
        System.out.printf("%-8s  %-8s  %-12s  %-12s  %-10s  %s%n", "Leave ID", "Crew ID", "From", "To", "Status", "Reason");
        System.out.println("-".repeat(75));
        for (Leave l : list) {
            System.out.printf("%-8d  %-8d  %-12s  %-12s  %-10s  %s%n",
                    l.getLeaveId(), l.getCrewId(), l.getFromDate(), l.getToDate(), l.getStatus(), safe(l.getReason()));
        }
    }

    private static void printLeave(Leave l) {
        if (l == null) {
            System.out.println("Leave request not found.");
            return;
        }
        System.out.println("\nLeave Request Details\n---------------------");
        System.out.println("Leave ID: " + l.getLeaveId());
        System.out.println("Crew ID: " + l.getCrewId());
        System.out.println("From: " + l.getFromDate());
        System.out.println("To: " + l.getToDate());
        System.out.println("Reason: " + safe(l.getReason()));
        System.out.println("Status: " + l.getStatus());
    }

    private static void printCrewForFlight(List<String[]> list) {
        if (list.isEmpty()) {
            System.out.println("No crew assigned to this flight.");
            return;
        }
        System.out.printf("%-10s %-26s %-16s %-16s%n", "Crew ID", "Name", "Role", "Assigned Date");
        System.out.println("-".repeat(72));
        for (String[] row : list) {
            System.out.printf("%-10s %-26s %-16s %-16s%n", row[0], row[1], row[2], row[3]);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private interface Choice {
        void run(int c);
    }

    private static void menu(String title, String choices, int back, Choice action) {
        while (true) {
            System.out.println("\n========== " + title + " ==========\n" + choices);
            int c = integer("Enter choice: ");
            if (c == back) return;
            if (c < 1 || c >= back) {
                System.out.println("Invalid input.");
                continue;
            }
            try {
                action.run(c);
            } catch (EndOfInput e) {
                throw e;
            } catch (OperationCancelled e) {
                System.out.println(e.getMessage());
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private interface DbAction {
        void run() throws Exception;
    }

    private static void action(DbAction a) {
        try {
            a.run();
        } catch (EndOfInput e) {
            throw e;
        } catch (OperationCancelled e) {
            System.out.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("A record with that unique value already exists.");
            } else if (e.getErrorCode() == 1451 || e.getErrorCode() == 1452) {
                System.out.println("Operation blocked by a related database record. Check crew/flight references.");
            } else {
                System.out.println("Database operation failed: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Operation failed: " + e.getMessage());
        }
    }

    private static int integer(String p) {
        while (true) {
            System.out.print(p);
            String s = line();
            if (s.equals("0")) throw new OperationCancelled();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }

    private static int positive(String p) {
        while (true) {
            int n = integer(p);
            if (n > 0) return n;
            System.out.println("ID must be a positive number.");
        }
    }

    private static String required(String p) {
        while (true) {
            System.out.print(p);
            String s = line();
            if (s.equals("0")) throw new OperationCancelled();
            if (!s.isEmpty()) return s;
            System.out.println("This value cannot be empty.");
        }
    }

    private static String optional(String p) {
        System.out.print(p);
        String value = line();
        if (value.equals("0")) throw new OperationCancelled();
        return value;
    }

    private static LocalDate localDate(String p) {
        while (true) {
            try {
                return LocalDate.parse(required(p).trim());
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use yyyy-MM-dd.");
            }
        }
    }

    private static LocalDateTime dateTime(String p) {
        while (true) {
            try {
                return LocalDateTime.parse(required(p).trim().replace(' ', 'T'));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date/time. Use yyyy-MM-dd HH:mm.");
            }
        }
    }

    private static String zoneId(String prompt) {
        while (true) {
            String value = required(prompt);
            try { return TimeZoneCatalog.canonicalize(value); }
            catch (Exception e) { System.out.println("Invalid time zone. Enter India or a valid IANA ZoneId such as Europe/London."); }
        }
    }

    private static String line() {
        if (!in.hasNextLine()) throw new EndOfInput();
        return in.nextLine().trim();
    }

    private static final class EndOfInput extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private static boolean confirm(String prompt) {
        while (true) {
            String answer = required(prompt);
            if (answer.equalsIgnoreCase("Y") || answer.equalsIgnoreCase("YES")) return true;
            if (answer.equalsIgnoreCase("N") || answer.equalsIgnoreCase("NO")) {
                System.out.println("Operation cancelled successfully.");
                return false;
            }
            System.out.println("Please enter Y or N (or 0 to cancel).");
        }
    }

    private static void deleted(boolean ok, String missing) {
        System.out.println(ok ? "Record deleted successfully." : missing);
    }
}
