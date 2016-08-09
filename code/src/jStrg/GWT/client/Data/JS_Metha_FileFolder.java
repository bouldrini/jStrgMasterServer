package jStrg.GWT.client.Data;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by Jura on 29.06.2016.
 */
public class JS_Metha_FileFolder extends JavaScriptObject {
    protected JS_Metha_FileFolder() {
    }

    public final native int getId()/*-{
        return this.id;
    }-*/;

    public final native int getParentId()/*-{
        return this.parent_id;
    }-*/;

    public final native void setId(int id)/*-{
        this.id = id;
    }-*/;

    public final native boolean isFolder()/*-{
        return this.isFolder;
    }-*/;

    public final native String getTitle()/*-{
        return this.title;
    }-*/;

    public final native JS_Privilege getPrivilege()/*-{
        return this.privilege;
    }-*/;

    public final native void setTitle(String title)/*-{
        this.title = title;
    }-*/;

    public final native void setType(boolean type)/*-{
        this.isFolder = type;
    }-*/;
}
