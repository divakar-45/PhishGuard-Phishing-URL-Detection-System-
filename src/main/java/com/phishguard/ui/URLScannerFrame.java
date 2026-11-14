
package com.phishguard.ui;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class URLScannerFrame extends JFrame {

    private JTextField urlField;

    public URLScannerFrame(String username) {

        setTitle("PhishGuard - URL Scanner");
        setSize(700, 400);
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

        JButton validateButton = new JButton("Validate URL");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(validateButton);

        add(headerPanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        validateButton.addActionListener(e -> validateURL());
    }

    private void validateURL() {

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

            JOptionPane.showMessageDialog(
                this,
                "URL format is valid. Detection analysis is the next step."
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                this,
                "Invalid URL. Please enter a valid HTTP or HTTPS URL."
            );
        }
    }
}
