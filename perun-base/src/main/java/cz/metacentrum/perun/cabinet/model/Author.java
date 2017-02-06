package cz.metacentrum.perun.cabinet.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunBean;

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
public class Author extends PerunBean {

	private String firstName;
	private String lastName;
	private String middleName;
	private String titleBefore;
	private String titleAfter;

	private List<Attribute> attributes = new ArrayList<>();

	/**
	 * Authorships related to this author.
	 * Provides authorships.getAuthorship(pubId).getCreatedBy() display for GUI.
	 * Used when displaying authors for some publication.
	 * !! This property must be filled manually !!
	 */
	private List<Authorship> authorships = new ArrayList<Authorship>();

	public Author() { }

	public Author(int id, String firstName, String lastName,
			String middleName, String titleBefore, String titleAfter) {
		super(id);
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleName = middleName;
		this.titleBefore = titleBefore;
		this.titleAfter = titleAfter;
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

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public List<Attribute> getAttributes() {
		return this.attributes;
	}

	public List<Authorship> getAuthorships() {
		return authorships;
	}

	public void setAuthorships(List<Authorship> authorships) {
		this.authorships = authorships;
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
		return str.append(getClass().getSimpleName()).append(":[id=").append(getId()).append(", firstName=").append(firstName).append(", lastName=").append(lastName).append(", displayName=").append(this.getDisplayName()).append(", attributes=").append(attributes).append(", authorships=").append(authorships).append("]").toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Author)) return false;
		if (!super.equals(o)) return false;
		Author author = (Author) o;
		return Objects.equals(firstName, author.firstName) &&
				Objects.equals(lastName, author.lastName) &&
				Objects.equals(middleName, author.middleName) &&
				Objects.equals(titleBefore, author.titleBefore) &&
				Objects.equals(titleAfter, author.titleAfter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), firstName, lastName, middleName, titleBefore, titleAfter);
	}

}
