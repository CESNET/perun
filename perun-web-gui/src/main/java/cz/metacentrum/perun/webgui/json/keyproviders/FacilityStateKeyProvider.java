package cz.metacentrum.perun.webgui.json.keyproviders;

import com.google.gwt.view.client.ProvidesKey;
import cz.metacentrum.perun.webgui.model.FacilityState;

/**
 * Provides key for tables with FacilityState objects
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilityStateKeyProvider implements ProvidesKey<FacilityState> {

	public Object getKey(FacilityState o) {
		// returns ID
		return o.getFacility().getId();
	}

}
