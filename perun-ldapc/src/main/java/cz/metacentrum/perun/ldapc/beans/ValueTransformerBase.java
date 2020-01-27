package cz.metacentrum.perun.ldapc.beans;

import java.util.Collection;
import java.util.Map;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;

public class ValueTransformerBase implements AttributeValueTransformer {
	
	protected Boolean massTransformationPreferred = false;
	protected Boolean reduce = false;
	
	@Override
	public String getValue(String value, Attribute attr) {
		return value;
	}

	@Override
	public String getValue(Collection<String> value, Attribute attr) {
		return value.toString();
	}

	@Override
	public String getValue(Map<String, String> value, Attribute attr) {
		return value.toString();
	}

	@Override
	public String[] getAllValues(Collection<String> value, Attribute attr) {
		return value.toArray(new String[1]);
	}

	@Override
	public String[] getAllValues(Map<String, String> value, Attribute attr) {
		throw new UnsupportedOperationException("This operation must be implemented in child class.");
	}

	@Override
	public Boolean isMassTransformationPreferred() {
		return massTransformationPreferred;
	}

	public void setMassTransformationPreferred(Boolean massTransformationPreferred) {
		this.massTransformationPreferred = massTransformationPreferred;
	}

	@Override
	public Boolean isReduce() {
		return reduce;
	}

	public void setReduce(Boolean reduce) {
		this.reduce = reduce;
	}

}
