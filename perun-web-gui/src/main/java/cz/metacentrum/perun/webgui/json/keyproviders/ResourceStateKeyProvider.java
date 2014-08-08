package cz.metacentrum.perun.webgui.json.keyproviders;

import com.google.gwt.view.client.ProvidesKey;
import cz.metacentrum.perun.webgui.model.ResourceState;

/**
 * Provides key for tables with ResourceState objects
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ResourceStateKeyProvider implements ProvidesKey<ResourceState> {

	public Object getKey(ResourceState o) {
		// returns ID
		return o.getResource().getId();
	}

}
