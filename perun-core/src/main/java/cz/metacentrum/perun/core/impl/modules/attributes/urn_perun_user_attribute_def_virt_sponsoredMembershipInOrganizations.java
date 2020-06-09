package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.MembersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Get all records (as list) of attribute group:def:sponsorOrganizationIdentifier
 * from any Group where user is valid Member.
 *
 * @author Michal Stava stavamichal@gmail.com
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_sponsoredMembershipInOrganizations extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private static final String sponsorOrganizationIdentifierFriendlyName = "sponsorOrganizationIdentifier";
	private static final String A_G_D_sponsorOrganizationIdentifier = AttributesManager.NS_GROUP_ATTR_DEF + ":" + sponsorOrganizationIdentifierFriendlyName;
	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_sponsoredMembershipInOrganizations.class);

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
		Attribute attribute = new Attribute(attributeDefinition);
		attribute.setValue(getSponsorOrganizationIdentifiersFromGroups(sess, user));
		return attribute;
	}

	/**
	 * Collect sponsor organization's identifiers from perun Groups
	 * @param sess Perun session
	 * @param user User for whom the values should be collected
	 * @return List of collected identifiers
	 * @throws InternalErrorException When some error occurs, see exception cause for details.
	 */
	private List<String> getSponsorOrganizationIdentifiersFromGroups(PerunSessionImpl sess, User user) {
		GroupsManagerBl groupsManagerBl = sess.getPerunBl().getGroupsManagerBl();
		MembersManagerBl membersManagerBl = sess.getPerunBl().getMembersManagerBl();

		return membersManagerBl.getMembersByUserWithStatus(sess, user, Status.VALID).stream()
			.flatMap(validMember -> groupsManagerBl.getGroupsWhereMemberIsActive(sess, validMember).stream())
			.map(groupWhereMemberIsActive -> getOrganizationIdentifierAttribute(sess, groupWhereMemberIsActive, user))
			.filter(Objects::nonNull)
			.map(Attribute::valueAsString)
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
	}

	/**
	 * Return attribute organization identifier for group.
	 * Return null if attribute definition does not exist.
	 *
	 * @param sess
	 * @param group group to get attribute for
	 * @param user user to add context information about in exceptions
	 * @return attribute of organization for group or null if definition does not exist in Perun
	 */
	private Attribute getOrganizationIdentifierAttribute(PerunSessionImpl sess, Group group, User user) {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_D_sponsorOrganizationIdentifier);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException("Wrong assignment of " + A_G_D_sponsorOrganizationIdentifier + " for user " + user.getId(), e);
		} catch (AttributeNotExistsException e) {
			log.debug("Attribute " + A_G_D_sponsorOrganizationIdentifier + " of group " + group.getId() + " does not exist, values will be skipped", e);
		}
		return null;
	}

	@Override
	public List<String> getStrongDependencies() {
		return Arrays.asList(A_G_D_sponsorOrganizationIdentifier);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("sponsoredMembershipInOrganizations");
		attr.setDisplayName("Sponsored membership in organizations");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("List of organization identifiers where user has sponsored membership.");
		return attr;
	}

}
