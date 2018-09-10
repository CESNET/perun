package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Get phone number for VŠUP from all possibilities.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_virt_preferredPhone extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private static final String A_U_D_phoneDc2 = AttributesManager.NS_USER_ATTR_DEF + ":phoneDc2";
	private static final String A_U_O_mobilePhone = AttributesManager.NS_USER_ATTR_OPT + ":mobilePhone";
	private static final String A_U_O_privatePhone = AttributesManager.NS_USER_ATTR_OPT + ":privatePhone";
	private static final String A_U_O_privatePhoneKos = AttributesManager.NS_USER_ATTR_OPT + ":privatePhoneKos";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {

		Attribute attribute = new Attribute(attributeDefinition);

		try {

			Attribute sourceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_D_phoneDc2);
			if (sourceAttribute.getValue() != null) {
				attribute.setValue(sourceAttribute.getValue());
				return attribute;
			}

			sourceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_O_mobilePhone);
			if (sourceAttribute.getValue() != null) {
				attribute.setValue(sourceAttribute.getValue());
				return attribute;
			}

			sourceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_O_privatePhone);
			if (sourceAttribute.getValue() != null) {
				attribute.setValue(sourceAttribute.getValue());
				return attribute;
			}

			sourceAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_O_privatePhoneKos);
			if (sourceAttribute.getValue() != null) {
				attribute.setValue(sourceAttribute.getValue());
				return attribute;
			}

			return attribute;

		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<>();
		strongDependencies.add(A_U_D_phoneDc2);
		strongDependencies.add(A_U_O_mobilePhone);
		strongDependencies.add(A_U_O_privatePhone);
		strongDependencies.add(A_U_O_privatePhoneKos);
		return strongDependencies;
	}

	@Override
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
