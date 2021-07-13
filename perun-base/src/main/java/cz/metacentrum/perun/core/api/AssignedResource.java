package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Represents resource assigned to a group.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class AssignedResource {
	private EnrichedResource enrichedResource;
	private GroupResourceStatus status;
	private Facility facility;
	private List<ResourceTag> resourceTags;

	public AssignedResource(EnrichedResource enrichedResource, GroupResourceStatus status, Facility facility) {
		this.enrichedResource = enrichedResource;
		this.status = status;
		this.facility = facility;
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

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public List<ResourceTag> getResourceTags() {
		return resourceTags;
	}

	public void setResourceTags(List<ResourceTag> resourceTags) {
		this.resourceTags = resourceTags;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AssignedResource that = (AssignedResource) o;
		return Objects.equals(getEnrichedResource(), that.getEnrichedResource()) &&
			getStatus() == that.getStatus() && Objects.equals(getFacility(), that.getFacility());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getEnrichedResource(), getStatus(), getFacility());
	}

	@Override
	public String toString() {
		return "AssignedResource{" +
			"enrichedResource=" + enrichedResource +
			", status=" + status +
			", facility=" + facility +
			", resourceTags=" + resourceTags +
			'}';
	}
}
