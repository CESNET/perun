package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;

/**
 * non authorized password reset mail template attribute
 *
 * @author Daniel Fecko dano9500@gmail.com
 */
public class urn_perun_entityless_attribute_def_def_nonAuthzPwdResetMailTemplate_namespace extends EntitylessAttributesModuleAbstract {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, String key, Attribute attribute) throws WrongAttributeValueException {
		if (attribute.getValue() == null) return;
		if (!(attribute.getValue() instanceof String)) {
			throw new WrongAttributeValueException(attribute, key, "value must be of type String");
		}
		String value = attribute.valueAsString();
		if (!value.contains("{link}")) {
			throw new WrongAttributeValueException(attribute, key, "value doesn't contain contain tag {link}");
		}
	}

}
