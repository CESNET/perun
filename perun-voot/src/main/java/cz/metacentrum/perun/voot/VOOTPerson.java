package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.User;

import java.util.Arrays;

/**
 * Class defines Person encoded according to the OpenSocial Social Data Specification.
 * Attributes that are not part of specifiacation have namespace prefix 'voot_'.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 * @see <a href="http://opensocial-resources.googlecode.com/svn/spec/2.0.1/Social-Data.xml#Person">Social-Data-Person</a>
 */
public class VOOTPerson implements Comparable<VOOTPerson>{

	//id,displayName required
	private String id;
	private String displayName;
	private Email[] emails;

	/**
	 * VOOTPerson represents person encoded according to the OpenSocial Social Data Specification using in VOOT protocol.
	 *
	 * @param user      user
	 * @param emails    email adressrs of user
	 */
	public VOOTPerson(User user, Email[] emails){
		this.id = Integer.toString(user.getId());
		this.displayName = user.getDisplayName();
		this.emails = emails;
	}

	/**
	 * Return id of person.
	 *
	 * @return    id of person
	 */
	public String getId() {
		return id;
	}

	/**
	 * Return name suitable for display to end-users, e.g. Martin Malik.
	 *
	 * @return    full name of person
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Return email adresses of person.
	 *
	 * @return    email adresses
	 */
	public Email[] getEmails() {
		return emails;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 23;
		result = prime * result + (id != null ? id.hashCode() : 0);
		result = prime * result + (displayName != null ? displayName.hashCode() : 0);

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final VOOTPerson other = (VOOTPerson) obj;

		if ((id == null) ? (other.id != null) : !id.equals(other.id)) {
			return false;
		}

		if ((displayName == null) ? (other.displayName != null) : !displayName.equals(other.displayName)) {
			return false;
		}

		return true;
	}

	@Override
	public int compareTo(VOOTPerson other) {
		if(id.compareTo(other.getId()) > 0){
			return 1;
		}else if(id.compareTo(other.getId()) == 0){
			return 0;
		}else{
			return -1;
		}
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName()).append(":[")
				.append("id='").append(getId()).append("', ")
				.append("displayName='").append(getDisplayName()).append("', ")
				.append("emails='").append(Arrays.toString(getEmails())).append("']");
		return sb.toString();
	}
}
