package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author  Peter Balcirak <peter.balcirak@gmail.com>
 * @date 15.08.2016
 */
public class urn_perun_group_attribute_def_def_fromEmail  extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^\".+\" <.+>$");

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongAttributeValueException {
		// null attribute
		if (attribute.getValue() == null) throw new WrongAttributeValueException(attribute, "Group fromEmail cannot be null.");

		// wrong type of the attribute
		if (!(attribute.getValue() instanceof String)) throw new WrongAttributeValueException(attribute, "Wrong type of the attribute. Expected: String");

		String fromEmail = attribute.valueAsString();

		if (!(sess.getPerunBl().getModulesUtilsBl().isNameOfEmailValid(sess, fromEmail))){

			Matcher match = pattern.matcher(fromEmail);

    		if (!match.matches()) {
				throw new WrongAttributeValueException(attribute, "Group : " + group.getName() + " has fromEmail " + fromEmail + " which is not valid. It has to be in form \"header\" <correct email> or just correct email.");
			}else{

				String[] emailParts = fromEmail.split("[<>]+");

				if (!(sess.getPerunBl().getModulesUtilsBl().isNameOfEmailValid(sess, emailParts[1]))){
					throw new WrongAttributeValueException(attribute, "Group : " + group.getName() +" has email in <> " + emailParts[1] +" which is not valid.");
				}
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("fromEmail");
		attr.setDisplayName("\"From\" email address");
		attr.setType(String.class.getName());
		attr.setDescription("Email address used as \"from\" in mail notifications");
		return attr;
	}
}
