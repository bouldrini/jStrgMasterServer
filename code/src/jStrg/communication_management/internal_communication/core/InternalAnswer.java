package jStrg.communication_management.internal_communication.core;

import jStrg.file_system.Application;
import jStrg.network_management.core.Cryptor;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Baseclass for incoming Answers by subserver
 * includes string based answer parsing logic
 */
public class InternalAnswer {

    public File m_file;

    // CONSTRUCTORS
    public InternalAnswer(Application _application, String _server_answer) throws GeneralSecurityException, IOException {
        String key = "";
        String value = "";
        m_answer_string = Cryptor.decrypt(_application.m_setting.m_network_communication_secret1, _application.m_setting.m_network_communication_secret2, _server_answer);

        // FILLING ERRORS
        System.out.println(m_answer_string);
        for(String line : m_answer_string.split(";")) {
            key = line.split(":")[0];
            value = line.split(":")[1];
            if (key.equals("status")) {
                if (value.equals(status.ERROR.toString())) {
                    m_status = status.ERROR;
                } else if (value.equals(status.DONE.toString())) {
                    m_status = status.DONE;
                } else if (value.equals(status.READY_FOR_FILEUPLOAD.toString())) {
                    m_status = status.READY_FOR_FILEUPLOAD;
                }  else if (value.equals(status.READY_FOR_FILEDOWNLOAD.toString())) {
                    m_status = status.READY_FOR_FILEDOWNLOAD;
                }
            } else if (key.equals("file_title")){

            } else if (key.equals("error_code")) {
                if (value.equals(error_code.UNAUTHORIZED.toString())) {
                    m_error_code = error_code.UNAUTHORIZED;
                } else if (value.equals(error_code.LOCATION_NOT_FOUND.toString())) {
                    m_error_code = error_code.LOCATION_NOT_FOUND;
                } else if (value.equals(error_code.NOT_ENOUGH_SPACE.toString())) {
                    m_error_code = error_code.NOT_ENOUGH_SPACE;
                } else if (value.equals(error_code.INVALID_FILE_FORMAT.toString())) {
                    m_error_code = error_code.INVALID_FILE_FORMAT;
                } else if (value.equals(error_code.FILE_NOT_FOUND.toString())) {
                    m_error_code = error_code.FILE_NOT_FOUND;
                } else if (value.equals(error_code.INVALID_QUERY.toString())) {
                    m_error_code = error_code.INVALID_QUERY;
                } else if (value.equals(error_code.INSUFFITIANT_PERMISSIONS.toString())) {
                    m_error_code = error_code.INSUFFITIANT_PERMISSIONS;
                } else if (value.equals(error_code.USER_ALREADY_EXISTS.toString())) {
                    m_error_code = error_code.USER_ALREADY_EXISTS;
                }
            } else if (key.equals("error_message")) {
                m_error_message = value;
            } else if (key.equals("transaction_id")) {
                m_transaction_id = value;
            }
        }
    }

    // CONSTANTS
    public enum status{DONE, READY_FOR_FILEUPLOAD, ERROR, READY_FOR_FILEDOWNLOAD}
    public enum error_code{UNAUTHORIZED, FILE_NOT_FOUND, FOLDER_NOT_FOUND, INVALID_QUERY, INVALID_FILE_FORMAT, LOCATION_NOT_FOUND, INSUFFITIANT_PERMISSIONS, NOT_ENOUGH_SPACE, UNKNOWN_REQUEST_TYPE, USER_ALREADY_EXISTS}


    // ATTRIBUTES
    public InternalAnswer.status m_status;
    public InternalAnswer.error_code m_error_code;
    public String m_error_message;
    public String m_answer_string;
    public String m_transaction_id;

    @Override
    public String toString() {
        return "<InternalAnswer::{m_status: " + m_status + ", m_error_code: " + m_error_code + ", m_error_message: " + m_error_message + ", m_answer_string: " + m_answer_string + "}>";
    }
}
