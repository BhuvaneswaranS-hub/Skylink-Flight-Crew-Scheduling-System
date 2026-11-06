# ✈️ SkyLink – Flight Crew Scheduling System

SkyLink is a Java-based desktop application designed to simplify and automate airline crew scheduling and flight operations.

The application provides an interactive graphical user interface developed using **Java Swing** and integrates with a **MySQL database** through **JDBC** to efficiently handle crew records, flight details, crew assignments, and leave requests.

SkyLink aims to reduce manual scheduling errors, improve crew availability tracking, and ensure efficient flight crew allocation through automated validation and scheduling rules.

The application follows a layered architecture using **Model, DAO (Data Access Object), and Service** components to maintain clean, modular, and scalable code.

---

## 🚀 Features

### 👨‍✈️ 1. Crew Administration
- Add, view, update, search, and delete crew members.
- Support for three different crew roles:
  - Pilot
  - Co-Pilot
  - Cabin Crew
- Maintain crew profiles and contact information.
- Track crew availability.
- Identify crew members currently available or on approved leave.
- Validate crew information before saving records.

### 🛫 2. Flight Operations
- Add, view, update, search, and delete flights.
- Maintain flight numbers and route information.
- Store source and destination details.
- Manage departure and arrival dates and times.
- Support multiple international time zones.
- Validate flight schedules.
- Prevent invalid departure and arrival times.

### 📅 3. Crew Scheduling
- Assign eligible crew members to flights.
- View, search, update, and remove crew assignments.
- Check crew availability before assigning flights.
- Prevent duplicate crew assignments.
- Detect overlapping flight schedules.
- Prevent assignments during approved leave periods.
- Validate mandatory rest periods between consecutive flight duties.

### 🏖️ 4. Leave Administration
- Allow crew members to submit leave requests.
- View and search leave applications.
- Approve or reject leave requests.
- Update and delete leave records.
- Detect conflicting leave requests.
- Prevent approval when existing flight assignments conflict with the requested leave period.

### 📊 5. Dashboard and Reports
- Interactive Java Swing dashboard.
- View crew and flight information.
- Monitor flight assignments.
- Track pending leave requests.
- View crew assigned to a particular flight.
- Access operational information through a centralized interface.

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java | Core application development |
| Java Swing | Graphical User Interface |
| MySQL | Database |
| JDBC | Database connectivity |
| MySQL Connector/J | JDBC driver |
| Object-Oriented Programming | Application design |
| DAO Pattern | Database access abstraction |

---

## 📁 Project Structure

```text
SkyLink - Flight Crew Scheduling System/
│
├── dao/
│   ├── AssignmentDAO.java
│   ├── CrewDAO.java
│   ├── FlightDAO.java
│   └── LeaveDAO.java
│
├── model/
│   ├── Assignment.java
│   ├── CabinCrew.java
│   ├── CoPilot.java
│   ├── CrewMember.java
│   ├── Flight.java
│   ├── Leave.java
│   └── Pilot.java
│
├── service/
│   ├── AssignmentService.java
│   ├── CrewService.java
│   ├── FlightService.java
│   └── LeaveService.java
│
├── util/
│   ├── DatabaseConnection.java
│   ├── OperationCancelled.java
│   └── TimeZoneCatalog.java
│
├── lib/
│   └── mysql-connector-j-26.7.0.jar
│
├── Main.java
├── DashboardWindow.java
│
├── schema.sql
├── seed_demo_data.sql
├── migration_add_flight_time_zones.sql
└── migration_fix_cascade_foreign_keys.sql
```

---

## ⚙️ Installation and Setup

### Prerequisites

Before running the application, make sure you have installed:

- Java Development Kit (JDK 8 or later)
- MySQL Server
- MySQL Workbench (optional)
- Visual Studio Code / IntelliJ IDEA / Eclipse (optional)

### Step 1: Clone the Repository

Clone the project using Git:

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
```

Navigate to the project directory:

```bash
cd "SkyLink - Flight Crew Scheduling System"
```

Replace the repository URL with your actual GitHub repository URL.

### Step 2: Database Setup

1. Start your MySQL server.
2. Open MySQL Workbench.
3. Open the `schema.sql` file.
4. Execute the SQL script.
5. The required database and tables will be created.

The application uses the following database tables:

- `crew`
- `flight`
- `assignment`
- `leave_record`

To insert demonstration records, execute:

```sql
SOURCE seed_demo_data.sql;
```

If you are upgrading an existing database, execute the applicable migration scripts:

```sql
SOURCE migration_add_flight_time_zones.sql;
SOURCE migration_fix_cascade_foreign_keys.sql;
```

### Step 3: Configure Database Connection

Open the following file:

```text
util/DatabaseConnection.java
```

Configure the MySQL connection according to your local environment.

Make sure that:

- MySQL Server is running.
- The database has been created using `schema.sql`.
- The JDBC connection URL points to the correct database.
- Your local database connection settings are configured properly.

**Note:** Database connection details should be configured locally and must not be exposed in the public GitHub repository.

### Step 4: Compile the Project

Open Command Prompt in the project directory.

For Windows:

```bat
if not exist out mkdir out

javac -cp "lib\mysql-connector-j-26.7.0.jar" -d out Main.java DashboardWindow.java dao\*.java model\*.java service\*.java util\*.java
```

### Step 5: Run the Application

Execute the following command:

```bat
java -cp "out;lib\mysql-connector-j-26.7.0.jar" Main
```

The Java Swing dashboard will launch.

To start the console-based interface:

```bat
java -cp "out;lib\mysql-connector-j-26.7.0.jar" Main --console
```

**For macOS and Linux:** Replace the Windows classpath separator `;` with `:` and use `/` in file paths.

---

## 🏗️ System Architecture

SkyLink follows a layered architecture to separate user interaction, business logic, and database operations.

### 1. Presentation Layer
- Developed using Java Swing.
- Provides an interactive graphical interface.
- Allows users to access different application modules.

### 2. Service Layer
- Handles business logic.
- Performs input validation.
- Checks crew availability.
- Validates flight assignments and scheduling constraints.

### 3. DAO Layer
- Handles database operations.
- Uses JDBC to communicate with MySQL.
- Performs CRUD operations using SQL queries.

### 4. Model Layer
- Contains the application's entity classes.
- Represents crew members, flights, assignments, and leave records.
- Uses object-oriented programming concepts.

### 5. Database Layer
- Uses MySQL for persistent data storage.
- Stores crew information, flight details, assignments, and leave requests.

### Application Workflow

```text
             USER
               |
               v
        JAVA SWING GUI
               |
               v
         SERVICE LAYER
               |
               v
           DAO LAYER
               |
               v
             JDBC
               |
               v
        MYSQL DATABASE
```

---

## 🔐 Scheduling and Validation Rules

The application implements several validation mechanisms to maintain reliable flight crew scheduling.

- Prevents duplicate crew assignments.
- Checks crew availability before assigning flights.
- Detects overlapping flight schedules.
- Prevents assignments during approved leave periods.
- Validates flight departure and arrival times.
- Supports time-zone-aware flight scheduling.
- Checks mandatory rest periods between consecutive duties.
- Validates crew roles and required information.
- Prevents conflicting leave approvals.

### Time-Zone Support

SkyLink supports international flight scheduling through IANA time-zone identifiers.

Examples:

| Location | Time Zone |
|---|---|
| India | Asia/Kolkata |
| United Kingdom | Europe/London |
| United States | America/New_York |
| Japan | Asia/Tokyo |
| Singapore | Asia/Singapore |

For assignments involving multiple time zones, the application requires a configured minimum rest policy through the JVM property:

```text
crew.rest.multipleZoneMinimumHours
```

Example for Windows:

```bat
java -Dcrew.rest.multipleZoneMinimumHours=12 -cp "out;lib\mysql-connector-j-26.7.0.jar" Main
```

The value must follow the applicable policy for the intended use case.

---

## 🎯 Project Objectives

- Simplify airline crew scheduling.
- Reduce manual scheduling errors.
- Maintain centralized crew and flight records.
- Improve crew availability tracking.
- Prevent overlapping flight assignments.
- Streamline leave request processing.
- Provide a user-friendly desktop interface.
- Demonstrate practical Java and database integration.

---

## 📚 Concepts Implemented

This project demonstrates the practical implementation of:

- Object-Oriented Programming (OOP)
- Encapsulation
- Inheritance
- Polymorphism
- Abstraction
- Java Swing GUI Development
- JDBC Connectivity
- MySQL Database Operations
- CRUD Operations
- DAO Design Pattern
- Layered Architecture
- Exception Handling
- Input Validation
- Time-Zone-Aware Scheduling

---

## 🔮 Future Enhancements

Some possible improvements for future versions include:

- Role-based authentication.
- Automated crew allocation using scheduling algorithms.
- Real-time flight status integration.
- Email and SMS notifications.
- Advanced reporting and analytics.
- Exportable reports in PDF and Excel formats.
- Cloud-based database integration.
- Web-based and mobile application support.

---

## 📄 License

No license has currently been specified for this project. A suitable open-source license can be added before public distribution.

---

<div align="center">

### ✈️ SkyLink

**Simplifying Flight Operations Through Smart Crew Scheduling.**

Built with Java, Swing, JDBC, and MySQL.

</div>
