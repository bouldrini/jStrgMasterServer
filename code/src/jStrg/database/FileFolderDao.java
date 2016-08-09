package jStrg.database;

import jStrg.file_system.FileFolder;
import jStrg.file_system.User;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by henne on 21.05.16.
 */
class FileFolderDao implements IFileFolderDao {

    private EntityManager m_entityManager;

    public FileFolderDao(EntityManager em) {
        this.m_entityManager = em;
    }

    public FileFolder find_rootfolder(User _user) {

        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + FileFolder.class.getCanonicalName()
                        + " c WHERE c.m_title = :title"
                        + " AND c.m_root_folder = :rootbool"
                        + " AND c.m_user = :userid"
        )
                .setParameter("title", "")
                .setParameter("rootbool", true)
                .setParameter("userid", _user);
        return (FileFolder) query.getSingleResult();
    }

    @Override
    public List<FileFolder> find_by_title(String _title, User _user) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + FileFolder.class.getCanonicalName()
                        + " c WHERE c.m_title = :title"
                        + " AND c.m_title = :title"
                        + " AND c.m_user = :userid"
        )
                .setParameter("title", _title)
                .setParameter("userid", _user);
        return query.getResultList();
    }

    @Override
    public List<FileFolder> find_by_parent(FileFolder _parent) {
        Query query = m_entityManager.createQuery(
                "SELECT c FROM " + FileFolder.class.getCanonicalName()
                        + " c WHERE c.m_parent_folder = :parent"
        )
                .setParameter("parent", _parent);
        return query.getResultList();
    }
}
