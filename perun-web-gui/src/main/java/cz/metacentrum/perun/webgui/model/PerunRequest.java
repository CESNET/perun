package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for PerunRequest object
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PerunRequest extends JavaScriptObject {

	protected PerunRequest() { }

	public final native String getMethod() /*-{
		return this.method;
	}-*/;

	public final native String getManager() /*-{
		return this.manager;
	}-*/;
	
	public final native double getStartTime() /*-{
		return this.startTime;
	}-*/;
	
	public final native String getSessionId() /*-{
		return this.method;
	}-*/;

	public final native String getParamsString() /*-{
		return this.params;
	}-*/;
	
	public final native PerunPrincipal getPerunPrincipal() /*-{
		return this.perunPrincipal;
	}-*/;





}