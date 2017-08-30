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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Group vomGroupName attribute.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class urn_perun_group_attribute_def_def_vomsGroupName extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[^<>&=]*$");

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String vomsGroupName;

		if(attribute.getValue() == null) {
			throw new WrongAttributeValueException(attribute, "Group vomsGroupName cannot be null.");
		}
		if(!(attribute.getValue() instanceof String)) {
			throw new WrongAttributeValueException(attribute, "Wrong type of the attribute. Expected: String");
		}

		vomsGroupName = (String) attribute.getValue();

		Matcher matcher = pattern.matcher(vomsGroupName);

		if(!matcher.matches()) {
			throw new WrongAttributeValueException(attribute, "Bad format of attribute vomsGroupName. It should not contain '<>&=' characters.");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setFriendlyName("vomsGroupName");
		attr.setDisplayName("Voms group name");
		attr.setDescription("Name of voms group, if defined.");
		attr.setType(String.class.getName());
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		return attr;
	}
}
