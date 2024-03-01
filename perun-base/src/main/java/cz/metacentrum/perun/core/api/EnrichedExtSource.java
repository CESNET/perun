package cz.metacentrum.perun.core.api;

import java.util.Map;
import java.util.Objects;

/**
 * Class representing an extended version of {@link ExtSource}.
 * <p>
 * This class is used for providing additional information about an ExtSource
 * via its attributes.
 *
 * @author Lucie Kureckova <luckureckova@gmail.com>
 */
public class EnrichedExtSource {
  private ExtSource extSource;
  private Map<String, String> attributes;

  public EnrichedExtSource(ExtSource extSource) {
    this.extSource = extSource;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnrichedExtSource that = (EnrichedExtSource) o;
    return Objects.equals(extSource, that.extSource) && Objects.equals(attributes, that.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(extSource, attributes);
  }

  @Override
  public String toString() {
    return "EnrichedExtSource{" +
        "extSource=" + extSource +
        ", attributes=" + attributes +
        '}';
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public ExtSource getExtSource() {
    return extSource;
  }

  public void setExtSource(ExtSource extSource) {
    this.extSource = extSource;
  }
}
