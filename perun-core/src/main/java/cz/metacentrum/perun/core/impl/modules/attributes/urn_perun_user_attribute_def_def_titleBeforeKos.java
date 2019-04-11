package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import static cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF;

/**
 * Attribute module for setting title before name from KOS.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_titleBeforeKos extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	/**
	 * When KOS title before name is set, check if there is title from VEMA (personal system).
	 * If not, update title in User.
	 *
	 * @param session
	 * @param user
	 * @param attribute
	 * @throws InternalErrorException
	 */
	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException {

		if (attribute.getValue() != null) {
			Attribute titleBeforeVema;
			try {
				titleBeforeVema = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, NS_USER_ATTR_DEF + ":titleBeforeVema");
				if (titleBeforeVema.getValue() == null) {
					// no title from VEMA - update from KOS
					user.setTitleBefore((String)attribute.getValue());
					session.getPerunBl().getUsersManagerBl().updateNameTitles(session, user);
				}
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
				throw new InternalErrorException(e);
			} catch (UserNotExistsException e) {
				throw new ConsistencyErrorException("User we set attributes for doesn't exists!", e);
			}

		}

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(NS_USER_ATTR_DEF);
		attr.setFriendlyName("titleBeforeKos");
		attr.setDisplayName("Title before (KOS)");
		attr.setType(Integer.class.getName());
		attr.setDescription("Title before name from KOS.");
		return attr;
	}

}
