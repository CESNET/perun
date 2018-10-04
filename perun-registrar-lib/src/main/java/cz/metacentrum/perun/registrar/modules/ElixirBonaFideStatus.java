package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.CantBeSubmittedException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Registration module for ElixirBonaFideStatus.
 * Contains logic if the user can acquire bonaFideStatus defined by ELIXIR.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class ElixirBonaFideStatus implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(ElixirBonaFideStatus.class);

	private static final String USER_BONA_FIDE_STATUS_ATTR_NAME = "bonaFideStatus";
	private static final String USER_BONA_FIDE_STATUS_REMS_ATTR_NAME = "elixirBonaFideStatusREMS";
	private static final String USER_AFFILIATIONS_ATTR_NAME = "eduPersonScopedAffiliations";
	private static final String GROUP_ATESTATION_ATTR_NAME = "attestation";

	private static final String A_U_D_userBonaFideStatus = AttributesManager.NS_USER_ATTR_DEF + ':' + USER_BONA_FIDE_STATUS_ATTR_NAME;
	private static final String A_U_D_userBonaFideStatusRems = AttributesManager.NS_USER_ATTR_DEF + ':' + USER_BONA_FIDE_STATUS_REMS_ATTR_NAME;
	private static final String A_U_D_userEduPersonScopedAffiliations = AttributesManager.NS_USER_ATTR_VIRT + ':' + USER_AFFILIATIONS_ATTR_NAME;
	private static final String A_G_D_groupAttestation = AttributesManager.NS_GROUP_ATTR_DEF + ':' + GROUP_ATESTATION_ATTR_NAME;

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
	 * Add new bonaFideStatus to the user attribute.
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws PerunException {
		User user = app.getUser();
		Group group = app.getGroup();

		AttributesManager am = session.getPerun().getAttributesManager();
		Attribute attestation = am.getAttribute(session, group, A_G_D_groupAttestation);

		String newValue = attestation.valueAsString();

		Attribute bonaFideStatus = am.getAttribute(session, user, A_U_D_userBonaFideStatus);

		List<String> value = new ArrayList<>();
		if (bonaFideStatus.getValue() != null && bonaFideStatus.valueAsList() != null) {
			value = bonaFideStatus.valueAsList();
		}

		value.add(newValue);

		bonaFideStatus.setValue(value);
		am.setAttribute(session, user, bonaFideStatus);

		return app;
	}

	@Override
	public Application rejectApplication(PerunSession session, Application app, String reason) throws PerunException {
		return app;
	}

	@Override
	public Application beforeApprove(PerunSession session, Application app) throws PerunException {
		Group group = app.getGroup();
		if (group == null) {
			throw new CantBeApprovedException("This module can be set only for registration to Group.");
		}

		AttributesManager am = session.getPerun().getAttributesManager();
		Attribute attestation = am.getAttribute(session, group, A_G_D_groupAttestation);

		if (attestation == null) {
			throw new CantBeApprovedException("Application cannot be approved: Group does not have attestation attribute set.");
		}

		String newValue = attestation.valueAsString();
		if (newValue == null || newValue.isEmpty()) {
			throw new CantBeApprovedException("Application cannot be approved: Group does not have attestation value set.");
		}

		return app;
	}

	@Override
	public void canBeApproved(PerunSession session, Application app) throws PerunException {

	}

	/**
	 * Validate if the user meets criteria for applying to group.
	 */
	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {
		User user = session.getPerunPrincipal().getUser();
		if (user == null) {
			throw new CantBeSubmittedException("This module can be set only for registration to Group.");
		}
		AttributesManager am = session.getPerun().getAttributesManager();

		Attribute affiliations = am.getAttribute(session, user, A_U_D_userEduPersonScopedAffiliations);

		if (affiliations.getValue() != null) {
			List<String> val = affiliations.valueAsList();
			for (String affiliation: val) {
				if (affiliation.startsWith("faculty@")) {
					return;
				}
			}
		}

		Attribute rems = am.getAttribute(session, user, A_U_D_userBonaFideStatusRems);

		if (rems.getValue() != null) {
			return;
		}

		throw new CantBeSubmittedException("User does not meet the criteria for applying for Bona Fide Status");
	}
}
