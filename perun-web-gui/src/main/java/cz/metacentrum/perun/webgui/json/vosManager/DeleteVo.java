package cz.metacentrum.perun.webgui.json.vosManager;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Ajax query which deletes VO from Perun
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class DeleteVo {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "vosManager/deleteVo";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private int voId = 0;
	private boolean force = false;

	/**
	 * Creates a new request
	 *
	 */
	public DeleteVo() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public DeleteVo(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testDeleting()
	{
		boolean result = true;
		String errorMsg = "";

		if(voId == 0){
			errorMsg += "Wrong parameter <strong>VO ID</strong>.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while deleting VO", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Attempts to delete VO, it first tests the values and then submits them.
	 *
	 * @param voId ID of VO to be deleted
	 * @param force true for force delete
	 */
	public void deleteVo(final int voId,final boolean force)
	{

		this.voId = voId;
		this.force = force;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting VO: " + voId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Virtual organization " + voId + " deleted.");
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
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject()
	{
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("vo", new JSONNumber(voId));
		if (force) {
			jsonQuery.put("force", null);
		}
		return jsonQuery;
	}

}
