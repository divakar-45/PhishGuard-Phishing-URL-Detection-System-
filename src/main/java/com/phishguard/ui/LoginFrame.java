
package com.phishguard.ui;

import javax.swing.*;
import java.awt.*;
import com.phishguard.database.UserDAO;

public class LoginFrame extends JFrame {

    public LoginFrame() {

        setTitle("PhishGuard - Login");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel heading = new JLabel("PhishGuard", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel subtitle = new JLabel(
                "Phishing URL Detection System",
                SwingConstants.CENTER
        );

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.add(heading);
        headerPanel.add(subtitle);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));
        formPanel.setBorder(
                BorderFactory.createEmptyBorder(30, 50, 30, 50)
        );

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Create Account");

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        formPanel.add(registerButton);
        formPanel.add(loginButton);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);

        registerButton.addActionListener(e -> {
            RegistrationFrame registrationFrame =
                    new RegistrationFrame();
            registrationFrame.setVisible(true);
        });

        loginButton.addActionListener(e -> {

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter username and password."
                );

                return;
            }

            try {

                UserDAO userDAO = new UserDAO();

                boolean authenticated =
                        userDAO.authenticateUser(username, password);

                if (!authenticated) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Invalid username or password, or inactive account."
                    );

                    return;
                }

                String role = userDAO.getUserRole(username);

                if (role == null) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Unable to retrieve user role.",
                            "Login Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                    return;
                }

                if ("ADMIN".equalsIgnoreCase(role)) {

                    AdminDashboardFrame adminDashboard =
                            new AdminDashboardFrame(username);

                    adminDashboard.setVisible(true);

                } else if ("USER".equalsIgnoreCase(role)) {

                    DashboardFrame dashboard =
                            new DashboardFrame(username);

                    dashboard.setVisible(true);

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "Unauthorized account role.",
                            "Access Denied",
                            JOptionPane.ERROR_MESSAGE
                    );

                    return;
                }

                dispose();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Login failed. Please try again.",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
