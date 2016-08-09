package jStrg.communication_management.external_communication.core;

import jStrg.file_system.Application;

import java.io.File;
import java.net.Socket;

/**
 * Baseclass for every Download
 */
public class ExternalDownload {

    // ATTRIBUTES
    public File m_file;
    public Application m_application;
    public String m_transaction_id;
    public Socket m_socket;

    public ExternalDownload(Socket _socket, Application _application, File _file, String _transaction_id){
        m_socket = _socket;
        m_application = _application;
        m_transaction_id = _transaction_id;
    }
}
