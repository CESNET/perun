package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;

import java.util.Map;

/**
 * Value transformer, which converts {@link Map} attributes from Perun to the multi-value LDAP attribute
 * using only map keys.
 */
public class KeysetValueTransformer extends ValueTransformerBase implements AttributeValueTransformer {

  @Override
  public String[] getAllValues(Map<String, String> value, Attribute attr) {
    return value.keySet().toArray(new String[value.size()]);
  }

  @Override
  public Boolean isMassTransformationPreferred() {
    return true;
  }

}
