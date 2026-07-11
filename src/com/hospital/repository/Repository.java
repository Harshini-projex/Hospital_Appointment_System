package com.hospital.repository;

import com.hospital.exception.DatabaseException;
import java.util.List;

/**
 * Generic Abstraction repository interface.
 * Exposes core CRUD actions for domain entities.
 *
 * @param <T> Domain entity type
 */
public interface Repository<T> {
    /**
     * Inserts a new record into the database.
     */
    void add(T entity) throws DatabaseException;

    /**
     * Retrieves an entity by its unique ID.
     */
    T getById(int id) throws DatabaseException;

    /**
     * Retrieves all records of this entity.
     */
    List<T> getAll() throws DatabaseException;

    /**
     * Updates an existing record in the database.
     */
    void update(T entity) throws DatabaseException;

    /**
     * Deletes a record by its unique ID.
     */
    void delete(int id) throws DatabaseException;
}
