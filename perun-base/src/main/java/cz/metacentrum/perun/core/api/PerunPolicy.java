package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * PerunPolicy represents a set of rules which is used to determine principal's access rights.
 * <p>
 * policyName is policy's unique identification which is used in the configuration file perun-roles.yml
 * perunRoles is a list of maps where each map entry consists from a role name as a key and a role object as a value.
 * Relation between each map in the list is logical OR and relation between each entry in the map is logical AND.
 * Example list - (Map1, Map2...)
 * Example map - key: VOADMIN ; value: Vo
 * key: GROUPADMIN ; value: Group
 * includePolicies is a list of policies names whose rules will be also included in the authorization.
 * mfaRules is a list of maps where each map entry consists from the key 'MFA' and the value is either null or an object.
 * If the value is null, the whole operation is considered as critical (thus requires MFA), however if an object is assigned,
 * the operation is critical only if the object is critical as well.
 */
public class PerunPolicy {

  private String policyName;
  private List<Map<String, String>> perunRoles;
  private List<String> includePolicies;
  private List<Map<String, String>> mfaRules;

  public PerunPolicy(String policyName, List<Map<String, String>> perunRoles, List<String> includePolicies,
                     List<Map<String, String>> mfaRules) {
    this.policyName = policyName;
    this.perunRoles = perunRoles;
    this.includePolicies = includePolicies;
    this.mfaRules = mfaRules;
  }

  public List<Map<String, String>> getPerunRoles() {
    return perunRoles;
  }

  public void setPerunRoles(List<Map<String, String>> perunRoles) {
    this.perunRoles = perunRoles;
  }

  public List<String> getIncludePolicies() {
    return includePolicies;
  }

  public void setIncludePolicies(List<String> includePolicies) {
    this.includePolicies = includePolicies;
  }

  public String getPolicyName() {
    return policyName;
  }

  public void setPolicyName(String policyName) {
    this.policyName = policyName;
  }

  public List<Map<String, String>> getMfaRules() {
    return mfaRules;
  }

  public void setMfaRules(List<Map<String, String>> mfaRules) {
    this.mfaRules = mfaRules;
  }

  @Override
  public String toString() {
    return "PerunPolicy{" +
        "policyName='" + policyName + '\'' +
        ", perunRoles=" + perunRoles +
        ", includePolicies=" + includePolicies +
        ", mfaRules=" + mfaRules +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PerunPolicy that = (PerunPolicy) o;
    return Objects.equals(policyName, that.policyName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyName);
  }
}
