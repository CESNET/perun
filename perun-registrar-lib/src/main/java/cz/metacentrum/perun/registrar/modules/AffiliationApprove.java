package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.FormNotExistsException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationMail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class AffiliationApprove extends DefaultRegistrarModule {
	private final static Logger log = LoggerFactory.getLogger(AffiliationApprove.class);
	private static final String groupRegexAttr = AttributesManager.NS_GROUP_ATTR_DEF + ":applicationAffiliationRegex";
	private static final String voRegexAttr = AttributesManager.NS_VO_ATTR_DEF + ":applicationAffiliationRegex";
	@Override
	public boolean autoApproveShouldBeForce(PerunSession sess, Application app) throws PerunException {
		List<Pattern> regexps = getRegexps(sess, app);
		List<String> affiliations = List.of(BeansUtils.stringToMapOfAttributes(app.getFedInfo()).get("affiliation").split(";"));
		for (Pattern regexp : regexps) {
			for (String affiliation : affiliations) {
				Matcher matcher = regexp.matcher(affiliation);
				if (matcher.matches()) {
					return true;
				}
			}
		}
		handleAutoApproveError(app);
		return false;
	}

	/**
	 * get regular expressions from the attributes
	 *
	 * @param session
	 * @param app
	 * @return list of patterns
	 */
	private List<Pattern> getRegexps(PerunSession session, Application app) {
		PerunBl perun = (PerunBl)session.getPerun();

		List<String> stringRegexps = new ArrayList<>();
		Attribute voAttr;
		try {
			voAttr = perun.getAttributesManagerBl().getAttribute(session, app.getVo(), voRegexAttr);
			stringRegexps = voAttr.valueAsList();
		} catch (Exception ex) {
			log.error("Couldn't retrieve domains attribute when trying to auto approve application: " + app +
				"with error: " + ex.getMessage());
		}
		if (app.getGroup() != null) {
			Attribute groupAttr;
			try {
				groupAttr = perun.getAttributesManagerBl().getAttribute(session, app.getGroup(), groupRegexAttr);
				stringRegexps.addAll(groupAttr.valueAsList());
			} catch (Exception ex) {
				log.error("Couldn't retrieve domains attribute when trying to auto approve application: " + app +
					"with error: " + ex.getMessage());
			}
		}
		return stringRegexps.stream().map(Pattern::compile).toList();
	}

	/**
	 * If auto approve is not enabled, set auto approve error describing failed regexp matching. Also send email notification
	 *
	 * @param app
	 */
	private void handleAutoApproveError(Application app) throws FormNotExistsException {
		ApplicationForm form;

		if (app.getGroup() != null) {
			// group application
			form = registrar.getFormForGroup(app.getGroup());
		} else {
			// vo application
			form = registrar.getFormForVo(app.getVo());
		}
		Application.AppType type = app.getType();
		if ((Application.AppType.INITIAL.equals(type) && !form.isAutomaticApproval()) || (Application.AppType.EXTENSION.equals(type) && !form.isAutomaticApprovalExtension()) || (Application.AppType.EMBEDDED.equals(type) && !form.isAutomaticApprovalEmbedded())) {
			CantBeApprovedException exception = new CantBeApprovedException("Application affiliations did not match any regular expressions defined in the Vo/Group attributes",
				null, null, BeansUtils.stringToMapOfAttributes(app.getFedInfo()).get("affiliation"), app.getId());
			registrar.setAutoApproveErrorToApplication(app, exception.getMessage());
			registrar.getMailManager().sendMessage(app, ApplicationMail.MailType.APP_ERROR_VO_ADMIN, null, List.of(exception));
		}
	}
}
