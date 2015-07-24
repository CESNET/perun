package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Attribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceAttributes {

	private List<Attribute> attributes;
	private List<ServiceAttributes> childElements;

	public ServiceAttributes() {
		attributes = new ArrayList<Attribute>();
		childElements = new ArrayList<ServiceAttributes>();
	}

	public void addAttribute(Attribute attr) {
		attributes.add(attr);
	}

	public void addAttributes(List<Attribute> attrs) {
		attributes.addAll(attrs);
	}

	public void addChildElement(ServiceAttributes childElement) {
		childElements.add(childElement);
	}

	public List<ServiceAttributes> getChildElements() {
		return Collections.unmodifiableList(childElements);
	}

	public List<Attribute> getAttributes() {
		return Collections.unmodifiableList(attributes);
	}
}
