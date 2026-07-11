package com.hospital.view;

import com.hospital.util.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Configuration dialog displayed when database connections fail.
 * Prompts the user to review and update database connection details.
 */
public class DBConfigDialog extends JDialog {
    private final JTextField txtHost;
    private final JTextField txtPort;
    private final JTextField txtDbName;
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private boolean isConnectionSuccessful = false;

    public DBConfigDialog(Frame parent) {
        super(parent, "Database Connection Settings", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 320);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Layout setup
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(contentPane);

        // Header message
        JLabel lblHeader = new JLabel("<html><b>Connection Failed!</b> Please configure your local MySQL database settings:</html>");
        lblHeader.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHeader.setForeground(new Color(220, 53, 69)); // Bootstrap Red
        contentPane.add(lblHeader, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Inputs initialization
        txtHost = new JTextField(DBConnection.getHost());
        txtPort = new JTextField(DBConnection.getPort());
        txtDbName = new JTextField(DBConnection.getDbName());
        txtUsername = new JTextField(DBConnection.getUsername());
        txtPassword = new JPasswordField(DBConnection.getPassword());

        // Field constraints helper
        addFormField(formPanel, "MySQL Host:", txtHost, gbc, 0);
        addFormField(formPanel, "MySQL Port:", txtPort, gbc, 1);
        addFormField(formPanel, "Database Name:", txtDbName, gbc, 2);
        addFormField(formPanel, "Username:", txtUsername, gbc, 3);
        addFormField(formPanel, "Password:", txtPassword, gbc, 4);

        contentPane.add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCancel = new JButton("Exit");
        JButton btnSave = new JButton("Test & Connect");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnCancel.addActionListener(e -> {
            isConnectionSuccessful = false;
            dispose();
        });

        btnSave.addActionListener(e -> {
            String host = txtHost.getText().trim();
            String port = txtPort.getText().trim();
            String dbName = txtDbName.getText().trim();
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());

            if (host.isEmpty() || port.isEmpty() || dbName.isEmpty() || username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields (except password) must be filled.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Save properties configurations
                DBConnection.saveConfig(host, port, dbName, username, password);
                
                // Show loading indicator contextually
                btnSave.setText("Connecting...");
                btnSave.setEnabled(false);

                // Run connection test and self healing
                DBConnection.testAndInitialize();
                
                isConnectionSuccessful = true;
                JOptionPane.showMessageDialog(this, "Connection established successfully! Database configured.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to connect to MySQL database:\n" + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
                btnSave.setText("Test & Connect");
                btnSave.setEnabled(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to save config.properties file:\n" + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                btnSave.setText("Test & Connect");
                btnSave.setEnabled(true);
            }
        });

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    public boolean isConnectionSuccessful() {
        return isConnectionSuccessful;
    }
}
