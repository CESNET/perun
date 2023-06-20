package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationAlreadyExists;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationNotAllowed;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.registrar.exceptions.FormNotExistsException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import cz.metacentrum.perun.registrar.model.ApplicationMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Module for VO "vo.esc.pithia.eu"
 *
 * This module is used by the group "organizationRequests" within the VO in order to create new groups
 * aka "organizations" in their logical context. It copies group configuration from the template group
 * "organizationTemplate" and appoints the user as a new group manager.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class EscPithiaOrganizations extends DefaultRegistrarModule {

	final static Logger log = LoggerFactory.getLogger(EscPithiaOrganizations.class);

	@Override
	public Application approveApplication(PerunSession session, Application app) throws UserNotExistsException, PrivilegeException, AlreadyAdminException, GroupNotExistsException, VoNotExistsException, MemberNotExistsException, AlreadyMemberException, ExternallyManagedException, WrongAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongReferenceAttributeValueException, RegistrarException, ExtendMembershipException, ExtSourceNotExistsException, NotGroupMemberException {

		// works only for initial group applications
		if (Application.AppType.INITIAL.equals(app.getType()) && app.getGroup() != null) {

			PerunBl perun = (PerunBl)session.getPerun();
			Group group = app.getGroup();
			Vo vo = app.getVo();
			User user = app.getUser();
			Member member = perun.getMembersManagerBl().getMemberByUser(session, vo, user);

			// create new "organization" as subgroup
			Group organizationsParentGroup = perun.getGroupsManagerBl().getGroupByName(session, vo, "organizations");
			String organizationName = getOrganizationName(session, app);
			String organizationDescription = getOrganizationDescription(session, app);
			Group newOrganization = new Group(organizationsParentGroup.getId(), organizationName,organizationDescription);
			try {
				newOrganization = perun.getGroupsManagerBl().createGroup(session, organizationsParentGroup, newOrganization);
			} catch (GroupExistsException | GroupRelationNotAllowed | GroupRelationAlreadyExists e) {
				throw new InternalErrorException("Unable to create required group - organization!", e);
			}

			Group organizationAdminsGroup = new Group(newOrganization.getId(), "admins", "Admins of "+organizationName);
			Group organizationMembersGroup = new Group(newOrganization.getId(), "members", "Members of "+organizationName);

			try {
				organizationAdminsGroup = perun.getGroupsManagerBl().createGroup(session, newOrganization, organizationAdminsGroup);
			} catch (GroupExistsException | GroupRelationNotAllowed | GroupRelationAlreadyExists e) {
				throw new InternalErrorException("Unable to create required group - organization:admins!", e);
			}

			try {
				organizationMembersGroup = perun.getGroupsManagerBl().createGroup(session, newOrganization, organizationMembersGroup);
			} catch (GroupExistsException | GroupRelationNotAllowed | GroupRelationAlreadyExists e) {
				throw new InternalErrorException("Unable to create required group - organization:members!", e);
			}

			// make "admins" group an admin of new organization (both subgroups)

			try {
				AuthzResolverBlImpl.setRole(session, organizationAdminsGroup, organizationAdminsGroup, Role.GROUPADMIN);
			} catch (RoleCannotBeManagedException e) {
				throw new InternalErrorException("Unable to set group manager for group - organization:admins!", e);
			}
			try {
				AuthzResolverBlImpl.setRole(session, organizationAdminsGroup, organizationMembersGroup, Role.GROUPADMIN);
			} catch (RoleCannotBeManagedException e) {
				throw new InternalErrorException("Unable to set group manager for group - organization:members!", e);
			}

			// make requestor a member and admin of new organization
			perun.getGroupsManagerBl().addMember(session, organizationMembersGroup, member);
			perun.getGroupsManagerBl().addMember(session, organizationAdminsGroup, member);

			// copy registration form and notifications from template
			copyFormAndNotifications(session, newOrganization.getShortName(), getTemplateGroup(session, vo),organizationMembersGroup);
			copyFormAndNotifications(session, newOrganization.getShortName(), getAdminTemplateGroup(session, vo), organizationAdminsGroup);

			// set specific module for the admins group
			ApplicationForm newForm = null;
			try {
				newForm = registrar.getFormForGroup(organizationAdminsGroup);
				newForm.setModuleClassName("EscPithiaOrganizationAdmins");
				registrar.updateForm(session, newForm);
			} catch (PerunException e) {
				throw new InternalErrorException("Can't set registration module to the admins groups!", e);
			}

			// copy group settings from template

			Group templateGroup = getTemplateGroup(session, vo);
			copySettings(perun, session, templateGroup, organizationMembersGroup,AttributesManager.NS_GROUP_ATTR_DEF+":blockManualMemberAdding");
			// handle replace in attribute value
			Attribute attribute = perun.getAttributesManagerBl().getAttribute(session, templateGroup, AttributesManager.NS_GROUP_ATTR_DEF+":applicationURL");
			attribute.setValue(attribute.valueAsString().replace("{groupName}", URLEncoder.encode(organizationMembersGroup.getName(), StandardCharsets.UTF_8)));
			perun.getAttributesManagerBl().setAttribute(session, organizationMembersGroup, attribute);

			Group adminTemplateGroup = getTemplateGroup(session, vo);
			copySettings(perun, session, adminTemplateGroup, organizationAdminsGroup,AttributesManager.NS_GROUP_ATTR_DEF+":blockManualMemberAdding");
			// handle replace in attribute value
			Attribute attribute2 = perun.getAttributesManagerBl().getAttribute(session, adminTemplateGroup, AttributesManager.NS_GROUP_ATTR_DEF+":applicationURL");
			attribute2.setValue(attribute2.valueAsString().replace("{groupName}", URLEncoder.encode(organizationAdminsGroup.getName(), StandardCharsets.UTF_8)));
			perun.getAttributesManagerBl().setAttribute(session, organizationAdminsGroup, attribute2);

			// remove group member in the end, so they can ask for more organizations later
			perun.getGroupsManagerBl().removeMember(session, group, member);

		}

		return app;

	}

	private String getOrganizationName(PerunSession session, Application application) throws PrivilegeException, RegistrarException {

		List<ApplicationFormItemData> items = registrar.getApplicationDataById(session, application.getId());
		for (ApplicationFormItemData item : items) {
			if ("groupName".equals(item.getShortname())) {
				return item.getValue();
			}
		}
		return null;

	}

	private String getOrganizationDescription(PerunSession session, Application application) throws PrivilegeException, RegistrarException {

		List<ApplicationFormItemData> items = registrar.getApplicationDataById(session, application.getId());
		for (ApplicationFormItemData item : items) {
			if ("groupDescription".equals(item.getShortname())) {
				return item.getValue();
			}
		}
		return null;

	}

	private Group getTemplateGroup(PerunSession session, Vo vo) throws PrivilegeException, RegistrarException, GroupNotExistsException {

		PerunBl perun = (PerunBl)session.getPerun();
		return perun.getGroupsManagerBl().getGroupByName(session, vo, "organizationTemplate");

	}

	private Group getAdminTemplateGroup(PerunSession session, Vo vo) throws PrivilegeException, RegistrarException, GroupNotExistsException {

		PerunBl perun = (PerunBl)session.getPerun();
		return perun.getGroupsManagerBl().getGroupByName(session, vo, "organizationTemplateAdmins");

	}

	private void copySettings(PerunBl perun, PerunSession session, Group templateGroup, Group newOrganization, String attributeName) throws WrongAttributeAssignmentException, AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeValueException {
		Attribute attribute = perun.getAttributesManagerBl().getAttribute(session, templateGroup, attributeName);
		perun.getAttributesManagerBl().setAttribute(session, newOrganization, attribute);
	}

	private void copyFormAndNotifications(PerunSession session, String organizationName, Group templateGroup, Group destinationGroup) {

		// copy registration form and notifications from template
		try {
			registrar.createApplicationFormInGroup(session, destinationGroup);
			registrar.copyFormFromGroupToGroup(session, templateGroup, destinationGroup);
			registrar.getMailManager().copyMailsFromGroupToGroup(session, templateGroup, destinationGroup);

			ApplicationForm newForm = registrar.getFormForGroup(destinationGroup);

			List<ApplicationFormItem> items = registrar.getFormItems(session, newForm);
			for (ApplicationFormItem item : items) {
				if ("header".equals(item.getShortname())) {
					Map<Locale, ApplicationFormItem.ItemTexts> i18n = item.getI18n();
					for (Locale locale : i18n.keySet()) {
						ApplicationFormItem.ItemTexts text = i18n.get(locale);
						text.setLabel(text.getLabel().replace("{groupName}", organizationName));
					}
					registrar.updateFormItem(session, item);
				}
			}

			List<ApplicationMail> mails = registrar.getMailManager().getApplicationMails(session,newForm);
			for (ApplicationMail mail : mails) {
				Map<Locale, ApplicationMail.MailText> texts = mail.getMessage();
				for (Locale locale : texts.keySet()) {
					ApplicationMail.MailText text = texts.get(locale);
					text.setSubject(text.getSubject().replace("{groupName}", organizationName));
					text.setText(text.getText().replace("{groupName}", organizationName));
				}
				registrar.getMailManager().updateMailById(session, mail);
			}

		} catch (PerunException e) {
			throw new InternalErrorException("Unable to set registration form and notifications to group - organization!", e);
		}

	}

}
