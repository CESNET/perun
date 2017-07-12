package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Module for VOs with external users at VŠUP
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Vsup implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(Vsup.class);

	private RegistrarManager registrar;

	@Override
	public void setRegistrar(RegistrarManager registrar) {
		this.registrar = registrar;
	}

	@Override
	public List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException {
		return data;
	}

	/**
	 * Set "membershipExpiration" attribute value to "expirationManual" so it's consumed by services.
	 * Set value only if membershipExpiration is after manual, or manual is empty.
	 * If membershipExpiration is null, set 4000-01-01 as unlimited.
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws PerunException {

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
					log.error("Can't parse manual expiration date: {}",e);
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
					log.error("Can't parse manual expiration date: {}",e);
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
				perun.getAttributesManager().setAttribute(session, user, manualExpirationAttr);
			}

		}

		return app;

	}

	@Override
	public Application rejectApplication(PerunSession session, Application app, String reason) throws PerunException {
		return app;
	}

	@Override
	public Application beforeApprove(PerunSession session, Application app) throws PerunException {
		return app;
	}

	@Override
	public void canBeApproved(PerunSession session, Application app) throws PerunException {
	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {
	}

}
