package cz.metacentrum.perun.dispatcher.model;

import java.util.Objects;

/**
 * Processing rule.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ProcessingRule {

	private int id;
	private String rule;

	/**
	 * Set rule
	 *
	 * @param rule Rule
	 */
	public void setRule(String rule) {
		this.rule = rule;
	}

	/**
	 * Get rule
	 *
	 * @return Rule
	 */
	public String getRule() {
		return rule;
	}

	/**
	 * Set Rule ID
	 *
	 * @param id ID to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get rule ID
	 *
	 * @return ID of rule
	 */
	public int getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ProcessingRule)) return false;
		ProcessingRule that = (ProcessingRule) o;
		return id == that.id &&
				Objects.equals(rule, that.rule);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, rule);
	}

}
