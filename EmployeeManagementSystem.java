/*
 * ============================================================================
 *  EMPLOYEE MANAGEMENT SYSTEM - Implementation for the Software Testing HW
 * ============================================================================
 *
 *  This is the "implementation" part of the assignment (Task ii). It is the
 *  real, runnable system that your test-case report (TC-001, TC-002, TC-003)
 *  is testing.
 *
 *  FUNCTIONAL REQUIREMENTS identified from the scenario (Task i):
 *    FR-01: The system shall allow a user to add a new employee with fields:
 *           Employee ID, Name, Department, Salary.
 *    FR-02: The system shall NOT allow duplicate Employee IDs.
 *    FR-03: The system shall correctly calculate an employee's net pay
 *           (basic salary minus deductions), used for salary payment
 *           processing.
 *
 *  HOW THE PARTS OF THIS FILE MAP TO YOUR REPORT:
 *    - Employee            -> the data record described in "Input Specifications"
 *    - EmployeeRepository  -> stands in for the MySQL "employees" table
 *                              mentioned in TC-001/TC-002 (no real DB needed
 *                              to run this; it's an in-memory Map, but the
 *                              logic - reject duplicates, store records - is
 *                              the same behaviour you'd get from a database).
 *    - addEmployee(...)    -> directly implements the pseudocode you wrote
 *                              in TC-003 (the numbered if/else steps 1-8).
 *                              This is what your BLACK-BOX tests (TC-001,
 *                              TC-002) exercise from the outside.
 *    - PaymentService.calculateNetPay(...) -> the method your WHITE-BOX test
 *                              (TC-003) targets for branch coverage.
 *    - runAutomatedTests()  -> re-runs TC-001, TC-002 and TC-003 automatically
 *                              and prints PASS/FAIL, so you can prove the
 *                              results in your report without doing it by hand
 *                              every time.
 *    - main(...)            -> an interactive menu so YOU (the user) can add
 *                              employees, try duplicate IDs, and calculate net
 *                              pay live, the same way a real user of the
 *                              "Employee Management module" would.
 *
 *  HOW TO RUN (no external libraries needed - plain Java):
 *      javac EmployeeManagementSystem.java
 *      java EmployeeManagementSystem
 * ============================================================================
 */

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/* ============================================================================
 * Employee: a plain data holder matching the "Input Specifications" table
 * in your report (Employee ID, Name, Department, Salary).
 * ============================================================================
 */
class Employee {
    private final String id;
    private final String name;
    private final String department;
    private final double salary;

    public Employee(String id, String name, String department, double salary) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.salary = salary;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public double getSalary() { return salary; }

    @Override
    public String toString() {
        // Format matches how you'd show a row of the employee list on screen
        return String.format("ID: %-6s | Name: %-15s | Dept: %-10s | Salary: %.2f",
                id, name, department, salary);
    }
}

/* ============================================================================
 * EmployeeRepository: stands in for the "employees" table in MySQL that
 * TC-001 / TC-002 reference ("Database Check: ... present in the employees
 * table"). Using an in-memory Map means you don't need a real MySQL server
 * running just to try the system out - the add/duplicate-check behaviour is
 * identical to what a database would enforce.
 * ============================================================================
 */
class EmployeeRepository {
    // LinkedHashMap keeps insertion order, so "View All Employees" lists them
    // in the order they were added - closer to what a real UI would show.
    private final Map<String, Employee> employees = new LinkedHashMap<>();

    /** Mirrors pseudocode step 5: "if id already exists in DB -> return error" */
    public boolean exists(String id) {
        return employees.containsKey(id);
    }

    /** Mirrors pseudocode step 6: "insert record into employees table" */
    public boolean insert(Employee employee) {
        employees.put(employee.getId(), employee);
        return true; // in a real DB this could fail (step 8); here it always succeeds
    }

    public Map<String, Employee> getAll() {
        return employees;
    }

    public int count() {
        return employees.size();
    }
}

/* ============================================================================
 * PaymentService: implements FR-03 (salary payment processing).
 * calculateNetPay(...) is the exact method your white-box test TC-003 is
 * meant to reach 100% branch coverage on.
 * ============================================================================
 */
class PaymentService {

    /**
     * Calculates net pay = basicSalary - deductions.
     *
     * Branches (this is what TC-003's JUnit test class should exercise,
     * one test per branch, for full branch coverage):
     *   B1: basicSalary <= 0            -> invalid input, throws exception
     *   B2: deductions  <  0            -> invalid input, throws exception
     *   B3: deductions  >  basicSalary  -> net pay cannot go negative, floor at 0
     *   B4: deductions ==  basicSalary  -> net pay is exactly 0
     *   B5: deductions  <  basicSalary  -> normal case, simple subtraction
     */
    public double calculateNetPay(double basicSalary, double deductions) {
        if (basicSalary <= 0) {                       // Branch B1
            throw new IllegalArgumentException("Basic salary must be greater than zero.");
        }
        if (deductions < 0) {                          // Branch B2
            throw new IllegalArgumentException("Deductions cannot be negative.");
        }
        if (deductions > basicSalary) {                 // Branch B3
            return 0.0;
        } else if (deductions == basicSalary) {          // Branch B4
            return 0.0;
        } else {                                         // Branch B5
            return basicSalary - deductions;
        }
    }
}

/* ============================================================================
 * EmployeeManagementSystem: ties everything together.
 *   - addEmployee(...) is a line-by-line translation of the pseudocode you
 *     wrote under TC-003's "Test Items and Features" section.
 *   - runAutomatedTests() replays TC-001, TC-002 and TC-003 automatically.
 *   - main(...) gives you an interactive menu to use as a real user.
 * ============================================================================
 */
public class EmployeeManagementSystem {

    private final EmployeeRepository repository = new EmployeeRepository();
    private final PaymentService paymentService = new PaymentService();

    /**
     * Implements FR-01 and FR-02 following the pseudocode from your report:
     *   1. if id is null or empty         -> return error
     *   2. if name is null or empty       -> return error
     *   3. if dept is null or empty       -> return error
     *   4. if salary is null or not numeric -> return error
     *   5. if id already exists in DB     -> return error
     *   6. insert record into employees table
     *   7. if insert successful           -> return "Employee added successfully"
     *   8. else                           -> return "Database error"
     */
    public String addEmployee(String id, String name, String department, String salaryText) {
        // Step 1
        if (id == null || id.trim().isEmpty()) {
            return "Error: Employee ID is required.";
        }
        // Step 2
        if (name == null || name.trim().isEmpty()) {
            return "Error: Name is required.";
        }
        // Step 3
        if (department == null || department.trim().isEmpty()) {
            return "Error: Department is required.";
        }
        // Step 4 - salary must be present AND numeric
        double salary;
        try {
            salary = Double.parseDouble(salaryText);
        } catch (NumberFormatException | NullPointerException e) {
            return "Error: Salary must be numeric.";
        }
        // Step 5 - this is exactly what FR-02 / TC-002 tests
        if (repository.exists(id)) {
            return "Employee ID already exists. Please use a unique ID.";
        }
        // Step 6
        boolean inserted = repository.insert(new Employee(id, name, department, salary));
        // Steps 7 / 8
        return inserted ? "Employee added successfully" : "Database error";
    }

    public EmployeeRepository getRepository() {
        return repository;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    /* ========================================================================
     * AUTOMATED TEST SUITE
     * Re-runs the exact scenarios from your test-case report and prints
     * PASS/FAIL, so you have a repeatable "black-box + white-box" check you
     * can run any time (menu option 4) instead of testing by hand.
     * ======================================================================== */
    public void runAutomatedTests() {
        int passCount = 0;
        int totalCount = 0;

        System.out.println("\n=================== AUTOMATED TEST SUITE ===================");

        // ---- TC-001 (Black-box): Add a valid new employee -------------------
        totalCount++;
        String tc001Result = addEmployee("101", "John Doe", "IT", "50000");
        boolean tc001Pass = tc001Result.equals("Employee added successfully")
                && repository.exists("101");
        System.out.println("TC-001 (Add valid employee): " + (tc001Pass ? "PASS" : "FAIL")
                + " -> " + tc001Result);
        if (tc001Pass) passCount++;

        // ---- TC-002 (Black-box): Reject duplicate Employee ID ---------------
        totalCount++;
        String tc002Result = addEmployee("101", "Jane Smith", "HR", "45000");
        boolean tc002Pass = tc002Result.equals("Employee ID already exists. Please use a unique ID.")
                && repository.count() == 1; // no new record should have been added
        System.out.println("TC-002 (Reject duplicate ID): " + (tc002Pass ? "PASS" : "FAIL")
                + " -> " + tc002Result);
        if (tc002Pass) passCount++;

        // ---- TC-003 (White-box): branch coverage on calculateNetPay ---------
        // One assertion per branch identified in PaymentService's comments.
        totalCount++;
        boolean tc003Pass = true;
        StringBuilder tc003Detail = new StringBuilder();

        // Branch B5: normal case -> 50000 - 5000 = 45000
        double normalResult = paymentService.calculateNetPay(50000, 5000);
        if (normalResult != 45000.0) {
            tc003Pass = false;
            tc003Detail.append("[normal case failed] ");
        }

        // Branch B4: deductions == basicSalary -> 0
        double equalResult = paymentService.calculateNetPay(30000, 30000);
        if (equalResult != 0.0) {
            tc003Pass = false;
            tc003Detail.append("[equal-deductions branch failed] ");
        }

        // Branch B3: deductions > basicSalary -> floored at 0
        double overResult = paymentService.calculateNetPay(20000, 25000);
        if (overResult != 0.0) {
            tc003Pass = false;
            tc003Detail.append("[over-deduction branch failed] ");
        }

        // Branch B1: basicSalary <= 0 -> must throw
        try {
            paymentService.calculateNetPay(0, 1000);
            tc003Pass = false; // should never reach here
            tc003Detail.append("[B1 did not throw] ");
        } catch (IllegalArgumentException expected) {
            // correct behaviour
        }

        // Branch B2: deductions < 0 -> must throw
        try {
            paymentService.calculateNetPay(10000, -500);
            tc003Pass = false; // should never reach here
            tc003Detail.append("[B2 did not throw] ");
        } catch (IllegalArgumentException expected) {
            // correct behaviour
        }

        System.out.println("TC-003 (White-box branch coverage on calculateNetPay): "
                + (tc003Pass ? "PASS" : "FAIL") + " " + tc003Detail);
        if (tc003Pass) passCount++;

        System.out.println("==============================================================");
        System.out.println("RESULT: " + passCount + " / " + totalCount + " test cases passed.");
        System.out.println("==============================================================\n");
    }

    /* ========================================================================
     * INTERACTIVE MENU - lets you act as the real user: add employees,
     * try to add a duplicate, view the employee list, calculate net pay,
     * or run the automated test suite above.
     * ======================================================================== */
    public static void main(String[] args) {
        EmployeeManagementSystem system = new EmployeeManagementSystem();
        Scanner scanner = new Scanner(System.in);

        // Pre-load nothing on purpose - you decide what to add, just like a
        // real tester filling in the "Add Employee" form from your report.

        boolean running = true;
        while (running) {
            System.out.println("\n===== EMPLOYEE MANAGEMENT SYSTEM =====");
            System.out.println("1. Add New Employee");
            System.out.println("2. View All Employees");
            System.out.println("3. Calculate Net Pay");
            System.out.println("4. Run Automated Test Suite (TC-001, TC-002, TC-003)");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": {
                    System.out.print("Employee ID: ");
                    String id = scanner.nextLine();
                    System.out.print("Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Department: ");
                    String dept = scanner.nextLine();
                    System.out.print("Salary: ");
                    String salary = scanner.nextLine();

                    String result = system.addEmployee(id, name, dept, salary);
                    System.out.println(">> " + result);
                    break;
                }
                case "2": {
                    if (system.getRepository().count() == 0) {
                        System.out.println(">> No employees yet.");
                    } else {
                        System.out.println(">> Employee list:");
                        for (Employee e : system.getRepository().getAll().values()) {
                            System.out.println("   " + e);
                        }
                    }
                    break;
                }
                case "3": {
                    try {
                        System.out.print("Basic salary: ");
                        double basic = Double.parseDouble(scanner.nextLine());
                        System.out.print("Deductions: ");
                        double deductions = Double.parseDouble(scanner.nextLine());
                        double netPay = system.getPaymentService().calculateNetPay(basic, deductions);
                        System.out.println(">> Net Pay: " + netPay);
                    } catch (NumberFormatException e) {
                        System.out.println(">> Error: please enter numeric values.");
                    } catch (IllegalArgumentException e) {
                        System.out.println(">> Error: " + e.getMessage());
                    }
                    break;
                }
                case "4":
                    system.runAutomatedTests();
                    break;
                case "5":
                    running = false;
                    System.out.println("Goodbye.");
                    break;
                default:
                    System.out.println(">> Invalid option, please choose 1-5.");
            }
        }
        scanner.close();
    }
}
