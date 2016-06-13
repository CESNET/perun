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
import java.util.Objects;

/**
 * Custom logic for all CESNET DataCenter VOs
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class Du implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(Du.class);

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

		List<ApplicationFormItemData> data = registrar.getApplicationDataById(session, app.getId());

		// if hostel with LoA = 2 => OK
		if (Objects.equals(app.getExtSourceName(), "https://idp.hostel.eduid.cz/idp/shibboleth") &&
				app.getExtSourceLoa() == 2) return app;

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

		switch(category) {
			case "university" : {
				if (affiliation.contains("employee@") ||
						affiliation.contains("faculty@") ||
						affiliation.contains("member@") ||
						affiliation.contains("student@") ||
						affiliation.contains("staff@"))
					return app;
			}
			case "avcr" : {
				if (affiliation.contains("member@")) return app;
			}
			case "library" : {
				if (affiliation.contains("employee@")) return app;
			}
			case "hospital" : {
				if (affiliation.contains("employee@")) return app;
			}
			case "other" : {
				if (affiliation.contains("employee@") || affiliation.contains("member@")) return app;
			}

		}

		throw new CantBeApprovedException("User is not active academia member or submitted his registration using non-academic identity. Please reject this application and let user know that he has to use different identity to log-in to registration interface.");

	}

	@Override
	public void canBeApproved(PerunSession session, Application app) throws CantBeApprovedException {
		try {
			beforeApprove(session, app);
		} catch (PerunException ex) {
			if (ex instanceof CantBeApprovedException) throw (CantBeApprovedException) ex;
			throw new CantBeApprovedException(ex.getMessage(), ex);
		}
	}

}
