
package com.phishguard.ui;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardFrame extends JFrame {

    public AdminDashboardFrame(String username) {

        setTitle("PhishGuard - Admin Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(
                "PhishGuard Admin Dashboard",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel welcome = new JLabel(
                "Welcome, " + username + " (ADMIN)",
                SwingConstants.CENTER
        );

        welcome.setFont(new Font("Arial", Font.PLAIN, 18));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        headerPanel.setBorder(
                BorderFactory.createEmptyBorder(25, 10, 25, 10)
        );

        headerPanel.add(heading);
        headerPanel.add(welcome);

        JPanel modulePanel = new JPanel(
                new GridLayout(2, 2, 20, 20)
        );

        modulePanel.setBorder(
                BorderFactory.createEmptyBorder(50, 70, 50, 70)
        );

        JButton trustedDomainButton =
                new JButton("Trusted Domain Management");

        JButton userManagementButton =
                new JButton("User Management");

        JButton adminLogsButton =
                new JButton("Admin Activity Logs");

        JButton logoutButton =
                new JButton("Logout");

        modulePanel.add(trustedDomainButton);
        modulePanel.add(userManagementButton);
        modulePanel.add(adminLogsButton);
        modulePanel.add(logoutButton);

        add(headerPanel, BorderLayout.NORTH);
        add(modulePanel, BorderLayout.CENTER);

        trustedDomainButton.addActionListener(e -> {

            TrustedDomainManagementFrame frame =
                    new TrustedDomainManagementFrame(username);

            frame.setVisible(true);
        });

        userManagementButton.addActionListener(e -> {

            UserManagementFrame frame =
                    new UserManagementFrame(username);

            frame.setVisible(true);
        });

        adminLogsButton.addActionListener(e -> {

            AdminLogsFrame frame = new AdminLogsFrame(username);
            frame.setVisible(true);
        });

        logoutButton.addActionListener(e -> {

            new LoginFrame().setVisible(true);
            dispose();
        });
    }
}
