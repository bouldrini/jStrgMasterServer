package jStrg.GWT.client.Data;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by Jura on 24.05.2016.
 */
public class JS_Privilege extends JavaScriptObject {
    protected JS_Privilege() {
    }
    public final native boolean canRead()/*-{
        return this.read;
    }-*/;

    public final native boolean canWrite()/*-{
        return this.write;
    }-*/;

    public final native boolean canDelete()/*-{
        return this.del;
    }-*/;

    public final native boolean canInvite()/*-{
        return this.invite;
    }-*/;
}
