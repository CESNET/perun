package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;

/**
 * non authorized password reset mail subject template attribute
 *
 * @author Daniel Fecko dano9500@gmail.com
 */
public class urn_perun_entityless_attribute_def_def_nonAuthzPwdResetMailSubject_namespace extends EntitylessAttributesModuleAbstract {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		if (attribute.getValue() == null) return;
		if (!(attribute.getValue() instanceof String)) {
			throw new WrongAttributeValueException(attribute, key, "value must be of type String");
		}
	}

}
