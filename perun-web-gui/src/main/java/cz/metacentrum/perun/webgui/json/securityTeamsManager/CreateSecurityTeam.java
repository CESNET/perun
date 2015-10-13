package cz.metacentrum.perun.webgui.json.securityTeamsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Request, which creates a new Security Team
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class CreateSecurityTeam {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// vo name
	private String name = "";

	// vo short name
	private String description = "";

	// URL to call
	final String JSON_URL = "securityTeamsManager/createSecurityTeam";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();


	/**
	 * Creates a new request
	 */
	public CreateSecurityTeam() {}

	/**
	 * Creates a new request with custom events
	 * @param events Custom events
	 */
	public CreateSecurityTeam(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return
	 */
	private boolean testCreating()
	{
		boolean result = true;
		String errorMsg = "";

		if(name.length() == 0){
			errorMsg += "You must fill in the parameter <strong>Name</strong>.<br />";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while creating SecurityTeam", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Attempts to create a new SecurityTeam, it first tests the values and then submits them.
	 *
	 * @param name		  SecurityTeam name
	 * @param description SecurityTeam description
	 */
	public void createSecurityTeam(final String name, final String description)
	{
		this.name = name;
		this.description = description;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating security team " + name + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Security team " + name + " created.");
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
	 * Prepares a JSON object.
	 * @return JSONObject - the whole query
	 */
	private JSONObject prepareJSONObject()
	{
		// vo
		JSONObject team = new JSONObject();
		team.put("name", new JSONString(name));
		team.put("description", new JSONString(description));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("securityTeam", team);
		return jsonQuery;
	}

}
