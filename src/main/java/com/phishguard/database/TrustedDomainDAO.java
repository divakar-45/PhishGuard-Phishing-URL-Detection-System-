package com.phishguard.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrustedDomainDAO {

    public boolean isTrustedDomain(String domain) throws SQLException {

        String sql = "SELECT 1 FROM trusted_domains WHERE domain_name = ? LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, domain);

                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        }
    }
}