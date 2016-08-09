package jStrg.GWT.client.GUI;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import jStrg.GWT.client.LoginServiceClientImpl;

/**
 * Created by Jura on 23.06.2016.
 */
public class UserData extends Composite {
    private static UserDataUiBinder ourUiBinder = GWT.create(UserDataUiBinder.class);
    @UiField
    TableCellElement name;
    @UiField
    TableCellElement role;
    @UiField
    TableCellElement Space;
    private LoginServiceClientImpl serviceClient;
    public UserData(LoginServiceClientImpl serviceClient) {
        initWidget(ourUiBinder.createAndBindUi(this));
        this.serviceClient = serviceClient;
    }

    public void userInit() {
        this.name.setInnerText(serviceClient.getUser().getUsername());
        this.role.setInnerText(serviceClient.getUser().getRoleName());
        this.Space.setInnerText((serviceClient.getUser().getUsedSpace() / serviceClient.getUser().getTotalSpace() * 100) + "% used");
    }

    interface UserDataUiBinder extends UiBinder<HTMLPanel, UserData> {
    }
}