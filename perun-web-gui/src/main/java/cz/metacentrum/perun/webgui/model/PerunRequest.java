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

	public final native String getParamsString() /*-{
		return this.params;
	}-*/;

	public final native String getCallbackId() /*-{
		return this.callbackId;
	}-*/;

	public final native PerunPrincipal getPerunPrincipal() /*-{
		return this.perunPrincipal;
	}-*/;

	public final native void setMethod(String method) /*-{
		this.method = method;
	}-*/;

	public final native void setManager(String manager) /*-{
		this.manager = manager;
	}-*/;

	public final native void setStartTime() /*-{
		this.startTime = new Date().getTime();
	}-*/;

	public final native double getDuration() /*-{
		return new Date().getTime() - this.startTime;
	}-*/;

	public final native double getEndTime() /*-{
		return this.endTime;
	}-*/;

	public final native JavaScriptObject getResult() /*-{
		return this.result;
	}-*/;

	public final native void setParamString(String params) /*-{
		this.params = params;
	}-*/;

}