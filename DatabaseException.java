package com.hospital.exception;

/**
 * Custom exception representing database errors and connectivity issues.
 */
public class DatabaseException extends HospitalException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
