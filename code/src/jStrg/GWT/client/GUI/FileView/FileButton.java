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
import jStrg.GWT.client.Data.JS_Metha_FileFolder;
import jStrg.GWT.client.Data.JS_Privilege;
import jStrg.GWT.client.LoginServiceClientImpl;

/**
 * Created by Jura on 29.06.2016.
 */
public class FileButton extends Composite {
    private static FileButtonUiBinder ourUiBinder = GWT.create(FileButtonUiBinder.class);
    private final LoginServiceClientImpl serviceClient;
    private final JS_Metha_FileFolder m_metha;
    @UiField
    Hyperlink button;
    @UiField
    HTMLPanel fileMenu;
    @UiField
    Hyperlink rename;
    @UiField
    Hyperlink remove;
    @UiField
    Hyperlink update;
    @UiField
    Hyperlink rollback;

    public FileButton(LoginServiceClientImpl _serviceClient, JS_Metha_FileFolder _js_metha_fileFolder) {
        initWidget(ourUiBinder.createAndBindUi(this));
        serviceClient = _serviceClient;
        m_metha = _js_metha_fileFolder;
        this.button.setText(m_metha.getTitle());
        // setting up viseble buttons
        if (!m_metha.getPrivilege().canDelete()) {
            this.remove.setVisible(false);
        }
        else {
            this.remove.setVisible(true);
        }
        if (m_metha.isFolder()){
            this.update.setVisible(false);
            this.rollback.setVisible(false);
        }else {
            this.update.setVisible(true);
            this.rollback.setVisible(true);
        }
    }

    @UiHandler("button")
    public void handleClick(ClickEvent event) {
        if (m_metha.isFolder())
            this.serviceClient.getFolderById(this.m_metha.getId());
        else
            this.serviceClient.getFileById(this.m_metha.getId());
    }

    @UiHandler("rename")
    public void renameClick(ClickEvent event) {
        this.serviceClient.getFolderGUI().content.clear();
        this.serviceClient.getFolderGUI().content.add(new EditCreateFolder(this.serviceClient, this.m_metha, false));
    }

    @UiHandler("remove")
    public void removeClick(ClickEvent event) {
        if(m_metha.isFolder())
            this.serviceClient.deleteFolder(m_metha.getId());
        else
            this.serviceClient.deleteFile(m_metha.getId());
    }


    @UiHandler("update")
    public void updateClick(ClickEvent event) {
        RootPanel.get(this.serviceClient.PAGE_CONTENT).clear();
        RootPanel.get(this.serviceClient.PAGE_CONTENT).add(new FileUpload(this.serviceClient, this.m_metha));
    }
    @UiHandler("rollback")
    public void rollbackClick(ClickEvent event) {
        this.serviceClient.getFileVersionList(m_metha.getId());
    }

    interface FileButtonUiBinder extends UiBinder<HTMLPanel, FileButton> {
    }
}