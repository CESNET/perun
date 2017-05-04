package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Module for VO Sitola
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Sitola implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(Sitola.class);

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
	 * All new Sitola members will have MU eduroam identity added if they posses MU login.
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws PerunException {

		// get perun from session
		PerunBl perun = (PerunBl) session.getPerun();
		User user = app.getUser();

		if (user != null) {

			Attribute eduroamIdentities = perun.getAttributesManager().getAttribute(session, user, "urn:perun:user:attribute-def:def:eduroamIdentities");
			Attribute loginMu = perun.getAttributesManager().getAttribute(session, user, "urn:perun:user:attribute-def:def:login-namespace:mu");

			if (eduroamIdentities.getValue() == null) {

				if (loginMu.getValue() != null) {

					// add MU identity
					List<String> identities = new ArrayList<>();
					identities.add(((String)loginMu.getValue())+"@eduroam.muni.cz");

					eduroamIdentities.setValue(identities);

					// use Bl since VO manager normally can't set this attribute
					perun.getAttributesManagerBl().setAttribute(session, user, eduroamIdentities);

				}

			} else {

				if (loginMu.getValue() != null) {

					// check if not already present and set
					boolean found = false;
					for (String value : (List<String>)eduroamIdentities.getValue()) {
						if (Objects.equals(value, ((String)loginMu.getValue())+"@eduroam.muni.cz")) {
							found = true;
							break;
						}
					}

					if (!found) {
						// add MU eduroam identity
						((List<String>) eduroamIdentities.getValue()).add(((String)loginMu.getValue())+"@eduroam.muni.cz");
						// use Bl since VO manager normally can't set this attribute
						perun.getAttributesManagerBl().setAttribute(session, user, eduroamIdentities);
					}

				}

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
