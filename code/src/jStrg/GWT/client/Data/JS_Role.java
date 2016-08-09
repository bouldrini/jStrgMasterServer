package jStrg.GWT.client.Data;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by Jura on 25.05.2016.
 */
public class JS_Role extends JavaScriptObject {
    protected JS_Role() {
    }

    public final native int getRoleId()/*-{
        return this.m_id;
    }-*/;

    public final native String getTitle()/*-{
        return this.m_title;
    }-*/;

    public final native int getVersion()/*-{
        return this.version;
    }-*/;

    public final native String getCreated()/*-{
        return this.created;
    }-*/;
}
