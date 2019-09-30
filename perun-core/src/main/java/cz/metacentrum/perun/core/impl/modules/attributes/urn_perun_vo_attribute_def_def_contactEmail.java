package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Vo;
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
	public void checkAttributeSyntax(PerunSessionImpl sess, Vo vo, Attribute attribute) throws WrongAttributeValueException {
		// null value is ok for syntax check
		if (attribute.getValue() == null) return;

		List<String> contactEmails = attribute.valueAsList();

		for (String email : contactEmails) {
			if (!(sess.getPerunBl().getModulesUtilsBl().isNameOfEmailValid(sess, email))) throw new WrongAttributeValueException(attribute, "Vo : " + vo.getName() +" has contact email " + email +" which is not valid.");
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Vo vo, Attribute attribute) throws WrongReferenceAttributeValueException {
		// null attribute
		if (attribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, "Vo contact email list cannot be null.");
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
