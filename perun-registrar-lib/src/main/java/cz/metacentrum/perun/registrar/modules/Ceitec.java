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
 * Module for CEITEC VO at MU instance of Perun.
 *
 * The module check if name provided by User and IdP is different. If so, automatic approval is cancelled
 * and VO manager must approve it manually.
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

		List<ApplicationFormItemData> data = registrar.getApplicationDataById(session, app.getId());

		String name = "";
		String fed_name = "";

		for (ApplicationFormItemData item : data) {
			if (Objects.equals(item.getShortname(),"jmeno")) {
				name = item.getValue();
			}
			if (Objects.equals(item.getShortname(),"jmeno_fed")) {
				fed_name = item.getValue();
			}
		}

		if (!Objects.equals(name,fed_name)) {
			throw new CantBeApprovedException("Users name provided by IdP and User differ. Please check for correct name before approval.","","","",true);
		}

	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {
	}

}
