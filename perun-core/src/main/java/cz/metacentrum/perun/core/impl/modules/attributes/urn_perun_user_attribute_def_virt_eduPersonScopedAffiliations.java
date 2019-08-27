package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * All affiliations collected from:
 *  - UserExtSources attributes
 *  - urn:perun:user:attribute-def:def:eduPersonScopedAffiliationsManuallyAssigned
 *  - urn:perun:group:attribute-def:def:groupAffiliations
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations extends UserVirtualAttributeCollectedFromUserExtSource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Pattern userAllAttrsRemovedPattern = Pattern.compile("All attributes removed for User:\\[(.*)]", Pattern.DOTALL);
	private final Pattern userEPSAMASetPattern = Pattern.compile("Attribute:\\[(.*)friendlyName=<" + getSecondarySourceAttributeFriendlyName() +">(.*)] set for User:\\[(.*)]", Pattern.DOTALL);
	private final Pattern userEPSAMARemovePattern = Pattern.compile("AttributeDefinition:\\[(.*)friendlyName=<" + getSecondarySourceAttributeFriendlyName() + ">(.*)] removed for User:\\[(.*)]", Pattern.DOTALL);

	// format has to match the format in Perun-wui setAffiliation miniapp (method createAssignedAffiliationsAttribute)
	private final String VALIDITY_DATE_FORMAT = "yyyy-MM-dd";

	@Override
	public String getSourceAttributeFriendlyName() {
		return "affiliation";
	}

	/**
	 * Get friendly name of secondary source attribute
	 * @return friendly name of secondary source attribute
	 */
	public String getSecondarySourceAttributeFriendlyName() {
		return "eduPersonScopedAffiliationsManuallyAssigned";
	}

	/**
	 * Get name of secondary source attribute
	 * @return name of secondary source attribute
	 */
	public String getSecondarySourceAttributeName() {
		return AttributesManager.NS_USER_ATTR_DEF + ":" + getSecondarySourceAttributeFriendlyName();
	}

	/**
	 * Get friendly name of tertiary source attribute
	 * @return friendly name of tertiary source attribute
	 */
	public String getTertiarySourceAttributeFriendlyName() {
		return "groupAffiliations";
	}

	/**
	 * Get name of tertiary source attribute
	 * @return name of secondary source attribute
	 */
	public String getTertiarySourceAttributeName() {
		return AttributesManager.NS_GROUP_ATTR_DEF + ":" + getTertiarySourceAttributeFriendlyName();
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "eduPersonScopedAffiliations";
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) throws InternalErrorException {
		//get already filled value obtained from UserExtSources
		Attribute attribute = super.getAttributeValue(sess, user, destinationAttributeDefinition);

		Attribute destinationAttribute = new Attribute(destinationAttributeDefinition);
		//get values previously obtained and add them to Set representing final value
		//for values use set because of avoiding duplicities
		Set<String> valuesWithoutDuplicities = new HashSet<>(attribute.valueAsList());

		valuesWithoutDuplicities.addAll(getAffiliationsManuallyAssigned(sess, user));
		valuesWithoutDuplicities.addAll(getAffiliationsFromGroups(sess, user));

		//convert set to list (values in list will be without duplicities)
		destinationAttribute.setValue(new ArrayList<>(valuesWithoutDuplicities));
		return destinationAttribute;
	}

	/**
	 * Collect manually assigned affiliations
	 * @param sess Perun session
	 * @param user User for whom the values should be collected
	 * @return Set of collected affiliations
	 * @throws InternalErrorException When some error occurs, see exception cause for details.
	 */
	private Set<String> getAffiliationsManuallyAssigned(PerunSessionImpl sess, User user) throws InternalErrorException {
		Set<String> result = new HashSet<>();

		Attribute manualEPSAAttr = null;
		try {
			manualEPSAAttr = sess.getPerunBl().getAttributesManagerBl()
				.getAttribute(sess, user, getSecondarySourceAttributeName());
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException("Wrong assignment of " + getSecondarySourceAttributeFriendlyName() + " for user " + user.getId(), e);
		} catch (AttributeNotExistsException e) {
			log.debug("Attribute " + getSecondarySourceAttributeFriendlyName() + " of user " + user.getId() + " does not exist, values will be skipped", e);
		}

		if (manualEPSAAttr != null) {
			Map<String, String> value = manualEPSAAttr.valueAsMap();
			if (value != null) {

				LocalDate now = LocalDate.now();
				DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(VALIDITY_DATE_FORMAT);
				for (Map.Entry<String, String> entry: value.entrySet()) {
					LocalDate expiration = LocalDate.parse(entry.getValue(), dateFormat);

					if (! now.isAfter(expiration)) {
						result.add(entry.getKey());
					}
				}
			}
		}

		return result;
	}

	/**
	 * Collect affiliations from perun Groups
	 * @param sess Perun session
	 * @param user User for whom the values should be collected
	 * @return Set of collected affiliations
	 * @throws InternalErrorException When some error occurs, see exception cause for details.
	 */
	private Set<String> getAffiliationsFromGroups(PerunSessionImpl sess, User user) throws InternalErrorException {
		Set<String> result = new HashSet<>();

		List<Member> userVoMembers = sess.getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		List<Member> validVoMembers = new ArrayList<>();
		if (userVoMembers != null && !userVoMembers.isEmpty()) {
			for (Member member: userVoMembers) {
				if (member.getStatus() == Status.VALID) {
					validVoMembers.add(member);
				}
			}
		}

		List<Member> groupMembers = new ArrayList<>();
		List<Group> groupsForAttrCheck = new ArrayList<>();
		for (Member member : validVoMembers) {
			List<Group> groups = sess.getPerunBl().getGroupsManagerBl().getMemberGroups(sess, member);
			if (groups == null || groups.isEmpty()) {
				continue;
			}

			for (Group group: groups) {
				try {
					Member groupMember = sess.getPerunBl().getGroupsManagerBl().getGroupMemberById(sess, group, member.getId());
					if (groupMember.getGroupStatus() == MemberGroupStatus.VALID) {
						groupsForAttrCheck.add(group);
					}
				} catch (NotGroupMemberException e) {
					log.debug("User: " + user.getId() + " is not a member of group: " + group.getId() + ", skipping");
				}
			}
		}

		for (Group group: groupsForAttrCheck) {
			try {
				Attribute groupAffiliations = sess.getPerunBl().getAttributesManagerBl()
					.getAttribute(sess, group, getTertiarySourceAttributeName());
				if (groupAffiliations != null && groupAffiliations.valueAsList() != null) {
					result.addAll(groupAffiliations.valueAsList());
				}
			} catch (WrongAttributeAssignmentException e) {
				throw new InternalErrorException("Wrong assignment of " + getTertiarySourceAttributeFriendlyName() + " for user " + user.getId(), e);
			} catch (AttributeNotExistsException e) {
				log.debug("Attribute " + getTertiarySourceAttributeFriendlyName() + " of group " + group.getId() + " does not exist, values will be skipped", e);
			}
		}

		return result;
	}

	@Override
	public List<AttributeHandleIdentifier> getHandleIdentifiers() {
		List<AttributeHandleIdentifier> handleIdentifiers = super.getHandleIdentifiers();
		handleIdentifiers.add(auditEvent -> {
			if (auditEvent instanceof AllAttributesRemovedForUser) {
				return ((AllAttributesRemovedForUser) auditEvent).getUser().getId();
			} else {
				return null;
			}
		});
		handleIdentifiers.add(auditEvent -> {
			if (auditEvent instanceof AttributeSetForUser && ((AttributeSetForUser) auditEvent).getAttribute().getFriendlyName().equals(getSecondarySourceAttributeFriendlyName())) {
				return ((AttributeSetForUser) auditEvent).getUser().getId();
			} else {
				return null;
			}
		});
		handleIdentifiers.add(auditEvent -> {
			if (auditEvent instanceof AttributeRemovedForUser && ((AttributeRemovedForUser) auditEvent).getAttribute().getFriendlyName().equals(getSecondarySourceAttributeFriendlyName())) {
				return ((AttributeRemovedForUser) auditEvent).getUser().getId();
			} else {
				return null;
			}
		});
		return handleIdentifiers;
	}
}
