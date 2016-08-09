package jStrg.GWT.client.GUI.Navigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.RootPanel;
import jStrg.GWT.client.LoginServiceClientImpl;

/**
 * Created by Jura on 17.05.2016.
 */
public class Navigation extends Composite {
    private static NavigationUiBinder ourUiBinder = GWT.create(NavigationUiBinder.class);
    public LoginServiceClientImpl serviceClient;
    @UiField
    UListElement ulElement;
    @UiField
    Hyperlink nav1;
    @UiField
    Hyperlink nav3;
    @UiField
    Hyperlink nav4;

    public Navigation(LoginServiceClientImpl serviceClient) {
        initWidget(ourUiBinder.createAndBindUi(this));
        this.serviceClient = serviceClient;
        if (serviceClient.getUser() == null) {
            nav3.setVisible(false);
            nav4.setVisible(false);
        }
    }

    public void navShow() {
        nav3.setVisible(true);
        nav4.setVisible(true);
    }

    @UiHandler("nav1")
    public void nav1Click(ClickEvent event) {
        if (serviceClient.getUser() != null) {
            RootPanel.get(serviceClient.PAGE_CONTENT).clear();
            RootPanel.get(serviceClient.PAGE_CONTENT).add(serviceClient.getUserData());
        } else {
            RootPanel.get(serviceClient.PAGE_CONTENT).clear();
            RootPanel.get(serviceClient.PAGE_CONTENT).add(serviceClient.getLoginGUI());
        }
    }

    @UiHandler("nav3")
    public void nav3Click(ClickEvent event) {
        serviceClient.getFolderById(serviceClient.getUser().getRootFolderId());
    }

    @UiHandler("nav4")
    public void nav4Click(ClickEvent event) {
        serviceClient.logout();
        Window.Location.reload();
    }

    interface NavigationUiBinder extends UiBinder<HTMLPanel, Navigation> {
    }
}