package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.ApplicationMail;
import cz.metacentrum.perun.webgui.model.MailText;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Request, which updates application email
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class UpdateApplicationMail {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/updateApplicationMail";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// data
	private ApplicationMail appMail;

	/**
	 * Creates a new request
	 */
	public UpdateApplicationMail() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public UpdateApplicationMail(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Updates ApplicationMail
	 *
	 * @param appMail
	 */
	public void updateMail(ApplicationMail appMail) {

		this.appMail = appMail;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating email failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Email updated.");
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

	private boolean testCreating() {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Prepares a JSON object.
	 * @return JSONObject - the whole query
	 */
	private JSONObject prepareJSONObject() {



		JSONObject mail = new JSONObject();

		// update send
		mail.put("send", JSONBoolean.getInstance(appMail.isSend()));

		JSONObject mailTexts = new JSONObject();

		// update texts
		MailText mt = appMail.getMessage("en");
		mailTexts.put("en", new JSONObject(mt));

		if (!Utils.getNativeLanguage().isEmpty()) {
			MailText mt2 = appMail.getMessage(Utils.getNativeLanguage().get(0));
			mailTexts.put(Utils.getNativeLanguage().get(0), new JSONObject(mt2));
		}

		mail.put("message", mailTexts);

		// sending other values just for sure
		mail.put("id", new JSONNumber(appMail.getId()));
		mail.put("appType", new JSONString(appMail.getAppType()));
		mail.put("mailType", new JSONString(appMail.getMailType()));
		mail.put("formId", new JSONNumber(appMail.getFormId()));


		JSONObject request = new JSONObject();
		request.put("mail", mail);

		return request;

	}

}
