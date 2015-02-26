package cz.metacentrum.perun.webgui.json.keyproviders;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.view.client.ProvidesKey;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.ServiceState;

/**
 * Key provider for all basic model classes in Perun for tables
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 *
 * @param <T>
 */
public class ServiceStateKeyProvider<T extends JavaScriptObject> implements ProvidesKey<T> {

	public Object getKey(T o) {
		// returns ID
		ServiceState go = (ServiceState) o;
		return go.getTaskId();
	}

}
