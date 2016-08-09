package jStrg.database;

import java.util.Collection;
import java.util.List;

/*
    Interface for Dao Objects
 */
public interface IGenericDao<T extends DatabaseEntity> {
    T findById(int id);

    List<T> findAll();

    void create(T Object);

    void createAll(Collection<T> newEntities);

    T update(T Object);

    void delete(int id);

    void delete(T Object);

    void delete(List<T> entries);

    void deleteAll();

    public Boolean isActive();
}