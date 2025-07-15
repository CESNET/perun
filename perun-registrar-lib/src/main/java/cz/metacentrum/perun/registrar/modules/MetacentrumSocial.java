package cz.metacentrum.perun.registrar.modules;

import static cz.metacentrum.perun.registrar.modules.Metacentrum.EINFRA_IDP;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.exceptions.CantBeSubmittedException;
import cz.metacentrum.perun.registrar.model.Application;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for "Social" group within the Metacentrum VO.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class MetacentrumSocial extends DefaultRegistrarModule {

  private static final Logger LOG = LoggerFactory.getLogger(MetacentrumSocial.class);

  private static final String A_MEMBER_MEMBERSHIP_EXPIRATION =
      AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration";
  private static final String A_MEMBER_GROUP_MEMBERSHIP_EXPIRATION =
      AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration";

  /**
   * Set GROUP MEMBERSHIP EXPIRATION based on the current VO MEMBERSHIP EXPIRATION
   */
  @Override
  public Application approveApplication(PerunSession session, Application app)
      throws MemberNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException,
      WrongAttributeValueException, WrongReferenceAttributeValueException {

    PerunBl perun = (PerunBl) session.getPerun();
    Vo vo = app.getVo();
    User user = app.getUser();
    Member member = perun.getMembersManagerBl().getMemberByUser(session, vo, user);
    Group group = app.getGroup();
    Attribute voExpiration =
        perun.getAttributesManagerBl().getAttribute(session, member, A_MEMBER_MEMBERSHIP_EXPIRATION);
    try {
      Attribute groupExpiration =
          perun.getAttributesManagerBl().getAttribute(session, member, group, A_MEMBER_GROUP_MEMBERSHIP_EXPIRATION);
      groupExpiration.setValue(voExpiration.getValue());
      perun.getAttributesManagerBl().setAttribute(session, member, group, groupExpiration);
      LOG.debug("{} expiration in Group {} aligned with the VO {} expiration: {}", member, group.getName(),
          vo.getName(), groupExpiration.valueAsString());
    } catch (MemberGroupMismatchException e) {
      LOG.error("Member and group should be from the same VO.", e);
      throw new ConsistencyErrorException("Member and group should be from the same VO.", e);
    }
    return app;

  }

  @Override
  public void canBeSubmitted(PerunSession session, Application.AppType appType, Map<String, String> params,
                             Map<String, List<String>> externalParams)
      throws PerunException {

    if (EINFRA_IDP.equals(session.getPerunPrincipal().getExtSourceName())) {
      throw new CantBeSubmittedException("You are currently logged-in using e-INFRA CZ IdP." +
                                         "It can't be used to register or extend membership in Metacentrum. Please " +
                                         "close browser and log-in using different identity provider.",
          "NOT_ELIGIBLE_EINFRAIDP", null, null);
    }

  }

}
