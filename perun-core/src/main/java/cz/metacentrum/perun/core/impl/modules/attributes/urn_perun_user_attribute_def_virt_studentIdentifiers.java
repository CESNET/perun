package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.DirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.IndirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberExpiredInGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberRemovedFromGroupTotally;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberValidatedInGroup;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reacts on audit messages about adding, removing... member to group.
 * When member is added or validated it will create userExtSource with appropriate attributes.
 * When member is removed or expired it will remove the userExtSource.
 *
 * @author Ľuboslav Halama lubo.halama@gmail.com
 */
public class urn_perun_user_attribute_def_virt_studentIdentifiers extends UserVirtualAttributesModuleAbstract
    implements UserVirtualAttributesModuleImplApi {

  private static final String studentIdentifiersValuePrefix = "urn:schac:personalUniqueCode:int:esi:";
  private static final String affiliationPrefix = "student@";

  private static final String organizationNamespaceFriendlyName = "organizationNamespace";
  private static final String organizationScopeFriendlyName = "organizationScope";
  private static final String organizationPersonalCodeScopeFriendlyName = "organizationPersonalCodeScope";

  private static final String schacHomeOrganizationFriendlyName = "schacHomeOrganization";
  private static final String voPersonExternalAffiliationFriendlyName = "affiliation";
  private static final String schacPersonalUniqueCodeFriendlyName = "schacPersonalUniqueCode";

  private static final String A_G_D_organizationNamespaceFriendlyName =
      AttributesManager.NS_GROUP_ATTR_DEF + ":" + organizationNamespaceFriendlyName;
  private static final String A_G_D_organizationScopeFriendlyName =
      AttributesManager.NS_GROUP_ATTR_DEF + ":" + organizationScopeFriendlyName;
  private static final String A_G_D_organizationPersonalCodeScopeFriendlyName =
      AttributesManager.NS_GROUP_ATTR_DEF + ":" + organizationPersonalCodeScopeFriendlyName;

  private static final String A_U_D_loginNamespaceFriendlyNamePrefix =
      AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":";

  private static final String A_UES_D_schacHomeOrganizationFriendlyName =
      AttributesManager.NS_UES_ATTR_DEF + ":" + schacHomeOrganizationFriendlyName;
  private static final String A_UES_D_voPersonExternalAffiliationFriendlyName =
      AttributesManager.NS_UES_ATTR_DEF + ":" + voPersonExternalAffiliationFriendlyName;
  private static final String A_UES_D_schacPersonalUniqueCodeFriendlyName =
      AttributesManager.NS_UES_ATTR_DEF + ":" + schacPersonalUniqueCodeFriendlyName;

  private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_studentIdentifiers.class);

  @Override
  public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl sess, AuditEvent message)
      throws AttributeNotExistsException, WrongAttributeAssignmentException {
    List<AuditEvent> resolvingMessages = new ArrayList<>();
    if (message == null) {
      return resolvingMessages;
    }

    if (message instanceof DirectMemberAddedToGroup) {
      processAddUserExtSource(sess, ((DirectMemberAddedToGroup) message).getGroup(),
          ((DirectMemberAddedToGroup) message).getMember());
    } else if (message instanceof IndirectMemberAddedToGroup) {
      processAddUserExtSource(sess, ((IndirectMemberAddedToGroup) message).getGroup(),
          ((IndirectMemberAddedToGroup) message).getMember());
    } else if (message instanceof MemberValidatedInGroup) {
      processAddUserExtSource(sess, ((MemberValidatedInGroup) message).getGroup(),
          ((MemberValidatedInGroup) message).getMember());
    } else if (message instanceof MemberRemovedFromGroupTotally) {
      processRemoveUserExtSource(sess, ((MemberRemovedFromGroupTotally) message).getGroup(),
          ((MemberRemovedFromGroupTotally) message).getMember());
    } else if (message instanceof MemberExpiredInGroup) {
      processRemoveUserExtSource(sess, ((MemberExpiredInGroup) message).getGroup(),
          ((MemberExpiredInGroup) message).getMember());
    }

    return resolvingMessages;
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("studentIdentifiers");
    attr.setDisplayName("student identifiers");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("Virtual attribute, which creates and removes userExtSource" +
        " with identifiers according to audit events.");
    return attr;
  }

  /**
   * Set userExtSource with attributes for member's user if not exists.
   *
   * @param sess   Perun session
   * @param group  from which appropriate attributes will be obtained
   * @param member for which the xtSource with attributes will be processed
   */
  private void processAddUserExtSource(PerunSessionImpl sess, Group group, Member member) {
    User user = sess.getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
    Attribute organizationScope = tryGetAttribute(sess, group, A_G_D_organizationScopeFriendlyName);
    if (organizationScope == null || organizationScope.getValue() == null) {
      return;
    }
    String schacPersonalCodeScope = organizationScope.valueAsString();
    Attribute organizationNamespace = this.tryGetAttribute(sess, group, A_G_D_organizationNamespaceFriendlyName);
    if (organizationNamespace == null || organizationNamespace.getValue() == null) {
      return;
    }
    Attribute userLoginID =
        tryGetAttribute(sess, user, A_U_D_loginNamespaceFriendlyNamePrefix + organizationNamespace.valueAsString());
    if (userLoginID == null || userLoginID.getValue() == null) {
      return;
    }
    // In case this attribute exists and it is filled, it will be used in SPUC attribute instead of sHO
    Attribute organizationPersonalCodeScope =
        tryGetAttribute(sess, group, A_G_D_organizationPersonalCodeScopeFriendlyName);
    if (organizationPersonalCodeScope != null && organizationPersonalCodeScope.getValue() != null) {
      schacPersonalCodeScope = organizationPersonalCodeScope.valueAsString();
    }
    ExtSource extSource = tryGetExtSource(sess, organizationScope.valueAsString());
    //Create and set userExtSource if not exists
    try {
      sess.getPerunBl().getUsersManagerBl().getUserExtSourceByExtLogin(sess, extSource, userLoginID.valueAsString());
    } catch (UserExtSourceNotExistsException e) {
      UserExtSource ues = new UserExtSource(extSource, userLoginID.valueAsString());
      try {
        ues = sess.getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, ues);
      } catch (UserExtSourceExistsException userExtSourceExistsException) {
        //Should not happen
        throw new InternalErrorException(e);
      }
      Attribute schacHomeOrganization = tryGetAttribute(sess, ues, A_UES_D_schacHomeOrganizationFriendlyName);
      Attribute voPersonExternalAffiliation =
          tryGetAttribute(sess, ues, A_UES_D_voPersonExternalAffiliationFriendlyName);
      Attribute schacPersonalUniqueCode = tryGetAttribute(sess, ues, A_UES_D_schacPersonalUniqueCodeFriendlyName);

      schacHomeOrganization.setValue(organizationScope.valueAsString());
      voPersonExternalAffiliation.setValue(affiliationPrefix + organizationScope.valueAsString());
      List<String> spucValue = new ArrayList<>();
      spucValue.add(studentIdentifiersValuePrefix + schacPersonalCodeScope + ":" + userLoginID.valueAsString());
      schacPersonalUniqueCode.setValue(spucValue);

      try {
        sess.getPerunBl().getAttributesManagerBl().setAttributes(sess, ues,
            Arrays.asList(schacHomeOrganization, voPersonExternalAffiliation, schacPersonalUniqueCode));
      } catch (WrongAttributeValueException | WrongAttributeAssignmentException |
               WrongReferenceAttributeValueException ex) {
        //Should not happen
        throw new InternalErrorException(ex);
      }
    }
  }

  /**
   * Remove userExtSource with attributes for member's user if exists.
   *
   * @param sess   Perun session
   * @param group  from which appropriate attributes will be obtained
   * @param member for which the xtSource with attributes will be processed
   */
  private void processRemoveUserExtSource(PerunSessionImpl sess, Group group, Member member) {
    User user = sess.getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
    Attribute organizationScope = tryGetAttribute(sess, group, A_G_D_organizationScopeFriendlyName);
    if (organizationScope == null || organizationScope.getValue() == null) {
      return;
    }
    Attribute organizationNamespace = this.tryGetAttribute(sess, group, A_G_D_organizationNamespaceFriendlyName);
    if (organizationNamespace == null || organizationNamespace.getValue() == null) {
      return;
    }
    Attribute userLoginID =
        tryGetAttribute(sess, user, A_U_D_loginNamespaceFriendlyNamePrefix + organizationNamespace.valueAsString());
    if (userLoginID == null || userLoginID.getValue() == null) {
      return;
    }
    ExtSource extSource = tryGetExtSource(sess, organizationScope.valueAsString());
    //Remove userExtSource if exists
    try {
      UserExtSource ues = sess.getPerunBl().getUsersManagerBl()
          .getUserExtSourceByExtLogin(sess, extSource, userLoginID.valueAsString());
      sess.getPerunBl().getUsersManagerBl().removeUserExtSource(sess, user, ues);
    } catch (UserExtSourceNotExistsException e) {
      //Means that the ues was already removed, which is ok
    } catch (UserExtSourceAlreadyRemovedException e) {
      //Should not happened
      throw new InternalErrorException(e);
    }
  }

  /**
   * Fetches ExtSource from the database.
   *
   * @param sess PerunSession
   * @param name ExtSource name
   * @return ExtSource
   */
  private ExtSource tryGetExtSource(PerunSessionImpl sess, String name) {
    try {
      return sess.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, name);
    } catch (ExtSourceNotExistsException e) {
      //Should not happened
      throw new InternalErrorException(e);
    }
  }

  /**
   * Fetches ues attribute if it is possible
   *
   * @param sess          PerunSession
   * @param ues           userextSource for which the attribute will be fetched
   * @param attributeName full name of the attribute
   * @return Attribute
   */
  private Attribute tryGetAttribute(PerunSessionImpl sess, UserExtSource ues, String attributeName) {
    try {
      return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, ues, attributeName);
    } catch (WrongAttributeAssignmentException e) {
      throw new InternalErrorException("Wrong assignment of " + attributeName + " for userExtSource " + ues.getId(), e);
    } catch (AttributeNotExistsException e) {
      throw new InternalErrorException(
          "Attribute " + attributeName + " for userExtSource " + ues.getId() + " does not exists.", e);
    }
  }

  /**
   * Fetches group attribute if it is possible
   *
   * @param sess          PerunSession
   * @param group         Group for which the attribute will be fetched
   * @param attributeName full name of the attribute
   * @return Attribute
   */
  private Attribute tryGetAttribute(PerunSessionImpl sess, Group group, String attributeName) {
    try {
      return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, attributeName);
    } catch (WrongAttributeAssignmentException e) {
      throw new InternalErrorException("Wrong assignment of " + attributeName + " for group " + group.getId(), e);
    } catch (AttributeNotExistsException e) {
      log.debug("Attribute " + attributeName + " of group " + group.getId() + " does not exist, values will be skipped",
          e);
    }
    return null;
  }

  /**
   * Fetches user attribute if it is possible
   *
   * @param sess          PerunSession
   * @param user          User for which the attribute will be fetched
   * @param attributeName full name of the attribute
   * @return Attribute
   */
  private Attribute tryGetAttribute(PerunSessionImpl sess, User user, String attributeName) {
    try {
      return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, attributeName);
    } catch (WrongAttributeAssignmentException e) {
      throw new InternalErrorException("Wrong assignment of " + attributeName + " for user " + user.getId(), e);
    } catch (AttributeNotExistsException e) {
      log.debug("Attribute " + attributeName + " of user " + user.getId() + " does not exist, values will be skipped",
          e);
    }
    return null;
  }

}
