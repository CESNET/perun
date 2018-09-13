package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;

import java.util.Collections;
import java.util.List;

/**
 * Class for access def:lifescience-hostel-persistent-shadow attribute. It generates value if you call it for the first time.
 *
 * @author Peter Jancus <p.jancus1996@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_login_namespace_lifescience_hostel_persistent extends UserVirtualAttributesModuleAbstract {

	public static final String SHADOW = "urn:perun:user:attribute-def:def:login-namespace:lifescience-hostel-persistent-shadow";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute lshPersistent = new Attribute(attributeDefinition);

		try {
			Attribute lshPersistentShadow = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, SHADOW);

			if (lshPersistentShadow.getValue() == null) {

				lshPersistentShadow = sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, user, lshPersistentShadow);

				if (lshPersistentShadow.getValue() == null) {
					throw new InternalErrorException("LIFESCIENCE HOSTEL id couldn't be set automatically");
				}
				sess.getPerunBl().getAttributesManagerBl().setAttribute(sess, user, lshPersistentShadow);
			}

			lshPersistent.setValue(lshPersistentShadow.getValue());
			return lshPersistent;

		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		} catch (AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		} catch (WrongReferenceAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeValueException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<String> getStrongDependencies() {
		return Collections.singletonList(SHADOW);
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("login-namespace:lifescience-hostel-persistent");
		attr.setDisplayName("LIFESCIENCE HOSTEL login");
		attr.setType(String.class.getName());
		attr.setDescription("Login to LIFESCIENCE HOSTEL. It is set automatically with first call.");
		return attr;
	}
}
