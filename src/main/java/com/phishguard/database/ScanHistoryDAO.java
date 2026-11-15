
package com.phishguard.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScanHistoryDAO {

    public boolean saveScan(
            int userId,
            String submittedUrl,
            String registeredDomain,
            int riskScore,
            String classification) throws SQLException {

        String sql = "INSERT INTO scan_history "
                + "(user_id, submitted_url, registered_domain, "
                + "risk_score, final_classification) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setInt(1, userId);
                statement.setString(2, submittedUrl);
                statement.setString(3, registeredDomain);
                statement.setInt(4, riskScore);
                statement.setString(5, classification);

                return statement.executeUpdate() == 1;
            }
        }
    }

    public List<Object[]> getUserScans(int userId) throws SQLException {

        List<Object[]> scans = new ArrayList<>();

        String sql = "SELECT scan_id, submitted_url, registered_domain, "
                + "risk_score, final_classification, scan_timestamp "
                + "FROM scan_history WHERE user_id = ? "
                + "ORDER BY scan_timestamp DESC";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setInt(1, userId);

                try (ResultSet result = statement.executeQuery()) {

                    while (result.next()) {

                        Object[] row = {
                            result.getInt("scan_id"),
                            result.getString("submitted_url"),
                            result.getString("registered_domain"),
                            result.getObject("risk_score"),
                            result.getString("final_classification"),
                            result.getTimestamp("scan_timestamp")
                        };

                        scans.add(row);
                    }
                }
            }
        }

        return scans;
    }

    public List<Object[]> searchUserScans(
            int userId, String searchText) throws SQLException {

        List<Object[]> scans = new ArrayList<>();

        String sql = "SELECT scan_id, submitted_url, registered_domain, "
                + "risk_score, final_classification, scan_timestamp "
                + "FROM scan_history "
                + "WHERE user_id = ? AND "
                + "(submitted_url LIKE ? "
                + "OR registered_domain LIKE ? "
                + "OR final_classification LIKE ?) "
                + "ORDER BY scan_timestamp DESC";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                String searchPattern = "%" + searchText + "%";

                statement.setInt(1, userId);
                statement.setString(2, searchPattern);
                statement.setString(3, searchPattern);
                statement.setString(4, searchPattern);

                try (ResultSet result = statement.executeQuery()) {

                    while (result.next()) {

                        Object[] row = {
                            result.getInt("scan_id"),
                            result.getString("submitted_url"),
                            result.getString("registered_domain"),
                            result.getObject("risk_score"),
                            result.getString("final_classification"),
                            result.getTimestamp("scan_timestamp")
                        };

                        scans.add(row);
                    }
                }
            }
        }

        return scans;
    }
}
