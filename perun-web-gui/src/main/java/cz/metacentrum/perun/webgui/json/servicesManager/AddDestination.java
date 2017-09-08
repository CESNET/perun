package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;

import java.util.ArrayList;

/**
 * Ajax query which creates a new destination for service and facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddDestination {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "servicesManager/addDestination";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private Facility facility;
	private ArrayList<Service> services = new ArrayList<Service>();
	private String destination = "";
	private String type = "";
	private String propagationType = "PARALLEL";

	/**
	 * Creates a new request
	 *
	 * @param facility facility to have destination added
	 */
	public AddDestination(Facility facility) {
		this.facility = facility;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param facility facility to have destination added
	 * @param events   custom events
	 */
	public AddDestination(Facility facility, final JsonCallbackEvents events) {
		this.facility = facility;
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testCreating() {

		boolean result = true;
		String errorMsg = "";

		if (facility == null) {
			errorMsg += "Wrong parameter <strong>Facility</strong>.</br>";
			result = false;
		}

		if (services == null || services.isEmpty()) {
			errorMsg += "Wrong parameter <strong>Service</strong>.</br>";
			result = false;
		}

		if (destination.length() == 0) {
			errorMsg += "Wrong parameter <strong>Destination</strong>.</br>";
			result = false;
		}

		if (type.length() == 0) {
			errorMsg += "Wrong parameter <strong>Destination type</strong>.";
			result = false;
		}

		if (propagationType.length() == 0 || (!propagationType.equals("PARALLEL") && !propagationType.equals("DUMMY"))) {
			errorMsg += "Wrong parameter <strong>Propagation</strong>.";
			result = false;
		}

		if (errorMsg.length() > 0) {
			UiElements.generateAlert("Parameter Error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to add new Destination to service and facility, it first tests the values and then
	 * submits them.
	 *
	 * @param destination destination string
	 * @param type        type of destination
	 * @param service     service to add destination to
	 */
	public void addDestination(final String destination, final String type, final Service service) {
		ArrayList<Service> servs = new ArrayList<Service>();
		servs.add(service);
		addDestination(destination, type, servs, "PARALLEL");
	}

	/**
	 * Attempts to add new Destination to service and facility, it first tests the values and then
	 * submits them.
	 *
	 * @param destination destination string
	 * @param type        type of destination
	 * @param service     service to add destination to
	 * @param propagationType type of propagation PARALLEL or DUMMY
	 */
	public void addDestination(final String destination, final String type, final Service service, final String propagationType) {
		ArrayList<Service> servs = new ArrayList<Service>();
		servs.add(service);
		addDestination(destination, type, servs, propagationType);
	}

	/**
	 * Attempts to add new Destination to services and facility, it first tests the values and then
	 * submits them.
	 *
	 * @param destination destination string
	 * @param type        type of destination
	 * @param services    services to add destination to
	 */
	public void addDestination(final String destination, final String type, final ArrayList<Service> services, final String propagationType) {

		this.destination = destination;
		this.type = type;
		if (services != null && !services.isEmpty()) {
			this.services = services;
		}
		if (propagationType != null && !propagationType.isEmpty()) {
			this.propagationType = propagationType;
		}

		// test arguments
		if (!this.testCreating()) {
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents() {
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding destination" + destination + " failed.");
				events.onError(error);
			}

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Destination " + destination + " added.");
				events.onFinished(jso);
			}

			public void onLoadingStart() {
				events.onLoadingStart();
			}
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, prepareJSONObject());

	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("destination", new JSONString(destination));
		jsonQuery.put("type", new JSONString(type));
		jsonQuery.put("facility", new JSONNumber(facility.getId()));
		jsonQuery.put("propagationType", new JSONString(propagationType));

		JSONArray servs = new JSONArray();
		for (int i = 0; i < services.size(); i++) {
			// rebuild service object
			JSONObject srv = new JSONObject();
			srv.put("id", new JSONNumber(services.get(i).getId()));
			srv.put("name", new JSONString(services.get(i).getName()));
			servs.set(i, srv);
		}
		jsonQuery.put("services", servs);

		return jsonQuery;

	}

}
