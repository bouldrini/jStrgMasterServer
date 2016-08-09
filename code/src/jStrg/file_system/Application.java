package jStrg.file_system;

// REQUIREMENTS

import jStrg.database.DatabaseEntity;
import jStrg.database.IApplicationDao;
import jStrg.database.IGenericDao;
import jStrg.environment.Environment;
import jStrg.network_management.storage_management.cluster.AmazonS3BucketCluster;
import jStrg.network_management.storage_management.cluster.DiskCluster;
import jStrg.network_management.storage_management.cluster.GoogleCloudBucketCluster;
import jStrg.network_management.storage_management.cluster.StorageCluster;
import jStrg.network_management.storage_management.core.ClusterManager;
import jStrg.network_management.storage_management.core.Location;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Entity
public class Application extends DatabaseEntity {

    private final static Logger LOGGER = Logger.getLogger(Settings.location_logging_target);
    // ATTRIBUTES
    public String m_title;
    @Transient
    public ClusterManager m_cluster_manager;
    @OneToOne
    public Settings m_setting;
    @OneToOne
    public AmazonS3BucketCluster m_amazon_s3_bucket_cluster = null;

    // RELATIONS
    @OneToOne
    public GoogleCloudBucketCluster m_google_cloud_bucket_cluster = null;
    @OneToOne
    public StorageCluster m_local_storage_cluster = null;
    @OneToOne
    public DiskCluster m_disk_storage_cluster = null;
    // CONSTRUCTORS
    public Application() {
    }
    public Application(String _title) {
        init_application(_title, null);
    }

    public Application(String _title, String _network_interface) {
        init_application(_title, _network_interface);
    }

    public void init_application(String _title, String _network_interface){
        try {
            // fill instance with values
            this.m_title = _title;

            // Generating settings
            Settings setting = new Settings(0, this.m_title);


            this.m_setting = setting;

            if (setting != null) {
                dao().create(this);

                if (setting.m_use_local_cluster_location) {
                    StorageCluster local_storage_cluster = new StorageCluster(this, _network_interface);
                    m_local_storage_cluster = local_storage_cluster;
                }
                if (setting.m_use_disk_cluster_location) {
                    DiskCluster disk_storage_cluster = new DiskCluster(this);
                    m_disk_storage_cluster = disk_storage_cluster;
                }

                if (setting.m_use_google_location) {
                    GoogleCloudBucketCluster google_cloud_bucket_cluster = new GoogleCloudBucketCluster(this);
                    m_google_cloud_bucket_cluster = google_cloud_bucket_cluster;
                }

                if (setting.m_use_s3_location) {
                    AmazonS3BucketCluster amazon_s3_bucket_cluster = new AmazonS3BucketCluster(this);
                    m_amazon_s3_bucket_cluster = amazon_s3_bucket_cluster;
                }

                m_cluster_manager = new ClusterManager(this);

                User operator = new User(0, Role.find_by_title("operator").get_id(), m_title.toLowerCase(), m_title.toLowerCase(), 0, 0, 0, this);
            } else {
                LOGGER.info("Application Operator User couldnt be created.");
            }
        } catch (NullPointerException e) {
            LOGGER.info("Send a cookie to the developers. They are very stressed.");
        } finally {
            if (this.m_title != null && !this.m_title.equals("")) {
                dao().create(this);
            }
        }
    }

    // DATABASE TRANSACTIONS

    /**
     * query for all applications that are known by this jstrg master (see: configuration instructions)
     *
     * @return list of applications
     */

    public static List<Application> all() {
        return dao().findAll();
    }

    // comment me
    private static IGenericDao dao() {
        return Environment.data().get_dao(Application.class);
    }

    /**
     * dao with specific funktions for this class
     *
     * @return applicationdao
     */
    private static IApplicationDao specific_dao() {
        return Environment.data().get_dao_application();
    }

    /**
     * deletes all entrys in database for this class
     */
    public static void delete_all() {
        for (Application app : all()) {
            specific_dao().delete(app);
        }
    }

    /**
     * finds an application by its title
     * @param _title application title
     * @return Application Object if it could be found
     */
    public static Application find_by_title(String _title) {
        return specific_dao().find_by_title(_title);
    }

    /**
     * query for an application by its ID
     *
     * @param _application_id int
     * @return FileFolder
     */
    public static Application find(int _application_id) {
        return (Application) dao().findById(_application_id);
    }

    /**
     * query for the last application known by this jstrg master
     *
     * @return Application
     */

    public static Application last() {
        List<Application> applications = dao().findAll();
        return (applications.size() != 0) ? applications.get(applications.size() - 1) : null;
    }

    /**
     * query for the first application known by this jstrg master
     *
     * @return Application
     */

    public static Application first() {
        List<Application> applications = dao().findAll();
        return (applications.size() != 0) ? applications.get(0) : null;
    }

    /**
     * query for an application by its portnumber
     *
     * @param _port int
     * @return Application
     */
    public static Application find_by_port(int _port) {
        Application app = null;
        for (Application application : Application.all()) {
            if (application.m_setting.m_network_communication_port == _port) {
                app = application;
            }
        }
        return app;
    }

    public void db_update() {
        dao().update(this);
    }

    /**
     * query for all subservers registered to this application
     *
     * @return list of subservers
     */
//    public List<StorageServer> storage_servers() {
//        List<StorageServer> subservers = new ArrayList<StorageServer>();
//        for (StorageServer subserver : StorageServer.all_inheritance()) {
//            if (subserver.application() == this) {
//                subservers.add(subserver);
//            }
//        }
//        return subservers;
//    }

    /**
     * query for all users of this application
     *
     * @return list of users
     */
    public List<User> users() {
        return User.all(this);
    }

    /**
     * query for all files of this application (this query might be inefficient and take a while)
     *
     * @return SET of files...........
     */
    public Set<Integer> files() {
        Set<Integer> returnset = new LinkedHashSet<>();
        for (File file : File.all()) {
            if (file.application().get_id() == this.m_id) {
                returnset.add(file.get_id());
            }
        }
        return returnset;
    }

    /**
     * query for all file folders of this application
     *
     * @return list of file folders
     */
    public List<FileFolder> file_folders() {
        List<FileFolder> file_folders = new ArrayList<FileFolder>();
        for (FileFolder folder : FileFolder.all()) {
            if (folder.m_application.get_id() == m_id) {
                file_folders.add(folder);
            }
        }
        return file_folders;
    }

    // HELPER

    /**
     * query for all locations that are allowed for this application
     *
     * @return list of Locations
     */
    public List<Location> locations() {
        // TODO: implement
        List<Location> locations = new ArrayList<Location>();
        return locations;
    }

    public int get_id() {
        return m_id;
    }

    public String toString() {
        return "<Application::{m_id: " + m_id + ", m_title: " + m_title + "}>";
    }


    public boolean has_storagepool(Location.TYPE _location_type) {
        for (Location location : this.locations()) {
            if (location.get_type() == _location_type) {
                return true;
            }
        }
        return false;
    }

//    public StorageServer get_free_storage_server() {
//
//        StorageServer server_with_most_free_space = null;
//        for (StorageServer server : this.storage_servers()) {
//            if (server_with_most_free_space == null) {
//                server_with_most_free_space = server;
//            } else {
//                if (server_with_most_free_space.m_unused_space < server.m_unused_space) {
//                    server_with_most_free_space = server;
//                }
//            }
//        }
//        return server_with_most_free_space;
//    }
}
