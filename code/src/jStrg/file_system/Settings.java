package jStrg.file_system;

// REQUIREMENTS

import jStrg.database.DatabaseEntity;
import jStrg.database.IGenericDao;
import jStrg.environment.Environment;
import jStrg.network_management.storage_management.core.ClusterManager;

import javax.persistence.Entity;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Entity
public class Settings extends DatabaseEntity {

    public static final String m_password_hash_alg = "PBKDF2WithHmacSHA512";

    ;
    public static final int m_password_hash_iterations = 1024;
    public static final int m_password_salt_length = 32;
    public static final int m_password_key_length = 512;
    // ATTRIBUTES
    public static boolean m_start_server = true;
    public static boolean m_use_console = false;
    // General Settings
    public static int bytes_per_upload_chunk = 32768 * 20;
    public static int bytes_per_upload_chunk_for_google = 640 * 1024; // 640 KB
    public static Logger LOGGER = Logger.getLogger("jstrg");
    public static String logging_target = "/tmp/jstrg.log";
    public static Level logging_level = Level.FINEST;
    public static String location_logging_target = "jstrg";
    public static String console_logging_target = "jstrg";
    public static String default_hashing_algorithm = "SHA-512";
    public static String user_folder_root = "";
    public static String m_db_server = "localhost";
    public static String m_db_port = "3306";
    public static String m_db_username;
    public static String m_db_password;
    public static String m_db_database;
    public static String m_db_loglevel = "INFO";
    /*
    *   WARNING, variables with "m_dev" are only for development!!!!! dont use in production code
    *
    * */
    public static boolean m_dev_enable_seed = true;
    public static boolean m_dev_use_s3_location = false;
    public static boolean m_dev_use_google_location = false;
    public static boolean m_dev_use_disk_location = true;
    // s3
    public static long m_dev_bytes_per_s3_bucket = 5368709120L; // 5GB default (free of charge for developers)
    public static String m_dev_s3_aws_access_key_id = "";
    public static String m_dev_s3_aws_access_key = "";
    public static String m_dev_default_s3_location = "";
    // google
    public static long m_dev_bytes_per_google_bucket = 5368709120L; // 5GB
    public static String m_dev_default_google_bucket = "";
    public static String m_dev_default_google_projectid = "";
    public static String m_dev_default_google_credentials = "";
    // disk and cache
    public static String m_dev_default_disk_location = "/tmp/";

    // DbController
    public static long m_dev_bytes_per_disk = 0L;
    public static String m_dev_default_cache_location = "/tmp/";
    // local path to repository
    public static String m_dev_repository_path = "";
    // Bucket Sizes
    public long m_bytes_per_user;
    public long m_bytes_per_s3_bucket;
    public long m_bytes_per_google_bucket;
    // Application Communication
    public int m_network_communication_port;

    // password factory
    public String m_network_communication_secret1;
    public String m_network_communication_secret2;
    // External Storages
    // s3 location config
    public boolean m_use_s3_location = Settings.m_dev_use_s3_location;
    public String m_default_s3_location = Settings.m_dev_default_s3_location;

    // -----------------------------------------
    // Develpoment funktions, do not use, only for tests!!!
    // -----------------------------------------
    public String m_s3_aws_access_key_id = Settings.m_dev_s3_aws_access_key_id;
    public String m_s3_aws_access_key = Settings.m_dev_s3_aws_access_key;
    // Google Location
    public boolean m_use_google_location = Settings.m_dev_use_google_location;
    public String m_default_google_bucket = Settings.m_dev_default_google_bucket;
    public String m_default_google_projectid = Settings.m_dev_default_google_projectid;
    public String m_default_google_credentials = Settings.m_dev_default_google_credentials;
    // disk location config
    public boolean m_use_disk_location = Settings.m_dev_use_disk_location;
    public String m_default_disk_location = Settings.m_dev_default_disk_location;
    // local cluster location config
    public boolean m_use_local_cluster_location = false;
    public boolean m_use_disk_cluster_location = Settings.m_dev_use_disk_location;
    public ClusterManager.PLACEMENT_MODE m_placement_mode = ClusterManager.PLACEMENT_MODE.MIRRORED;
    // temp location config
    public String m_default_cache_location = Settings.m_dev_default_cache_location;
    // Internal Communication
    private String m_internal_communication_secret;
    // CONSTRUCTORS
    public Settings() {
    }
    public Settings(int _id, String _app_title) {
        if (this.read_app_config(_app_title)) {
            // attach new instance to fakeDB
            dao().create(this);
        } else {
            throw new NullPointerException("ConfigFileNotFound"); // TODO must be catched
        }
        ;
        dao().create(this);
    }

    // -----------------------------------------
    //              END
    // -----------------------------------------

    // comment me
    private static IGenericDao dao() {
        return Environment.data().get_dao(Application.class);
    }

    // DATABASE TRANSACTIONS

    /**
     * query for all settings existing in the database
     *
     * @return List of Settings
     */
    public static List<Settings> all() {
        return dao().findAll();
    }

    /**
     * deletes all entrys in database for this class
     */
    public static void delete_all() {
        dao().deleteAll();
    }

    /**
     * query for settings by ID
     *
     * @param _id int
     * @return Settings
     */
    public static Settings find(int _id) {
        Settings settings = null;
        for (Settings cur_settings : Settings.all()) {
            if (cur_settings.m_id == _id) settings = cur_settings;
        }
        ;
        return settings;
    }

    // HELPER

    /**
     * Takes the logging level as string and builds the proper Level. Defaults to Level.ALL
     *
     * @param _level the logging level case insensitive.
     * @return loglevel that can be passed to logging facility
     */
    private static Level get_log_level(String _level) {
        _level = _level.toLowerCase();
        Level level;
        switch (_level) {
            case "finest":
                level = Level.FINEST;
                break;
            case "fine":
                level = Level.FINE;
                break;
            case "info":
                level = Level.INFO;
                break;
            case "warning":
                level = Level.WARNING;
                break;
            case "severe":
                level = Level.SEVERE;
                break;
            default:
                level = Level.ALL;
        }
        return level;
    }

    /**
     * Updates default config. Values are read from $HOME/.jstrg.config
     *
     * @return success
     */
    public static boolean read_global_config() {
        FileInputStream inputStream;
        boolean result = false;
        try {
            Properties prop = new Properties();
            String propFileName = System.getProperty("user.home") + "/.jstrg.conf";
            inputStream = new FileInputStream(propFileName);
            prop.load(inputStream);
            inputStream.close();

            if (prop.getProperty("loglevel") != null) {
                logging_level = get_log_level(prop.getProperty("loglevel"));
            }
            if (prop.getProperty("logfile") != null) {
                Settings.logging_target = prop.getProperty("logfile");
            }
            if (prop.getProperty("user_folder") != null) {
                if (Files.exists(Paths.get(prop.getProperty("user_folder"))) && Files.isDirectory(Paths.get(prop.getProperty("user_folder")))) {
                    Settings.user_folder_root = prop.getProperty("user_folder");
                } else {
                    Settings.LOGGER.warning("user_folder: " + prop.getProperty("user_folder") + " doesn't exist. Emptying string.");
                }
            }
            if (prop.getProperty("console") != null) {
                if (prop.getProperty("console").toLowerCase().equals("yes")) {
                    m_use_console = true;
                }
            }
            if (prop.getProperty("networkserver") != null) {
                if (prop.getProperty("networkserver").toLowerCase().equals("no")) {
                    m_start_server = false;
                }
            }
            if (prop.getProperty("db_server") != null) {
                m_db_server = prop.getProperty("db_server");
            }
            if (prop.getProperty("db_port") != null) {
                m_db_port = prop.getProperty("db_port");
            }
            if (prop.getProperty("db_username") != null) {
                m_db_username = prop.getProperty("db_username");
            }
            if (prop.getProperty("db_password") != null) {
                m_db_password = prop.getProperty("db_password");
            }
            if (prop.getProperty("db_database") != null) {
                m_db_database = prop.getProperty("db_database");
            }
            if (prop.getProperty("db_loglevel") != null) {
                m_db_loglevel = prop.getProperty("db_loglevel");
            }
            // development options, these have no effects and should not be used in production

            if (prop.getProperty("disable_seed") != null) {
                if (prop.getProperty("disable_seed").toLowerCase().equals("yes")) {
                    m_dev_enable_seed = false;
                }
            }
            if (prop.getProperty("bytes_per_s3_bucket") != null) {
                m_dev_bytes_per_s3_bucket = Long.parseLong(prop.getProperty("bytes_per_s3_bucket"));
            }
            if (prop.getProperty("bytes_per_google_bucket") != null) {
                m_dev_bytes_per_google_bucket = Long.parseLong(prop.getProperty("bytes_per_google_bucket"));
            }
            if (prop.getProperty("s3") != null) {
                if (prop.getProperty("s3").toLowerCase().equals("yes")) {
                    m_dev_use_s3_location = true;
                }
            }
            if (prop.getProperty("disk") != null) {
                switch (prop.getProperty("disk").toLowerCase()) {
                    case "yes":
                        m_dev_use_disk_location = true;
                        break;
                    case "no":
                        m_dev_use_disk_location = false;
                        break;
                    default:
                        break;
                }
            }
            if (prop.getProperty("s3_default") != null) {
                m_dev_default_s3_location = prop.getProperty("s3_default");
            }
            if (prop.getProperty("disk_default") != null) {
                m_dev_default_disk_location = prop.getProperty("disk_default");
            }
            if (prop.getProperty("bytes_per_disk") != null) {
                m_dev_bytes_per_disk = Long.parseLong(prop.getProperty("bytes_per_disk"));
            }
            if (prop.getProperty("cache_default") != null) {
                m_dev_default_cache_location = prop.getProperty("cache_default");
            }
            if (prop.getProperty("gcloud") != null) {
                if (prop.getProperty("gcloud").toLowerCase().equals("yes")) {
                    m_dev_use_google_location = true;
                }
            }
            if (prop.getProperty("gcloud_bucket") != null) {
                m_dev_default_google_bucket = prop.getProperty("gcloud_bucket");
            }
            if (prop.getProperty("gcloud_projectid") != null) {
                m_dev_default_google_projectid = prop.getProperty("gcloud_projectid");
            }
            if (prop.getProperty("gcloud_json_credentials") != null) {
                m_dev_default_google_credentials = prop.getProperty("gcloud_json_credentials");
            }
            if (prop.getProperty("bytes_per_google_bucket") != null) {
                m_dev_bytes_per_google_bucket = Long.parseLong(prop.getProperty("bytes_per_google_bucket"));
            }
            if (prop.getProperty("s3_aws_access_key_id") != null) {
                m_dev_s3_aws_access_key_id = prop.getProperty("s3_aws_access_key_id");
            }
            if (prop.getProperty("s3_aws_access_key") != null) {
                m_dev_s3_aws_access_key = prop.getProperty("s3_aws_access_key");
            }
            if (prop.getProperty("repository_path") != null) {
                m_dev_repository_path = prop.getProperty("repository_path");
            }
            result = true;
        } catch (FileNotFoundException e) {
            System.out.println("no config file found: " + e);
        } catch (IOException e) {
            System.out.println("Exception while accessing config file: " + e);
        }
        return result;
    }

    /**
     * Updates Application config. Values are read from $HOME/.jstrg-<appname>.config
     *
     * @param _appname application name
     * @return success
     */
    private boolean read_app_config(String _appname) {
        FileInputStream inputStream;
        boolean result = false;
        try {
            Properties prop = new Properties();
            String propFileName = System.getProperty("user.home") + "/.jstrg-" + _appname.toLowerCase() + ".conf";
            inputStream = new FileInputStream(propFileName);
            prop.load(inputStream);
            inputStream.close();

            if (prop.getProperty("network_communication_port") != null) {
                m_network_communication_port = Integer.parseInt(prop.getProperty("network_communication_port"));
            }
            if (prop.getProperty("network_communication_secret1") != null) {
                m_network_communication_secret1 = prop.getProperty("network_communication_secret1");
            }
            if (prop.getProperty("network_communication_secret2") != null) {
                m_network_communication_secret2 = prop.getProperty("network_communication_secret2");
            }

            if (prop.getProperty("internal_communication_secret") != null) {
                m_internal_communication_secret = prop.getProperty("gcloud_bucket");
            }
            if (prop.getProperty("bytes_per_user") != null) {
                m_bytes_per_user = Long.parseLong(prop.getProperty("bytes_per_user"));
            }
            if (prop.getProperty("bytes_per_s3_bucket") != null) {
                m_bytes_per_s3_bucket = Long.parseLong(prop.getProperty("bytes_per_s3_bucket"));
            }
            if (prop.getProperty("bytes_per_google_bucket") != null) {
                m_bytes_per_google_bucket = Long.parseLong(prop.getProperty("bytes_per_google_bucket"));
            }
            if (prop.getProperty("s3") != null) {
                if (prop.getProperty("s3").toLowerCase().equals("yes")) {
                    m_use_s3_location = true;
                }
            }
            if (prop.getProperty("local_cluster") != null) {
                if (prop.getProperty("local_cluster").toLowerCase().equals("yes")) {
                    m_use_local_cluster_location = true;
                }
            }
            if (prop.getProperty("disk") != null) {
                switch (prop.getProperty("disk").toLowerCase()) {
                    case "yes":
                        m_use_disk_location = true;
                        break;
                    case "no":
                        m_use_disk_location = false;
                        break;
                    default:
                        break;
                }
            }
            if (prop.getProperty("s3_default") != null) {
                m_default_s3_location = prop.getProperty("s3_default");
            }
            if (prop.getProperty("disk_default") != null) {
                m_default_disk_location = prop.getProperty("disk_default");
            }
            if (prop.getProperty("cache_default") != null) {
                m_default_cache_location = prop.getProperty("cache_default");
            }
            if (prop.getProperty("gcloud") != null) {
                if (prop.getProperty("gcloud").toLowerCase().equals("yes")) {
                    m_use_google_location = true;
                }
            }
            if (prop.getProperty("gcloud_bucket") != null) {
                m_default_google_bucket = prop.getProperty("gcloud_bucket");
            }
            if (prop.getProperty("gcloud_projectid") != null) {
                m_default_google_projectid = prop.getProperty("gcloud_projectid");
            }
            if (prop.getProperty("gcloud_json_credentials") != null) {
                m_default_google_credentials = prop.getProperty("gcloud_json_credentials");
            }
            if (prop.getProperty("bytes_per_google_bucket") != null) {
                m_bytes_per_google_bucket = Long.parseLong(prop.getProperty("bytes_per_google_bucket"));
            }
            if (prop.getProperty("s3_aws_access_key_id") != null) {
                m_s3_aws_access_key_id = prop.getProperty("s3_aws_access_key_id");
            }
            if (prop.getProperty("s3_aws_access_key") != null) {
                m_s3_aws_access_key = prop.getProperty("s3_aws_access_key");
            }
            result = true;

        } catch (FileNotFoundException e) {
            System.out.println("config file for " + _appname + " not found: " + e);
        } catch (IOException e) {
            System.out.println("Exception while accessing config file: " + e);
        }
        //TODO commented out ridiculous long screen output, please cut or do a multiple line output..
        // System.out.println(this); -- too long for my screen, please find a better solution
        return result;
    }

    public String toString() {
        StringBuilder returnstring = new StringBuilder("<Settings::{");
        returnstring.append("m_id: " + m_id);
        returnstring.append(", m_bytes_per_user: " + m_bytes_per_user);
        returnstring.append(", m_bytes_per_s3_bucket: " + m_bytes_per_s3_bucket);
        returnstring.append(", m_bytes_per_google_bucket: " + m_bytes_per_google_bucket);

        returnstring.append(", m_network_communication_port: " + m_network_communication_port);
        returnstring.append(", m_network_communication_secret1 : " + m_network_communication_secret1);
        returnstring.append(", m_network_communication_secret2: " + m_network_communication_secret2);

        returnstring.append(", m_internal_communication_secret: " + m_internal_communication_secret);

        returnstring.append(", m_use_s3_location: '" + m_use_s3_location);
        returnstring.append(", m_default_s3_location: '" + m_default_s3_location);

        returnstring.append(", m_use_google_location: '" + m_use_google_location);
        returnstring.append(", m_default_google_bucket: '" + m_default_google_bucket);
        returnstring.append(", m_default_google_projectid: '" + m_default_google_projectid);
        returnstring.append(", m_default_google_creadentials: '" + m_default_google_credentials);

        returnstring.append(", m_use_disk_location: '" + m_use_disk_location);
        returnstring.append(", m_default_disk_location: '" + m_default_disk_location);
        returnstring.append(", m_default_cache_location: '" + m_default_cache_location);

        returnstring.append(", m_use_console: '" + m_use_console);

        return returnstring.append(" }>").toString();
    }
}


