
package com.phishguard.ui;

import com.phishguard.database.AdminLogDAO;
import com.phishguard.database.UserDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AdminLogsFrame extends JFrame {

    private final AdminLogDAO adminLogDAO;
    private final String username;

    private DefaultTableModel tableModel;
    private JTable logsTable;

    public AdminLogsFrame(String username) {

        this.username = username;
        this.adminLogDAO = new AdminLogDAO();

        setTitle("PhishGuard - Admin Activity Logs");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(
                "Admin Activity Logs",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel welcome = new JLabel(
                "Administrator: " + username,
                SwingConstants.CENTER
        );

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        headerPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 10, 15, 10)
        );

        headerPanel.add(heading);
        headerPanel.add(welcome);

        add(headerPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{
                        "Log ID",
                        "Administrator",
                        "Action Performed",
                        "Date and Time"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        logsTable = new JTable(tableModel);

        logsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        add(new JScrollPane(logsTable), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh Logs");
        JButton closeButton = new JButton("Close");

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadLogs());
        closeButton.addActionListener(e -> dispose());

        if (!isAdministrator()) {

            JOptionPane.showMessageDialog(
                    null,
                    "Administrator access required.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );

            dispose();
            return;
        }

        loadLogs();
    }

    private boolean isAdministrator() {

        try {

            UserDAO userDAO = new UserDAO();

            String role = userDAO.getUserRole(username);

            return "ADMIN".equalsIgnoreCase(role);

        } catch (Exception e) {
            return false;
        }
    }

    private void loadLogs() {

        try {

            List<Object[]> logs = adminLogDAO.getAllAdminLogs();

            tableModel.setRowCount(0);

            for (Object[] log : logs) {
                tableModel.addRow(log);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load administrative activity logs.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
