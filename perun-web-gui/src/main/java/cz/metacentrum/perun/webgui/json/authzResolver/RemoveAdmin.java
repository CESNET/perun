package cz.metacentrum.perun.webgui.json.authzResolver;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.*;

/**
 * Ajax query which removes admin from VO / Group
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class RemoveAdmin {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String VO_JSON_URL = "vosManager/removeAdmin";
	final String GROUP_JSON_URL = "groupsManager/removeAdmin";
	final String FACILITY_JSON_URL = "facilitiesManager/removeAdmin";
	final String SECURITY_JSON_URL = "securityTeamsManager/removeAdmin";

	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private int userId = 0;
	private int entityId = 0;
	private PerunEntity entity;

	/**
	 * Creates a new request
	 *
	 * @param entity VO/GROUP/FACILITY
	 */
	public RemoveAdmin(PerunEntity entity) {
		this.entity = entity;
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param entity VO/GROUP/FACILITY
	 * @param events custom events
	 */
	public RemoveAdmin(PerunEntity entity, final JsonCallbackEvents events) {
		this.entity = entity;
		this.events = events;
	}

	/**
	 * Attempts to remove admin from Group, it first tests the values and then submits them.
	 *
	 * @param group where we want to remove admin
	 * @param user User to be removed from admin
	 */
	public void removeGroupAdmin(final Group group, final User user) {

		this.userId = (user != null) ? user.getId() : 0;
		this.entityId = (group != null) ? group.getId() : 0;
		this.entity = PerunEntity.GROUP;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing "+user.getFullName()+" from managers failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User " + user.getFullName()+ " removed from managers of "+group.getName());
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(GROUP_JSON_URL, prepareJSONObject());

	}

	/**
	 * Attempts to remove admin from VO, it first tests the values and then submits them.
	 *
	 * @param vo where we want to remove admin from
	 * @param user User to be removed from admins
	 */
	public void removeVoAdmin(final VirtualOrganization vo, final User user) {

		this.userId = (user != null) ? user.getId() : 0;
		this.entityId = (vo != null) ? vo.getId() : 0;
		this.entity = PerunEntity.VIRTUAL_ORGANIZATION;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing "+user.getFullName()+" from managers failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User " + user.getFullName()+ " removed from managers of "+vo.getName());
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(VO_JSON_URL, prepareJSONObject());

	}

	/**
	 * Attempts to remove admin from Facility, it first tests the values and then submits them.
	 *
	 * @param facility where we want to remove admin from
	 * @param user User to be removed from admins
	 */
	public void removeFacilityAdmin(final Facility facility, final User user) {

		this.userId = (user != null) ? user.getId() : 0;
		this.entityId = (facility != null) ? facility.getId() : 0;
		this.entity = PerunEntity.FACILITY;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing "+user.getFullName()+" from managers failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User " + user.getFullName()+ " removed form managers of "+facility.getName());
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(FACILITY_JSON_URL, prepareJSONObject());

	}

	/**
	 * Attempts to remove admin from SecurityTeam, it first tests the values and then submits them.
	 *
	 * @param securityTeam where we want to remove admin from
	 * @param user User to be removed from admins
	 */
	public void removeSecurityTeamAdmin(final SecurityTeam securityTeam, final User user) {

		this.userId = (user != null) ? user.getId() : 0;
		this.entityId = (securityTeam != null) ? securityTeam.getId() : 0;
		this.entity = PerunEntity.SECURITY_TEAM;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing "+user.getFullName()+" from managers failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User " + user.getFullName()+ " removed form managers of "+securityTeam.getName());
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(SECURITY_JSON_URL, prepareJSONObject());

	}

	/**
	 * Attempts to remove admin group from Group, it first tests the values and then submits them.
	 *
	 * @param groupToAddAdminTo where we want to remove admin group from
	 * @param group Group to be removed from admins
	 */
	public void removeGroupAdminGroup(final Group groupToAddAdminTo,final Group group) {

		// store group id to user id to used unified check method
		this.userId = (group != null) ? group.getId() : 0;
		this.entityId = (groupToAddAdminTo != null) ? groupToAddAdminTo.getId() : 0;
		this.entity = PerunEntity.GROUP;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing group "+group.getShortName()+" from managers failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group " + group.getShortName()+ " removed from managers of "+groupToAddAdminTo.getName());
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(GROUP_JSON_URL, prepareJSONObjectForGroup());

	}

	/**
	 * Attempts to remove admin group from VO, it first tests the values and then submits them.
	 *
	 * @param vo where we want to remove admin from
	 * @param group Group to be removed from admins
	 */
	public void removeVoAdminGroup(final VirtualOrganization vo,final Group group) {

		// store group id to user id to used unified check method
		this.userId = (group != null) ? group.getId() : 0;
		this.entityId = (vo != null) ? vo.getId() : 0;
		this.entity = PerunEntity.VIRTUAL_ORGANIZATION;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing group "+group.getShortName()+" from managers failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group " + group.getShortName()+ " removed from managers of "+vo.getName());
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(VO_JSON_URL, prepareJSONObjectForGroup());

	}

	/**
	 * Attempts to remove admin group from Facility, it first tests the values and then submits them.
	 *
	 * @param facility where we want to remove admin from
	 * @param group Group to be removed from admins
	 */
	public void removeFacilityAdminGroup(final Facility facility,final Group group) {

		// store group id to user id to used unified check method
		this.userId = (group != null) ? group.getId() : 0;
		this.entityId = (facility != null) ? facility.getId() : 0;
		this.entity = PerunEntity.FACILITY;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing group "+group.getShortName()+" from managers failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group " + group.getShortName()+ " removed from managers of "+facility.getName());
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(FACILITY_JSON_URL, prepareJSONObjectForGroup());

	}

	/**
	 * Attempts to remove admin group from SecurityTeam, it first tests the values and then submits them.
	 *
	 * @param securityTeam where we want to remove admin from
	 * @param group Group to be removed from admins
	 */
	public void removeSecurityTeamAdminGroup(final SecurityTeam securityTeam, final Group group) {

		// store group id to user id to used unified check method
		this.userId = (group != null) ? group.getId() : 0;
		this.entityId = (securityTeam != null) ? securityTeam.getId() : 0;
		this.entity = PerunEntity.SECURITY_TEAM;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing group "+group.getShortName()+" from managers failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group " + group.getShortName()+ " removed from managers of "+securityTeam.getName());
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(SECURITY_JSON_URL, prepareJSONObjectForGroup());

	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testRemoving() {
		boolean result = true;
		String errorMsg = "";

		if(entityId == 0){
			errorMsg += "Wrong parameter <strong>Entity ID</strong>.<br/>";
			result = false;
		}

		if(userId == 0){
			errorMsg += "Wrong parameter <strong>User ID</strong>.";
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
		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
			jsonQuery.put("vo", new JSONNumber(entityId));
		} else if (entity.equals(PerunEntity.GROUP)) {
			jsonQuery.put("group", new JSONNumber(entityId));
		} else if (entity.equals(PerunEntity.FACILITY)) {
			jsonQuery.put("facility", new JSONNumber(entityId));
		} else if (entity.equals(PerunEntity.SECURITY_TEAM)) {
			jsonQuery.put("securityTeam", new JSONNumber(entityId));
		}
		jsonQuery.put("user", new JSONNumber(userId));
		return jsonQuery;
	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObjectForGroup() {
		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
			jsonQuery.put("vo", new JSONNumber(entityId));
		} else if (entity.equals(PerunEntity.GROUP)) {
			jsonQuery.put("group", new JSONNumber(entityId));
		} else if (entity.equals(PerunEntity.FACILITY)) {
			jsonQuery.put("facility", new JSONNumber(entityId));
		} else if (entity.equals(PerunEntity.SECURITY_TEAM)) {
			jsonQuery.put("securityTeam", new JSONNumber(entityId));
		}
		jsonQuery.put("authorizedGroup", new JSONNumber(userId));
		return jsonQuery;
	}

}
