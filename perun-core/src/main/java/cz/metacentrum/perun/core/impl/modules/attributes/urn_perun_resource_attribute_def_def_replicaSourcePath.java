package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import java.util.List;

/**
 * @author Simona Kruppova, Oliver Mrazik
 */
public class urn_perun_resource_attribute_def_def_replicaSourcePath extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

		if(attribute.getValue() == null) {
			throw new WrongAttributeValueException(attribute, resource, "This attribute can't be empty");
		}

		Facility facility = perunSession.getPerunBl().getResourcesManagerBl().getFacility(perunSession, resource);

		Attribute facilityAttr = null;
		try {
			facilityAttr = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":homeMountPoints");
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}

		if(facilityAttr.getValue() == null) {
			throw new WrongReferenceAttributeValueException(attribute, facilityAttr, resource, facility, "Reference attribute has null value.");
		}
		
		if (!((List<String>) facilityAttr.getValue()).contains((String) attribute.getValue())) {
			throw new WrongReferenceAttributeValueException(attribute, facilityAttr, resource, facility, "ReplicaSourcePath has to be the same as one of the F:D:homeMountPoints");
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("replicaSourcePath");
		attr.setDisplayName("Replica source path");
		attr.setType(String.class.getName());
		attr.setDescription("Absolute path in the filesystem to copy from");
		return attr;
	}
}
