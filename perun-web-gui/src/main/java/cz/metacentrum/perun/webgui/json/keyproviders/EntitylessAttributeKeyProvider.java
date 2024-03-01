package cz.metacentrum.perun.webgui.json.keyproviders;

import com.google.gwt.view.client.ProvidesKey;
import cz.metacentrum.perun.webgui.model.Attribute;

/**
 * Proivdes key for entityless attribute
 *
 * @author Dano Fecko <dano9500@gmail.com>
 */
public class EntitylessAttributeKeyProvider implements ProvidesKey<Attribute> {

  public Object getKey(Attribute attribute) {
    return attribute.getKey();
  }

}
