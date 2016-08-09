package jStrg.GWT.client.GUI.FileView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import jStrg.GWT.client.Data.JS_Version;
import jStrg.GWT.client.LoginServiceClientImpl;

/**
 * Created by Jura on 06.07.2016.
 */
public class Rollback extends Composite {
    private LoginServiceClientImpl m_serviceClient;
    private JsArray<JS_Version> m_versionList;

    public void init(JsArray<JS_Version> _versionList) {
        m_versionList = _versionList;
        this.rollbackItems.clear();
        for (int i = 0; i < m_versionList.length(); ++i){
            this.rollbackItems.add(new VersionItem(m_serviceClient, m_versionList.get(i)));
        }
    }

    interface RollbackUiBinder extends UiBinder<HTMLPanel, Rollback> {
    }

    private static RollbackUiBinder ourUiBinder = GWT.create(RollbackUiBinder.class);
    @UiField
    HTMLPanel rollbackItems;


    public Rollback(LoginServiceClientImpl _serviceClient) {
        m_serviceClient = _serviceClient;
        initWidget(ourUiBinder.createAndBindUi(this));
    }
}