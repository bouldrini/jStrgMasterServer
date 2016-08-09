package jStrg.communication_management.internal_communication.core;

import java.io.File;
import java.net.Socket;

/**
 * Baseclass for Downloads by master from subserver
 */
public class InternalDownload {

    // ATTRIBUTES
    public File m_file;
    public String m_transaction_id;
    public Socket m_socket;

    public InternalDownload(Socket _socket, File _file, String _transaction_id){
        m_socket = _socket;
        m_transaction_id = _transaction_id;
    }
}
