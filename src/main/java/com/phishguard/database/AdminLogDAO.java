
package com.phishguard.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminLogDAO {

    public List<Object[]> getAllAdminLogs() throws SQLException {

        List<Object[]> logs = new ArrayList<>();

        String sql = "SELECT l.log_id, u.username, "
                + "l.action_performed, l.log_timestamp "
                + "FROM admin_logs l "
                + "INNER JOIN users u "
                + "ON l.administrator_id = u.user_id "
                + "ORDER BY l.log_timestamp DESC, l.log_id DESC";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Object[] row = {
                            result.getInt("log_id"),
                            result.getString("username"),
                            result.getString("action_performed"),
                            result.getTimestamp("log_timestamp")
                    };

                    logs.add(row);
                }
            }
        }

        return logs;
    }
}
