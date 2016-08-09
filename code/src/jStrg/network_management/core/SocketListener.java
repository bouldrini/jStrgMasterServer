package jStrg.network_management.core;

import jStrg.communication_management.external_communication.core.ExternalAnswer;
import jStrg.communication_management.external_communication.core.ExternalRequest;
import jStrg.communication_management.external_communication.requests.application.operator_requests.CreateUserExternalRequest;
import jStrg.communication_management.external_communication.requests.application.user_requests.DeleteFileFolderRequest;
import jStrg.communication_management.external_communication.requests.application.user_requests.DeleteFileExternalRequest;
import jStrg.communication_management.external_communication.requests.application.user_requests.DownloadFileExternalRequest;
import jStrg.communication_management.external_communication.requests.application.user_requests.UploadFileExternalRequest;
import jStrg.file_system.Application;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;

public class SocketListener extends Thread {

    public int m_port = 3001;
    public Thread m_thread;
    public Application m_application;
    public SocketListener(Application _application, int _port) {
        m_port = _port;
        m_application = _application;
    }

    /**
     * Starting the Socketlistener
     *
     * @throws IOException socket error
     */
    public void listen() throws IOException {
        this.start();
    }


    public void run() {
        ServerSocket server_socket = null;
        try {
            server_socket = new ServerSocket(m_port);
            while (true) {
                try {
                    System.out.println("====WAITING FOR INPUT ON PORT " + m_port + "====");
                    Socket socket = server_socket.accept();

                    System.out.println("");
                    System.out.println("====CLIENT CONNECTED, WAITING FOR REQUEST====");
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    String client_request_string = in.readUTF();
                    ExternalRequest client_request = new ExternalRequest(socket, client_request_string, m_application);
                    if (client_request.m_user != null) {
                        System.out.println("Client: " + client_request.m_user.m_username + " connected");
                        System.out.println(client_request.m_user);
                    }
                    if (client_request.m_status) {
                        if (client_request.m_request_type == ExternalRequest.type.UPLOAD_FILE_REQUEST) {
                            System.out.println("====PROCESSING CLIENT UPLOAD FILE REQUEST====");
                            UploadFileExternalRequest request = new UploadFileExternalRequest(socket, client_request_string, m_application);
                            request.start();
                        } else if (client_request.m_request_type == ExternalRequest.type.DOWNLOAD_FILE) {
                            System.out.println("====PROCESSING CLIENT DOWNLOAD FILE REQUEST====");
                            DownloadFileExternalRequest request = new DownloadFileExternalRequest(socket, client_request_string, m_application);
                            request.start();
                        } else if (client_request.m_request_type == ExternalRequest.type.CREATE_USER) {
                            System.out.println("====PROCESSING CLIENT CREATE USER REQUEST====");
                            CreateUserExternalRequest request = new CreateUserExternalRequest(socket, client_request_string, m_application);
                            request.start();
                        } else if (client_request.m_request_type == ExternalRequest.type.DELETE_FOLDER) {
                            System.out.println("====PROCESSING CLIENT DELETE FOLDER REQUEST====");
                            DeleteFileFolderRequest request = new DeleteFileFolderRequest(socket, client_request_string, m_application);
                            request.start();
                        } else if (client_request.m_request_type == ExternalRequest.type.DELETE_FILE) {
                            System.out.println("====PROCESSING CLIENT DELETE FILE REQUEST====");
                            DeleteFileExternalRequest request = new DeleteFileExternalRequest(socket, client_request_string, m_application);
                            request.start();
                        } else {
                            System.out.println(client_request.m_request_string);
                            System.out.println("====INVALID REQUEST TYPE====");
                            ExternalAnswer answer = new ExternalAnswer(socket, this.m_application, ExternalAnswer.error_code.UNKNOWN_REQUEST_TYPE);
                            answer.send();
                        }
                    } else {
                        System.out.println("====USER AUTHENTICATION FAILED====");
                        ExternalAnswer answer = new ExternalAnswer(socket, this.m_application, ExternalAnswer.error_code.UNAUTHORIZED);
                        answer.send();
                    }
                    System.out.println("");
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (m_thread == null) {
            m_thread = new Thread(this, this.application().m_title + "ListenerThread");
            m_thread.start();
        }
    }

    /**
     * query for the application this socketlistener belongs to
     *
     * @return Application
     */
    public Application application() {
        Application app = null;
        for (Application cur_app : Application.all()) {
            if (cur_app == m_application) {
                app = cur_app;
            }
        }
        return app;
    }
}