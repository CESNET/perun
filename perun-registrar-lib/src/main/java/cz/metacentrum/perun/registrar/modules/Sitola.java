package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Module for VO Sitola
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Sitola extends DefaultRegistrarModule {

	final static Logger log = LoggerFactory.getLogger(Sitola.class);

	/**
	 * All new Sitola members will have MU eduroam identity added if they posses MU login.
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws WrongAttributeAssignmentException, UserNotExistsException, AttributeNotExistsException, PrivilegeException, WrongAttributeValueException, WrongReferenceAttributeValueException {

		// get perun from session
		PerunBl perun = (PerunBl) session.getPerun();
		User user = app.getUser();

		if (user != null) {

			Attribute eduroamIdentities = perun.getAttributesManagerBl().getAttribute(session, user, "urn:perun:user:attribute-def:def:eduroamIdentities");
			Attribute loginMu = perun.getAttributesManagerBl().getAttribute(session, user, "urn:perun:user:attribute-def:def:login-namespace:mu");

			if (eduroamIdentities.getValue() == null) {

				if (loginMu.getValue() != null) {

					// add MU identity
					List<String> identities = new ArrayList<>();
					identities.add(loginMu.getValue() +"@eduroam.muni.cz");

					eduroamIdentities.setValue(identities);

					// use Bl since VO manager normally can't set this attribute
					perun.getAttributesManagerBl().setAttribute(session, user, eduroamIdentities);

				}

			} else {

				if (loginMu.getValue() != null) {

					// check if not already present and set
					boolean found = false;
					for (String value : eduroamIdentities.valueAsList()) {
						if (Objects.equals(value, loginMu.getValue() +"@eduroam.muni.cz")) {
							found = true;
							break;
						}
					}

					if (!found) {
						// add MU eduroam identity
						((List<String>) eduroamIdentities.valueAsList()).add(loginMu.getValue() +"@eduroam.muni.cz");
						// use Bl since VO manager normally can't set this attribute
						perun.getAttributesManagerBl().setAttribute(session, user, eduroamIdentities);
					}

				}

			}


		}

		return app;

	}

}
