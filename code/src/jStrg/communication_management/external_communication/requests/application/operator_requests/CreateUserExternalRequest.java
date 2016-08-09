package jStrg.communication_management.external_communication.requests.application.operator_requests;

import jStrg.communication_management.external_communication.answers.application.operator_answers.CreateUserRequestExternalAnswer;
import jStrg.communication_management.external_communication.core.ExternalAnswer;
import jStrg.communication_management.external_communication.core.ExternalRequest;
import jStrg.file_system.Application;
import jStrg.file_system.Role;
import jStrg.file_system.User;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;

/**
 * incoming Request to create a new user by client side operator
 */
public class CreateUserExternalRequest extends ExternalRequest {
    // CONSTRUCTORS
    public CreateUserExternalRequest(Socket _socket, String _client_request_string, Application _application) throws GeneralSecurityException, IOException {
        super(_socket, _client_request_string, _application);
        String key = "";
        String value = "";
        for (String line : m_request_string.split(";")) {
            key = line.split(":")[0];
            value = line.split(":")[1];
            if (key.equals("new_username")) {
                m_new_username = value;
            } else if (key.equals("new_userpassword")) {
                m_new_userpassword = value;
            } else if (key.equals("use_s3_storage")) {
                m_use_s3_storage = true;
            } else if (key.equals("use_google_storage")) {
                m_use_google_storage = true;
            }
        }
    }

    // ATTRIBUTES
    public long m_file_size;
    public String m_new_username = "";
    public String m_new_userpassword = "";
    public boolean m_use_s3_storage = false;
    public boolean m_use_google_storage = false;

    // HANDLE THE REQUEST
    /**
     * handles the Request in a thread
     *
     */
    public void run() {
        System.out.println("CREATE USER THREAD RUNNUNG");
        ExternalAnswer answer = null;
        if (this.m_user.has_role(Role.find_by_title("admin")) || this.m_user.has_role(Role.find_by_title("operator"))) {
            System.out.println("USER IS ALLOWED TO CREATE NEW USERS");
            if(User.find_by_name(m_new_username) == null){
                User user = new User(0, Role.find_by_title("user").get_id(), m_new_username, m_new_userpassword, 0, m_application.m_setting.m_bytes_per_user, m_application.m_setting.m_bytes_per_user, m_application);
                answer = new CreateUserRequestExternalAnswer(m_socket, m_application, ExternalAnswer.status.DONE);
            } else {
                System.out.println("USER ALREADY EXISTS");
                answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.USER_ALREADY_EXISTS, "User '" + m_new_username + "' already exists !");
            }
        } else {
            System.out.println("USER IS NOT ALLOWED TO CREATE NEW USERS");
            answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INSUFFITIANT_PERMISSIONS_CREATE_USER);
        }

        try {
            answer.send();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    // HELPER
    @Override
    public String toString() {
        StringBuilder returnstring = new StringBuilder("<CreateUserExternalRequest::{");
        returnstring.append("m_status: " + m_status);
        returnstring.append(", m_user_id: " + m_user.get_id());
        returnstring.append(", m_request_type: " + m_request_type);
        returnstring.append(", m_new_username: " + m_new_username);
        returnstring.append(", m_new_userpassword: " + m_new_userpassword);
        returnstring.append(", m_use_s3_storage: " + m_use_s3_storage);
        returnstring.append(", m_use_google_storage: " + m_use_google_storage);
        returnstring.append("}>");
        return returnstring.toString();
    }
}
