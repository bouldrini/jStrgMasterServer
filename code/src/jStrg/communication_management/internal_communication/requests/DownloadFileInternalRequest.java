package jStrg.communication_management.internal_communication.requests;

import jStrg.communication_management.internal_communication.answers.DownloadFileRequestInternalAnswer;
import jStrg.communication_management.internal_communication.core.InternalAnswer;
import jStrg.communication_management.internal_communication.core.InternalRequest;
import jStrg.network_management.core.Cryptor;
import jStrg.network_management.storage_management.internal.StorageServer;

import java.io.*;
import java.security.GeneralSecurityException;

/**
 * Request format to to download a file from subserver by master
 */
public class DownloadFileInternalRequest extends InternalRequest {

    public DownloadFileInternalRequest(StorageServer _subserver, String _file_path, InternalRequest.type _type){
        super(_type, _subserver);
        m_file_path = _file_path;
    }

    public String m_file_path;
    public String m_owner;

    public InternalAnswer process() throws IOException, GeneralSecurityException {
        DownloadFileRequestInternalAnswer request_answer = this.send_file_upload_request();
        return request_answer;
    }

    private DownloadFileRequestInternalAnswer send_file_upload_request() throws IOException, GeneralSecurityException {
        System.out.println("SENDING FILE DOWNLOAD REQUEST....");
        OutputStream outToServer = m_subserver.m_current_connection.m_socket.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);
        out.writeUTF(Cryptor.encrypt(m_application.m_setting.m_network_communication_secret1, m_application.m_setting.m_network_communication_secret2, this.for_server_request()));

        System.out.println("RECEIVING ANSWER....");
        InputStream inFromServer = m_subserver.m_current_connection.m_socket.getInputStream();
        DataInputStream in = new DataInputStream(inFromServer);
        String server_answer = in.readUTF();

        DownloadFileRequestInternalAnswer answer = new DownloadFileRequestInternalAnswer(m_application, server_answer);
        return answer;
    }

    // HELPER
    public String for_server_request() {
        String query = "";
        query = "request_type:" + m_type + ";file_path:" +  m_file_path + ";";
        return query;
    }

    private String encrypt() throws GeneralSecurityException, UnsupportedEncodingException {
        return Cryptor.encrypt(m_application.m_setting.m_network_communication_secret1, m_application.m_setting.m_network_communication_secret2, this.for_server_request());
    }

    @Override
    public String toString() {
        return super.toString();
    }
}