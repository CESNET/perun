package cz.metacentrum.perun.ldapc.model;

import cz.metacentrum.perun.core.api.Attribute;

public interface AttributeValueTransformer {
	
	public String getValue(String value, Attribute attr);
}
