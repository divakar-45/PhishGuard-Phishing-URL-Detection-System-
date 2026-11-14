
package com.phishguard.ui;
import com.phishguard.database.UserDAO;

import javax.swing.*;
import java.awt.*;

public class RegistrationFrame extends JFrame {

    public RegistrationFrame() {

        setTitle("PhishGuard - User Registration");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel heading = new JLabel("Create Account", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 26));

        JPanel headerPanel = new JPanel();
        headerPanel.add(heading);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JTextField nameField = new JTextField();
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back to Login");

        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);

        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        formPanel.add(backButton);
        formPanel.add(registerButton);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);

        backButton.addActionListener(e -> dispose());

       registerButton.addActionListener(e -> {

    String name = nameField.getText().trim();
    String username = usernameField.getText().trim();
    String email = emailField.getText().trim();
    String password = new String(passwordField.getPassword());

    if (name.isEmpty() || username.isEmpty()
            || email.isEmpty() || password.isEmpty()) {

        JOptionPane.showMessageDialog(
            this,
            "Please fill all fields."
        );

        return;
    }

    try {
        UserDAO userDAO = new UserDAO();

        userDAO.registerUser(name, username, email, password);

        JOptionPane.showMessageDialog(
            this,
            "Registration successful!"
        );

        dispose();

    } catch (java.sql.SQLIntegrityConstraintViolationException ex) {

        JOptionPane.showMessageDialog(
            this,
            "Username or email already exists."
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
            this,
            "Registration failed: " + ex.getMessage()
        );
    }
});
    }
}
