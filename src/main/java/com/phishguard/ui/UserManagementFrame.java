
package com.phishguard.ui;

import com.phishguard.database.UserDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UserManagementFrame extends JFrame {

    private final String administratorUsername;
    private final UserDAO userDAO;
    private final DefaultTableModel tableModel;
    private final JTable userTable;

    public UserManagementFrame(String username) {

        administratorUsername = username;
        userDAO = new UserDAO();

        setTitle("PhishGuard - User Management");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(
                "User Management",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setBorder(
                BorderFactory.createEmptyBorder(20, 10, 10, 10)
        );

        add(heading, BorderLayout.NORTH);

        String[] columns = {
                "User ID",
                "Full Name",
                "Username",
                "Email",
                "Role",
                "Account Status",
                "Registration Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        userTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(userTable);

        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 15, 15)
        );

        JButton refreshButton = new JButton("Refresh Users");
        JButton activateButton = new JButton("Activate User");
        JButton deactivateButton = new JButton("Deactivate User");
        JButton deleteButton = new JButton("Delete User");
        JButton closeButton = new JButton("Close");

        buttonPanel.add(refreshButton);
        buttonPanel.add(activateButton);
        buttonPanel.add(deactivateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadUsers());

        activateButton.addActionListener(e ->
                updateSelectedUserStatus("ACTIVE")
        );

        deactivateButton.addActionListener(e ->
                updateSelectedUserStatus("INACTIVE")
        );

        deleteButton.addActionListener(e -> deleteSelectedUser());

        closeButton.addActionListener(e -> dispose());

        if (!isAdministrator()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Access denied. Administrator privileges required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );

            dispose();
            return;
        }

        loadUsers();
    }

    private boolean isAdministrator() {

        try {

            String role = userDAO.getUserRole(administratorUsername);

            return "ADMIN".equalsIgnoreCase(role);

        } catch (SQLException exception) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to verify administrator access: "
                            + exception.getMessage(),
                    "Authorization Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }
    }

    private void loadUsers() {

        tableModel.setRowCount(0);

        try {

            List<Object[]> users = userDAO.getAllUsers();

            for (Object[] user : users) {
                tableModel.addRow(user);
            }

        } catch (SQLException exception) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load users: " + exception.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String getSelectedUsername() {

        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a user first.",
                    "No User Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        String role = String.valueOf(
                tableModel.getValueAt(selectedRow, 4)
        );

        if (!"USER".equalsIgnoreCase(role)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Administrator accounts cannot be modified.",
                    "Operation Restricted",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        return String.valueOf(
                tableModel.getValueAt(selectedRow, 2)
        );
    }

    private void updateSelectedUserStatus(String status) {

        String username = getSelectedUsername();

        if (username == null) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Change account status of " + username
                        + " to " + status + "?",
                "Confirm Account Status",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            int administratorId =
                    userDAO.getUserId(administratorUsername);

            if (administratorId == -1) {
                throw new SQLException("Administrator account not found.");
            }

            userDAO.updateUserStatus(
                    username,
                    status,
                    administratorId
            );

            JOptionPane.showMessageDialog(
                    this,
                    "User account status updated successfully."
            );

            loadUsers();

        } catch (SQLException exception) {

            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Operation Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void deleteSelectedUser() {

        String username = getSelectedUsername();

        if (username == null) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the account "
                        + username + "?\n\n"
                        + "Deletion will be blocked if dependent records exist.",
                "Confirm User Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            int administratorId =
                    userDAO.getUserId(administratorUsername);

            if (administratorId == -1) {
                throw new SQLException("Administrator account not found.");
            }

            userDAO.deleteUser(username, administratorId);

            JOptionPane.showMessageDialog(
                    this,
                    "User account deleted successfully."
            );

            loadUsers();

        } catch (SQLException exception) {

            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Deletion Failed",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }
}
