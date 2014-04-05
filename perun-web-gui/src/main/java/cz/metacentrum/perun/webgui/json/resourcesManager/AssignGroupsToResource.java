package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonErrorHandler;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.*;

import java.util.ArrayList;

/**
 * Ajax query which Assigns group to resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AssignGroupsToResource {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "resourcesManager/assignGroupsToResource";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private Resource resource;
	private ArrayList<Group> groups;

	/**
	 * Creates a new request
	 */
	public AssignGroupsToResource() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public AssignGroupsToResource(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to assign groups to resource
	 *
	 * @param groups groups which should be assigned
	 * @param resource resource where should be assigned
	 */
	public void assignGroupsToResource(final ArrayList<Group> groups, final Resource resource) {

		this.resource = resource;
		this.groups = groups;

		// test arguments
		if(!this.testAssigning()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Assigning group(s) to resource: " + resource.getName() + " failed.");
				handleCommonExceptions(error, resource);
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group(s) successfully assigned to resource: "+ resource.getName());
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
        // to allow own error handling for attributes errors.
        jspc.setHidden(true);
		jspc.sendData(JSON_URL, prepareJSONObject());

	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testAssigning() {

		boolean result = true;
		String errorMsg = "";

		if(groups == null || groups.isEmpty()){
			errorMsg += "Wrong parameter <strong>Groups</strong>.<br />";
			result = false;
		}

		if(resource == null){
			errorMsg += "Wrong parameter <strong>Resource</strong>.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

		JSONObject jsonQuery = new JSONObject();

		JSONArray array = new JSONArray();
		for (int i=0; i<groups.size(); i++) {
			array.set(i, new JSONNumber(groups.get(i).getId()));
		}
		jsonQuery.put("groups", array);
		jsonQuery.put("resource", new JSONNumber(resource.getId()));

		return jsonQuery;
	}

	/**
	 * Handle common exceptions caused by this callback.
	 *
	 * @param error     PerunError returned from Perun
	 * @param resource  related resource
	 */
	private void handleCommonExceptions(PerunError error, Resource resource) {

		if (error != null) {

			if ("WrongAttributeValueException".equals(error.getName())) {

				Attribute a = error.getAttribute();
				GeneralObject holder = error.getAttributeHolder();
				GeneralObject secondHolder = error.getAttributeHolderSecondary();

				String text = "One of groups can't be assigned to resource " + resource.getName() + ".";

				if (a != null) {

					if (a.getValue().equals("null")) {
						text += "<p>Following setting is missing, but it's required by services on resource.";
					} else {
						text += "<p>Following setting, required by services on resource, is wrong.";
					}

					String attrName = a.getDisplayName();
					String attrValue = a.getValue();
					text += "<p><strong>Setting:&nbsp;</strong>" + attrName + "<br />";

					if (holder != null) {
						if (!holder.getName().equalsIgnoreCase("undefined")) {
							text += "<strong>" + holder.getObjectType() + ":</strong>&nbsp;" + holder.getName() + "<br />";
						}
					}
					if (secondHolder != null) {
						if (!secondHolder.getName().equalsIgnoreCase("undefined")) {
							text += "<strong>" + secondHolder.getObjectType() + ":</strong>&nbsp;" + secondHolder.getName() + "<br />";
						}
					}

					if (!a.getValue().equals("null")) {
						text += "<strong>Value:&nbsp;</strong>" + attrValue;
					}

					text += "<p>Please fix the issue before assigning group to resource on group's settings page.";

				} else {
					text += "<p><i>Attribute is null, please report this error.</i>";
				}

				UiElements.generateError(error, "Wrong settings", text);

			} else if ("GroupAlreadyAssignedException".equals(error.getName())) {

				UiElements.generateError(error, "Already assigned", "One of groups is already assigned to resource.");

			} else if ("WrongReferenceAttributeValueException".equals(error.getName())) {

				String text = "One of groups can't be assigned to resource " + resource.getName() + ".";

				text += "<p>Following combination of settings is not correct:";

				Attribute a = error.getAttribute();
				Attribute a2 = error.getReferenceAttribute();

				if (a != null) {
					String attrName = a.getDisplayName();
					String attrValue = a.getValue();
					String entity = a.getEntity();
					text += "<p><strong>Setting&nbsp;1:</strong>&nbsp;" + attrName + " (" + entity + ")";
					text += "<br/><strong>Value&nbsp;1:</strong>&nbsp;" + attrValue;
				} else {
					text += "<p><i>Setting 1 is null or not present in error message.</i>";
				}

				if (a2 != null) {
					String attrName = a2.getDisplayName();
					String attrValue = a2.getValue();
					String entity = a2.getEntity();
					text += "<p><strong>Setting&nbsp;2:</strong>&nbsp;" + attrName + " (" + entity + ")";
					text += "<br/><strong>Value&nbsp;2:</strong>&nbsp;" + attrValue;
				} else {
					text += "<p><i>Setting 2 is null or not present in error message.</i>";
				}

				UiElements.generateError(error, "Wrong settings", text);

			} else {

				// use standard processing for other errors
				JsonErrorHandler.alertBox(error);

			}

		}


	}

}
