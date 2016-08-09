package jStrg.communication_management.external_communication.answers.application.user_answers;

import jStrg.communication_management.external_communication.core.ExternalAnswer;
import jStrg.file_system.Application;

import java.net.Socket;

/**
 * answer format to answer a Request to upload a file by client side
 */
public class UploadFileRequestExternalAnswer extends ExternalAnswer {
    public String m_transaction_id;

    //CONSTRUCTORS
    public UploadFileRequestExternalAnswer(Socket _socket, Application _application, ExternalAnswer.status _status, String _transaction_id) {
        super(_socket, _application);
        m_status = _status;
        m_transaction_id = _transaction_id;
    }

    // HELPER
    /**
     * @return socket answer string
     */
    public String for_socket_answer() {
        return "status:" + m_status + ";transaction_id:" + m_transaction_id + ";";
    }

    @Override
    public String toString() {
        return "<UploadFileRequestExternalAnswer::{m_status: " + m_status + ", m_transaction_id: " + m_transaction_id + "}>";
    }
}
