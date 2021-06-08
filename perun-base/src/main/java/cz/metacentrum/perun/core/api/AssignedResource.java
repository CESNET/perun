package cz.metacentrum.perun.core.api;

import java.util.Objects;

/**
 * Represents resource assigned to a group.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class AssignedResource {
	private EnrichedResource enrichedResource;
	private GroupResourceStatus status;

	public AssignedResource(EnrichedResource enrichedResource, GroupResourceStatus status) {
		this.enrichedResource = enrichedResource;
		this.status = status;
	}

	public EnrichedResource getEnrichedResource() {
		return enrichedResource;
	}

	public void setEnrichedResource(EnrichedResource enrichedResource) {
		this.enrichedResource = enrichedResource;
	}

	public GroupResourceStatus getStatus() {
		return status;
	}

	public void setStatus(GroupResourceStatus status) {
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AssignedResource that = (AssignedResource) o;
		return Objects.equals(getEnrichedResource(), that.getEnrichedResource()) && getStatus() == that.getStatus();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getEnrichedResource(), getStatus());
	}

	@Override
	public String toString() {
		return "AssignedResource{" +
			"enrichedResource=" + enrichedResource +
			", status=" + status +
			'}';
	}
}
