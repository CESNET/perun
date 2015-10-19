package cz.metacentrum.perun.webgui.json.keyproviders;

import com.google.gwt.view.client.ProvidesKey;
import cz.metacentrum.perun.webgui.model.Pair;

/**
 * Provides key for Pair objects (left is expected to be key)
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class PairKeyProvider<T,E> implements ProvidesKey<Pair<T,E>> {

	@Override
	public Object getKey(Pair<T, E> item) {
		return item.getLeft();
	}
}
