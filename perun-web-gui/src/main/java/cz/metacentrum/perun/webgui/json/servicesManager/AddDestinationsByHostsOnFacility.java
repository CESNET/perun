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
 * Ajax query which creates a new destinations for service and facility based on facility hosts (their names)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddDestinationsByHostsOnFacility {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "servicesManager/addDestinationsDefinedByHostsOnFacility";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private Facility facility;
	private ArrayList<Service> services = new ArrayList<Service>();

	/**
	 * Creates a new request
	 *
	 * @param facility facility to have destination added
	 */
	public AddDestinationsByHostsOnFacility(Facility facility) {
		this.facility = facility;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param facility facility to have destination added
	 * @param events   custom events
	 */
	public AddDestinationsByHostsOnFacility(Facility facility, final JsonCallbackEvents events) {
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

		if (errorMsg.length() > 0) {
			UiElements.generateAlert("Wrong parameter", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to add new Destination to service and facility, it first tests the values and then
	 * submits them.
	 *
	 * @param service to have destinations by hosts added
	 */
	public void addDestinationByHosts(Service service) {
		ArrayList<Service> servs = new ArrayList<Service>();
		servs.add(service);
		addDestinationByHosts(servs);
	}

	/**
	 * Attempts to add new Destination to services and facility, it first tests the values and then
	 * submits them.
	 *
	 * @param services list of services to have destinations by hosts added
	 */
	public void addDestinationByHosts(ArrayList<Service> services) {

		this.services = services;

		// test arguments
		if (!this.testCreating()) {
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents() {
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding destination for facility: " + facility.getName() + " failed.");
				events.onError(error);
			}

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Destination for facility: " + facility.getName() + " added.");
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
		jsonQuery.put("facility", new JSONNumber(facility.getId()));
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
