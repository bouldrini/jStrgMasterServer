package jStrg.communication_management.internal_communication.answers;

import jStrg.communication_management.internal_communication.core.InternalAnswer;
import jStrg.file_system.Application;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class UploadFileRequestInternalAnswer extends InternalAnswer {
    public UploadFileRequestInternalAnswer(Application _application, String _server_answer) throws GeneralSecurityException, IOException {
        super(_application, _server_answer);
    }
}
