package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebConstants;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class with a client for JSON calls. For each call a new instance must be created.
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class JsonClient  {

	// Request number
	private static int jsonRequestId = 0;

	// Module called when operation is finished
	private JsonCallback module;

	// default timeout for JSON calls
	public final static int DEFAULT_TIMEOUT = PerunWebConstants.INSTANCE.jsonTimeout();

	// the timeout
	private int timeout;
	
	// important - cannot be deleted
	private boolean important = false;

	private boolean cacheEnabled = false;

	/**
	 * Unique request key
	 */
	private String cacheKey;
	
	static private Map<String, JavaScriptObject> cache = new HashMap<String, JavaScriptObject>();
	
	// loading widget
	static private FlexTable loadingWidget = new FlexTable();
	
	// map with active requests info
	// KEY = requestId, VALUE = JsonClientRequest
	static private Map<Integer, JsonClientRequest> activeRequestsMap = new HashMap<Integer, JsonClientRequest>();
	
	// dialogbox
	static private DialogBox activeRequestsDialog = new DialogBox(true);
	
	// silent - doesn't display the loading image - use only if you know what are you doing
	private boolean silent = false;

	/**
	 * URL prefix of RPC
	 */
	private String urlPrefix;

	// whether should not trigger the alert box if error
	private boolean hidden = false;
	
	/**
	 * Is callback silent ? (doesn't show processing)
     *
	 * @return TRUE = silent / FALSE = normal
	 */
	public boolean isSilent() {
		return silent;
	}

	/**
	 * Whether should the json client show "Loading" or not
     *
	 * @param silent TRUE = do not show call as processing / FALSE = normal
	 */
	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	/**
	 * Sets the timeout - in milliseconds.
	 * The core constructor
	 * 
	 * @param timeout
	 */
	public JsonClient(int timeout) {
		this.timeout = timeout;
		
		// get json URL
		this.urlPrefix = PerunWebSession.getInstance().getRpcUrl();
	}
	
	/**
	 * Default instance of the client
	 */
	public JsonClient() {
		this(DEFAULT_TIMEOUT);
	}
	
	/**
	 * This action won't be deleted on page change.
	 * @param important
	 */
	public JsonClient(boolean important) {
		this();
		this.important = important;
	}

	/**
	 * This action won't be deleted on page change.
	 * @param important
	 * @param timeout
	 */
	public JsonClient(boolean important, int timeout) {
		this(timeout);
		this.important = important;
	}

	/**
	 * Builds a new JsonClient with custom PREFIX URL - other than Perun RPC
	 * @param url
	 */
	public JsonClient(String url)
	{
		this();
		this.urlPrefix = url;
	}

	/**
	 * Calls a URL with specified parameters.
	 * When operation finishes, calles the JsonCallback
	 * 
	 * @param url URL after the prefix.
	 * @param parameters Parameters to send. 
	 * @param m JsonCallback, which is called after it finishes.
     *
     * @return ID of request
	 */
	public int retrieveData(String url, String parameters, JsonCallback m){

		this.module = m;
		cacheKey = url + "?" + parameters;
		
		if(cacheEnabled)
		{
			if(cache.containsKey(cacheKey)){
				JavaScriptObject jso = cache.get(cacheKey);
				handleJsonResponseFromCache(jso);
				return ++jsonRequestId;
			}
		}	
		
		String rpcUrl = URL.encode(urlPrefix + url) + "?callback=";
		this.module.onLoadingStart();

		// if new loading
		if(activeRequestsMap.size() == 0 && !silent)
		{
			loadingStarted();
		}


		// params - cache
		parameters = "cache=" + cacheEnabled + "&" + parameters;
		
		// new request ID
		jsonRequestId++;
		getJson(jsonRequestId, rpcUrl, this, parameters, timeout, important);

		if(!silent){
			// sets the active request
			JsonClientRequest request = new JsonClientRequest(jsonRequestId, url, parameters, important, timeout);
			activeRequestsMap.put(jsonRequestId, request);
			
			// refreshes loading details
			refreshLoadingDetails();
		}
		
		return jsonRequestId;
	}


	/**
	 * Calls a url
	 * When operation finishes, calles the JsonCallback
	 * 
	 * @param url URL after the prefix.
	 * @param m JsonCallback, which is called after it finishes.
     *
     * @return ID of request
	 */
	public int retrieveData(String url, JsonCallback m)
	{
		return this.retrieveData(url, "", m);
	}


	/**
	 * Makes a call to remote server
	 * 
	 * @param requestId Number of the request.
	 * @param url Requested URL.
	 * @param handler JsonHandler - this.
	 * @param params Parameters
	 * @param timeout Timeout
	 */
	private native static void getJson(int requestId, String url, JsonClient handler, String params, int timeout, boolean important) /*-{
			
			var callback = "callback" + requestId;
			var parameters = "&" + params;

			// [2] Define the callback function on the window object.
			window[callback] = function(jso) {
				
				// if not already done - expired?
				if (window[callback + "done"]) {
					return;
				}
				// [3]
				// basic type or not?
				try
  				{
	  				if ((typeof jso) != "object")
					{
						jso = {value: jso};
					}
				}catch(err){
					
				}
				window[callback + "done"] = true;
				handler.@cz.metacentrum.perun.webgui.json.JsonClient::handleJsonResponse(ILcom/google/gwt/core/client/JavaScriptObject;)(requestId, jso);
			}
			
			// [1] Create a script element.
			var script = document.createElement("script");
			script.setAttribute("src", url + callback + parameters);
			script.setAttribute("type", "text/javascript");
			
			// [4] JSON download has a timeout.
			setTimeout(
					function() {
						if (!window[callback + "done"]) {
							handler.@cz.metacentrum.perun.webgui.json.JsonClient::handleJsonResponse(ILcom/google/gwt/core/client/JavaScriptObject;)(requestId, null);
						}
					
						
						// [5] Cleanup. Remove script and callback elements.
						document.body.removeChild(script);
						delete window[callback];
						delete window[callback + "done"];
					}, timeout);

			// [6] Attach the script element to the document body.
			document.body.appendChild(script);
	}-*/;


	/**
	 * Removes all running requests, which are NOT IMPORTANT (auth, ...)
	 */
	public static void removeRunningRequests()
	{
		Set<Integer> requestsToRemove = new HashSet<Integer>();
		
		
		for(Map.Entry<Integer, JsonClientRequest> entry : activeRequestsMap.entrySet())
		{
			JsonClientRequest request = entry.getValue();
			if(!request.isImportant()){
				requestsToRemove.add(request.getId());
			}
		}
		
		
		for(int request : requestsToRemove)
		{
			removeRunningRequest(request);
		}
	}
	
	
	/**
	 * Removes one running request
	 * @param requestId
	 */
	public static boolean removeRunningRequest(int requestId){
		
		// if the request doesn't exist, return false
		if(!activeRequestsMap.containsKey(requestId)){
			return false;
		}
		
		// remove request		
		removeRunningRequestNative(requestId);
		activeRequestsMap.remove(requestId);
		return true;
	}

	/**
	 * Removes the request in javascript
	 * @param requestId
	 */
	private static native void removeRunningRequestNative(int requestId) /*-{
		var callback = "callback" + requestId;
		window[callback + "done"] = true;		
	}-*/;

	/**
	 * Handle the response to the request for stock data from a remote server.
	 * @param jso JavaScriptObject to be processed. If null, the error is called.
	 */
	public void handleJsonResponseFromCache(final JavaScriptObject jso)
	{
		
		// if an ERROR
		if (jso == null)
		{
			this.module.onError(null);
		}
		else
		{
			// try to recognize an error
			PerunError error = jso.cast();
			if(!error.getErrorId().equals("")){
				// if error, alert it
				if(!hidden)
				{
                    JsonErrorHandler.alertBox(error, cacheKey, null);
				}
				module.onError(error);
				
			}else{ // no error
				// OK
				module.onFinished(jso);
				
				// fire the resize event
				UiElements.runResizeCommands();
			}
		}
		
	}
	
	/**
	 * Handle the response to the request for stock data from a remote server.
	 * @param requestId ID of the called request
	 * @param jso JavaScriptObject to be processed. If null, the error is called.
	 */
	public void handleJsonResponse(int requestId, final JavaScriptObject jso)
	{
		handleJsonResponseFromCache(jso);
		
		// remove active requests
		activeRequestsMap.remove(requestId);
		if(activeRequestsMap.size() == 0){
			loadingFinished();
		}
		
		if(cacheEnabled){
			cache.put(cacheKey, jso);
		}
		
		refreshLoadingDetails();
	}

	
	/**
	 * Returns the loading widget
	 * @return
	 */
	static public FlexTable getLoadingWidget(){
		return loadingWidget;
	}
	
	/**
	 * Displays the loading widget
	 */
	static private void loadingStarted(){
		
		Anchor text = new Anchor("Loading");
		Image image = new Image(AjaxLoaderImage.SMALL_IMAGE_URL);
		image.setTitle("Click to view pending calls");
		
		loadingWidget.setWidget(0, 0, image);
		
		text.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(activeRequestsMap.size() == 0)
				{
					loadingFinished();
					return;
				}
				
				activeRequestsDialog.setModal(false);
				activeRequestsDialog.setWidth("400px");
				activeRequestsDialog.setGlassEnabled(true);
				activeRequestsDialog.center();
				activeRequestsDialog.show();
				
				refreshLoadingDetails();
			}
		});
		loadingWidget.setWidget(0, 1, text);
	}
	
	/**
	 * Displays no active requests
	 */
	static private void loadingFinished(){

        Label text = new Label("No active requests");
		Image image = new Image(SmallIcons.INSTANCE.acceptIcon());
		image.setTitle("No active requests");

        loadingWidget.setWidget(0, 0, image);
        loadingWidget.setWidget(0, 1, text);
		
		activeRequestsDialog.hide();
	}
	
	/**
	 * Refreshes loading details
	 */
	static private void refreshLoadingDetails()
	{
		// only if dialog visible
		if(!activeRequestsDialog.isShowing()){
			return;
		}
		
		// if requests count != 0
		if(activeRequestsMap.size()==0)
		{
			return;
		}
		
		VerticalPanel vp = new VerticalPanel();
		vp.clear();
		activeRequestsDialog.setText(activeRequestsMap.size() + " active requests");
		for(Map.Entry<Integer, JsonClientRequest> entry : activeRequestsMap.entrySet())
		{
			vp.add(new Label(entry.getValue().toString()));
		}
		activeRequestsDialog.setWidget(vp);
		activeRequestsDialog.center();
	}

    /**
     * Enable caching of call results
     *
     * @param enable TRUE = cache enabled / FALSE = cache disabled
     */
	public void setCacheEnabled(boolean enable) {
		this.cacheEnabled = enable;
	}

    /**
     * Set callback as hidden, meaning DO NOT SHOW ERROR if occurs
     *
     * @param hidden TRUE = hidden / FALSE = normal
     */
	public void setHidden(boolean hidden) {
		this.hidden  = hidden;
	}

}
