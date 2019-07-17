package cz.metacentrum.perun.ldapc.model.impl;


import java.util.ArrayList;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;

public class PerunAttributeDesc<T extends PerunBean> implements PerunAttribute<T> {

	private String name;
	private Boolean required;
	private Boolean multivalued;
	private PerunAttribute.SingleValueExtractor<T> singleValueExtractor;
	private PerunAttribute.MultipleValuesExtractor<T> multipleValuesExtractor;

	public PerunAttributeDesc() {
		super();
	}
	
	public PerunAttributeDesc(String name, Boolean required, PerunAttribute.SingleValueExtractor<T> extractor) {
		super();
		this.name = name;
		this.required = required;
		this.multivalued = false;
		this.singleValueExtractor = extractor;
	}

	public PerunAttributeDesc(String name, Boolean required, PerunAttribute.MultipleValuesExtractor<T> extractor) {
		super();
		this.name = name;
		this.required = required;
		this.multivalued = true;
		this.multipleValuesExtractor = extractor;
	}
	
	@Override
	public boolean isRequired() {
		return getRequired();
	}

	@Override
	public boolean isMultiValued() {
		return getMultivalued();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getName(AttributeDefinition attr) {
		// TODO: check if the attribute name parameter is not empty
		if(name.contains(";") && attr != null) {
			String param = attr.getFriendlyNameParameter();
			return name + param;
		}
		return name;
	}

	@Override
	public String getBaseName() {
		if(name.contains(";")) {
			return name.substring(0, name.indexOf(";"));
		};
		return name;
	}

	@Override
	public boolean hasValue(T bean, Attribute...attributes) throws InternalErrorException {
		if(isMultiValued()) {
			Object[] values = getValues(bean, attributes);
			return values != null && values.length > 0;
		} else {
			Object value = getValue(bean, attributes);
			return value != null && !value.toString().isEmpty();
		}
	}

	@Override
	public String getValue(T bean, Attribute...attributes) throws InternalErrorException {
		return singleValueExtractor != null ? singleValueExtractor.getValue(bean, attributes) : null;
	}

	@Override
	public String[] getValues(T bean, Attribute...attributes) throws InternalErrorException {
		return multipleValuesExtractor != null ? multipleValuesExtractor.getValues(bean, attributes) : null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getMultivalued() {
		return multivalued;
	}

	@Override
	public PerunAttribute.SingleValueExtractor<T> getSingleValueExtractor() {
		return singleValueExtractor;
	}

	@Override
	public void setSingleValueExtractor(PerunAttribute.SingleValueExtractor<T> valueExtractor) {
		this.multivalued = false;
		this.singleValueExtractor = valueExtractor;
	}

	@Override
	public PerunAttribute.MultipleValuesExtractor<T> getMultipleValuesExtractor() {
		return multipleValuesExtractor;
	}

	@Override
	public void setMultipleValuesExtractor(PerunAttribute.MultipleValuesExtractor<T> valueExtractor) {
		this.multivalued = true;
		this.multipleValuesExtractor = valueExtractor;
	}

	@Override
	public boolean requiresAttributeBean() {
		if(isMultiValued()) {
			return getMultipleValuesExtractor() instanceof AttributeValueExtractor; 
		} else {
			return getSingleValueExtractor() instanceof AttributeValueExtractor;
		}
	}
	
}
