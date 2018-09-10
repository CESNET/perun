package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

/**
 * Update title before name on User if value in attribute is changed.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_titleBeforeVema extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	/**
	 * When title before name from VEMA changes, update User.
	 *
	 * @param session
	 * @param user
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 */
	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {

		user.setTitleBefore((String)attribute.getValue());
		try {
			session.getPerunBl().getUsersManagerBl().updateNameTitles(session, user);
		} catch (UserNotExistsException e) {
			throw new ConsistencyErrorException("User we set attributes for doesn't exists!", e);
		}

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("titleBeforeVema");
		attr.setDisplayName("Title before (VEMA)");
		attr.setType(Integer.class.getName());
		attr.setDescription("Title before name from VEMA.");
		return attr;
	}

}
