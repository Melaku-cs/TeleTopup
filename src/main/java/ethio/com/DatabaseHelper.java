package ethio.com;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://10.57.40.118:3306/ussd"; // Update with your DB details
        String user = "root";
        String password = "root";
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to the database", e);
        }
    }
}
