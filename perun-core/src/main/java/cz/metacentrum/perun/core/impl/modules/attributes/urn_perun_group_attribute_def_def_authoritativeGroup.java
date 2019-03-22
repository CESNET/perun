package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authoritative group module.
 * If some group has authoritativeGroup attribute set to 1 (true), synchronizator
 * can remove member from whole vo if this group was the last authoritative and
 * synchronizator remove member from this group.
 *
 * @author Michal Stava  stavamichal@gmail.com
 */
public class urn_perun_group_attribute_def_def_authoritativeGroup extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_group_attribute_def_def_authoritativeGroup.class);

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException{
		//Null value is ok, means no settings for group
		if(attribute.getValue() == null) return;

		//Member group can't have set this attribute
		if(group.getName().equals(VosManager.MEMBERS_GROUP)) throw new WrongAttributeValueException(attribute, group, "Members group is authoritative automatic, there is not allowed to set this attribute for members group.");
	
		Integer value = (Integer) attribute.getValue();
		if(value < 0 || value > 1) throw new WrongAttributeValueException(attribute, group, "Attribute can have only value 1 or 0 (true or false).");
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("authoritativeGroup");
		attr.setDisplayName("Authoritative Group");
		attr.setType(Integer.class.getName());
		attr.setDescription("If group is authoritative for member. (for synchronization)");
		return attr;
	}
}
