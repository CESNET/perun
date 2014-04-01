package cz.metacentrum.perun.webgui.json.keyproviders;

import com.google.gwt.view.client.ProvidesKey;
import cz.metacentrum.perun.webgui.model.Destination;

/**
 * Provides key for Destination objects for tables
 * (if service param is available, it's used too)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class DestinationKeyProvider implements ProvidesKey<Destination> {

	public Object getKey(Destination o) {
		return o;
	}

}
