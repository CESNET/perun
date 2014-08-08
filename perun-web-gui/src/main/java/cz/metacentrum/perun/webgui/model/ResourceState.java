package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONValue;
import cz.metacentrum.perun.webgui.json.JsonUtils;

import java.util.Map;

/**
 * Object definition for resource propagation state
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ResourceState extends JavaScriptObject {

	protected ResourceState(){};

	public final native Resource getResource() /*-{
		return this.resource;
	}-*/;

	public final native JsArray<Task> getTasks() /*-{
		return this.taskList;
	}-*/;

	/**
	 * Compares to another object
	 *
	 * @param o Object to compare
	 * @return true, if they are the same
	 */
	public final boolean equals(ResourceState o) {
		return o.getResource().equals(getResource());
	}

}