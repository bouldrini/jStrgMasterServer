package jStrg.communication_management.external_communication.requests.application.user_requests;

import jStrg.communication_management.external_communication.answers.application.user_answers.FileDownloadRequestExternalAnswer;
import jStrg.communication_management.external_communication.core.ExternalAnswer;
import jStrg.communication_management.external_communication.core.FloatingTransaction;
import jStrg.communication_management.external_communication.core.ExternalRequest;
import jStrg.communication_management.external_communication.transactions.downloads.ExternalFileExternalDownload;
import jStrg.file_system.Application;
import jStrg.file_system.File;
import jStrg.file_system.Privilege;
import jStrg.file_system.User;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;

/**
 * incoming Request to download a file by clientside
 */
public class DownloadFileExternalRequest extends ExternalRequest {

    // CONSTRUCTORS
    public DownloadFileExternalRequest(Socket _socket, String _client_request_string, Application _application) throws GeneralSecurityException, IOException {
        super(_socket, _client_request_string, _application);
        String key = "";
        String value = "";
        for (String line : m_request_string.split(";")) {
            key = line.split(":")[0];
            value = line.split(":")[1];
            if (key.equals("file_path")) {
                m_file_path = value;
            } else if (key.equals("ownername")) {
                m_owner = User.find_by_name(value, _application);
            }
        }
        ;
    }

    // ATTRIBUTES
    public String m_file_path = "";
    public User m_owner;

    // HANDLE THE REQUEST
    /**
     * handles the Request in a thread
     *
     */
    public void run() {
        boolean result = false;
        System.out.println("DOWNLOAD THREAD RUNNUNG");
        System.out.println(m_file_path);
        System.out.println(m_owner);
        File file = File.get_file_by_path(m_file_path, m_owner);

        if (file != null) {
            Privilege privilege = this.m_user.privilege_for(file);
            if (privilege != null) {
                if (privilege.read()) {
                    FloatingTransaction transaction = new FloatingTransaction(0);
                    ExternalAnswer answer = new FileDownloadRequestExternalAnswer(m_socket, m_application, ExternalAnswer.status.READY_FOR_FILEDOWNLOAD, transaction.m_transaction_id, file);
                    try {
                        answer.send();
                        System.out.println("EXPECT CLIENT TO BE READY TO RECEIVE THE FILE");
                        ExternalFileExternalDownload file_download = new ExternalFileExternalDownload(m_socket, file.get_real_file(), m_application, transaction.m_transaction_id);
                        file_download.process();
                        System.out.println("Done.");
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
        return "<DownloadRequest::{m_status: " + m_status + ", m_user_id: " + m_user.get_id() + ", m_request_type: " + m_request_type + ", m_file_path: " + m_file_path + "}>";
    }
}
