package cz.metacentrum.perun.core.api;

import java.util.Objects;

/**
 * Represents group-resource assignment with its status.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class GroupResourceAssignment {
	private Group group;
	private Resource resource;
	private GroupResourceStatus status;

	public GroupResourceAssignment(Group group, Resource resource, GroupResourceStatus status) {
		this.group = group;
		this.resource = resource;
		this.status = status;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
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
		GroupResourceAssignment that = (GroupResourceAssignment) o;
		return Objects.equals(getGroup(), that.getGroup()) && Objects.equals(getResource(), that.getResource())
			&& getStatus() == that.getStatus();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getGroup(), getResource(), getStatus());
	}

	@Override
	public String toString() {
		return "GroupResourceAssignment{" +
			"group=" + group +
			", resource=" + resource +
			", status=" + status +
			'}';
	}
}
