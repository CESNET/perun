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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class urn_perun_vo_attribute_def_def_RTVoQueue extends VoAttributesModuleAbstract implements VoAttributesModuleImplApi {

	//Pattern extensionDatePattern = Pattern.compile("^$");

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		//There is no special queue in RT for this VO (in specific method use default queue)
		if(attribute.getValue() == null) return;

		//Get value from attribute
		String attrValue = null;
		attrValue = (String) attribute.getValue();

		//TODO: Create some regexp Pattern for RTVoQueue and test it there
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		return new Attribute(attribute);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
		attr.setFriendlyName("RTVoQueue");
		attr.setDisplayName("RT queue for VO");
		attr.setType(String.class.getName());
		attr.setDescription("Definition of Queue for specific Vo.");
		return attr;
	}
}
