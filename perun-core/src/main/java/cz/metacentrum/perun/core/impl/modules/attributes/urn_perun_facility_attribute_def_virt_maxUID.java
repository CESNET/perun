package cz.metacentrum.perun.core.impl.modules.attributes;

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
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityVirtualAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_facility_attribute_def_virt_maxUID extends FacilityVirtualAttributesModuleAbstract implements FacilityVirtualAttributesModuleImplApi {

	private static final String A_E_namespaceMaxUID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxUID";
	private static final String A_FAC_uidNamespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace";

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		try {
			Attribute uidNamespaceAttribute = getUidNamespaceAttribute(sess, facility);
			if(uidNamespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, uidNamespaceAttribute);
			Attribute namespaceMaxUidAttribute = getNamespaceMaxUidAttribute(sess, uidNamespaceAttribute.valueAsString());
			sess.getPerunBl().getAttributesManagerBl().checkAttributeSemantics(sess, uidNamespaceAttribute.valueAsString(), namespaceMaxUidAttribute);
		} catch(WrongReferenceAttributeValueException ex) {
			throw new WrongReferenceAttributeValueException(attribute, ex.getReferenceAttribute());
		}

	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Facility facility, AttributeDefinition attributeDefinition) {
		return new Attribute(attributeDefinition);
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		Attribute uidNamespaceAttribute = getUidNamespaceAttribute(sess, facility);
		if(uidNamespaceAttribute.getValue() == null) return attribute;
		Attribute namespaceMaxUidAttribute = getNamespaceMaxUidAttribute(sess, (String) uidNamespaceAttribute.getValue());
		return Utils.copyAttributeToVirtualAttributeWithValue(namespaceMaxUidAttribute, attribute);
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		Attribute uidNamespaceAttribute = getUidNamespaceAttribute(sess, facility);
		if(uidNamespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, uidNamespaceAttribute);
		Attribute namespaceMaxUidAttribute = getNamespaceMaxUidAttribute(sess, (String) uidNamespaceAttribute.getValue());
		if(! (attribute.getValue() == null ? namespaceMaxUidAttribute.getValue() == null : attribute.getValue().equals(namespaceMaxUidAttribute.getValue()))) {
			//attribute from param have other vale then physical attribute
			throw new WrongReferenceAttributeValueException(attribute, namespaceMaxUidAttribute);
		}
		return false;
	}

	@Override
	public void removeAttributeValue(PerunSessionImpl sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException {
		//Not suported yet.
		throw new InternalErrorException("Can't remove value of this virtual attribute this way. " + attributeDefinition);
	}

	private Attribute getNamespaceMaxUidAttribute(PerunSessionImpl sess, String uidNamespace) throws InternalErrorException {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, uidNamespace, A_E_namespaceMaxUID);
		} catch(AttributeNotExistsException ex) { throw new ConsistencyErrorException(ex);
		} catch(WrongAttributeAssignmentException ex) { throw new InternalErrorException(ex);
		}
	}

	private Attribute getUidNamespaceAttribute(PerunSessionImpl sess, Facility facility) throws InternalErrorException {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_FAC_uidNamespace);
		} catch(AttributeNotExistsException | WrongAttributeAssignmentException ex) { throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(A_FAC_uidNamespace);
		dependencies.add(A_E_namespaceMaxUID);
		return dependencies;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<>();
		strongDependencies.add(A_FAC_uidNamespace);
		strongDependencies.add(A_E_namespaceMaxUID);
		return strongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_VIRT);
		attr.setFriendlyName("maxUID");
		attr.setDisplayName("Max UID");
		attr.setType(Integer.class.getName());
		attr.setDescription("Maximal unix UID allowed.");
		return attr;
	}

}
