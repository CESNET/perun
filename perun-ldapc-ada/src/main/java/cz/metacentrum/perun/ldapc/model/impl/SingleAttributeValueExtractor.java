package cz.metacentrum.perun.ldapc.model.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute.SingleValueExtractor;

public class SingleAttributeValueExtractor<T extends PerunBean> extends AttributeValueExtractor implements SingleValueExtractor<T> {

	@Override
	public String getValue(T bean, Attribute... attributes) throws InternalErrorException {
		for (Attribute attribute : attributes) {
			if(this.appliesToAttribute(attribute)) {
				if(attribute == null || attribute.getValue() == null) return null;
				if(valueTransformer != null) 
					// TODO check the cast
					return valueTransformer.getValue(attribute.getValue().toString(), attribute);
				else
					return attribute.getValue().toString();
			}
		}
		return null;
	}

}
