package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;

/**
 * Ajax query which disconnects service user and user
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class RemoveServiceUserOwner {

    // web session
    private PerunWebSession session = PerunWebSession.getInstance();
    // URL to call
    final String JSON_URL = "usersManager/removeServiceUserOwner";
    // external events
    private JsonCallbackEvents events = new JsonCallbackEvents();
    // local variables for entity to send
    private User user;
    private User serviceUser;

    /**
     * Creates a new request
     */
    public RemoveServiceUserOwner() {}

    /**
     * Creates a new request with custom events passed from tab or page
     *
     * @param events custom events
     */
    public RemoveServiceUserOwner(final JsonCallbackEvents events) {
        this.events = events;
    }

    /**
     * Removes connection between user and service user
     *
     * @param user
     * @param serviceUser
     */
    public void removeServiceUser(final User user, final User serviceUser) {

        this.user = user;
        this.serviceUser = serviceUser;

        // test arguments
        if(!this.testRemoving()){
            return;
        }

        // new events
        JsonCallbackEvents newEvents = new JsonCallbackEvents(){
            public void onError(PerunError error) {
                session.getUiElements().setLogErrorText("Removing "+serviceUser.getFullName()+" from user: " + user.getFullName() + " failed.");
                events.onError(error); // custom events
            };

            public void onFinished(JavaScriptObject jso) {
                session.getUiElements().setLogSuccessText("Service identity: "+serviceUser.getFullName()+" removed from user: " + user.getFullName());
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
    private boolean testRemoving() {

        boolean result = true;
        String errorMsg = "";

        if(user.isServiceUser()){
            errorMsg += "Can't disconnect two service identities.</br>";
            result = false;
        }
        if(!serviceUser.isServiceUser()){
            errorMsg += "Can't disconnect two 'Person' like users.";
            result = false;
        }

        if(errorMsg.length()>0){
            UiElements.generateAlert("Error while disconnecting identities", errorMsg);
        }

        return result;
    }

    /**
     * Prepares a JSON object
     *
     * @return JSONObject the whole query
     */
    private JSONObject prepareJSONObject() {
        // create whole JSON query
        JSONObject jsonQuery = new JSONObject();
        jsonQuery.put("user", new JSONNumber(user.getId()));
        jsonQuery.put("serviceUser", new JSONNumber(serviceUser.getId()));
        return jsonQuery;
    }

}