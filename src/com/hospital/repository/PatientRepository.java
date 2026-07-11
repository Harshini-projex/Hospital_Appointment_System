package com.hospital.repository;

import com.hospital.exception.DatabaseException;
import com.hospital.model.Patient;

/**
 * Interface specializing the general Repository for Patient.
 * Defines custom queries like phone uniqueness checking.
 */
public interface PatientRepository extends Repository<Patient> {
    /**
     * Checks if a phone number is registered by another patient.
     */
    boolean existsByPhone(String phone, Integer excludeId) throws DatabaseException;
}
