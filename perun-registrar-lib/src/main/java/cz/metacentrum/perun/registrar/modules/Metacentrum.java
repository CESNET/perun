package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
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
 * Module for VO Metacentrum
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Metacentrum implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(Metacentrum.class);

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
	 * Add all new Metacentrum members to "storage" group.
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws PerunException {

		// get perun from session
		Perun perun = session.getPerun();

		if (Application.AppType.INITIAL.equals(app.getType())) {

			Vo vo = app.getVo();
			User user = app.getUser();
			Group group = perun.getGroupsManager().getGroupByName(session, vo, "storage");
			Member mem = perun.getMembersManager().getMemberByUser(session, vo, user);

			try  {
				perun.getGroupsManager().addMember(session, group, mem);
			} catch (AlreadyMemberException ex) {

			}
		}

		// Support statistic groups
		String statisticGroupName = "";

		List<ApplicationFormItemData> formData = registrar.getApplicationDataById(session, app.getId());
		for (ApplicationFormItemData item : formData) {
			if (Objects.equals("urn:perun:user:attribute-def:def:researchGroupStatistic", item.getFormItem().getPerunDestinationAttribute())) {
				statisticGroupName = item.getValue();
				break;
			}
		}

		if (statisticGroupName != null && !statisticGroupName.isEmpty()) {
			Group group;
			try {
				group = perun.getGroupsManager().getGroupByName(session, app.getVo(), statisticGroupName);
			} catch (GroupNotExistsException ex) {
				// user filled non existing group, just skip adding
				return app;
			} catch (InternalErrorException ex) {
				// wrong group name
				return app;
			}

			Attribute isStatisticGroup = perun.getAttributesManager().getAttribute(session, group, "urn:perun:group:attribute-def:def:statisticGroup");
			Attribute isStatisticGroupAutoFill = perun.getAttributesManager().getAttribute(session, group, "urn:perun:group:attribute-def:def:statisticGroupAutoFill");

			boolean statisticGroup = (isStatisticGroup.getValue() != null) ? (Boolean)isStatisticGroup.getValue() : false;
			boolean statisticGroupAutoFill = (isStatisticGroupAutoFill.getValue() != null) ? (Boolean)isStatisticGroupAutoFill.getValue() : false;

			if (statisticGroup && statisticGroupAutoFill) {
				try  {
					Member mem = perun.getMembersManager().getMemberByUser(session, app.getVo(), app.getUser());
					perun.getGroupsManager().addMember(session, group, mem);
				} catch (AlreadyMemberException ex) {

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

		// allow only Education & Research community members

		// allow hostel with loa=2
		if (Objects.equals(app.getExtSourceName(), "https://idp.hostel.eduid.cz/idp/shibboleth") &&
				app.getExtSourceLoa() == 2) return;

		List<ApplicationFormItemData> data = registrar.getApplicationDataById(session, app.getId());

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
