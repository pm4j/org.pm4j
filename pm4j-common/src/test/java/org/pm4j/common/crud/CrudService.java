package org.pm4j.common.crud;


/**
 * Interface for common CRUD scenarios.
 *
 * @param <T>
 *            Type of the handled entities.
 * @param <T_ID>
 *            Type of the entity identifier.
 *
 * @author Olaf Boede
 */
public interface CrudService<T, T_ID> {

    /**
     * Persists a new or updated entity.
     *
     * @param entity
     *            The entity to save.
     * @return The saved entity.
     */
    T save(T entity);

    /**
     * Deletes the given entity.
     *
     * @param entity
     *            The entity to delete.
     */
    void delete(T entity);

    /**
     * Finds an entity by ID.
     *
     * @param id
     *            ID of the instance to find.
     * @return The found instance or <code>null</code>.
     */
    T findById(T_ID id);

}
