package jStrg.communication_management.external_communication.answers.application.user_answers;

import jStrg.communication_management.external_communication.core.ExternalAnswer;
import jStrg.file_system.Application;
import jStrg.file_system.File;

import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * answer format to answer a Request to download a file by client side
 */
public class FileDownloadRequestExternalAnswer extends ExternalAnswer {
    public String m_transaction_id;

    //CONSTRUCTORS
    public FileDownloadRequestExternalAnswer(Socket _socket, Application _application, ExternalAnswer.status _status, String _transaction_id, File _file) {
        super(_socket, _application);
        m_status = _status;
        System.out.println(m_status);
        m_transaction_id = _transaction_id;
        // TODO: REPLACE WITH REAL FILE_SIZE
        Path path = Paths.get("docs/er_model.pdf");
        java.io.File file = new java.io.File(path.toString());

        m_file_size = file.length();
    }

    public long m_file_size;

    // HELPER
    /**
     * @return socket answer string
     */
    public String for_socket_answer() {
        return "status:" + m_status + ";transaction_id:" + m_transaction_id + ";file_size:" + m_file_size+ ";";
    }

    @Override
    public String toString() {
        return "<FileDownloadRequestExternalAnswer::{m_status: " + m_status + ", m_transaction_id: " + m_transaction_id + ", m_file_size:" + m_file_size + "}>";
    }
}
