
package com.phishguard.ui;

import com.phishguard.database.ScanHistoryDAO;
import com.phishguard.database.UserDAO;
import com.phishguard.detection.DetectionEngine;
import com.phishguard.detection.DetectionResult;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

public class URLScannerFrame extends JFrame {

    private JTextField urlField;
    private String username;

    private DetectionEngine detectionEngine;
    private ScanHistoryDAO scanHistoryDAO;
    private UserDAO userDAO;

    public URLScannerFrame(String username) {

        this.username = username;

        detectionEngine = new DetectionEngine();
        scanHistoryDAO = new ScanHistoryDAO();
        userDAO = new UserDAO();

        setTitle("PhishGuard - URL Scanner");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel heading = new JLabel(
                "URL Scanner",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel welcome = new JLabel(
                "Logged in as: " + username,
                SwingConstants.CENTER
        );

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.add(heading);
        headerPanel.add(welcome);

        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        inputPanel.setBorder(
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        );

        JLabel instruction = new JLabel("Enter Website URL:");

        urlField = new JTextField();

        inputPanel.add(instruction);
        inputPanel.add(urlField);

        JButton scanButton = new JButton("Scan URL");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(scanButton);

        add(headerPanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        scanButton.addActionListener(e -> scanURL());
    }

    private void scanURL() {

        String input = urlField.getText().trim();

        if (input.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a URL."
            );

            return;
        }

        try {

            URI uri = new URI(input);

            String scheme = uri.getScheme();

            if (scheme == null
                    || (!scheme.equalsIgnoreCase("http")
                    && !scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null
                    || uri.getHost().isEmpty()) {

                throw new IllegalArgumentException("Invalid URL");
            }

            DetectionResult result =
                    detectionEngine.analyzeURL(input);

            int userId = userDAO.getUserId(username);

            if (userId == -1) {
                throw new IllegalStateException("User ID not found.");
            }

            boolean saved = scanHistoryDAO.saveScan(
                    userId,
                    result.getSubmittedUrl(),
                    result.getRegisteredDomain(),
                    result.getRiskScore(),
                    result.getClassification(),
                    result.getIndicators()
                );
            if (!saved) {

                JOptionPane.showMessageDialog(
                        this,
                        "Scan completed, but the result could not be saved to history.",
                        "Scan History Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            List<String> indicators = result.getIndicators();

            StringBuilder message = new StringBuilder();

            message.append("URL Scan Result\n\n");
            message.append("URL: ").append(result.getSubmittedUrl()).append("\n");
            message.append("Domain: ").append(result.getRegisteredDomain()).append("\n");

            if (result.isTrustedDomain()) {
                message.append("Reason: Trusted domain found in database.\n");
            } else {
                message.append("Reason: Detection rules analysis completed.\n");
            }

            message.append("\nDetected Indicators:\n");

            if (indicators.isEmpty()) {
                message.append("No indicators detected.\n");
            } else {
                for (String indicator : indicators) {
                    message.append("- ").append(indicator).append("\n");
                }
            }

            message.append("\nRisk Score: ")
                    .append(result.getRiskScore())
                    .append("/100");

            message.append("\nClassification: ")
                    .append(result.getClassification());

            JOptionPane.showMessageDialog(
                    this,
                    message.toString(),
                    "Scan Result",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (URISyntaxException | IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid HTTP or HTTPS URL.",
                    "Invalid URL",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to save or retrieve scan information. Please try again later.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to complete the scan. Please try again.",
                    "Scan Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
