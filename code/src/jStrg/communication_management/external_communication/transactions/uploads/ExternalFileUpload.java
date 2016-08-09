package jStrg.communication_management.external_communication.transactions.uploads;

import jStrg.communication_management.external_communication.answers.application.user_answers.FileUploadExternalAnswer;
import jStrg.communication_management.external_communication.core.ExternalAnswer;
import jStrg.communication_management.external_communication.core.ExternalUpload;
import jStrg.file_system.Application;
import jStrg.file_system.FileFolder;
import jStrg.file_system.FileVersion;
import jStrg.file_system.User;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.io.File;
import java.net.Socket;
import java.security.GeneralSecurityException;

/**
 * handling a fileupload by masterserver
 */
public class ExternalFileUpload extends ExternalUpload {

    public ExternalFileUpload(Socket _socket, String _file_name, String _file_path, long _file_size, User _upload_for, User _uploader, Application _application, String _transaction_id) throws GeneralSecurityException, IOException {
        super(_socket, _file_name, _file_path, _file_size, _upload_for, _uploader, _application, _transaction_id);
    }


    /**
     * processing the fileupload
     *
     * @return ExternalAnswer
     */
    public ExternalAnswer process() {
        try {
            File floating_file = new File(m_application.m_setting.m_default_cache_location + "/" + m_user.m_username + "/" + m_transaction_id + "-" + m_file_name);

            floating_file.getParentFile().mkdirs();
            floating_file.createNewFile();

            System.out.println("EXPECT THE CLIENT TO START SENDING");

            try {
                DataInputStream dis = new DataInputStream(m_socket.getInputStream());
                try {
                    FileOutputStream fos = new FileOutputStream(floating_file.getAbsolutePath());
                    byte[] buffer = new byte[4096];

                    int read = 0;
                    int totalRead = 0;
                    long remaining = m_file_size;

                    while((read = dis.read(buffer, 0, Math.min(buffer.length, (int)remaining))) > 0) {
                        totalRead += read;
                        remaining -= read;
                        fos.write(buffer);
                    }

                    fos.close();

                    FileFolder folder = FileFolder.find_first_existing(m_file_path, m_user);
                    jStrg.file_system.File file = new jStrg.file_system.File(folder, m_file_name, m_uploader);
                    FileVersion version = new FileVersion(m_uploader, file);
//                    version.set_checksum(DatatypeConverter.printHexBinary(file.digest()));

                    System.out.println("FILE IS AT MASTER");


                    ExternalAnswer answer = new FileUploadExternalAnswer(this.m_socket, m_application, ExternalAnswer.status.DONE);
                    return answer;
                } catch (FileNotFoundException ex) {
                    System.out.println("File not found. ");
                    ExternalAnswer answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INTERNAL_ERROR, "There was an internal Problem with saving the file temporarily to the master. The reason for this may be having not enough storage space left on the master");
                    return answer;
                }
            } catch (IOException ex) {
                System.out.println("Can't get socket input stream. ");
                ExternalAnswer answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INTERNAL_ERROR, "There was an internal Problem with listening to the input stream. Try again or contact your Administrator");
                return answer;
            }
        } catch (IOException e) {
            e.printStackTrace();
            ExternalAnswer answer = new ExternalAnswer(m_socket, m_application, ExternalAnswer.error_code.INTERNAL_ERROR, "There was an internal Problem with saving the file temporarily to the master. The reason for this may be having not enough storage space left on the master");
            return answer;
        }
    }
}
