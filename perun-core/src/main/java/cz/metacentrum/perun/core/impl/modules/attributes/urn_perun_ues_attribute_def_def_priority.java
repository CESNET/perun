package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserExtSourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserExtSourceAttributesModuleImplApi;

import java.util.List;

/**
 * @author Pavel Vyskocil <vyskocilpavel@muni.cz>
 */
public class urn_perun_ues_attribute_def_def_priority extends UserExtSourceAttributesModuleAbstract implements UserExtSourceAttributesModuleImplApi {

	public void checkAttributeValue(PerunSessionImpl sess, UserExtSource userExtSource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		if (attribute.getValue() == null) {
			return;
		}
		if ((Integer)attribute.getValue() < 0 ) {
			throw new WrongAttributeValueException("Value can not be negative.");
		}
		try {
			User user = sess.getPerunBl().getUsersManagerBl().getUserById(sess, userExtSource.getUserId());
			List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
			for (UserExtSource ues: userExtSources) {
				if (!ues.equals(userExtSource)) {
					int priority = sess.getPerunBl().getUsersManagerBl().getUserExtSourcePriority(sess, ues);
					if (priority > 0 && (Integer) attribute.getValue() == priority) {
						throw new WrongAttributeValueException("This value " +  attribute.valueAsInteger() + " for user ext source: " + ues + " is already used and you cannot used it again.");
					}
				}
			}
		} catch (UserNotExistsException e) {
			throw new InternalErrorException("User for UserExtSource does not exist.");
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException("");
		}

	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_UES_ATTR_DEF);
		attr.setFriendlyName("priority");
		attr.setDisplayName("Priority");
		attr.setType(Integer.class.getName());
		attr.setDescription("Priority of UserExtSource. Priority must be bigger than 0.");
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
