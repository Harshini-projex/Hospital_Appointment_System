package com.hospital.repository;

import com.hospital.util.DBConnection;

/**
 * Factory class to manage repository singletons.
 * Switches between JDBC implementations and In-Memory implementations based on DB Connection Mode.
 */
public class RepositoryFactory {
    private static PatientRepository patientRepository;
    private static AppointmentRepository appointmentRepository;

    /**
     * Retrieves the single instance of PatientRepository.
     */
    public static synchronized PatientRepository getPatientRepository() {
        if (patientRepository == null) {
            if ("memory".equalsIgnoreCase(DBConnection.getDbMode())) {
                patientRepository = new PatientMemoryRepositoryImpl();
            } else {
                patientRepository = new PatientRepositoryImpl();
            }
        }
        return patientRepository;
    }

    /**
     * Retrieves the single instance of AppointmentRepository.
     */
    public static synchronized AppointmentRepository getAppointmentRepository() {
        if (appointmentRepository == null) {
            if ("memory".equalsIgnoreCase(DBConnection.getDbMode())) {
                appointmentRepository = new AppointmentMemoryRepositoryImpl();
            } else {
                appointmentRepository = new AppointmentRepositoryImpl();
            }
        }
        return appointmentRepository;
    }

    /**
     * Resets repository instances (useful if DB connection toggles dynamically).
     */
    public static synchronized void reset() {
        patientRepository = null;
        appointmentRepository = null;
    }
}
