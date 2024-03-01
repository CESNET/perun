package cz.metacentrum.perun.ldapc.model;

import cz.metacentrum.perun.core.api.Attribute;
import java.util.Collection;
import java.util.Map;

/**
 * Value transformer used to modify incoming attribute values from Perun to conform expected format in the LDAP.
 */
public interface AttributeValueTransformer {

  /**
   * Returns array of string values to be stored in LDAP from incoming list value from Perun.
   *
   * @param value Value of Attribute from Perun
   * @param attr  Attribute from Perun
   * @return Multi String value to be stored in LDAP
   */
  public String[] getAllValues(Collection<String> value, Attribute attr);

  /**
   * Returns array of string values to be stored in LDAP from incoming map value from Perun.
   *
   * @param value Value of Attribute from Perun
   * @param attr  Attribute from Perun
   * @return Multi String value to be stored in LDAP
   */
  public String[] getAllValues(Map<String, String> value, Attribute attr);

  /**
   * Returns single string value to be stored in LDAP from incoming map value from Perun.
   *
   * @param value Value of Attribute from Perun
   * @param attr  Attribute from Perun
   * @return Single String value to be stored in LDAP
   */
  public String getValue(Map<String, String> value, Attribute attr);

  /**
   * Returns single string value to be stored in LDAP from incoming single string value from Perun.
   *
   * @param value Value of Attribute from Perun
   * @param attr  Attribute from Perun
   * @return Single String value to be stored in LDAP
   */
  public String getValue(String value, Attribute attr);

  /**
   * Returns single string value to be stored in LDAP from incoming list of values from Perun.
   *
   * @param value Value of Attribute from Perun
   * @param attr  Attribute from Perun
   * @return Single String value to be stored in LDAP
   */
  public String getValue(Collection<String> value, Attribute attr);

  /**
   * TRUE if transformer prefers to convert all attribute values at once (whole attribute). FALSE if transformer is
   * applied to each attribute value.
   *
   * @return TRUE = convert attribute at once / FALSE = convert each attribute value
   */
  public Boolean isMassTransformationPreferred();

  /**
   * TRUE if transformer is reducing multi-valued input into single value (eg. Map -> JSON, List -> String). FALSE if
   * transformer keeps original single-/multi-valued nature of attribute.
   *
   * @return TRUE = reduces multi-valued attributes to single-valued / FALSE = keeps nature of attribute.
   */
  public Boolean isReduce();

}
