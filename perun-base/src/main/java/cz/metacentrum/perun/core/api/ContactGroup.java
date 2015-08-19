package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * Represents group of contacts for Facility (users, owners and groups)
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ContactGroup {
	private String contactGroupName;
	private Facility facility;
	private List<Group> groups;
	private List<Owner> owners;
	private List<RichUser> users;

	//empty constructor
	public ContactGroup() {
	}

	//basic constructor
	public ContactGroup(String contactGroupName, Facility facility) {
		this.contactGroupName = contactGroupName;
		this.facility = facility;
		this.groups = new ArrayList<>();
		this.owners = new ArrayList<>();
		this.users = new ArrayList<>();
	}

	//full constructor
	public ContactGroup(String contactGroupName, Facility facility, List<Group> groups, List<Owner> owners, List<RichUser> users) {
		this.contactGroupName = contactGroupName;
		this.facility = facility;
		this.groups = groups;
		this.owners = owners;
		this.users = users;
	}

	public String getContactGroupName() {
		return contactGroupName;
	}

	public void setContactGroupName(String contactGroupName) {
		this.contactGroupName = contactGroupName;
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
		if (!Objects.equals(this.contactGroupName, other.contactGroupName)) {
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

	public boolean equalsGroup(ContactGroup other) {
		if (other == null) {
			return false;
		}
		if (!Objects.equals(this.contactGroupName, other.contactGroupName)) {
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
		result = prime * result + ((contactGroupName == null) ? 0 : contactGroupName.hashCode());
		result = prime * result + ((users == null) ? 0 : users.hashCode());
		result = prime * result + ((groups == null) ? 0 : groups.hashCode());
		result = prime * result + ((owners == null) ? 0 : owners.hashCode());
		return result;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		return str.append("ContactGroup:[facility='").append(getFacility()).append("', contactGroupName='").append(getContactGroupName()).append("', groups='").append(getGroups()).append(
		  "', owners='").append(getOwners()).append("', users='").append(users).append("']").toString();
	}


}
