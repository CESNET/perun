package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForGroup;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForGroup;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForGroup;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.DirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.IndirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberExpiredInGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberRemovedFromGroupTotally;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberValidatedInGroup;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberDisabled;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberExpired;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberInvalidated;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberSuspended;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberValidated;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All affiliations collected from: - UserExtSources attributes -
 * urn:perun:user:attribute-def:def:voPersonExternalAffiliationManuallyAssigned -
 * urn:perun:group:attribute-def:def:groupAffiliations
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_voPersonExternalAffiliation
    extends UserVirtualAttributeCollectedFromUserExtSource {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final Pattern userAllAttrsRemovedPattern =
      Pattern.compile("All attributes removed for User:\\[(.*)]", Pattern.DOTALL);
  private final Pattern userEPSAMASetPattern = Pattern.compile(
      "Attribute:\\[(.*)friendlyName=<" + getSecondarySourceAttributeFriendlyName() + ">(.*)] set for User:\\[(.*)]",
      Pattern.DOTALL);
  private final Pattern userEPSAMARemovePattern = Pattern.compile(
      "AttributeDefinition:\\[(.*)friendlyName=<" + getSecondarySourceAttributeFriendlyName() +
      ">(.*)] removed for User:\\[(.*)]", Pattern.DOTALL);

  // format has to match the format in Perun-wui setAffiliation miniapp (method createAssignedAffiliationsAttribute)
  private static final String VALIDITY_DATE_FORMAT = "yyyy-MM-dd";

  /**
   * Collect affiliations from perun Groups
   *
   * @param sess Perun session
   * @param user User for whom the values should be collected
   * @return Set of collected affiliations
   * @throws InternalErrorException When some error occurs, see exception cause for details.
   */
  private Set<String> getAffiliationsFromGroups(PerunSessionImpl sess, User user) {
    Set<String> result = new HashSet<>();

    List<Member> validVoMembers =
        sess.getPerunBl().getMembersManagerBl().getMembersByUserWithStatus(sess, user, Status.VALID);

    GroupsManagerBl groupsManagerBl = sess.getPerunBl().getGroupsManagerBl();

    Set<Group> groupsForAttrCheck = new HashSet<>();
    for (Member member : validVoMembers) {
      groupsForAttrCheck.addAll(groupsManagerBl.getGroupsWhereMemberIsActive(sess, member));
    }

    if (!groupsForAttrCheck.isEmpty()) {
      try {
        // check, if attribute exists
        sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, getTertiarySourceAttributeName());
      } catch (AttributeNotExistsException e) {
        log.debug("Attribute " + getTertiarySourceAttributeFriendlyName() + " does not exist", e);
        return result;
      }
    }

    for (Group group : groupsForAttrCheck) {
      try {
        Attribute groupAffiliations =
            sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, getTertiarySourceAttributeName());
        if (groupAffiliations != null && groupAffiliations.valueAsList() != null) {
          result.addAll(groupAffiliations.valueAsList());
        }
      } catch (WrongAttributeAssignmentException e) {
        throw new InternalErrorException(
            "Wrong assignment of " + getTertiarySourceAttributeFriendlyName() + " for user " + user.getId(), e);
      } catch (AttributeNotExistsException e) {
        log.debug("Attribute " + getTertiarySourceAttributeFriendlyName() + " of group " + group.getId() +
                  " does not exist, values will be skipped", e);
      }
    }

    return result;
  }

  /**
   * Collect manually assigned affiliations
   *
   * @param sess Perun session
   * @param user User for whom the values should be collected
   * @return Set of collected affiliations
   * @throws InternalErrorException When some error occurs, see exception cause for details.
   */
  private Set<String> getAffiliationsManuallyAssigned(PerunSessionImpl sess, User user) {
    Set<String> result = new HashSet<>();

    Attribute manualEPSAAttr = null;
    try {
      manualEPSAAttr =
          sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, getSecondarySourceAttributeName());
    } catch (WrongAttributeAssignmentException e) {
      throw new InternalErrorException(
          "Wrong assignment of " + getSecondarySourceAttributeFriendlyName() + " for user " + user.getId(), e);
    } catch (AttributeNotExistsException e) {
      log.debug("Attribute " + getSecondarySourceAttributeFriendlyName() + " of user " + user.getId() +
                " does not exist, values will be skipped", e);
    }

    if (manualEPSAAttr != null) {
      Map<String, String> value = manualEPSAAttr.valueAsMap();
      if (value != null) {

        LocalDate now = LocalDate.now();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(VALIDITY_DATE_FORMAT);
        for (Map.Entry<String, String> entry : value.entrySet()) {
          LocalDate expiration = LocalDate.parse(entry.getValue(), dateFormat);

          if (!now.isAfter(expiration)) {
            result.add(entry.getKey());
          }
        }
      }
    }

    return result;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user,
                                     AttributeDefinition destinationAttributeDefinition) {
    //get already filled value obtained from UserExtSources
    Attribute attribute = super.getAttributeValue(sess, user, destinationAttributeDefinition);

    Attribute destinationAttribute = new Attribute(destinationAttributeDefinition);
    //get values previously obtained and add them to Set representing final value
    //for values use set because of avoiding duplicities
    Set<String> valuesWithoutDuplicities = new HashSet<>(attribute.valueAsList());

    valuesWithoutDuplicities.addAll(getAffiliationsManuallyAssigned(sess, user));
    valuesWithoutDuplicities.addAll(getAffiliationsFromGroups(sess, user));

    // remove duplicities, by accepting only the first occurrence of each value (other occurences, case-insensitive,
    // will be removed)
    Set<String> valuesWithoutDuplicitiesCaseInsensitive = new HashSet<>();
    for (String value : valuesWithoutDuplicities) {
      boolean isDuplicity = valuesWithoutDuplicitiesCaseInsensitive.stream().anyMatch(value::equalsIgnoreCase);
      if (isDuplicity) {
        continue;
      }
      valuesWithoutDuplicitiesCaseInsensitive.add(value);
    }

    //convert set to list (values in list will be without duplicities)
    destinationAttribute.setValue(new ArrayList<>(valuesWithoutDuplicitiesCaseInsensitive));
    return destinationAttribute;
  }

  @Override
  public String getDestinationAttributeFriendlyName() {
    return "voPersonExternalAffiliation";
  }

  @Override
  public List<AttributeHandleIdentifier> getHandleIdentifiers() {

    List<AttributeHandleIdentifier> handleIdentifiers = super.getHandleIdentifiers();

    // member related events
    handleIdentifiers.add(auditEvent -> {

      if (auditEvent instanceof DirectMemberAddedToGroup) {
        return ((DirectMemberAddedToGroup) auditEvent).getMember().getUserId();
      } else if (auditEvent instanceof IndirectMemberAddedToGroup) {
        return ((IndirectMemberAddedToGroup) auditEvent).getMember().getUserId();
      } else if (auditEvent instanceof MemberRemovedFromGroupTotally) {
        return ((MemberRemovedFromGroupTotally) auditEvent).getMember().getUserId();
      } else if (auditEvent instanceof MemberExpiredInGroup) {
        return ((MemberExpiredInGroup) auditEvent).getMember().getUserId();
      } else if (auditEvent instanceof MemberValidatedInGroup) {
        return ((MemberValidatedInGroup) auditEvent).getMember().getUserId();
      } else if (auditEvent instanceof MemberValidated) {
        return ((MemberValidated) auditEvent).getMember().getUserId();
      } else if (auditEvent instanceof MemberExpired) {
        return ((MemberExpired) auditEvent).getMember().getUserId();
      } else if (auditEvent instanceof MemberSuspended) {
        return ((MemberSuspended) auditEvent).getMember().getUserId();
      } else if (auditEvent instanceof MemberDisabled) {
        return ((MemberDisabled) auditEvent).getMember().getUserId();
      } else if (auditEvent instanceof MemberInvalidated) {
        return ((MemberInvalidated) auditEvent).getMember().getUserId();
      }

      // no match
      return null;

    });

    return handleIdentifiers;
  }

  /**
   * Get friendly name of secondary source attribute
   *
   * @return friendly name of secondary source attribute
   */
  public String getSecondarySourceAttributeFriendlyName() {
    return "voPersonExternalAffiliationManuallyAssigned";
  }

  /**
   * Get name of secondary source attribute
   *
   * @return name of secondary source attribute
   */
  public String getSecondarySourceAttributeName() {
    return AttributesManager.NS_USER_ATTR_DEF + ":" + getSecondarySourceAttributeFriendlyName();
  }

  @Override
  public String getSourceAttributeFriendlyName() {
    return "affiliation";
  }

  /**
   * Get friendly name of tertiary source attribute
   *
   * @return friendly name of tertiary source attribute
   */
  public String getTertiarySourceAttributeFriendlyName() {
    return "groupAffiliations";
  }

  /**
   * Get name of tertiary source attribute
   *
   * @return name of secondary source attribute
   */
  public String getTertiarySourceAttributeName() {
    return AttributesManager.NS_GROUP_ATTR_DEF + ":" + getTertiarySourceAttributeFriendlyName();
  }

  @Override
  public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message)
      throws WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {

    // generic handling
    List<AuditEvent> resolvingMessages = super.resolveVirtualAttributeValueChange(perunSession, message);

    // handle source user attribute changes

    if (message instanceof AttributeSetForUser && ((AttributeSetForUser) message).getAttribute().getFriendlyName()
        .equals(getSecondarySourceAttributeFriendlyName())) {

      AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl()
          .getAttributeDefinition(perunSession, getDestinationAttributeName());
      resolvingMessages.add(
          new AttributeChangedForUser(new Attribute(attributeDefinition), ((AttributeSetForUser) message).getUser()));

    } else if (message instanceof AttributeRemovedForUser &&
               ((AttributeRemovedForUser) message).getAttribute().getFriendlyName()
                   .equals(getSecondarySourceAttributeFriendlyName())) {

      AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl()
          .getAttributeDefinition(perunSession, getDestinationAttributeName());
      resolvingMessages.add(new AttributeChangedForUser(new Attribute(attributeDefinition),
          ((AttributeRemovedForUser) message).getUser()));

    } else if (message instanceof AllAttributesRemovedForUser) {

      boolean skip = false;
      try {
        AttributeDefinition sourceExists = perunSession.getPerunBl().getAttributesManagerBl()
            .getAttributeDefinition(perunSession, getSecondarySourceAttributeName());
        User user = perunSession.getPerunBl().getUsersManagerBl()
            .getUserById(perunSession, ((AllAttributesRemovedForUser) message).getUser().getId());
      } catch (AttributeNotExistsException | UserNotExistsException ex) {
        // silently skip this event, since source attribute couldn't be between deleted
        // or user no longer exist
        skip = true;
      }

      if (!skip) {
        AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl()
            .getAttributeDefinition(perunSession, getDestinationAttributeName());
        resolvingMessages.add(new AttributeChangedForUser(new Attribute(attributeDefinition),
            ((AllAttributesRemovedForUser) message).getUser()));
      }

    }

    // handle group attr changes, exclude "members" group, since its not counted within attr value

    if (message instanceof AttributeSetForGroup &&
        !VosManager.MEMBERS_GROUP.equals(((AttributeSetForGroup) message).getGroup().getName()) &&
        ((AttributeSetForGroup) message).getAttribute().getName().equals(getTertiarySourceAttributeName())) {

      AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl()
          .getAttributeDefinition(perunSession, getDestinationAttributeName());
      // TODO - get only active group users, since expired are not affected by current group affiliations
      List<User> users = perunSession.getPerunBl().getGroupsManagerBl()
          .getGroupUsers(perunSession, ((AttributeSetForGroup) message).getGroup());
      for (User user : users) {
        resolvingMessages.add(new AttributeChangedForUser(new Attribute(attributeDefinition), user));
      }

    } else if (message instanceof AttributeRemovedForGroup &&
               !VosManager.MEMBERS_GROUP.equals(((AttributeRemovedForGroup) message).getGroup().getName()) &&
               ((AttributeRemovedForGroup) message).getAttribute().getName().equals(getTertiarySourceAttributeName())) {

      AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl()
          .getAttributeDefinition(perunSession, getDestinationAttributeName());
      // TODO - get only active group users, since expired are not affected by current group affiliations
      List<User> users = perunSession.getPerunBl().getGroupsManagerBl()
          .getGroupUsers(perunSession, ((AttributeRemovedForGroup) message).getGroup());
      for (User user : users) {
        resolvingMessages.add(new AttributeChangedForUser(new Attribute(attributeDefinition), user));
      }

    } else if (message instanceof AllAttributesRemovedForGroup &&
               !VosManager.MEMBERS_GROUP.equals(((AllAttributesRemovedForGroup) message).getGroup().getName())) {

      boolean skip = false;
      try {
        AttributeDefinition sourceExists = perunSession.getPerunBl().getAttributesManagerBl()
            .getAttributeDefinition(perunSession, getTertiarySourceAttributeName());
        Group group = perunSession.getPerunBl().getGroupsManagerBl()
            .getGroupById(perunSession, ((AllAttributesRemovedForGroup) message).getGroup().getId());
      } catch (AttributeNotExistsException | GroupNotExistsException ex) {
        // silently skip this event, since source attribute couldn't be between deleted
        // or group no longer exist.
        skip = true;
      }

      if (!skip) {
        AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl()
            .getAttributeDefinition(perunSession, getDestinationAttributeName());
        // TODO - get only active group users, since expired are not affected by current group affiliations
        List<User> users = perunSession.getPerunBl().getGroupsManagerBl()
            .getGroupUsers(perunSession, ((AllAttributesRemovedForGroup) message).getGroup());
        for (User user : users) {
          resolvingMessages.add(new AttributeChangedForUser(new Attribute(attributeDefinition), user));
        }
      }

    }

    // case AllAttributesRemovedForGroup doesn't matter, everything was processed by removing members before group
    // deletion

    return resolvingMessages;

  }

}
