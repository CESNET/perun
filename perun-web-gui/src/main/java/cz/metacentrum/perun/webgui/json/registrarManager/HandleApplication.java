package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.Confirm;

import java.util.ArrayList;

/**
 * Common request, which handles applications (VERIFY, REJECT, APPROVE)
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class HandleApplication {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL_VERIFY = "registrarManager/verifyApplication";
	final String JSON_URL_APPROVE = "registrarManager/approveApplication";
	final String JSON_URL_REJECT = "registrarManager/rejectApplication";
    final String JSON_URL_DELETE = "registrarManager/deleteApplication";


    // custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	
	// application form
	private int appId = 0;
	
	private String reason;

	/**
	 * Creates a new request
	 */
	public HandleApplication() {}

	/**
	 * Creates a new request with custom events
     *
	 * @param events Custom events
	 */
	public HandleApplication(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Verify application
	 *
	 * @param appId
	 */
	public void verifyApplication(int appId)
	{
		
		this.appId = appId;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Verifing application failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Application verified.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL_VERIFY, prepareJSONObject());
		
		
	}
	
	/**
	 * Reject application
	 *
	 * @param appId
	 * @param reason
	 */
	public void rejectApplication(int appId, String reason)
	{
		
		this.appId = appId;
		this.reason = reason;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Rejecting application failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Application rejected.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL_REJECT, prepareJSONObject());
		
		
	}
	
	/**
	 * Approve application
	 *
	 * @param appId
	 */
	public void approveApplication(int appId)
	{
		
		this.appId = appId;

		// test arguments
		if(!this.testCreating()){
			return;
		}

        // new events
        final JsonCallbackEvents newEvents = new JsonCallbackEvents(){
            public void onError(PerunError error) {
                session.getUiElements().setLogErrorText("Approving application failed.");
                events.onError(error);
            };

            public void onFinished(JavaScriptObject jso) {
                session.getUiElements().setLogSuccessText("Application approved.");
                events.onFinished(jso);
            };

            public void onLoadingStart() {
                events.onLoadingStart();
            };
        };

        // check for similar users before approving

        JSONObject jso = new JSONObject();
        jso.put("appId", new JSONNumber(appId));

        JsonPostClient checkJspc = new JsonPostClient(new JsonCallbackEvents(){
            public void onError(PerunError error) {
                session.getUiElements().setLogErrorText("Approving application failed.");
                events.onError(error);
            };

            public void onFinished(JavaScriptObject jso) {

                ArrayList<User> users = JsonUtils.jsoAsList(jso);
                if (users != null && !users.isEmpty()) {

                    FlexTable ft = new FlexTable();
                    ft.setWidth("400px");
                    ft.setHTML(0,0, "<p>Please check, if new applicant isn't the same person as listed below. " +
                            "If so, please join his/hers identities before approving this application. If unsure, please contact <a href=\"mailto:perun@cesnet.cz\">support</a> to help you.");

                    for (int i=0; i<users.size(); i++) {
                        if (!users.get(i).isServiceUser()) {
                            ft.setHTML(i+1, 0, " - "+users.get(i).getFullNameWithTitles() + " (ID: "+users.get(i).getId()+")");
                        }
                    }

                    ft.setHTML(ft.getRowCount(), 0, "<p><strong>Do you wish to approve application anyway ?</strong>");

                    Confirm c = new Confirm("Similar users found!", ft, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {

                            // ok approve sending data
                            JsonPostClient jspc = new JsonPostClient(newEvents);
                            jspc.sendData(JSON_URL_APPROVE, prepareJSONObject());

                        }
                    }, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            events.onFinished(null);
                        }
                    }, true);
                    c.setNonScrollable(true);
                    c.show();

                } else {

                    // ok approve sending data
                    JsonPostClient jspc = new JsonPostClient(newEvents);
                    jspc.sendData(JSON_URL_APPROVE, prepareJSONObject());

                }

            };

            public void onLoadingStart() {
                events.onLoadingStart();
            };
        });
        checkJspc.sendData("registrarManager/checkForSimilarUsers", jso);

	}

	private boolean testCreating() {
		
		boolean result = true;
		String errorMsg = "";

		if(appId == 0){
			errorMsg += "AppId can't be 0.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
		
	}

	/**
	 * Prepares a JSON object.
	 * @return JSONObject - the whole query
	 */
	private JSONObject prepareJSONObject()
	{
		// query
		JSONObject query = new JSONObject();
		query.put("id", new JSONNumber(appId));
		
		if (reason != null) {
			query.put("reason", new JSONString(reason));
		}
		
		return query;
	}

    /**
     * Delete application
     *
     * @param appId
     */
    public void deleteApplication(int appId)
    {

        this.appId = appId;

        // test arguments
        if(!this.testCreating()){
            return;
        }

        // new events
        JsonCallbackEvents newEvents = new JsonCallbackEvents(){
            public void onError(PerunError error) {
                session.getUiElements().setLogErrorText("Deleting application failed.");
                events.onError(error);
            };

            public void onFinished(JavaScriptObject jso) {
                session.getUiElements().setLogSuccessText("Application deleted.");
                events.onFinished(jso);
            };

            public void onLoadingStart() {
                events.onLoadingStart();
            };
        };

        // sending data
        JsonPostClient jspc = new JsonPostClient(newEvents);
        jspc.sendData(JSON_URL_DELETE, prepareJSONObject());


    }

}