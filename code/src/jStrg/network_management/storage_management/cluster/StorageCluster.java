package jStrg.network_management.storage_management.cluster;

import jStrg.database.IGenericDao;
import jStrg.environment.Environment;
import jStrg.file_system.Application;
import jStrg.file_system.File;
import jStrg.file_system.FileVersion;
import jStrg.network_management.core.Server;
import jStrg.network_management.core.SocketListener;
import jStrg.network_management.storage_management.config.ILocationConfig;
import jStrg.network_management.storage_management.core.Location;
import jStrg.network_management.storage_management.core.StorageCell;
import jStrg.network_management.storage_management.internal.StorageServer;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.net.Socket;

@Entity
/**
 * local servercluster
 */
public class StorageCluster extends Cluster {

    // CONSTRUCTORS
    public StorageCluster() {
    }

    public StorageCluster(Application _application, String _network_interface) {
        super(_application);
        m_network_interface = _network_interface;
        dao().create(this);
    }

    // ATTRIBUTES
    public String m_network_interface;

    @Transient
    public Socket m_socket;

    /**
     * get dao object of this class
     *
     * @return dao object
     */
    public static IGenericDao dao() {
        return Environment.data().get_dao(Server.class);
    }

    /**
     * register a new storage cell to the storagecluster
     * @param _config Locationconfiguration (ILocationConfig)
     * @return Storagecell Object
     */
    public StorageServer register(ILocationConfig _config) {
        StorageServer storage_server = (StorageServer) Location.create_location(Location.TYPE.SERVER, _config);
        this.m_storage_cells.add(storage_server);
        return storage_server;
    }
}
