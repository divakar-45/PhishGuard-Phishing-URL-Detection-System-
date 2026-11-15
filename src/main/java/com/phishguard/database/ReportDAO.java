
package com.phishguard.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public boolean createReport(
            int userId,
            int scanId,
            String reportTitle) throws SQLException {

        if (reportTitle == null || reportTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Report title is required.");
        }

        String sql = "INSERT INTO reports (scan_id, report_title) "
                + "SELECT scan_id, ? FROM scan_history "
                + "WHERE scan_id = ? AND user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setString(1, reportTitle.trim());
                statement.setInt(2, scanId);
                statement.setInt(3, userId);

                return statement.executeUpdate() == 1;
            }
        }
    }

    public List<Object[]> getUserReports(int userId) throws SQLException {

        List<Object[]> reports = new ArrayList<>();

        String sql = "SELECT r.report_id, r.scan_id, r.report_title, "
                + "r.generated_at, s.submitted_url, s.registered_domain, "
                + "s.risk_score, s.final_classification "
                + "FROM reports r "
                + "INNER JOIN scan_history s ON r.scan_id = s.scan_id "
                + "WHERE s.user_id = ? "
                + "ORDER BY r.generated_at DESC";

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
                            result.getInt("report_id"),
                            result.getInt("scan_id"),
                            result.getString("report_title"),
                            result.getTimestamp("generated_at"),
                            result.getString("submitted_url"),
                            result.getString("registered_domain"),
                            result.getObject("risk_score"),
                            result.getString("final_classification")
                        };

                        reports.add(row);
                    }
                }
            }
        }

        return reports;
    }

    public Object[] getReportDetails(
            int userId,
            int reportId) throws SQLException {

        String sql = "SELECT r.report_id, r.scan_id, r.report_title, "
                + "r.generated_at, s.submitted_url, s.registered_domain, "
                + "s.risk_score, s.final_classification, "
                + "s.detected_indicators "
                + "FROM reports r "
                + "INNER JOIN scan_history s ON r.scan_id = s.scan_id "
                + "WHERE r.report_id = ? AND s.user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setInt(1, reportId);
                statement.setInt(2, userId);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {

                        return new Object[]{
                            result.getInt("report_id"),
                            result.getInt("scan_id"),
                            result.getString("report_title"),
                            result.getTimestamp("generated_at"),
                            result.getString("submitted_url"),
                            result.getString("registered_domain"),
                            result.getObject("risk_score"),
                            result.getString("final_classification"),
                            result.getString("detected_indicators")
                        };
                    }
                }
            }
        }

        return null;
    }
}
