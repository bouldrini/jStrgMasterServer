package jStrg.database;

import jStrg.file_system.Application;
import jStrg.network_management.storage_management.core.Location;
import jStrg.network_management.storage_management.core.StorageCell;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by henne on 21.05.16.
 */
class LocationDao implements ILocationDao {

    private EntityManager m_entityManager;

    public LocationDao(EntityManager em) {
        this.m_entityManager = em;
    }

    public List<StorageCell> find_all_by_type(Location.TYPE _type) {

        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + StorageCell.class.getCanonicalName()
                        + " c WHERE c.m_type = :type"
        )
                .setParameter("type", _type);
        return query.getResultList();
    }

    public Boolean contains_type(Location.TYPE _type) {
        return find_all_by_type(_type).size() != 0;
    }

    @Override
    public List<StorageCell> find_with_and_condition(Location.TYPE _type, Application _app) {

        Query query = m_entityManager.createQuery(
                "SELECT distinct l FROM " + StorageCell.class.getCanonicalName() + " l"
                        + " JOIN l.m_cluster c"
                        + " JOIN c.m_application a"
                        + " WHERE a = :app"
                        + " AND l.m_type = :type"
        )
                .setParameter("app", _app)
                .setParameter("type", _type);
        return query.getResultList();
    }
}
