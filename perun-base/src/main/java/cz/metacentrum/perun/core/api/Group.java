package cz.metacentrum.perun.core.api;

/**
 * Group entity.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */

public class Group extends Auditable implements Comparable<PerunBean> {
	private int voId;
	private Integer parentGroupId;
	private String name;
	private String description;


	/**
	 * Constructor.
	 */
	public Group() {
	}

	/**
	 * Constructor.
	 *
	 * @param name        name of the Group
	 * @param description description the the group
	 */
	public Group(String name, String description)  {
		this.name = name;
		this.description = description;
	}

	public Group(String name, String description, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid){
		super(createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.description = description;
	}

	public Group(int id, String name, String description, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid){
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.description = description;
	}

	@Deprecated
	public Group(int id, String name, String description, String createdAt, String createdBy, String modifiedAt, String modifiedBy){
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, null, null);
		this.name = name;
		this.description = description;

	}

	public Group(Integer parentGroupId, String name, String description) {
		this(name, description);
		this.parentGroupId = parentGroupId;
	}

	public Group(String name, String description, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer parentGroupId, Integer createdByUid, Integer modifiedByUid){
		super(createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.description = description;
		this.parentGroupId = parentGroupId;
	}

	public Group(int id, String name, String description, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer parentGroupId, Integer createdByUid, Integer modifiedByUid){
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.description = description;
		this.parentGroupId = parentGroupId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;

	}

	public Integer getParentGroupId() {
		return parentGroupId;
	}

	public void setParentGroupId(Integer parentGroupId) {
		this.parentGroupId = parentGroupId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getVoId() {
		return voId;
	}

	public void setVoId(int voId) {
		this.voId = voId;
	}


	public String getShortName() {
		if(name == null) return null;
		return name.substring(name.lastIndexOf(':')+1);
	}

	public void setShortName(String shortName) {
		if(name == null) {
			name = shortName;
		} else {
			this.name = name.substring(0, name.lastIndexOf(':') + 1) + shortName;
		}
	}

	@Override
	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof Group) {
			Group group = (Group) perunBean;
			if (this.getName() == null && group.getName() != null) return -1;
			if (group.getName() == null && this.getName() != null) return 1;
			if (this.getName() == null && group.getName() == null) return 0;
			return this.getName().compareToIgnoreCase(group.getName());
		} else {
			return (this.getId() - perunBean.getId());
		}
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", parentGroupId=<").append(getParentGroupId() == null ? "\\0" : getParentGroupId()).append(">").append(
			", name=<").append(getName() == null ? "\\0" : BeansUtils.createEscaping(getName())).append(">").append(
			", shortName=<").append(getShortName() == null ? "\\0" : BeansUtils.createEscaping(getShortName())).append(">").append(
			", description=<").append(getDescription() == null ? "\\0" : BeansUtils.createEscaping(getDescription())).append(">").append(
			", voId=<").append(getVoId()).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(getClass().getSimpleName());
		ret.append(":[");
		ret.append("id='");
		ret.append(this.getId());
		ret.append("', parentGroupId='");
		ret.append(parentGroupId);
		ret.append("', name='");
		ret.append(name);
		ret.append("', shortName='");
		ret.append(this.getShortName());
		ret.append("', description='");
		ret.append(description);
		ret.append("', voId='");
		ret.append(voId);
		ret.append("']");
		return ret.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Group other = (Group) obj;
		if (this.getId() != other.getId()) {
			return false;
		}
		if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
