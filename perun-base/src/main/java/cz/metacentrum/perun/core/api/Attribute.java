package cz.metacentrum.perun.core.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * This class represents attribute (with value) of some object (VO, member).
 * TODO
 *
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
@SuppressWarnings({"RedundantIfStatement", "SimplifiableIfStatement"})
public class Attribute extends AttributeDefinition implements Serializable {

	private final static Logger log = LoggerFactory.getLogger(Attribute.class);
	/**
	 * Value of the attribute, can be Map, List, String, Integer, Boolean...
	 */
	private Object value;

	/**
	 * Attribute with information about time when the value was created.
	 */
	private String valueCreatedAt;

	/**
	 * Attribute with information who created the value.
	 */
	private String valueCreatedBy;

	/**
	 * Attribute with information when the value was modified.
	 */
	private String valueModifiedAt;

	/**
	 * Attribute with information who modified the value.
	 */
	private String valueModifiedBy;

	public Attribute() {
	}

	public Attribute(Attribute attribute, boolean copyValue) {
		super(attribute);
		if(copyValue) {
			this.value = attribute.getValue();
			this.valueCreatedAt = attribute.getValueCreatedAt();
			this.valueCreatedBy = attribute.getValueCreatedBy();
			this.valueModifiedAt = attribute.getValueModifiedAt();
			this.valueModifiedBy = attribute.getValueModifiedBy();
		}
	}

	/**
	 * Creates Attribute instance from an AttributeDefinition with null value.
	 */
	public Attribute(AttributeDefinition attributeDefinition) {
		super(attributeDefinition);
		this.value = null;
		this.valueCreatedAt = null;
		this.valueCreatedBy = null;
		this.valueModifiedAt = null;
		this.valueModifiedBy = null;
	}

	/**
	 * Creates Attribute instance from an AttributeDefinition and a value.
	 */
	public Attribute(AttributeDefinition attributeDefinition, Object value) {
		this(attributeDefinition);
		this.setValue(value);
	}

	public Object getValue() {
		return value;
	}

	public String valueAsString() {
		return (String) value;
	}

	public Integer valueAsInteger() {
		return (Integer) value;
	}

	public Boolean valueAsBoolean() {
		return (Boolean) value;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> valueAsList() {
		return (ArrayList<String>) value;
	}

	@SuppressWarnings("unchecked")
	public LinkedHashMap<String,String> valueAsMap() {
		return (LinkedHashMap<String,String>) value;
	}


	public String getValueCreatedAt() {
		return valueCreatedAt;
	}

	public String getValueCreatedBy() {
		return valueCreatedBy;
	}

	public String getValueModifiedAt() {
		return valueModifiedAt;
	}

	public String getValueModifiedBy() {
		return valueModifiedBy;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public void setValueCreatedAt(String valueCreatedAt) {
		this.valueCreatedAt = valueCreatedAt;
	}

	public void setValueCreatedBy(String valueCreatedBy) {
		this.valueCreatedBy = valueCreatedBy;
	}

	public void setValueModifiedAt(String valueModifiedAt) {
		this.valueModifiedAt = valueModifiedAt;
	}

	public void setValueModifiedBy(String valueModifiedBy) {
		this.valueModifiedBy = valueModifiedBy;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + getId();
		hash = 53 * hash + (getFriendlyName() == null ? 0 : getFriendlyName().hashCode());
		hash = 53 * hash + (getNamespace() == null ? 0 : getNamespace().hashCode());
		hash = 53 * hash + (value == null ? 0 : value.hashCode());
		return hash;
	}

	/**
	 * Check if the attribute value contains value. In case of list, it uses method contains. In case of array it searches in both keys and values.
	 *
	 * @param value value
	 * @return true if the attribute value contains value.
	 */
	@SuppressWarnings("unchecked")
	public boolean valueContains(String value) {
		if (this.getType().equals(String.class.getName()) || this.getType().equals(BeansUtils.largeStringClassName)) {
			return value == null ? this.getValue() == null : value.equals(this.getValue());
		} else if (this.getType().equals(ArrayList.class.getName()) || this.getType().equals(BeansUtils.largeArrayListClassName)) {
			return this.getValue() == null ? value == null : ((ArrayList<String>) this.getValue()).contains(value);
		} else if (this.getType().equals(LinkedHashMap.class.getName())) {
			return this.getValue() == null ? value == null :
				(((LinkedHashMap<String, String>) this.getValue()).containsKey(value) ||
				 ((LinkedHashMap<String, String>) this.getValue()).containsValue(value));
		} else return false;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;

		if (!(obj instanceof AttributeDefinition)) return false;

		if(!super.equals(obj)) return false;

		if(!(obj instanceof Attribute)) {
			//Compare only as AttributeDefinition, which was done above
			return true;
		}

		final Attribute other = (Attribute) obj;

		if(this.value == null ? other.value != null : !this.value.equals(other.value)) return false;
		return true;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		String stringValue;
		if(getValue() == null) stringValue = null;
		else
			try {
				stringValue = BeansUtils.attributeValueToString(this);

			} catch (InternalErrorException ex) {
				//WARNING: This error is not catched. There is very low chance to occur.
				//When this happens, error is logged and there is need to look on attributeValueToString script above.
				log.error("Attribute value can't be serialize! {}",ex);
				stringValue = null;
			}
		return str.append(this.getClass().getSimpleName()).append(":[").append(
				"id=<").append(getId()).append(">").append(
				", friendlyName=<").append(getFriendlyName() == null ? "\\0" : BeansUtils.createEscaping(getFriendlyName())).append(">").append(
				", namespace=<").append(getNamespace() == null ? "\\0" : BeansUtils.createEscaping(getNamespace())).append(">").append(
				", type=<").append(getType() == null ? "\\0" : BeansUtils.createEscaping(getType())).append(">").append(
				", unique=<").append(isUnique()).append(">").append(
				", value=<").append(BeansUtils.createEscaping(stringValue)).append(">").append(
				']').toString();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ":[" +
				"id='" + getId() + '\'' +
				", friendlyName='" + getFriendlyName() + '\'' +
				", namespace='" + getNamespace() + '\'' +
				", type='" + getType() + '\'' +
				", unique='" + isUnique() + '\'' +
				", value='" + getValue() + '\'' +
				']';
	}
}
