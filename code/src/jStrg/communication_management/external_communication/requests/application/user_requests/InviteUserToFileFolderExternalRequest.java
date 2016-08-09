package jStrg.communication_management.external_communication.requests.application.user_requests;

import jStrg.communication_management.external_communication.core.ExternalRequest;
import jStrg.file_system.Application;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;

/**
 * incoming Request to grant another user privileges on a file or a folder by clientside
 */
public class InviteUserToFileFolderExternalRequest extends ExternalRequest {
    // ATTRIBUTES
    public long m_file_size;
    public String m_file_path = "";
    public String m_file_name = "";
    // CONSTRUCTORS
    public InviteUserToFileFolderExternalRequest(Socket _socket, String _client_request_string, Application _application) throws GeneralSecurityException, IOException {
        super(_socket, _client_request_string, _application);
        String key = "";
        String value = "";
        for (String line : m_request_string.split(";")) {
            key = line.split(":")[0];
            value = line.split(":")[1];
            if (key.equals("file_size")) {
                m_file_size = Long.parseLong(value);
            } else if (key.equals("file_path")) {
                m_file_path = value;
            } else if (key.equals("file_name")) {
                m_file_name = value;
            }
        }
        ;
    }

    // HANDLE THE REQUEST
    /**
     * handles the Request in a thread
     *
     */
    public void run() {
        boolean result = false;
        System.out.println("INVITE USER THREAD RUNNING");
    }

    // HELPER
    @Override
    public String toString() {
        return "<InviteUserToFileFolderExternalRequest::{m_status: " + m_status + ", m_user_id: " + m_user.get_id() + ", m_request_type: " + m_request_type + ", m_file_path: " + m_file_path + ", m_file_size: " + m_file_size + "}>";
    }
}
