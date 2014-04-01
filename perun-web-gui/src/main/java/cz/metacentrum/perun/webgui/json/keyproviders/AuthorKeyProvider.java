package cz.metacentrum.perun.webgui.json.keyproviders;

import com.google.gwt.view.client.ProvidesKey;
import cz.metacentrum.perun.webgui.model.Author;

/**
 * Provides key for Author objects for tables
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AuthorKeyProvider implements ProvidesKey<Author> {

	public Object getKey(Author o) {
		// returns ID
		return o.getId();
	}

}