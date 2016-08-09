package jStrg.communication_management.external_communication.requests.application.user_requests;

import jStrg.communication_management.external_communication.core.ExternalAnswer;
import jStrg.communication_management.external_communication.core.FloatingTransaction;
import jStrg.communication_management.external_communication.core.ExternalRequest;
import jStrg.communication_management.external_communication.answers.application.user_answers.UploadFileRequestExternalAnswer;
import jStrg.communication_management.external_communication.transactions.uploads.ExternalFileUpload;
import jStrg.file_system.*;
import jStrg.file_system.File;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;

/**
 * incoming Request to upload a file by clientside
 */
public class UploadFileExternalRequest extends ExternalRequest {
    // CONSTRUCTORS
    public UploadFileExternalRequest(Socket _socket, String _client_request_string, Application _application) throws GeneralSecurityException, IOException {
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
            } else if (key.equals("ownername")) {
                m_owner = User.find_by_name(value, _application);
            }
        }
        ;
    }

    // ATTRIBUTES
    public long m_file_size;
    public String m_file_path = "";
    public String m_file_name = "";
    public User m_owner;

    // HANDLE THE REQUEST
    /**
     * handles the Request in a thread
     *
     */
    public void run() {
        System.out.println("UPLOAD FILE REQUEST RUNNING");

        Boolean data_received = false;
        Boolean upload_successfull = false;
        String error_message = "";
        ExternalAnswer answer;

        if(!m_application.m_cluster_manager.has_enough_space(m_file_size)){
            error_message = "Not enough StorageSpace in the Cluster. Upgrade your Clusters Storage Space or choose a non reduntant placement method to save your files in the cluster";
            answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.NOT_ENOUGH_SPACE, error_message);
        } else {
            if(m_owner == null){
                error_message = "The Owner of the Folder you attembed to upload in doesnt exist";
                answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.NOT_ENOUGH_SPACE, error_message);
            } else {
                if (!m_owner.has_enough_space(m_file_size)) {
                    error_message = "User doesnt have enough space left";
                    answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.NOT_ENOUGH_SPACE, error_message);
                } else {
                    jStrg.file_system.File file = File.get_file_by_path(m_file_path + "/" + m_file_name, m_owner);
                    if (file == null) {
                        // FILE DOESNT EXIST
                        System.out.println("file doesnt exist");
                        if (m_owner != m_user) {
                            System.out.println("user not owner");
                            // UPLOADER IS NOT THE OWNER
                            FileFolder folder = FileFolder.get_filefolder_by_path(m_file_path, m_owner);
                            if(folder == null){
                                // FOLDER DOESNT EXIST
                                folder = FileFolder.find_first_existing(m_file_path, m_owner);
                                Privilege privilege = m_user.privilege_for(folder);
                                if(privilege != null){
                                    if(privilege.write()){
                                        // GOT PERMISSION TO WRITE IN THE FIRST EXISTING FOLDER
                                        folder = FileFolder.create_by_path(m_file_path, m_owner, m_user);
                                        answer = start_upload();
                                    } else {
                                        error_message = "Insuffitiant permission set by the privilege for the folder you attembed to upload in";
                                        answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INSUFFITIANT_PERMISSION_UPLOAD, error_message);
                                    }
                                } else {
                                    error_message = "No privileges for the folder you attembed to upload in";
                                    answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INSUFFITIANT_PERMISSION_UPLOAD, error_message);
                                }
                            } else {
                                // FOLDER DOES EXIST
                                Privilege privilege = m_user.privilege_for(folder);
                                if (privilege.write()) {
                                    // GOT PERMISSION TO WRITE IN THE FOLDER
                                    answer = start_upload();
                                } else {
                                    error_message = "No privileges for the folder you attembed to upload in";
                                    answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INSUFFITIANT_PERMISSION_UPLOAD, error_message);
                                }
                            }


                        } else {
                            System.out.println("user is owner");
                            // UPLOADER IS THE OWNER
                            FileFolder.create_by_path(m_file_path, m_user, m_user);
                            answer = start_upload();
                        }
                    } else {
                        // FILE EXISTS
                        System.out.println("file exists");
                        if (m_owner != m_user) {
                            System.out.println("user not owner");
                            // UPLOADER IS NOT THE OWNER
                            Privilege privilege = m_user.privilege_for(file);
                            if(privilege != null){
                                if (privilege.write()) {
                                    answer = start_upload();
                                } else {
                                    error_message = "No privileges for the folder you attembed to upload in";
                                    answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INSUFFITIANT_PERMISSION_UPLOAD, error_message);
                                }
                            } else {
                                error_message = "No privileges for the file you attembed to update";
                                answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INSUFFITIANT_PERMISSION_UPLOAD, error_message);
                            }

                        } else {
                            System.out.println("user owner");
                            // UPLOADER IS THE OWNER
                            answer = start_upload();
                        }
                    }
                }
            }
        }

        try {
            answer.send();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public ExternalAnswer start_upload(){
        System.out.println("READY FOR FILEUPLOAD");
        FloatingTransaction transaction = new FloatingTransaction(0);
        ExternalAnswer answer = new UploadFileRequestExternalAnswer(m_socket, m_application, ExternalAnswer.status.READY_FOR_FILEUPLOAD, transaction.m_transaction_id);
        try {
            answer.send();
            ExternalFileUpload upload = new ExternalFileUpload(m_socket, m_file_name, m_file_path, m_file_size, m_owner, m_user, m_application, transaction.m_transaction_id);
            ExternalAnswer final_answer = upload.process();
        } catch (IOException e) {
            e.printStackTrace();
            String error_message = "Something went wrong during the ExternalFileUpload. Please try again";
            answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INTERNAL_ERROR, error_message);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            String error_message = "Something went wrong during the ExternalFileUpload. Please try again";
            answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INTERNAL_ERROR, error_message);
        }
        return answer;
    }


    // HELPER
    @Override
    public String toString() {
        return "<UploadRequest::{m_status: " + m_status + ", m_user_id: " + m_user.get_id() + ", m_request_type: " + m_request_type + ", m_file_path: " + m_file_path + ", m_file_size: " + m_file_size + "}>";
    }
}
