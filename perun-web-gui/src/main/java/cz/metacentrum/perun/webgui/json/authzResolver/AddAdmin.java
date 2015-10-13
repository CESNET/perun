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
 * Ajax query which adds admin to VO or Group
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddAdmin {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// ids
	private int userId = 0;
	private int entityId = 0;
	private PerunEntity entity;
	// URL to call
	final String VO_JSON_URL = "vosManager/addAdmin";
	final String GROUP_JSON_URL = "groupsManager/addAdmin";
	final String FACILITY_JSON_URL = "facilitiesManager/addAdmin";
	final String SECURITY_JSON_URL = "securityTeamsManager/addAdmin";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public AddAdmin() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public AddAdmin(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testAdding() {

		boolean result = true;
		String errorMsg = "";

		if(entityId == 0){
			errorMsg += "Wrong parameter <strong>VO/Group/Facility/SecurityTeam ID</strong>";
			result = false;
		}

		if(userId == 0){
			errorMsg += "Wrong parameter <strong>User/Group ID</strong>";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to add a new admin to Group, it first tests the values and then submits them.
	 *
	 * @param group where we want to add admin
	 * @param user User to be admin
	 */
	public void addGroupAdmin(final Group group, final User user) {

		this.userId = (user != null) ? user.getId() : 0;
		this.entityId = (group != null) ? group.getId() : 0;
		this.entity = PerunEntity.GROUP;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding "+user.getFullName()+" as manager failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User " + user.getFullName()+ " added as manager of "+group.getName());
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
	 * Attempts to add a new admin to VO, it first tests the values and then submits them.
	 *
	 * @param vo where we want to add admin
	 * @param user User to be admin
	 */
	public void addVoAdmin(final VirtualOrganization vo, final User user) {

		this.userId = (user != null) ? user.getId() : 0;
		this.entityId = (vo != null) ? vo.getId() : 0;
		this.entity = PerunEntity.VIRTUAL_ORGANIZATION;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding "+user.getFullName()+" as manager failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User " + user.getFullName()+ " added as manager of "+vo.getName());
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
	 * Attempts to add a new admin to Facility, it first tests the values and then submits them.
	 *
	 * @param facility where we want to add admin
	 * @param user User to be admin
	 */
	public void addFacilityAdmin(final Facility facility, final User user) {

		this.userId = (user != null) ? user.getId() : 0;
		this.entityId = (facility != null) ? facility.getId() : 0;
		this.entity = PerunEntity.FACILITY;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding "+user.getFullName()+" as manager failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User " + user.getFullName()+ " added as manager of "+facility.getName());
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
	 * Attempts to add a new admin to SecurityTeam, it first tests the values and then submits them.
	 *
	 * @param securityTeam where we want to add admin
	 * @param user User to be admin
	 */
	public void addSecurityTeamAdmin(final SecurityTeam securityTeam, final User user) {

		this.userId = (user != null) ? user.getId() : 0;
		this.entityId = (securityTeam != null) ? securityTeam.getId() : 0;
		this.entity = PerunEntity.SECURITY_TEAM;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding "+user.getFullName()+" as manager failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User " + user.getFullName()+ " added as manager of "+securityTeam.getName());
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
	 * Attempts to add a new admin group to Group, it first tests the values and then submits them.
	 *
	 * @param groupToAddAdminTo where we want to add admin
	 * @param group Group to be admin
	 */
	public void addGroupAdminGroup(final Group groupToAddAdminTo,final Group group) {

		// store group id to user id to used unified check method
		this.userId = (group != null) ? group.getId() : 0;
		this.entityId = (groupToAddAdminTo != null) ? groupToAddAdminTo.getId() : 0;
		this.entity = PerunEntity.GROUP;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding group "+group.getShortName()+" as manager failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group " + group.getShortName()+ " added as manager of "+groupToAddAdminTo.getName());
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
	 * Attempts to add a new admin group to VO, it first tests the values and then submits them.
	 *
	 * @param vo where we want to add admin
	 * @param group Group to be admin
	 */
	public void addVoAdminGroup(final VirtualOrganization vo,final Group group) {

		// store group id to user id to used unified check method
		this.userId = (group != null) ? group.getId() : 0;
		this.entityId = (vo != null) ? vo.getId() : 0;
		this.entity = PerunEntity.VIRTUAL_ORGANIZATION;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding group "+group.getShortName()+" as manager failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group " + group.getShortName()+ " added as manager of "+vo.getName());
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
	 * Attempts to add a new admin group to Facility, it first tests the values and then submits them.
	 *
	 * @param facility where we want to add admin
	 * @param group Group to be admin
	 */
	public void addFacilityAdminGroup(final Facility facility,final Group group) {

		// store group id to user id to used unified check method
		this.userId = (group != null) ? group.getId() : 0;
		this.entityId = (facility != null) ? facility.getId() : 0;
		this.entity = PerunEntity.FACILITY;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding group "+group.getShortName()+" as manager failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group " + group.getShortName()+ " added as manager of "+facility.getName());
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
	 * Attempts to add a new admin group to SecurityTeam, it first tests the values and then submits them.
	 *
	 * @param securityTeam where we want to add admin
	 * @param group Group to be admin
	 */
	public void addSecurityTeamAdminGroup(final SecurityTeam securityTeam,final Group group) {

		// store group id to user id to used unified check method
		this.userId = (group != null) ? group.getId() : 0;
		this.entityId = (securityTeam != null) ? securityTeam.getId() : 0;
		this.entity = PerunEntity.SECURITY_TEAM;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding group "+group.getShortName()+" as manager failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group " + group.getShortName()+ " added as manager of "+securityTeam.getName());
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
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

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
