package cz.metacentrum.perun.webgui.client.localization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

/**
 * Translations for objects and object parameters with fixed values
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface ObjectTranslation extends Messages {

	public static final ObjectTranslation INSTANCE = GWT.create(ObjectTranslation.class);

	/* ============ APPLICATION OBJECT ================ */

	@DefaultMessage("Waiting for mail verification")
	String applicationStateNew();

	@DefaultMessage("Submitted")
	String applicationStateVerified();

	@DefaultMessage("Approved")
	String applicationStateApproved();

	@DefaultMessage("Rejected")
	String applicationStateRejected();

	@DefaultMessage("Initial")
	String applicationTypeInitial();

	@DefaultMessage("Extension")
	String applicationTypeExtension();

	/* ========== APPLICATION MAIL OBJECT ============ */

	@DefaultMessage("Created / user")
	String applicationMailTypeAppCreatedUser();

	@DefaultMessage("Created / manager")
	String applicationMailTypeAppCreatedVoAdmin();

	@DefaultMessage("Mail validation / user")
	String applicationMailTypeMailValidation();

	@DefaultMessage("Approved / user")
	String applicationMailTypeAppApprovedUser();

	@DefaultMessage("Rejected / user")
	String applicationMailTypeAppRejectedUser();

	@DefaultMessage("Error / manager")
	String applicationMailTypeAppErrorVoAdmin();

	@DefaultMessage("Invitation / user")
	String applicationMailTypeInvitationUser();

	/* ========== OWNER OBJECT ============ */

	@DefaultMessage("administrative")
	String ownerTypeAdministrative();

	@DefaultMessage("technical")
	String ownerTypeTechnical();

}