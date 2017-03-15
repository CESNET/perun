package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * Ajax query to create service member in VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateSpecificMember {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "membersManager/createSpecificMember";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// params
	private String name = "";
	private int voId = 0;
	private ArrayList<User> users;
	private String login = "";
	private String namespace = "";
	private String email = "";
	private String certDN = "";
	private String caCertDN = "";
	private String specificUserType = "service";

	/**
	 * Creates a new request
	 */
	public CreateSpecificMember() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public CreateSpecificMember(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to create service member in VO
	 *
	 * @param voId vo where member should be created
	 * @param name name of service member
	 * @param email email of service member
	 * @param users list of real users
	 * @param namespace namespace to create login in
	 * @param login users login in namespace
	 * @param certDN users cert DN
	 * @param caCertDN users CA cert DN
	 */
	public void createMember(final int voId, final String name, final String email, ArrayList<User> users, String namespace, String login, String certDN, String caCertDN, String specificUserType)  {

		this.voId = voId;
		this.name = name;
		this.email = email;
		this.users = users;
		this.login = login;
		this.namespace = namespace;
		this.certDN = certDN;
		this.caCertDN = caCertDN;
		this.specificUserType = specificUserType;

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
				request.setHidden(true);
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
	private boolean testAdding() {
		boolean result = true;
		String errorMsg = "";

		if(voId == 0){
			errorMsg += "Wrong <strong>Vo</strong> parameter.<br />";
			result = false;
		}
		if(name.isEmpty()){
			errorMsg += "Wrong <strong>Name</strong> parameter.<br />";
			result = false;
		}
		if(email.isEmpty()){
			errorMsg += "Wrong <strong>Email</strong> parameter.<br />";
			result = false;
		}
		if(!namespace.isEmpty() && !namespace.equals("mu") && login.isEmpty()){
			errorMsg += "Wrong <strong>login-namespace</strong> parameter.<br />";
			result = false;
		}
		if((certDN.isEmpty() && !caCertDN.isEmpty()) || (!certDN.isEmpty() && caCertDN.isEmpty())){
			errorMsg += "Wrong <strong>Cert / CA Cert</strong> parameter.<br />";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Error creating service member", errorMsg);
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

		if (!namespace.isEmpty() && !namespace.equals("mu")) {
			attrs.put("urn:perun:user:attribute-def:def:login-namespace:"+namespace, new JSONString(login));
		}

		attrs.put("urn:perun:member:attribute-def:def:mail", new JSONString(email));

		// create new form of candidate
		JSONObject newCandidate = new JSONObject();
		newCandidate.put("attributes", attrs);
		newCandidate.put("additionalUserExtSources", null);
		newCandidate.put("id", null);
		newCandidate.put("middleName", null);
		newCandidate.put("titleAfter", null);
		newCandidate.put("titleBefore", null);

		if (Objects.equals(specificUserType, "SERVICE")) {
			newCandidate.put("firstName", new JSONString(""));
			newCandidate.put("lastName", new JSONString(name));
		} else if (Objects.equals(specificUserType, "SPONSORED")) {
			Map<String,String> parsedName = Utils.parseCommonName(name);
			String firstName = parsedName.get("firstName");
			String lastName = parsedName.get("lastName");
			String titleAfter = parsedName.get("titleAfter");
			String titleBefore = parsedName.get("titleBefore");
			newCandidate.put("firstName", (firstName != null) ? new JSONString(firstName) : null);
			newCandidate.put("lastName", (lastName != null) ? new JSONString(lastName) : null);
			newCandidate.put("titleAfter", (titleAfter != null) ? new JSONString(titleAfter) : null);
			newCandidate.put("titleBefore", (titleBefore != null) ? new JSONString(titleBefore) : null);
		}

		if (!certDN.isEmpty() && !caCertDN.isEmpty()) {

			JSONObject userExtSource = new JSONObject();
			userExtSource.put("id", null);
			userExtSource.put("login", new JSONString(certDN));
			// we do not trust manually added certs
			userExtSource.put("loa", new JSONNumber(0));

			// create ext source
			JSONObject extSource = new JSONObject();
			extSource.put("id", null);
			extSource.put("name", new JSONString(caCertDN));
			extSource.put("type", new JSONString("cz.metacentrum.perun.core.impl.ExtSourceX509"));

			userExtSource.put("extSource", extSource);
			newCandidate.put("userExtSource", userExtSource);

		} else {
			newCandidate.put("userExtSource", null);
		}

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
			JSONValue sponsored = user.get("sponsoredUser");

			JSONObject newUser = new JSONObject();
			newUser.put("id", id);
			newUser.put("firstName", firstName);
			newUser.put("lastName", lastName);
			newUser.put("middleName", middleName);
			newUser.put("titleAfter", titleAfter);
			newUser.put("titleBefore", titleBefore);
			newUser.put("serviceUser", service);
			newUser.put("sponsoredUser", sponsored);

			array.set(i, newUser);

		}

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("vo", selectedVoId);
		jsonQuery.put("candidate", newCandidate);
		jsonQuery.put("specificUserOwners", array);
		jsonQuery.put("specificUserType", new JSONString(specificUserType));

		return jsonQuery;

	}

}
