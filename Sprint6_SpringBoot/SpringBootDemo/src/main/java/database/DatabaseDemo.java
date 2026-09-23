package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseDemo {

    // 1. Define database connection details
    // Format: jdbc:postgresql://<host>:<port>/<database_name>
    private static final String URL = "jdbc:postgresql://localhost:5432/finance";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "password");
        props.setProperty("currentSchema", "trading");
        // 2. Establish connection and execute queries using try-with-resources
        try (Connection connection = DriverManager.getConnection(URL,props)) {

            System.out.println("Successfully connected to PostgreSQL database!");

            // Example A: Create a Table
            try (Statement statement = connection.createStatement()) {
                String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS users (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(100) UNIQUE NOT NULL
                    )
                """;
                statement.execute(createTableSQL);
                System.out.println("Table 'users' verified/created.");
            }

            // Example B: Insert Data (Using PreparedStatement to prevent SQL Injection)
            String insertSQL = "INSERT INTO users (name, email) VALUES (?, ?) ON CONFLICT DO NOTHING";
            try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
                pstmt.setString(1, "Alice Smith");
                pstmt.setString(2, "alice@example.com");

                int rowsInserted = pstmt.executeUpdate();
                System.out.println(rowsInserted + " row(s) inserted.");
            }

             //Example C: Read/Query Data
            String selectSQL = "SELECT id, name, email FROM users";
            try (PreparedStatement pstmt = connection.prepareStatement(selectSQL);
                 ResultSet resultSet = pstmt.executeQuery()) {

                System.out.println("\n--- User List ---");
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String email = resultSet.getString("email");

                    System.out.printf("ID: %d | Name: %s | Email: %s%n", id, name, email);
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error occurred:");
            e.printStackTrace();
        }
    }
}
