package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
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
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongReferenceAttributeValueException {
		// null attribute
		if (attribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, "Attribute collectionID cannot be null.");
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
