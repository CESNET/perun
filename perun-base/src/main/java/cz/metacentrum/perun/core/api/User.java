package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Represents user of some source.
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 * @author Martin Kuba
 */
public class User extends Auditable implements Comparable<PerunBean> {

	protected String firstName;
	protected String lastName;
	protected String middleName;
	protected String titleBefore;
	protected String titleAfter;
	protected boolean serviceUser = false;
	protected boolean sponsoredUser = false;

	public User() {
		super();
	}

	public User(int id, String firstName, String lastName, String middleName, String titleBefore, String titleAfter) {
		super(id);
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleName = middleName;
		this.titleBefore = titleBefore;
		this.titleAfter = titleAfter;
	}

	public User(int id, String firstName, String lastName, String middleName, String titleBefore, String titleAfter, boolean serviceUser, boolean sponsoredUser) {
		this(id, firstName, lastName, middleName, titleBefore, titleAfter);
		this.serviceUser = serviceUser;
		this.sponsoredUser = sponsoredUser;
	}

	public User(int id, String firstName, String lastName, String middleName, String titleBefore, String titleAfter,
			String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleName = middleName;
		this.titleAfter = titleAfter;
		this.titleBefore = titleBefore;
	}

	public User(int id, String firstName, String lastName, String middleName, String titleBefore, String titleAfter,
			String createdAt, String createdBy, String modifiedAt, String modifiedBy, boolean serviceUser, boolean sponsoredUser, Integer createdByUid, Integer modifiedByUid) {
		this(id, firstName, lastName, middleName, titleBefore, titleAfter, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.serviceUser = serviceUser;
		this.sponsoredUser = sponsoredUser;
	}


	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getTitleBefore() {
		return titleBefore;
	}

	public void setTitleBefore(String titleBefore) {
		this.titleBefore = titleBefore;
	}

	public String getTitleAfter() {
		return titleAfter;
	}

	public String getCommonName() {
		String name = "";
		if (firstName != null) name = firstName;
		if (middleName != null) {
			if (name.length() != 0) name += " ";
			name += middleName;
		}
		if (lastName != null) {
			if (name.length() != 0) name += " ";
			name += lastName;
		}
		return name;
	}

	public String getDisplayName() {
		String name = "";
		if (titleBefore != null) name = titleBefore;
		if (firstName != null) {
			if (name.length() != 0) name += " ";
			name += firstName;
		}
		if (middleName != null) {
			if (name.length() != 0) name += " ";
			name += middleName;
		}
		if (lastName != null) {
			if (name.length() != 0) name += " ";
			name += lastName;
		}
		if (titleAfter != null) {
			if (name.length() != 0) name += " ";
			name += titleAfter;
		}
		return name;
	}

	public void setTitleAfter(String titleAfter) {
		this.titleAfter = titleAfter;
	}

	public boolean isServiceUser() {
		return serviceUser;
	}

	public void setServiceUser(boolean serviceUser) {
		this.serviceUser = serviceUser;
	}

	public boolean isSponsoredUser() {
		return sponsoredUser;
	}

	public void setSponsoredUser(boolean sponsoredUser) {
		this.sponsoredUser = sponsoredUser;
	}

	public boolean isSpecificUser() {
		return isServiceUser() || isSponsoredUser();
	}

	public SpecificUserType getMajorSpecificType() {
		if(isServiceUser()) return SpecificUserType.SERVICE;
		else if(isSponsoredUser()) return SpecificUserType.SPONSORED;
		else return SpecificUserType.NORMAL;
	}

	/**
	 * Compare this object with another perunBean.
	 *
	 * If the perunBean is User object, compare them by LastName, then FirstName and then Id
	 *
	 * @see Comparable#compareTo(Object)
	 * @param perunBean some perunBean object or User
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
	 */
	@Override
	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof User) {
			User user = (User) perunBean;
			int compare;
			//Compare on last Name
			if (this.getLastName() == null && user.getLastName() != null) compare = -1;
			else if (user.getLastName() == null && this.getLastName() != null) compare = 1;
			else if (this.getLastName() == null && user.getLastName() == null) compare = 0;
			else compare = this.getLastName().compareToIgnoreCase(user.getLastName());
			if(compare != 0) return compare;
			//Compare on first Name if not
			if (this.getFirstName() == null && user.getFirstName() != null) compare = -1;
			else if (user.getFirstName() == null && this.getFirstName() != null) compare = 1;
			else if (this.getFirstName()== null && user.getFirstName() == null) compare = 0;
			else compare = this.getFirstName().compareToIgnoreCase(user.getFirstName());
			if(compare != 0) return compare;
			//Compare to id if not
			return (this.getId() - perunBean.getId());
		} else {
			return (this.getId() - perunBean.getId());
		}
	}

	/**
	 * Compares Strings and handles null values.
	 * @param s1 string or null
	 * @param s2 string or null
	 * @return compare of the two strings
	 */
	private int compare(String s1,String s2) {
		if (s1==null) s1 = "";
		if (s2==null) s2 = "";
		return s1.compareTo(s2);
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", titleBefore=<").append(getTitleBefore() == null ? "\\0" : BeansUtils.createEscaping(getTitleBefore())).append(">").append(
			", firstName=<").append(getFirstName() == null ? "\\0" : BeansUtils.createEscaping(getFirstName())).append(">").append(
			", lastName=<").append(getLastName() == null ? "\\0" : BeansUtils.createEscaping(getLastName())).append(">").append(
			", middleName=<").append(getMiddleName() == null ? "\\0" : BeansUtils.createEscaping(getMiddleName())).append(">").append(
			", titleAfter=<").append(getTitleAfter() == null ? "\\0" : BeansUtils.createEscaping(getTitleAfter())).append(">").append(
			", serviceAccount=<").append(isServiceUser()).append(">").append(
			", sponsoredAccount=<").append(isSponsoredUser()).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(getClass().getSimpleName());
		ret.append(":[id='");
		ret.append(getId());
		ret.append("', titleBefore='");
		ret.append(titleBefore);
		ret.append("', firstName='");
		ret.append(firstName);
		ret.append("', lastName='");
		ret.append(lastName);
		ret.append("', middleName='");
		ret.append(middleName);
		ret.append("', titleAfter='");
		ret.append(titleAfter);
		ret.append("', serviceAccount='");
		ret.append(serviceUser);
		ret.append("', sponsoredAccount='");
		ret.append(sponsoredUser);
		ret.append("']");

		return ret.toString();

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + getId();
		result = prime * result
			+ ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result
			+ ((middleName == null) ? 0 : middleName.hashCode());
		result = prime * result
			+ ((titleAfter == null) ? 0 : titleAfter.hashCode());
		result = prime * result
			+ ((titleBefore == null) ? 0 : titleBefore.hashCode());
		result = prime * result
			+ ((serviceUser ? 1 : 2));
		result = prime * result
			+ ((sponsoredUser ? 1 : 2));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (getId() != other.getId())
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (middleName == null) {
			if (other.middleName != null)
				return false;
		} else if (!middleName.equals(other.middleName))
			return false;
		if (titleAfter == null) {
			if (other.titleAfter != null)
				return false;
		} else if (!titleAfter.equals(other.titleAfter))
			return false;
		if (titleBefore == null) {
			if (other.titleBefore != null)
				return false;
		} else if (!titleBefore.equals(other.titleBefore))
			return false;
		else if (serviceUser != other.serviceUser)
			return false;
		else if (sponsoredUser != other.sponsoredUser)
			return false;
		return true;
	}
}
