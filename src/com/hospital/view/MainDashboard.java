package com.hospital.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.hospital.service.AppointmentService;
import com.hospital.service.PatientService;
import com.hospital.exception.HospitalException;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main dashboard container frame for the We Care system.
 * Features premium KPI cards, modern tabbed navigation and a live status bar.
 */
public class MainDashboard extends JFrame {

    // ─── Color Palette ─────────────────────────────────────────────────────
    static final Color BG_DARK       = new Color(15,  20,  30);   // deepest bg
    static final Color BG_PANEL      = new Color(22,  30,  46);   // panel bg
    static final Color BG_CARD       = new Color(30,  41,  59);   // card bg
    static final Color BG_CARD_HOVER = new Color(38,  52,  74);   // card hover
    static final Color ACCENT_BLUE   = new Color(56, 139, 253);   // primary accent
    static final Color ACCENT_GREEN  = new Color(34, 197,  94);   // success green
    static final Color ACCENT_RED    = new Color(239, 68,  68);   // danger red
    static final Color ACCENT_CYAN   = new Color(34, 211, 238);   // info cyan
    static final Color ACCENT_PURPLE = new Color(167, 139, 250);  // purple accent
    static final Color TEXT_PRIMARY  = new Color(241, 245, 249);  // bright text
    static final Color TEXT_MUTED    = new Color(148, 163, 184);  // muted text
    static final Color BORDER_COLOR  = new Color(51,  65,  85);   // subtle border

    private final PatientService patientService;
    private final AppointmentService appointmentService;

    // Panels
    private PatientPanel patientPanel;
    private BookAppointmentPanel bookAppointmentPanel;
    private AppointmentListPanel appointmentListPanel;

    // KPI card value labels (updated via updateStatusBar)
    private JLabel lblTotalPatientsVal;
    private JLabel lblTotalApptVal;
    private JLabel lblCancelledVal;
    private JLabel lblScheduledVal;

    // Status bar
    private JLabel lblConnectionStatus;
    private JLabel lblStats;
    private boolean isDarkMode = true;

    public MainDashboard() {
        this.patientService    = new PatientService();
        this.appointmentService = new AppointmentService();

        setTitle("We Care - Hospital Appointment Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);

        // Root content pane with dark background
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        setContentPane(root);

        // ── Build sections in correct order so status labels exist before panels ──
        root.add(buildHeader(),    BorderLayout.NORTH);
        root.add(buildStatusBar(), BorderLayout.SOUTH);   // ← built BEFORE tabs
        root.add(buildCenter(),    BorderLayout.CENTER);  // ← panels created here

        updateStatusBar();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HEADER
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(BG_PANEL);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(14, 24, 14, 24)
        ));

        // ── Brand / Logo area
        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandPanel.setOpaque(false);

        JLabel lblBrand = new JLabel("⬛ We Care");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBrand.setForeground(TEXT_PRIMARY);

        JLabel lblTagline = new JLabel("Clinical Appointment & Patient Records Management Portal");
        lblTagline.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTagline.setForeground(TEXT_MUTED);

        brandPanel.add(lblBrand);
        brandPanel.add(lblTagline);
        header.add(brandPanel, BorderLayout.WEST);

        // ── KPI Cards row ──────────────────────────────────────────────────
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);

        lblTotalPatientsVal = new JLabel("0");
        lblTotalApptVal     = new JLabel("0");
        lblScheduledVal     = new JLabel("0");
        lblCancelledVal     = new JLabel("0");

        kpiRow.add(buildKpiCard("Patients Registered", lblTotalPatientsVal, ACCENT_BLUE));
        kpiRow.add(buildKpiCard("Total Appointments",  lblTotalApptVal,     ACCENT_PURPLE));
        kpiRow.add(buildKpiCard("Scheduled",           lblScheduledVal,     ACCENT_GREEN));
        kpiRow.add(buildKpiCard("Cancelled",           lblCancelledVal,     ACCENT_RED));

        header.add(kpiRow, BorderLayout.CENTER);

        // ── Theme toggle button
        JButton btnTheme = new JButton("☀ Light Mode");
        btnTheme.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnTheme.setFocusPainted(false);
        btnTheme.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTheme.addActionListener(e -> toggleTheme(btnTheme));

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        eastPanel.setOpaque(false);
        eastPanel.add(btnTheme);
        header.add(eastPanel, BorderLayout.EAST);

        return header;
    }

    /** Builds a single KPI metric card with value label, title, and accent color. */
    private JPanel buildKpiCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(10, 16, 10, 16)
        ));

        // Accent top bar
        JPanel accentBar = new JPanel();
        accentBar.setBackground(accent);
        accentBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 3));
        accentBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        accentBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(accentBar);
        card.add(Box.createVerticalStrut(8));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(TEXT_MUTED);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblTitle);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(BG_CARD_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { card.setBackground(BG_CARD); }
        });

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  STATUS BAR  (built BEFORE center panels)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_PANEL);
        bar.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(5, 20, 5, 20)
        ));

        lblConnectionStatus = new JLabel("  Database Status: Initializing...");
        lblConnectionStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblConnectionStatus.setForeground(ACCENT_CYAN);

        lblStats = new JLabel("Loading stats...   ");
        lblStats.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStats.setForeground(TEXT_MUTED);

        JLabel lblVersion = new JLabel("We Care v1.0   ");
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblVersion.setForeground(new Color(71, 85, 105));

        bar.add(lblConnectionStatus, BorderLayout.WEST);
        bar.add(lblStats,            BorderLayout.CENTER);
        bar.add(lblVersion,          BorderLayout.EAST);

        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CENTER — tabbed panels
    // ─────────────────────────────────────────────────────────────────────────
    private JComponent buildCenter() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBorder(new EmptyBorder(10, 10, 10, 10));

        patientPanel         = new PatientPanel(patientService, this);
        bookAppointmentPanel = new BookAppointmentPanel(patientService, appointmentService, this);
        appointmentListPanel = new AppointmentListPanel(appointmentService, this);

        tabs.addTab("  \uD83D\uDC64  Manage Patients  ",    patientPanel);
        tabs.addTab("  \uD83D\uDCC5  Book Appointment  ",   bookAppointmentPanel);
        tabs.addTab("  \uD83D\uDCCB  View / Cancel Appointments  ", appointmentListPanel);

        return tabs;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  THEME TOGGLE
    // ─────────────────────────────────────────────────────────────────────────
    private void toggleTheme(JButton btn) {
        try {
            if (isDarkMode) {
                UIManager.setLookAndFeel(new FlatLightLaf());
                btn.setText("⬛ Dark Mode");
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                btn.setText("☀ Light Mode");
            }
            isDarkMode = !isDarkMode;
            SwingUtilities.updateComponentTreeUI(this);
            repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to switch theme:\n" + ex.getMessage(), "Theme Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PUBLIC REFRESH METHODS (called by child panels)
    // ─────────────────────────────────────────────────────────────────────────
    public void refreshAppointmentPatientCombos() {
        bookAppointmentPanel.refreshPatientCombo();
    }

    public void refreshAppointmentsTable() {
        appointmentListPanel.refreshTable();
    }

    /** Updates KPI cards and status bar with live counts from services. */
    public void updateStatusBar() {
        try {
            int patients   = patientService.getAllPatients().size();
            int allAppt    = appointmentService.getAllAppointments().size();
            long scheduled = appointmentService.getAllAppointments().stream()
                    .filter(a -> "Scheduled".equalsIgnoreCase(a.getStatus())).count();
            long cancelled = allAppt - scheduled;

            // Update KPI cards
            lblTotalPatientsVal.setText(String.valueOf(patients));
            lblTotalApptVal.setText(String.valueOf(allAppt));
            lblScheduledVal.setText(String.valueOf(scheduled));
            lblCancelledVal.setText(String.valueOf(cancelled));

            // Status bar
            lblStats.setText(String.format(
                "  Patients: %d   |   Appointments: %d   |   Scheduled: %d   |   Cancelled: %d   ",
                patients, allAppt, scheduled, cancelled));

            String mode = com.hospital.util.DBConnection.getDbMode();
            if ("memory".equalsIgnoreCase(mode)) {
                lblConnectionStatus.setText("  \u25CF  Offline — Simulation Mode");
                lblConnectionStatus.setForeground(ACCENT_CYAN);
            } else {
                lblConnectionStatus.setText("  \u25CF  MySQL Connected  (localhost)");
                lblConnectionStatus.setForeground(ACCENT_GREEN);
            }

        } catch (HospitalException e) {
            if (lblConnectionStatus != null) {
                lblConnectionStatus.setText("  \u25CF  Database Error");
                lblConnectionStatus.setForeground(ACCENT_RED);
            }
        }
    }
}
