package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Represents group of contacts for Facility (users, owners and groups)
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ContactGroup implements Comparable<ContactGroup>{
	private String name;
	private Facility facility;
	private List<Group> groups;
	private List<Owner> owners;
	private List<RichUser> users;

	//empty constructor
	public ContactGroup() {
	}

	//basic constructor
	public ContactGroup(String name, Facility facility) {
		this.name = name;
		this.facility = facility;
		this.groups = new ArrayList<>();
		this.owners = new ArrayList<>();
		this.users = new ArrayList<>();
	}

	//full constructor
	public ContactGroup(String name, Facility facility, List<Group> groups, List<Owner> owners, List<RichUser> users) {
		this.name = name;
		this.facility = facility;
		this.groups = groups;
		this.owners = owners;
		this.users = users;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public List<Owner> getOwners() {
		return owners;
	}

	public void setOwners(List<Owner> owners) {
		this.owners = owners;
	}

	public List<RichUser> getUsers() {
		return users;
	}

	public void setUsers(List<RichUser> users) {
		this.users = users;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ContactGroup other = (ContactGroup) obj;
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		if (!Objects.equals(this.facility, other.facility)) {
			return false;
		}
		if (!Objects.equals(this.groups, other.groups)) {
			return false;
		}
		if (!Objects.equals(this.owners, other.owners)) {
			return false;
		}
		if (!Objects.equals(this.users, other.users)) {
			return false;
		}
		return true;
	}

	/**
	 * Check if ContactGroup is equals in base params like name and facility.
	 * It is used when merging ContactGroups selected from DB together
	 * and filling connected owners, groups and users.
	 *
	 * @param other Other ContactGroup to compare with
	 * @return TRUE if both equals in name and facility
	 */
	public boolean equalsGroup(ContactGroup other) {
		if (other == null) {
			return false;
		}
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		if (!Objects.equals(this.facility, other.facility)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((facility == null) ? 0 : facility.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((users == null) ? 0 : users.hashCode());
		result = prime * result + ((groups == null) ? 0 : groups.hashCode());
		result = prime * result + ((owners == null) ? 0 : owners.hashCode());
		return result;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append("ContactGroup:[facilityID='").append(getFacility().getId()).append("', name='").append(getName()).append("', groupsIDs='").append(BeansUtils.getIDsOfPerunBeans(getGroups())).append(
		  "', ownersIDs='").append(BeansUtils.getIDsOfPerunBeans(getOwners())).append("', usersIDs='").append(BeansUtils.getIDsOfPerunBeans(users)).append("']").toString();
	}

	@Override
	public int compareTo(ContactGroup contactGroup) {
		if(contactGroup == null) throw new NullPointerException("PerunBean to compare with is null.");
		if (this.getName() == null && contactGroup.getName() != null) return -1;
		if (contactGroup.getName() == null && this.getName() != null) return 1;
		if (this.getName() == null && contactGroup.getName() == null) return 0;
		return this.getName().compareToIgnoreCase(contactGroup.getName());
	}
}
