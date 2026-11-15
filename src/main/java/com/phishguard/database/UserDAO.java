
package com.phishguard.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    public int getUserId(String username) throws SQLException {

        String sql = "SELECT user_id FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setString(1, username);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        return result.getInt("user_id");
                    }
                }
            }
        }

        return -1;
    }

    public String getUserRole(String username) throws SQLException {

        String sql = "SELECT role FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setString(1, username);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        return result.getString("role");
                    }
                }
            }
        }

        return null;
    }

    public List<Object[]> getAllUsers() throws SQLException {

        List<Object[]> users = new ArrayList<>();

        String sql = "SELECT user_id, full_name, username, email, "
                + "role, account_status, registration_date "
                + "FROM users ORDER BY user_id ASC";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Object[] row = {
                            result.getInt("user_id"),
                            result.getString("full_name"),
                            result.getString("username"),
                            result.getString("email"),
                            result.getString("role"),
                            result.getString("account_status"),
                            result.getTimestamp("registration_date")
                    };

                    users.add(row);
                }
            }
        }

        return users;
    }

    public boolean updateUserStatus(
            String username,
            String newStatus,
            int administratorId) throws SQLException {

        if (!"ACTIVE".equalsIgnoreCase(newStatus)
                && !"INACTIVE".equalsIgnoreCase(newStatus)) {
            throw new SQLException("Invalid account status.");
        }

        String selectSql = "SELECT user_id, role FROM users "
                + "WHERE username = ? FOR UPDATE";

        String updateSql = "UPDATE users "
                + "SET account_status = ? WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            connection.setAutoCommit(false);

            try {
                int targetUserId;
                String targetRole;

                try (PreparedStatement statement =
                        connection.prepareStatement(selectSql)) {

                    statement.setString(1, username);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            throw new SQLException("User not found.");
                        }

                        targetUserId = result.getInt("user_id");
                        targetRole = result.getString("role");
                    }
                }

                if (!"USER".equalsIgnoreCase(targetRole)) {
                    throw new SQLException(
                            "Administrator accounts cannot be modified here."
                    );
                }

                try (PreparedStatement statement =
                        connection.prepareStatement(updateSql)) {

                    statement.setString(1, newStatus.toUpperCase());
                    statement.setInt(2, targetUserId);

                    if (statement.executeUpdate() == 0) {
                        throw new SQLException("User status was not updated.");
                    }
                }

                insertAdminLog(
                        connection,
                        administratorId,
                        "Changed account status of " + username
                                + " to " + newStatus.toUpperCase()
                );

                connection.commit();
                return true;

            } catch (SQLException exception) {

                connection.rollback();
                throw exception;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public boolean deleteUser(
            String username,
            int administratorId) throws SQLException {

        String selectSql = "SELECT user_id, role FROM users "
                + "WHERE username = ? FOR UPDATE";

        String deleteSql = "DELETE FROM users WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            connection.setAutoCommit(false);

            try {
                int targetUserId;
                String targetRole;

                try (PreparedStatement statement =
                        connection.prepareStatement(selectSql)) {

                    statement.setString(1, username);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            throw new SQLException("User not found.");
                        }

                        targetUserId = result.getInt("user_id");
                        targetRole = result.getString("role");
                    }
                }

                if (!"USER".equalsIgnoreCase(targetRole)) {
                    throw new SQLException(
                            "Administrator accounts cannot be deleted."
                    );
                }

                int dependentRecords = 0;

                String[] dependencyQueries = {
                        "SELECT COUNT(*) FROM scan_history WHERE user_id = ?",
                        "SELECT COUNT(*) FROM trusted_domains WHERE added_by = ?",
                        "SELECT COUNT(*) FROM admin_logs WHERE administrator_id = ?"
                };

                for (String query : dependencyQueries) {

                    try (PreparedStatement statement =
                            connection.prepareStatement(query)) {

                        statement.setInt(1, targetUserId);

                        try (ResultSet result = statement.executeQuery()) {

                            if (result.next()) {
                                dependentRecords += result.getInt(1);
                            }
                        }
                    }
                }

                if (dependentRecords > 0) {
                    throw new SQLException(
                            "This user has " + dependentRecords
                                    + " dependent record(s). "
                                    + "Deletion is blocked to preserve database records. "
                                    + "Deactivate this account instead."
                    );
                }

                insertAdminLog(
                        connection,
                        administratorId,
                        "Deleted user account: " + username
                );

                try (PreparedStatement statement =
                        connection.prepareStatement(deleteSql)) {

                    statement.setInt(1, targetUserId);

                    if (statement.executeUpdate() == 0) {
                        throw new SQLException("User deletion failed.");
                    }
                }

                connection.commit();
                return true;

            } catch (SQLException exception) {

                connection.rollback();
                throw exception;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void insertAdminLog(
            Connection connection,
            int administratorId,
            String action) throws SQLException {

        String sql = "INSERT INTO admin_logs "
                + "(administrator_id, action_performed) VALUES (?, ?)";

        try (PreparedStatement statement =
                connection.prepareStatement(sql)) {

            statement.setInt(1, administratorId);
            statement.setString(2, action);

            statement.executeUpdate();
        }
    }
}

