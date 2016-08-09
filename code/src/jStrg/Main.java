package jStrg;

// REQUIREMENTS
// EXTERNAL

import jStrg.environment.Environment;
import jStrg.file_system.*;
import jStrg.network_management.core.Server;
import jStrg.network_management.core.SocketListener;
import jStrg.simple_console.Console;

import java.io.IOException;
import java.sql.SQLException;

// INTERNAL
//import jStrg.network_management.core.SocketListener;

public class Main {
    public static Server server;

    public static void main(String[] args) throws SQLException, IOException {

        // initialize environment and read config
        Environment.startup();

        // USE DATABASE
//        jstrg_db.migrate();
//        jstrg_db.populate();

        // SETUP TEST ENVIRONMENT
        Environment.seed(true);

        // HANDLING REQUESTS
        for (Application application : Application.all()) {
            Settings settings = application.m_setting;
            if (settings.m_use_local_cluster_location) {
                SocketListener listener = new SocketListener(application, settings.m_network_communication_port);
                listener.listen();
            }
        }

        try {
            //Console.init_console();
            Console my_console = Console.getInstance(Application.last().get_id());
            my_console.init_console();
        } catch (IOException e) {
            Settings.LOGGER.severe("failure while init console: " + e);
        }
    }
}
