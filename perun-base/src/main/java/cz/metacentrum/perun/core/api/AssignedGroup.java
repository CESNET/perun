package cz.metacentrum.perun.core.api;

import java.util.Objects;

/**
 * Represents group assigned to a resource.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class AssignedGroup {
	private EnrichedGroup enrichedGroup;
	private GroupResourceStatus status;
	private String failureCause;

	public AssignedGroup(EnrichedGroup enrichedGroup, GroupResourceStatus status, String failureCause) {
		this.enrichedGroup = enrichedGroup;
		this.status = status;
		this.failureCause = failureCause;
	}

	public EnrichedGroup getEnrichedGroup() {
		return enrichedGroup;
	}

	public void setEnrichedGroup(EnrichedGroup enrichedGroup) {
		this.enrichedGroup = enrichedGroup;
	}

	public GroupResourceStatus getStatus() {
		return status;
	}

	public void setStatus(GroupResourceStatus status) {
		this.status = status;
	}

	public String getFailureCause() {
		return failureCause;
	}

	public void setFailureCause(String failureCause) {
		this.failureCause = failureCause;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AssignedGroup that = (AssignedGroup) o;
		return getEnrichedGroup().equals(that.getEnrichedGroup()) && getStatus() == that.getStatus();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getEnrichedGroup(), getStatus());
	}

	@Override
	public String toString() {
		return "AssignedGroup{" +
			"enrichedGroup=" + enrichedGroup +
			", status=" + status +
			", failureCause=" + failureCause +
			'}';
	}
}
