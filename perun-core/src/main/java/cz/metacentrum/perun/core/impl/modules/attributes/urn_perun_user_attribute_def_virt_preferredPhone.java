package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

/**
 * Get phone number for VŠUP from all possibilities.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_virt_preferredPhone extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {

		Attribute attribute = new Attribute(attributeDefinition);

		try {

			Attribute sourceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, "urn:perun:user:attribute-def:def:phoneDc2");
			if (sourceAttribute.getValue() != null) {
				attribute.setValue(sourceAttribute.getValue());
				return attribute;
			}

			sourceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, "urn:perun:user:attribute-def:opt:mobilePhone");
			if (sourceAttribute.getValue() != null) {
				attribute.setValue(sourceAttribute.getValue());
				return attribute;
			}

			sourceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, "urn:perun:user:attribute-def:opt:privatePhone");
			if (sourceAttribute.getValue() != null) {
				attribute.setValue(sourceAttribute.getValue());
				return attribute;
			}

			sourceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, "urn:perun:user:attribute-def:opt:privatePhoneKos");
			if (sourceAttribute.getValue() != null) {
				attribute.setValue(sourceAttribute.getValue());
				return attribute;
			}

			return attribute;

		} catch (AttributeDefinitionNotExistsException ex) {
			throw new InternalErrorException(ex);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
	}


	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("preferredPhone");
		attr.setDisplayName("Preferred phone");
		attr.setType(String.class.getName());
		attr.setDescription("Preferred phone resolved from phone, mobilePhone and privatePhone (both DC2 and KOS).");
		return attr;
	}

}
