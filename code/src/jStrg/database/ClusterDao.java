package jStrg.database;

import jStrg.file_system.Application;
import jStrg.network_management.storage_management.cluster.Cluster;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by henne on 21.05.16.
 */
class ClusterDao implements IClusterDao {

    private EntityManager m_entityManager;

    public ClusterDao(EntityManager em) {
        this.m_entityManager = em;
    }

    @Override
    public List<Cluster> find(Application _app) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + Cluster.class.getCanonicalName()
                        + " c WHERE c.m_application = :app"
        )
                .setParameter("app", _app);
        return query.getResultList();
    }
}
