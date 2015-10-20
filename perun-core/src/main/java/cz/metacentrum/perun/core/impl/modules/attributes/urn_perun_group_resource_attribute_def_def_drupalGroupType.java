package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleImplApi;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for drupal group type
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 16.10.2015
 */
public class urn_perun_group_resource_attribute_def_def_drupalGroupType extends ResourceGroupAttributesModuleAbstract implements ResourceGroupAttributesModuleImplApi {

	private final static org.slf4j.Logger log = LoggerFactory.getLogger(urn_perun_group_resource_attribute_def_def_drupalGroupType.class);
	
	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String attributeValue = null;

		if(attribute.getValue() == null) {
			throw new WrongAttributeValueException(attribute, resource, group, "Type of drupal group can't be null.");
		}
		else {
			attributeValue = (String) attribute.getValue();
		}

		if(!(attributeValue.equals("public") || attributeValue.equals("private"))) {
			throw new WrongAttributeValueException(attribute, resource, group, "Type of drupal group is not in correct form. It can be either 'public' or 'private'.");
		}
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl session, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute filledAttribute = new Attribute(attribute);

		String attributeValue = (String) filledAttribute.getValue();
		if((attributeValue == null) || ("".equals(attributeValue))) {
			filledAttribute.setValue("public");
		} else {
			try {
				checkAttributeValue(session, resource, group, filledAttribute);
			} catch (WrongAttributeValueException ex) {
				log.error("Type of drupal group can be either 'public' or 'private'.", ex);
			} catch (WrongReferenceAttributeValueException ex) {
				log.error("Reference attribute for drupal group type can be either 'public' or 'private'.", ex);
			}
		}
		return filledAttribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("drupalGroupType");
		attr.setDisplayName("Drupal group type");
		attr.setType(String.class.getName());
		attr.setDescription("Type of the drupal group");
		return attr;
	}
}
