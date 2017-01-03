package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

/**
 * Contains ID of BBMRI biobank collection.
 *
 * @author Jiri Mauritz <jirmauritz@gmail.com>
 */
public class urn_perun_group_attribute_def_def_collectionID extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String collectionID = null;

		// null attribute
		if (attribute.getValue() == null) throw new WrongAttributeValueException(attribute, "Attribute collectionID cannot be null.");

		// wrong type of the attribute
		if (!(attribute.getValue() instanceof String)) throw new WrongAttributeValueException(attribute, "Wrong type of the attribute. Expected: String");

		collectionID = (String) attribute.getValue();

		if (collectionID.isEmpty()) {
			throw new WrongAttributeValueException(attribute, "Attribute collectionID cannot be empty.");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("collectionID");
		attr.setDisplayName("Collection ID");
		attr.setType(String.class.getName());
		attr.setDescription("ID of BBMRI collection");
		return attr;
	}
}
