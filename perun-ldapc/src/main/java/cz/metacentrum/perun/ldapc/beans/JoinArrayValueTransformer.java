package cz.metacentrum.perun.ldapc.beans;

import static java.util.stream.Collectors.joining;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;
import java.util.Collection;
import java.util.List;

/**
 * Value transformer which joins {@link List <String>} attribute values from Perun into single String using defined
 * separator. Result is used for single-valued LDAP attributes.
 */
public class JoinArrayValueTransformer extends ValueTransformerBase implements AttributeValueTransformer {

  /**
   * Separator used to join incoming multi-valued attribute into single value Initialized from Spring context.
   */
  protected String separator;

  public String getSeparator() {
    return separator;
  }

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

  public void setSeparator(String separator) {
    this.separator = separator;
  }

}
