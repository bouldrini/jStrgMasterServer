package jStrg.network_management.storage_management.core;

import jStrg.network_management.storage_management.internal.StorageServer;

import java.io.*;
import java.net.Socket;

/**
 * Representing a Connection between master and subserver
 */
public class Connection {
    public Connection(Socket _socket, StorageServer _subserver){
        this.m_socket = _socket;
        this.m_subserver= _subserver;
    }

    public Socket m_socket;
    private StorageServer m_subserver;

    public boolean close() throws IOException {
        this.m_socket.close();
        return true;
    }
}