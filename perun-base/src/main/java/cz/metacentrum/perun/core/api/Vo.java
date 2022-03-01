package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Vo entity.
 */
public class Vo extends Auditable implements Comparable<PerunBean> {
	private String name;
	private String shortName;
	private boolean memberVosEnabled = false; // can other VOs be member organizations of this VO?

	public Vo() {
	}

	public Vo(int id, String name, String shortName) {
		super(id);
		if (name == null)  throw new InternalErrorException(new NullPointerException("name is null"));
		if (shortName == null)  throw new InternalErrorException(new NullPointerException("shortName is null"));
		this.name = name;
		this.shortName = shortName;

	}

	public Vo(int id, String name, String shortName, boolean memberVosEnabled) {
		this(id, name, shortName);
		this.memberVosEnabled = memberVosEnabled;
	}

	@Deprecated
	public Vo(int id, String name, String shortName, String createdAt, String createdBy, String modifiedAt, String modifiedBy) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, null, null);
		if (name == null)  throw new InternalErrorException(new NullPointerException("name is null"));
		if (shortName == null)  throw new InternalErrorException(new NullPointerException("shortName is null"));
		this.name = name;
		this.shortName = shortName;
	}

	public Vo(int id, String name, String shortName, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		if (name == null)  throw new InternalErrorException(new NullPointerException("name is null"));
		if (shortName == null)  throw new InternalErrorException(new NullPointerException("shortName is null"));
		this.name = name;
		this.shortName = shortName;
	}

	public Vo(int id, String name, String shortName, boolean memberVosEnabled, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		this(id, name, shortName, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.memberVosEnabled = memberVosEnabled;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setName(String name) {
		if (name == null)  throw new InternalErrorException(new NullPointerException("name is null"));
		this.name = name;
	}

	public void setShortName(String shortName) {
		if (shortName == null)  throw new InternalErrorException(new NullPointerException("shortName is null"));
		this.shortName = shortName;
	}

	public boolean isMemberVosEnabled() {
		return memberVosEnabled;
	}

	public void setMemberVosEnabled(boolean memberVosEnabled) {
		this.memberVosEnabled = memberVosEnabled;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", name=<").append(getName() == null ? "\\0" : BeansUtils.createEscaping(getName())).append(">").append(
			", shortName=<").append(getShortName() == null ? "\\0" : BeansUtils.createEscaping(getShortName())).append(">").append(
			", memberVosEnabled=<").append(isMemberVosEnabled()).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id='").append(this.getId()).append('\'').append(
			", name='").append(name).append('\'').append(
			", shortName='").append(shortName).append('\'').append(
			", memberVosEnabled='").append(memberVosEnabled).append('\'').append(
			']').toString();
	}

	@Override
	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof Vo) {
			Vo vo = (Vo) perunBean;
			if (this.getName() == null && vo.getName() != null) return -1;
			if (vo.getName() == null && this.getName() != null) return 1;
			if (this.getName() == null && vo.getName() == null) return 0;
			return this.getName().compareToIgnoreCase(vo.getName());
		} else {
			return (this.getId() - perunBean.getId());
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + this.getId();
		hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 53 * hash + (this.shortName != null ? this.shortName.hashCode() : 0);
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
		final Vo other = (Vo) obj;
		if (this.getId() != other.getId()) {
			return false;
		}
		if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
			return false;
		}
		if ((this.shortName == null) ? (other.shortName != null) : !this.shortName.equals(other.shortName)) {
			return false;
		}
		return true;
	}
}
