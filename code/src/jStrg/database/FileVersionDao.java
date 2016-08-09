package jStrg.database;

import jStrg.file_system.Application;
import jStrg.file_system.File;
import jStrg.file_system.FileVersion;
import jStrg.file_system.Settings;
import jStrg.network_management.storage_management.core.StorageCell;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by henne on 21.05.16.
 */
class FileVersionDao implements IFileVersionDao {

    private final static Logger LOGGER = Logger.getLogger(Settings.location_logging_target);
    private EntityManager m_entityManager;

    public FileVersionDao(EntityManager em) {
        this.m_entityManager = em;
    }

    private FileVersion single_result_or_null(Query _query) {
        FileVersion fileversion = null;
        try {
            fileversion = (FileVersion) _query.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.fine("DB_NOTFOUND: + " + _query);
        }

        return fileversion;
    }

    @Override
    public List<FileVersion> find_by_chksum(String _chksum) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + FileVersion.class.getCanonicalName()
                        + " c WHERE c.m_checksum = :chksum"
        )
                .setParameter("chksum", _chksum);
        return query.getResultList();
    }

    @Override
    public FileVersion find_by_previous(FileVersion _version) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + FileVersion.class.getCanonicalName()
                        + " c WHERE c.m_previous = :previous"
        )
                .setParameter("previous", _version);
        return single_result_or_null(query);
    }

    public List<StorageCell> find_references_chksum_to_location(String _chksum, StorageCell _location) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + FileVersion.class.getCanonicalName() + " v"
                        + " JOIN v.m_location c"
                        + " WHERE v.m_checksum = :checksum"
                        + " AND c = :location"
        )
                .setParameter("checksum", _chksum)
                .setParameter("location", _location);
        // SELECT c.*  FROM FILEVERSION v JOIN FILEVERSION_CONNECTORBLUEPRINT fk ON fk.FileVersion_M_ID = v.M_ID JOIN CONNECTORBLUEPRINT c on fk.m_location_M_ID = c.M_ID;

        //Query query = m_entityManager.createNativeQuery(sqlstring, StorageCell)
        return query.getResultList();
    }

    @Override
    public List<FileVersion> find_by_file(File _file) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + FileVersion.class.getCanonicalName()
                        + " c WHERE c.m_file = :file"
        )
                .setParameter("file", _file);
        return query.getResultList();
    }

    @Override
    public List<FileVersion> find_by_app(Application _app) {

        // get application from owner and join tables until we have the application table

        Query query = m_entityManager.createQuery(
                "SELECT distinct v FROM " + FileVersion.class.getCanonicalName() + " v"
                        + " JOIN v.m_location l"
                        + " JOIN l.m_cluster c"
                        + " JOIN c.m_application a"
                        + " WHERE a = :app"
        )
                .setParameter("app", _app);
        return query.getResultList();
    }


}