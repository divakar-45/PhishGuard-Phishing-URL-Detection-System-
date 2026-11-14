
package com.phishguard.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.phishguard.util.PasswordUtil;

public class UserDAO {

    public boolean registerUser(
            String fullName,
            String username,
            String email,
            String password) throws Exception {

        String sql = "INSERT INTO users "
                + "(full_name, username, email, password_hash, role, account_status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        String passwordHash = PasswordUtil.hashPassword(password);

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setString(1, fullName);
                statement.setString(2, username);
                statement.setString(3, email);
                statement.setString(4, passwordHash);
                statement.setString(5, "USER");
                statement.setString(6, "ACTIVE");

                statement.executeUpdate();

                return true;
            }
        }
    }

    public boolean authenticateUser(
            String username, String password) throws Exception {

        String sql = "SELECT password_hash, account_status "
                + "FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setString(1, username);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {

                        String storedHash = result.getString("password_hash");
                        String status = result.getString("account_status");

                        if (!"ACTIVE".equalsIgnoreCase(status)) {
                            return false;
                        }

                        return PasswordUtil.verifyPassword(
                            password, storedHash
                        );
                    }
                }
            }
        }

        return false;
    }
}
