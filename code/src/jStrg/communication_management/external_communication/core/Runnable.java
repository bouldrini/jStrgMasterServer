package jStrg.communication_management.external_communication.core;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Baseinterface for threaded classes
 */
public interface Runnable {
    public void run() throws IOException, GeneralSecurityException;
}
