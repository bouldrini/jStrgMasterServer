package jStrg.communication_management.external_communication.answers.application.operator_answers;

import jStrg.communication_management.external_communication.core.ExternalAnswer;
import jStrg.file_system.Application;

import java.net.Socket;

/**
 * answer format to answer a Request to create a new user by client side operator
 */
public class CreateUserRequestExternalAnswer extends ExternalAnswer {
    //CONSTRUCTORS
    public CreateUserRequestExternalAnswer(Socket _socket, Application _application, ExternalAnswer.status _status) {
        m_application = _application;
        m_status = _status;
        m_socket = _socket;
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
        return "<CreateUserRequestExternalAnswer::{m_status: " + m_status + "}>";
    }
}
