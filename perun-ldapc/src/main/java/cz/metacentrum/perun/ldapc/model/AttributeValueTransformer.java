package cz.metacentrum.perun.ldapc.model;

import java.util.Collection;
import java.util.Map;

import cz.metacentrum.perun.core.api.Attribute;

public interface AttributeValueTransformer {
	
	public String getValue(String value, Attribute attr);
	
	public String getValue(Collection<String> value, Attribute attr);
	
	public String getValue(Map<String, String> value, Attribute attr);
	
	public String[] getAllValues(Collection<String> value, Attribute attr);
	
	public String[] getAllValues(Map<String, String> value, Attribute attr);
	
	public Boolean isMassTransformationPreferred();
	
	public Boolean isReduce();
}
