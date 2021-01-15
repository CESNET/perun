package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractApplicationAutoRejectMessagesModule;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.LinkedHashMap;

/**
 * @author vojtech sassmann <vojtech.sassmann@gmail.com>
 */
public class urn_perun_group_attribute_def_def_applicationAutoRejectMessages extends AbstractApplicationAutoRejectMessagesModule<Group> implements GroupAttributesModuleImplApi {

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongAttributeValueException {
		super.checkAttributeSyntax(sess, group, attribute);
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Group group, Attribute attribute) throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException { }

	@Override
	public Attribute fillAttribute(PerunSessionImpl perunSession, Group group, AttributeDefinition attribute) throws WrongAttributeAssignmentException {
		return null;
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Group group, Attribute attribute) throws WrongReferenceAttributeValueException {}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("applicationAutoRejectMessages");
		attr.setDisplayName("Messages for automatic application rejection");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription(
		        "These messages are used when some of the submitted applications is automatically rejected. " +
		        "Use `ignoredByAdmin` to define a message that is used when some application is rejected when it is " +
		        "ignored by admins. For language specific version, use `ignoredByAdmin-{lang}` e.g. `ignoredByAdmin-en`. " +
		        "Use `emailVerification` to define a message that is used when some application is rejected because " +
		        "user didn't verify his email in time. For language specific version, use `emailVerification-{lang}`, " +
		        "e.g. `emailVerification-cs`.");
		return attr;
	}
}
