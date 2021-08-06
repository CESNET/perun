package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Get all student identifiers (as list) from any Group where user is valid Member.
 *
 * @author Ľuboslav Halama lubo.halama@gmail.com
 */
public class urn_perun_user_attribute_def_virt_studentIdentifiers extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private static final String studentIdentifiersValuePrefix = "urn:schac:personalUniqueCode:int:esi:";

	private static final String organizationNamespaceFriendlyName = "organizationNamespace";
	private static final String organizationScopeFriendlyName = "organizationScope";

	private static final String A_G_D_organizationNamespaceFriendlyName = AttributesManager.NS_GROUP_ATTR_DEF + ":" + organizationNamespaceFriendlyName;
	private static final String A_G_D_organizationScopeFriendlyName = AttributesManager.NS_GROUP_ATTR_DEF + ":" + organizationScopeFriendlyName;

	private static final String A_U_D_loginNamespaceFriendlyNamePrefix = AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":";

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_studentIdentifiers.class);

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
		Attribute attribute = new Attribute(attributeDefinition);

		List<Group> userValidGroups = sess.getPerunBl().getMembersManagerBl()
			.getMembersByUserWithStatus(sess, user, Status.VALID).stream()
			.flatMap(validMember -> sess.getPerunBl().getGroupsManagerBl().getGroupsWhereMemberIsActive(sess, validMember).stream())
			.toList();

		Attribute organizationNamespace;
		Attribute organizationScope;
		Attribute userLoginID;
		List<String> values = new ArrayList<>();

		// use only groups with specified namespace and scope
		for (Group group : userValidGroups) {

			organizationScope = this.tryGetAttribute(sess, group, A_G_D_organizationScopeFriendlyName);
			if (organizationScope == null || organizationScope.getValue() == null) {
				continue;
			}

			organizationNamespace = this.tryGetAttribute(sess, group, A_G_D_organizationNamespaceFriendlyName);
			if (organizationNamespace == null || organizationNamespace.getValue() == null) {
				continue;
			}

			userLoginID = this.tryGetAttribute(sess, user, A_U_D_loginNamespaceFriendlyNamePrefix + organizationNamespace.valueAsString());
			if (userLoginID == null || userLoginID.getValue() == null) {
				continue;
			}

			values.add(studentIdentifiersValuePrefix + organizationScope.valueAsString() + ":" + userLoginID.valueAsString());
		}

		attribute.setValue(values);

		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("studentIdentifiers");
		attr.setDisplayName("student identifiers");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("List of student identifiers from all organisations.");
		return attr;
	}

	private Attribute tryGetAttribute(PerunSessionImpl sess, Group group, String attributeName) {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, attributeName);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException("Wrong assignment of " + attributeName + " for group " + group.getId(), e);
		} catch (AttributeNotExistsException e) {
			log.debug("Attribute " + attributeName + " of group " + group.getId() + " does not exist, values will be skipped", e);
		}
		return null;
	}

	private Attribute tryGetAttribute(PerunSessionImpl sess, User user, String attributeName) {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, attributeName);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException("Wrong assignment of " + attributeName + " for user " + user.getId(), e);
		} catch (AttributeNotExistsException e) {
			log.debug("Attribute " + attributeName + " of user " + user.getId() + " does not exist, values will be skipped", e);
		}
		return null;
	}

}
