package cz.metacentrum.perun.ldapc.model.impl;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AttributeValueExtractor {

  protected AttributeValueTransformer valueTransformer;
  private String namespace;
  private String name;
  private String nameRegexp;

  AttributeValueExtractor() {
  }

  public boolean appliesToAttribute(AttributeDefinition attr) {
    if (nameRegexp != null && !nameRegexp.isEmpty()) {
      Matcher matcher = Pattern.compile(nameRegexp).matcher(attr.getName());
      return matcher.find();
    }
    if (!attr.getNamespace().equals(this.namespace)) {
      return false;
    }
    if (name != null && !name.isEmpty()) {
      return attr.getBaseFriendlyName().equals(name);
    }
    return false;
  }

  public String getName() {
    return name;
  }

  public String getNameRegexp() {
    return nameRegexp;
  }

  public String getNamespace() {
    return namespace;
  }

  public AttributeValueTransformer getValueTransformer() {
    return valueTransformer;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNameRegexp(String nameRegexp) {
    this.nameRegexp = nameRegexp;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public void setValueTransformer(AttributeValueTransformer valueTransformer) {
    this.valueTransformer = valueTransformer;
  }
}
