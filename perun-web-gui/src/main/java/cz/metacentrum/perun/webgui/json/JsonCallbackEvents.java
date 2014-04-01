package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.CheckBox;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

/**
 * This class is passed to the JSON request and called when an event happen.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class JsonCallbackEvents {

	private static PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Called when request finishes with success.
	 * @param jso
	 */
	public void onFinished(JavaScriptObject jso){
		// do nothing
	}

	/**
	 * Called, when an error occurs/
	 * @param error
	 */
	public void onError(PerunError error){
		// do nothing
	}

	/**
	 * Called, when started
	 */
	public void onLoadingStart(){
		// do nothing
	}

	/**
	 * Creates a new instance of JsonCallbackEvent.
	 * When a callback finishes successfully, the tab is closed
	 *
	 * @param session Perun Web Session
	 * @param tab Tab to be closed
	 * @return JsonCallbackEvents object
	 */
	static public JsonCallbackEvents closeTabEvents(final PerunWebSession session, final TabItem tab)	{

		JsonCallbackEvents closeTabEvents = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso)
			{
				session.getTabManager().closeTab(tab);
			}
		};

		return closeTabEvents;
	}

	/**
	 * Creates a new instance of JsonCallbackEvent.
	 *
	 * When a callbacks starts, the button is disabled
	 * When a callback finishes with error or successfully, the button is enabled
	 *
	 *
	 *
	 * @param button Button to be enabled / disabled
	 * @return JsonCallbackEvents object
	 */
	static public JsonCallbackEvents disableButtonEvents(final CustomButton button)	{
		return disableButtonEvents(button, null);
	}

	/**
	 * Creates a new instance of JsonCallbackEvent.
	 *
	 * When a callbacks starts, the button is disabled
	 * When a callback finishes with error or successfully, the button is enabled
	 *
	 *
	 *
	 *
	 * @param button Button to be enabled / disabled
	 * @param events Events passed to this events
	 * @return JsonCallbackEvents object
	 */
	static public JsonCallbackEvents disableButtonEvents(final CustomButton button, final JsonCallbackEvents events)	{

		JsonCallbackEvents closeTabEvents = new JsonCallbackEvents(){

			public void onLoadingStart(){
				button.setProcessing(true);
				if(events != null){
					events.onLoadingStart();
				}
			}

			public void onError(PerunError err){
				button.setProcessing(false);
				if(events != null){
					events.onError(err);
				}
			}

			public void onFinished(JavaScriptObject jso) {
				button.setProcessing(false);
				if(events != null){
					events.onFinished(jso);
				}
			}
		};

		return closeTabEvents;
	}


	/**
	 * Creates a new instance of JsonCallbackEvent.
	 *
	 * When a callbacks starts, the button is disabled
	 * When a callback finishes with error, the button is enabled
	 * When a callback finishes successfully, the tab is closed
	 *
	 *
	 * @param button Button to be enabled / disabled
	 * @param tab Tab to be closed
	 * @return JsonCallbackEvents object
	 */
	static public JsonCallbackEvents closeTabDisableButtonEvents(final CustomButton button, final TabItem tab)	{
		return closeTabDisableButtonEvents(button, tab, null);
	}


	/**
	 * Creates a new instance of JsonCallbackEvent.
	 *
	 * When a callbacks starts, the button is disabled
	 * When a callback finishes with error, the button is enabled
	 * When a callback finishes successfully, the tab is closed
	 *
	 *
	 * @param button Button to be enabled / disabled
	 * @param tab Tab to be closed
	 * @param events Events inside this event
	 * @return JsonCallbackEvents object
	 */
	static public JsonCallbackEvents closeTabDisableButtonEvents(final CustomButton button, final TabItem tab, final JsonCallbackEvents events)	{

		JsonCallbackEvents closeTabEvents = new JsonCallbackEvents(){

			public void onLoadingStart(){
				button.setProcessing(true);
				if(events != null){
					events.onLoadingStart();
				}
			}

			public void onError(PerunError err){
				button.setProcessing(false);
				if(events != null){
					events.onError(err);
				}
			}

			public void onFinished(JavaScriptObject jso)
			{
				button.setProcessing(false);
				session.getTabManager().closeTab(tab);
				if(events != null){
					events.onFinished(jso);
				}
			}
		};

		return closeTabEvents;
	}

	/**
	 * Creates a new instance of JsonCallbackEvent.
	 * When a callback finishes successfully, the table is reloaded.
	 *
	 * @param request Request to be reloaded
	 * @return refresh table event
	 */
	static public <T extends JavaScriptObject> JsonCallbackEvents refreshTableEvents(final JsonCallbackTable<T> request)	{

		JsonCallbackEvents refreshTableEvents = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				request.clearTable();
				request.retrieveData();
			}
		};
		return refreshTableEvents;
	}

	/**
	 * Merges two JsonCallbackEvents
	 *
	 * @param events1 Events passed to this events
	 * @param events2 Events passed to this events
	 * @return JsonCallbackEvents object
	 */
	static public JsonCallbackEvents mergeEvents(final JsonCallbackEvents events1, final JsonCallbackEvents events2)	{

		return new JsonCallbackEvents(){

			public void onLoadingStart(){
				events1.onLoadingStart();
				events2.onLoadingStart();
			}

			public void onError(PerunError err){
				events1.onError(err);
				events2.onError(err);
			}

			public void onFinished(JavaScriptObject jso)
			{
				events1.onFinished(jso);
				events2.onFinished(jso);
			}
		};
	}

	/**
	 * Return event which:
	 * disable checkbox on events start
	 * enable checkbox on event finish (or error)
	 *
	 * @param checkbox checkbox to handle
	 * @return checkbox disabling event
	 */
	static public JsonCallbackEvents disableCheckboxEvents(final CheckBox checkbox) {

		return new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				checkbox.setEnabled(true);
			}
			@Override
			public void onError(PerunError error) {
				checkbox.setEnabled(true);
			}
			@Override
			public void onLoadingStart() {
				checkbox.setEnabled(false);
			}
		};

	}

	/**
	 * Return event, which will pass all of its content
	 * to another callback's event.
	 *
	 * Used for filling tables with data from different callbacks
	 * than table origins.
	 *
	 * @param callback Callback to pass event results too.
	 * @return content passing events
	 */
	static public JsonCallbackEvents passDataToAnotherCallback(final JsonCallback callback) {

		return new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				callback.onFinished(jso);
			}
			@Override
			public void onError(PerunError error) {
				callback.onError(error);
			}
			@Override
			public void onLoadingStart() {
				callback.onLoadingStart();
			}
		};

	}


}
