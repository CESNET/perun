package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.FlexTable;
import cz.metacentrum.perun.webgui.client.PerunWebConstants;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.Confirm;

import java.util.ArrayList;

/**
 * Common request, which handles applications (VERIFY, REJECT, APPROVE, DELETE)
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
	public void verifyApplication(int appId) {
		
		this.appId = appId;

		// test arguments
		if(!this.testApplication()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			@Override
            public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Verifing application failed.");
				events.onError(error);
			};
            @Override
			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Application verified.");
				events.onFinished(jso);
			};
            @Override
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
	public void rejectApplication(int appId, String reason) {
		
		this.appId = appId;
		this.reason = reason;

		// test arguments
		if(!this.testApplication()){
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
	public void approveApplication(int appId) {
		
		this.appId = appId;

		// test arguments
		if(!this.testApplication()){
			return;
		}

        // new events
        final JsonCallbackEvents newEvents = new JsonCallbackEvents(){
            @Override
            public void onError(PerunError error) {
                session.getUiElements().setLogErrorText("Approving application failed.");
                events.onError(error);
            };
            @Override
            public void onFinished(JavaScriptObject jso) {
                session.getUiElements().setLogSuccessText("Application approved.");
                events.onFinished(jso);
            };
            @Override
            public void onLoadingStart() {
                events.onLoadingStart();
            };
        };

        // check for similar users before approving

        JSONObject jso = new JSONObject();
        jso.put("appId", new JSONNumber(appId));

        JsonPostClient checkJspc = new JsonPostClient(new JsonCallbackEvents(){
            @Override
            public void onError(PerunError error) {
                session.getUiElements().setLogErrorText("Approving application failed.");
                events.onError(error);
            };
            @Override
            public void onFinished(JavaScriptObject jso) {

                ArrayList<User> users = JsonUtils.jsoAsList(jso);
                if (users != null && !users.isEmpty()) {

                    FlexTable ft = new FlexTable();
                    ft.setWidth("400px");
                    ft.setHTML(0, 0, "<p><strong>Following user(s) with similar name as new applicant were found in Perun system:");

                    for (int i=0; i<users.size(); i++) {
                        if (!users.get(i).isServiceUser()) {
                            ft.setHTML(i+1, 0, users.get(i).getFullNameWithTitles() + " (User ID: "+users.get(i).getId()+")");
                        }
                    }

                    ft.setHTML(ft.getRowCount(), 0,  "<p>Please consider contacting new applicant with question, if he/she is already member of any other VO in Perun." +
                            "<ul><li>If YES, ask user to join his identities before application approval at <a href=\""+Utils.getIdentityConsolidatorLink(false)+"\" target=\"_blank\">identity consolidator</a>"+
                    "</li><li>If NO, you can approve this application. " +
                            "</li><li>If UNSURE, contact <a href=\"mailto:"+ Utils.perunReportEmailAddress()+"\">support</a> to help you.</li></ul>");

                    ft.setHTML(ft.getRowCount(), 0, "<strong>Do you wish to approve this application anyway ?</strong>");

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
            @Override
            public void onLoadingStart() {
                events.onLoadingStart();
            };
        });
        checkJspc.sendData("registrarManager/checkForSimilarUsers", jso);

	}

    /**
     * Delete application
     *
     * @param appId
     */
    public void deleteApplication(int appId) {

        this.appId = appId;

        // test arguments
        if(!this.testApplication()){
            return;
        }

        // new events
        JsonCallbackEvents newEvents = new JsonCallbackEvents(){
            @Override
            public void onError(PerunError error) {
                session.getUiElements().setLogErrorText("Deleting application failed.");
                events.onError(error);
            };
            @Override
            public void onFinished(JavaScriptObject jso) {
                session.getUiElements().setLogSuccessText("Application deleted.");
                events.onFinished(jso);
            };
            @Override
            public void onLoadingStart() {
                events.onLoadingStart();
            };
        };

        // sending data
        JsonPostClient jspc = new JsonPostClient(newEvents);
        jspc.sendData(JSON_URL_DELETE, prepareJSONObject());

    }


    private boolean testApplication() {

        boolean result = true;
        String errorMsg = "";

        if(appId == 0){
            errorMsg += "Application ID can't be 0.";
            result = false;
        }

        if(errorMsg.length()>0){
            UiElements.generateAlert("Parameter error", errorMsg);
        }

        return result;

    }

    /**
     * Prepares a JSON object.
     * @return JSONObject - the whole query
     */
    private JSONObject prepareJSONObject() {
        // query
        JSONObject query = new JSONObject();
        query.put("id", new JSONNumber(appId));

        if (reason != null) {
            query.put("reason", new JSONString(reason));
        }

        return query;
    }

}