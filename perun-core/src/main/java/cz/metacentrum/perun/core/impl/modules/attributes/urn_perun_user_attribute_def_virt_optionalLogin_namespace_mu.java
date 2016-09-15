package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;

/**
 * Attribute value depends on login-namespace:mu attribute
 *
 * @author Simona Kruppova, Michal Stava
 */
public class urn_perun_user_attribute_def_virt_optionalLogin_namespace_mu extends UserVirtualAttributesModuleAbstract {

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);

		try {
			Attribute loginInMU = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:mu");
			attribute = Utils.copyAttributeToVirtualAttributeWithValue(loginInMU, attribute);
		} catch (AttributeNotExistsException ex) {
			//That means that egi-ui attribute not exists at all, return empty attribute
			return attribute;
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
		return attribute;
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("optionalLogin-namespace:mu");
		attr.setDisplayName("MU login (if available)");
		attr.setType(String.class.getName());
		attr.setDescription("Masaryk University login (if available)");
		return attr;
	}
}
