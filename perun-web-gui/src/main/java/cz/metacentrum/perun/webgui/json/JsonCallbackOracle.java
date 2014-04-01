package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

/**
 * Provides methods for handling suggestions / local filtering in tables from JsonCallbacks
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @param <T> type of javascript objects in table
 */

public interface JsonCallbackOracle <T extends JavaScriptObject> extends JsonCallbackTable<T> {

	/**
	 * Filter results in table (show/hide)
	 */
	void filterTable(String filter);

	/**
	 * Get suggestion oracle
	 */
	UnaccentMultiWordSuggestOracle getOracle();

	/**
	 * Set new suggestion oracle for table
	 *
	 * @param oracle
	 */
	void setOracle(UnaccentMultiWordSuggestOracle oracle);

}
