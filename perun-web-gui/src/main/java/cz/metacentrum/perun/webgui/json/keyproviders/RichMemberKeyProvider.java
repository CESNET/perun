package cz.metacentrum.perun.webgui.json.keyproviders;

import com.google.gwt.view.client.ProvidesKey;
import cz.metacentrum.perun.webgui.model.RichMember;

/**
 * Key provider for the table with RichMember objects
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class RichMemberKeyProvider implements ProvidesKey<RichMember> {

	public Object getKey(RichMember o) {
		// returns ID
		return o.getUser().getId();
	}

}
