package com.hospital.view;

import com.hospital.exception.HospitalException;
import com.hospital.model.Appointment;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import com.hospital.service.PatientService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


import static com.hospital.view.MainDashboard.*;
import static com.hospital.view.PatientPanel.*;

/**
 * Premium appointment booking panel with modern card-based form layout.
 */
public class BookAppointmentPanel extends JPanel {

    private final PatientService     patientService;
    private final AppointmentService appointmentService;
    private final MainDashboard      dashboard;

    private final JComboBox<Patient> cmbPatient;
    private final JComboBox<String>  cmbDoctor;
    private final JTextField         txtDate;
    private final JComboBox<String>  cmbTimeSlot;
    private final JButton            btnToday;
    private final JButton            btnBook;
    private final JButton            btnClear;

    private static final String[] DOCTORS = {
        "Dr. Alice Smith  |  Cardiology",
        "Dr. Bob Johnson  |  Pediatrics",
        "Dr. Carol White  |  Dermatology",
        "Dr. David Green  |  Neurology",
        "Dr. Emily Taylor |  General Medicine"
    };

    private static final String[] SLOTS = {
        "09:00 AM","09:30 AM","10:00 AM","10:30 AM","11:00 AM","11:30 AM",
        "02:00 PM","02:30 PM","03:00 PM","03:30 PM","04:00 PM","04:30 PM"
    };

    public BookAppointmentPanel(PatientService ps, AppointmentService as, MainDashboard db) {
        this.patientService     = ps;
        this.appointmentService = as;
        this.dashboard          = db;

        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        cmbPatient  = new JComboBox<>();
        cmbDoctor   = new JComboBox<>(DOCTORS);
        cmbTimeSlot = new JComboBox<>(SLOTS);
        txtDate     = styledField("YYYY-MM-DD");

        btnToday = ghostButton("Today");
        btnBook  = primaryButton("\uD83D\uDCC5  Confirm Appointment");
        btnClear = ghostButton("Clear");

        // Outer centering wrapper
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(buildCard());
        add(wrapper, BorderLayout.CENTER);

        setupListeners();
        refreshPatientCombo();
    }

    private JPanel buildCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_PANEL);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(32, 40, 32, 40)
        ));
        card.setPreferredSize(new Dimension(560, 450));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 18, 0);
        g.gridx = 0; g.weightx = 1;

        // Title
        g.gridy = 0;
        JLabel title = sectionTitle("\uD83D\uDCC5  Book a New Appointment");
        card.add(title, g);

        // Subtitle
        g.gridy = 1; g.insets = new Insets(0, 0, 24, 0);
        JLabel sub = new JLabel("Select a patient, doctor, date and time slot.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_MUTED);
        card.add(sub, g);

        // Reset insets
        g.insets = new Insets(0, 0, 14, 0);

        // Patient
        g.gridy = 2; card.add(formLabel("Patient"), g);
        g.gridy = 3; card.add(styledCombo(cmbPatient), g);

        // Doctor
        g.gridy = 4; card.add(formLabel("Attending Doctor"), g);
        g.gridy = 5; card.add(styledCombo(cmbDoctor), g);

        // Date row
        g.gridy = 6; card.add(formLabel("Appointment Date"), g);
        g.gridy = 7;
        JPanel dateRow = new JPanel(new BorderLayout(8, 0));
        dateRow.setOpaque(false);
        dateRow.add(txtDate,  BorderLayout.CENTER);
        dateRow.add(btnToday, BorderLayout.EAST);
        card.add(dateRow, g);

        // Time slot
        g.gridy = 8; card.add(formLabel("Time Slot"), g);
        g.gridy = 9; card.add(styledCombo(cmbTimeSlot), g);

        // Divider
        g.gridy = 10; g.insets = new Insets(8, 0, 20, 0);
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        card.add(sep, g);

        // Action buttons
        g.gridy = 11; g.insets = new Insets(0, 0, 0, 0);
        JPanel btns = new JPanel(new GridLayout(1, 2, 12, 0));
        btns.setOpaque(false);
        btns.add(btnClear);
        btns.add(btnBook);
        card.add(btns, g);

        return card;
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED);
        l.setBorder(new EmptyBorder(0, 0, 4, 0));
        return l;
    }

    private <T> JComboBox<T> styledCombo(JComboBox<T> c) {
        c.setBackground(BG_CARD);
        c.setForeground(TEXT_PRIMARY);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        return c;
    }

    private void setupListeners() {
        btnToday.addActionListener(e -> txtDate.setText(LocalDate.now().toString()));
        btnClear.addActionListener(e -> clearForm());

        btnBook.addActionListener(e -> {
            Patient patient = (Patient) cmbPatient.getSelectedItem();
            if (patient == null) { showError("Please select or register a patient first."); return; }

            String doctor   = (String) cmbDoctor.getSelectedItem();
            String dateText = txtDate.getText().trim();
            String slot     = (String) cmbTimeSlot.getSelectedItem();

            if (dateText.isEmpty()) { showError("Please enter an appointment date (YYYY-MM-DD)."); return; }

            LocalDate date;
            try { date = LocalDate.parse(dateText); }
            catch (DateTimeParseException ex) { showError("Invalid date format — use YYYY-MM-DD (e.g. 2026-08-15)."); return; }

            try {
                Appointment appt = new Appointment(patient.getId(), doctor, date, slot, "Scheduled");
                appointmentService.bookAppointment(appt);
                showSuccess("<html>Appointment confirmed!<br>" +
                    "<b>Patient:</b> " + patient.getName() + "<br>" +
                    "<b>Doctor:</b>  " + doctor + "<br>" +
                    "<b>Date:</b>    " + date + "&nbsp;&nbsp;<b>Time:</b> " + slot + "</html>");
                clearForm();
                dashboard.refreshAppointmentsTable();
            } catch (HospitalException ex) {
                showError(ex.getMessage());
            }
        });
    }

    public void refreshPatientCombo() {
        cmbPatient.removeAllItems();
        try {
            for (Patient p : patientService.getAllPatients()) cmbPatient.addItem(p);
        } catch (HospitalException ignored) {}
    }

    private void clearForm() {
        if (cmbPatient.getItemCount() > 0) cmbPatient.setSelectedIndex(0);
        cmbDoctor.setSelectedIndex(0);
        txtDate.setText("");
        cmbTimeSlot.setSelectedIndex(0);
    }
}
