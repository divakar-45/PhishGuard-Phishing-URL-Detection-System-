
package com.phishguard.ui;

import com.phishguard.database.ReportDAO;
import com.phishguard.database.ScanHistoryDAO;
import com.phishguard.database.UserDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ReportFrame extends JFrame {

    private ReportDAO reportDAO;
    private ScanHistoryDAO scanHistoryDAO;
    private int userId;

    private JTable scanTable;
    private JTable reportTable;

    private DefaultTableModel scanModel;
    private DefaultTableModel reportModel;

    public ReportFrame(String username) {

        reportDAO = new ReportDAO();
        scanHistoryDAO = new ScanHistoryDAO();

        setTitle("PhishGuard - Report Management");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

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
                "Report Management",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 26));
        heading.setBorder(
                BorderFactory.createEmptyBorder(15, 10, 15, 10)
        );

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Available Scans", createScanPanel());
        tabs.addTab("Generated Reports", createReportPanel());

        add(heading, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        loadScans();
        loadReports();
    }

    private JPanel createScanPanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] columns = {
            "Scan ID",
            "Submitted URL",
            "Domain",
            "Risk Score",
            "Classification",
            "Scan Timestamp"
        };

        scanModel = createTableModel(columns);
        scanTable = new JTable(scanModel);

        scanTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JButton createButton = new JButton("Create Report");
        JButton refreshButton = new JButton("Refresh Scans");

        JPanel buttons = new JPanel();
        buttons.add(createButton);
        buttons.add(refreshButton);

        createButton.addActionListener(e -> createReport());
        refreshButton.addActionListener(e -> loadScans());

        panel.add(new JScrollPane(scanTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReportPanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] columns = {
            "Report ID",
            "Scan ID",
            "Report Title",
            "Generated At",
            "Submitted URL",
            "Domain",
            "Risk Score",
            "Classification"
        };

        reportModel = createTableModel(columns);
        reportTable = new JTable(reportModel);

        reportTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JButton previewButton = new JButton("Preview Report");
        JButton refreshButton = new JButton("Refresh Reports");

        JPanel buttons = new JPanel();
        buttons.add(previewButton);
        buttons.add(refreshButton);

        previewButton.addActionListener(e -> previewReport());
        refreshButton.addActionListener(e -> loadReports());

        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private DefaultTableModel createTableModel(String[] columns) {

        return new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void loadScans() {

        try {
            List<Object[]> scans =
                    scanHistoryDAO.getUserScans(userId);

            scanModel.setRowCount(0);

            for (Object[] scan : scans) {
                scanModel.addRow(scan);
            }

        } catch (SQLException e) {
            showDatabaseError();
        }
    }

    private void loadReports() {

        try {
            List<Object[]> reports =
                    reportDAO.getUserReports(userId);

            reportModel.setRowCount(0);

            for (Object[] report : reports) {
                reportModel.addRow(report);
            }

        } catch (SQLException e) {
            showDatabaseError();
        }
    }

    private void createReport() {

        int selectedRow = scanTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a scan record first."
            );
            return;
        }

        int scanId = Integer.parseInt(
                scanModel.getValueAt(selectedRow, 0).toString()
        );

        String reportTitle = JOptionPane.showInputDialog(
                this,
                "Enter Report Title:",
                "Create Report",
                JOptionPane.PLAIN_MESSAGE
        );

        if (reportTitle == null) {
            return;
        }

        if (reportTitle.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Report title cannot be empty."
            );
            return;
        }

        try {
            boolean created = reportDAO.createReport(
                    userId,
                    scanId,
                    reportTitle
            );

            if (created) {
                JOptionPane.showMessageDialog(
                        this,
                        "Report created successfully."
                );

                loadReports();

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Unable to create report. Scan record not found."
                );
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "A report may already exist for this scan, "
                            + "or the database operation failed.",
                    "Report Creation Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void previewReport() {

        int selectedRow = reportTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a report first."
            );
            return;
        }

        int reportId = Integer.parseInt(
                reportModel.getValueAt(selectedRow, 0).toString()
        );

        try {
            Object[] report =
                    reportDAO.getReportDetails(userId, reportId);

            if (report == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Report not found."
                );
                return;
            }

            StringBuilder details = new StringBuilder();

            details.append("Report ID: ").append(report[0]).append("\n");
            details.append("Scan ID: ").append(report[1]).append("\n");
            details.append("Report Title: ").append(report[2]).append("\n");
            details.append("Generated At: ").append(report[3]).append("\n\n");

            details.append("Submitted URL: ").append(report[4]).append("\n");
            details.append("Registered Domain: ").append(report[5]).append("\n");
            details.append("Risk Score: ").append(report[6]).append("/100\n");
            details.append("Classification: ").append(report[7]).append("\n\n");

            details.append("Detected Indicators:\n");

            String indicators = (String) report[8];

            if (indicators == null) {
                details.append("Not available for this scan.");
            } else if (indicators.trim().isEmpty()) {
                details.append("No indicators detected.");
            } else {
                details.append(indicators);
            }

            JTextArea textArea = new JTextArea(
                    details.toString(),
                    20,
                    70
            );

            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setCaretPosition(0);

            JOptionPane.showMessageDialog(
                    this,
                    new JScrollPane(textArea),
                    "Report Preview",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {
            showDatabaseError();
        }
    }

    private void showDatabaseError() {

        JOptionPane.showMessageDialog(
                this,
                "Unable to access report data. Please try again later.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
