package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONValue;
import cz.metacentrum.perun.webgui.json.JsonUtils;

import java.util.Map;

/**
 * Object definition for facility propagation state
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilityState extends JavaScriptObject {

	protected FacilityState(){};

	public final native Facility getFacility() /*-{
		return this.facility;
	}-*/;

	public final native String getState() /*-{
		return this.state;
	}-*/;

	public final native JavaScriptObject getResults() /*-{
		return this.results;
	}-*/;

	public final Map<String, JSONValue> getDestinations() {
		return JsonUtils.parseJsonToMap(getResults());
	};

	/**
	 * Compares to another object
	 * @param o Object to compare
	 * @return true, if they are the same
	 */
	public final boolean equals(FacilityState o) {
		return o.getFacility().equals(getFacility()) && o.getState().equals(getState());
	}

}
