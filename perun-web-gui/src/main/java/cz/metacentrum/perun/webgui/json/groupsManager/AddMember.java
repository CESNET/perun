package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonErrorHandler;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.*;

/**
 * Ajax query to add member to group
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddMember {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "groupsManager/addMember";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public AddMember() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public AddMember(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to add member to group
	 *
	 * @param group  group
	 * @param member member to be member of group
	 */
	public void addMemberToGroup(final Group group, final RichMember member) {

		String errorMsg = "";

		if (((group != null) ? group.getId() : 0) == 0) {
			errorMsg += "Wrong parameter <strong>Group</strong>.</br>";
		}

		if (((member != null) ? member.getId() : 0) == 0) {
			errorMsg += "Wrong parameter <strong>Member</strong>.";
		}

		if (errorMsg.length() > 0) {
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents() {
			@Override
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding member: " + member.getUser().getFullName() + " to group: " + group.getShortName() + " failed.");
				handleCommonExceptions(error, member, group);
				events.onError(error);
			}
			@Override
			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Member: " + member.getUser().getFullName() + " added to group: " + group.getShortName());
				events.onFinished(jso);
			}
			@Override
			public void onLoadingStart() {
				events.onLoadingStart();
			}
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		// to allow own error handling for attributes errors.
		jspc.setHidden(true);
		// put data
		jspc.put("group", new JSONNumber(group.getId()));
		jspc.put("member", new JSONNumber(member.getId()));
		jspc.sendData(JSON_URL);

	}

	/**
	 * Handle common exceptions caused by this callback.
	 *
	 * @param error PerunError returned from Perun
	 * @param member Member related to action
	 * @param group Group related to action
	 */
	private void handleCommonExceptions(PerunError error, RichMember member, Group group) {

		if (error != null) {

			if ("WrongAttributeValueException".equals(error.getName())) {

				Attribute a = error.getAttribute();
				GeneralObject holder = error.getAttributeHolder();
				GeneralObject secondHolder = error.getAttributeHolderSecondary();

				String text = member.getUser().getFullNameWithTitles() + " can't be added to group " + group.getShortName() + ".";

				if (a != null) {

					if (a.getValue().equals("null")) {
						text += "<p>Following setting is missing, but it's required by resources this group has access to.";
					} else {
						text += "<p>Following setting, required by resources this group has access to, is wrong.";
					}

					String attrName = a.getDisplayName();
					String attrValue = a.getValue();
					text += "<p><strong>Setting:&nbsp;</strong>" + attrName + "<br />";

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

					if (!a.getValue().equals("null")) {
						text += "<strong>Value:&nbsp;</strong>" + attrValue;
					}

					text += "<p>Please fix the issue before adding member to group on member's settings page. If you are not allowed to do so, ask user to fill application form or edit own entries on user detail.";

				} else {
					text += "<p><i>Attribute is null, please report this error.</i>";
				}

				UiElements.generateError(error, "Wrong settings", text);

			} else if ("AlreadyMemberException".equals(error.getName())) {

				UiElements.generateError(error, "Already member", member.getUser().getFullNameWithTitles() + " is already member of group " + group.getShortName() + ".");

			} else if ("WrongReferenceAttributeValueException".equals(error.getName())) {

				String text = member.getUser().getFullNameWithTitles() + " can't be added to group " + group.getShortName() + ".";

				text += "<p>Following combination of settings is not correct:";

				Attribute a = error.getAttribute();
				Attribute a2 = error.getReferenceAttribute();

				if (a != null) {
					String attrName = a.getDisplayName();
					String attrValue = a.getValue();
					String entity = a.getEntity();
					text += "<p><strong>Setting&nbsp;1:</strong>&nbsp;" + attrName + " (" + entity + ")";
					text += "<br/><strong>Value&nbsp;1:</strong>&nbsp;" + attrValue;
				} else {
					text += "<p><i>Setting 1 is null or not present in error message.</i>";
				}

				if (a2 != null) {
					String attrName = a2.getDisplayName();
					String attrValue = a2.getValue();
					String entity = a2.getEntity();
					text += "<p><strong>Setting&nbsp;2:</strong>&nbsp;" + attrName + " (" + entity + ")";
					text += "<br/><strong>Value&nbsp;2:</strong>&nbsp;" + attrValue;
				} else {
					text += "<p><i>Setting 2 is null or not present in error message.</i>";
				}

				UiElements.generateError(error, "Wrong settings", text);

			} else {

				// use standard processing for other errors
				JsonErrorHandler.alertBox(error);

			}

		}

	}

}