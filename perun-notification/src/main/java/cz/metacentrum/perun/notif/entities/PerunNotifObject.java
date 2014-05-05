package cz.metacentrum.perun.notif.entities;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Object which can be found in regex from auditer. Contains properties, which
 * holds functions, these functions can be used to get neccessary informations
 * about real object using java reflection, one object has unique name across
 * other objects.
 *
 * Table pn_object
 *
 * @author tomas.tunkl
 *
 */
public class PerunNotifObject {

	/**
	 * Unique id of object
	 *
	 * Column id Sequence pn_object_id_seq
	 */
	private Integer id;

	/**
	 * Holds name of object
	 *
	 * Column name
	 */
	private String name;

	/**
	 * Holds Class of object
	 *
	 * Column object_class
	 */
	private Class objectClass;

	/**
	 * Set of functions which can be called on object using java reflection
	 *
	 * Column properties
	 */
	private Set<String> properties;

	public PerunNotifObject() {
		properties = new HashSet<String>();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getProperties() {
		return properties;
	}

	public void setProperties(Set<String> properties) {
		this.properties = properties;
	}

	public Class getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(Class objectClass) {
		this.objectClass = objectClass;
	}

	public void addProperty(String value) {

		if (properties == null) {
			properties = new HashSet<String>();
		}

		properties.add(value);
	}

	/**
	 * RowMapper to load entity from db row Uses java reflection to resolve
	 * class
	 */
	public static final RowMapper<PerunNotifObject> PERUN_NOTIF_OBJECT = new RowMapper<PerunNotifObject>() {

		public PerunNotifObject mapRow(ResultSet rs, int i) throws SQLException {

			PerunNotifObject object = new PerunNotifObject();
			object.setId(rs.getInt("id"));
			object.setName(rs.getString("name"));
			object.setProperties(parseSet(rs.getString("properties")));
			String className = rs.getString("class_name");
			if (className != null) {
				try {
					Class objectClass = Class.forName(className);
					object.setObjectClass(objectClass);
				} catch (ClassNotFoundException ex) {
					//Class cannot be resolved
					throw new SQLException("Class: " + className + " cannot be resolved.");
				}
			}
			return object;
		}
	};

	/**
	 * Set delimiter for serialization to string
	 */
	private static final String SET_DELIMITER = ";";

	/**
	 * Parses row to set of properties
	 *
	 * @param row
	 * @return
	 */
	private static Set<String> parseSet(String row) {

		if (row == null) {
			return null;
		}

		Set<String> result = new HashSet<String>();

		String[] splittedValue = row.split(SET_DELIMITER);
		for (String entry : splittedValue) {
			result.add(entry);
		}

		return result;
	}

	/**
	 * Serialize properties to string
	 *
	 * @return
	 */
	public String getSerializedProperties() {

		StringBuilder builder = new StringBuilder();
		for (Iterator<String> iter = properties.iterator(); iter.hasNext();) {
			String temp = iter.next();
			builder.append(temp);
			if (iter.hasNext()) {
				builder.append(SET_DELIMITER);
			}
		}

		return builder.toString();
	}

	@Override
	public String toString() {
		return "id: " + getId() + " name: " + getName() + " properties: " + getProperties() + " object class: " + getObjectClass();
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof PerunNotifObject)) {
			return false;
		}

		PerunNotifObject perunObject = (PerunNotifObject) obj;

		return perunObject.getId().equals(this.getId());
	}

	/**
	 * Method updates basic parameters
	 *
	 * @param newObject
	 */
	public void update(PerunNotifObject newObject) {

		this.setName(newObject.getName());
		this.setProperties(newObject.getProperties());
		this.setObjectClass(newObject.getObjectClass());
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (properties != null ? properties.hashCode() : 0);
		return result;
	}
}
