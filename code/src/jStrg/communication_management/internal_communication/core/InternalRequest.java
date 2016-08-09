package jStrg.communication_management.internal_communication.core;

import jStrg.file_system.Application;
import jStrg.network_management.storage_management.internal.StorageServer;

/**
 * Baseclass for Requests to be send to subservers
 */
public class InternalRequest {

    // CONSTRUCTORS
    public InternalRequest(InternalRequest.type _type, StorageServer _sub_server){
        m_type = _type;
        m_application = _sub_server.m_cluster.application();
        m_subserver = _sub_server;
    }



    // CONSTANTS
    public enum type{DOWNLOAD_FILE, UPLOAD_FILE, CREATE_USER, DELETE_FILE, DELETE_FOLDER, UPLOAD_FILE_REQUEST};

    // ATTRIBUTES
    public InternalRequest.type m_type;
    public StorageServer m_subserver;
    public Application m_application;
}
