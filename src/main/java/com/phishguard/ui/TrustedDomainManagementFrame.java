

package com.phishguard.ui;

import com.phishguard.database.TrustedDomainDAO;
import com.phishguard.database.UserDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class TrustedDomainManagementFrame extends JFrame {

    private final String username;
    private final int administratorId;
    private final TrustedDomainDAO trustedDomainDAO;

    private JTable domainTable;
    private DefaultTableModel tableModel;
    private JTextField domainField;
    private JTextField categoryField;

    public TrustedDomainManagementFrame(String username) {

        this.username = username;
        this.trustedDomainDAO = new TrustedDomainDAO();

        int id;

        try {
            UserDAO userDAO = new UserDAO();

            id = userDAO.getUserId(username);

            if (id == -1) {
                throw new SQLException("Administrator not found.");
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Unable to verify administrator account.", e
            );
        }

        this.administratorId = id;

        setTitle("PhishGuard - Trusted Domain Management");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(
                "Trusted Domain Management",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 25));
        add(heading, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{
                        "Domain ID",
                        "Domain Name",
                        "Category",
                        "Date Added",
                        "Added By"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        domainTable = new JTable(tableModel);
        domainTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane = new JScrollPane(domainTable);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        inputPanel.setBorder(
                BorderFactory.createTitledBorder("Domain Information")
        );

        domainField = new JTextField();
        categoryField = new JTextField();

        inputPanel.add(new JLabel("Domain Name:"));
        inputPanel.add(domainField);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryField);

        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JButton addButton = new JButton("Add Domain");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton refreshButton = new JButton("Refresh");
        JButton clearButton = new JButton("Clear Fields");

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 10)
        );

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addDomain());
        updateButton.addActionListener(e -> updateDomain());
        deleteButton.addActionListener(e -> deleteDomain());
        refreshButton.addActionListener(e -> loadDomains());
        clearButton.addActionListener(e -> clearFields());

        domainTable.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int selectedRow = domainTable.getSelectedRow();

                if (selectedRow >= 0) {
                    domainField.setText(
                            tableModel.getValueAt(selectedRow, 1).toString()
                    );

                    Object category =
                            tableModel.getValueAt(selectedRow, 2);

                    categoryField.setText(
                            category == null ? "" : category.toString()
                    );
                }
            }
        });

        loadDomains();
    }

    private void loadDomains() {

        try {

            List<Object[]> domains =
                    trustedDomainDAO.getAllTrustedDomains();

            tableModel.setRowCount(0);

            for (Object[] domain : domains) {
                tableModel.addRow(domain);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load trusted domains.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void addDomain() {

        String domain = domainField.getText().trim();
        String category = categoryField.getText().trim();

        if (!isValidDomain(domain)) {
            showValidationError();
            return;
        }

        try {

            boolean added = trustedDomainDAO.addTrustedDomain(
                    domain,
                    category,
                    administratorId
            );

            if (added) {

                JOptionPane.showMessageDialog(
                        this,
                        "Trusted domain added successfully."
                );

                clearFields();
                loadDomains();
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to add domain. It may already exist or the database operation failed.",
                    "Add Domain Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateDomain() {

        int selectedRow = domainTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a domain to update."
            );
            return;
        }

        String domain = domainField.getText().trim();
        String category = categoryField.getText().trim();

        if (!isValidDomain(domain)) {
            showValidationError();
            return;
        }

        int domainId = Integer.parseInt(
                tableModel.getValueAt(selectedRow, 0).toString()
        );

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Update the selected trusted domain?",
                "Confirm Update",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean updated = trustedDomainDAO.updateTrustedDomain(
                    domainId,
                    domain,
                    category,
                    administratorId
            );

            if (updated) {

                JOptionPane.showMessageDialog(
                        this,
                        "Trusted domain updated successfully."
                );

                clearFields();
                loadDomains();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Selected domain was not found."
                );
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to update domain. Check for duplicate domain names.",
                    "Update Domain Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void deleteDomain() {

        int selectedRow = domainTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a domain to delete."
            );
            return;
        }

        int domainId = Integer.parseInt(
                tableModel.getValueAt(selectedRow, 0).toString()
        );

        String domain = tableModel.getValueAt(
                selectedRow, 1
        ).toString();

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Delete trusted domain: " + domain + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean deleted = trustedDomainDAO.deleteTrustedDomain(
                    domainId,
                    administratorId
            );

            if (deleted) {

                JOptionPane.showMessageDialog(
                        this,
                        "Trusted domain deleted successfully."
                );

                clearFields();
                loadDomains();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Selected domain was not found."
                );
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to delete domain. The database operation failed.",
                    "Delete Domain Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean isValidDomain(String domain) {

        if (domain == null || domain.isEmpty()
                || domain.length() > 255) {
            return false;
        }

        if (domain.contains("://")
                || domain.contains("/")
                || domain.contains(":")
                || domain.contains("@")
                || domain.contains(" ")) {
            return false;
        }

        return domain.matches(
                "(?i)(?=.{1,255}$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}"
        );
    }

    private void showValidationError() {

        JOptionPane.showMessageDialog(
                this,
                "Enter a valid domain name, such as example.com.",
                "Invalid Domain",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void clearFields() {

        domainField.setText("");
        categoryField.setText("");
        domainTable.clearSelection();
    }
}
