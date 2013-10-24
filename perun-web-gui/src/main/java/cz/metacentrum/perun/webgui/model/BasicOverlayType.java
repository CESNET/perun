package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Object definition for primitive types. 
 * Can return Int, String, Boolean, Float from itself.
 * 
 * @author Vaclav Mach  <374430@mail.muni.cz>
 * @version $Id$
 */
public class BasicOverlayType extends JavaScriptObject {

	protected BasicOverlayType() {}

	public final native String getString() /*-{
		return this.value;
	}-*/;
	
	public final native boolean getBoolean() /*-{
		return this.value;
	}-*/;
	
	public final native int getInt() /*-{
		return this.value;
	}-*/;
	
	public final native float getFloat() /*-{
		return this.value;
	}-*/;

}