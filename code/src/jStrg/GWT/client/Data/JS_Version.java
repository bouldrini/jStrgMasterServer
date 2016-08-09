package jStrg.GWT.client.Data;

import com.google.gwt.core.client.JavaScriptObject;
import java.util.Date;

/**
 * Created by Jura on 06.07.2016.
 */
public class JS_Version extends JavaScriptObject {
    protected JS_Version() {}

    public final native int getId()/*-{
        return this.id;
    }-*/;
    public final native int getVersion()/*-{
        return this.version;
    }-*/;
    public final native String getLastChanged()/*-{
        return this.modified;
    }-*/;
    public final native int getSize()/*-{
        return this.size;
    }-*/;
}
