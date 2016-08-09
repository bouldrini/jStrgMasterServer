package jStrg.GWT.client.GUI.FileView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import jStrg.GWT.client.Data.JS_FileFolder;
import jStrg.GWT.client.Data.JS_Metha_FileFolder;
import jStrg.GWT.client.LoginServiceClientImpl;

/**
 * Created by Jura on 30.06.2016.
 */
public class FileUpload extends Composite {
    private final LoginServiceClientImpl m_serviceClient;
    private final JS_FileFolder m_folder;
    private final JS_Metha_FileFolder m_metha;
    @UiField
    FormPanel uploadForm;
    @UiField
    com.google.gwt.user.client.ui.FileUpload uploadFile;
    @UiField
    Button uploadFileBtn;
    @UiField
    Hidden parent_id;
    @UiField
    Hidden file_id;

    @UiHandler("uploadFileBtn")
    public void handleClick(ClickEvent event) {
        this.parent_id.setValue("" + (this.m_folder == null? -1 : this.m_folder.getId()));
        this.file_id.setValue(""+ (this.m_metha == null? -1 : this.m_metha.getId()));
        this.uploadForm.submit();
    }

    /**
     *  handele resporse after post request
     */
    @UiHandler("uploadForm")
    public void handleSubmitComplete(FormPanel.SubmitCompleteEvent event) {
        if (event.getResults().contains("success")){
            this.m_serviceClient.getFolderById(m_folder != null? m_folder.getId(): m_metha.getParentId());
        }else
            Window.alert(event.getResults());
    }

    interface FileUploadUiBinder extends UiBinder<HTMLPanel, FileUpload> {
    }

    private static FileUploadUiBinder ourUiBinder = GWT.create(FileUploadUiBinder.class);

    public FileUpload(LoginServiceClientImpl _serviceClient, JS_FileFolder _folder) {
        initWidget(ourUiBinder.createAndBindUi(this));
        m_serviceClient = _serviceClient;
        m_folder = _folder;
        m_metha = null;
        uploadForm.setAction(GWT.getModuleBaseURL() + "Service");
        uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        uploadForm.setMethod(FormPanel.METHOD_POST);
    }

    public FileUpload(LoginServiceClientImpl _serviceClient, JS_Metha_FileFolder _metha) {
        initWidget(ourUiBinder.createAndBindUi(this));
        m_serviceClient = _serviceClient;
        m_folder = null;
        m_metha = _metha;
        uploadForm.setAction(GWT.getModuleBaseURL() + "Service");
        uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        uploadForm.setMethod(FormPanel.METHOD_POST);
    }

}