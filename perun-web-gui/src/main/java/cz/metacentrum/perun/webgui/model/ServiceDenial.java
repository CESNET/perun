package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for ServiceDenial object
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ServiceDenial extends JavaScriptObject {

  protected ServiceDenial() {
  }

  public final native int getServiceId() /*-{
		return this.serviceId;
	}-*/;

  public final native int getFacilityId() /*-{
		return this.facilityId;
	}-*/;

  public final native int getDestinationId() /*-{
		return this.destinationId;
	}-*/;

}
