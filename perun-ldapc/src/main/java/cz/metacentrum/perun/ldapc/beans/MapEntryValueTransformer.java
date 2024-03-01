package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;

import java.util.Map;

/**
 * Value transformer, which converts {@link Map} attributes from Perun to the multi-value LDAP attribute
 * using both key and value joining them with specified separator.
 * <p>
 * If you wish to convert only keys or values, see {@link KeysetValueTransformer} or {@link ValuesetValueTransformer}.
 */
public class MapEntryValueTransformer extends ValueTransformerBase implements AttributeValueTransformer {

  /**
   * Separator used to join KEY=VALUE pairs of resulting attribute values.
   * It is initialized from Spring config.
   */
  protected String separator;

  @Override
  public String[] getAllValues(Map<String, String> value, Attribute attr) {
    String[] result = new String[value.size()];
    int i = 0;
    for (Map.Entry<String, String> entry : value.entrySet()) {
      result[i] = entry.getKey() + this.separator + entry.getValue();
      i++;
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
