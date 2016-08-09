package jStrg.database;

import jStrg.file_system.File;
import jStrg.file_system.FileFolder;
import jStrg.file_system.FileVersion;
import jStrg.file_system.Settings;
import jStrg.file_system.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.Join;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by henne on 21.05.16.
 */
class FileDao implements IFileDao {

    private final static Logger LOGGER = Logger.getLogger(Settings.location_logging_target);

    private EntityManager m_entityManager;

    public FileDao(EntityManager em) {
        this.m_entityManager = em;
    }

    private File single_result_or_null(Query _query) {
        File file = null;
        try {
            file = (File) _query.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.fine("DB_NOTFOUND: + " + _query);
        }
        return file;
    }

    @Override
    public List<File> find_by_title(String _title) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + File.class.getCanonicalName() + " c"
                        + " WHERE c.m_title = :title"
        )
                .setParameter("title", _title);
        return query.getResultList();
    }

    @Override
    public List<File> find_by_title(String _title, User _user) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + File.class.getCanonicalName() + " c"
                        + " WHERE c.m_title = :title"
                        + " AND c.m_user = :user"
        )
                .setParameter("title", _title)
                .setParameter("user", _user);
        return query.getResultList();
    }

    @Override
    public List<File> find_by_parent(FileFolder _folder) {
        Query query = m_entityManager.createQuery(
                "SELECT f FROM " + File.class.getCanonicalName() + " f"
                        + " WHERE f.m_parent = :parent"
        )
                .setParameter("parent", _folder);
        return query.getResultList();
    }

    @Override
    public void delete(File _file) {
        List<FileVersion> versionlist = new FileVersionDao(m_entityManager).find_by_file(_file);
        m_entityManager.getTransaction().begin();
        for (FileVersion version : versionlist) {
            m_entityManager.remove(version);
        }
        m_entityManager.remove(_file);
        m_entityManager.getTransaction().commit();
    }
}
