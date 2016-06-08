package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import cz.metacentrum.perun.webgui.json.JsonUtils;

import java.util.ArrayList;

/**
 * Overlay type for RichResource object from Perun
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class RichResource extends JavaScriptObject {

	protected RichResource() {}

	public final native VirtualOrganization getVo() /*-{
		return this.vo;
	}-*/;

	public final native Facility getFacility() /*-{
		return this.facility;
	}-*/;

	public final native int getId() /*-{
		return this.id;
	}-*/;

	public final native String getName() /*-{
		return this.name;
	}-*/;

	public final native void setName(String newName) /*-{
		this.name = newName;
	}-*/;

	public final native String getDescription() /*-{
		return this.description;
	}-*/;

	public final native void setDescription(String newDesc) /*-{
		this.description = newDesc;
	}-*/;

	public final native int getFacilityId() /*-{
		return this.facilityId;
	}-*/;

	public final native int getVoId() /*-{
		return this.voId;
	}-*/;

	public final ArrayList<ResourceTag> getResourceTags() {
		if (getResourceTagsInternal() != null) {
			return JsonUtils.jsoAsList(getResourceTagsInternal());
		} else {
			return new ArrayList<ResourceTag>();
		}
	}

	public final native JsArray<ResourceTag> getResourceTagsInternal() /*-{
		return this.resourceTags;
	}-*/;

	/**
	 * Returns Perun specific type of object
	 *
	 * @return type of object
	 */
	public final native String getObjectType() /*-{
		if (!this.beanName) {
			return "JavaScriptObject"
		}
		return this.beanName;
	}-*/;

	/**
	 * Sets Perun specific type of object
	 *
	 * @param type type of object
	 */
	public final native void setObjectType(String type) /*-{
		this.beanName = type;
	}-*/;

	/**
	 * Returns the status of this item in Perun system as String
	 * VALID, INVALID, SUSPENDED, EXPIRED, DISABLED
	 *
	 * @return string which defines item status
	 */
	public final native String getStatus() /*-{
		return this.status;
	}-*/;


	/**
	 * Compares to another object
	 * @param o Object to compare
	 * @return true, if they are the same
	 */
	public final boolean equals(RichResource o)
	{
		return o.getId() == this.getId();
	}
}
