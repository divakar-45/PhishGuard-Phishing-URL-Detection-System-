
package com.phishguard.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrustedDomainDAO {

    public boolean isTrustedDomain(String domain) throws SQLException {

        String sql = "SELECT 1 FROM trusted_domains WHERE domain_name = ? LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                statement.setString(1, domain);

                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        }
    }

    public List<Object[]> getAllTrustedDomains() throws SQLException {

        List<Object[]> domains = new ArrayList<>();

        String sql = "SELECT domain_id, domain_name, category, "
                + "date_added, added_by FROM trusted_domains "
                + "ORDER BY domain_id DESC";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Object[] row = {
                            result.getInt("domain_id"),
                            result.getString("domain_name"),
                            result.getString("category"),
                            result.getTimestamp("date_added"),
                            result.getInt("added_by")
                    };

                    domains.add(row);
                }
            }
        }

        return domains;
    }

    public boolean addTrustedDomain(
            String domain,
            String category,
            int administratorId) throws SQLException {

        String sql = "INSERT INTO trusted_domains "
                + "(domain_name, category, added_by) VALUES (?, ?, ?)";

        String normalizedDomain = normalizeDomain(domain);

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            connection.setAutoCommit(false);

            try {

                try (PreparedStatement statement =
                        connection.prepareStatement(sql)) {

                    statement.setString(1, normalizedDomain);

                    if (category == null || category.trim().isEmpty()) {
                        statement.setNull(2, java.sql.Types.VARCHAR);
                    } else {
                        statement.setString(2, category.trim());
                    }

                    statement.setInt(3, administratorId);

                    statement.executeUpdate();
                }

                insertAdminLog(
                        connection,
                        administratorId,
                        "Added trusted domain: " + normalizedDomain
                );

                connection.commit();
                return true;

            } catch (SQLException e) {

                connection.rollback();
                throw e;
            }
        }
    }

    public boolean updateTrustedDomain(
            int domainId,
            String domain,
            String category,
            int administratorId) throws SQLException {

        String sql = "UPDATE trusted_domains "
                + "SET domain_name = ?, category = ? "
                + "WHERE domain_id = ?";

        String normalizedDomain = normalizeDomain(domain);

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            connection.setAutoCommit(false);

            try {

                int affectedRows;

                try (PreparedStatement statement =
                        connection.prepareStatement(sql)) {

                    statement.setString(1, normalizedDomain);

                    if (category == null || category.trim().isEmpty()) {
                        statement.setNull(2, java.sql.Types.VARCHAR);
                    } else {
                        statement.setString(2, category.trim());
                    }

                    statement.setInt(3, domainId);

                    affectedRows = statement.executeUpdate();
                }

                if (affectedRows == 0) {
                    connection.rollback();
                    return false;
                }

                insertAdminLog(
                        connection,
                        administratorId,
                        "Updated trusted domain ID " + domainId
                                + ": " + normalizedDomain
                );

                connection.commit();
                return true;

            } catch (SQLException e) {

                connection.rollback();
                throw e;
            }
        }
    }

    public boolean deleteTrustedDomain(
            int domainId,
            int administratorId) throws SQLException {

        String selectSql =
                "SELECT domain_name FROM trusted_domains WHERE domain_id = ?";

        String deleteSql =
                "DELETE FROM trusted_domains WHERE domain_id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            connection.setAutoCommit(false);

            try {

                String domainName;

                try (PreparedStatement statement =
                        connection.prepareStatement(selectSql)) {

                    statement.setInt(1, domainId);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }

                        domainName = result.getString("domain_name");
                    }
                }

                try (PreparedStatement statement =
                        connection.prepareStatement(deleteSql)) {

                    statement.setInt(1, domainId);
                    statement.executeUpdate();
                }

                insertAdminLog(
                        connection,
                        administratorId,
                        "Deleted trusted domain: " + domainName
                );

                connection.commit();
                return true;

            } catch (SQLException e) {

                connection.rollback();
                throw e;
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

    private String normalizeDomain(String domain) {

        if (domain == null || domain.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain cannot be empty.");
        }

        return domain.trim().toLowerCase(Locale.ROOT);
    }
}
