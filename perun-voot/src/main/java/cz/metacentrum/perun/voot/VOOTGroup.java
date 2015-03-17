package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.Group;

/**
 * Class defines group encoded according to the OpenSocial Social Data Specification.
 * Attributes that are not part of specifiacation have namespace prefix 'voot_'.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 * @see <a href="http://opensocial-resources.googlecode.com/svn/spec/2.0.1/Social-Data.xml#Group">Social-Data-Group</a>
 */
public class VOOTGroup implements Comparable<VOOTGroup>{

	//id, title required
	private String id;
	private String title;
	private String description;
	private String voot_membership_role;

	/**
	 * VOOTGroup represents group encoded according to the OpenSocial Social Data Specification using in VOOT protocol.
	 *
	 * @param group                   group
	 * @param voShortName             short name of vo
	 * @param voot_membership_role    membership role of person in group
	 */
	public VOOTGroup(Group group, String voShortName, String voot_membership_role) throws VOOTException{

		//group must be in vo
		if(voShortName == null) throw new VOOTException("internal_error_exception");

		this.id = voShortName.concat(":").concat(group.getName());
		this.title = group.getName();
		this.description = group.getDescription();
		this.voot_membership_role = voot_membership_role;
	}

	/**
	 * Return id of group, which is consist of short name of vo, short name of parents group and short name of current group, e.g. 'vo1:group1:group2'.
	 * 'vo1' is short name of virtual organisation
	 * 'group1' is short name of parent group
	 * 'group2' is short name of current group
	 *
	 * @return    id of group
	 */
	public String getId() {
		return id;
	}

	/**
	 * Return title of group, which is consist of short name of parents group and short name of current group, e.g. 'group1:group2'.
	 * 'group1' is short name of parent group
	 * 'group2' is short name of current group
	 *
	 * @return    title of group
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Return description of group.
	 *
	 * @return    description of group
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Return membership role of person in current group, e.g. 'admin', 'member'.
	 *
	 * @return    membership role of person in group
	 */
	public String getVoot_membership_role() {
		return voot_membership_role;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 23;
		result = prime * result + (id != null ? id.hashCode() : 0);
		result = prime * result + (title != null ? title.hashCode() : 0);
		result = prime * result + (description != null ? description.hashCode() : 0);
		result = prime * result + (voot_membership_role != null ? voot_membership_role.hashCode() : 0);

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

		final VOOTGroup other = (VOOTGroup) obj;

		if ((id == null) ? (other.id != null) : !id.equals(other.id)) {
			return false;
		}

		if ((title == null) ? (other.title != null) : !title.equals(other.title)) {
			return false;
		}

		if ((description == null) ? (other.description != null) : !description.equals(other.description)) {
			return false;
		}

		if ((voot_membership_role == null) ? (other.voot_membership_role != null) : !voot_membership_role.equals(other.voot_membership_role)) {
			return false;
		}

		return true;
	}

	@Override
	public int compareTo(VOOTGroup other) {
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
		sb.append(getClass().getSimpleName()).append(":[")
				.append("id='").append(getId()).append("', ")
				.append("title='").append(getTitle()).append("', ")
				.append("description='").append(getDescription()).append("', ")
				.append("voot_membership_role='").append(getVoot_membership_role()).append("']");
		return sb.toString();
	}
}
