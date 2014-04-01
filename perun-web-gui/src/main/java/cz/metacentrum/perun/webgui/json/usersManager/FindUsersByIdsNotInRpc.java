package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Searching for users.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class FindUsersByIdsNotInRpc implements JsonCallback{

	// session
	private PerunWebSession session = PerunWebSession.getInstance();

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// IDs divided by a comma
	private String searchString;

	private int idsCount = 0;
	private int idsFound = 0;

	private JSONArray result = new JSONArray();

	/**
	 * Creates a new request with custom events
	 * @param events
	 * @param searchString IDs divided by a comma
	 */
	public FindUsersByIdsNotInRpc(JsonCallbackEvents events, String searchString) {
		this.events = events;
		this.searchString = searchString;
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData()
	{
		String[] ids = searchString.split(",");
		if(ids.length == 0){
			return;
		}

		idsCount = ids.length;

		onLoadingStart();

		for(String id : ids)
		{
			// trims the whitespace
			id = id.trim();

			try{
				int idint = Integer.parseInt(id);

				GetEntityById req = new GetEntityById(PerunEntity.USER, idint, new JsonCallbackEvents(){

					public void onFinished(JavaScriptObject jso){
						idsFound++;

						// add to result
						int i = result.size();
						result.set(i, new JSONObject(jso));

						isFinished();
					}

					public void onError(PerunError err){
						idsFound++;

						isFinished();
					}

				});
				req.retrieveData();

			}catch(Exception e){
			}
		}
	}

	protected void isFinished()
	{
		if(idsFound != idsCount){
			return;
		}

		onFinished(result.getJavaScriptObject());
	}



	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while searching for: " + searchString);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		session.getUiElements().setLogText("Found users");
		events.onFinished(jso);
	}



}
