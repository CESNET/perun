package cz.metacentrum.perun.ldapc.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute.MultipleValuesExtractor;

public class MultipleAttributeValueExtractor<T extends PerunBean> extends AttributeValueExtractor implements MultipleValuesExtractor<T> {

	@Override
	public String[] getValues(T bean, Attribute... attributes) throws InternalErrorException {
		String[] result = null;
		for (Attribute attribute : attributes) {
			if(this.appliesToAttribute(attribute)) {
				if(attribute == null) return null;
				if (attribute.getType().equals(String.class.getName()) || attribute.getType().equals(BeansUtils.largeStringClassName)) {
					Object value = attribute.getValue();
					if(value == null) 
						return null;
					else
						result = new String[] { (String)value };
				} else if (attribute.getType().equals(ArrayList.class.getName()) || attribute.getType().equals(BeansUtils.largeArrayListClassName)) {
					List<String> values = attribute.valueAsList();
					if(values == null || values.size() == 0) 
						return null;
					else 
						result = values.toArray(new String[1]);
				} else if (attribute.getType().equals(LinkedHashMap.class.getName())) {
					LinkedHashMap<String, String> values = attribute.valueAsMap();
					if(values == null || values.isEmpty()) 
						return null;
					else
						result = values.keySet().toArray(new String[values.size()]);
				} else {
					return null;
				}

				return valueTransformer == null ? result : 
					Arrays.stream(result)
						.map(value -> valueTransformer.getValue(value, attribute))
						.toArray(String[]::new);
			}
		}
		return null;
	}

}
