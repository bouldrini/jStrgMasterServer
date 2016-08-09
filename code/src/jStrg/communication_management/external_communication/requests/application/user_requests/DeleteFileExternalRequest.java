package jStrg.communication_management.external_communication.requests.application.user_requests;

import jStrg.communication_management.external_communication.answers.application.user_answers.DeleteFileRequestExternalAnswer;
import jStrg.communication_management.external_communication.core.ExternalAnswer;
import jStrg.communication_management.external_communication.core.ExternalRequest;
import jStrg.file_system.Application;
import jStrg.file_system.File;
import jStrg.file_system.Privilege;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;

/**
 * incoming Request to delete a file by clientside
 */
public class DeleteFileExternalRequest extends ExternalRequest {
    // ATTRIBUTES
    public String m_file_path = "";
    public String m_file_name = "";
    // CONSTRUCTORS
    public DeleteFileExternalRequest(Socket _socket, String _client_request_string, Application _application) throws GeneralSecurityException, IOException {
        super(_socket, _client_request_string, _application);
        String key = "";
        String value = "";
        for (String line : m_request_string.split(";")) {
            key = line.split(":")[0];
            value = line.split(":")[1];
            if (key.equals("file_path")) {
                m_file_path = value;
            } else if (key.equals("file_name")) {
                m_file_name = value;
            }
        }
    }

    // HANDLE THE REQUEST
    /**
     * handles the Request in a thread
     *
     */
    public void run() {

        System.out.println("DELETE FILE THREAD RUNNUNG");

        // TODO: Replace with Query for File
        File file = this.m_user.files().iterator().next();

        System.out.println("CHECKING IF FILE EXISTS");
        if (file != null) {
            System.out.println("FILE FOUND");
            Privilege privilege = this.m_user.privilege_for(file);
            System.out.println("CHECKING USERS PRIVILEGE TO DELETE THIS FILE");
            if (privilege != null) {
                if (privilege.delete()) {
                    System.out.println("USER IS ALLOWED TO DELETE");
                    DeleteFileRequestExternalAnswer answer = new DeleteFileRequestExternalAnswer(this.m_socket, m_application, ExternalAnswer.status.DONE);
                    try {
                        answer.send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("INSUFFITIANT PERMISSIONS");
                    ExternalAnswer answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INSUFFITIANT_PERMISSIONS_DELETE_FILE);
                    try {
                        answer.send();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("INSUFFITIANT PERMISSIONS");
                ExternalAnswer answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INSUFFITIANT_PERMISSIONS_DELETE_FILE);
                try {
                    answer.send();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("FILE NOT FOUND");
            ExternalAnswer answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.FILE_NOT_FOUND);
            try {
                answer.send();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
    }

    // HELPER
    @Override
    public String toString() {
        return "<DeleteFileExternalRequest::{m_status: " + m_status + ", m_user_id: " + m_user.get_id() + ", m_request_type: " + m_request_type + ", m_file_path: " + m_file_path + ", m_file_name: " + m_file_name + "}>";
    }
}
