package cz.metacentrum.perun.ldapc.beans;

import java.util.Map;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;

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
