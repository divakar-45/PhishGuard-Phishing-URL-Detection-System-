package com.phishguard.database;

import java.sql.SQLException;

public class TrustedDomainTest {

    public static void main(String[] args) {

        TrustedDomainDAO dao = new TrustedDomainDAO();

        String[] domains = {
                "google.com",
                "microsoft.com",
                "unknown-example.com"
        };

        for (String domain : domains) {

            try {
                boolean trusted = dao.isTrustedDomain(domain);

                System.out.println("Domain: " + domain);
                System.out.println("Status: " +
                        (trusted ? "Trusted Domain" : "Not Found in Trusted Database"));
                System.out.println("-------------------------");

            } catch (SQLException e) {
                System.out.println("Database Error: " + e.getMessage());
            }
        }
    }
}