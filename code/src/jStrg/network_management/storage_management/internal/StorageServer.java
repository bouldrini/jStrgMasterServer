package jStrg.network_management.storage_management.internal;

import jStrg.communication_management.internal_communication.core.InternalAnswer;
import jStrg.communication_management.internal_communication.requests.UploadFileInternalRequest;
import jStrg.communication_management.internal_communication.transactions.uploads.InternalFileUpload;
import jStrg.data_types.exceptions.ConfigException;
import jStrg.database.IUserDao;
import jStrg.environment.Environment;
import jStrg.file_system.FileVersion;
import jStrg.file_system.Settings;
import jStrg.network_management.core.Server;
import jStrg.network_management.storage_management.config.ILocationConfig;
import jStrg.network_management.storage_management.core.Connection;
import jStrg.network_management.storage_management.core.StorageCell;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

@Entity
public class StorageServer extends StorageCell {
    // ATTRIBUTES
    private final static Logger LOGGER = Logger.getLogger(Settings.location_logging_target);
    // RELATIONS

    @OneToOne
    Server m_server;

    @Transient
    public Connection m_current_connection;

    // CONSTRUCTORS
    protected StorageServer() {
    }

    public StorageServer(ILocationConfig _config) throws ConfigException, IOException {
        super(TYPE.SERVER, _config);

        Server server = new Server(_config.get_servername(), _config.get_ip_address(), _config.get_port(), _config.get_network_interface());
        m_server = server;
        m_cluster = _config.get_application().m_local_storage_cluster;
        update_free_space();

    }

    // DATABASE
    private static IUserDao specific_dao() {
        return Environment.data().get_dao_user();
    }

    public Server server() {
        return m_server;
    }

    // HELPER

    @Override
    /**
     * sending a request to the server receiving free available capacity
     */
    protected void update_free_space() {
        // invoke ExternalRequest to the Server asking for free space
        m_free_space = 10000000;
        enter_maintenance();
    }

    @Override
    /**
     * upload file to this server
     */
    public StorageCell write_file(File _file, FileVersion _version) {
        this.connect();
        jStrg.file_system.File file = _version.get_file();

        System.out.println(this);
        System.out.println(file);
        System.out.println(file.get_path());
        System.out.println(file.get_title());

//        UploadFileInternalRequest request = new UploadFileInternalRequest(this, _file, _file.getPath().toString(), _version.get_file().get_title());
//        InternalAnswer answer  = null;
//        try {
//            answer = request.process();
//            if(answer.m_status == InternalAnswer.status.READY_FOR_FILEUPLOAD){
//                System.out.println("SHOULD SEND FILE NOW");
//                InternalFileUpload upload = new InternalFileUpload(this, answer.m_transaction_id, _file, request.m_file_path);
//                answer = upload.process();
//            }
//            this.disconnect();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        }
        return this;
    }

    /**
     * connect to server
     * @return true if connection was established false if connection couldnt be established
     */
    public boolean connect(){
        boolean result = false;
        try {
            Socket client_socket = new Socket(m_server.ip_address().getHostAddress(), Integer.parseInt(m_server.m_port));
            this.m_current_connection = new Connection(client_socket, this);
            System.out.println("=====CONNECTED=====");
        }catch(IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * terminate current connection
     * @return true if the connection terminated false if there was a problem
     */
    public boolean disconnect() throws IOException {
        boolean result = false;
        if(this.m_current_connection != null){
            this.m_current_connection.close();
            this.m_current_connection = null;
            System.out.println("=====DISCONNECTED=====");
            result = true;
        }
        return result;
    }

    /**
     * checks if a file with the checksum is located on the server
     * @param _checksum checksum to compare a file with
     ** @return bool
     */
    @Override
    public boolean contains(String _checksum) throws Exception {

        // Implement RequestType for that in jStrgStorageServer and jStrg Project
        return false;
    }

    /**
     * delete a file with checksum on that server
     * @param _checksum file tot delete
     * @return bool
     */

    @Override
    public boolean delete(String _checksum) {

        // find file by checksum

        Server server = m_server;
        // invoke delete file request and send it to the server

        return false;
    }

    /**
     * uncommented
     * @param _fileversion -
     * @return bool
     */
    @Override
    public File stage_file_to_cache_location(FileVersion _fileversion, String _destination) {
        return null;
    }

    public String toString() {
        return "<StorageServer::{m_id: " + this.get_id() + ", m_network_interface: " + m_server.m_network_interface + "; m_ip_address: " + m_server.m_ip_address + ", m_servername: " + m_server.m_servername + ", m_port: '" + m_server.m_port + "', m_type: " + m_type + "}>";
    }
}
