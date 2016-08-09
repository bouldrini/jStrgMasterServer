package jStrg.GWT.client.GUI;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import jStrg.GWT.client.LoginServiceClientImpl;

/**
 * Created by Jura on 24.05.2016.
 */
public class Registration extends Composite {
    private static RegistrationUiBinder ourUiBinder = GWT.create(RegistrationUiBinder.class);
    private final LoginServiceClientImpl serviceClient;

    public Registration(LoginServiceClientImpl serviceClient) {
        initWidget(ourUiBinder.createAndBindUi(this));
        this.serviceClient = serviceClient;
    }

    interface RegistrationUiBinder extends UiBinder<HTMLPanel, Registration> {
    }
}