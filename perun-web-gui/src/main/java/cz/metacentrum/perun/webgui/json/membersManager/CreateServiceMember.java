package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;

import java.util.ArrayList;

/**
 * Ajax query to create service member in VO
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class CreateServiceMember {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "membersManager/createServiceMember";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// params
	private String name = "";
	private int voId = 0;
    private ArrayList<User> users;
    private String login = "";
    private String namespace = "";
    private String email = "";

	/**
	 * Creates a new request
	 */
	public CreateServiceMember() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public CreateServiceMember(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to create service member in VO
	 * 
	 * @param voId vo where member should be created
	 * @param name name of service member
     * @param users list of real users
     * @param login users login in einfra
	 */
	public void createMember(final int voId, final String name, final String email, ArrayList<User> users, String namespace, String login)
	{

		this.voId = voId;
		this.name = name;
        this.email = email;
        this.users = users;
        this.login = login;
        this.namespace = namespace;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating member: " + name + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Member "+ name +" created !");
				
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
        if(namespace.isEmpty()){
            errorMsg += "Wrong 'Namespace' parameter.\n";
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

        // attributes object !! login !!
        JSONObject attrs = new JSONObject();
        attrs.put("urn:perun:user:attribute-def:def:login-namespace:"+namespace, new JSONString(login));
        attrs.put("urn:perun:member:attribute-def:def:mail", new JSONString(email));

		// create new form of candidate
		JSONObject newCandidate = new JSONObject();
		newCandidate.put("attributes", attrs);
		newCandidate.put("additionalUserExtSources", null);
		newCandidate.put("userExtSource", null);
		newCandidate.put("id", null);
		newCandidate.put("firstName", new JSONString(""));
		newCandidate.put("lastName", new JSONString(name));
		newCandidate.put("middleName", null);
		newCandidate.put("titleAfter", null);
		newCandidate.put("titleBefore", null);

        JSONArray array = new JSONArray();

        for (int i=0; i<users.size(); i++) {

            JSONObject user = new JSONObject(users.get(i));

            JSONValue id = user.get("id");
            JSONValue firstName = user.get("firstName");
            JSONValue lastName = user.get("lastName");
            JSONValue middleName = user.get("middleName");
            JSONValue titleAfter = user.get("titleAfter");
            JSONValue titleBefore = user.get("titleBefore");
            JSONValue service = user.get("serviceUser");

            JSONObject newUser = new JSONObject();
            newUser.put("id", id);
            newUser.put("firstName", firstName);
            newUser.put("lastName", lastName);
            newUser.put("middleName", middleName);
            newUser.put("titleAfter", titleAfter);
            newUser.put("titleBefore", titleBefore);
            newUser.put("serviceUser", service);

            array.set(i, newUser);

        }

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();      
		jsonQuery.put("vo", selectedVoId);    
		jsonQuery.put("candidate", newCandidate);
        jsonQuery.put("serviceUserOwners", array);

		return jsonQuery;
		
	}

}
