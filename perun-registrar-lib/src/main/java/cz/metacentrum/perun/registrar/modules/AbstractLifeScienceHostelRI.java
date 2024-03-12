package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract module for VO lifescience_hostel used in the LifeScience (LS AAI) Perun instance. Creates UES with LS Hostel
 * identity and adds user to the lifescience VO directly when the application is approved. The concrete implementation
 * has to specify some properties, as this module can be re-used by various instances (e.g. production and acceptance
 * instances)
 *
 * @author Pavel Vyskocil <Pavel.Vyskocil@cesnet.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @see cz.metacentrum.perun.registrar.modules.LifeScienceHostelRI for production instance module
 * @see cz.metacentrum.perun.registrar.modules.LifeScienceHostelRIAcc for acceptance instance module
 */
public abstract class AbstractLifeScienceHostelRI extends DefaultRegistrarModule {

  private static final Logger LOG = LoggerFactory.getLogger(LifescienceHostel.class);

  private static final String VO_SHORTNAME = "lifescience";

  private static final String LOGIN_NAMESPACE = "login-namespace:lifescienceid-username";

  private static final String AUIDS_ATTRIBUTE = "urn:perun:ues:attribute-def:def:additionalIdentifiers";


  /**
   * Create proper UserExtSource
   */
  @Override
  public Application approveApplication(PerunSession session, Application app)
      throws PrivilegeException, GroupNotExistsException, MemberNotExistsException, ExternallyManagedException,
      WrongReferenceAttributeValueException, WrongAttributeValueException, RegistrarException,
      ExtSourceNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, VoNotExistsException,
      ExtendMembershipException, AlreadyMemberException {
    PerunBl perun = (PerunBl) session.getPerun();

    User user = app.getUser();
    if (user != null) {
      // Create UES for user
      Attribute userLogin = perun.getAttributesManagerBl()
          .getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":" + LOGIN_NAMESPACE);
      if (userLogin != null && userLogin.getValue() != null) {
        UserExtSource ues = storeAndGetUserExtSource(session, user, userLogin, perun);
        setAuidIntoUesAttributes(session, ues, perun);
      }

      if (Application.AppType.INITIAL.equals(app.getType())) {
        try {
          Vo vo = perun.getVosManagerBl().getVoByShortName(session, VO_SHORTNAME);
          Member member = perun.getMembersManagerBl().createMember(session, vo, user);
          perun.getMembersManagerBl().validateMemberAsync(session, member);
          LOG.debug("LS Hostel member added to the main VO Lifescience {}", member);
        } catch (VoNotExistsException e) {
          LOG.warn("VO: " + VO_SHORTNAME + " not exists, can't add member into it.");
        } catch (AlreadyMemberException ignore) {
          // user is already in lifescience
        } catch (ExtendMembershipException e) {
          // can't be member of lifescience, shouldn't happen
          LOG.error("LS Hostel member can't be added to VO: " + VO_SHORTNAME, e);
        }
      }
      // User doesn't have login - don't set UES
    }

    return app;
  }

  /**
   * Get name of the ExtSource for which the login is generated
   *
   * @return ExtSource name
   */
  protected abstract String getExtSourceName();

  /**
   * Get scope part of the user login (including @ character)
   *
   * @return scope
   */
  protected abstract String getScope();

  /**
   * Stores the user login from passed UserExtSource into the AUIDS attribute (constant AUIDS_ATTRIBUTE).
   */
  private void setAuidIntoUesAttributes(PerunSession session, UserExtSource ues, PerunBl perun)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, PrivilegeException,
      WrongAttributeValueException {
    try {
      Attribute auidsAttr = perun.getAttributesManager().getAttribute(session, ues, AUIDS_ATTRIBUTE);
      Set<String> attrValue = new HashSet<>();
      if (auidsAttr.getValue() != null && auidsAttr.valueAsList() != null && !auidsAttr.valueAsList().isEmpty()) {
        attrValue.addAll(auidsAttr.valueAsList());
      }
      attrValue.add(ues.getLogin());
      auidsAttr.setValue(new ArrayList<>(attrValue));
      perun.getAttributesManager().setAttribute(session, ues, auidsAttr);
    } catch (UserExtSourceNotExistsException e) {
      // should not happen
    } catch (AttributeNotExistsException e) {
      // ok, attribute is probably not used
    }
  }

  /**
   * Creates the UserExtSource object with user login and returns it. If the UES already exists, method just returns
   * it.
   */
  private UserExtSource storeAndGetUserExtSource(PerunSession session, User user, Attribute userLogin, PerunBl perun)
      throws ExtSourceNotExistsException {
    ExtSource extSource = perun.getExtSourcesManagerBl().getExtSourceByName(session, getExtSourceName());
    String login = userLogin.valueAsString();
    UserExtSource ues = new UserExtSource(extSource, login + getScope());
    ues.setLoa(0);

    try {
      ues = perun.getUsersManagerBl().addUserExtSource(session, user, ues);
    } catch (UserExtSourceExistsException ex) {
      try {
        ues = perun.getUsersManagerBl().getUserExtSourceByExtLogin(session, extSource, login);
      } catch (UserExtSourceNotExistsException e) {
        // should not happen due to parent catch block
      }
    }
    return ues;
  }

}

