package jStrg.communication_management.external_communication.core;

import jStrg.file_system.Application;
import jStrg.file_system.User;
import jStrg.network_management.core.Cryptor;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;

/**
 * Baseclass for every Request
 * includes string based based parsing logic
 */
public class ExternalRequest extends Thread implements Runnable {

    // CONSTANTS
    public enum type{ DOWNLOAD_FILE, UPLOAD_FILE, CREATE_USER, DELETE_FILE, DELETE_FOLDER, UPLOAD_FILE_REQUEST }

    // ATTRIBUTES
    public User m_user;
    public Socket m_socket;
    public ExternalRequest.type m_request_type;
    public boolean m_status;
    public String m_request_string;
    public Thread m_thread;
    public Application m_application;
    // CONSTRUCTORS
    public ExternalRequest(Socket _socket, String _client_request, Application _application) throws GeneralSecurityException, IOException {
        String username = "";
        String password = "";
        String key = "";
        String value = "";
        m_application = _application;
        m_request_string = Cryptor.decrypt(m_application.m_setting.m_network_communication_secret1, m_application.m_setting.m_network_communication_secret2, _client_request);
        for (String line : m_request_string.split(";")) {
            key = line.split(":")[0];
            value = line.split(":")[1];
            if (key.equals("username")) {
                username = value;
            } else if (key.equals("password")) {
                password = value;
            } else if (key.equals("request_type")) {
                if (value.equals(type.DOWNLOAD_FILE.toString())) {
                    m_request_type = type.DOWNLOAD_FILE;
                } else if (value.equals(type.UPLOAD_FILE.toString())) {
                    m_request_type = type.UPLOAD_FILE;
                } else if (value.equals(type.CREATE_USER.toString())) {
                    m_request_type = type.CREATE_USER;
                } else if (value.equals(type.DELETE_FILE.toString())) {
                    m_request_type = type.DELETE_FILE;
                } else if (value.equals(type.DELETE_FOLDER.toString())) {
                    m_request_type = type.DELETE_FOLDER;
                } else if (value.equals(type.UPLOAD_FILE_REQUEST.toString())) {
                    m_request_type = type.UPLOAD_FILE_REQUEST;
                }
            }
        }

        if (User.authenticate(username, password, m_application)) {
            m_user = User.find_by_name(username, m_application);
            m_status = true;
            m_socket = _socket;
        } else {
            m_status = false;
        }
    }

    @Override
    public void run() {
    }

    public void start() {
        if (m_thread == null) {
            m_thread = new Thread(this, "Requestthread");
            m_thread.start();
        }
    }
}
