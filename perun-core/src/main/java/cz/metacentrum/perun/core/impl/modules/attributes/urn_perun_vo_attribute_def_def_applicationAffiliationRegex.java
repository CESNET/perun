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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Contains regular expressions matching affiliations to auto approve applications for.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class urn_perun_vo_attribute_def_def_applicationAffiliationRegex extends VoAttributesModuleAbstract implements VoAttributesModuleImplApi {
	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, Vo vo, Attribute attribute) throws WrongAttributeValueException {
		if (attribute.getValue() == null) return;
		for (String regex : attribute.valueAsList()) {
			try {
				Pattern.compile(regex);
			} catch (PatternSyntaxException exp) {
				throw new WrongAttributeValueException(attribute, "Regexp: \"" + regex + "\" syntax is not in the correct form");
			}
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Vo vo, Attribute attribute) throws WrongReferenceAttributeValueException {
		// null attribute
		if (attribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, "Affiliation regular expression attribute cannot be null.");
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
		attr.setFriendlyName("applicationAffiliationRegex");
		attr.setDisplayName("Affiliation auto approve regex");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("Regular expressions matching affiliations to auto approve applications for");
		return attr;
	}
}
