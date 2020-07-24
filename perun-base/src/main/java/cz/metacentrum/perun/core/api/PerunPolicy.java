package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * PerunPolicy represents a set of rules which is used to determine principal's access rights.
 *
 * policyName is policy's unique identification which is used in the configuration file perun-roles.yml
 * perunRoles is a list of maps where each map entry consists from a role name as a key and a role object as a value.
 *            Relation between each map in the list is logical OR and relation between each entry in the map is logical AND.
 *            Example list - (Map1, Map2...)
 *            Example map - key: VOADMIN ; value: Vo
 *                          key: GROUPADMIN ; value: Group
 * includePolicies is a list of policies names whose rules will be also included in the authorization.
 *
 */
public class PerunPolicy {

	private String policyName;
	private List<Map<String, String>> perunRoles;
	private List<String> includePolicies;

	public PerunPolicy(String policyName, List<Map<String, String>> perunRoles, List<String> includePolicies) {
		this.policyName = policyName;
		this.perunRoles = perunRoles;
		this.includePolicies = includePolicies;
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

	@Override
	public String toString() {
		return "PerunPolicy{" +
			"policyName='" + policyName + '\'' +
			", perunRoles=" + perunRoles +
			", includePolicies=" + includePolicies +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PerunPolicy that = (PerunPolicy) o;
		return Objects.equals(policyName, that.policyName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(policyName);
	}
}
