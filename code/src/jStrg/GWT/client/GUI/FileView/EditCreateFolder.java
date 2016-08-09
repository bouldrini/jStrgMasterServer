package jStrg.GWT.client.GUI.FileView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import jStrg.GWT.client.Data.JS_Metha_FileFolder;
import jStrg.GWT.client.LoginServiceClientImpl;

/**
 * Created by Jura on 29.06.2016.
 */
public class EditCreateFolder extends Composite {
    private static RenameFolderUiBinder ourUiBinder = GWT.create(RenameFolderUiBinder.class);
    private final JS_Metha_FileFolder metha_fileFolder;
    private final LoginServiceClientImpl serviceClient;
    @UiField
    TextBox renameBox;
    @UiField
    Button folderBtn;
    private boolean m_create;

    public EditCreateFolder(LoginServiceClientImpl _serviceClient, JS_Metha_FileFolder _folder, boolean _create) {
        serviceClient = _serviceClient;
        metha_fileFolder = _folder;
        m_create = _create;
        initWidget(ourUiBinder.createAndBindUi(this));
        if (!_create) {
            this.folderBtn.setText("Rename");
            this.renameBox.setValue(metha_fileFolder.getTitle());
        }
    }

    @UiHandler("folderBtn")
    public void handleClick(ClickEvent event) {
        if (m_create) {
            if (metha_fileFolder.isFolder()) {
                serviceClient.createFolder(this.serviceClient.getUser().getApplicationId(), metha_fileFolder.getId(), this.renameBox.getValue());
            }
        } else {
            if (metha_fileFolder.isFolder())
                serviceClient.renameFolder(metha_fileFolder.getId(), this.renameBox.getValue());
            else
                serviceClient.renameFile(metha_fileFolder.getId(), this.renameBox.getValue());
        }
    }

    interface RenameFolderUiBinder extends UiBinder<HTMLPanel, EditCreateFolder> {
    }
}