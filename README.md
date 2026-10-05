# employee-management-system

<div align="center">

# 💼 Employee Management System

### A small Java console app for employee records, payroll, and reporting — built as a software-testing case study.

[![Language](https://img.shields.io/badge/Language-Java%208%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Tests](https://img.shields.io/badge/Tests-19%2F19%20Passing-2ea44f?style=for-the-badge&logo=checkmarx&logoColor=white)](#-test-results)
[![Coverage](https://img.shields.io/badge/Branch%20Coverage-100%25-4c1?style=for-the-badge&logo=codecov&logoColor=white)](#-white-box-structural-tests)
[![License](https://img.shields.io/badge/License-MIT-8A2BE2?style=for-the-badge)](#-license)

</div>

---

## ✨ What this is

A single, dependency-free Java class that models the core of an **HR + payroll system**:

| Capability | Requirement | Emoji |
|---|---|:---:|
| Register an employee (ID, name, department, salary) | `FR1` | 🆕 |
| Reject bad or duplicate registrations | `FR2` | 🚫 |
| Remove an employee by ID | `FR3` | 🗑️ |
| Calculate net pay — overtime, department bonus, tax brackets | `FR4` | 💰 |
| Reject invalid payment requests | `FR5` | ⚠️ |
| Generate a payroll report (totals, average, top earner) | `FR6` | 📊 |
| Report headcount | `FR7` | 👥 |

No database, no frameworks, no build tool — just `javac` and `java`.

---

## 🚀 Quick start

```bash
# Compile
javac EmployeeManagementSystem.java

# Run
java EmployeeManagementSystem
```

You'll land on an interactive menu:

```
===== EMPLOYEE MANAGEMENT SYSTEM =====
1. Add Employee
2. Remove Employee
3. Process Payment
4. Generate Payroll Report
5. Show Employee Count
6. Run Automated Test Suite (BB-01..12, WB-01..07)
7. Exit
Choose an option:
```

Option **6** replays the entire test suite below and prints a live `PASS`/`FAIL` for every case — no separate test runner needed.

---

## 🧮 How a payment is calculated

```
hourlyRate   = baseSalary / 160                     # standard monthly hours
regularPay   = hourlyRate × hours
overtimePay  = hourlyRate × 1.5 × overtimeHours      # time-and-a-half
grossPay     = regularPay + overtimePay

if department == "Management":
    grossPay *= 1.10                                 # 10% management bonus

netPay = grossPay × 0.75   if grossPay >= 5000        # 25% tax bracket
       = grossPay × 0.85   otherwise                  # 15% tax bracket
```

<details>
<summary><strong>📐 Worked example — why 6000 → 4950.00</strong></summary>

<br>

A **Management** employee on a base salary of **6000**, working 160 standard hours with no overtime:

1. `hourlyRate = 6000 / 160 = 37.5`
2. `regularPay = 37.5 × 160 = 6000`
3. `overtimePay = 0`
4. `grossPay = 6000`
5. Management bonus → `6000 × 1.10 = 6600`
6. `6600 ≥ 5000` → 25% tax → `6600 × 0.75 = 4950.00` ✅

</details>

---

## 🧪 Test strategy

Testing follows the classic two-lens approach:

- **🔲 Black-box** — exercises the public API (`addEmployee`, `removeEmployee`, `processPayment`, `generatePayrollReport`) using equivalence partitioning and boundary values, with no knowledge of the code inside.
- **🔳 White-box** — targets `processPayment(...)` directly, the single most decision-heavy method, aiming for **100% branch coverage** of its `if / else-if / else` logic.

### ✅ Black-box (functional) tests

| ID | Scenario | Requirement | Result |
|---|---|:---:|:---:|
| BB-01 | Add employee with valid data | FR1 | 🟢 Pass |
| BB-02 | Reject duplicate Employee ID | FR2 | 🟢 Pass |
| BB-03 | Reject empty Employee ID | FR2 | 🟢 Pass |
| BB-04 | Reject negative base salary | FR2 | 🟢 Pass |
| BB-05 | Remove an existing employee | FR3 | 🟢 Pass |
| BB-06 | Remove a non-existent ID | FR3 | 🟢 Pass |
| BB-07 | Pay a non-management employee below $5000 | FR4 | 🟢 Pass |
| BB-08 | Pay a management employee at/above $5000 | FR4 | 🟢 Pass |
| BB-09 | Reject negative hours | FR5 | 🟢 Pass |
| BB-10 | Reject payment for unknown ID | FR5 | 🟢 Pass |
| BB-11 | Generate report with employees | FR6 | 🟢 Pass |
| BB-12 | Generate report with no employees | FR6 | 🟢 Pass |

### 🌿 White-box (structural) tests

| ID | Branch exercised in `processPayment(...)` | Result |
|---|---|:---:|
| WB-01 | `hours < 0` → exception | 🟢 Pass |
| WB-02 | `overtimeHours < 0` → exception | 🟢 Pass |
| WB-03 | unregistered ID → exception | 🟢 Pass |
| WB-04 | `department == "Management"` → bonus applied | 🟢 Pass |
| WB-05 | non-Management → bonus skipped | 🟢 Pass |
| WB-06 | `grossPay >= 5000` → 25% tax | 🟢 Pass |
| WB-07 | `grossPay < 5000` → 15% tax | 🟢 Pass |

> Full details — setup, environment, inputs, expected vs. actual results — are in [`TESTING.md`](./TESTING.md).

---

## 📁 Project layout

```
.
├── EmployeeManagementSystem.java   # Employee, payroll logic, menu, automated test suite
├── TESTING.md                      # Full test-case report (black-box + white-box)
└── README.md                       # You are here
```

---

## 🧭 Design notes

- **In-memory store.** `employees` is a `LinkedHashMap<String, Employee>` — it behaves like a database table (unique keys, lookups, inserts) without needing one, which keeps the whole project runnable with zero setup.
- **Two kinds of "no"**: a *duplicate ID* is an expected, recoverable outcome (`boolean false`); a *malformed input* (empty ID/name, negative salary, negative hours) is a programming error (`IllegalArgumentException`); an *unknown ID at payment time* is `NoSuchElementException`. Keeping these distinct made the black-box/white-box split in `TESTING.md` much cleaner to design.
- **`lastNetPay` is nullable** on purpose — an employee who has never been paid shouldn't silently show `0.00` in a report as if they'd been processed and earned nothing.

---

## 📜 License

MIT — use it, fork it, learn from it.

<div align="center">

Made with ☕ and a lot of `if` statements.

</div>
