
package com.phishguard.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardDAO {

    public int[] getScanStatistics(String username) throws SQLException {

        int[] statistics = new int[4];

        String sql = "SELECT COUNT(*) AS total_scans, "
                + "SUM(CASE WHEN sh.final_classification = 'Safe' THEN 1 ELSE 0 END) AS safe_count, "
                + "SUM(CASE WHEN sh.final_classification = 'Suspicious' THEN 1 ELSE 0 END) AS suspicious_count, "
                + "SUM(CASE WHEN sh.final_classification = 'Phishing' THEN 1 ELSE 0 END) AS phishing_count "
                + "FROM scan_history sh "
                + "JOIN users u ON sh.user_id = u.user_id "
                + "WHERE u.username = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setString(1, username);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        statistics[0] = result.getInt("total_scans");
                        statistics[1] = result.getInt("safe_count");
                        statistics[2] = result.getInt("suspicious_count");
                        statistics[3] = result.getInt("phishing_count");
                    }
                }
            }
        }

        return statistics;
    }
}
