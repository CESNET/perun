package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Custom logic for all CESNET DataCenter VOs with SOFT approval (VO admin decide if can be approved)
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class DuSoft implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(DuSoft.class);

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
		return app;
	}

	@Override
	public Application rejectApplication(PerunSession session, Application app, String reason) throws PerunException {
		return app;
	}

	@Override
	public Application beforeApprove(PerunSession session, Application app) throws PerunException {
		// allow approval of any application based on VO rules
		return app;
	}

	@Override
	public void canBeApproved(PerunSession session, Application app) throws PerunException {

		// allow only Education & Research community members
		List<ApplicationFormItemData> data = registrar.getApplicationDataById(session, app.getId());
		String eligibleString = "";

		for (ApplicationFormItemData item : data) {
			if (item.getFormItem() != null && Objects.equals("isCesnetEligibleLastSeen", item.getFormItem().getFederationAttribute())) {
				if (item.getValue() != null && !item.getValue().trim().isEmpty()) {
					eligibleString = item.getValue();
					break;
				}
			}
		}

		if (eligibleString != null && !eligibleString.isEmpty()) {

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			df.setLenient(false);
			try {
				// get eligible date + 1 year
				Date eligibleDate = df.parse(eligibleString);

				Calendar c = Calendar.getInstance();
				c.setTime(eligibleDate);
				c.add(Calendar.YEAR, 1);
				Date eligibleDatePlusYear = c.getTime();

				// get now
				Calendar cal = Calendar.getInstance();
				Date now = cal.getTime();

				// compare
				if (now.before(eligibleDatePlusYear)) {
					return;
				}

			} catch (ParseException e) {
				log.warn("Unable to parse date to determine, if user is eligible for CESNET services. {}", e);
			}
		}

		throw new CantBeApprovedException("User is not eligible for CESNET services.", "NOT_ELIGIBLE", null, null, true);

	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {

	}

}
