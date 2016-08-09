package jStrg.communication_management.external_communication.core;

import jStrg.file_system.Application;
import jStrg.file_system.User;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;

/**
 * Baseclass for every Upload
 */
public class ExternalUpload {

    // ATTRIBUTES
    public File m_file;
    public String m_file_name;
    public Application m_application;
    public User m_user;
    public User m_uploader;
    public String m_transaction_id;
    public long m_file_size;
    public String m_file_path;
    public Socket m_socket;

    public ExternalUpload(Socket _socket, String _file_name, String _file_path, long _file_size, User _user, User _uploader, Application _application, String _transaction_id) throws GeneralSecurityException, IOException {
        m_socket = _socket;
        m_file_name = _file_name;
        m_file_size = _file_size;
        m_file_path = _file_path;
        m_user = _user;
        m_uploader = _uploader;
        m_application = _application;
        m_transaction_id = _transaction_id;
    }
}
