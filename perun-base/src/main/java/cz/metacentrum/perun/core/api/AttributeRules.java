package cz.metacentrum.perun.core.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class containing attribute policies and information which actions are critical
 *
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class AttributeRules {

  private List<AttributePolicyCollection> attributePolicyCollections;
  private Map<AttributeAction, Boolean> criticalActions = new HashMap<>();

  public AttributeRules() {
  }

  public AttributeRules(List<AttributePolicyCollection> attributePolicyCollections) {
    this.attributePolicyCollections = attributePolicyCollections;
  }

  public List<AttributePolicyCollection> getAttributePolicyCollections() {
    return attributePolicyCollections;
  }

  public void setAttributePolicyCollections(List<AttributePolicyCollection> attributePolicyCollections) {
    this.attributePolicyCollections = attributePolicyCollections;
  }

  public Map<AttributeAction, Boolean> getCriticalActions() {
    return criticalActions;
  }

  public void setCriticalActions(Map<AttributeAction, Boolean> criticalActions) {
    this.criticalActions = criticalActions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AttributeRules that = (AttributeRules) o;
    return Objects.equals(attributePolicyCollections, that.attributePolicyCollections) &&
        Objects.equals(criticalActions, that.criticalActions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributePolicyCollections, criticalActions);
  }

  @Override
  public String toString() {
    return "AttributeRules{" +
        "attributePolicyCollections=" + attributePolicyCollections +
        ", criticalActions=" + criticalActions +
        '}';
  }
}
