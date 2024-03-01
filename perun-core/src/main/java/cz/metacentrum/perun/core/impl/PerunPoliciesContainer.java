package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.PerunPolicy;
import cz.metacentrum.perun.core.api.RoleManagementRules;
import cz.metacentrum.perun.core.api.exceptions.PolicyNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleManagementRulesNotExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * PerunPoliciesContainer stores a list of perun policies and map of role management rules..
 */
public class PerunPoliciesContainer {

  private static final Logger log = LoggerFactory.getLogger(PerunBasicDataSource.class);
  private Set<PerunPolicy> perunPolicies = new HashSet<>();
  private Map<String, RoleManagementRules> rolesManagementRules = new HashMap<>();

  public void setPerunPolicies(Set<PerunPolicy> perunPolicies) {
    this.perunPolicies = perunPolicies;
  }


  public void setRolesManagementRules(Map<String, RoleManagementRules> rolesManagementRules) {
    this.rolesManagementRules = rolesManagementRules;
  }

  /**
   * Get PerunPolicy for the policy name from the PerunPoliciesContainer
   *
   * @param policyName for which will be the policy fetched
   * @return PerunPolicy for the role name
   * @throws PolicyNotExistsException of there is no policy for the policy name
   */
  public PerunPolicy getPerunPolicy(String policyName) throws PolicyNotExistsException {
    for (PerunPolicy policy : perunPolicies) {
      if (policy.getPolicyName().equals(policyName)) {
        return policy;
      }
    }
    throw new PolicyNotExistsException(
        "Policy with name " + policyName + "does not exists in the PerunPoliciesContainer.");
  }

  /**
   * Get RoleManagementRules for the role name from the PerunPoliciesContainer
   *
   * @param roleName for which will be the rules fetched
   * @return RoleManagementRules for the role name
   * @throws RoleManagementRulesNotExistsException of there are no rules for the role name
   */
  public RoleManagementRules getRoleManagementRules(String roleName) throws RoleManagementRulesNotExistsException {
    if (rolesManagementRules.containsKey(roleName)) {
      return rolesManagementRules.get(roleName);
    } else {
      throw new RoleManagementRulesNotExistsException(
          "Management rules for role name " + roleName + "does not exists in the PerunPoliciesContainer.");
    }
  }

  /**
   * Fetch policy and all its (also nested) included policies.
   * Method detects and skips cycles.
   *
   * @param policyName is a policy definition for which will be policy and its all included policies fetched.
   * @return all included policies together with the policy defined by policyName.
   * @throws PolicyNotExistsException when the given policyName does not exist in the PerunPoliciesContainer.
   */
  public List<PerunPolicy> fetchPolicyWithAllIncludedPolicies(String policyName) throws PolicyNotExistsException {
    Map<String, PerunPolicy> allIncludedPolicies = new HashMap<>();
    Queue<String> policiesToCheck = new LinkedList<>();
    policiesToCheck.add(policyName);

    while (!policiesToCheck.isEmpty()) {
      String policy = policiesToCheck.remove();
      if (allIncludedPolicies.containsKey(policy)) {
        log.warn("Policy {} creates a cycle in the included policies of the policy {}", policy, policyName);
        continue;
      }
      PerunPolicy policyToCheck = getPerunPolicy(policy);
      allIncludedPolicies.put(policy, policyToCheck);
      policiesToCheck.addAll(policyToCheck.getIncludePolicies());
    }
    return new ArrayList<>(allIncludedPolicies.values());
  }

  /**
   * Return all loaded perun policies.
   *
   * @return all loaded policies
   */
  public Set<PerunPolicy> getAllPolicies() {
    return Collections.unmodifiableSet(perunPolicies);
  }

  /**
   * Return all loaded roles management rules.
   *
   * @return all roles management rules
   */
  public List<RoleManagementRules> getAllRolesManagementRules() {
    return Collections.unmodifiableList(new ArrayList<>(rolesManagementRules.values()));
  }
}
