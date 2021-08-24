package cz.metacentrum.perun.core.api;

import java.util.Objects;

/**
 * Represents member of group assigned to resource
 *
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class AssignedMember {
	private Member member;
	private GroupResourceStatus status;

	public AssignedMember(Member member, GroupResourceStatus status) {
		this.member = member;
		this.status = status;
	}


	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
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
		AssignedMember that = (AssignedMember) o;
		return Objects.equals(getMember(), that.getMember()) && getStatus() == that.getStatus();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getMember(), getStatus());
	}

	@Override
	public String toString() {
		return "AssignedMember{" +
			"member= " + member +
			", status=" + status +
			'}';
	}
}
