package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserExtSourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserExtSourceAttributesModuleImplApi;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Vyskocil <vyskocilpavel@muni.cz>
 */
public class urn_perun_ues_attribute_def_def_storedAttributes extends UserExtSourceAttributesModuleAbstract implements UserExtSourceAttributesModuleImplApi{

	public void checkAttributeValue(PerunSessionImpl sess, UserExtSource userExtSource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		if (attribute.getValue() == null) {
			return;
		}
		try {
			new JSONObject(attribute.valueAsString());
		} catch (JSONException e) {
			throw new WrongAttributeValueException("Value is not a valid JSON");
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_UES_ATTR_DEF);
		attr.setFriendlyName("storedAttributes");
		attr.setDisplayName("Stored attributes");
		attr.setType(String.class.getName());
		attr.setDescription("Stored attributes during synchronization.");
		return attr;
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl sess, UserExtSource userExtSource, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		try {
			User user = sess.getPerunBl().getUsersManagerBl().getUserById(sess, userExtSource.getUserId());
			sess.getPerunBl().getUsersManagerBl().updateUserAttributesByUserExtSources(sess, user);
		} catch (UserNotExistsException | WrongAttributeValueException | WrongAttributeAssignmentException | AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}
}
