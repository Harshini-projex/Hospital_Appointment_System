package com.hospital.service;

import com.hospital.exception.DatabaseException;
import com.hospital.exception.ValidationException;
import com.hospital.model.Patient;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.RepositoryFactory;

import java.util.List;

/**
 * Service layer managing Patient business rules, validation, and data coordination.
 */
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService() {
        this.patientRepository = RepositoryFactory.getPatientRepository();
    }

    /**
     * Registers a new patient.
     * Validates input rules and checks phone uniqueness before calling repository.
     */
    public void registerPatient(Patient patient) throws ValidationException, DatabaseException {
        validatePatient(patient);
        patientRepository.add(patient);
    }

    /**
     * Updates an existing patient's details.
     * Validates input rules and checks phone uniqueness before calling repository.
     */
    public void updatePatient(Patient patient) throws ValidationException, DatabaseException {
        if (patient.getId() == null) {
            throw new ValidationException("Cannot update a patient without a valid ID.");
        }
        validatePatient(patient);
        patientRepository.update(patient);
    }

    /**
     * Retrieves all registered patients.
     */
    public List<Patient> getAllPatients() throws DatabaseException {
        return patientRepository.getAll();
    }

    /**
     * Retrieves a patient by ID.
     */
    public Patient getPatientById(int id) throws DatabaseException {
        return patientRepository.getById(id);
    }

    /**
     * Deletes a patient by ID.
     */
    public void deletePatient(int id) throws DatabaseException {
        patientRepository.delete(id);
    }

    /**
     * Performs business rule validations on a Patient object.
     */
    private void validatePatient(Patient patient) throws ValidationException, DatabaseException {
        // Name Validation
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            throw new ValidationException("Patient name cannot be empty.");
        }
        if (!patient.getName().matches("^[a-zA-Z\\s]+$")) {
            throw new ValidationException("Patient name can only contain alphabetic characters and spaces.");
        }

        // Age Validation
        if (patient.getAge() < 1 || patient.getAge() > 120) {
            throw new ValidationException("Patient age must be a positive integer between 1 and 120.");
        }

        // Gender Validation
        if (patient.getGender() == null || patient.getGender().trim().isEmpty()) {
            throw new ValidationException("Patient gender must be specified.");
        }

        // Phone Validation
        if (patient.getPhone() == null || patient.getPhone().trim().isEmpty()) {
            throw new ValidationException("Phone number cannot be empty.");
        }
        if (!patient.getPhone().matches("^\\d{10}$")) {
            throw new ValidationException("Phone number must be exactly 10 digits.");
        }

        // Email Validation
        if (patient.getEmail() == null || patient.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email address cannot be empty.");
        }
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!patient.getEmail().matches(emailRegex)) {
            throw new ValidationException("Email address must follow a valid pattern (e.g., example@domain.com).");
        }

        // Phone Uniqueness Check
        if (patientRepository.existsByPhone(patient.getPhone(), patient.getId())) {
            throw new ValidationException("A patient with phone number " + patient.getPhone() + " is already registered.");
        }
    }
}
