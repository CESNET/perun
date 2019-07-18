package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
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
 * Module for VO Metacentrum
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Metacentrum implements RegistrarModule {

	private final static Logger log = LoggerFactory.getLogger(Metacentrum.class);

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
	public Application approveApplication(PerunSession session, Application app) throws PrivilegeException, InternalErrorException, VoNotExistsException, GroupNotExistsException, UserNotExistsException, MemberNotExistsException, ExternallyManagedException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeValueException, RegistrarException {

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
			} catch (GroupNotExistsException | InternalErrorException ex) {
				// user filled non existing group, just skip adding OR wrong group name
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
	public Application beforeApprove(PerunSession session, Application app) {
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

				LocalDateTime timeInOneYear = LocalDateTime.ofInstant(eligibleDate.toInstant(), ZoneId.systemDefault()).plusYears(1);

				// compare
				if (LocalDateTime.now().isBefore(timeInOneYear)) {
					return;
				}

			} catch (ParseException e) {
				log.warn("Unable to parse date to determine, if user is eligible for CESNET services.", e);
			}
		}

		throw new CantBeApprovedException("User is not eligible for CESNET services.", "NOT_ELIGIBLE", null, null, true);

	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {

	}

}
