package com.phishguard.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/phishguard";

    public static Connection getConnection() {

        String user = System.getenv("PHISHGUARD_DB_USER");
        String password = System.getenv("PHISHGUARD_DB_PASSWORD");

        if (user == null || password == null) {
            System.out.println("Database credentials are not configured.");
            return null;
        }

        try {
            Connection connection =
                    DriverManager.getConnection(URL, user, password);

            return connection;

        } catch (SQLException e) {
            System.out.println("Database Connection Failed!");
            System.out.println(e.getMessage());
            return null;
        }
    }
}