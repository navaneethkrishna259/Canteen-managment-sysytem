# Canteen Management System

A desktop-based Canteen Management System developed using Java Swing, JDBC, MySQL, and Maven. The application allows students to view available food items, place orders, receive bills, and check their order history. Administrators can manage the menu, monitor stock, and view sales reports.

## Features

### Student Module
- Securely enter login credentials through the login interface.
- View the available canteen menu, including item names, prices, and stock.
- Select food items and specify order quantities.
- Place orders with stock availability validation.
- Automatically calculate the order total and student discount.
- Generate a bill showing the item, quantity, price, discount, and final amount.
- View personal order history.
- Refresh the menu to view updated stock and prices.
- Log out and return to the login screen.

### Admin Module
- Log in using an administrator account.
- View the current food menu and stock details.
- Add new food items with their prices and stock quantities.
- Delete selected menu items.
- Refresh the menu after changes.
- Generate a sales report containing total order records, gross sales, total discounts, and net sales.
- Log out and return to the login screen.

### Database Features
- MySQL database integration using JDBC.
- Persistent storage for users, menu items, and orders.
- Prepared statements for database operations.
- Automatic stock deduction after successful orders.
- Transaction handling for order placement and stock updates.
- Foreign key relationships between users, menu items, and orders.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Application logic |
| Java Swing | Desktop graphical user interface |
| MySQL | Database management |
| JDBC | Database connectivity |
| MySQL Connector/J 9.4.0 | MySQL JDBC driver |
| Apache Maven | Dependency management and build |
| Git and GitHub | Version control and source code hosting |

## Project Structure

```text
canteen firos/
├── mysql/
│   └── schema.sql
├── src/
│   └── main/
│       └── java/
│           └── canteen/
│               ├── Admin.java
│               ├── AdminFrame.java
│               ├── DBConnection.java
│               ├── LoginFrame.java
│               ├── Main.java
│               ├── MenuItem.java
│               ├── Student.java
│               ├── StudentFrame.java
│               └── User.java
├── .gitignore
├── pom.xml
└── README.md
```

## Prerequisites

Install the following before running the application:

- Java Development Kit (JDK) 17 or later.
- Apache Maven.
- MySQL Server.
- A MySQL client such as MySQL Workbench.
- Git, if you want to clone the repository.

Verify Java and Maven from a terminal:

```bash
java -version
mvn -version
```

## Installation and Setup

### 1. Clone the repository

```bash
git clone https://github.com/navaneethkrishna259/Canteen-managment-sysytem.git
```

Move into the cloned project directory:

```bash
cd Canteen-managment-sysytem
```

### 2. Create the database

Open MySQL Workbench or another MySQL client.

Execute the SQL script located at:

```text
mysql/schema.sql
```

The script creates the `canteen_firos` database and the following tables:

- `users` — stores user accounts and roles.
- `menu_items` — stores food item names, prices, and stock.
- `orders` — stores order details, totals, discounts, final amounts, and order dates.

The script also inserts sample accounts and food items.

### 3. Configure database credentials

The application reads its MySQL connection settings from environment variables. Set them in your terminal before running the application.

**Windows PowerShell:**

```powershell
$env:CANTEEN_DB_URL="jdbc:mysql://localhost:3306/canteen_firos"
$env:CANTEEN_DB_USER="your_mysql_username"
$env:CANTEEN_DB_PASSWORD="your_mysql_password"
```

Replace the example username and password with your local MySQL credentials.

These variables apply to the current PowerShell session. If you open another terminal, set them again.

Do not commit real database passwords to GitHub or place them in this README.

### 4. Build the project

From the directory containing `pom.xml`, run:

```bash
mvn clean compile
```

Maven will download the required dependencies and compile the Java source files.

### 5. Run the application

Start the application using:

```bash
mvn exec:java
```

The login window should open if the database is available and the configuration is correct.

## Sample Login Accounts

The SQL setup script inserts the following demonstration accounts:

| Role | Username | Password |
|---|---|---|
| Student | `student1` | `student123` |
| Admin | `admin` | `admin123` |

These are demonstration credentials, not production accounts. The application currently stores and compares application passwords as plain text. Do not use real or reused passwords with this version.

## Discount Calculation

The application applies the following discount rules:

- **Student:** 5% discount on the order total.
- **Admin:** 0% discount.

The final amount is calculated as:

```text
Total = Item Price × Quantity
Discount = Total × Discount Rate
Final Amount = Total − Discount
```

Stock is checked before an order is saved, and the stock quantity is reduced after a successful order.

## Database Schema Overview

### Users

Stores the account ID, username, password, and role (`Student` or `Admin`).

### Menu Items

Stores the food item ID, item name, price, and available stock.

### Orders

Stores the order ID, user ID, item ID, quantity, total, discount, final amount, and order timestamp.

Foreign keys connect each order to its associated user and menu item.

## Troubleshooting

### Database connection failed
- Confirm that MySQL Server is running.
- Confirm that the `canteen_firos` database exists.
- Verify the `CANTEEN_DB_URL`, `CANTEEN_DB_USER`, and `CANTEEN_DB_PASSWORD` variables.
- Check that the MySQL username has permission to access the database.

### Maven command not found
Install Maven and ensure that it is available in your system's `PATH`. Restart the terminal after changing environment settings.

### Login failed
- Verify the username, password, and selected role.
- Confirm that the sample accounts were inserted by executing `mysql/schema.sql`.

### Unable to delete a menu item
A food item referenced by an existing order may not be deletable because of the database's foreign key constraint.

## Security Notes

- Database credentials should be supplied through environment variables.
- Never commit real passwords, access tokens, or private configuration files.
- Sample login credentials are for demonstration only.
- Application passwords are currently stored as plain text; password hashing should be implemented before real-world use.
- Review database permissions and error handling before deploying the application in a production environment.

## Future Improvements

- Password hashing and stronger authentication.
- Role-based authorization improvements.
- Search and category filters for menu items.
- Better reporting and sales analytics.
- More robust input validation and error handling.
- Improved financial precision using `BigDecimal`.
- Automated tests for ordering, stock management, and discounts.
- A more detailed installation and deployment process.

## Author

**Navaneeth Krishna A.**

## License

No license has been specified yet. Unless a license is added, the repository should not be assumed to grant others permission to redistribute or modify the project.
