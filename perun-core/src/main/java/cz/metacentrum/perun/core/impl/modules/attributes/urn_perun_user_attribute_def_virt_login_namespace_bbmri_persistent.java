package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for access def:bbmri-persistent-shadow attribute. It generates value if you call it for the first time.
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 * @author Jakub Hruska <jhruska@mail.muni.cz>
 *
 * @date 07.11.2016
 */
public class urn_perun_user_attribute_def_virt_login_namespace_bbmri_persistent extends UserVirtualAttributesModuleAbstract {
    
        public static final String SHADOW = "urn:perun:user:attribute-def:def:login-namespace:bbmri-persistent-shadow";

        @Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute bbmriPersistent = new Attribute(attributeDefinition);

		try {
			Attribute bbmriPersistentShadow = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, SHADOW);

			if (bbmriPersistentShadow.getValue() == null) {

				bbmriPersistentShadow = sess.getPerunBl().getAttributesManagerBl().fillAttribute(sess, user, bbmriPersistentShadow);

				if (bbmriPersistentShadow.getValue() == null) {
					throw new InternalErrorException("BBMRI id couldn't be set automatically");
				}
				sess.getPerunBl().getAttributesManagerBl().setAttribute(sess, user, bbmriPersistentShadow);
			}

			bbmriPersistent.setValue(bbmriPersistentShadow.getValue());
			return bbmriPersistent;

		} catch (WrongAttributeAssignmentException | WrongAttributeValueException | WrongReferenceAttributeValueException | AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		}
        }

	@Override
	public List<String> getStrongDependencies() {
		return Collections.singletonList(SHADOW);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("login-namespace:bbmri-persistent");
		attr.setDisplayName("BBMRI login");
		attr.setType(String.class.getName());
		attr.setDescription("Login to BBMRI. It is set automatically with first call.");
		return attr;
	}
}
