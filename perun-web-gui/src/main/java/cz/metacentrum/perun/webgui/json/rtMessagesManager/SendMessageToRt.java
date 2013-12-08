package cz.metacentrum.perun.webgui.json.rtMessagesManager;


import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebConstants;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RTMessage;

/**
 * Sending messages to RT
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id: c69bb36a4235fae205475e4c12f55a79ea65c628 $
 */
public class SendMessageToRt {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "rtMessagesManager/sentMessageToRT";
	
	// default queue
	static public final String DEFAULT_QUEUE = ((PerunWebConstants) GWT.create(PerunWebConstants.class)).defaultRtQueue();
	
	static public final String SIGNATURE = "\n\n---------------------\nSent via Perun GUI";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public SendMessageToRt() {}

	/**
	 * Creates a new request with custom events
	 * @param events external events
	 */
	public SendMessageToRt(JsonCallbackEvents events) {
		this.events = events;
	}
	
	/**
	 * Sends a new message to RT with the default member and default RT queue
	 *  
	 * @param subject
	 * @param text
	 * @return True if message sent 
	 */
	public boolean sendMessage(final String subject, final String text)
	{
		return sendMessage("", subject, text, 0);
	}
	
	/**
	 * Sends a new message to RT with the default member
	 *  
	 * @param queue
	 * @param subject
	 * @param text
	 * @return True if message sent 
	 */
	public boolean sendMessage(final String queue, final String subject, final String text)
	{
		return sendMessage(queue, subject, text, 0);
	}
	
	/**
	 * Sends a new message to RT with the default queue
	 *  
	 * @param subject
	 * @param text
	 * @param voId
	 * @return True if message sent 
	 */
	public boolean sendMessage(final String subject, final String text, int voId)
	{
		return sendMessage("", subject, text, voId);
	}
	
	/**
	 * Sends a new message to RT
	 * If VO ID specified, the e-mail will contain the member which corresponds with the current user and VO
	 *  
	 * @param queue
	 * @param subject
	 * @param text
	 * @param voId
	 * @return True if message sent 
	 */
	public boolean sendMessage(final String queue, final String subject, String text, final int voId)
	{
		// append signature
		text += SIGNATURE;
		
		// appended space after each new line ("\n" to "\n ")
		text = text.replace("\n", "\n ");
		
		// encode URL
		//text = URL.encode(text);
		
		// json object
		JSONObject jsonQuery = prepareJSONObject(queue, subject, text, voId);

		// local events
		JsonCallbackEvents sendEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Sending RT ticket " + subject + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				
				RTMessage msg = jso.cast();
				
				session.getUiElements().setLogSuccessText("RT ticket " + subject + " send. Responses will be sent to the e-mail address: " + msg.getMemberPreferredEmail());
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};

		};

		// create request
		JsonPostClient request = new JsonPostClient(sendEvents);
        request.setHidden(true);
		request.sendData(JSON_URL, jsonQuery);

		return true;
	}

	/**
	 * Prepares a JSON object
	 * @param queue 
	 * @param subject
	 * @param text 
	 * @param voId 
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject(String queue, String subject, String text, int voId)
	{
		JSONObject rtMsg = new JSONObject();
		
		if(!queue.isEmpty())
		{
			rtMsg.put("queue", new JSONString(queue));
		}
		rtMsg.put("subject", new JSONString(subject));
		
		
		rtMsg.put("text", new JSONString(text));
		
		if(voId != 0)
		{
			rtMsg.put("voId", new JSONNumber(voId));
		}

		return rtMsg;
	}

}