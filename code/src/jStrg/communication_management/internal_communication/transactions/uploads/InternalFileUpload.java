package jStrg.communication_management.internal_communication.transactions.uploads;


import jStrg.communication_management.internal_communication.answers.FileUploadInternalAnswer;
import jStrg.communication_management.internal_communication.core.InternalRequest;
import jStrg.network_management.storage_management.internal.StorageServer;

import java.io.*;
import java.security.GeneralSecurityException;

/**
 * fileuploadfrom subserver by master
 */
public class InternalFileUpload extends InternalRequest {

    public InternalFileUpload(StorageServer _subserver, String _transaction_id, File _file, String _file_path) {
        super(type.UPLOAD_FILE, _subserver);
        if(_file.exists() && !_file.isDirectory()) {
            m_file = _file;
        }
        m_transaction_id = _transaction_id;
        m_file_path = _file_path;

    }

    // ATTRIBUTES
    public File m_file;
    public String m_transaction_id;
    public String m_file_path;

    /**
     * processing the file upload
     */
    public FileUploadInternalAnswer process() throws IOException, GeneralSecurityException {
        FileUploadInternalAnswer answer = null;
        System.out.println("Using open transaction_id: " + m_transaction_id);

        System.out.println("SENDING FILE UPLOAD REQUEST....");
        OutputStream outToServer = m_subserver.m_current_connection.m_socket.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);
//        out.writeUTF(Cryptor.encrypt(this.for_server_request()));

        System.out.println("SENDING FILE");
        this.send_file(outToServer);
        System.out.println("TRANSFER DONE.");

        System.out.println("RECEIVING ANSWER");
        InputStream inFromServer = m_subserver.m_current_connection.m_socket.getInputStream();
        DataInputStream in3 = new DataInputStream(inFromServer);
        String server_answer = in3.readUTF();
        answer = new FileUploadInternalAnswer(this.m_application, server_answer);
        return answer;
    }

    /**
     * sending the file
     * expects open OutputStream to use for file transfer
     * @params _server_output_stream
     * @retuns boolean
     */
    private boolean send_file(OutputStream _server_output_stream) throws IOException {
        System.out.println(m_file.getAbsolutePath());
        FileInputStream fis = new FileInputStream(m_file.getAbsolutePath().toString());

        byte[] buffer = new byte[4096];

        while (fis.read(buffer) > 0) {
            _server_output_stream.write(buffer);
        }

        fis.close();
        return true;
    }

    /**
     * converts the instance to a valid string to be parsed by another server
     * @returns request string
     */
    private String for_server_request() {
        String query = "";
        query = "request_type:" + m_type + ";;file_path:" + m_file_path + ";";
        return query;
    }
}
