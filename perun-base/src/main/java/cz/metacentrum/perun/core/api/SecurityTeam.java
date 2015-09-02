package cz.metacentrum.perun.core.api;

/**
 * Security team entity
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SecurityTeam extends Auditable implements Comparable<SecurityTeam> {
	private String name;
	private String description;

	public SecurityTeam() {
	}

	public SecurityTeam(String name) {
		super();
		this.name = name;
	}

	public SecurityTeam(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	public SecurityTeam(int id, String name, String description) {
		super(id);
		this.name = name;
		this.description = description;
	}

	public SecurityTeam(int id, String name, String description, String createdAt, String createdBy, String modifiedAt,
	                    String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
				"id=<").append(getId()).append(">").append(
				", name=<").append(getName() == null ? "\\0" : BeansUtils.createEscaping(getName())).append(">").append(
				", description=<").append(getDescription() == null ? "\\0" : BeansUtils.createEscaping(getDescription())).append(">").append(
				']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
				"id='").append(this.getId()).append('\'').append(
				", name='").append(name).append('\'').append(
				", description='").append(description).append('\'').append(
				']').toString();
	}

	public int compareTo(SecurityTeam securityTeam) {
		if (securityTeam == null) {
			throw new NullPointerException();
		}
		return this.getName().compareTo(securityTeam.getName());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + this.getId();
		hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 53 * hash + (this.description != null ? this.description.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SecurityTeam other = (SecurityTeam) obj;
		if (this.getId() != other.getId()) {
			return false;
		}
		if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
			return false;
		}
		if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
			return false;
		}
		return true;
	}
}
