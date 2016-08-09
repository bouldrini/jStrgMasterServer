package jStrg.database;

import jStrg.file_system.File;
import jStrg.file_system.Privilege;
import jStrg.file_system.Settings;
import org.eclipse.persistence.config.PersistenceUnitProperties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

class DbController {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private final static Logger LOGGER = Logger.getLogger(Settings.logging_target);
    private static final String PERSISTENCE_UNIT_NAME = "jstrg_unit";
    private static DbController m_instance;
    // DB
    private EntityManagerFactory m_EntityManagerFactory = null;
    private EntityManager m_EntityManager = null;


    private DbController() {
        Map<String, String> db_properties = new HashMap();
        try {
            db_properties.put(PersistenceUnitProperties.JDBC_DRIVER, JDBC_DRIVER);
            db_properties.put(PersistenceUnitProperties.LOGGING_LEVEL, Settings.m_db_loglevel);
            db_properties.put(PersistenceUnitProperties.JDBC_USER, Settings.m_db_username);
            db_properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Settings.m_db_password);
            db_properties.put(PersistenceUnitProperties.JDBC_URL, "jdbc:mysql://" + Settings.m_db_server + ":" + Settings.m_db_port + "/" + Settings.m_db_database);


        } catch (NullPointerException e) {
            LOGGER.warning("failure while read db config option: " + e);
        }
        m_EntityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, db_properties);


        m_EntityManager = m_EntityManagerFactory.createEntityManager();
    }

    public static DbController getInstance() {
        if (m_instance == null)
            m_instance = new DbController();
        return m_instance;
    }

    public static void closeEntityManager() {
        m_instance.get_EntityManager().close();
    }

    public static void openEntityManager() {
        m_instance.m_EntityManager = m_instance.m_EntityManagerFactory.createEntityManager();
    }

    public EntityManagerFactory get_factory() {
        return m_EntityManagerFactory;
    }

    public Boolean simple_statement(String _query) {
        /*if (m_conn == null)
            return false;

        Boolean success = false;
        Statement statement = null;
        try {
            statement = m_conn.createStatement();

            statement.execute(_query);

            success = true;
        } catch (SQLException e) {
            LOGGER.warning("SQL failure: " + e.toString());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                LOGGER.severe("Error while closing statement: " + e);
            }
        }
        return success;
    */
        LOGGER.severe("not implemented");
        return false;
    }

    /* dao getters */
    public IGenericDao<File> getFileDao() {
        return new GenericDao<File>(File.class,
                this.m_EntityManagerFactory.createEntityManager());
    }

    public IGenericDao<Privilege> getPrivilegeDao() {
        return new GenericDao<Privilege>(Privilege.class,
                this.m_EntityManagerFactory.createEntityManager());
    }


    public EntityManager get_EntityManager() {
        return m_EntityManager;
    }
}
