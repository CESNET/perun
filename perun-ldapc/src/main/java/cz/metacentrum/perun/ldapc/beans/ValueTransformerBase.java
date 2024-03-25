package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;
import java.util.Collection;
import java.util.Map;

/**
 * Base implementation of value transformer. It converts attribute values from Perun into either single or multi-valued
 * string values, which are acceptable by LDAP attributes.
 * <p>
 * Not all transformations are possible. In such case {@link UnsupportedOperationException} is thrown.
 * <p>
 * Specific transformers are expected to extend this class.
 * <p>
 * To apply multiple transformation to single attribute, see {@link CompositeValueTransformer}.
 */
public class ValueTransformerBase implements AttributeValueTransformer {

  /**
   * TRUE if transformer prefers to modify all attribute values at once or FALSE if transformation is applied
   * sequentially to each attribute value.
   */
  protected Boolean massTransformationPreferred = false;

  /**
   * TRUE if transformer is reducing incoming value (multi-value to single-value)
   */
  protected Boolean reduce = false;

  @Override
  public String[] getAllValues(Collection<String> value, Attribute attr) {
    return value.toArray(new String[1]);
  }

  @Override
  public String[] getAllValues(Map<String, String> value, Attribute attr) {
    throw new UnsupportedOperationException("This operation must be implemented in child class.");
  }

  @Override
  public String getValue(Map<String, String> value, Attribute attr) {
    return value.toString();
  }

  @Override
  public String getValue(String value, Attribute attr) {
    return value;
  }

  @Override
  public String getValue(Collection<String> value, Attribute attr) {
    return value.toString();
  }

  @Override
  public Boolean isMassTransformationPreferred() {
    return massTransformationPreferred;
  }

  @Override
  public Boolean isReduce() {
    return reduce;
  }

  public void setMassTransformationPreferred(Boolean massTransformationPreferred) {
    this.massTransformationPreferred = massTransformationPreferred;
  }

  public void setReduce(Boolean reduce) {
    this.reduce = reduce;
  }

}
