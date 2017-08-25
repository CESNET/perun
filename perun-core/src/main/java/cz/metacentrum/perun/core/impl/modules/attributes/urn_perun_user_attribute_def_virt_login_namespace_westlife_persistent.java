package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;

/**
 * Class for access def:westlife-persistent-shadow attribute. It generates value if you call it for the first time.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_virt_login_namespace_westlife_persistent extends UserVirtualAttributesModuleAbstract {

	public static final String SHADOW = "urn:perun:user:attribute-def:def:login-namespace:westlife-persistent-shadow";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute westlifePersistent = new Attribute(attributeDefinition);

		try {
			Attribute westlifePersistentShadow = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, SHADOW);

			if (westlifePersistentShadow.getValue() == null) {

				westlifePersistentShadow = sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, user, westlifePersistentShadow);

				if (westlifePersistentShadow.getValue() == null) {
					throw new InternalErrorException("Westlife id couldn't be set automatically");
				}
				sess.getPerunBl().getAttributesManagerBl().setAttribute(sess, user, westlifePersistentShadow);
			}

			westlifePersistent.setValue(westlifePersistentShadow.getValue());
			return westlifePersistent;

		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		} catch (AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		} catch (WrongReferenceAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (MemberResourceMismatchException ex) {
			throw new InternalErrorException(ex);
		} catch (GroupResourceMismatchException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("login-namespace:westlife-persistent");
		attr.setDisplayName("WEST-LIFE login");
		attr.setType(String.class.getName());
		attr.setDescription("Login to WEST-LIFE. It is set automatically with first call.");
		return attr;
	}
}
