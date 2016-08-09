package jStrg.database;

import jStrg.file_system.Settings;

import java.util.logging.Logger;

/**
 * class saved in environment to get dao objects and restart db connection
 */
public class Data<T extends DatabaseEntity> {

    private final static Logger LOGGER = Logger.getLogger(Settings.logging_target);

    public IGenericDao<T> get_dao(Class<T> _type) {
        return new GenericDao<>(_type, DbController.getInstance().get_EntityManager());
    }

    public void create(Class<T> _type, T _object) {
        new GenericDao<>(_type, DbController.getInstance().get_EntityManager()).create(_object);
    }

    public ILocationDao get_dao_location() {
        return new LocationDao(DbController.getInstance().get_EntityManager());
    }

    public IApplicationDao get_dao_application() {
        return new ApplicationDao(DbController.getInstance().get_EntityManager());
    }

    public IFileDao get_dao_file() {
        return new FileDao(DbController.getInstance().get_EntityManager());
    }

    public IFileFolderDao get_dao_filefolder() {
        return new FileFolderDao(DbController.getInstance().get_EntityManager());
    }

    public IUserDao get_dao_user() {
        return new UserDao(DbController.getInstance().get_EntityManager());
    }

    public IPrivilegeDao get_dao_privilege() {
        return new PrivilegeDao(DbController.getInstance().get_EntityManager());
    }

    public IFileVersionDao get_dao_fileversion() {
        return new FileVersionDao(DbController.getInstance().get_EntityManager());
    }

    public IClusterDao get_dao_cluster() {
        return new ClusterDao(DbController.getInstance().get_EntityManager());
    }

    public void closeDBconnection() {
        DbController.closeEntityManager();
    }

    public void openDBconnection() {
        DbController.openEntityManager();
    }

    @Override
    public String toString() {
        return "Data Access Controller using: " + DbController.JDBC_DRIVER;
    }

    public IFileTypeDao get_dao_file_type() {
        return new FileTypeDao(DbController.getInstance().get_EntityManager());
    }
}
