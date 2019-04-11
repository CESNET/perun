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
@Deprecated
public class urn_perun_facility_attribute_def_virt_minGID extends FacilityVirtualAttributesModuleAbstract implements FacilityVirtualAttributesModuleImplApi {

	private static final String A_E_namespaceMinGID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-minGID";
	private static final String A_FAC_unixGIDNamespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace";

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		try {
			Attribute gidNamespaceAttribute = getUnixGIDNamespaceAttribute(sess, facility);
			if(gidNamespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, gidNamespaceAttribute);
			Attribute namespaceMinGidAttribute = getNamespaceMinGidAttribute(sess, (String) gidNamespaceAttribute.getValue());
			sess.getPerunBl().getAttributesManagerBl().checkAttributeValue(sess, (String) gidNamespaceAttribute.getValue(), namespaceMinGidAttribute);
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
		Attribute gidNamespaceAttribute = getUnixGIDNamespaceAttribute(sess, facility);
		if(gidNamespaceAttribute.getValue() == null) return attribute;
		Attribute namespaceMinGidAttribute = getNamespaceMinGidAttribute(sess, (String) gidNamespaceAttribute.getValue());
		return Utils.copyAttributeToVirtualAttributeWithValue(namespaceMinGidAttribute, attribute);
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		Attribute gidNamespaceAttribute = getUnixGIDNamespaceAttribute(sess, facility);
		if(gidNamespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, gidNamespaceAttribute);
		Attribute namespaceMinGidAttribute = getNamespaceMinGidAttribute(sess, (String) gidNamespaceAttribute.getValue());
		if(! (attribute.getValue() == null ? namespaceMinGidAttribute.getValue() == null : attribute.getValue().equals(namespaceMinGidAttribute.getValue()))) {
			//attribute from param have other vale then physical attribute
			throw new WrongReferenceAttributeValueException(attribute, namespaceMinGidAttribute);
		}
		return false;
	}

	@Override
	public void removeAttributeValue(PerunSessionImpl sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException {
		//Not suported yet.
		throw new InternalErrorException("Can't remove value of this virtual attribute this way. " + attributeDefinition);
	}

	private Attribute getNamespaceMinGidAttribute(PerunSessionImpl sess, String uidNamespace) throws InternalErrorException {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, uidNamespace, A_E_namespaceMinGID);
		} catch(AttributeNotExistsException ex) { throw new ConsistencyErrorException(ex);
		} catch(WrongAttributeAssignmentException ex) { throw new InternalErrorException(ex);
		}
	}

	private Attribute getUnixGIDNamespaceAttribute(PerunSessionImpl sess, Facility facility) throws InternalErrorException {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_FAC_unixGIDNamespace);
		} catch(AttributeNotExistsException | WrongAttributeAssignmentException ex) { throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(A_E_namespaceMinGID);
		dependencies.add(A_FAC_unixGIDNamespace);
		return dependencies;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<>();
		strongDependencies.add(A_E_namespaceMinGID);
		strongDependencies.add(A_FAC_unixGIDNamespace);
		return strongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_VIRT);
		attr.setFriendlyName("minGID");
		attr.setDisplayName("Min GID");
		attr.setType(Integer.class.getName());
		attr.setDescription("Minimal unix GID allowed.");
		return attr;
	}

}
