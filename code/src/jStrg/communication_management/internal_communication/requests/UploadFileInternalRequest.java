package jStrg.communication_management.internal_communication.requests;

import jStrg.communication_management.internal_communication.answers.UploadFileRequestInternalAnswer;
import jStrg.communication_management.internal_communication.core.InternalAnswer;
import jStrg.communication_management.internal_communication.core.InternalRequest;
import jStrg.network_management.core.Cryptor;
import jStrg.network_management.storage_management.internal.StorageServer;

import java.io.*;
import java.security.GeneralSecurityException;

/**
 * Request format to upload a file to a subserver by master
 */
public class UploadFileInternalRequest extends InternalRequest {

    public UploadFileInternalRequest(StorageServer _subserver, File _file, String _file_path, String _file_name) {
        super(InternalRequest.type.DOWNLOAD_FILE, _subserver);
        m_file = _file;
        m_file_size = m_file.length();
        m_file_name = _file_name;
        m_file_path = _file_path;
    }

    public File m_file;
    public long m_file_size;
    public String m_file_path;
    public String m_file_name;
    public String m_owner;

    public InternalAnswer process() throws IOException, GeneralSecurityException {
        UploadFileRequestInternalAnswer request_answer = this.send_file_upload_request();
        return request_answer;
    }

    private UploadFileRequestInternalAnswer send_file_upload_request() throws IOException, GeneralSecurityException {
        System.out.println("SENDING FILE UPLOAD REQUEST....");
        OutputStream outToServer = m_subserver.m_current_connection.m_socket.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);
        out.writeUTF(Cryptor.encrypt(this.m_subserver.m_cluster.application().m_setting.m_network_communication_secret1, this.m_subserver.m_cluster.application().m_setting.m_network_communication_secret2, this.for_server_request()));

        System.out.println("RECEIVING ANSWER....");
        InputStream inFromServer = m_subserver.m_current_connection.m_socket.getInputStream();
        DataInputStream in = new DataInputStream(inFromServer);
        String server_answer = in.readUTF();

        UploadFileRequestInternalAnswer answer = new UploadFileRequestInternalAnswer(this.m_subserver.m_cluster.application(), server_answer);
        return answer;
    }

    // HELPER
    public String for_server_request() {
        String query = "";
        query = "request_type:" + m_type + ";file_path:" + m_file_path + ";file_name:" + m_file_name + ";file_size:" + m_file_size + ";";
        return query;
    }
    private String encrypt() throws GeneralSecurityException, UnsupportedEncodingException {
        return Cryptor.encrypt(m_application.m_setting.m_network_communication_secret1, m_application.m_setting.m_network_communication_secret1,  this.for_server_request());
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
