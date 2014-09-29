package cz.metacentrum.perun.cabinet.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.UserExtSource;

/**
 * Class representing author. Author it's not stored in cabinet DB.
 * It's identified by his userId (~id) and always manually created from User.
 *
 * Other author's params are optional, therefore can be null or empty when not
 * requested by original SQL query.
 *
 * E.g. Authors used inside PublicationForGUI object usually contains
 * only base User's params like name. But authors used when "listing all of them"
 * contains also their logins, all authorships etc.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Author implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String firstName;
	private String lastName;
	private String middleName;
	private String titleBefore;
	private String titleAfter;

	/**
	 * List of logins in UserExtSources from Perun.
	 * !! This property must be filled manually !!
	 */
	private List<UserExtSource> logins = new ArrayList<UserExtSource>();

	/**
	 * Authorships related to this author.
	 * Provides authorships.getAuthorship(pubId).getCreatedBy() display for GUI.
	 * Used when displaying authors for some publication.
	 * !! This property must be filled manually !!
	 */
	private List<Authorship> authorships = new ArrayList<Authorship>();

	public Author() { }

	public Author(Integer id, String firstName, String lastName,
			String middleName, String titleBefore, String titleAfter) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleName = middleName;
		this.titleBefore = titleBefore;
		this.titleAfter = titleAfter;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public void setTitleAfter(String titleAfter) {
		this.titleAfter = titleAfter;
	}

	public void setLogins(List<UserExtSource> logins) {
		this.logins = logins;
	}

	public List<UserExtSource> getLogins() {
		return this.logins;
	}

	public List<Authorship> getAuthorships() {
		return authorships;
	}

	public void setAuthorships(List<Authorship> authorships) {
		this.authorships = authorships;
	}

	public String getBeanName() {
		return this.getClass().getSimpleName();
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

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":[id=").append(id).append(", firstName=").append(firstName).append(", lastName=").append(lastName).append(", displayName=").append(this.getDisplayName()).append(", logins=").append(logins).append(", authorships=").append(authorships).append("]").toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
			+ ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result
			+ ((middleName == null) ? 0 : middleName.hashCode());
		result = prime * result
			+ ((titleAfter == null) ? 0 : titleAfter.hashCode());
		result = prime * result
			+ ((titleBefore == null) ? 0 : titleBefore.hashCode());
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
		Author other = (Author) obj;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
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
		return true;
	}

}
