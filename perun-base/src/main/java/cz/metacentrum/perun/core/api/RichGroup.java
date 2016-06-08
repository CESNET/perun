package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Group with list of all its attributes.
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class RichGroup extends Group {

	private List<Attribute> groupAttributes;

	public RichGroup() {
	}

	public RichGroup(Group group, List<Attribute> attrs) {
		super(group.getId(), group.getName(), group.getDescription(),
				group.getCreatedAt(), group.getCreatedBy(),
				group.getModifiedAt(), group.getModifiedBy(),
				group.getParentGroupId(), group.getCreatedByUid(),
				group.getModifiedByUid());
		this.setVoId(group.getVoId());
		this.groupAttributes = attrs;
	}

	public List<Attribute> getAttributes() {
		return this.groupAttributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.groupAttributes = attributes;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(getClass().getSimpleName());
		ret.append(":[");
		ret.append("id='");
		ret.append(this.getId());
		ret.append("', parentGroupId='");
		ret.append(getParentGroupId());
		ret.append("', name='");
		ret.append(this.getName());
		ret.append("', shortName='");
		ret.append(this.getShortName());
		ret.append("', description='");
		ret.append(this.getDescription());
		ret.append("', voId='");
		ret.append(this.getVoId());
		ret.append("', groupAttributes='");
		ret.append(this.getAttributes());
		ret.append("']");
		return ret.toString();
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		String sGroupAttrs;
		List<String> nAttrs = new ArrayList<>();
		List<Attribute> oAttrs = this.getAttributes();
		if (oAttrs == null) {
			sGroupAttrs = "\\0";
		} else {
			for (Attribute attr : oAttrs) {
				nAttrs.add(attr.serializeToString());
			}
			sGroupAttrs = nAttrs.toString();
		}

		return str.append(this.getClass().getSimpleName()).append(":["
				).append("id=<").append(getId()).append(">"
				).append(", parentGroupId=<").append(getParentGroupId() == null ? "\\0" : getParentGroupId()).append(">"
				).append(", name=<").append(getName() == null ? "\\0" : BeansUtils.createEscaping(getName())).append(">"
				).append(", shortName=<").append(getShortName() == null ? "\\0" : BeansUtils.createEscaping(getShortName())).append(">"
				).append(", description=<").append(getDescription() == null ? "\\0" : BeansUtils.createEscaping(getDescription())).append(">"
				).append(", voId=<").append(getVoId()).append(">"
				).append(", groupAttributes=<").append(sGroupAttrs).append(">"
				).append(']').toString();
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 53 * hash + Objects.hashCode(this.groupAttributes);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RichGroup other = (RichGroup) obj;
		if (getId() != other.getId()) {
			return false;
		}
		if (groupAttributes == null) {
			if (other.getAttributes() != null) {
				return false;
			}
		} else if (!this.getAttributes().equals(other.getAttributes())) {
			return false;
		}
		return true;
	}
}
