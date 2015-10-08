package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * This class represents definition of attribute. All attributes comes from some definition.
 * Attribute definition is attribute without connection to some object.
 * TODO
 *
 *
 *
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public class AttributeDefinition extends Auditable implements Comparable<PerunBean> {

	/**
	 * Attribute name, <strong>excluding</strong> the whole namespace.
	 */
	private String friendlyName;

	/**
	 * Attribute namespace, including the whole namespace, will be also used by the perl scripts.
	 */
	private String namespace;

	/**
	 * Attribute description
	 */
	private String description;

	/**
	 * Type of attribute's value. It's a name of java class. "Java.lang.String" for expample. (To get this use something like <em>String.class.getName()</em>)
	 */
	private String type;

	/**
	 * Attribute name, that is displayed in GUI.
	 */
	private String displayName;

	/**
	 * If the user in session has also right to write this attribute
	 */
	private boolean writable;

	public AttributeDefinition() {
		this.writable = false;
	}

	/**
	 * Copy constructor. New attribute will be exactly the same as attribute from parameter.
	 *
	 * @param attributeDefinition attribute to copy
	 */
	public AttributeDefinition(AttributeDefinition attributeDefinition) {
		super(attributeDefinition.getId(), attributeDefinition.getCreatedAt(), attributeDefinition.getCreatedBy(),
				attributeDefinition.getModifiedAt(), attributeDefinition.getModifiedBy(), attributeDefinition.getCreatedByUid(), attributeDefinition.getModifiedByUid());
		this.friendlyName = attributeDefinition.getFriendlyName();
		this.namespace = attributeDefinition.getNamespace();
		this.description = attributeDefinition.getDescription();
		this.type = attributeDefinition.getType();
		this.displayName = attributeDefinition.getDisplayName();
		this.writable = attributeDefinition.getWritable();
	}


	/**
	 * Returns the whole attribute name including namespace
	 *
	 * @return attribute namespace + friendly name
	 */
	public String getName() {
		return namespace + ":" + friendlyName;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getType() {
		//return this.value.getClass().getName();
		return type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean getWritable() {
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	/**
	 * Get the first part from the friendlyName if the friendlyName contains parameter friendlyName = name:param. Otherwise returns firendlyName.
	 */
	public String getBaseFriendlyName() {
		String[] friendlyNames = friendlyName.split(":");

		return friendlyNames[0];
	}

	/**
	 * Returns parameter of the friendly name, e.g. fiendlyName=name:param.
	 */
	public String getFriendlyNameParameter() {
		int index = friendlyName.indexOf(':');

		if (index != -1 && index < friendlyName.length()-1) {
			return friendlyName.substring(index+1);
		} else return "";
	}

	/**
	 * Returns name of the entity from the attribute name (urn:perun:[entity]:attribute-def). e.g. member, facility, user, ...
	 */
	public String getEntity() {
		if (namespace != null && namespace.length() > 0) {
			String pattern = "urn:perun:(.+?):.+";
			return namespace.replaceAll(pattern, "$1");
		} else return "";
	}

	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof AttributeDefinition) {
			AttributeDefinition attrDef = (AttributeDefinition) perunBean;
			if (this.getFriendlyName()== null && attrDef.getFriendlyName() != null) return -1;
			if (attrDef.getFriendlyName() == null && this.getFriendlyName() != null) return 1;
			if (this.getFriendlyName() == null && attrDef.getFriendlyName() == null) return 0;
			return this.getFriendlyName().compareToIgnoreCase(attrDef.getFriendlyName());
		} else {
			return (this.getId() - perunBean.getId());
		}
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + getId();
		hash = 53 * hash + (friendlyName == null ? 0 : friendlyName.hashCode());
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;

		if (!(obj instanceof AttributeDefinition)) return false;

		final AttributeDefinition other = (AttributeDefinition) obj;

		if (this.getId() != other.getId()) return false;
		if(this.friendlyName == null ? other.friendlyName != null : !this.friendlyName.equals(other.friendlyName)) return false;

		return true;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
				"id=<").append(getId()).append(">").append(
				", friendlyName=<").append(getFriendlyName() == null ? "\\0" : BeansUtils.createEscaping(getFriendlyName())).append(">").append(
				", namespace=<").append(getNamespace() == null ? "\\0" : BeansUtils.createEscaping(getNamespace())).append(">").append(
				", type=<").append(getType() == null ? "\\0" : BeansUtils.createEscaping(getType())).append(">").append(
				']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
				"id='").append(getId()).append('\'').append(
				", friendlyName='").append(friendlyName).append('\'').append(
				", namespace='").append(namespace).append('\'').append(
				", type='").append(type).append('\'').append(
				']').toString();
	}
}
