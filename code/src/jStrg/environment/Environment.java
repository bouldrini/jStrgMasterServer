package jStrg.environment;

// REQUIREMENTS
//import com.sun.tools.doclint.Env;

import jStrg.Main;
import jStrg.communication_management.external_communication.core.FloatingTransaction;
import jStrg.data_types.exceptions.ConfigException;
import jStrg.database.Data;
import jStrg.file_system.*;
import jStrg.network_management.core.Server;
import jStrg.network_management.storage_management.config.LocationConfig;
import jStrg.tests.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.SimpleFormatter;

public interface Environment {

    static Data db = new Data();

    List<FloatingTransaction> FLOATING_TRANSACTIONS = new ArrayList<FloatingTransaction>();
    // GENERATING TESTDATA

    /**
     * Seeding Testservers
     *
     * @param _commented boolean seed with or without comments
     * @return boolean
     * @throws IOException internal instance method call on Server object: server.ping()
     */
    public static boolean seed_servers(boolean _commented) throws IOException {
        boolean result = false;
        if (_commented) {
            System.out.println("===========================================================================================");
            System.out.println("Creating SERVERS...");
            System.out.println("===========================================================================================");
        }

        Server jstrg_master_server = new Server("jStrg Master Server", "127.0.0.1", "3000", null);
        Main.server = jstrg_master_server;

        if (_commented) {
            System.out.println(jstrg_master_server);
        }

        /*Database jstrg_main_database = new Database("jStrgDB Server", "127.0.0.1", "5000", "jstrgdb", "jstrg", "jstrg");
        Main.jstrg_db = jstrg_main_database;
        Environment.SERVERS.add(jstrg_main_database);
        if (_commented) {
            System.out.println(jstrg_main_database);
        }
        */
        return result;
    }

    /**
     * Seeding Testdata
     *
     * @param _commented boolean seed with or without comments
     * @return boolean
     * @throws IOException some reason
     */
    public static boolean seed(boolean _commented) throws IOException {
        if (!Settings.m_dev_enable_seed) {
            return true;
        }
        boolean result = true;
        Environment.seed_servers(_commented);

        Application something_like_dropbox = new Application("SomethingLikeDropbox");
        System.out.println(something_like_dropbox);

        if(something_like_dropbox.m_setting.m_use_local_cluster_location){
            LocationConfig config = new LocationConfig();
            config.set_application(something_like_dropbox);
            config.set_ip_address("127.0.0.1");
            config.set_port("3020");
            config.set_servername("Dropbox Subserver 1");
            config.set_network_interface("eth0");
            config.set_path("127.0.0.1");
            something_like_dropbox.m_local_storage_cluster.register(config);
        }

        try {
            helper.create_locations(something_like_dropbox);
        } catch (ConfigException e) {
            e.printStackTrace();
        }

        if (_commented) {
            System.out.println("");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!GENERALS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("");
            System.out.println("===========================================================================================");
            System.out.println("Creating ROLES...");
            System.out.println("===========================================================================================");
        }

        Role admin_role = new Role(0, "admin");
        Role operator_role = new Role(0, "operator");
        Role user_role = new Role(0, "user");

        for (Role Role : jStrg.file_system.Role.all()) {
            if (_commented) {
                System.out.println(Role);
            }
        }

        if (_commented) {
            System.out.println("");
            System.out.println("===========================================================================================");
            System.out.println("Creating FILETYPES...");
            System.out.println("===========================================================================================");
        }

        FileType css = new FileType(0, "css");
        FileType js = new FileType(0, "js");
        FileType html = new FileType(0, "html");
        FileType java = new FileType(0, "java");
        FileType c = new FileType(0, "c");
        FileType txt = new FileType(0, "txt");

        for (FileType file_type : FileType.all()) {
            if (_commented) {
                System.out.println(file_type);
            }
        }

        User admin = new User("admin", "jstrg");

        System.out.println("");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!APPLICATION SPECIFIC!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        if (_commented) {
            System.out.println("");
            System.out.println("===========================================================================================");
            System.out.println("Creating TEST APPLICATION...");
            System.out.println("===========================================================================================");
        }



        if (_commented) {

            System.out.println("");
            System.out.println("===========================================================================================");
            System.out.println("Creating USERS...");
            System.out.println("===========================================================================================");
        }

        User application_operator = new User(0, "somethinglikedropbox", "somethinglikedropbox", something_like_dropbox);

        int max_users = 5;
        int max_file_folders = 1;
        int max_files = 3;
        int max_file_types = 2;

        for (int i = 1; i <= max_users; i++) {
            User user = new User(0, Role.find_by_title("user").get_id(), "Dropboxuser " + i, "jstrg", 0, 2000000000, 2000000000, something_like_dropbox);
            if (_commented) {
                System.out.println("" + user);
                System.out.println("Creating USERS FILEFOLDERS...");
                System.out.println("");
            }
            for (int n = 0; n < max_file_folders; n++) {
                FileFolder folder = new FileFolder(user.get_rootfolder(), "folder_" + n);

                for (int k = 0; k < max_file_types; k++) {
                    Random index = new Random();
                    folder.m_file_types.add(FileType.last());
                    folder.db_update();
                }

                Privilege privilege;
                if (folder.privileges().size() != 0) {
                    privilege = folder.privileges().get(0);
                    folder = privilege.privilegable();
                }

                if (_commented) {
                    System.out.println("\t" + folder);
                    System.out.println("\tCreating FILES for users FILEFOLDER");
                    System.out.println("");
                }

                for (int k = 0; k < max_files; k++) {
//                    File file = new File(0, folder.get_id(), user.get_id(), "File " + k, folder.file_types().get(0).m_file_extension, something_like_dropbox);
                    File file = new File(folder, "file" + k+ ".txt");

                    privilege = user.privilege_for(file);

                    if (privilege != null) {
                        file = privilege.privilegable();
                    }
                    if (_commented) {
                        System.out.println("\t\t" + file);
                    }
                }
                if (_commented) {
                    System.out.println("===========================================================================================");
                }
            }
            helper.fill_files(user);
        }

        User user_empty = new User(0, Role.find_by_title("user").get_id(), "LeerDropboxuser", "jstrg", 0, 0, 2000000000, something_like_dropbox);
        user_empty = null;

        if (_commented) {
            System.out.println("");
            System.out.println("===========================================================================================");
            System.out.println("Creating FILE LOCATIONS...");
            System.out.println("===========================================================================================");
        }




        if (_commented) {

            System.out.println("");
            System.out.println("===========================================================================================");
            System.out.println("Creating FILEVERSIONS...");
            System.out.println("===========================================================================================");
        }

        return result;
    }


    /**
     * initialization of the main program, everthing that is need at startup
     */
    static void startup() {
        Settings.read_global_config();
        Environment.init_logger();
    }

    /**
     * initializes LOGGER as configured
     */
    static void init_logger() {
        try {
            Handler handler = new FileHandler(Settings.logging_target, true);
            handler.setFormatter(new SimpleFormatter());
            Settings.LOGGER.addHandler(handler);
            Settings.LOGGER.setLevel(Settings.logging_level);
            Settings.LOGGER.info("<--- Process startup --->");
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    /**
     * Access data controller, should be used to get dao Objects
     *
     * @return Data Controller
     */
    static Data data() {
        return db;
    }
}
