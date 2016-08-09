package jStrg.GWT.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import jStrg.GWT.client.Data.JS_File;
import jStrg.GWT.client.Data.JS_FileFolder;
import jStrg.GWT.client.Data.JS_User;
import jStrg.GWT.client.Data.JS_Version;
import jStrg.GWT.client.GUI.FileView.File;
import jStrg.GWT.client.GUI.FileView.Folder;
import jStrg.GWT.client.GUI.FileView.Rollback;
import jStrg.GWT.client.GUI.Login;
import jStrg.GWT.client.GUI.Navigation.Navigation;
import jStrg.GWT.client.GUI.Registration;
import jStrg.GWT.client.GUI.UserData;
import jStrg.GWT.client.service.Service;
import jStrg.GWT.client.service.ServiceAsync;

import java.util.Date;

/**
 * Created by Jura on 15.05.2016.
 */
public class LoginServiceClientImpl implements LoginServiceClientInt {

    public final String PAGE_CONTENT = "content";
    public final String PAGE_NAVIGATION = "navigation";
    private final long DURATION = 1000 * 60 * 60 * 24 * 1;

    private ServiceAsync serviceAsync;
    private Navigation navGUI;
    private Login loginGUI;
    private Registration regUI;
    private JS_User user;
    private UserData userData;
    private File fileGUI;
    private Folder folderGUI;
    private Rollback rollbackGUI;

    public void setUser(JS_User user) {
        this.user = user;
    }

    public LoginServiceClientImpl(String url) {
        this.serviceAsync = GWT.create(Service.class);
        ServiceDefTarget endPoint = (ServiceDefTarget) this.serviceAsync;
        endPoint.setServiceEntryPoint(url);

        loginGUI = new Login(this);
        navGUI = new Navigation(this);
        regUI = new Registration(this);
        userData = new UserData(this);
        fileGUI = new File(this);
        folderGUI = new Folder(this);
        rollbackGUI = new Rollback(this);
    }

    @Override
    public void login(String name, String password) {
        this.serviceAsync.login(name, password, new ServerCallback());
    }

    @Override
    public void loginFromSession() {
        this.serviceAsync.loginFromSession(new ServerCallback());
    }

    @Override
    public void getFolderById(int folder_id) {
        this.serviceAsync.getFolderById(folder_id, new ServerCallback());
    }

    @Override
    public void getFileById(int file_id) {
        this.serviceAsync.getFileById(file_id, new ServerCallback());
    }

    @Override
    public void renameFolder( int folder_id, String newTitle) {
        this.serviceAsync.renameFolder(folder_id, newTitle, new ServerCallback());
    }

    @Override
    public void createFolder( int app_id, int parent_id, String title) {
        this.serviceAsync.createFolder(app_id, parent_id, title, new ServerCallback());
    }

    @Override
    public void deleteFolder( int folder_id) {
        this.serviceAsync.deleteFolder(folder_id, new ServerCallback());
    }

    @Override
    public void renameFile( int file_id, String newTitle) {
        this.serviceAsync.renameFile(file_id, newTitle, new ServerCallback());
    }

    @Override
    public void deleteFile( int file_id) {
        this.serviceAsync.deleteFile(file_id, new ServerCallback());
    }

    @Override
    public void getFileVersionList(int file_id) {
        this.serviceAsync.getFileVersionList(file_id, new ServerCallback());
    }

    @Override
    public void rollbackToVersion(int version_id) {
        this.serviceAsync.rollbackToVersion(version_id, new ServerCallback());
    }

    @Override
    public void logout() {
        user = null;
        this.serviceAsync.logout(new ServerCallback());
    }

    public UserData getUserData() {
        return userData;
    }

    public JS_User getUser() {
        return user;
    }

    public Login getLoginGUI() {
        return this.loginGUI;
    }

    public Navigation getNav() {
        return navGUI;
    }

    public Registration getRegUI() {
        return regUI;
    }

    public File getFileGUI() {
        return fileGUI;
    }

    public Folder getFolderGUI() {
        return folderGUI;
    }

    public Rollback getRollbackGUI() {
        return rollbackGUI;
    }

    private class ServerCallback implements AsyncCallback {

        @Override
        public void onFailure(Throwable caught) {
            System.out.println("Error: No response from server!");
        }

        @Override
        public void onSuccess(Object result) {
            if (result instanceof String) {
                String json = result.toString();
                json = json.substring(json.indexOf(":") + 1, json.length());
                // user jsonsring
                if (((String) result).startsWith("\"User\"")) {
                    user = JsonUtils.<JS_User>safeEval(json);
                    if (!user.getSessionId().isEmpty()) { // session test
                        Date expires = new Date(System.currentTimeMillis() + DURATION);
                        Cookies.setCookie("sid", user.getSessionId(), expires, null, "/", false);
                        navGUI.navShow();
                        userData.userInit();
                        RootPanel.get(PAGE_CONTENT).clear();
                        RootPanel.get(PAGE_CONTENT).add(userData);
                    }else
                        Window.alert("Access denied!");
                    // file jsonsring
                } else if (((String) result).startsWith("\"File\"")) {
                    JS_File file = JsonUtils.<JS_File>safeEval(json);
                    fileGUI.init(file);
                    RootPanel.get(PAGE_CONTENT).clear();
                    RootPanel.get(PAGE_CONTENT).add(fileGUI);
                    // folder jsonsring
                } else if (((String) result).startsWith("\"Folder\"")) {
                    JS_FileFolder folder = JsonUtils.<JS_FileFolder>safeEval(json);
                    folderGUI.init(folder);
                    RootPanel.get(PAGE_CONTENT).clear();
                    RootPanel.get(PAGE_CONTENT).add(folderGUI);
                    // version jsonsring
                } else if (((String) result).startsWith("\"Version\"")) {
                    JsArray<JS_Version> versionList = JsonUtils.<JsArray<JS_Version>>safeEval(json);
                    RootPanel.get(PAGE_CONTENT).clear();
                    rollbackGUI.init(versionList);
                    RootPanel.get(PAGE_CONTENT).add(rollbackGUI);
                    // error jsonsring
                } else if (((String) result).startsWith("Error")){
                    Window.alert(json);
                }
            }
        }
    }
}
