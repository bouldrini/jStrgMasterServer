package jStrg.communication_management.external_communication.requests.application.user_requests;


import jStrg.communication_management.external_communication.answers.application.user_answers.DeleteFileFolderRequestExternalAnswer;
import jStrg.communication_management.external_communication.core.ExternalAnswer;
import jStrg.communication_management.external_communication.core.ExternalRequest;
import jStrg.file_system.Application;
import jStrg.file_system.FileFolder;
import jStrg.file_system.Privilege;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;

/**
 * incoming Request to delete a file folder by clientside
 */
public class DeleteFileFolderRequest extends ExternalRequest {
    // ATTRIBUTES
    public String m_folder_path = "";

    // CONSTRUCTORS
    public DeleteFileFolderRequest(Socket _socket, String _client_request_string, Application _application) throws GeneralSecurityException, IOException {
        super(_socket, _client_request_string, _application);
        String key = "";
        String value = "";
        for (String line : m_request_string.split(";")) {
            key = line.split(":")[0];
            value = line.split(":")[1];
            if (key.equals("folder_path")) {
                m_folder_path = value;
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
        System.out.println("DELETE FILE FOLDER THREAD RUNNUNG");

        // TODO: Replace with Query for Filefolder
        FileFolder folder = FileFolder.last(this.m_application.get_id());

        System.out.println("CHECKING IF FOLDER EXISTS");
        if (folder != null) {
            System.out.println("FOLDER FOUND");
            Privilege privilege = this.m_user.privilege_for(folder);
            System.out.println("CHECKING USERS PRIVILEGE TO DELETE THIS FOLDER");
            if (privilege.delete()) {
                System.out.println("USER IS ALLOWED TO DELETE");
                DeleteFileFolderRequestExternalAnswer answer = new DeleteFileFolderRequestExternalAnswer(this.m_socket, m_application, ExternalAnswer.status.DONE);

                // TODO: Delete Files in FileFolder too ?

                try {
                    answer.send();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("INSUFFITIANT PERMISSIONS");
                ExternalAnswer answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INSUFFITIANT_PERMISSIONS_DELETE_FOLDER);
                try {
                    answer.send();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("FOLDER NOT FOUND");
            ExternalAnswer answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.FOLDER_NOT_FOUND);
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
        return "<DownloadRequest::{m_status: " + m_status + ", m_user_id: " + m_user.get_id() + ", m_request_type: " + m_request_type + ", m_folder_path: " + m_folder_path + "}>";
    }
}
