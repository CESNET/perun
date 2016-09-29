package cz.metacentrum.perun.webgui.json;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebConstants;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ApplicationMessages;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.rtMessagesManager.SendMessageToRt;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Class for handling Error objects returned from RPC server
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class JsonErrorHandler {

	/**
	 * Creates and display an error box containing information about error from RPC server.
	 *
	 * @param error Error object returned from RPC
	 */
	public static void alertBox(final PerunError error) {

		if (PerunWebSession.getInstance().isPerunAdmin()) {
			// PERUN ADMIN SEE RAW ERROR MESSAGE
			UiElements.generateError(error, getCaption(error), "<span style=\"color:red\">" + error.getName() + "</span><p>" + error.getErrorInfo());
		} else {
			// OTHERS SEE TRANSLATED TEXT
			UiElements.generateError(error, getCaption(error), getText(error.getName(), error));
		}

	}

	/**
	 * Creates and display a report box used for reporting errors
	 *
	 * @param error Error object returned from RPC
	 */
	public static void reportBox(final PerunError error) {

		// clear password fields if present
		final JSONObject postObject = new JSONObject(JsonUtils.parseJson(error.getPostData()));

		if (postObject.getJavaScriptObject() != null) {
			clearPasswords(postObject);
		}

		String s = "unknown";
		if (PerunWebSession.getInstance().getTabManager() != null) {
			s = PerunWebSession.getInstance().getTabManager().getCurrentUrl(true);
		}
		final String status = s;

		final TextBox boxSubject = new TextBox();
		boxSubject.setValue("Reported error: " + error.getRequest().getManager() + "/" + error.getRequest().getMethod() + " (" +error.getErrorId() + ")");
		boxSubject.setWidth("100%");

		final TextArea messageTextBox = new TextArea();
		messageTextBox.setSize("335px", "100px");

		// ok click - report
		ClickHandler sendReportHandler = new ClickHandler() {

			public void onClick(ClickEvent event) {

				String text = getErrorFullMessage(messageTextBox, error, postObject, status);

				final String finalText = text;

				// request itself
				SendMessageToRt msg = new SendMessageToRt(new JsonCallbackEvents() {
					@Override
					public void onError(PerunError error) {

						FlexTable layout = new FlexTable();

						TextArea scrollPanel = new TextArea();
						scrollPanel.setText(finalText);

						layout.setWidget(0, 0, new HTML("<p>" + new Image(LargeIcons.INSTANCE.errorIcon())));
						layout.setHTML(0, 1, "<p>Reporting errors is not working at the moment. We are sorry for inconvenience. <p>Please send following text to <strong>perun@cesnet.cz</strong>.");

						layout.getFlexCellFormatter().setColSpan(1, 0, 2);
						layout.setWidget(1, 0, scrollPanel);

						scrollPanel.setSize("350px", "150px");

						layout.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
						layout.getFlexCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
						layout.getFlexCellFormatter().setStyleName(0, 0, "alert-box-image");

						Confirm c = new Confirm("Error report is not working", layout, true);
						c.setNonScrollable(true);
						c.setAutoHide(false);
						c.show();

					}
				});

				if (boxSubject.getValue().isEmpty()) {
					msg.sendMessage(SendMessageToRt.DEFAULT_QUEUE, "Reported error: " + error.getRequest().getManager() + "/" + error.getRequest().getMethod() + " (" +error.getErrorId() + ")", text);
				} else {
					msg.sendMessage(SendMessageToRt.DEFAULT_QUEUE, boxSubject.getValue(), text);
				}

			}
		};

		FlexTable baseLayout = new FlexTable();
		baseLayout.setStyleName("alert-box-table");
		baseLayout.setWidth("350px");
		baseLayout.setHTML(0, 0, "<p>You can provide any message for this error report (e.g. describing what you tried to do). When you are done, click on send button.");
		baseLayout.setHTML(1, 0, "<strong>Subject:</strong>");
		baseLayout.setWidget(2, 0, boxSubject);
		baseLayout.setHTML(3, 0, "<strong>Message:</strong>");
		baseLayout.setWidget(4, 0, messageTextBox);
		final Anchor showDetails = new Anchor("Show message preview");
		final TextArea fullMessage = new TextArea();
		fullMessage.setReadOnly(true);
		fullMessage.setVisible(false);
		fullMessage.setSize("335px", "100px");
		showDetails.addClickHandler(new ClickHandler() {
			boolean pressed = false;
			@Override
			public void onClick(ClickEvent clickEvent) {
				if (pressed) {
					showDetails.setText("Show message preview");
					fullMessage.setVisible(false);
				} else {
					showDetails.setText("Hide preview");
					fullMessage.setText(getErrorFullMessage(messageTextBox, error, postObject, status));
					fullMessage.setVisible(true);
				}
				pressed = !pressed;
			}
		});

		messageTextBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent changeEvent) {
				if (fullMessage.isVisible()) {
					fullMessage.setText(getErrorFullMessage(messageTextBox, error, postObject, status));
				}
			}
		});

		baseLayout.setWidget(5, 0, showDetails);
		baseLayout.setWidget(6, 0, fullMessage);

		// box definition
		final Confirm conf = new Confirm(WidgetTranslation.INSTANCE.jsonClientSendErrorButton(), baseLayout, sendReportHandler, WidgetTranslation.INSTANCE.jsonClientSendErrorButton(), true);
		conf.setOkIcon(SmallIcons.INSTANCE.emailIcon());
		conf.setNonScrollable(true);
		conf.setAutoHide(false);
		conf.setFocusOkButton(true);
		conf.show();

		messageTextBox.setFocus(true);

	}

	private static String getErrorFullMessage(TextArea messageTextBox, PerunError error, JSONObject postObject, String status) {
		String text = messageTextBox.getText() + "\n\n";
		text += "-------------------------------------\n";
		text += "Technical details: \n\n";
		text += error.getErrorId() + " - " + error.getName() + "\n";
		text += error.getErrorInfo() + "\n\n";
		text += "Perun instance: " + Utils.perunInstanceName()+ "\n";
		text += "Request: " + error.getRequestURL() + "\n";
		if (postObject != null) text += "Post data: " + postObject.toString() + "\n";
		text += "Application state: " + status + "\n\n";
		text += "Authz: " + PerunWebSession.getInstance().getRolesString() + "\n\n";

		if (PerunWebSession.getInstance().getUser() == null) {

			// post original authz if unknown user
			text += "Actor/ExtSource: " + PerunWebSession.getInstance().getPerunPrincipal().getActor() + " / " +
					PerunWebSession.getInstance().getPerunPrincipal().getExtSource() + " (" +
					PerunWebSession.getInstance().getPerunPrincipal().getExtSourceType() + ")" + "\n\n";

		}
		text += "GUI version: " + PerunWebConstants.INSTANCE.guiVersion();
		return text;
	}

	/**
	 * Clear all password-like params from posted objects
	 *
	 * @param object object to clear
	 */
	public static void clearPasswords(JSONObject object) {

		for (String key : object.keySet()) {
			if (key.equals("oldPassword") || key.equals("newPassword") || key.equals("password")) {
				object.put(key, new JSONString(""));
			} else {
				JSONObject obj = object.get(key).isObject();
				if (obj != null) {
					clearPasswords(obj);
				}
			}
		}
	}

	/**
	 * Return caption text for error box
	 *
	 * @param error error object
	 * @return text for caption
	 */
	private static String getCaption(PerunError error) {

		String errorName = error.getName();

		if ("PrivilegeException".equalsIgnoreCase(errorName)) {

			return WidgetTranslation.INSTANCE.jsonClientNotAuthorizedHeader();

		} else if ("WrongAttributeAssignmentException".equalsIgnoreCase(errorName)) {

			return "Wrong attribute assignment";

		} else if ("WrongAttributeValueException".equalsIgnoreCase(errorName)) {

			return "Wrong attribute value";

		} else if ("WrongReferenceAttributeValueException".equalsIgnoreCase(errorName)) {

			return "Wrong value of related attributes";

		} else if ("MissingRequiredDataException".equalsIgnoreCase(errorName)) {

			return "IDP doesn't provide required data";

		} else if ("ApplicationNotCreatedException".equalsIgnoreCase(errorName)) {

			return ApplicationMessages.INSTANCE.errorWhileCreatingApplication();

		}

		// default caption
		return WidgetTranslation.INSTANCE.jsonClientAlertBoxHeader();

	}

	private static String getText(String errorName, PerunError error) {

		String pleaseRefresh = "<p>Try to <strong>refresh the browser</strong> window and retry.<br />If problem persist, please report it.";

		// RPC ERRORS
		if ("RpcException".equalsIgnoreCase(errorName)) {

			if ("UNCATCHED_EXCEPTION".equalsIgnoreCase(error.getType())) {
				return "Unknown error occurred. Please report it.";
			} else {
				return "Error in communication with server. " + pleaseRefresh;
			}

		} else if ("PrivilegeException".equalsIgnoreCase(errorName)) {

			return WidgetTranslation.INSTANCE.jsonClientNotAuthorizedMessage();

		} else if ("WrongAttributeAssignmentException".equalsIgnoreCase(errorName)) {

			return "You tried to set wrong attribute for entity. Please report this error.";

		} else if ("WrongAttributeValueException".equalsIgnoreCase(errorName)) {

			Attribute a = error.getAttribute();
			GeneralObject holder = error.getAttributeHolder();
			GeneralObject secondHolder = error.getAttributeHolderSecondary();

			String text = "Wrong value of attribute (value or format).<p>";

			if (holder != null) {
				if (!holder.getName().equalsIgnoreCase("undefined")) {
					text += "<strong>" + holder.getObjectType() + ":</strong>&nbsp;" + holder.getName() + "<br />";
				}
			}
			if (secondHolder != null) {
				if (!secondHolder.getName().equalsIgnoreCase("undefined")) {
					text += "<strong>" + secondHolder.getObjectType() + ":</strong>&nbsp;" + secondHolder.getName() + "<br />";
				}
			}
			if (a != null) {
				String attrName = a.getDisplayName();
				String attrValue = a.getValue();
				text += "<strong>Attribute:&nbsp;</strong>" + attrName + "<br /><strong>Value:&nbsp;</strong>" + attrValue;
			} else {
				text += "<i>Attribute is null</i>";
			}

			return text;

		} else if ("WrongReferenceAttributeValueException".equalsIgnoreCase(errorName)) {

			String text = "Value of one of related attributes is incorrect.";

			Attribute a = error.getAttribute();
			Attribute a2 = error.getReferenceAttribute();

			if (a != null) {
				String attrName = a.getDisplayName();
				String attrValue = a.getValue();
				String entity = a.getEntity();
				text += "<p><strong>Attribute&nbsp;1:</strong>&nbsp;" + attrName + " (" + entity + ")";
				text += "<br/><strong>Value&nbsp;1:</strong>&nbsp;" + attrValue;
			} else {
				text += "<p><i>Attribute 1 is null</i>";
			}

			if (a2 != null) {
				String attrName = a2.getDisplayName();
				String attrValue = a2.getValue();
				String entity = a2.getEntity();
				text += "<p><strong>Attribute&nbsp;2:</strong>&nbsp;" + attrName + " (" + entity + ")";
				text += "<br/><strong>Value&nbsp;2:</strong>&nbsp;" + attrValue;
			} else {
				text += "<p><i>Attribute 2 is null</i>";
			}

			return text;

		} else if ("AttributeNotExistsException".equalsIgnoreCase(errorName)) {

			Attribute a = error.getAttribute();
			if (a != null) {
				return "Attribute definition for attribute <i>" + a.getName() + "</i> doesn't exist.";
			} else {
				return "Attribute definition for attribute <i>null</i> doesn't exist.";
			}

			// ALL CABINET EXCEPTIONS
		} else if ("CabinetException".equalsIgnoreCase(errorName)) {

			String text = "";
			if (error.getType().equalsIgnoreCase("NO_IDENTITY_FOR_PUBLICATION_SYSTEM")) {
				text = "You don't have registered identity in Perun related to selected publication system.<p>Please visit <a target=\"new\" href=\"" + Utils.getIdentityConsolidatorLink(false) + "\">identity consolidator</a> to add more identities.";
			}

			return text;

			// STANDARD ERRORS ALPHABETICALLY
		} else if ("AlreadyAdminException".equalsIgnoreCase(errorName)) {

			String text = "";
			if (error.getUser() != null) {
				text = error.getUser().getFullName();
			} else {
				text = "User";
			}
			if (error.getVo() != null) {
				text += " is already manager of VO: " + error.getVo().getName();
			} else if (error.getFacility() != null) {
				text += " is already manager of Facility: " + error.getFacility().getName();
			} else if (error.getGroup() != null) {
				text += " is already manager of Group: " + error.getGroup().getName();
			} else if (error.getSecurityTeam() != null) {
				text += " is already manager of SecurityTeam: " + error.getSecurityTeam().getName();
			}
			return text;

		} else if ("AlreadyMemberException".equalsIgnoreCase(errorName)) {

			// TODO - this exception must contain user first !!
			return "User is already member of VO / Group.";

		} else if ("AlreadyReservedLoginException".equalsIgnoreCase(errorName)) {

			String text = "";
			if (error.getLogin() != null) {
				text += "Login: " + error.getLogin();
				if (error.getNamespace() != null) {
					text += " in namespace: " + error.getNamespace() + " is already reserved.";
				} else {
					text += " is already reserved.";
				}
			} else {
				text += "Login";
				if (error.getNamespace() != null) {
					text += " in namespace: " + error.getNamespace() + " is already reserved.";
				} else {
					text += " is already reserved in selected namespace.";
				}
			}
			return text;

		} else if ("AttributeAlreadyAssignedException".equalsIgnoreCase(errorName)) {

			if (error.getAttribute() != null) {
				return "Attribute <i>" + error.getAttribute().getDisplayName() + "</i> is already set as required by service.";
			} else {
				return "Attribute is already set as required by service.";
			}

		} else if ("AttributeExistsException".equalsIgnoreCase(errorName)) {

			return "Same attribute definition already exists in Perun.";

		} else if ("AttributeNotAssignedException".equalsIgnoreCase(errorName)) {

			if (error.getAttribute() != null) {
				return "Attribute <i>" + error.getAttribute().getDisplayName() + "</i> is already NOT required by service.";
			} else {
				return "Attribute is already NOT required by service.";
			}

		} else if ("AttributeNotExistsException".equalsIgnoreCase(errorName)) {

			// FIXME - attribute object inside is never used, but has good description
			return error.getErrorInfo();

		} else if ("AttributeValueException".equalsIgnoreCase(errorName)) {

			// FIXME - core always uses extensions of this exception
			return error.getErrorInfo();

		} else if ("ApplicationNotCreatedException".equalsIgnoreCase(errorName)) {

			return ApplicationMessages.INSTANCE.errorWhileCreatingApplicationMessage();

		} else if ("CandidateNotExistsException".equalsIgnoreCase(errorName)) {

			return "Candidate for VO membership doesn't exists in external source.";

		} else if ("ClusterNotExistsException".equalsIgnoreCase(errorName)) {

			return "Facility is not of type <i>cluster</i> or <i>virtual cluster</i>";

		} else if ("ConsistencyErrorException".equalsIgnoreCase(errorName)) {

			return "Your operation can't be completed. There seems to be a problem with DB consistency, please report this error.";

		} else if ("DestinationAlreadyAssignedException".equalsIgnoreCase(errorName)) {

			if (error.getDestination() != null) {
				return "Destination <i>" + error.getDestination().getDestination() + "</i> already exists for facility/service.";
			} else {
				return "Same destination already exists for facility/service combination.";
			}

		} else if ("DestinationAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			if (error.getDestination() != null) {
				return "Destination <i>" + error.getDestination().getDestination() + "</i> already removed for facility/service.";
			} else {
				return "Destination is already removed from facility/service combination.";
			}

		} else if ("DestinationExistsException".equalsIgnoreCase(errorName)) {

			return "Same destination already exists.";

		} else if ("DestinationNotExistsException".equalsIgnoreCase(errorName)) {

			return "Destination of this name/id doesn't exists.";

		} else if ("DiacriticNotAllowedException".equalsIgnoreCase(errorName)) {

			// has meaningful info
			return error.getErrorInfo();

			// FIXME - ENTITY exceptions are always extended - we will use specific types

		} else if ("ExtendMembershipException".equalsIgnoreCase(errorName)) {

			String text = "Membership in VO can't be established or extended. ";
			if ("NOUSERLOA".equalsIgnoreCase(error.getReason())) {
				text += " User's IDP does not provide Level of Assurance but VO requires it.";
			} else if ("INSUFFICIENTLOA".equalsIgnoreCase(error.getReason())) {
				text += " User's Level of Assurance is not sufficient for VO.";
			} else if ("INSUFFICIENTLOAFOREXTENSION".equalsIgnoreCase(error.getReason())) {
				text += " User's Level of Assurance is not sufficient for VO.";
			} else if ("OUTSIDEEXTENSIONPERIOD".equalsIgnoreCase(error.getReason())) {
				text += " It can be done usually in short time before and after membership expiration.";
			}
			return text;

		} else if ("ExtSourceAlreadyAssignedException".equalsIgnoreCase(errorName)) {

			if (error.getExtSource() != null) {
				return "Same external source is already assigned to your VO." +
						"<p><strong>Name:</strong> " + error.getExtSource().getName() + "</br>" +
						"<strong>Type:</strong> " + error.getExtSource().getType();
			} else {
				return "Same external source is already assigned to your VO.";
			}

		} else if ("ExtSourceAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			if (error.getExtSource() != null) {
				return "Same external source was already removed from your VO." +
						"<p><strong>Name:</strong> " + error.getExtSource().getName() + "</br>" +
						"<strong>Type:</strong> " + error.getExtSource().getType();
			} else {
				return "Same external source was already removed from your VO.";
			}

		} else if ("ExtSourceExistsException".equalsIgnoreCase(errorName)) {

			if (error.getExtSource() != null) {
				return "Same external source already exists." +
						"<p><strong>Name:</strong> " + error.getExtSource().getName() + "</br>" +
						"<strong>Type:</strong> " + error.getExtSource().getType();
			} else {
				return "Same external source already exists.";
			}

		} else if ("ExtSourceInUseException".equalsIgnoreCase(errorName)) {

			// TODO - ext source not in exception
			return "Selected external source is currently in use and can't be removed from VO or deleted.";

		} else if ("ExtSourceNotAssignedException".equalsIgnoreCase(errorName)) {

			// TODO - ext source not in exception
			return "Selected external source is not assigned to your VO and can't be removed.";

		} else if ("ExtSourceNotExistsException".equalsIgnoreCase(errorName)) {

			// TODO - better text ?? + ext source not in exception
			return "External source of this ID or name doesn't exists.";

		} else if ("ExtSourceUnsupportedOperationException".equalsIgnoreCase(errorName)) {

			// TODO - probably is never thrown to GUI ??
			return error.getErrorInfo();

		} else if ("FacilityAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			return "Facility of the same name and type was already deleted.";

		} else if ("FacilityExistsException".equalsIgnoreCase(errorName)) {

			return "Facility of the same name and type already exists.";

		} else if ("FacilityNotExistsException".equalsIgnoreCase(errorName)) {

			return "Requested Facility (by id or name/type) doesn't exists.";

		} else if ("FacilityNotExistsException".equalsIgnoreCase(errorName)) {

			return "Requested Facility (by id or name/type) doesn't exists.";

		} else if ("GroupAlreadyAssignedException".equalsIgnoreCase(errorName)) {

			Group g = error.getGroup();
			if (g != null) {
				return "Group: " + g.getName() + " is already assigned to Resource.";
			} else {
				return "Group is already assigned to Resource.";
			}

		} else if ("GroupExistsException".equalsIgnoreCase(errorName)) {

			return "Group with same name already exists in your VO. Group names must be unique in VO.";

		} else if ("GroupAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			return "Same group was already removed from your VO/Group.";

		} else if ("GroupAlreadyRemovedFromResourceException".equalsIgnoreCase(errorName)) {

			return "Same group was already removed from resource.";

		} else if ("GroupNotDefinedOnResourceException".equalsIgnoreCase(errorName)) {

			return "Group is not assigned to Resource and therefore can't be removed from it.";

		} else if ("GroupNotExistsException".equalsIgnoreCase(errorName)) {

			return "Requested Group (by id or name/vo) doesn't exists.";

		} else if ("GroupResourceMismatchException".equalsIgnoreCase(errorName)) {

			return "Group and Resource doesn't belong to the same VO.";

		} else if ("GroupOperationsException".equalsIgnoreCase(errorName)) {

			return "Action is not permitted, since it violates group arithmetic rules.";

		} else if ("GroupRelationAlreadyExists".equalsIgnoreCase(errorName)) {

			return "Groups are already in a relation. Please refresh your view/table to see current state.";

		} else if ("GroupRelationCannotBeRemoved".equalsIgnoreCase(errorName)) {

			return "Relation can't be removed, since groups are in a direct hierarchy. If necessary, please delete the sub-group.";

		} else if ("GroupRelationDoesNotExist".equalsIgnoreCase(errorName)) {

			return "Groups are not in a relation. Please refresh your view/table to see current state.";

		} else if ("GroupRelationNotAllowed".equalsIgnoreCase(errorName)) {

			return "You can't add groups to relation. It would create a cycle.";

		} else if ("GroupSynchronizationAlreadyRunningException".equalsIgnoreCase(errorName)) {

			return "Can't start group synchronization between Perun and external source, because it's already running.";

		} else if ("HostAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			return "Same host was already removed from facility.";

		} else if ("HostExistsException".equalsIgnoreCase(errorName)) {

			// TODO - Facility object in this exception would really help
			return "Either same host already exists on Facility or you are trying to add more than one host to (v)host Facility type (can have only one).";

		} else if ("HostNotExistsException".equalsIgnoreCase(errorName)) {

			// TODO - host object is not filled on core side
			return "Requested Host (by name) doesn't exists.";

		} else if ("IllegalArgumentException".equalsIgnoreCase(errorName)) {

			// FIXME - is this generic error ??
			return "Your operation can't be completed. Illegal argument exception occurred. Please report this error.";

		} else if ("InternalErrorException".equalsIgnoreCase(errorName)) {

			// FIXME - is this generic error ??
			return "Your operation can't be completed. Internal error occurred. Please report this error.";

		} else if ("LoginNotExistsException".equalsIgnoreCase(errorName)) {

			// TODO - login + namespace should be in exception
			return "User doesn't have login set for selected namespace.";

		} else if ("MaxSizeExceededException".equalsIgnoreCase(errorName)) {

			// has meaningfull message
			return error.getErrorInfo();

		} else if ("MemberAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			return "Member was already removed from group/VO.";

		} else if ("MemberNotAdminException".equalsIgnoreCase(errorName)) {

			// FIXME - will be removed in favor of UserNotAdminException
			return "Can't remove user from administrators (user is not an administrator).";

		} else if ("MemberNotExistsException".equalsIgnoreCase(errorName)) {

			return "Requested member (by id or ues) doesn't exists.";

		} else if ("MemberNotValidYetException".equalsIgnoreCase(errorName)) {

			return "Can't disable membership for VO member if not valid yet.";

		} else if ("MembershipMismatchException".equalsIgnoreCase(errorName)) {

			return "Can't add member to group. They are from different VOs.";

		} else if ("MessageParsingFailException".equalsIgnoreCase(errorName)) {

			return error.getErrorInfo();

		} else if ("ModuleNotExistsException".equalsIgnoreCase(errorName)) {

			return "Module for virtual attribute doesn't exists. Please report this error.";

		} else if ("NotGroupMemberException".equalsIgnoreCase(errorName)) {

			return "Can't remove user from group. User already isn't group member.";

		} else if ("NotMemberOfParentGroupException".equalsIgnoreCase(errorName)) {

			return "Can't add user to this group. User must be member of parent group first.";

		} else if ("NotSpecificUserExpectedException".equalsIgnoreCase(errorName)) {

			return "Operation can't be done. Expected person type of user, but service type was provided instead.";

		} else if ("NumberNotInRangeException".equalsIgnoreCase(errorName)) {

			return error.getErrorInfo();

		} else if ("NumbersNotAllowedException".equalsIgnoreCase(errorName)) {

			return error.getErrorInfo();

		} else if ("OwnerAlreadyAssignedException".equalsIgnoreCase(errorName)) {

			return "Can't add owner to Facility. Owner is already assigned.";

		} else if ("OwnerAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			return "Can't remove owner from Facility. Owner is already removed.";

		} else if ("OwnerNotExistsException".equalsIgnoreCase(errorName)) {

			return "Requested Owner (by id) doesn't exists.";

		} else if ("ParentGroupNotExistsException".equalsIgnoreCase(errorName)) {

			return "Group doesn't have parent group.";

		} else if ("PasswordChangeFailedException".equalsIgnoreCase(errorName)) {

			return "Changing password failed due to an internal error. Please report it.";

		} else if ("PasswordCreationFailedException".equalsIgnoreCase(errorName)) {

			return "Password creation failed due to an internal error. Please report it.";

		} else if ("PasswordDeletionFailedException".equalsIgnoreCase(errorName)) {

			return "Password deletion failed due to an internal error. Please report it.";

		} else if ("PasswordDoesntMatchException".equalsIgnoreCase(errorName)) {

			return "Can't set new password. Old password doesn't match.";

		} else if ("PasswordStrengthFailedException".equalsIgnoreCase(errorName)) {

			return "Used password doesn't match required strength constraints.";

		} else if ("PasswordOperationTimeoutException".equalsIgnoreCase(errorName)) {

			return "Operation with password exceeded expected time limit.";

		} else if ("RelationExistsException".equalsIgnoreCase(errorName)) {

			// FIXME - better text on core side
			return error.getErrorInfo();

		} else if ("RelationNotExistsException".equalsIgnoreCase(errorName)) {

			// FIXME - better text on core side
			return error.getErrorInfo();

		} else if ("ResourceAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			return "Same resource was already removed from facility (deleted).";

		} else if ("ResourceExistsException".equalsIgnoreCase(errorName)) {

			return "Resource with same name \"" + error.getResource().getName() + "\" already exists with id="+error.getResource().getId()+".";

		} else if ("ResourceNotExistsException".equalsIgnoreCase(errorName)) {

			return "Requested resource (by id) doesn't exists.";

		} else if ("ResourceTagAlreadyAssignedException".equalsIgnoreCase(errorName)) {

			// FIXME - must contain also resource
			return "Same tag is already assigned to resource.";

		} else if ("ResourceTagNotAssignedException".equalsIgnoreCase(errorName)) {

			// FIXME - must contain also resource
			return "Tag is not assigned to resource.";

		} else if ("ResourceTagNotExistsException".equalsIgnoreCase(errorName)) {

			// FIXME - must contain also resource
			return error.getErrorInfo();

		} else if ("SecurityTeamAlreadyAssignedException".equalsIgnoreCase(errorName)) {

			if (error.getSecurityTeam() != null) {
				return "SecurityTeam <i>" + error.getSecurityTeam().getName() + "</i> is already assigned to facility.";
			} else {
				return "Same SecurityTeam is already assigned to facility.";
			}

		} else if ("ServiceAlreadyAssignedException".equalsIgnoreCase(errorName)) {

			// FIXME - must contain also resource
			if (error.getService() != null) {
				return "Service " + error.getService().getName() + " is already assigned to resource.";
			} else {
				return "Same service is already assigned to resource.";
			}

		} else if ("ServiceAlreadyBannedException".equalsIgnoreCase(errorName)) {

			if (error.getService() != null && error.getFacility() != null) {
				return "Service " + error.getService().getName() + " is already banned on facility "+error.getFacility().getName()+".";
			} else {
				return "Same service is already banned on facility.";
			}

		} else if ("ServiceExistsException".equalsIgnoreCase(errorName)) {

			if (error.getService() != null) {
				return "Service " + error.getService().getName() + " already exists in Perun. Choose different name.";
			} else {
				return "Service with same name already exists in Perun.";
			}

		} else if ("ServiceNotAssignedException".equalsIgnoreCase(errorName)) {

			// FIXME - must contain also resource
			if (error.getService() != null) {
				return "Service " + error.getService().getName() + " is not assigned to resource.";
			} else {
				return "Service is not assigned to resource.";
			}

		} else if ("ServiceAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			return "Same service was already deleted.";

		} else if ("ServiceNotExistsException".equalsIgnoreCase(errorName)) {

			return "Requested service (by id or name) doesn't exists.";

		} else if ("ServicesPackageExistsException".equalsIgnoreCase(errorName)) {

			// TODO - we don't support service packages yet
			return error.getErrorInfo();

		} else if ("ServiceAlreadyRemovedFromServicePackageException".equalsIgnoreCase(errorName)) {

			return "Same service was already removed from service package.";

		} else if ("ServiceAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			return "Same service was already deleted.";

		} else if ("SpecificUserExpectedException".equalsIgnoreCase(errorName)) {

			return "Operation can't be done. Expected specific type of user, but person type was provided instead.";

		} else if ("SpecificUserAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			return "Same specific user was already removed from user.";

		} else if ("SpecificUserOwnerAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			return "Same user was already removed from owners of specific user.";

		} else if ("SpecificUserMustHaveOwnerException".equalsIgnoreCase(errorName)) {

			return "Specific type user must have at least 1 person type user assigned, which is responsible for it.";

		} else if ("SpaceNotAllowedException".equalsIgnoreCase(errorName)) {

			return error.getErrorInfo();

		} else if ("SpecialCharsNotAllowedException".equalsIgnoreCase(errorName)) {

			return error.getErrorInfo() + " You can use only letters, numbers and spaces.";

		} else if ("SpecialCharsNotAllowedException".equalsIgnoreCase(errorName)) {

			return error.getErrorInfo() + " You can use only letters, numbers and spaces.";

		} else if ("SubGroupCannotBeRemovedException".equalsIgnoreCase(errorName)) {

			return "Subgroup can't be removed from resource. Only directly assigned groups can be removed.";

		} else if ("SubjectNotExistsException".equalsIgnoreCase(errorName)) {

			// FIXME - probably never thrown to GUI ?? + better exception text.
			return "Requested user by login in LDAP external source doesn't exists or more than one was found.";

		} else if ("UserExtSourceExistsException".equalsIgnoreCase(errorName)) {

			// TODO - user ext source object in exception
			return "Same user external identity already exists and is used by different user.";

		} else if ("UserExtSourceNotExistsException".equalsIgnoreCase(errorName)) {

			return "Requested user external identity doesn't exists.";

		} else if ("UserExtSourceAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			return "Same user's external identity was already removed from him/her.";

		} else if ("UserNotAdminException".equalsIgnoreCase(errorName)) {

			// FIXME - add vo, group or facility !!
			return "Can't remove user from managers of VO/Group/Facility. User is not manager.";

		} else if ("UserNotExistsException".equalsIgnoreCase(errorName)) {

			// TODO - get user from exception
			return "Requested user (by id or external identity) doesn't exists.";

		} else if ("UserAlreadyRemovedException".equalsIgnoreCase(errorName)) {

			// TODO - shoud contain user objects
			return "Same user was already deleted.";

		} else if ("VoExistsException".equalsIgnoreCase(errorName)) {

			return "VO with same name already exists. Please choose different name.";

		} else if ("VoNotExistsException".equalsIgnoreCase(errorName)) {

			// TODO - get vo from exception
			return "Requested VO (by id or name) doesn't exists.";

		} else if ("WrongModuleTypeException".equalsIgnoreCase(errorName)) {

			return error.getErrorInfo();

		} else if ("WrongRangeOfCountException".equalsIgnoreCase(errorName)) {

			return error.getErrorInfo();

		} else if ("WrongPatternException".equalsIgnoreCase(errorName)) {

			// meaningful message
			return error.getErrorInfo();

		} else if ("MissingRequiredDataException".equalsIgnoreCase(errorName)) {

			String result = "Your IDP doesn't provide all required data for this application form. Please contact your IDP to resolve this issue or log-in using different IDP.";

			String missingItems = "<p>";
			if (error.getFormItems() != null) {
				for (int i = 0; i < error.getFormItems().length(); i++) {
					missingItems += "<strong>Missing attribute: </strong>";
					missingItems += error.getFormItems().get(i).getFormItem().getFederationAttribute();
					missingItems += "<br />";
				}
			}

			result += missingItems;

			return result;

		} else if ("RequestTimeout".equalsIgnoreCase(errorName)) {

			String result = "Your operation is still processing on server. Please refresh your view (table) to see, if it ended up successfully before trying again.";

			return result;

		}


		//default text
		return error.getErrorInfo();

	}


}
