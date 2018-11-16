package cz.metacentrum.perun.webgui.json.keyproviders;

import com.google.gwt.view.client.ProvidesKey;
import cz.metacentrum.perun.webgui.model.Attribute;

import java.util.HashMap;

/**
 * Proivdes key for entityless attribute
 *
 * @author Dano Fecko <dano9500@gmail.com>
 */
public class EntitylessAttributeKeyProvider implements ProvidesKey<Attribute> {

	private HashMap<Attribute, String> map;

	public EntitylessAttributeKeyProvider(HashMap<Attribute, String> map){
		this.map = map;
	}

	public Object getKey(Attribute attribute) {
		return map.get(attribute);
	}

}