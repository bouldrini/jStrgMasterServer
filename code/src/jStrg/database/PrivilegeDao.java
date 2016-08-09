package jStrg.database;

import jStrg.data_types.privileges.AccessModifier;
import jStrg.file_system.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by henne on 21.05.16.
 */
class PrivilegeDao implements IPrivilegeDao {

    private final static Logger LOGGER = Logger.getLogger(Settings.location_logging_target);

    private EntityManager m_entityManager;

    public PrivilegeDao(EntityManager em) {
        this.m_entityManager = em;
    }

    private Privilege single_result_or_null(Query _query) {
        Privilege privilege = null;
        try {
            privilege = (Privilege) _query.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.fine("DB_NOTFOUND: + " + _query);
        }

        return privilege;
    }

    @Override
    public List<Privilege> user_privileges(User _user) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + Privilege.class.getCanonicalName() + " c"
                        + " WHERE c.m_user = :user"
        )
                .setParameter("user", _user);
        return query.getResultList();
    }

    @Override
    public List<Privilege> user_privileges(User _user, Privilege.TYPE _privtype) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + Privilege.class.getCanonicalName() + " c"
                        + " WHERE c.m_user = :user"
                        + " AND c.m_privilegable_type = :ptype"
        )
                .setParameter("user", _user)
                .setParameter("ptype", _privtype);
        return query.getResultList();
    }

    @Override
    public List<Privilege> user_privileges(User _user, Privilege.TYPE _privtype, AccessModifier _modifier) {
        StringBuilder access_where = new StringBuilder("");
        if (_modifier.read() != null) {
            access_where.append(" AND c.m_read = :read");
        }
        if (_modifier.write() != null) {
            access_where.append(" AND c.m_write = :write");
        }
        if (_modifier.delete() != null) {
            access_where.append(" AND c.m_delete = :delete");
        }
        if (_modifier.invite() != null) {
            access_where.append(" AND c.m_write = :invite");
        }

        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + Privilege.class.getCanonicalName() + " c"
                        + " WHERE c.m_user = :user"
                        + " AND c.m_privilegable_type = :ptype"
                        + access_where.toString()
        )
                .setParameter("user", _user)
                .setParameter("ptype", _privtype);

        if (_modifier.read() != null) {
            query.setParameter("read", _modifier.read());
        }
        if (_modifier.write() != null) {
            query.setParameter("write", _modifier.write());
        }
        if (_modifier.delete() != null) {
            query.setParameter("delete", _modifier.delete());
        }
        if (_modifier.invite() != null) {
            query.setParameter("invite", _modifier.invite());
        }

        return query.getResultList();
    }

    @Override
    public Privilege find_user_privilege_for_file(User _user, File _file) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + Privilege.class.getCanonicalName() + " c"
                        + " WHERE c.m_user = :user"
                        + " AND c.m_privilegable_id = :pid"
                        + " AND c.m_privilegable_type = :ptype"
        )
                .setParameter("user", _user)
                .setParameter("ptype", Privilege.TYPE.FILE)
                .setParameter("pid", _file.get_id());

        return (Privilege) single_result_or_null(query);
    }

    @Override
    public Privilege find_user_privilege_for_filefolder(User _user, FileFolder _folder) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + Privilege.class.getCanonicalName() + " c"
                        + " WHERE c.m_user = :name"
                        + " AND c.m_privilegable_id = :title"
                        + " AND c.m_privilegable_type = :type"
        )
                .setParameter("name", _user)
                .setParameter("title", _folder.get_id())
                .setParameter("type", Privilege.TYPE.FILEFOLDER);
        return single_result_or_null(query);
    }

    @Override
    public List<Privilege> application_privileges(Application _application) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + Privilege.class.getCanonicalName() + " c"
                        + " WHERE c.m_application = :application"
        )
                .setParameter("application", _application);
        return query.getResultList();
    }

    @Override
    public List<Privilege> folder_privileges(FileFolder _folder) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + Privilege.class.getCanonicalName() + " c"
                        + " WHERE c.m_folder = :folder"
        )
                .setParameter("folder", _folder);
        return query.getResultList();
    }

    @Override
    public List<Privilege> folder_privileges(FileFolder _folder, User _user) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + Privilege.class.getCanonicalName() + " c"
                        + " WHERE c.m_folder = :folder"
                        + " AND c.m_user = :user"
        )
                .setParameter("folder", _folder)
                .setParameter("user", _user);
        return query.getResultList();
    }
}
