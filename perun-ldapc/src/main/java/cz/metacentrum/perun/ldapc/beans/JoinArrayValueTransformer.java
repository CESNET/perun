package cz.metacentrum.perun.ldapc.beans;

import java.util.Collection;
import static java.util.stream.Collectors.joining;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;

public class JoinArrayValueTransformer extends ValueTransformerBase implements AttributeValueTransformer {

	protected String separator;

	@Override
	public String getValue(Collection<String> value, Attribute attr) {
		return value.stream().collect(joining(this.separator));
	}

	@Override
	public Boolean isMassTransformationPreferred() {
		return true;
	}

	@Override
	public Boolean isReduce() {
		return true;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}
	
	
}
