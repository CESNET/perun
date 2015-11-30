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
 * Ajax query which Assigns group to resources
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AssignGroupToResources {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "resourcesManager/assignGroupToResources";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private Group group;
	private ArrayList<RichResource> resources;

	/**
	 * Creates a new request
	 */
	public AssignGroupToResources() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public AssignGroupToResources(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to assign group to resources
	 *
	 * @param group group which should be assigned
	 * @param resources resources where group should be assigned
	 */
	public void assignGroupToResources(final Group group, final ArrayList<RichResource> resources) {

		this.group = group;
		this.resources = resources;

		// test arguments
		if(!this.testAssigning()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Assigning group: " + group.getShortName() + " failed.");
				handleCommonExceptions(error, group);
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group: "+group.getShortName()+" was successfully assigned to resources.");
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

		if(resources == null || resources.isEmpty()){
			errorMsg += "Wrong parameter <strong>Resources</strong>.<br />";
			result = false;
		}

		if(group == null){
			errorMsg += "Wrong parameter <strong>group</strong>.";
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
		for (int i=0; i< resources.size(); i++) {
			array.set(i, new JSONNumber(resources.get(i).getId()));
		}
		jsonQuery.put("resources", array);
		jsonQuery.put("group", new JSONNumber(group.getId()));

		return jsonQuery;
	}

	/**
	 * Handle common exceptions caused by this callback.
	 *
	 * @param error     PerunError returned from Perun
	 * @param group  related group
	 */
	private void handleCommonExceptions(PerunError error, Group group) {

		if (error != null) {

			if ("WrongAttributeValueException".equals(error.getName())) {

				Attribute a = error.getAttribute();
				GeneralObject holder = error.getAttributeHolder();
				GeneralObject secondHolder = error.getAttributeHolderSecondary();

				String text = "Group "+group.getShortName()+" can't be assigned to one of resources.";

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

				UiElements.generateError(error, "Already assigned", "Group is already assigned to one of resources.");

			} else if ("WrongReferenceAttributeValueException".equals(error.getName())) {

				String text = "Group "+group.getShortName()+" can't be assigned to one of resources.";

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
