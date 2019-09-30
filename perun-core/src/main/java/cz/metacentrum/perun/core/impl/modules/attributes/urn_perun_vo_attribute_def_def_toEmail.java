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
 * @author Jiří Mauritz
 */
public class urn_perun_vo_attribute_def_def_toEmail extends VoAttributesModuleAbstract implements VoAttributesModuleImplApi {

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, Vo vo, Attribute attribute) throws WrongAttributeValueException {
		// null attribute is ok for syntax check
		if (attribute.getValue() == null) return;

		List<String> toEmails = attribute.valueAsList();

		for (String email : toEmails) {
			if (!(sess.getPerunBl().getModulesUtilsBl().isNameOfEmailValid(sess, email))) throw new WrongAttributeValueException(attribute, "Vo : " + vo.getName() +" has toEmail " + email +" which is not valid.");
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Vo vo, Attribute attribute) throws WrongReferenceAttributeValueException {
		// null attribute
		if (attribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, "Vo toEmail list cannot be null.");
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
