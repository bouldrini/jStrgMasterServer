package jStrg.GWT.client.GUI;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import jStrg.GWT.client.LoginServiceClientImpl;

/**
 * Created by Jura on 17.05.2016.
 */
public class Login extends Composite {
    private static LoginUiBinder ourUiBinder = GWT.create(LoginUiBinder.class);
    @UiField
    TextBox nameTextBox;
    @UiField
    PasswordTextBox passwordTextBox;
    @UiField
    Hyperlink loginBtn;
    @UiField
    Hyperlink registerLink;
    private LoginServiceClientImpl serviceClient;


    public Login(LoginServiceClientImpl serviceClient) {
        initWidget(ourUiBinder.createAndBindUi(this));
        this.serviceClient = serviceClient;
    }

    @UiHandler("loginBtn")
    public void loginBtnClick(ClickEvent event) {
        serviceClient.login(nameTextBox.getText(), passwordTextBox.getText());
    }

    @UiHandler("registerLink")
    public void registerClick(ClickEvent event) {
        RootPanel.get(serviceClient.PAGE_CONTENT).clear();
        RootPanel.get(serviceClient.PAGE_CONTENT).add(serviceClient.getRegUI());
    }

    interface LoginUiBinder extends UiBinder<HTMLPanel, Login> {
    }
}