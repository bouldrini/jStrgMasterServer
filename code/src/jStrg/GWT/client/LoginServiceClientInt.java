package jStrg.GWT.client;

/**
 * Created by Jura on 15.05.2016.
 * Interface of the RPC methodes
 */
public interface LoginServiceClientInt {
    void login(String name, String password);

    void loginFromSession();

    void logout();

    void getFolderById(int folder_id);

    void getFileById(int file_id);

    void renameFolder(int folder_id, String newTitle);

    void createFolder(int app_id, int parent_id, String title);

    void deleteFolder(int folder_id);

    void renameFile(int file_id, String newTitle);

    void deleteFile(int file_id);

    void getFileVersionList(int file_id);

    void rollbackToVersion(int version_id);
}
