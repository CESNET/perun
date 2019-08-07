package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

/**
 * Attribute module for generating unique UČO for every person in VŠUP.
 * When UCO is set, UserExtSource is added too.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_ucoVsup extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {

		if (attribute.getValue() == null) throw new WrongAttributeValueException(attribute, user, "UČO can't be null.");

	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, User user, AttributeDefinition attribute) throws InternalErrorException {

		Attribute attr = new Attribute(attribute);
		int uco = Utils.getNewId(sess.getPerunBl().getDatabaseManagerBl().getJdbcPerunTemplate(), "vsup_uco_seq");
		attr.setValue(uco);
		return attr;

	}

	/**
	 * When UCO changes: add UserExtSource, since UCOs are generated in Perun.
	 *
	 * @param session
	 * @param user
	 * @param attribute
	 * @throws InternalErrorException
	 */
	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException {

		if (attribute.getValue() != null) {

			// add UES
			ExtSource es;

			try {
				es = session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, "UCO");
			} catch (ExtSourceNotExistsException ex) {
				throw new InternalErrorException("UCO ext source on VŠUP doesn't exists.", ex);
			}
			try {
				session.getPerunBl().getUsersManagerBl().getUserExtSourceByExtLogin(session, es, String.valueOf(attribute.getValue()));
			} catch (UserExtSourceNotExistsException ex) {
				// add UES
				UserExtSource ues = new UserExtSource(es, 2, String.valueOf(attribute.getValue()));
				try {
					session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, ues);
				} catch (UserExtSourceExistsException ex2) {
					throw new ConsistencyErrorException(ex2);
				}
			}
		}

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("ucoVsup");
		attr.setDisplayName("UČO");
		attr.setType(Integer.class.getName());
		attr.setDescription("Unique University person identifier.");
		return attr;
	}

}
