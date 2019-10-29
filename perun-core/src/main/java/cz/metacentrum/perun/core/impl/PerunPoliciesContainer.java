package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.PerunPolicy;
import cz.metacentrum.perun.core.api.exceptions.PolicyNotExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * PerunPoliciesContainer stores a list of perun policies.
 */
public class PerunPoliciesContainer {

	private static final Logger log = LoggerFactory.getLogger(PerunBasicDataSource.class);
	private List<PerunPolicy> perunPolicies = new ArrayList<>();

	public void setPerunPolicies(List<PerunPolicy> perunPolicies) {
		this.perunPolicies = perunPolicies;
	}

	public PerunPolicy getPerunPolicy(String policyName) throws PolicyNotExistsException {
		for (PerunPolicy policy : perunPolicies) {
			if (policy.getPolicyName().equals(policyName)) return policy;
		}
		throw new PolicyNotExistsException("Policy with name "+ policyName + "does not exists in the PerunPoliciesContainer.");
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
}
