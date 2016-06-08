package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.*;

/**
 * Ajax query to create member in VO from candidate or User
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class CreateMember {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "membersManager/createMember";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// params
	private Candidate candidate = null;
	private int voId = 0;
	private Group group;

	/**
	 * Creates a new request
	 */
	public CreateMember() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public CreateMember(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to create member in VO from candidate
	 *
	 * @param voId vo where member should be created
	 * @param candidate candidate to be member
	 *
	 */
	public void createMember(final int voId,final Candidate candidate) {
		createMember(voId, null, candidate);
	}

	/**
	 * Attempts to create member in VO from candidate
	 *
	 * @param voId vo where member should be created
	 * @param group where member should be created
	 * @param candidate candidate to be member
	 *
	 */
	public void createMember(final int voId, Group group, final Candidate candidate) {

		this.voId = voId;
		this.group = group;
		this.candidate = candidate;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		if (!session.isVoAdmin(voId)) {
			// GROUP ADMIN HAVE OWN PROCESSING
			createMemberAsGroupAdmin(voId, group, candidate);
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating member: " + candidate.getDisplayName() + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Member "+ candidate.getDisplayName() +" created !");

				// call validation asynchronously
				Member mem = jso.cast();
				ValidateMemberAsync request = new ValidateMemberAsync();
				request.validateMemberAsync(mem);

				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, prepareJSONObject());

	}

	/**
	 * Attempts to create member in VO from candidate for Group admin
	 *
	 * @param voId vo where member should be created
	 * @param group where member should be created
	 * @param candidate candidate to be member
	 *
	 */
	public void createMemberAsGroupAdmin(final int voId, Group group, final Candidate candidate) {

		this.voId = voId;
		this.group = group;
		this.candidate = candidate;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating member: " + candidate.getDisplayName() + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Member "+ candidate.getDisplayName() +" created !");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("vo", new JSONNumber(voId));

		if (group != null) {
			// GROUP OBJECT
			JSONObject oldGroup = new JSONObject(group);
			// RECONSTRUCT OBJECT
			JSONObject newGroup = new JSONObject();
			newGroup.put("id", oldGroup.get("id"));
			// fake new group short name as name in order to update
			newGroup.put("name", oldGroup.get("name"));
			newGroup.put("shortName", oldGroup.get("shortName"));
			newGroup.put("description", oldGroup.get("description"));
			newGroup.put("voId", oldGroup.get("voId"));
			newGroup.put("parentGroupId", oldGroup.get("parentGroupId"));
			newGroup.put("beanName", oldGroup.get("beanName"));

			JSONArray arr = new JSONArray();
			arr.set(0, newGroup);
			jsonQuery.put("groups", arr);
		}

		if (candidate != null) {
			ExtSource source = candidate.getUserExtSource().getExtSource();
			jsonQuery.put("extSource", new JSONNumber(source.getId()));
			jsonQuery.put("login", new JSONString(candidate.getUserExtSource().getLogin()));
		}

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, jsonQuery);

	}

	/**
	 * Attempts to create member in VO from existing perun user
	 *
	 * @param voId vo where member should be created
	 * @param user user to be member
	 */
	public void createMember(final int voId, final User user) {
		createMember(voId, null, user);
	}

	/**
	 * Attempts to create member in VO from existing perun user
	 *
	 * @param voId vo where member should be created
	 * @param group where member should be created
	 * @param user user to be member
	 *
	 */
	public void createMember(final int voId, final Group group, final User user) {

		this.voId = voId;
		this.group = group;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating member: " + user.getFullName() + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Member "+ user.getFullName() +" created !");

				// call validation asynchronously
				Member mem = jso.cast();
				ValidateMemberAsync request = new ValidateMemberAsync();
				request.validateMemberAsync(mem);

				events.onFinished(jso);

			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		JSONObject query = new JSONObject();
		query.put("vo", new JSONNumber(voId));
		query.put("user", new JSONNumber(user.getId()));
		if (group != null) {
			// GROUP OBJECT
			JSONObject oldGroup = new JSONObject(group);
			// RECONSTRUCT OBJECT
			JSONObject newGroup = new JSONObject();
			newGroup.put("id", oldGroup.get("id"));
			// fake new group short name as name in order to update
			newGroup.put("name", oldGroup.get("name"));
			newGroup.put("shortName", oldGroup.get("shortName"));
			newGroup.put("description", oldGroup.get("description"));
			newGroup.put("voId", oldGroup.get("voId"));
			newGroup.put("parentGroupId", oldGroup.get("parentGroupId"));
			newGroup.put("beanName", oldGroup.get("beanName"));

			JSONArray arr = new JSONArray();
			arr.set(0, newGroup);
			query.put("groups", arr);
		}

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, query);

	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testAdding()
	{
		boolean result = true;
		String errorMsg = "";

		if(voId == 0){
			errorMsg += "Wrong 'Vo' parameter.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

		JSONNumber selectedVoId = new JSONNumber(voId);

		// create Json object from webgui candidate
		JSONObject toBeCandidate = new JSONObject(candidate);

		// get only interested in items (lost unknown $H property added by gwt)
		JSONValue attributes = toBeCandidate.get("attributes");
		JSONValue additionalUserExtSources = toBeCandidate.get("additionalUserExtSources");
		JSONValue userExtSource = toBeCandidate.get("userExtSource");
		JSONValue id = toBeCandidate.get("id");
		JSONValue firstName = toBeCandidate.get("firstName");
		JSONValue lastName = toBeCandidate.get("lastName");
		JSONValue middleName = toBeCandidate.get("middleName");
		JSONValue titleAfter = toBeCandidate.get("titleAfter");
		JSONValue titleBefore = toBeCandidate.get("titleBefore");

		// create new form of candidate
		JSONObject newCandidate = new JSONObject();
		newCandidate.put("attributes", attributes);
		newCandidate.put("additionalUserExtSources", additionalUserExtSources);
		newCandidate.put("userExtSource", userExtSource);
		newCandidate.put("id", id);
		newCandidate.put("firstName", firstName);
		newCandidate.put("lastName", lastName);
		newCandidate.put("middleName", middleName);
		newCandidate.put("titleAfter", titleAfter);
		newCandidate.put("titleBefore", titleBefore);

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("vo", selectedVoId);
		jsonQuery.put("candidate", newCandidate);

		if (group != null) {
			// GROUP OBJECT
			JSONObject oldGroup = new JSONObject(group);
			// RECONSTRUCT OBJECT
			JSONObject newGroup = new JSONObject();
			newGroup.put("id", oldGroup.get("id"));
			// fake new group short name as name in order to update
			newGroup.put("name", oldGroup.get("name"));
			newGroup.put("shortName", oldGroup.get("shortName"));
			newGroup.put("description", oldGroup.get("description"));
			newGroup.put("voId", oldGroup.get("voId"));
			newGroup.put("parentGroupId", oldGroup.get("parentGroupId"));
			newGroup.put("beanName", oldGroup.get("beanName"));

			JSONArray arr = new JSONArray();
			arr.set(0, newGroup);
			jsonQuery.put("groups", arr);
		}

		return jsonQuery;

	}

}
