package cz.metacentrum.perun.ldapc.model.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;
import cz.metacentrum.perun.ldapc.model.PerunAttribute.MultipleValuesExtractor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Extracts the value of the first matching attribute into a form of a multi-valued LDAP attribute, optionally using an
 * {@link AttributeValueTransformer}
 * @param <T> Type parameter extending the PerunBean class.
 */
public class MultipleAttributeValueExtractor<T extends PerunBean> extends AttributeValueExtractor
    implements MultipleValuesExtractor<T> {

  @Override
  public String[] getValues(T bean, Attribute... attributes) {
    String[] result = null;
    for (Attribute attribute : attributes) {
      if (this.appliesToAttribute(attribute)) {
        if (attribute == null) {
          return null;
        }
        if (attribute.getType().equals(ArrayList.class.getName())) {
          List<String> values = attribute.valueAsList();
          if (values == null || values.size() == 0) {
            return null;
          } else {
            if (valueTransformer == null) {
              result = values.toArray(new String[1]);
            } else {
              if (valueTransformer.isMassTransformationPreferred()) {
                result = valueTransformer.getAllValues(values, attribute);
              } else {
                result =
                    values.stream().map(value -> valueTransformer.getValue(value, attribute)).toArray(String[]::new);
              }
            }
          }
        } else if (attribute.getType().equals(LinkedHashMap.class.getName())) {
          LinkedHashMap<String, String> values = attribute.valueAsMap();
          if (values == null || values.isEmpty()) {
            return null;
          } else {
            if (valueTransformer != null) {
              result = valueTransformer.getAllValues(values, attribute);
            } else {
              // no default way to convert attribute map to list
              result = null;
            }
          }
        } else {
          // use toString() for String, Integer nad Boolean types
          Object value = attribute.getValue();
          if (value == null) {
            return null;
          } else {
            result = new String[] {
                (valueTransformer == null) ? value.toString() : valueTransformer.getValue((String) value, attribute)};
          }
        }
        // values must unique, otherwise ldap server rejects them
        result = Arrays.asList(result).stream().distinct().toArray(String[]::new);
        return result;
      }
    }
    return null;
  }

}
