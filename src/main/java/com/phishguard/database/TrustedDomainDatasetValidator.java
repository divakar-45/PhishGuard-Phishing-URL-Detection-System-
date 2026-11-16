
package com.phishguard.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.IDN;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TrustedDomainDatasetValidator {

    private final Set<String> seenDomains = new HashSet<>();

    private long total;
    private long valid;
    private long idnReview;
    private long duplicates;
    private long invalid;
    private long rankErrors;

    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println(
                    "Usage: TrustedDomainDatasetValidator "
                    + "<input.csv> <valid.csv> <idn-review.csv> <invalid.csv>"
            );
            return;
        }

        Path input = Path.of(args[0]);
        Path validOutput = Path.of(args[1]);
        Path idnOutput = Path.of(args[2]);
        Path invalidOutput = Path.of(args[3]);

        TrustedDomainDatasetValidator validator =
                new TrustedDomainDatasetValidator();

        try {
            validator.validate(
                    input,
                    validOutput,
                    idnOutput,
                    invalidOutput
            );
        } catch (Exception e) {
            System.out.println("Validation failed: " + e.getMessage());
        }
    }

    public void validate(
            Path input,
            Path validOutput,
            Path idnOutput,
            Path invalidOutput) throws IOException {

        if (!Files.isRegularFile(input)) {
            throw new IOException("Input dataset not found: " + input);
        }

        Path inputAbsolute = input.toAbsolutePath().normalize();
        Path validAbsolute = validOutput.toAbsolutePath().normalize();
        Path idnAbsolute = idnOutput.toAbsolutePath().normalize();
        Path invalidAbsolute = invalidOutput.toAbsolutePath().normalize();

        if (inputAbsolute.equals(validAbsolute)
                || inputAbsolute.equals(idnAbsolute)
                || inputAbsolute.equals(invalidAbsolute)
                || validAbsolute.equals(idnAbsolute)
                || validAbsolute.equals(invalidAbsolute)
                || idnAbsolute.equals(invalidAbsolute)) {
            throw new IOException("Input and output paths must be different");
        }

        createParentDirectory(validOutput);
        createParentDirectory(idnOutput);
        createParentDirectory(invalidOutput);

        try (BufferedReader reader = Files.newBufferedReader(
                    input, StandardCharsets.UTF_8);
             BufferedWriter validWriter = Files.newBufferedWriter(
                    validOutput, StandardCharsets.UTF_8);
             BufferedWriter idnWriter = Files.newBufferedWriter(
                    idnOutput, StandardCharsets.UTF_8);
             BufferedWriter invalidWriter = Files.newBufferedWriter(
                    invalidOutput, StandardCharsets.UTF_8)) {

            String line;
            long expectedRank = 1;
            long lineNumber = 0;

            while ((line = reader.readLine()) != null) {

                lineNumber++;
                total++;

                if (lineNumber == 1) {
                    line = line.replace("\uFEFF", "");
                }

                String[] columns = line.split(",", -1);

                if (columns.length != 2) {
                    writeCsvRecord(
                            invalidWriter,
                            line,
                            "Invalid CSV structure"
                    );
                    printProgress();
                    continue;
                }

                String rankText = columns[0].trim();
                String originalDomain = columns[1].trim();

                long rank;

                try {
                    rank = Long.parseLong(rankText);

                    if (rank != expectedRank) {
                        rankErrors++;
                    }

                    expectedRank++;

                } catch (NumberFormatException e) {

                    writeCsvRecord(
                            invalidWriter,
                            line,
                            "Invalid rank"
                    );

                    expectedRank++;
                    printProgress();
                    continue;
                }

                if (rank <= 0 || originalDomain.isEmpty()) {
                    writeCsvRecord(
                            invalidWriter,
                            line,
                            "Empty domain or invalid rank"
                    );
                    printProgress();
                    continue;
                }

                String normalizedDomain;

                try {
                    normalizedDomain = normalizeDomain(originalDomain);
                } catch (IllegalArgumentException e) {
                    writeCsvRecord(
                            invalidWriter,
                            line,
                            e.getMessage()
                    );
                    printProgress();
                    continue;
                }

                if (!seenDomains.add(normalizedDomain)) {

                    duplicates++;

                    writeCsvRecord(
                            invalidWriter,
                            line,
                            "Duplicate domain: " + normalizedDomain
                    );

                    printProgress();
                    continue;
                }

                if (containsPunycodeLabel(normalizedDomain)) {

                    idnReview++;

                    writeReviewRecord(
                            idnWriter,
                            rank + "," + normalizedDomain,
                            "Requires separate IDNA review; not approved for import"
                    );

                } else {

                    valid++;

                    validWriter.write(rank + "," + normalizedDomain);
                    validWriter.newLine();
                }

                printProgress();
            }
        }

        printSummary();
    }

    private void createParentDirectory(Path path) throws IOException {

        Path parent = path.toAbsolutePath().getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private String normalizeDomain(String domain) {

        String normalized = domain.toLowerCase(Locale.ROOT);

        if (normalized.startsWith(".")
                || normalized.endsWith(".")
                || normalized.contains("..")
                || normalized.contains("_")
                || normalized.contains("*")) {
            throw new IllegalArgumentException(
                    "Wildcard or invalid domain"
            );
        }

        if (normalized.contains("://")
                || normalized.contains("/")
                || normalized.contains("\\")
                || normalized.contains("@")
                || normalized.contains(":")
                || normalized.contains("?")
                || normalized.contains("#")
                || normalized.contains(" ")) {
            throw new IllegalArgumentException("Not a domain entry");
        }

        String ascii;

        try {
            ascii = IDN.toASCII(
                    normalized,
                    IDN.USE_STD3_ASCII_RULES
            ).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid domain encoding");
        }

        if (ascii.length() > 253 || !ascii.contains(".")) {
            throw new IllegalArgumentException("Invalid domain length");
        }

        String[] labels = ascii.split("\\.", -1);

        for (String label : labels) {

            if (label.isEmpty() || label.length() > 63) {
                throw new IllegalArgumentException("Invalid domain label");
            }

            if (label.startsWith("xn--")) {

                String punycodeSuffix = label.substring(4);

                if (punycodeSuffix.isEmpty()
                        || !punycodeSuffix.matches(
                                "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"
                        )) {
                    throw new IllegalArgumentException(
                            "Malformed Punycode label"
                    );
                }

            } else if (!label.matches(
                    "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"
            )) {
                throw new IllegalArgumentException("Invalid domain label");
            }
        }

        String tld = labels[labels.length - 1];

        if (!tld.matches(
                "(?:[a-z]{2,63}|xn--[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)"
        )) {
            throw new IllegalArgumentException("Invalid top-level domain");
        }

        return ascii;
    }

    private boolean containsPunycodeLabel(String domain) {

        for (String label : domain.split("\\.")) {
            if (label.startsWith("xn--")) {
                return true;
            }
        }

        return false;
    }

    private void writeCsvRecord(
            BufferedWriter writer,
            String line,
            String reason) throws IOException {

        invalid++;
        writeRecord(writer, line, reason);
    }

    private void writeReviewRecord(
            BufferedWriter writer,
            String line,
            String reason) throws IOException {

        writeRecord(writer, line, reason);
    }

    private void writeRecord(
            BufferedWriter writer,
            String line,
            String reason) throws IOException {

        String safeLine = line.replace("\"", "\"\"");
        String safeReason = reason.replace("\"", "\"\"");

        writer.write("\"" + safeLine + "\",\"" + safeReason + "\"");
        writer.newLine();
    }

    private void printProgress() {

        if (total % 100000 == 0) {
            System.out.println("Processed records: " + total);
        }
    }

    private void printSummary() {

        long accounted = valid + idnReview + invalid;

        System.out.println();
        System.out.println("========== DATASET VALIDATION ==========");
        System.out.println("Total records       : " + total);
        System.out.println("Valid unique domains: " + valid);
        System.out.println("IDN review records  : " + idnReview);
        System.out.println("Duplicate domains   : " + duplicates);
        System.out.println("Invalid records     : " + invalid);
        System.out.println("Rank sequence errors: " + rankErrors);
        System.out.println("----------------------------------------");
        System.out.println("Accounted records   : " + accounted);
        System.out.println(
                "Accounting status   : "
                + (accounted == total ? "PASS" : "FAIL")
        );
        System.out.println("========================================");
    }
}
