package cz.metacentrum.perun.ldapc.beans;

import java.util.Map;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;

public class MapEntryValueTransformer extends ValueTransformerBase implements AttributeValueTransformer {

	protected String separator;
	
	@Override
	public String[] getAllValues(Map<String, String> value, Attribute attr) {
		String[] result = new String[value.size()]; 
		int i = 0;
		for(Map.Entry<String, String> entry : value.entrySet()) {
			result[i] = entry.getKey() + this.separator + entry.getValue();
		}
		return result;
	}

	@Override
	public Boolean isMassTransformationPreferred() {
		return true;
	}

	@Override
	public Boolean isReduce() {
		return false;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}


}
