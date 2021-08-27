package cz.metacentrum.perun.core.api;

/**
 * Enum defining status of group-resource assignment.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public enum GroupResourceStatus {

	ACTIVE(3),
	PROCESSING(2),
	INACTIVE(1),
	FAILED(0);

	private Integer level;

	GroupResourceStatus(int level) {
		this.level = level;
	}

	public boolean isMoreImportantThan(GroupResourceStatus otherStatus) {
		return this.level > otherStatus.level;
	}

}
