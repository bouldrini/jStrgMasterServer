package jStrg.GWT.client.GUI.FileView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import jStrg.GWT.client.Data.JS_File;
import jStrg.GWT.client.LoginServiceClientImpl;

/**
 * Created by Jura on 29.06.2016.
 */
public class File extends Composite {
    private static FileUiBinder ourUiBinder = GWT.create(FileUiBinder.class);
    @UiField
    TableCellElement name;
    @UiField
    TableCellElement extension;
    @UiField
    Anchor download;


    private JS_File m_file;
    private LoginServiceClientImpl serviceClient;
    public File(LoginServiceClientImpl _loginServiceClient) {
        initWidget(ourUiBinder.createAndBindUi(this));
        this.serviceClient = _loginServiceClient;
    }

    /**
     *
     * @param _file Javascript file object
     */
    public void init(JS_File _file) {
        this.m_file = _file;
        this.name.setInnerText(m_file.getTitle());
        this.extension.setInnerText(m_file.getExtension());
        download.setText("Download");
        // get request for file
        download.setHref(GWT.getModuleBaseURL()+ "Service?file="+m_file.getId());
    }
    interface FileUiBinder extends UiBinder<HTMLPanel, File> {
    }
}