
package canteen;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
private static final String URL =
        System.getenv().getOrDefault(
                "CANTEEN_DB_URL",
                "jdbc:mysql://localhost:3306/canteen_firos");

private static final String USER =
        System.getenv("CANTEEN_DB_USER");

private static final String PASSWORD =
        System.getenv("CANTEEN_DB_PASSWORD");

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("MySQL database connected successfully!");
            System.out.println("Database: " +
                    connection.getCatalog());
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            System.out.println(e.getMessage());
        }
    }
}
