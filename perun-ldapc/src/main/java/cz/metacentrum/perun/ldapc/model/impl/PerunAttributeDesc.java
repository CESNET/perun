package cz.metacentrum.perun.ldapc.model.impl;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;

/**
 * Represents a description of a Perun attribute, providing functionality
 * for single-valued or multi-valued attribute extraction and handling
 * various attribute configuration details for when it gets propagated into LDAP.
 *
 * @param <T> Type parameter extending the PerunBean class.
 */
public class PerunAttributeDesc<T extends PerunBean> implements PerunAttribute<T> {

  private String name;
  private Boolean required;
  private Boolean multivalued;
  private Boolean deleted = false;
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
  public String getBaseName() {
    if (name.contains(";")) {
      return name.substring(0, name.indexOf(";"));
    }

    return name;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  @Override
  public PerunAttribute.MultipleValuesExtractor<T> getMultipleValuesExtractor() {
    return multipleValuesExtractor;
  }

  public Boolean getMultivalued() {
    return multivalued;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getName(AttributeDefinition attr) {
    // TODO: check if the attribute name parameter is not empty
    if (name.contains(";") && attr != null) {
      String param = attr.getFriendlyNameParameter();
      return name + param;
    }
    return name;
  }

  public Boolean getRequired() {
    return required;
  }

  @Override
  public PerunAttribute.SingleValueExtractor<T> getSingleValueExtractor() {
    return singleValueExtractor;
  }

  @Override
  public String getValue(T bean, Attribute... attributes) {
    return singleValueExtractor != null ? singleValueExtractor.getValue(bean, attributes) : null;
  }

  @Override
  public String[] getValues(T bean, Attribute... attributes) {
    return multipleValuesExtractor != null ? multipleValuesExtractor.getValues(bean, attributes) : null;
  }

  @Override
  public boolean hasValue(T bean, Attribute... attributes) {
    if (isMultiValued()) {
      Object[] values = getValues(bean, attributes);
      return values != null && values.length > 0;
    } else {
      Object value = getValue(bean, attributes);
      return value != null && !value.toString().isEmpty();
    }
  }

  @Override
  public boolean isDeleted() {
    return getDeleted();
  }

  @Override
  public boolean isMultiValued() {
    return getMultivalued();
  }

  @Override
  public boolean isRequired() {
    return getRequired();
  }

  @Override
  public boolean requiresAttributeBean() {
    if (isMultiValued()) {
      return getMultipleValuesExtractor() instanceof AttributeValueExtractor;
    } else {
      return getSingleValueExtractor() instanceof AttributeValueExtractor;
    }
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public void setMultipleValuesExtractor(PerunAttribute.MultipleValuesExtractor<T> valueExtractor) {
    this.multivalued = true;
    this.multipleValuesExtractor = valueExtractor;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setRequired(Boolean required) {
    this.required = required;
  }

  @Override
  public void setSingleValueExtractor(PerunAttribute.SingleValueExtractor<T> valueExtractor) {
    this.multivalued = false;
    this.singleValueExtractor = valueExtractor;
  }

}
