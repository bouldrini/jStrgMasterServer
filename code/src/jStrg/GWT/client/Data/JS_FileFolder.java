package jStrg.GWT.client.Data;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Created by Jura on 25.05.2016.
 */
public class JS_FileFolder extends JavaScriptObject {
    protected JS_FileFolder() {
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

    public final native JsArray<JS_Metha_FileFolder> getFolders()/*-{
        return this.folders;
    }-*/;

    public final native JsArray<JS_Metha_FileFolder> getFiles()/*-{
        return this.files;
    }-*/;
}
