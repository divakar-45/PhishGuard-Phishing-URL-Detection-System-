
package com.phishguard.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.net.IDN;
import java.util.Locale;

public class TrustedDomainBulkImporter {

    private static final int BATCH_SIZE = 1000;
    private static final String DEFAULT_CATEGORY = "Popular Domains";

    private static final String INSERT_SQL =
            "INSERT IGNORE INTO trusted_domains "
            + "(domain_name, category, added_by) "
            + "VALUES (?, ?, NULL)";

    private int inserted;
    private int duplicates;
    private int invalid;
    private int processed;

    public static void main(String[] args) {

        if (args.length < 1 || args.length > 2) {
            System.out.println(
                    "Usage: TrustedDomainBulkImporter <file-path> [category]"
            );
            return;
        }

        Path file = Path.of(args[0]);
        String category = args.length == 2
                ? args[1].trim()
                : DEFAULT_CATEGORY;

        if (category.isEmpty()) {
            System.out.println("Category cannot be empty.");
            return;
        }

        TrustedDomainBulkImporter importer =
                new TrustedDomainBulkImporter();

        try {
            importer.importFile(file, category);
        } catch (Exception e) {
            System.out.println("Import failed: " + e.getMessage());
        }
    }

    public void importFile(Path file, String category)
            throws IOException, SQLException {

        if (!Files.isRegularFile(file)) {
            throw new IOException("Dataset file not found: " + file);
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                throw new SQLException("Database connection unavailable.");
            }

            connection.setAutoCommit(false);

            try (BufferedReader reader = Files.newBufferedReader(
                        file, StandardCharsets.UTF_8);
                 PreparedStatement statement =
                        connection.prepareStatement(INSERT_SQL)) {

                String line;
                int batchCount = 0;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {

                    lineNumber++;
                    processed++;

                    if (lineNumber == 1) {
                        line = line.replace("\uFEFF", "");
                    }

                    line = line.trim();

                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    String domain;

                    try {
                        domain = extractDomain(line);
                        domain = normalizeDomain(domain);
                    } catch (IllegalArgumentException e) {
                        invalid++;
                        continue;
                    }

                    statement.setString(1, domain);
                    statement.setString(2, category);
                    statement.addBatch();

                    batchCount++;

                    if (batchCount >= BATCH_SIZE) {
                        executeBatch(connection, statement, batchCount);
                        batchCount = 0;
                    }
                }

                if (batchCount > 0) {
                    executeBatch(connection, statement, batchCount);
                }

                connection.commit();

            } catch (Exception e) {

                connection.rollback();
                throw e;
            }
        }

        printSummary();
    }

    private void executeBatch(
            Connection connection,
            PreparedStatement statement,
            int batchCount) throws SQLException {

        int[] results = statement.executeBatch();

        int successful = 0;

        for (int result : results) {
            if (result == Statement.SUCCESS_NO_INFO || result > 0) {
                successful++;
            }
        }

        inserted += successful;
        duplicates += batchCount - successful;

        connection.commit();
        statement.clearBatch();

        System.out.println(
                "Batch processed: " + batchCount
                + " | Inserted: " + successful
                + " | Duplicates: " + (batchCount - successful)
        );
    }

    private String extractDomain(String line) {

        String value = line.trim();

        if (value.contains(",")) {
            String[] columns = value.split(",", -1);
            value = columns[columns.length - 1].trim();
        }

        if (value.equalsIgnoreCase("domain")
                || value.equalsIgnoreCase("domain_name")) {
            throw new IllegalArgumentException("Header row");
        }

        if (value.contains("://")
                || value.contains("/")
                || value.contains("@")
                || value.contains(":")
                || value.contains("?")
                || value.contains("#")) {
            throw new IllegalArgumentException("Not a domain entry");
        }

        return value;
    }

    private String normalizeDomain(String domain) {

        if (domain == null || domain.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty domain");
        }

        String normalized = domain.trim()
                .toLowerCase(Locale.ROOT);

        if (normalized.endsWith(".")) {
            normalized = normalized.substring(
                    0, normalized.length() - 1
            );
        }

        try {
            normalized = IDN.toASCII(
                    normalized,
                    IDN.USE_STD3_ASCII_RULES
            ).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid domain");
        }

        if (normalized.length() > 253 || !normalized.contains(".")) {
            throw new IllegalArgumentException("Invalid domain length");
        }

        String[] labels = normalized.split("\\.", -1);

        for (String label : labels) {

            if (label.isEmpty()
                    || label.length() > 63
                    || !label.matches(
                            "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"
                    )) {
                throw new IllegalArgumentException("Invalid domain label");
            }
        }

        String tld = labels[labels.length - 1];

        if (!tld.matches("[a-z]{2,63}")) {
            throw new IllegalArgumentException("Invalid top-level domain");
        }

        return normalized;
    }

    private void printSummary() {

        System.out.println();
        System.out.println("========== IMPORT SUMMARY ==========");
        System.out.println("Input lines processed : " + processed);
        System.out.println("Successfully inserted : " + inserted);
        System.out.println("Already existed       : " + duplicates);
        System.out.println("Invalid entries       : " + invalid);
        System.out.println("====================================");
    }
}
