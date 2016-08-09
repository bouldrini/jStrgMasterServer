package jStrg.GWT.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import jStrg.GWT.client.Data.JS_User;
import jStrg.GWT.client.service.Service;

/**
 * Created by Jura on 15.05.2016.
 */

public class Login implements EntryPoint {

    private LoginServiceClientImpl clientImpl;
    /**
     * inizialization methode for client application
     */
    public void onModuleLoad() {
        //connect client and "server/service/ServiceImpl" for rpc
        clientImpl = new LoginServiceClientImpl(GWT.getModuleBaseURL() + "Service");

        String sessionID = Cookies.getCookie("sid");
        if (sessionID == null)
        {
            RootPanel.get(clientImpl.PAGE_CONTENT).add(clientImpl.getLoginGUI());
            RootPanel.get(clientImpl.PAGE_NAVIGATION).add(clientImpl.getNav());
        } else
        {
            checkWithServerIfSessionIdIsStillLegal(sessionID);
        }
    }

    private void checkWithServerIfSessionIdIsStillLegal(String sessionID)
    {
        //clientImpl = new LoginServiceClientImpl(GWT.getModuleBaseURL() + "Service");
        Service.App.getInstance().loginFromSession(new AsyncCallback<String>()
        {
            @Override
            public void onFailure(Throwable caught)
            {
                RootPanel.get(clientImpl.PAGE_CONTENT).clear();
                RootPanel.get(clientImpl.PAGE_NAVIGATION).clear();
                RootPanel.get(clientImpl.PAGE_CONTENT).add(clientImpl.getLoginGUI());
                RootPanel.get(clientImpl.PAGE_NAVIGATION).add(clientImpl.getNav());
            }

            @Override
            public void onSuccess(String result)
            {
                JS_User user = null;
                if (result == null)
                {
                    RootPanel.get(clientImpl.PAGE_CONTENT).clear();
                    RootPanel.get(clientImpl.PAGE_NAVIGATION).clear();
                    RootPanel.get(clientImpl.PAGE_CONTENT).add(clientImpl.getLoginGUI());
                    RootPanel.get(clientImpl.PAGE_NAVIGATION).add(clientImpl.getNav());
                } else
                {
                    String json = result;
                    json = json.substring(json.indexOf(":") + 1, json.length());
                    if (result.startsWith("\"User\"")) {
                        user = JsonUtils.<JS_User>safeEval(json);
                        clientImpl.setUser(user);
                        // show user view
                        if (!user.getSessionId().isEmpty()&&!user.getSessionId().equals("null")) {
                            clientImpl.getNav().navShow();
                            clientImpl.getUserData().userInit();
                            RootPanel.get(clientImpl.PAGE_CONTENT).clear();
                            RootPanel.get(clientImpl.PAGE_NAVIGATION).clear();
                            RootPanel.get(clientImpl.PAGE_CONTENT).add(clientImpl.getUserData());
                            RootPanel.get(clientImpl.PAGE_NAVIGATION).add(clientImpl.getNav());
                            // loginview
                        } else {
                            RootPanel.get(clientImpl.PAGE_CONTENT).clear();
                            RootPanel.get(clientImpl.PAGE_NAVIGATION).clear();
                            RootPanel.get(clientImpl.PAGE_CONTENT).add(clientImpl.getLoginGUI());
                            RootPanel.get(clientImpl.PAGE_NAVIGATION).add(clientImpl.getNav());
                        }
                    }
                }
            }

        });
    }
}
