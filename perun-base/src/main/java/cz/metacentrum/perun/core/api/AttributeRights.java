package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Class represents rights of an attribute. The rights relates to an Attribute and
 * a Role. Object ActionType represents one right (READ or WRITE).
 *
 * @author Jiří Mauritz
 */
public class AttributeRights {

	/**
	 * ID of the attribute.
	 */
	private int attributeId;

	/**
	 * Role, that specifies the users, who have the rights upon the attribute.
	 */
	private Role role;

	/**
	 * List of all rights the role has upon the attribute.
	 */
	private List<ActionType> rights;

	public AttributeRights() {}

	public AttributeRights(int attributeId, Role role, List<ActionType> rights) {
		this.attributeId = attributeId;
		this.role = role;
		if (rights == null) {
			this.rights = new ArrayList<ActionType>();
		} else {
			this.rights = rights;
		}
	}

	public int getAttributeId() {
		return attributeId;
	}

	public Role getRole() {
		return role;
	}

	public List<ActionType> getRights() {
		return rights;
	}

	public void setAttributeId(int attributeId) {
		this.attributeId = attributeId;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public void setRights(List<ActionType> rights) {
		this.rights = rights;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 61 * hash + this.attributeId;
		hash = 61 * hash + (this.role != null ? this.role.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AttributeRights)) {
			return false;
		}
		final AttributeRights other = (AttributeRights) obj;
		if (this.attributeId != other.attributeId) {
			return false;
		}
		if (this.role != other.role) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append("AttributeRights{").append("attributeId=").append(attributeId).append(", role=").append(role).append(", rights=").append(rights).append('}').toString();
	}

}
