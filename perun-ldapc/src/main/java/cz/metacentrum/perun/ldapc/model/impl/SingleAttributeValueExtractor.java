package cz.metacentrum.perun.ldapc.model.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute.SingleValueExtractor;

public class SingleAttributeValueExtractor<T extends PerunBean> extends AttributeValueExtractor implements SingleValueExtractor<T> {

	@Override
	public String getValue(T bean, Attribute... attributes) throws InternalErrorException {
		String result;
		for (Attribute attribute : attributes) {
			if(this.appliesToAttribute(attribute)) {
				if(attribute == null) return null;
				if (attribute.getType().equals(String.class.getName()) || attribute.getType().equals(BeansUtils.largeStringClassName)) {
					Object value = attribute.getValue();
					if(value == null) 
						return null;
					else {
						result =  value.toString();
						if(valueTransformer != null) {
							result = valueTransformer.getValue(result, attribute);
						}
					}
				} else if (attribute.getType().equals(ArrayList.class.getName()) || attribute.getType().equals(BeansUtils.largeArrayListClassName)) {
					List<String> values = attribute.valueAsList();
					if(values == null || values.size() == 0) 
						return null;
					else 
						result = (valueTransformer == null) 
							? values.toString() : valueTransformer.getValue(values, attribute);
				} else if (attribute.getType().equals(LinkedHashMap.class.getName())) {
					LinkedHashMap<String, String> values = attribute.valueAsMap();
					if(values == null || values.isEmpty()) 
						return null;
					else
						result = (valueTransformer == null) 
							? values.toString() : valueTransformer.getValue(values, attribute);
				} else {
					return null;
				}
				return result;
			}
		}
		return null;
	}

}
