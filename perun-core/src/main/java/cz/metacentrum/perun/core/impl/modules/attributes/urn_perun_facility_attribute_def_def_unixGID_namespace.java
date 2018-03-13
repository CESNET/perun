package cz.metacentrum.perun.core.impl.modules.attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;

/**
 * Modul of unixGID-namespace attribut
 *
 * @author Michal Stava  stavamichal@gmail.com
 */
public class urn_perun_facility_attribute_def_def_unixGID_namespace extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_facility_attribute_def_def_unixGID_namespace.class);

	public void checkAttributeValue(PerunSessionImpl sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException{
		if(attribute.getValue() == null) throw new WrongAttributeValueException(attribute, "Attribute value can't be null");

		try {
			sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + (String) attribute.getValue());
			sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + (String) attribute.getValue());
		} catch (AttributeDefinitionNotExistsException e) {
			throw new WrongAttributeValueException(attribute, e);
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("unixGID-namespace");
		attr.setDisplayName("GID namespace");
		attr.setType(String.class.getName());
		attr.setDescription("Namespace of UnixGID.");
		return attr;
	}
}
