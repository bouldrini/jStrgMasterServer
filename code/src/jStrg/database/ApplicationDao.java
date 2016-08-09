package jStrg.database;

import jStrg.file_system.Application;
import jStrg.network_management.storage_management.cluster.Cluster;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by henne on 21.05.16.
 */
class ApplicationDao implements IApplicationDao {

    private EntityManager m_entityManager;

    public ApplicationDao(EntityManager em) {
        this.m_entityManager = em;
    }

    public Application find_by_title(String _title) {

        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + Application.class.getCanonicalName()
                        + " c WHERE c.m_title = :title"
        )
                .setParameter("title", _title);
        return (Application) query.getSingleResult();
    }

    @Override
    public void delete(Application _app) {
        List<Cluster> clusterlist = Cluster.find_by_app(_app);
        m_entityManager.getTransaction().begin();
        for (Cluster clustertype : clusterlist) {
            m_entityManager.remove(clustertype);
        }
        m_entityManager.remove(_app);
        m_entityManager.getTransaction().commit();
    }
}
