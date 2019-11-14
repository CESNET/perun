package cz.metacentrum.perun.core.api;

import java.util.HashMap;
import java.util.Map;

/**
 * External Source Bean
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 */
public class ExtSource extends Auditable implements Comparable<PerunBean>{

	private String name;
	private String type;

	public ExtSource() {
		super();
	}

	public ExtSource(int id, String name, String type) {
		super(id);
		this.name = name;
		this.type = type;
	}

	public ExtSource(int id, String name, String type, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.type = type;
	}

	public ExtSource(String name) {
		this();
		this.name = name;
	}

	public ExtSource(String name, String type) {
		this(name);
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", name=<").append(getName() == null ? "\\0" : BeansUtils.createEscaping(getName())).append(">").append(
			", type=<").append(getType() == null ? "\\0" : BeansUtils.createEscaping(getType())).append(">").append(
			']').toString();
	}

	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id='").append(getId()).append('\'').append(
			", name='").append(name).append('\'').append(
			", type='").append(type).append('\'').append(
			']').toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getId();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/*
	 * Overriding this, since implementation of each ExtSource returned their class name
	 * instead of required unified "ExtSource" value.
	 */
	@Override
	public String getBeanName() {
		return ExtSource.class.getSimpleName();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		// obj can be ExtSourceSql or ExtSourceLdap or whathever
		if (!(obj instanceof ExtSource))	return false;

		ExtSource other = (ExtSource) obj;
		if (getId() != other.getId())	return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (type == null) {
			if (other.type != null)	return false;
		} else if (!type.equals(other.type)) return false;
		return true;
	}

	@Override
	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof ExtSource) {
			ExtSource extSource = (ExtSource) perunBean;
			int compare;
			//Compare on last Name
			if (this.getName()== null && extSource.getName() != null) compare = -1;
			else if (extSource.getName() == null && this.getName() != null) compare = 1;
			else if (this.getName() == null && extSource.getName() == null) compare = 0;
			else compare = this.getName().compareToIgnoreCase(extSource.getName());
			if(compare != 0) return compare;
			//Compare on type
			if (this.getType()== null && extSource.getType() != null) compare = -1;
			else if (extSource.getType() == null && this.getType() != null) compare = 1;
			else if (this.getType()== null && extSource.getType() == null) compare = 0;
			else compare = this.getType().compareToIgnoreCase(extSource.getType());
			if(compare != 0) return compare;
			//Compare to id if not
			return (this.getId() - perunBean.getId());
		} else {
			return (this.getId() - perunBean.getId());
		}
	}
}
