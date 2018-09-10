package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.util.*;

/**
 * Get administrator ssh keys for openNebula from attribute sshPublicAdminKey if it is not empty
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_openNebulaSSHAdminKeys extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private static final String A_U_sshPublicAdminKey = AttributesManager.NS_USER_ATTR_DEF + ":sshPublicAdminKey";

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		List<String> userNebulaSSHAdminKeys = new ArrayList<>();

		Attribute userSSHAdminKeys;
		try {
			userSSHAdminKeys = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_sshPublicAdminKey);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		if(userSSHAdminKeys.getValue() != null) {
			userNebulaSSHAdminKeys = (ArrayList<String>) userSSHAdminKeys.getValue();
		}

		attribute.setValue(userNebulaSSHAdminKeys);
		return attribute;
	}

	@Override
	public List<String> getStrongDependencies() {
		return Collections.singletonList(A_U_sshPublicAdminKey);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("openNebulaSSHAdminKeys");
		attr.setDisplayName("Open Nebula SSH Admin keys");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("List of user's ssh admin keys if any exists.");
		return attr;
	}
}
