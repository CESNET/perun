package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JsArray;
import java.util.ArrayList;

/**
 * Overlay type for Group object from Perun
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class RichGroup extends Group {

	protected RichGroup() { }

	public final native void showAttributes() /*-{
		alert(JSON.stringify(this.attributes));
	}-*/;

	public final native JsArray getAttributes() /*-{
		return this.attributes;
	}-*/;

	public final native void setAttributes(ArrayList<Attribute> attrs) /*-{
		this.groupAttributes = attrs;
	}-*/;

	public final native boolean isSyncEnabled() /*-{
		for (var id in this.attributes) {
			if (this.attributes[id].friendlyName === "synchronizationEnabled") {
				if (this.attributes[id].value === null) return false;
				return (this.attributes[id].value === "true");
			}
		}
		return false;
	}-*/;
	public final native String getSynchronizationInterval() /*-{
		for (var id in this.attributes) {
			if (this.attributes[id].friendlyName === "synchronizationInterval") {
				return this.attributes[id].value;
			}
		}
		return null;
	}-*/;
	public final native String getLastSynchronizationState() /*-{
		for (var id in this.attributes) {
			if (this.attributes[id].friendlyName === "lastSynchronizationState") {
				return this.attributes[id].value;
			}
		}
		return null;
	}-*/;
	public final native String getLastSynchronizationTimestamp() /*-{
		for (var id in this.attributes) {
			if (this.attributes[id].friendlyName === "lastSynchronizationTimestamp") {
				return this.attributes[id].value;
			}
		}
		return null;
	}-*/;
	public final native String getLastSuccessSynchronizationTimestamp() /*-{
		for (var id in this.attributes) {
			if (this.attributes[id].friendlyName === "lastSuccessSynchronizationTimestamp") {
				return this.attributes[id].value;
			}
		}
		return null;
	}-*/;
	public final native String getAuthoritativeGroup() /*-{
		for (var id in this.attributes) {
			if (this.attributes[id].friendlyName === "authoritativeGroup") {
				if (this.attributes[id].value === null) return "0"; // not authoritative
				return this.attributes[id].value;
			}
		}
		return null;
	}-*/;

	/**
	 * Compares to another object
	 * @param o Object to compare
	 * @return true, if they are the same
	 */
	public final boolean equals(RichGroup o) {
		return o.getId() == this.getId();
	}

}