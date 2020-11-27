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
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Module for VO Metacentrum
 *
 * Set different membership expiration to users, which are not eligible for CESNET services.
 * Prevent them from extending the membership. Warn VO manager about it before application approval.
 *
 * On approval of initial application add all members to "storage" group.
 * On approval of any application sort them in statistic groups.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class Metacentrum extends DefaultRegistrarModule {

	private final static Logger log = LoggerFactory.getLogger(Metacentrum.class);

	private final static String A_USER_RESEARCH_GROUP_STATISTICS = AttributesManager.NS_USER_ATTR_DEF+":researchGroupStatistic";
	private final static String A_GROUP_STATISTIC_GROUP = AttributesManager.NS_GROUP_ATTR_DEF+":statisticGroup";
	private final static String A_GROUP_STATISTIC_GROUP_AUTOFILL = AttributesManager.NS_GROUP_ATTR_DEF+":statisticGroupAutoFill";
	private final static String A_USER_IS_CESNET_ELIGIBLE_LAST_SEEN = AttributesManager.NS_USER_ATTR_DEF+":isCesnetEligibleLastSeen";
	private final static String A_MEMBER_MEMBERSHIP_EXPIRATION = AttributesManager.NS_MEMBER_ATTR_DEF+":membershipExpiration";

	/**
	 * Add all new Metacentrum members to "storage" group.
	 * Sort them in the statistics groups.
	 * Set shorter expiration to users, which are not eligible for CESNET services.
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws PrivilegeException, GroupNotExistsException, MemberNotExistsException, ExternallyManagedException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeValueException, RegistrarException {

		PerunBl perun = (PerunBl)session.getPerun();
		Vo vo = app.getVo();
		User user = app.getUser();
		Member mem = perun.getMembersManagerBl().getMemberByUser(session, vo, user);

		// Add new members to "storage" group
		if (Application.AppType.INITIAL.equals(app.getType())) {

			Group group = perun.getGroupsManagerBl().getGroupByName(session, vo, "storage");

			try  {
				perun.getGroupsManager().addMember(session, group, mem);
			} catch (AlreadyMemberException ex) {
				// IGNORE
			}
		}

		// SET EXPIRATION BASED ON "isCesnetEligibleLastSeen"
		boolean eligibleUser = isCesnetEligibleLastSeen(getIsCesnetEligibleLastSeenFromUser(session, app.getUser()));
		boolean eligibleApplication = isCesnetEligibleLastSeen(getIsCesnetEligibleLastSeenFromApplication(session, app));

		if (!eligibleUser && !eligibleApplication) {

			Attribute expirationAttribute = perun.getAttributesManagerBl().getAttribute(session, mem, A_MEMBER_MEMBERSHIP_EXPIRATION);

			// only if member already has some expiration set !!
			if (expirationAttribute.getValue() != null) {
				LocalDate date = null;
				if (Application.AppType.INITIAL.equals(app.getType())) {
					// set 3 months from now (since generic logic already set wrong expiration to the member)
					date = LocalDate.now().plusMonths(3);
				} else {
					// set 3 months from current expiration
					date = LocalDate.parse(expirationAttribute.valueAsString(), DateTimeFormatter.ISO_LOCAL_DATE).plusMonths(3);
				}
				expirationAttribute.setValue(date.toString());
				perun.getAttributesManagerBl().setAttribute(session, mem, expirationAttribute);
			}

		}

		// Support statistic groups
		String statisticGroupName = "";

		List<ApplicationFormItemData> formData = registrar.getApplicationDataById(session, app.getId());
		for (ApplicationFormItemData item : formData) {
			if (Objects.equals(A_USER_RESEARCH_GROUP_STATISTICS, item.getFormItem().getPerunDestinationAttribute())) {
				statisticGroupName = item.getValue();
				break;
			}
		}

		if (statisticGroupName != null && !statisticGroupName.isEmpty()) {

			Group group;
			try {
				group = perun.getGroupsManagerBl().getGroupByName(session, app.getVo(), statisticGroupName);
			} catch (GroupNotExistsException | InternalErrorException ex) {
				// user filled non existing group, just skip adding OR wrong group name
				return app;
			}

			Attribute isStatisticGroup = perun.getAttributesManagerBl().getAttribute(session, group, A_GROUP_STATISTIC_GROUP);
			Attribute isStatisticGroupAutoFill = perun.getAttributesManagerBl().getAttribute(session, group, A_GROUP_STATISTIC_GROUP_AUTOFILL);

			boolean statisticGroup = (isStatisticGroup.getValue() != null) ? (Boolean)isStatisticGroup.getValue() : false;
			boolean statisticGroupAutoFill = (isStatisticGroupAutoFill.getValue() != null) ? (Boolean)isStatisticGroupAutoFill.getValue() : false;

			if (statisticGroup && statisticGroupAutoFill) {
				try  {
					perun.getGroupsManager().addMember(session, group, mem);
				} catch (AlreadyMemberException ignored) {

				}
			}
		}

		return app;

	}

	@Override
	public void canBeApproved(PerunSession session, Application app) throws PerunException {

		boolean eligibleUser = isCesnetEligibleLastSeen(getIsCesnetEligibleLastSeenFromUser(session, app.getUser()));
		boolean eligibleApplication = isCesnetEligibleLastSeen(getIsCesnetEligibleLastSeenFromApplication(session, app));

		if (!eligibleUser && !eligibleApplication) {
			throw new CantBeApprovedException("User is not eligible for CESNET services.", "NOT_ELIGIBLE", null, null, true);
		}

	}

	@Override
	public void canBeSubmitted(PerunSession session, Application.AppType appType, Map<String, String> params) throws PerunException {

		if (Application.AppType.EXTENSION.equals(appType)) {

			User user = session.getPerunPrincipal().getUser();
			boolean eligibleUser = isCesnetEligibleLastSeen(getIsCesnetEligibleLastSeenFromUser(session, user));
			boolean eligibleFromFederation = isCesnetEligibleLastSeen(params.get("isCesnetEligibleLastSeen"));

			if (!eligibleUser && !eligibleFromFederation) {
				// TODO - We must have much better info in GUI!
				throw new CantBeSubmittedException("Your membership in VO Metacentrum can't be extended right now. Your account is not verified." +
						" Please visit your profile and add verified academic identity to your account (from identity federation eduID.cz) " +
						" or verify your academia status by letter signed by the head of your institution.", "NOT_ELIGIBLE", null, null);
			}

		}

	}

	/**
	 * Check whether passed eligible timestamp is valid and not older than 1 year. If so, user is eligible for CESNET services.
	 *
	 * @param eligibleString Timestamp to check
	 * @return TRUE if eligible, FALSE if not
	 */
	private boolean isCesnetEligibleLastSeen(String eligibleString) {

		if (eligibleString != null && !eligibleString.isEmpty()) {

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			df.setLenient(false);
			try {
				// get eligible date + 1 year
				Date eligibleDate = df.parse(eligibleString);

				LocalDateTime timeInOneYear = LocalDateTime.ofInstant(eligibleDate.toInstant(), ZoneId.systemDefault()).plusYears(1);

				// compare
				if (LocalDateTime.now().isBefore(timeInOneYear)) {
					return true;
				}

			} catch (ParseException e) {
				log.warn("Unable to parse date to determine, if user is eligible for CESNET services.", e);
			}
		}

		return false;

	}

	/**
	 * Get value of "isCesnetEligibleLastSeen" form item (first item with this source federation attribute).
	 * Return empty string if not found or it somehow fails.
	 *
	 * @param session PerunSession
	 * @param app Application to get form items from
	 * @return Timestamp (yyyy-MM-dd HH:mm:ss) value or empty string.
	 */
	private String getIsCesnetEligibleLastSeenFromApplication(PerunSession session, Application app) {

		String eligibleString = "";

		try {
			List<ApplicationFormItemData> data = registrar.getApplicationDataById(session, app.getId());

			for (ApplicationFormItemData item : data) {
				if (item.getFormItem() != null && Objects.equals("isCesnetEligibleLastSeen", item.getFormItem().getFederationAttribute())) {
					if (item.getValue() != null && !item.getValue().trim().isEmpty()) {
						eligibleString = item.getValue();
						break;
					}
				}
			}
		} catch (PrivilegeException | RegistrarException e) {
			log.error("Unable to get 'isCesnetEligibleLastSeen' from application.", e);
		}

		return eligibleString;

	}

	/**
	 * Get value of "user:def:isCesnetEligibleLastSeen" attribute.
	 * If application has no associated user or we anyhow fail to get the value, empty string is returned.
	 *
	 * @param session PerunSession
	 * @param user User to check it for
	 * @return Timestamp (yyyy-MM-dd HH:mm:ss) value or empty string.
	 */
	private String getIsCesnetEligibleLastSeenFromUser(PerunSession session, User user) {

		String eligibleString = "";
		if (user != null) {
			try {
				PerunBl perun = (PerunBl) session.getPerun();
				Attribute attribute = perun.getAttributesManagerBl().getAttribute(session, user, A_USER_IS_CESNET_ELIGIBLE_LAST_SEEN);
				if (attribute.getValue() != null)  {
					eligibleString = attribute.valueAsString();
				}
			} catch (Exception ex) {
				log.error("Unable to get 'isCesnetEligibleLastSeen' from user.", ex);
			}
		}

		return eligibleString;

	}

}
