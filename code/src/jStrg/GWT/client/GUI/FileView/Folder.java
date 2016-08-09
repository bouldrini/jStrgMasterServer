package jStrg.GWT.client.GUI.FileView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.RootPanel;
import jStrg.GWT.client.Data.JS_FileFolder;
import jStrg.GWT.client.Data.JS_Metha_FileFolder;
import jStrg.GWT.client.LoginServiceClientImpl;

/**
 * Created by Jura on 29.06.2016.
 */
public class Folder extends Composite {
    private static FolderUiBinder ourUiBinder = GWT.create(FolderUiBinder.class);
    private final LoginServiceClientImpl serviceClient;
    @UiField
    HTMLPanel content;
    @UiField
    Hyperlink newFolder;
    @UiField
    Hyperlink newFile;
    @UiField
    Hyperlink back;
    private JS_FileFolder m_folder;

    public Folder(LoginServiceClientImpl _loginServiceClient) {
        initWidget(ourUiBinder.createAndBindUi(this));
        this.serviceClient = _loginServiceClient;
    }

    @UiHandler("back")
    public void backClick(ClickEvent event) {
        this.serviceClient.getFolderById(m_folder.getParentId());
    }

    @UiHandler("newFolder")
    public void newFolderClick(ClickEvent event) {
        this.content.clear();
        JS_Metha_FileFolder metha_fileFolder = JsonUtils.<JS_Metha_FileFolder>safeEval("{\"id\" : " +
                m_folder.getId() + ", \"isFolder\" : true, \"title\" : \"" + m_folder.getTitle() + "\"}");
        this.content.add(new EditCreateFolder(this.serviceClient, metha_fileFolder, true));
    }

    @UiHandler("newFile")
    public void newFileClick(ClickEvent event) {
        RootPanel.get(this.serviceClient.PAGE_CONTENT).clear();
        RootPanel.get(this.serviceClient.PAGE_CONTENT).add(new FileUpload(this.serviceClient, this.m_folder));
    }

    public void init(JS_FileFolder _folder) {
        content.clear();
        this.m_folder = _folder;
        if(m_folder.getPrivilege().canWrite()){
            this.newFile.setVisible(true);
            this.newFolder.setVisible(true);
        }else {
            this.newFile.setVisible(false);
            this.newFolder.setVisible(false);
        }
        if (m_folder.getId() == this.serviceClient.getUser().getRootFolderId()) {
            this.back.setVisible(false);
        } else {
            this.back.setVisible(true);
        }
        for (int folderIndex = 0; folderIndex < m_folder.getFolders().length(); ++folderIndex) {
            JS_Metha_FileFolder tmp = m_folder.getFolders().get(folderIndex);
            FileButton btn = new FileButton(this.serviceClient, tmp);
            content.add(btn);
        }
        for (int fileIndex = 0; fileIndex < m_folder.getFiles().length(); ++fileIndex) {
            JS_Metha_FileFolder tmp = m_folder.getFiles().get(fileIndex);
            FileButton btn = new FileButton(this.serviceClient, tmp);
            content.add(btn);
        }
    }

    interface FolderUiBinder extends UiBinder<HTMLPanel, Folder> {
    }
}