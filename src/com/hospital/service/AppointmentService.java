package com.hospital.service;

import com.hospital.exception.DatabaseException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Appointment;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.RepositoryFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer managing Appointment booking rules, slot conflict validations, and cancellations.
 */
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    public AppointmentService() {
        this.appointmentRepository = RepositoryFactory.getAppointmentRepository();
    }

    /**
     * Books a new appointment.
     * Validates that the slot is open and the date is valid.
     */
    public void bookAppointment(Appointment appointment) throws ValidationException, DatabaseException {
        validateAppointment(appointment);
        appointmentRepository.add(appointment);
    }

    /**
     * Updates an appointment's details (e.g., rescheduling).
     */
    public void updateAppointment(Appointment appointment) throws ValidationException, DatabaseException {
        if (appointment.getId() == null) {
            throw new ValidationException("Cannot update an appointment without a valid ID.");
        }
        validateAppointment(appointment);
        appointmentRepository.update(appointment);
    }

    /**
     * Cancels an appointment (changes status to 'Cancelled').
     */
    public void cancelAppointment(int id) throws ValidationException, DatabaseException {
        Appointment appointment = appointmentRepository.getById(id);
        if (appointment == null) {
            throw new ValidationException("Appointment with ID " + id + " was not found.");
        }
        if ("Cancelled".equalsIgnoreCase(appointment.getStatus())) {
            throw new ValidationException("This appointment is already cancelled.");
        }
        appointment.setStatus("Cancelled");
        appointmentRepository.update(appointment);
    }

    /**
     * Deletes an appointment record permanently.
     */
    public void deleteAppointment(int id) throws DatabaseException {
        appointmentRepository.delete(id);
    }

    /**
     * Retrieves all booked appointments.
     */
    public List<Appointment> getAllAppointments() throws DatabaseException {
        return appointmentRepository.getAll();
    }

    /**
     * Retrieves an appointment by ID.
     */
    public Appointment getAppointmentById(int id) throws DatabaseException {
        return appointmentRepository.getById(id);
    }

    /**
     * Performs business rule validations on an Appointment object.
     */
    private void validateAppointment(Appointment appointment) throws ValidationException, DatabaseException {
        // Null checks
        if (appointment.getDoctorName() == null || appointment.getDoctorName().trim().isEmpty()) {
            throw new ValidationException("Doctor name must be selected.");
        }
        if (appointment.getAppointmentDate() == null) {
            throw new ValidationException("Appointment date must be specified.");
        }
        if (appointment.getAppointmentTime() == null || appointment.getAppointmentTime().trim().isEmpty()) {
            throw new ValidationException("Appointment time slot must be selected.");
        }

        // Date Validation: date cannot be in the past
        if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Appointment date cannot be in the past.");
        }

        // Slot Conflict Check
        // If an appointment is Cancelled, it doesn't occupy the slot, so conflict check only counts Scheduled ones.
        if (appointmentRepository.isSlotConflict(
                appointment.getDoctorName(), 
                appointment.getAppointmentDate(), 
                appointment.getAppointmentTime(), 
                appointment.getId())) {
            throw new ValidationException(String.format(
                "Time slot %s on %s is already booked for %s. Please choose a different slot or doctor.",
                appointment.getAppointmentTime(), 
                appointment.getAppointmentDate(), 
                appointment.getDoctorName()
            ));
        }
    }
}
