package com.hospital.exception;

/**
 * Custom exception representing business validation failures.
 */
public class ValidationException extends HospitalException {
    public ValidationException(String message) {
        super(message);
    }
}
