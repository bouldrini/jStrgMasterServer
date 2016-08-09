package jStrg.network_management.core;

import jStrg.database.DatabaseEntity;
import jStrg.database.IGenericDao;
import jStrg.environment.Environment;
import jStrg.file_system.Settings;

import javax.persistence.Entity;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a Server in the Local Network Environment of jStrg
 * - multiple Local StorageCell Server used by jStrg
 */

@Entity
public class Server extends DatabaseEntity {

    // ATTRIBUTES
    public final static Logger LOGGER = Logger.getLogger(Settings.location_logging_target);
    public String m_ip_address;
    public String m_servername;
    public String m_port;
    public Server.TYPE m_type;
    public String m_path_prefix;
    public String m_network_interface;
    // CONSTRUCTORS
    public Server() {
    }
    public Server(String _servername, String _ip_address, String _port, String _network_interface) throws IOException {
        init_server(_servername, _ip_address, _port, _network_interface);
        dao().create(this);
    }

    /**
     * get dao object of this class
     *
     * @return dao object
     */
    public static IGenericDao dao() {
        return Environment.data().get_dao(Server.class);
    }

    /**
     * deletes all entrys in database for this class
     */
    public static void delete_all() {
        dao().deleteAll();
    }

    // DATABASE

    public static List<Server> all() {
        return dao().findAll();
    }

    public void init_server(String _servername, String _ip_address, String _port, String _network_interface) {
        this.m_ip_address = _ip_address;
        try {
            if (this.ping()) {
                this.m_servername = _servername;
                this.m_port = _port;
                this.m_type = TYPE.STORAGE;
                this.m_path_prefix = "";
            }
        } catch (IOException e) {
            LOGGER.warning("================================================");
            LOGGER.warning("================================================");
            LOGGER.finest("Failed to create the Server");
            LOGGER.finest("Invalid IP Address");
            LOGGER.warning("================================================");
            LOGGER.warning("================================================");
        }
    }

    // HELPER

    /**
     * pinging the server
     *
     * @return true if the server ist reachable
     * @throws IOException intenal isReachable(4000)
     */
    public boolean ping() throws IOException {
        boolean result = false;
        if (this.ip_address().isReachable(4000)) {
            result = true;
        }
        ;
        return result;
    }

    public InetAddress ip_address() throws UnknownHostException {
        return Inet4Address.getByName(m_ip_address);
    }

    public String toString() {
        return "<Server::{m_id: " + this.m_id + ", m_ip_address: " + this.m_ip_address + ", m_servername: " + this.m_servername + ", m_port: '" + this.m_port + "', m_type: " + this.m_type + ", m_path_prefix: " + this.m_path_prefix + "}>";
    }

    public enum TYPE {DATABASE, STORAGE, SUBSERVER}
}
