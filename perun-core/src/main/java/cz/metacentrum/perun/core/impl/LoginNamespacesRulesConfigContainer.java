package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.NamespaceRules;
import cz.metacentrum.perun.core.api.exceptions.NamespaceRulesNotExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginNamespacesRulesConfigContainer {

  private static final Logger log = LoggerFactory.getLogger(LoginNamespacesRulesConfigContainer.class);

  private Map<String, NamespaceRules> namespacesRules = new HashMap<>();

  public void setNamespacesRules(Map<String, NamespaceRules> namespacesRules) {
    this.namespacesRules = namespacesRules;
  }

  /**
   * Get Rules for a specific namespace from the LoginNamespacesRulesConfigContainer
   *
   * @param namespace for which will be rules fetched
   * @return NamespaceRules for the namespace name
   * @throws NamespaceRulesNotExistsException of there are no rules for the namespace
   */
  public NamespaceRules getNamespaceRules(String namespace) throws NamespaceRulesNotExistsException {
    if (namespacesRules.get(namespace) == null) {
      throw new NamespaceRulesNotExistsException(
          "Namespace with name '" + namespace + "' does not exist in the LoginNamespacesRulesConfigContainer.");
    }

    return namespacesRules.get(namespace);
  }

  public List<NamespaceRules> getAllNamespacesRules() {
    return List.copyOf(namespacesRules.values());
  }
}
