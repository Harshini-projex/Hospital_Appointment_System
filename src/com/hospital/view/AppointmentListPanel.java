package com.hospital.view;

import com.hospital.exception.HospitalException;
import com.hospital.model.Appointment;
import com.hospital.service.AppointmentService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.hospital.view.MainDashboard.*;
import static com.hospital.view.PatientPanel.*;

/**
 * Premium appointment list panel with color-coded status, zebra grid, and filter bar.
 */
public class AppointmentListPanel extends JPanel {

    private final AppointmentService appointmentService;
    private final MainDashboard      dashboard;

    private final JTable            table;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> rowSorter;

    private final JTextField     txtSearch;
    private final JComboBox<String> cmbDoctorFilter;
    private final JButton        btnCancel;
    private final JButton        btnDelete;
    private final JButton        btnRefresh;

    private static final String[] DOCTOR_FILTERS = {
        "All Doctors",
        "Dr. Alice Smith  |  Cardiology",
        "Dr. Bob Johnson  |  Pediatrics",
        "Dr. Carol White  |  Dermatology",
        "Dr. David Green  |  Neurology",
        "Dr. Emily Taylor |  General Medicine"
    };

    public AppointmentListPanel(AppointmentService as, MainDashboard db) {
        this.appointmentService = as;
        this.dashboard          = db;

        setLayout(new BorderLayout(0, 12));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        txtSearch       = styledField("Search by patient, doctor, date, time...");
        cmbDoctorFilter = buildDoctorCombo();
        btnCancel  = dangerButton("\u274C  Cancel Appointment");
        btnDelete  = ghostButton("\uD83D\uDDD1  Delete Record");
        btnRefresh = primaryButton("\u21BB  Refresh");

        // Build table
        String[] cols = {"ID", "Patient ID", "Patient Name", "Doctor", "Date", "Time", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = buildStyledTable(tableModel);
        applyStatusRenderer();

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        add(buildFilterBar(), BorderLayout.NORTH);
        add(styledScroll(table), BorderLayout.CENTER);
        add(buildActionBar(),    BorderLayout.SOUTH);

        setupListeners();
        refreshTable();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FILTER BAR
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new GridBagLayout());
        bar.setBackground(BG_PANEL);
        bar.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 12);

        JLabel lblFilter = new JLabel("\uD83D\uDD0D  Filter Appointments");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFilter.setForeground(TEXT_PRIMARY);
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        bar.add(lblFilter, g);

        g.gridx = 1; g.weightx = 0.5;
        bar.add(txtSearch, g);

        JLabel lblDoc = new JLabel("Doctor:");
        lblDoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDoc.setForeground(TEXT_MUTED);
        g.gridx = 2; g.weightx = 0;
        bar.add(lblDoc, g);

        g.gridx = 3; g.weightx = 0.4;
        bar.add(cmbDoctorFilter, g);

        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ACTION BAR
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Left: record count hint
        JLabel hint = new JLabel("Select a row to cancel or permanently delete an appointment.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(new Color(71, 85, 105));
        bar.add(hint, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnRefresh);
        right.add(btnDelete);
        right.add(btnCancel);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  STATUS COLUMN RENDERER
    // ─────────────────────────────────────────────────────────────────────────
    private void applyStatusRenderer() {
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(new EmptyBorder(4, 10, 4, 10));

                if (!sel) {
                    String status = val == null ? "" : val.toString();
                    if ("Scheduled".equalsIgnoreCase(status)) {
                        lbl.setBackground(new Color(22, 60, 36));
                        lbl.setForeground(ACCENT_GREEN);
                        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                        lbl.setText("  \u25CF  Scheduled");
                    } else if ("Cancelled".equalsIgnoreCase(status)) {
                        lbl.setBackground(new Color(60, 20, 20));
                        lbl.setForeground(ACCENT_RED);
                        lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
                        lbl.setText("  \u25CB  Cancelled");
                    } else {
                        lbl.setBackground(BG_CARD);
                        lbl.setForeground(TEXT_MUTED);
                    }
                    lbl.setOpaque(true);
                }
                return lbl;
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LISTENERS
    // ─────────────────────────────────────────────────────────────────────────
    private void setupListeners() {
        btnRefresh.addActionListener(e -> refreshTable());

        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { showWarn("Please select an appointment to cancel."); return; }

            int modelRow = table.convertRowIndexToModel(row);
            int id       = (Integer) tableModel.getValueAt(modelRow, 0);
            String pName = (String)  tableModel.getValueAt(modelRow, 2);
            String doc   = (String)  tableModel.getValueAt(modelRow, 3);
            String date  = tableModel.getValueAt(modelRow, 4).toString();
            String time  = (String)  tableModel.getValueAt(modelRow, 5);
            String status = (String) tableModel.getValueAt(modelRow, 6);

            if ("Cancelled".equalsIgnoreCase(status)) {
                showWarn("This appointment is already cancelled.");
                return;
            }

            int ok = JOptionPane.showConfirmDialog(this,
                "<html>Cancel appointment for <b>" + pName + "</b>?<br>" +
                "<font color='#aaaaaa'>" + doc + " — " + date + " at " + time + "</font></html>",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (ok == JOptionPane.YES_OPTION) {
                try {
                    appointmentService.cancelAppointment(id);
                    showSuccess("Appointment successfully cancelled.");
                    refreshTable();
                } catch (HospitalException ex) {
                    showError(ex.getMessage());
                }
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { showWarn("Please select an appointment to delete."); return; }

            int modelRow = table.convertRowIndexToModel(row);
            int id       = (Integer) tableModel.getValueAt(modelRow, 0);
            String pName = (String)  tableModel.getValueAt(modelRow, 2);

            int ok = JOptionPane.showConfirmDialog(this,
                "<html>Permanently delete the appointment record for <b>" + pName + "</b>?<br>" +
                "<font color='#ef4444'>This action cannot be undone.</font></html>",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (ok == JOptionPane.YES_OPTION) {
                try {
                    appointmentService.deleteAppointment(id);
                    showSuccess("Appointment record deleted.");
                    refreshTable();
                } catch (HospitalException ex) {
                    showError(ex.getMessage());
                }
            }
        });

        // Composite filter
        cmbDoctorFilter.addActionListener(e -> applyFilters());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
    }

    private void applyFilters() {
        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
        String search = txtSearch.getText().trim();
        String doctor = (String) cmbDoctorFilter.getSelectedItem();

        if (!search.isEmpty()) filters.add(RowFilter.regexFilter("(?i)" + search));
        if (doctor != null && !doctor.equals("All Doctors"))
            filters.add(RowFilter.regexFilter("\\Q" + doctor + "\\E", 3));

        rowSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PUBLIC REFRESH
    // ─────────────────────────────────────────────────────────────────────────
    public void refreshTable() {
        tableModel.setRowCount(0);
        try {
            for (Appointment a : appointmentService.getAllAppointments()) {
                tableModel.addRow(new Object[]{
                    a.getId(), a.getPatientId(), a.getPatientName(),
                    a.getDoctorName(), a.getAppointmentDate(),
                    a.getAppointmentTime(), a.getStatus()
                });
            }
            dashboard.updateStatusBar();
        } catch (HospitalException e) {
            showError("Error loading appointments: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────────────────────────────────
    private JComboBox<String> buildDoctorCombo() {
        JComboBox<String> c = new JComboBox<>(DOCTOR_FILTERS);
        c.setBackground(BG_CARD);
        c.setForeground(TEXT_PRIMARY);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        return c;
    }
}
