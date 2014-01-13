package cz.metacentrum.perun.webgui.json.authzResolver;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;

/**
 * Ajax query which removes admin from VO / Group
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 61d3044b749b99e22722b4696d2c1abfd221b7fc $
 */
public class RemoveAdmin {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String VO_JSON_URL = "vosManager/removeAdmin";
    final String GROUP_JSON_URL = "groupsManager/removeAdmin";
    final String FACILITY_JSON_URL = "facilitiesManager/removeAdmin";

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
	 * Attempts to remove admin from VO
	 * 
	 * @param id ID of entity, where should be change done
	 * @param userId ID of user which should be removed from admins
	 */
	public void removeAdmin(final int id,final int userId)	{

		this.userId = userId;
		this.entityId = id;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing admin (User ID: " + userId + ") from "+entity+": "+entityId+" failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Admin (User ID: " + userId + ") removed from "+entity+": "+ entityId);
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);

        if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
            jspc.sendData(VO_JSON_URL, prepareJSONObject());
        } else if (entity.equals(PerunEntity.GROUP)) {
            jspc.sendData(GROUP_JSON_URL, prepareJSONObject());
        } else if (entity.equals(PerunEntity.FACILITY)) {
            jspc.sendData(FACILITY_JSON_URL, prepareJSONObject());
        }

	}

    /**
     * Attempts to remove admin from VO/Group/Facility, it first tests the values and then submits them.
     *
     * @param entity entity, where we want to remove admin
     * @param user User to be removed from admins
     */
    public void removeAdmin(final GeneralObject entity, final User user) {

        this.userId = (user != null) ? user.getId() : 0;
        this.entityId = (entity != null) ? entity.getId() : 0;

        // test arguments
        if(!this.testRemoving()){
            return;
        }

        // new events
        JsonCallbackEvents newEvents = new JsonCallbackEvents(){
            public void onError(PerunError error) {
                session.getUiElements().setLogErrorText("Removing "+user.getFullName()+" as admin failed.");
                events.onError(error); // custom events
            };

            public void onFinished(JavaScriptObject jso) {
                session.getUiElements().setLogSuccessText("User " + user.getFullName()+ " removed from admins of "+entity.getName());
                events.onFinished(jso);
            };

            public void onLoadingStart() {
                events.onLoadingStart();
            };
        };

        // sending data
        JsonPostClient jspc = new JsonPostClient(newEvents);

        if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
            jspc.sendData(VO_JSON_URL, prepareJSONObject());
        } else if (entity.equals(PerunEntity.GROUP)) {
            jspc.sendData(GROUP_JSON_URL, prepareJSONObject());
        } else if (entity.equals(PerunEntity.FACILITY)) {
            jspc.sendData(FACILITY_JSON_URL, prepareJSONObject());
        }

    }

    /**
     * Attempts to remove admin from VO
     *
     * @param id ID of entity, where should be change done
     * @param groupId ID of group which should be removed from admins
     */
    public void removeAdminGroup(final int id,final int groupId)	{

        this.userId = groupId;
        this.entityId = id;

        // test arguments
        if(!this.testRemoving()){
            return;
        }

        // new events
        JsonCallbackEvents newEvents = new JsonCallbackEvents(){
            public void onError(PerunError error) {
                session.getUiElements().setLogErrorText("Removing admin (Group ID: " + userId + ") from "+entity+": "+entityId+" failed.");
                events.onError(error);
            };

            public void onFinished(JavaScriptObject jso) {
                session.getUiElements().setLogSuccessText("Admin (Group ID: " + userId + ") removed from "+entity+": "+ entityId);
                events.onFinished(jso);
            };

            public void onLoadingStart() {
                events.onLoadingStart();
            };
        };

        // sending data
        JsonPostClient jspc = new JsonPostClient(newEvents);

        if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
            jspc.sendData(VO_JSON_URL, prepareJSONObjectForGroup());
        } else if (entity.equals(PerunEntity.GROUP)) {
            jspc.sendData(GROUP_JSON_URL, prepareJSONObjectForGroup());
        } else if (entity.equals(PerunEntity.FACILITY)) {
            jspc.sendData(FACILITY_JSON_URL, prepareJSONObjectForGroup());
        }

    }

    /**
     * Attempts to remove admin from VO/Group/Facility, it first tests the values and then submits them.
     *
     * @param entity where we want to remove admin
     * @param group Group to be removed from admins
     */
    public void removeAdminGroup(final GeneralObject entity,final Group group) {

        // store group id to user id to used unified check method
        this.userId = (group != null) ? group.getId() : 0;
        this.entityId = (entity != null) ? entity.getId() : 0;

        // test arguments
        if(!this.testRemoving()){
            return;
        }

        // new events
        JsonCallbackEvents newEvents = new JsonCallbackEvents(){
            public void onError(PerunError error) {
                session.getUiElements().setLogErrorText("Removing group "+group.getShortName()+" as admin failed.");
                events.onError(error); // custom events
            };

            public void onFinished(JavaScriptObject jso) {
                session.getUiElements().setLogSuccessText("Group " + group.getShortName()+ " removed from admins of "+entity.getName());
                events.onFinished(jso);
            };

            public void onLoadingStart() {
                events.onLoadingStart();
            };
        };

        // sending data
        JsonPostClient jspc = new JsonPostClient(newEvents);

        if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
            jspc.sendData(VO_JSON_URL, prepareJSONObjectForGroup());
        } else if (entity.equals(PerunEntity.GROUP)) {
            jspc.sendData(GROUP_JSON_URL, prepareJSONObjectForGroup());
        } else if (entity.equals(PerunEntity.FACILITY)) {
            jspc.sendData(FACILITY_JSON_URL, prepareJSONObjectForGroup());
        }

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
        }
        jsonQuery.put("authorizedGroup", new JSONNumber(userId));
        return jsonQuery;
    }

}