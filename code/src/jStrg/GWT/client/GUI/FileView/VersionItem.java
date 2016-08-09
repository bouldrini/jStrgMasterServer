package jStrg.GWT.client.GUI.FileView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import jStrg.GWT.client.Data.JS_Version;
import jStrg.GWT.client.LoginServiceClientImpl;

/**
 * Created by Jura on 06.07.2016.
 */
public class VersionItem extends Composite {
    private LoginServiceClientImpl m_serviceClient;
    private JS_Version m_version;
    @UiField
    Label version;
    @UiField
    Label modified;
    @UiField
    Label size;
    @UiField
    Anchor download;
    @UiField
    Hyperlink rollback;

    @UiHandler("rollback")
    public void handleClick(ClickEvent event) {
        this.m_serviceClient.rollbackToVersion(m_version.getId());
    }

    public VersionItem(LoginServiceClientImpl _serviceClient, JS_Version _version) {
        initWidget(ourUiBinder.createAndBindUi(this));
        m_serviceClient = _serviceClient;
        m_version = _version;
        this.download.setHref(GWT.getModuleBaseURL()+ "Service?fileversion="+m_version.getId());
        this.version.setText(m_version.getVersion() + "");
        this.modified.setText(m_version.getLastChanged());
        this.size.setText(byteConverter(m_version.getSize()));
    }

    interface VersionItemUiBinder extends UiBinder<HTMLPanel, VersionItem> {
    }

    private static VersionItemUiBinder ourUiBinder = GWT.create(VersionItemUiBinder.class);

    public VersionItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    /**
     * conwerts size in Byte MB...
     * @param b
     * @return
     */
    private String byteConverter(long b){
        int unit = 1024;
        if (b < unit) return b + " B";
        int exp = (int) (Math.log(b) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return Math.round(b / Math.pow(unit, exp) * 100) /100.0 + pre + "B";
    }
}