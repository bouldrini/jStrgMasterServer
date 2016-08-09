package jStrg.GWT.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Created by Jura on 15.05.2016.
 * interface with rpc methode deffinitions
 */
@RemoteServiceRelativePath("Service")
//association fith ServiceImpl servlet
public interface Service extends RemoteService {
    /**
     * User login methode
     * @param name is the username to login
     * @param password password from user
     * @return json String with some userdata
     */
    String login(String name, String password);

    String loginFromSession();

    void logout();

    String getFolderById(int folder_id);

    String getFileById(int file_id);

    String renameFolder(int folder_id, String newTitle);

    String renameFile(int file_id, String newTitle);

    String createFolder(int app_id, int parent_id, String title);

    String deleteFolder(int folder_id);

    String deleteFile(int file_id);

    String getFileVersionList(int file_id);

    String rollbackToVersion(int version_id);

    /**
     * Utility/Convenience class.
     * Use Service.App.getInstance() to access static instance of LoginServiceAsync
     */
    public static class App {
        private static final ServiceAsync ourInstance = (ServiceAsync) GWT.create(Service.class);

        public static ServiceAsync getInstance() {
            return ourInstance;
        }
    }
}
