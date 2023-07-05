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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Module for VO lifescience_hostel on LifeScience Perun machine
 *
 * On approval create UES with LS Hostel identity and add user to the lifescience VO directly.
 *
 * @author Pavel Vyskocil <Pavel.Vyskocil@cesnet.cz>
 */
public class LifeScienceHostelRI extends DefaultRegistrarModule {

	private final static Logger log = LoggerFactory.getLogger(LifescienceHostel.class);

	private final static String HOSTEL_HOSTANAME = "hostel.aai.lifescience-ri.eu";
	private final static String LOGIN_NAMESPACE = "login-namespace:lifescienceid-username";
	private final static String LS_HOSTEL_SCOPE = "@" + HOSTEL_HOSTANAME;
	private final static String LS_HOSTEL_EXT_SOURCE_NAME = "https://" + HOSTEL_HOSTANAME + "/lshostel/";
	private final static String VO_SHORTNAME = "lifescience";
	private final static String AUIDS_ATTRIBUTE = "urn:perun:ues:attribute-def:def:additionalIdentifiers";

	/**
	 * Create proper UserExtSource
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws PrivilegeException, GroupNotExistsException, MemberNotExistsException, ExternallyManagedException, WrongReferenceAttributeValueException, WrongAttributeValueException, RegistrarException, ExtSourceNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, VoNotExistsException, ExtendMembershipException, AlreadyMemberException {

		PerunBl perun = (PerunBl)session.getPerun();

		User user = app.getUser();

		if (user != null) {

			// Create UES for user
			Attribute userLogin = perun.getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":" + LOGIN_NAMESPACE);
			if (userLogin != null && userLogin.getValue() != null) {
				ExtSource extSource = perun.getExtSourcesManagerBl().getExtSourceByName(session, LS_HOSTEL_EXT_SOURCE_NAME);
				String login = userLogin.valueAsString();
				UserExtSource ues = new UserExtSource(extSource, login + LS_HOSTEL_SCOPE);
				ues.setLoa(0);

				try {
					ues = perun.getUsersManagerBl().addUserExtSource(session, user, ues);
				} catch (UserExtSourceExistsException ex) {
					// this is OK
				}

				try {
					Attribute auidsAttr = perun.getAttributesManager().getAttribute(session, ues, AUIDS_ATTRIBUTE);
					List<String> attrValue = new ArrayList<>();
					if (auidsAttr.getValue() != null
						&& auidsAttr.valueAsList() != null
						&& !auidsAttr.valueAsList().isEmpty()
					) {
						attrValue = auidsAttr.valueAsList();
					}
					auidsAttr.valueAsList().add(login + LS_HOSTEL_SCOPE);
					auidsAttr.setValue(attrValue);
					perun.getAttributesManager().setAttribute(session, ues, auidsAttr);
				} catch (UserExtSourceNotExistsException e) {
					// should not happen
				} catch (AttributeNotExistsException e) {
					// ok, attribute is probably not used
				}

			}

			if (Application.AppType.INITIAL.equals(app.getType())) {
				try {
					Vo vo = perun.getVosManagerBl().getVoByShortName(session, VO_SHORTNAME);
					Member member = perun.getMembersManagerBl().createMember(session, vo, user);
					perun.getMembersManagerBl().validateMemberAsync(session, member);
					log.debug("LS Hostel member added to the main VO Lifescience {}", member);
				} catch (VoNotExistsException e) {
					log.warn("VO: " + VO_SHORTNAME + " not exists, can't add member into it.");
				} catch (AlreadyMemberException ignore) {
					// user is already in lifescience
				} catch (ExtendMembershipException e) {
					// can't be member of lifescience, shouldn't happen
					log.error("LS Hostel member can't be added to VO: " + VO_SHORTNAME, e);
				}
			}

			// User doesn't have login - don't set UES

		}

		return app;

	}

}

