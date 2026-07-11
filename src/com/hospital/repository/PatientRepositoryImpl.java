package com.hospital.repository;

import com.hospital.exception.DatabaseException;
import com.hospital.model.Patient;
import com.hospital.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete JDBC implementation of Repository for Patient entity.
 * Uses PreparedStatement for security and try-with-resources for safe resource closing.
 */
public class PatientRepositoryImpl implements PatientRepository {

    @Override
    public void add(Patient patient) throws DatabaseException {
        String sql = "INSERT INTO patients (name, age, gender, phone, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Ensure transaction is committed instantly and visible to other connections
            conn.setAutoCommit(true);
            
            pstmt.setString(1, patient.getName());
            pstmt.setInt(2, patient.getAge());
            pstmt.setString(3, patient.getGender());
            pstmt.setString(4, patient.getPhone());
            pstmt.setString(5, patient.getEmail());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    patient.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add patient to database.", e);
        }
    }

    @Override
    public Patient getById(int id) throws DatabaseException {
        String sql = "SELECT * FROM patients WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPatient(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve patient with ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<Patient> getAll() throws DatabaseException {
        String sql = "SELECT * FROM patients ORDER BY id DESC";
        List<Patient> patients = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                patients.add(mapRowToPatient(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all patients.", e);
        }
        return patients;
    }

    @Override
    public void update(Patient patient) throws DatabaseException {
        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, phone = ?, email = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, patient.getName());
            pstmt.setInt(2, patient.getAge());
            pstmt.setString(3, patient.getGender());
            pstmt.setString(4, patient.getPhone());
            pstmt.setString(5, patient.getEmail());
            pstmt.setInt(6, patient.getId());
            
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new DatabaseException("Updating patient failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update patient with ID: " + patient.getId(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseException {
        String sql = "DELETE FROM patients WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete patient with ID: " + id, e);
        }
    }

    /**
     * Checks if a patient already exists with the given phone number.
     * If excludeId is provided, checks other patients (used for updates).
     */
    public boolean existsByPhone(String phone, Integer excludeId) throws DatabaseException {
        String sql = excludeId == null 
            ? "SELECT COUNT(*) FROM patients WHERE phone = ?" 
            : "SELECT COUNT(*) FROM patients WHERE phone = ? AND id != ?";
            
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, phone);
            if (excludeId != null) {
                pstmt.setInt(2, excludeId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking patient uniqueness by phone.", e);
        }
        return false;
    }

    private Patient mapRowToPatient(ResultSet rs) throws SQLException {
        return new Patient(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("age"),
            rs.getString("gender"),
            rs.getString("phone"),
            rs.getString("email")
        );
    }
}
