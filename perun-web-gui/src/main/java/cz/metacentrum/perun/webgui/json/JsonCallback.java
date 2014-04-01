package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Interface of a class, which is called when Ajax operation finishes.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public interface JsonCallback {

	/**
	 * Called, when the call successfully finishes.
	 *
	 * @param jso The object, which is returned.
	 */
	void onFinished(JavaScriptObject jso);

	/**
	 * Called, when an error occurs
	 * @param error error object returned from RPC
	 */
	void onError(PerunError error);

	/**
	 * Called, when started
	 */
	void onLoadingStart();

	/**
	 * This must be called ONLY IF we want to get the values via custom
	 * onFinished request
	 * When getting table, it's called automatically.
	 */
	void retrieveData();
}
