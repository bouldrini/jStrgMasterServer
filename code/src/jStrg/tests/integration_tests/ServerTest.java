package jStrg.tests.integration_tests;

import jStrg.environment.Environment;
import jStrg.file_system.Application;
import jStrg.file_system.Settings;
import jStrg.network_management.core.Server;
import jStrg.network_management.storage_management.config.ILocationConfig;
import jStrg.network_management.storage_management.core.Location;
import jStrg.network_management.storage_management.internal.StorageServer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.testng.AssertJUnit.assertTrue;

public class ServerTest {

    @BeforeClass
    public static void onceBefore() {
        Settings.read_global_config();
    }

    @Test
    public void create() throws IOException {
        Environment.seed(false);
        Application app = Application.first();

        ILocationConfig storage_location_config = ILocationConfig.create_configurator();
        storage_location_config.set_application(app);
        storage_location_config.set_ip_address("127.0.0.1");
        storage_location_config.set_network_interface("eth3");
        storage_location_config.set_port("3020");
        storage_location_config.set_servername("Test Sub-Server 1");

        StorageServer subserver = (StorageServer) Location.create_location(Location.TYPE.SERVER, storage_location_config);

        // cant access the m_server attribute since it is casted
        assertTrue(subserver != null);
    }

    @Test
    public void ping_co_servers() throws IOException {
        Environment.seed(false);
        for (Server server : Server.all()) {
            boolean status = false;
            if (server.ping()) {
                System.out.println("<Success::{server: '" + server.m_servername + "', ip_address: '" + server.m_ip_address.toString() + "', reason: 'reachable'}>");
                status = true;
            } else {
                System.out.println("<Error::{server: '" + server.m_servername + "', ip_address: '" + server.m_ip_address.toString() + "', reason: 'unreachable'}>");
                assertTrue(server.ping());
            }

        }
    }
}
