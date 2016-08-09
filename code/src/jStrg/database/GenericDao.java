package jStrg.database;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;


class GenericDao<T extends DatabaseEntity> implements IGenericDao<T> {

    protected final Class<T> m_persistentClass;
    protected EntityManager m_entityManager;

    public GenericDao(Class<T> type, EntityManager em) {
        this.m_persistentClass = type;
        this.m_entityManager = em;
    }

    public T findById(int id) {
        final T result = get_entityManager().find(m_persistentClass, id);
        return result;
    }

    public List<T> findAll() {
        Query query = get_entityManager().createQuery(
                "SELECT e FROM " + getEntityClass().getCanonicalName() + " e");
        return (List<T>) query.getResultList();
    }

    public void create(T entity) {
        get_entityManager().getTransaction().begin();
        get_entityManager().persist(entity);
        get_entityManager().getTransaction().commit();
    }

    public void createAll(Collection<T> newEntities) {
        get_entityManager().getTransaction().begin();

        for (T entry : newEntities)
            get_entityManager().persist(entry);

        get_entityManager().getTransaction().commit();
    }

    public T update(T entity) {
        get_entityManager().getTransaction().begin();
        final T savedEntity = get_entityManager().merge(entity);
        get_entityManager().getTransaction().commit();

        return savedEntity;
    }

    public void delete(int id) {
        T entity = this.findById(id);
        this.delete(entity);
    }

    public void delete(T entity) {
        get_entityManager().getTransaction().begin();
        get_entityManager().remove(entity);
        get_entityManager().getTransaction().commit();
    }

    public void delete(List<T> entries) {
        get_entityManager().getTransaction().begin();

        for (T entry : entries) {
            get_entityManager().remove(entry);
        }

        get_entityManager().getTransaction().commit();
    }

    public void deleteAll() {
        this.delete(this.findAll());
    }

    public Boolean isActive() {
        return get_entityManager().getTransaction().isActive();
    }

    public void detach(T entity) {
        get_entityManager().detach(entity);
    }

    /*
        Getter & Setter
     */
    public Class<T> getEntityClass() {
        return m_persistentClass;
    }

    public void setM_entityManager(final EntityManager m_entityManager) {
        this.m_entityManager = m_entityManager;
    }

    public EntityManager get_entityManager() {
        return m_entityManager;
    }
}
