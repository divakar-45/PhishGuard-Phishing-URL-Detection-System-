
package com.phishguard.ui;

import com.phishguard.database.ScanHistoryDAO;
import com.phishguard.database.UserDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ScanHistoryFrame extends JFrame {

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private ScanHistoryDAO scanHistoryDAO;
    private int userId;

    public ScanHistoryFrame(String username) {

        scanHistoryDAO = new ScanHistoryDAO();

        setTitle("PhishGuard - Scan History");
        setSize(1100, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        try {

            UserDAO userDAO = new UserDAO();
            userId = userDAO.getUserId(username);

            if (userId == -1) {
                throw new SQLException("User ID not found.");
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to identify user. Please try again.",
                    "User Identification Error",
                    JOptionPane.ERROR_MESSAGE
            );

            dispose();
            return;
        }

        JLabel heading = new JLabel(
                "Scan History",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 26));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(heading, BorderLayout.CENTER);

        searchField = new JTextField();

        JButton searchButton = new JButton("Search");
        JButton resetButton = new JButton("Show All");
        JButton previewButton = new JButton("Preview Selected Scan");

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );

        searchPanel.add(searchField, BorderLayout.CENTER);

        JPanel searchButtons = new JPanel();
        searchButtons.add(searchButton);
        searchButtons.add(resetButton);

        searchPanel.add(searchButtons, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        String[] columns = {
            "Scan ID",
            "Submitted URL",
            "Registered Domain",
            "Risk Score",
            "Classification",
            "Scan Timestamp"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);

        historyTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane = new JScrollPane(historyTable);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(previewButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> searchHistory());

        resetButton.addActionListener(e -> {
            searchField.setText("");
            loadHistory();
        });

        previewButton.addActionListener(e -> previewSelectedScan());

        loadHistory();
    }

    private void loadHistory() {

        try {

            List<Object[]> scans =
                    scanHistoryDAO.getUserScans(userId);

            displayScans(scans);

        } catch (SQLException e) {

            showDatabaseError();
        }
    }

    private void searchHistory() {

        String searchText = searchField.getText().trim();

        try {

            List<Object[]> scans =
                    scanHistoryDAO.searchUserScans(
                            userId,
                            searchText
                    );

            displayScans(scans);

        } catch (SQLException e) {

            showDatabaseError();
        }
    }

    private void displayScans(List<Object[]> scans) {

        tableModel.setRowCount(0);

        for (Object[] scan : scans) {
            tableModel.addRow(scan);
        }
    }

    private void previewSelectedScan() {

        int selectedRow = historyTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a scan record first."
            );

            return;
        }

        StringBuilder details = new StringBuilder();

        details.append("Scan ID: ")
                .append(tableModel.getValueAt(selectedRow, 0))
                .append("\n");

        details.append("Submitted URL: ")
                .append(tableModel.getValueAt(selectedRow, 1))
                .append("\n");

        details.append("Registered Domain: ")
                .append(tableModel.getValueAt(selectedRow, 2))
                .append("\n");

        details.append("Risk Score: ")
                .append(tableModel.getValueAt(selectedRow, 3))
                .append("\n");

        details.append("Classification: ")
                .append(tableModel.getValueAt(selectedRow, 4))
                .append("\n");

        details.append("Timestamp: ")
                .append(tableModel.getValueAt(selectedRow, 5));

        JOptionPane.showMessageDialog(
                this,
                details.toString(),
                "Scan Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showDatabaseError() {

        JOptionPane.showMessageDialog(
                this,
                "Unable to access scan history. Please try again later.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
