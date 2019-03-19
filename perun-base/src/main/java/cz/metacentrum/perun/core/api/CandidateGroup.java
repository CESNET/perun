package cz.metacentrum.perun.core.api;

import java.util.Objects;

/**
 * Group obtained from an extSource with the name of its parent in the external source.
 * It can be then created as a group of a virtual organization in Perun.
 *
 * @date 8/30/17.
 * @author Peter Balcirak peter.balcirak@gmail.com
 */
public class CandidateGroup extends Auditable {

	private ExtSource extSource;
	private String parentGroupName;
	private Group group;

	public CandidateGroup() {
		this.group = new Group();
	}

	public ExtSource getExtSource() {
		return extSource;
	}

	public void setExtSource(ExtSource extSource) {
		this.extSource = extSource;
	}

	public String getParentGroupName() {
		return parentGroupName;
	}

	public void setParentGroupName(String parentGroupName) {
		this.parentGroupName = parentGroupName;
	}

	public Group asGroup() {
		return group;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CandidateGroup that = (CandidateGroup) o;
		return Objects.equals(getExtSource(), that.getExtSource()) &&
				Objects.equals(getParentGroupName(), that.getParentGroupName()) &&
				Objects.equals(asGroup(), that.asGroup());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getExtSource(), getParentGroupName(), asGroup());
	}

	@Override
	public String toString() {
		return "CandidateGroup{" +
				"extSource=" + extSource +
				", parentGroupName='" + parentGroupName + '\'' +
				", group=" + group +
				'}';
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName())
				.append(":[extSource=<")
				.append(getExtSource() == null ? "\\0" : getExtSource().serializeToString())
				.append(">, parentGroupName=<")
				.append(getParentGroupName())
				.append(">, group=<")
				.append(asGroup() == null ? "\\0" : asGroup().serializeToString())
				.append(">]").toString();
	}
}
