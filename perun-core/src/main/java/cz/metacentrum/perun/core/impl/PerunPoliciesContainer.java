package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.core.api.exceptions.PolicyNotExistsException;

import java.util.HashMap;
import java.util.Map;

/**
 * PerunPoliciesContainer stores policies in a HashMap.
 * Key is a name of the policy and value is JsonNode which represent the particular policy.
 */
public class PerunPoliciesContainer {

	private Map<String, JsonNode> perunPolicies = new HashMap<>();

	public void setPerunPolicies(Map<String, JsonNode> perunPolicies) {
		this.perunPolicies = perunPolicies;
	}

	public JsonNode getPerunPolicy(String policyName) throws PolicyNotExistsException {
		if (!perunPolicies.containsKey(policyName)) throw new PolicyNotExistsException("Policy with name "+ policyName + "does not exists in the PerunPoliciesContainer.");
		return perunPolicies.get(policyName);
	}
}
