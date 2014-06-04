package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityVirtualAttributesModuleImplApi;

/**
 *
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_facility_attribute_def_virt_maxGID extends FacilityVirtualAttributesModuleAbstract implements FacilityVirtualAttributesModuleImplApi {

	public void checkAttributeValue(PerunSessionImpl sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		try {
			Attribute gidNamespaceAttribute = getUnixGIDNamespaceAttribute(sess, facility);
			if(gidNamespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, gidNamespaceAttribute);
			Attribute namespaceMaxGidAttribute = getNamespaceMaxGidAttribute(sess, (String) gidNamespaceAttribute.getValue());
			sess.getPerunBl().getAttributesManagerBl().checkAttributeValue(sess, (String) gidNamespaceAttribute.getValue(), namespaceMaxGidAttribute);
		} catch(WrongReferenceAttributeValueException ex) {
			throw new WrongReferenceAttributeValueException(attribute, ex.getReferenceAttribute());
		}

	}

	public Attribute fillAttribute(PerunSessionImpl sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		return new Attribute(attributeDefinition);
	}

	public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		try {
			Attribute gidNamespaceAttribute = getUnixGIDNamespaceAttribute(sess, facility);
			if(gidNamespaceAttribute.getValue() == null) return attribute;
			Attribute namespaceMaxGidAttribute = getNamespaceMaxGidAttribute(sess, (String) gidNamespaceAttribute.getValue());
			attribute = Utils.copyAttributeToVirtualAttributeWithValue(namespaceMaxGidAttribute, attribute);
			return attribute;
		} catch(WrongReferenceAttributeValueException ex) {
			return attribute;
		}
	}

	public boolean setAttributeValue(PerunSessionImpl sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		Attribute gidNamespaceAttribute = getUnixGIDNamespaceAttribute(sess, facility);
		if(gidNamespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, gidNamespaceAttribute);
		Attribute namespaceMaxGidAttribute = getNamespaceMaxGidAttribute(sess, (String) gidNamespaceAttribute.getValue());
		if(! (attribute.getValue() == null ? namespaceMaxGidAttribute.getValue() == null : attribute.getValue().equals(namespaceMaxGidAttribute.getValue()))) {
			//attribute from param have other vale then physical attribute
			throw new WrongReferenceAttributeValueException(attribute, namespaceMaxGidAttribute);
		}
		return false;
	}

	public void removeAttributeValue(PerunSessionImpl sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException {
		//Not suported yet.
		throw new InternalErrorException("Can't remove value of this virtual attribute this way. " + attributeDefinition);
	}

	private Attribute getNamespaceMaxGidAttribute(PerunSessionImpl sess, String uidNamespace) throws InternalErrorException, WrongReferenceAttributeValueException {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, (String) uidNamespace, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxGID");
		} catch(AttributeNotExistsException ex) { throw new ConsistencyErrorException(ex);
		} catch(WrongAttributeAssignmentException ex) { throw new InternalErrorException(ex);
		}
	}

	private Attribute getUnixGIDNamespaceAttribute(PerunSessionImpl sess, Facility facility) throws InternalErrorException {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
		} catch(AttributeNotExistsException ex) { throw new InternalErrorException(ex);
		} catch(WrongAttributeAssignmentException ex) { throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<String>();
		strongDependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
		strongDependencies.add(AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxGID");
		return strongDependencies;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_VIRT);
		attr.setFriendlyName("maxGID");
		attr.setDisplayName("Max GID");
		attr.setType(Integer.class.getName());
		attr.setDescription("Maximal unix GID allowed.");
		return attr;
	}

}
