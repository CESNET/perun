package cz.metacentrum.perun.dispatcher.model;

import java.util.List;
import java.util.Objects;

/**
 * Set of MatchingRules. Used to match Event headers to EngineMessageProducer queues.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class MatchingRule {

	private final List<String> rules;

	/**
	 * Create new set of MatchingRules
	 *
	 * @param rules Rules to set
	 */
	public MatchingRule(List<String> rules) {
		this.rules = rules;
	}

	/**
	 * Return list of rules
	 *
	 * @return list of rules
	 */
	public List<String> getRules() {
		return rules;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MatchingRule)) return false;
		MatchingRule that = (MatchingRule) o;
		return Objects.equals(rules, that.rules);
	}

	@Override
	public int hashCode() {
		return Objects.hash(rules);
	}

}
