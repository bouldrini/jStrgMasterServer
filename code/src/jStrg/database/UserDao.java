package jStrg.database;

import jStrg.file_system.Application;
import jStrg.file_system.Settings;
import jStrg.file_system.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by henne on 21.05.16.
 */
class UserDao implements IUserDao {

    private final static Logger LOGGER = Logger.getLogger(Settings.location_logging_target);

    private EntityManager m_entityManager;

    public UserDao(EntityManager em) {
        this.m_entityManager = em;
    }

    private User single_result_or_null(Query _query) {
        User user = null;
        try {
            user = (User) _query.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.fine("DB_NOTFOUND: + " + _query);
        }

        return user;
    }

    @Override
    public User find_by_name(String _name) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + User.class.getCanonicalName() + " c"
                        + " WHERE c.m_username = :name"
        )
                .setParameter("name", _name);
        return single_result_or_null(query);
    }

    @Override
    public User find_by_name(String _name, Application _application) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + User.class.getCanonicalName() + " c"
                        + " WHERE c.m_username = :name"
                        + " AND c.m_application = :application"
        )
                .setParameter("name", _name)
                .setParameter("application", _application);
        return single_result_or_null(query);
    }

    @Override
    public List<User> find_all_of_application(Application _application) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + User.class.getCanonicalName() + " c"
                        + " WHERE c.m_application = :application"
        )
                .setParameter("application", _application);
        return query.getResultList();
    }
}
