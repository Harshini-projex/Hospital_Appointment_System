package com.hospital.exception;

/**
 * Base custom exception for the Hospital Appointment Management System.
 * Enforces structured, typed custom exceptions for service and data access layers.
 */
public class HospitalException extends Exception {
    public HospitalException(String message) {
        super(message);
    }

    public HospitalException(String message, Throwable cause) {
        super(message, cause);
    }
}
