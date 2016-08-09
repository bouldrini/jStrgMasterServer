package jStrg.GWT.client.Data;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by Jura on 24.05.2016.
 */
public class JS_File extends JavaScriptObject {
    protected JS_File() {
    }

    public final native int getId()/*-{
        return this.id;
    }-*/;

    public final native int getParentId()/*-{
        return this.parent_id;
    }-*/;

    public final native String getTitle()/*-{
        return this.title;
    }-*/;

    public final native JS_Privilege getPrivilege()/*-{
        return this.privilege;
    }-*/;

    public final native String getExtension()/*-{
        return this.extension;
    }-*/;

    public final native boolean isPersistent()/*-{
        return this.persistent;
    }-*/;
}
