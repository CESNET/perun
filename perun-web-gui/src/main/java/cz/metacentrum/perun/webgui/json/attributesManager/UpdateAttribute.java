package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.AttributeDefinition;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Ajax query which updates attribute definition (only description is updated)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class UpdateAttribute {

    // web session
    private PerunWebSession session = PerunWebSession.getInstance();

    // URL to call
    final String JSON_URL = "attributesManager/updateAttributeDefinition";

    // external events
    private JsonCallbackEvents events = new JsonCallbackEvents();

    private AttributeDefinition attributeDefinition;

    /**
     * Creates attributeDefinition new request
     */
    public UpdateAttribute() {}

    /**
     * Creates attributeDefinition new request with custom events passed from tab or page
     * @param events external events
     */
    public UpdateAttribute(final JsonCallbackEvents events) {
        this.events = events;
    }

    /**
     * Update attribute definition in DB (only description)
     *
     * @param a attribute definition to update to
     */
    public void updateAttribute(AttributeDefinition a) {

        this.attributeDefinition = a;

        // test arguments
        if(!this.testUpdating()){
            return;
        }

        // new events
        JsonCallbackEvents newEvents = new JsonCallbackEvents(){
            public void onError(PerunError error) {
                session.getUiElements().setLogErrorText("Updating attribute definition: " + attributeDefinition.getDisplayName() + " failed.");
                events.onError(error);
            };

            public void onFinished(JavaScriptObject jso) {
                session.getUiElements().setLogSuccessText("Attribute definition: "+ attributeDefinition.getDisplayName() +" successfully updated.");
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
    private boolean testUpdating()
    {
        boolean result = true;
        String errorMsg = "";

        if(attributeDefinition == null){
            errorMsg += "Attribute to update can't be null.</ br>";
            result = false;
        }

        if(attributeDefinition.getId() == 0){
            errorMsg += "Wrong ID of attribute definition.</ br>";
            result = false;
        }

        if(errorMsg.length()>0){
            Confirm c = new Confirm("Error while creating attribute", new HTML(errorMsg), true);
            c.show();
        }

        return result;
    }

    /**
     * Prepares attributeDefinition JSON object.
     *
     * @return JSONObject - the whole query
     */
    private JSONObject prepareJSONObject() {

        JSONObject original = new JSONObject(attributeDefinition);

        JSONObject attributeDef = new JSONObject();
        attributeDef.put("id", original.get("id"));
        attributeDef.put("friendlyName", original.get("friendlyName"));
        attributeDef.put("description", original.get("description"));
        attributeDef.put("namespace", original.get("namespace"));
        attributeDef.put("type", original.get("type"));
        attributeDef.put("displayName", original.get("displayName"));

        // create whole JSON query
        JSONObject jsonQuery = new JSONObject();
        jsonQuery.put("attributeDefinition", attributeDef);
        return jsonQuery;

    }

}