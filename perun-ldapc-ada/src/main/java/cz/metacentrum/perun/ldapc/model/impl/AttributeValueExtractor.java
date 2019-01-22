package cz.metacentrum.perun.ldapc.model.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;

public abstract class AttributeValueExtractor {

	private String namespace;
	private String name;
	private String nameRegexp;

	protected AttributeValueTransformer valueTransformer;
	
	AttributeValueExtractor() {
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameRegexp() {
		return nameRegexp;
	}

	public void setNameRegexp(String nameRegexp) {
		this.nameRegexp = nameRegexp;
	}

	public AttributeValueTransformer getValueTransformer() {
		return valueTransformer;
	}

	public void setValueTransformer(AttributeValueTransformer valueTransformer) {
		this.valueTransformer = valueTransformer;
	}

	public boolean appliesToAttribute(AttributeDefinition attr) {
		if(nameRegexp != null && !nameRegexp.isEmpty()) {
			Matcher matcher = Pattern.compile(nameRegexp).matcher(attr.getName());
			return matcher.find();
		}
		if(!attr.getNamespace().equals(this.namespace)) {
			return false;
		}
		if(name != null && !name.isEmpty()) {
			return attr.getBaseFriendlyName().equals(name);
		}
		return false;
	}
}
