package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents consent definition for facilities, services and attributes
 */
public class ConsentDefinition extends Auditable {
	private String name;
	private String text;
	private List<Facility> facilities;
	private List<Service> services;
	private List<Attribute> attributes;

	public ConsentDefinition() {
	}

	public ConsentDefinition(int id, String name, List<Facility> facilities, List<Service> services, List<Attribute> attributes) {
		super(id);
		this.name = name;
		this.facilities = facilities;
		this.services = services;
		this.attributes = attributes;
	}

	public ConsentDefinition(int id, String name, String text, List<Facility> facilities, List<Service> services, List<Attribute> attributes) {
		this(id, name, facilities, services, attributes);
		this.text = text;
	}

	public ConsentDefinition(int id, String name, List<Facility> facilities, List<Service> services, List<Attribute> attributes, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.facilities = facilities;
		this.services = services;
		this.attributes = attributes;
	}

	public ConsentDefinition(int id, String name, String text, List<Facility> facilities, List<Service> services, List<Attribute> attributes, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		this(id, name, facilities, services, attributes, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.text = text;
	}

	public String getName() { return name; }

	public void setName(String name) { this.name = name; }

	public String getText() { return text; }

	public void setText(String text) { this.text = text; }

	public List<Facility> getFacilities() { return facilities; }

	public void setFacilities(List<Facility> facilities) { this.facilities = facilities; }

	public List<Service> getServices() { return services; }

	public void setServices(List<Service> services) { this.services = services; }

	public List<Attribute> getAttributes() { return attributes; }

	public void setAttributes(List<Attribute> attributes) { this.attributes = attributes; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConsentDefinition that = (ConsentDefinition) o;
		return Objects.equals(getName(), that.getName()) && Objects.equals(getText(), that.getText());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getText());
	}

	@Override
	public String serializeToString() {
		List<Facility> facilities = getFacilities();
		List<Service> services = getServices();
		List<Attribute> attributes = getAttributes();
		List<String> entityStringArray = new ArrayList<>();
		String facilitiesString;
		String servicesString;
		String attributesString;


		if (getFacilities() == null) facilitiesString = "\\0";
		else {
			for(Facility f: facilities) {
				entityStringArray.add(f.serializeToString());
			}
			facilitiesString = entityStringArray.toString();
			entityStringArray.clear();
		}

		if (getServices() == null) servicesString = "\\0";
		else {
			for(Service s: services) {
				entityStringArray.add(s.serializeToString());
			}
			servicesString = entityStringArray.toString();
			entityStringArray.clear();
		}

		if (getAttributes() == null) attributesString = "\\0";
		else {
			for(Attribute a: attributes) {
				entityStringArray.add(a.serializeToString());
			}
			attributesString = entityStringArray.toString();
		}

		return this.getClass().getSimpleName() + ":[" +
			"id=<" + getId() + ">" +
			", name=<" + getName() + ">" +
			", text=<" + getText() + ">" +
			", facilities=<" + facilitiesString + ">" +
			", services=<" + servicesString + ">" +
			", attributes=<" + attributesString + ">" +
			']';
	}

	@Override
	public String toString() {
		return "Consent:[id='" + getId() +
			"', name='" + name +
			"', text='" + text +
			"', facilities='" + facilities +
			"', services='" + services +
			"', attributes='" + attributes +
			"']";
	}

}
