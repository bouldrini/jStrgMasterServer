package jStrg.communication_management.external_communication.answers.application.user_answers;

import jStrg.communication_management.external_communication.core.ExternalAnswer;
import jStrg.file_system.Application;

import java.net.Socket;

/**
 * answer format to answer after a fileupload was done
 */
public class FileUploadExternalAnswer extends ExternalAnswer {
    //CONSTRUCTORS
    public FileUploadExternalAnswer(Socket _socket, Application _application, ExternalAnswer.status _status) {
        super(_socket, _application);
        m_status = _status;
    }

    // HELPER
    /**
     * @return socket answer string
     */
    public String for_socket_answer() {
        return "status:" + m_status + ";";
    }

    @Override
    public String toString() {
        return "<FileUploadExternalAnswer::{m_status: " + m_status + "}>";
    }
}
