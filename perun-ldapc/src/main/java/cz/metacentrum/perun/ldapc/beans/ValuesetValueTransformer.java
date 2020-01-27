package cz.metacentrum.perun.ldapc.beans;

import java.util.Map;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;

/**
 * Value transformer, which converts {@link Map} attributes from Perun to the multi-value LDAP attribute
 * using only map values.
 *
 * NOTE: Map values are not converted to unique set. Based on destination LDAP attribute equality you might
 * need to modify/convert values to unique by other means.
 */
public class ValuesetValueTransformer extends ValueTransformerBase implements AttributeValueTransformer {

	@Override
	public String[] getAllValues(Map<String, String> value, Attribute attr) {
		return value.values().toArray(new String[value.size()]);
	}

	@Override
	public Boolean isMassTransformationPreferred() {
		return true;
	}

}
