package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/BakeryStoreDB";
    private static final String USER = "root";  
    private static final String PASSWORD = "Tuan2004@";  

    private static Connection connection = null;

    // Phương thức kết nối cơ sở dữ liệu
    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            try {
                // Đăng ký driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connection established!");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new SQLException("MySQL JDBC Driver not found", e);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Error connecting to the database", e);
            }
        }
        return connection;
    }

    // Phương thức đóng kết nối cơ sở dữ liệu
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error closing the database connection.");
        }
    }
}
