package com.hospital.view;

import com.hospital.exception.HospitalException;
import com.hospital.model.Patient;
import com.hospital.service.PatientService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import static com.hospital.view.MainDashboard.*;

/**
 * Patient management panel with modern form, search bar, and premium styled data grid.
 */
public class PatientPanel extends JPanel {

    private final PatientService patientService;
    private final MainDashboard  dashboard;

    // Form inputs
    private final JTextField        txtName;
    private final JSpinner          spnAge;
    private final JComboBox<String> cmbGender;
    private final JTextField        txtPhone;
    private final JTextField        txtEmail;

    // Table
    private final JTable            table;
    private final DefaultTableModel tableModel;
    private final JTextField        txtSearch;
    private final TableRowSorter<DefaultTableModel> rowSorter;

    // Buttons
    private final JButton btnRegister;
    private final JButton btnUpdate;
    private final JButton btnDelete;
    private final JButton btnClear;

    private Integer selectedPatientId = null;

    public PatientPanel(PatientService patientService, MainDashboard dashboard) {
        this.patientService = patientService;
        this.dashboard      = dashboard;

        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // ── Initialise all inputs/buttons first ───────────────────────────
        txtName   = styledField("Full name of the patient");
        spnAge    = new JSpinner(new SpinnerNumberModel(25, 1, 120, 1));
        styleSpinner(spnAge);
        cmbGender = styledCombo(new JComboBox<>(new String[]{"Male", "Female", "Other"}));
        txtPhone  = styledField("10-digit mobile number");
        txtEmail  = styledField("name@example.com");

        btnRegister = primaryButton("Register Patient");
        btnUpdate   = ghostButton("Update Details");
        btnClear    = ghostButton("Clear");
        btnDelete   = dangerButton("Delete Selected");
        btnUpdate.setEnabled(false);

        // Search field
        txtSearch = styledField("Search by name, phone or email...");

        // Table model
        String[] cols = {"ID", "Name", "Age", "Gender", "Phone", "Email"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = buildStyledTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        // ── Build layout ─────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildFormPanel(), buildListPanel());
        split.setDividerLocation(370);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBackground(BG_DARK);
        split.setOpaque(false);

        add(split, BorderLayout.CENTER);

        setupListeners();
        refreshTable();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FORM PANEL (left side)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(22, 22, 22, 22)
        ));

        panel.add(sectionTitle("Patient Details"), BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.gridx = 0;

        addField(fields, g, 0, "Full Name",         txtName);
        addField(fields, g, 1, "Age (1–120)",        spnAge);
        addField(fields, g, 2, "Gender",             cmbGender);
        addField(fields, g, 3, "Phone (10 digits)",  txtPhone);
        addField(fields, g, 4, "Email Address",      txtEmail);

        // Filler to push fields to top
        g.gridy = 10; g.weighty = 1;
        fields.add(Box.createVerticalGlue(), g);

        panel.add(fields, BorderLayout.CENTER);

        JPanel btns = new JPanel(new GridLayout(1, 3, 8, 0));
        btns.setOpaque(false);
        btns.setBorder(new EmptyBorder(12, 0, 0, 0));
        btns.add(btnRegister);
        btns.add(btnUpdate);
        btns.add(btnClear);
        panel.add(btns, BorderLayout.SOUTH);

        return panel;
    }

    private void addField(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_MUTED);

        g.gridy = row * 2;     g.insets = new Insets(8, 0, 2, 0);  p.add(lbl,   g);
        g.gridy = row * 2 + 1; g.insets = new Insets(0, 0, 0, 0);  p.add(field, g);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LIST PANEL (right side)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        // Top row: title + search
        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);
        topRow.add(sectionTitle("Registered Patients"), BorderLayout.WEST);
        topRow.add(txtSearch, BorderLayout.CENTER);
        panel.add(topRow, BorderLayout.NORTH);

        // Table inside scroll pane
        JScrollPane scroll = styledScroll(table);
        panel.add(scroll, BorderLayout.CENTER);

        // Delete button
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.setOpaque(false);
        south.add(btnDelete);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LISTENERS
    // ─────────────────────────────────────────────────────────────────────────
    private void setupListeners() {
        btnRegister.addActionListener(e -> {
            try {
                Patient p = new Patient(
                    txtName.getText().trim(), (Integer) spnAge.getValue(),
                    (String) cmbGender.getSelectedItem(),
                    txtPhone.getText().trim(), txtEmail.getText().trim()
                );
                patientService.registerPatient(p);
                showSuccess("Patient registered successfully!");
                clearForm();
                refreshTable();
                dashboard.refreshAppointmentPatientCombos();
            } catch (HospitalException ex) { showError(ex.getMessage()); }
        });

        btnUpdate.addActionListener(e -> {
            if (selectedPatientId == null) return;
            try {
                Patient p = new Patient(
                    selectedPatientId,
                    txtName.getText().trim(), (Integer) spnAge.getValue(),
                    (String) cmbGender.getSelectedItem(),
                    txtPhone.getText().trim(), txtEmail.getText().trim()
                );
                patientService.updatePatient(p);
                showSuccess("Patient details updated!");
                clearForm();
                refreshTable();
                dashboard.refreshAppointmentPatientCombos();
            } catch (HospitalException ex) { showError(ex.getMessage()); }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int mr = table.convertRowIndexToModel(table.getSelectedRow());
                selectedPatientId = (Integer) tableModel.getValueAt(mr, 0);
                txtName.setText((String)  tableModel.getValueAt(mr, 1));
                spnAge.setValue(          tableModel.getValueAt(mr, 2));
                cmbGender.setSelectedItem(tableModel.getValueAt(mr, 3));
                txtPhone.setText((String) tableModel.getValueAt(mr, 4));
                txtEmail.setText((String) tableModel.getValueAt(mr, 5));
                btnRegister.setEnabled(false);
                btnUpdate.setEnabled(true);
            }
        });

        btnClear.addActionListener(e -> clearForm());

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { showWarn("Please select a patient from the table to delete."); return; }
            int mr  = table.convertRowIndexToModel(row);
            int id  = (Integer) tableModel.getValueAt(mr, 0);
            String name = (String) tableModel.getValueAt(mr, 1);

            int ok = JOptionPane.showConfirmDialog(this,
                "<html>Delete patient <b>" + name + "</b>?<br>" +
                "<font color='#ef4444'>This will also remove all their appointments.</font></html>",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (ok == JOptionPane.YES_OPTION) {
                try {
                    patientService.deletePatient(id);
                    showSuccess("Patient deleted successfully.");
                    clearForm();
                    refreshTable();
                    dashboard.refreshAppointmentsTable();
                    dashboard.refreshAppointmentPatientCombos();
                } catch (HospitalException ex) { showError(ex.getMessage()); }
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String s = txtSearch.getText().trim();
                rowSorter.setRowFilter(s.isEmpty() ? null : RowFilter.regexFilter("(?i)" + s));
            }
            @Override public void insertUpdate(DocumentEvent e)  { filter(); }
            @Override public void removeUpdate(DocumentEvent e)  { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PUBLIC
    // ─────────────────────────────────────────────────────────────────────────
    public void clearForm() {
        txtName.setText(""); spnAge.setValue(25); cmbGender.setSelectedIndex(0);
        txtPhone.setText(""); txtEmail.setText("");
        table.clearSelection(); selectedPatientId = null;
        btnRegister.setEnabled(true); btnUpdate.setEnabled(false);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        try {
            for (Patient p : patientService.getAllPatients()) {
                tableModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getAge(), p.getGender(), p.getPhone(), p.getEmail()
                });
            }
            dashboard.updateStatusBar();
        } catch (HospitalException e) {
            showError("Error loading patients: " + e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SHARED STATIC STYLE HELPERS  (used by BookAppointment & AppointmentList)
    // ═════════════════════════════════════════════════════════════════════════

    /** Zebra-striped, header-styled JTable. */
    static JTable buildStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            private static final Color STRIPE = new Color(28, 38, 56);
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? BG_CARD : STRIPE);
                    c.setForeground(TEXT_PRIMARY);
                }
                return c;
            }
        };

        t.setBackground(BG_CARD);
        t.setForeground(TEXT_PRIMARY);
        t.setGridColor(new Color(40, 52, 70));
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setRowHeight(36);
        t.setSelectionBackground(new Color(56, 139, 253, 70));
        t.setSelectionForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setFillsViewportHeight(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(10, 15, 30));
        h.setForeground(TEXT_MUTED);
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setPreferredSize(new Dimension(h.getWidth(), 40));
        h.setBorder(new MatteBorder(0, 0, 2, 0, ACCENT_BLUE));
        h.setReorderingAllowed(false);

        return t;
    }

    static JScrollPane styledScroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        return sp;
    }

    static JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.setBackground(BG_CARD);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(TEXT_PRIMARY);
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return f;
    }

    static void styleSpinner(JSpinner s) {
        s.setBackground(BG_CARD);
        s.setForeground(TEXT_PRIMARY);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JComponent editor = s.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(BG_CARD);
            de.getTextField().setForeground(TEXT_PRIMARY);
        }
        s.setBorder(new LineBorder(BORDER_COLOR, 1, true));
    }

    static <T> JComboBox<T> styledCombo(JComboBox<T> c) {
        c.setBackground(BG_CARD);
        c.setForeground(TEXT_PRIMARY);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        return c;
    }

    static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setForeground(TEXT_PRIMARY);
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return l;
    }

    static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(ACCENT_BLUE);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(new Color(31,111,235)); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(ACCENT_BLUE); }
        });
        return b;
    }

    static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setBackground(BG_CARD);
        b.setForeground(TEXT_MUTED);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(new Color(50, 15, 15));
        b.setForeground(ACCENT_RED);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
            new LineBorder(new Color(120, 40, 40), 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    static void showSuccess(String msg) {
        JOptionPane.showMessageDialog(null, "<html>" + msg + "</html>", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    static void showError(String msg) {
        JOptionPane.showMessageDialog(null, "<html>" + msg + "</html>", "Error", JOptionPane.ERROR_MESSAGE);
    }
    static void showWarn(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}
