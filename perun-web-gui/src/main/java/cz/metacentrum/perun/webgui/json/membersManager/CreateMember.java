package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Candidate;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;

/**
 * Ajax query to create member in VO from candidate or User
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
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
	public void createMember(final int voId, final Candidate candidate)
	{

		this.voId = voId;
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
	 * Attempts to create member in VO from existing perun user
	 * 
	 * @param voId vo where member should be created
	 * @param user user to be member
	 * 
	 */
	public void createMember(final int voId, final User user)
	{

		this.voId = voId;

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
			errorMsg += "Wrong 'Vo' parametr.\n";
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
		return jsonQuery;
		
	}

}
