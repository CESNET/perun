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
 * GID Ranges computed for specific namespace on chosen facility
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class urn_perun_facility_attribute_def_virt_GIDRanges extends FacilityVirtualAttributesModuleAbstract implements FacilityVirtualAttributesModuleImplApi {

	private static final String A_FAC_unixGIDNamespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace";
	private static final String A_E_namespaceGIDRanges = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-GIDRanges";

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		try {
			Attribute gidNamespaceAttribute = getUnixGIDNamespaceAttribute(sess, facility);
			if(gidNamespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, gidNamespaceAttribute, "There is missing GID namespace on the facility.");
			Attribute namespaceGIDRangesAttribute = getNamespaceGIDRangesAttribute(sess, gidNamespaceAttribute.valueAsString());
			sess.getPerunBl().getAttributesManagerBl().checkAttributeSemantics(sess, gidNamespaceAttribute.valueAsString(), namespaceGIDRangesAttribute);
		} catch(WrongReferenceAttributeValueException ex) {
			throw new WrongReferenceAttributeValueException(attribute, ex.getReferenceAttribute());
		}
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		Attribute gidNamespaceAttribute = getUnixGIDNamespaceAttribute(sess, facility);
		if(gidNamespaceAttribute.getValue() == null) return attribute;
		Attribute namespaceGIDRangesAttribute = getNamespaceGIDRangesAttribute(sess, (String) gidNamespaceAttribute.getValue());
		return Utils.copyAttributeToVirtualAttributeWithValue(namespaceGIDRangesAttribute, attribute);
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		Attribute gidNamespaceAttribute = getUnixGIDNamespaceAttribute(sess, facility);
		if(gidNamespaceAttribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, gidNamespaceAttribute, "There is missing GID namespace on the facility.");
		Attribute namespaceGIDRangesAttribute = getNamespaceGIDRangesAttribute(sess, (String) gidNamespaceAttribute.getValue());
		if(! (attribute.getValue() == null ? namespaceGIDRangesAttribute.getValue() == null : attribute.getValue().equals(namespaceGIDRangesAttribute.getValue()))) {
			//attribute from param have other value then physical attribute
			throw new WrongReferenceAttributeValueException(attribute, namespaceGIDRangesAttribute, "You can't change attribute value of GID Ranges by changing value of virtual attribute.");
		}
		return false;
	}

	private Attribute getNamespaceGIDRangesAttribute(PerunSessionImpl sess, String uidNamespace) throws InternalErrorException {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, uidNamespace, A_E_namespaceGIDRanges);
		} catch(AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	private Attribute getUnixGIDNamespaceAttribute(PerunSessionImpl sess, Facility facility) throws InternalErrorException {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_FAC_unixGIDNamespace);
		} catch(AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(A_E_namespaceGIDRanges);
		dependencies.add(A_FAC_unixGIDNamespace);
		return dependencies;
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<>();
		strongDependencies.add(A_FAC_unixGIDNamespace);
		strongDependencies.add(A_E_namespaceGIDRanges);
		return strongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_VIRT);
		attr.setFriendlyName("GIDRanges");
		attr.setDisplayName("GID ranges in set namespace for the Facility");
		attr.setType(List.class.getName());
		attr.setDescription("Computed GID ranges in set namespace for the facility");
		return attr;
	}
}
