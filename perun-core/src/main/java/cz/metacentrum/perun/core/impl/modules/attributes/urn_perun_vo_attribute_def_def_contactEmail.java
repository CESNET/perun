package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jiří Mauritz
 */
public class urn_perun_vo_attribute_def_def_contactEmail extends VoAttributesModuleAbstract implements VoAttributesModuleImplApi{

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		List<String> contactEmails = null;

		// null attribute
		if (attribute.getValue() == null) throw new WrongAttributeValueException(attribute, "Vo contact email list cannot be null.");

		// wrong type of the attribute
		if (!(attribute.getValue() instanceof List)) throw new WrongAttributeValueException(attribute, "Wrong type of the attribute. Expected: List");

		contactEmails = (List) attribute.getValue();

		// the List is empty
		if (contactEmails.isEmpty()) throw new WrongAttributeValueException(attribute, "Attribute List of contact emails is empty.");

		for (String email : contactEmails) {
			if (email == null) throw new WrongAttributeValueException(attribute, "Email " + email + " is null.");
			if (!(sess.getPerunBl().getModulesUtilsBl().isNameOfEmailValid(sess, email))) throw new WrongAttributeValueException(attribute, "Vo : " + vo.getName() +" has contact email " + email +" which is not valid.");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
		attr.setFriendlyName("contactEmail");
		attr.setDisplayName("Contact e-mails");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("VO contact email");
		return attr;
	}

}
