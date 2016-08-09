package jStrg.database;

import jStrg.file_system.FileType;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Created by henne on 21.05.16.
 */
class FileTypeDao implements IFileTypeDao {

    private EntityManager m_entityManager;

    public FileTypeDao(EntityManager em) {
        this.m_entityManager = em;
    }

    public FileType find_by_file_extension(String _file_extension) {

        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + FileType.class.getCanonicalName()
                        + " c WHERE c.m_file_extension = :title"
        )
                .setParameter("m_file_extension", _file_extension);
        return (FileType) query.getSingleResult();
    }

    @Override
    public void delete(FileType _type) {
        m_entityManager.remove(_type);
    }
}
