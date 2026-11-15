
package com.phishguard.ui;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import com.phishguard.database.DashboardDAO;

public class DashboardFrame extends JFrame {

    private JLabel totalScans;
    private JLabel safeScans;
    private JLabel suspiciousScans;
    private JLabel phishingScans;

    public DashboardFrame(String username) {

        setTitle("PhishGuard - Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel heading = new JLabel(
                "PhishGuard Dashboard",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel welcome = new JLabel(
                "Welcome, " + username,
                SwingConstants.CENTER
        );

        welcome.setFont(new Font("Arial", Font.PLAIN, 18));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 10, 20, 10)
        );

        headerPanel.add(heading);
        headerPanel.add(welcome);

        JPanel statisticsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statisticsPanel.setBorder(
                BorderFactory.createEmptyBorder(30, 50, 30, 50)
        );

        totalScans = createStatisticLabel("Total Scans: 0");
        safeScans = createStatisticLabel("Safe: 0");
        suspiciousScans = createStatisticLabel("Suspicious: 0");
        phishingScans = createStatisticLabel("Phishing: 0");

        statisticsPanel.add(totalScans);
        statisticsPanel.add(safeScans);
        statisticsPanel.add(suspiciousScans);
        statisticsPanel.add(phishingScans);

        JButton scannerButton = new JButton("Open URL Scanner");
        JButton historyButton = new JButton("View Scan History");

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(scannerButton);
        bottomPanel.add(historyButton);

        add(headerPanel, BorderLayout.NORTH);
        add(statisticsPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        scannerButton.addActionListener(e -> {
            URLScannerFrame scanner = new URLScannerFrame(username);
            scanner.setVisible(true);
        });

        historyButton.addActionListener(e -> {
            ScanHistoryFrame history = new ScanHistoryFrame(username);
            history.setVisible(true);
        });

        loadStatistics(username);
    }

    private JLabel createStatisticLabel(String text) {

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        return label;
    }

    private void loadStatistics(String username) {

        try {

            DashboardDAO dashboardDAO = new DashboardDAO();

            int[] statistics =
                    dashboardDAO.getScanStatistics(username);

            totalScans.setText("Total Scans: " + statistics[0]);
            safeScans.setText("Safe: " + statistics[1]);
            suspiciousScans.setText("Suspicious: " + statistics[2]);
            phishingScans.setText("Phishing: " + statistics[3]);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load dashboard statistics: " + e.getMessage()
            );
        }
    }
}
