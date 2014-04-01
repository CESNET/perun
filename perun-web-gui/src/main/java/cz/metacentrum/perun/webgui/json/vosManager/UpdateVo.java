package cz.metacentrum.perun.webgui.json.vosManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Request, which updates VO.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UpdateVo {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// vo
	private VirtualOrganization vo;

	// URL to call
	final String JSON_URL = "vosManager/updateVo";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();


	/**
	 * Creates a new request
	 */
	public UpdateVo() {}

	/**
	 * Creates a new request with custom events
	 * @param events Custom events
	 */
	public UpdateVo(JsonCallbackEvents events) {
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

		if(vo == null){
			errorMsg += "Can't update NULL VO.<br />";
			result = false;
		}

		if(vo.getName().length() == 0){
			errorMsg += "VO must have parameter <strong>Name</strong>.<br />";
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
	 * @param vo Virtual organization to update
	 */
	public void updateVo(final VirtualOrganization vo) {

		this.vo = vo;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating virtual organization " + vo.getName() + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Virtual organization " + vo.getName() + " updated.");
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
		JSONObject newVo = new JSONObject();
		newVo.put("id", new JSONNumber(vo.getId()));
		newVo.put("name", new JSONString(vo.getName()));
		newVo.put("shortName", new JSONString(vo.getShortName()));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("vo", newVo);
		return jsonQuery;
	}

}
