package cz.metacentrum.perun.webgui.json.vosManager;


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
 * Request, which creates a new VO.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CreateVo {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// vo name
	private String name = "";

	// vo short name
	private String shortName = "";

	// URL to call
	final String JSON_URL = "vosManager/createVo";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();


	/**
	 * Creates a new request
	 */
	public CreateVo() {}

	/**
	 * Creates a new request with custom events
	 * @param events Custom events
	 */
	public CreateVo(JsonCallbackEvents events) {
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

		if(shortName.length() == 0){
			errorMsg += "You must fill in the parameter <strong>Short name</strong>.<br />";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while creating VO", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Attempts to create a new VO, it first tests the values and then submits them.
	 *
	 * @param name		VO name
	 * @param shortName VO short name
	 */
	public void createVo(final String name, final String shortName)
	{
		this.name = name;
		this.shortName = shortName;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating virtual organization " + name + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Virtual organization " + name + " created.");
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
		JSONObject vo = new JSONObject();
		vo.put("name", new JSONString(name));
		vo.put("shortName", new JSONString(shortName));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("vo", vo);
		return jsonQuery;
	}

}
