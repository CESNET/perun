package cz.metacentrum.perun.dispatcher.model;

import java.util.List;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public class MatchingRule {

	private final List<String> rules;

	public MatchingRule(List<String> rules) {
		this.rules = rules;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rules == null) ? 0 : rules.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatchingRule other = (MatchingRule) obj;
		if (rules == null) {
			if (other.rules != null)
				return false;
		} else if (!rules.equals(other.rules))
			return false;
		return true;
	}

	public List<String> getRules() {
		return rules;
	}

}
