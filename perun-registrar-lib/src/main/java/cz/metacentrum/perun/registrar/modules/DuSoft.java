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

		// warn before approval on non-academic

		List<ApplicationFormItemData> data = registrar.getApplicationDataById(session, app.getId());

		// if hostel with LoA = 2 => OK
		if (Objects.equals(app.getExtSourceName(), "https://idp.hostel.eduid.cz/idp/shibboleth") &&
				app.getExtSourceLoa() == 2) return;

		// For others check IdP attributes
		String category = "";
		String affiliation = "";

		for (ApplicationFormItemData item : data) {
			if (item.getFormItem() != null && Objects.equals("md_entityCategory", item.getFormItem().getFederationAttribute())) {
				if (item.getValue() != null && !item.getValue().trim().isEmpty()) {
					category = item.getValue();
					break;
				}
			}
		}

		for (ApplicationFormItemData item : data) {
			if (item.getFormItem() != null && Objects.equals("affiliation", item.getFormItem().getFederationAttribute())) {
				if (item.getValue() != null && !item.getValue().trim().isEmpty()) {
					affiliation = item.getValue();
					break;
				}
			}
		}

		if (category.contains("http://eduid.cz/uri/idp-group/university")) {
			if (affiliation.contains("employee@") ||
					affiliation.contains("faculty@") ||
					affiliation.contains("member@") ||
					affiliation.contains("student@") ||
					affiliation.contains("staff@"))
				return;
		} else if (category.contains("http://eduid.cz/uri/idp-group/avcr")) {
			if (affiliation.contains("member@")) return;
		} else if (category.contains("http://eduid.cz/uri/idp-group/library")) {
			if (affiliation.contains("employee@")) return;
		} else if (category.contains("http://eduid.cz/uri/idp-group/hospital")) {
			if (affiliation.contains("employee@")) return;
		} else if (category.contains("http://eduid.cz/uri/idp-group/other")) {
			if (affiliation.contains("employee@") || affiliation.contains("member@")) return;
		}

		throw new CantBeApprovedException("User is not active academia member", "NOT_ACADEMIC", category, affiliation, true);

	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {

	}

}
