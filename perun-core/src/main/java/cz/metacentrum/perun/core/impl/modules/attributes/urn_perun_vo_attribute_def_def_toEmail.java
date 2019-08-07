package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jiří Mauritz
 */
public class urn_perun_vo_attribute_def_def_toEmail extends VoAttributesModuleAbstract implements VoAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Vo vo, Attribute attribute) throws WrongAttributeValueException {
		// null attribute
		if (attribute.getValue() == null) throw new WrongAttributeValueException(attribute, "Vo toEmail list cannot be null.");

		// wrong type of the attribute
		if (!(attribute.getValue() instanceof List)) throw new WrongAttributeValueException(attribute, "Wrong type of the attribute. Expected: List");

		List<String> toEmails = attribute.valueAsList();

		// the List is empty
		if (toEmails.isEmpty()) throw new WrongAttributeValueException(attribute, "Attribute List of toEmails is empty.");

		for (String email : toEmails) {
			if (email == null) throw new WrongAttributeValueException(attribute, "Email is null.");
			if (!(sess.getPerunBl().getModulesUtilsBl().isNameOfEmailValid(sess, email))) throw new WrongAttributeValueException(attribute, "Vo : " + vo.getName() +" has toEmail " + email +" which is not valid.");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
		attr.setFriendlyName("toEmail");
		attr.setDisplayName("\"To\" email addresses");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("Email addresses (of VO managers) used as \"to\" in mail notifications");
		return attr;
	}

}
