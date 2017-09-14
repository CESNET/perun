package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Module for CEITEC VO at MU instance of Perun.
 *
 * The module
 * 1. Check if user checked "I'm student" on registration form.
 * 2. If not, set expiration to 1.1.9999
 * 3. If yes, set expiration to 31.10.current/nextYear
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class Ceitec implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(Ceitec.class);

	private RegistrarManager registrar;

	@Override
	public void setRegistrar(RegistrarManager registrar) {
		this.registrar = registrar;
	}

	@Override
	public List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException {
		return data;
	}

	@Override
	public Application approveApplication(PerunSession session, Application app) throws PerunException {

		if ((app.getGroup() != null && Objects.equals(app.getType(), Application.AppType.INITIAL))
				|| app.getGroup() == null && Objects.equals(app.getType(), Application.AppType.EXTENSION)) {

			// IF GROUP INITIAL OR VO EXTENSION - handle student/non-student changes

			List<ApplicationFormItemData> data = registrar.getApplicationDataById(session, app.getId());
			boolean student = false;

			for (ApplicationFormItemData item : data) {
				if (item.getFormItem() != null && Objects.equals("urn:perun:member:attribute-def:def:student", item.getFormItem().getPerunDestinationAttribute())) {
					student = Objects.equals(item.getValue(), "student");
					break;
				}
			}

			PerunBl perun = (PerunBl)session.getPerun();
			Member member = perun.getMembersManagerBl().getMemberByUser(session, app.getVo(), app.getUser());

			// all should have value set
			Attribute attr = perun.getAttributesManagerBl().getAttribute(session, member, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

			if (!student) {

				attr.setValue("9999-01-01"); // set distant future as never expires
				perun.getAttributesManagerBl().setAttribute(session, member, attr);

				// remove student flag (not stored by application)
				Attribute attr2 = perun.getAttributesManagerBl().getAttribute(session, member, AttributesManager.NS_MEMBER_ATTR_DEF + ":student");
				perun.getAttributesManagerBl().removeAttribute(session, member, attr2);

			} else {

				// student flag is stored by application

				// calculate now
				Calendar now = Calendar.getInstance();
				int year = now.get(Calendar.YEAR);

				// expiration this year 31.8. (since it's original expiration minus 2 months grace period)
				Calendar expiration = Calendar.getInstance();
				expiration.set(year, Calendar.AUGUST, 31);

				if (expiration.after(now)) {
					// set current year expiration on 31.10.
					attr.setValue(year+"-10-31");
				} else {
					// set next year expiration on 31.10.
					attr.setValue((year+1)+"-10-31");
				}

			}

			// store change in expiration
			perun.getAttributesManagerBl().setAttribute(session, member, attr);

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
