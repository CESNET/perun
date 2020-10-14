package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Module for VOs with external users at VŠUP
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Vsup extends DefaultRegistrarModule {

	private final static Logger log = LoggerFactory.getLogger(Vsup.class);

	@Override
	public void canBeApproved(PerunSession session, Application app) throws PerunException {

		List<ApplicationFormItemData> data = registrar.getApplicationDataById(session, app.getId());

		if (app.getUser() == null) {

			for (ApplicationFormItemData item : data) {
				if (item.getFormItem() != null &&
					Objects.equals(AttributesManager.NS_USER_ATTR_DEF + ":birthNumber", item.getFormItem().getPerunDestinationAttribute())) {

					// if application contains birth number, try to map to existing user
					String rc = item.getValue();
					if (rc != null && !rc.isEmpty()) {

						try {
							User user = ((PerunBl) session.getPerun()).getUsersManagerBl().getUserByExtSourceNameAndExtLogin(session, "RC", rc);
							throw new CantBeApprovedException("Application has the same birth number " + rc + " as user " + user + " already in Perun and thus would be merged with him.", null, null, null, true);
						} catch (CantBeApprovedException ex) {
							throw ex;
						} catch (Exception ex) {
							log.warn("Couldn't find or set user to application {} by RC: {}", app, ex);
						}
					}
					break;
				}
			}
		}
	}

	@Override
	public Application beforeApprove(PerunSession session, Application app) throws RegistrarException, PrivilegeException {

		List<ApplicationFormItemData> data = registrar.getApplicationDataById(session, app.getId());

		if (app.getUser() == null) {

			for (ApplicationFormItemData item : data) {
				if (item.getFormItem() != null &&
					Objects.equals(AttributesManager.NS_USER_ATTR_DEF + ":birthNumber", item.getFormItem().getPerunDestinationAttribute())) {

					// if application contains birth number, try to map to existing user
					String rc = item.getValue();
					if (rc != null && !rc.isEmpty()) {

						try {
							User user = ((PerunBl) session.getPerun()).getUsersManagerBl().getUserByExtSourceNameAndExtLogin(session, "RC", rc);
							app.setUser(user);
							registrar.updateApplicationUser(session, app);
							log.debug("Existing user found by RC for {}", app);
						} catch (Exception ex) {
							log.warn("Couldn't find or set user to application {} by RC: {}", app, ex);
						}

						// associate existing user with the identity used on registration form
						if (app.getUser() != null) {
							PerunBl perunBl = (PerunBl)session.getPerun();
							ExtSource es = perunBl.getExtSourcesManager().checkOrCreateExtSource(session, app.getExtSourceName(), app.getExtSourceType());
							UserExtSource ues = new UserExtSource(es, app.getExtSourceLoa(), app.getCreatedBy());
							try {
								ues = perunBl.getUsersManagerBl().addUserExtSource(session, app.getUser(), ues);
								log.debug("{} associated with {} from application {}", app.getUser(), ues, app);
							} catch (UserExtSourceExistsException ex) {
								// we can ignore, user will be paired with application
								log.warn("{} already had identity associated from application {}", app.getUser(), app);
							}
						}

					}
					break;
				}
			}
		}
		return app;
	}

	/**
	 * Set "membershipExpiration" attribute value to "expirationManual" so it's consumed by services.
	 * Set value only if membershipExpiration is after manual, or manual is empty.
	 * If membershipExpiration is null, set 4000-01-01 as unlimited.
	 *
	 * Create userExtSource RC to user for future merging.
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, UserNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, PrivilegeException {

		PerunBl perun = (PerunBl)session.getPerun();

		Vo vo = app.getVo();
		User user = app.getUser();

		if (user == null) {
			log.error("At the end of approval action, we should have user present in application: {}",app);
		} else {

			Member member = perun.getMembersManagerBl().getMemberByUser(session, vo, user);
			Date membershipExpiration = null;
			Date manualExpiration = null;

			Attribute membershipExpirationAttr = perun.getAttributesManagerBl().getAttribute(session, member, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");
			if (membershipExpirationAttr.getValue() != null) {

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				df.setLenient(false);

				String expiration = (String)membershipExpirationAttr.getValue();
				try {
					membershipExpiration = df.parse(expiration);
				} catch (ParseException e) {
					log.error("Can't parse manual expiration date.",e);
				}

			}

			Attribute manualExpirationAttr = perun.getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":expirationManual");
			if (manualExpirationAttr.getValue() != null) {

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				df.setLenient(false);

				String expiration = (String)manualExpirationAttr.getValue();
				try {
					manualExpiration = df.parse(expiration);
				} catch (ParseException e) {
					log.error("Can't parse manual expiration date.",e);
				}

			}

			boolean changed = false;

			if (membershipExpiration == null) {
				// has no membership expiration - set as unlimited - but it shouldn't happened based on VO rules
				manualExpirationAttr.setValue("4000-01-01");
				changed = true;
			} else if (manualExpiration == null || membershipExpiration.after(manualExpiration)) {
				// has no manual expiration - set from membership expiration
				// OR
				// has membership expiration after manual
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				String value = df.format(membershipExpiration);
				manualExpirationAttr.setValue(value);
				changed = true;
			}

			if (changed) {
				// update manual expiration attribute
				perun.getAttributesManagerBl().setAttribute(session, user, manualExpirationAttr);
			}

		}

		// create ues RC for future merging
		List<ApplicationFormItemData> data = new ArrayList<>();
		try {
			data = registrar.getApplicationDataById(session, app.getId());
		} catch (RegistrarException e) {
			// ignore because application's id is not null
		}
		for (ApplicationFormItemData item : data) {
			if (item.getFormItem() != null &&
				Objects.equals(AttributesManager.NS_USER_ATTR_DEF + ":birthNumber", item.getFormItem().getPerunDestinationAttribute())) {

				String rc = item.getValue();
				if (rc != null && !rc.isEmpty()) {
					ExtSource es = perun.getExtSourcesManager().checkOrCreateExtSource(session, "RC", rc);
					UserExtSource ues = new UserExtSource(es, app.getExtSourceLoa(), app.getCreatedBy());
					try {
						perun.getUsersManagerBl().addUserExtSource(session, app.getUser(), ues);
					} catch (UserExtSourceExistsException e) {
						log.info("User external source from RC already created.");
					}
				}
				break;
			}
		}

		return app;

	}

}
