package jStrg.GWT.client.Data;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by Jura on 24.05.2016.
 */
public class JS_User extends JavaScriptObject {
    protected JS_User() {
    }

    public final native int getUserId()/*-{
        return this.id;
    }-*/;

    public final native String  getSessionId()/*-{
        return this.session_id;
    }-*/;

    public final native String getUsername()/*-{
        return this.username;
    }-*/;

    public final native int getTotalSpace()/*-{
        return this.total_space;
    }-*/;

    public final native int getUsedSpace()/*-{
        return this.used_space;
    }-*/;

    public final native int getRoleId()/*-{
        return this.role_id;
    }-*/;

    public final native String getRoleName()/*-{
        return this.role_name;
    }-*/;

    public final native int getRootFolderId()/*-{
        return this.root_folder_id;
    }-*/;

    public final native int getApplicationId()/*-{
        return this.application_id;
    }-*/;


}
