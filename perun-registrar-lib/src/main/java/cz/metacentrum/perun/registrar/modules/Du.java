package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.CantBeSubmittedException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Custom logic for all CESNET DataCenter VOs
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class Du extends DefaultRegistrarModule {

	private final static Logger log = LoggerFactory.getLogger(Du.class);

	@Override
	public Application beforeApprove(PerunSession session, Application app) throws CantBeApprovedException, RegistrarException, PrivilegeException {

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

				LocalDateTime timeInOneYear = LocalDateTime.ofInstant(eligibleDate.toInstant(), ZoneId.systemDefault()).plusYears(1);

				// compare
				if (LocalDateTime.now().isBefore(timeInOneYear)) {
					return app;
				}

			} catch (ParseException e) {
				log.warn("Unable to parse date to determine, if user is eligible for CESNET services.", e);
			}
		}

		throw new CantBeApprovedException("User is not eligible for CESNET services.", "NOT_ELIGIBLE", null, null);

	}

	@Override
	public void canBeApproved(PerunSession session, Application app) throws PerunException {

		beforeApprove(session, app);

	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {

		String eligibleString = params.get("isCesnetEligibleLastSeen");

		if (eligibleString != null && !eligibleString.isEmpty()) {

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			df.setLenient(false);
			try {
				// get eligible date + 1 year
				Date eligibleDate = df.parse(eligibleString);

				LocalDateTime timeInOneYear = LocalDateTime.ofInstant(eligibleDate.toInstant(), ZoneId.systemDefault()).plusYears(1);

				// compare
				if (LocalDateTime.now().isBefore(timeInOneYear)) {
					return;
				}

			} catch (ParseException e) {
				log.warn("Unable to parse date to determine, if user is eligible for CESNET services.", e);
			}
		}

		throw new CantBeSubmittedException("User is not eligible for CESNET services. You must log-in using verified academic identity (at least once a year) in order to access CESNET services.", "NOT_ELIGIBLE", null, null);

	}

}
