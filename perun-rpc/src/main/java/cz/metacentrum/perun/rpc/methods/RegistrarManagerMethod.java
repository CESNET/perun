package cz.metacentrum.perun.rpc.methods;

import java.util.*;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.registrar.model.*;
import cz.metacentrum.perun.registrar.model.Application.AppType;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

public enum RegistrarManagerMethod implements ManagerMethod {

	/*#
	 * Retrieves all necessary data about VO under registrar session.
	 *
	 * @param voShortName String VO's shortname to get info about
	 * @return List<Attribute> List of VO attributes
	 */
	/*#
	 * Retrieves all necessary data about VO and Group under registrar session.
	 *
	 * @param voShortName String VO's shortname to get info about
	 * @param groupName String Group's full name (including groups structure) to get info about
	 * @return List<Attribute> List of VO attributes
	 */
	initialize {

		@Override
		public List<Attribute> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("group")) {
				return ac.getRegistrarManager().initialize(parms.readString("vo"), parms.readString("group"));
			} else {
				return ac.getRegistrarManager().initialize(parms.readString("vo"), null);
			}

		}

	},

	/*#
	 * Sends invitation email to user which is not member of VO
	 *
	 * @param userId int ID of user to send invitation to
	 * @param voId int ID of VO to send invitation into
	 */
	/*#
	 * Sends invitation email to user which is not member of Group
	 *
	 * If user is not even member of VO, invitation link targets
	 * VO application form fist, after submission, Group application form is displayed.
	 *
	 * @param userId int ID of user to send invitation to
	 * @param voId int ID of VO to send invitation into
	 * @param groupId int ID of Group to send invitation into
	 */
	/*#
	 * Sends invitation email to user which is not member of VO
	 *
	 * @param voId int ID of VO to send invitation into
	 * @param name String name of person used in invitation email (optional)
	 * @param email String email address to send invitation to
	 * @param language String preferred language to use
	 */
	/*#
	 * Sends invitation email to user which is not member of VO and Group
	 *
	 * Invitation link targets VO application form fist, after submission,
	 * Group application form is displayed.
	 *
	 * @param voId int ID of VO to send invitation into
	 * @param groupId int ID of Group to send invitation into
	 * @param name String name of person used in invitation email (optional)
	 * @param email String email address to send invitation to
	 * @param language String preferred language to use
	 */
	sendInvitation {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("userId")) {
				if (parms.contains("groupId")) {
					ac.getRegistrarManager().getMailManager().sendInvitation(ac.getSession(),
							ac.getVoById(parms.readInt("voId")),
							ac.getGroupById(parms.readInt("groupId")),
							ac.getUserById(parms.readInt("userId")));
				} else {
					ac.getRegistrarManager().getMailManager().sendInvitation(ac.getSession(),
							ac.getVoById(parms.readInt("voId")),
							null,
							ac.getUserById(parms.readInt("userId")));
				}
			} else {
				if (parms.contains("groupId")) {
					ac.getRegistrarManager().getMailManager().sendInvitation(ac.getSession(),
							ac.getVoById(parms.readInt("voId")),
							ac.getGroupById(parms.readInt("groupId")),
							(parms.contains("name")) ? parms.readString("name") : null,
							parms.readString("email"),
							parms.readString("language"));
				} else {
					ac.getRegistrarManager().getMailManager().sendInvitation(ac.getSession(),
							ac.getVoById(parms.readInt("voId")),
							null,
							(parms.contains("name")) ? parms.readString("name") : null,
							parms.readString("email"),
							parms.readString("language"));
				}
			}

			return null;

		}

	},
	/*#
	 * Re-send mail notification for existing application. Message of specified type is sent only,
	 * when application is in expected state related to the notification.
	 *
	 * Note, that some data related to processing application are not available (e.g. list of exceptions
	 * during approval), since this method doesn't perform any action with Application itself.
	 *
	 * Perun admin can send any notification except USER_INVITE type.
	 * @see #sendInvitation() for this.
	 *
	 * @param mailType MailType type of mail notification
	 * @param appId int ID of application to send notification for
	 * @param reason String you can specify reason for case: mailType == APP_REJECTED_USER
	 *
	 * @throw RegistrarException if notification can't be sent
	 */
	sendMessage{

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.readString("mailType").equals("APP_REJECTED_USER")) {

				ac.getRegistrarManager().getMailManager().sendMessage(ac.getSession(),
						ac.getApplicationById(parms.readInt("appId")),
						ApplicationMail.MailType.valueOf(parms.readString("mailType")),
						parms.readString("reason"));

			} else {

				ac.getRegistrarManager().getMailManager().sendMessage(ac.getSession(),
						ac.getApplicationById(parms.readInt("appId")),
						ApplicationMail.MailType.valueOf(parms.readString("mailType")),
						null);

			}

			return null;

		}

	},
	/*#
	 * Create application form for a VO.
	 *
	 * @param vo int VO ID
	 * @return Object Always returned null
	 */
	/*#
	 * Create application form for a group.
	 *
	 * @param group int Group ID
	 * @return Object Always returned null
	 */
	createApplicationForm {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("vo")) {
				ac.getRegistrarManager().createApplicationFormInVo(ac.getSession(), ac.getVoById(parms.readInt("vo")));
			} else if (parms.contains("group")) {
				Group g = ac.getGroupById(parms.readInt("group"));
				ac.getRegistrarManager().createApplicationFormInGroup(ac.getSession(), g);
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or group");
			}

			return null;
		}
	},

	/*#
	 * Gets an application form for a given VO.
	 * There is exactly one form for membership per VO, one form is used for both initial registration and annual account expansion,
	 * just the form items are marked whether the should be present in one, the other, or both types of application.
	 *
	 * @param vo int VO ID
	 * @return ApplicationForm Registration form description
	 */
	/*#
	 * Gets an application form for a given Group.
	 * There is exactly one form for membership per Group, one form is used for both initial registration and annual account expansion,
	 * just the form items are marked whether the should be present in one, the other, or both types of application.
	 *
	 * @param group int Group ID
	 * @return ApplicationForm Registration form description
	 */
	getApplicationForm {

		@Override
		public ApplicationForm call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("vo")) {
				return ac.getRegistrarManager().getFormForVo(ac.getVoById(parms.readInt("vo")));
			} else if (parms.contains("group")) {
				return ac.getRegistrarManager().getFormForGroup(ac.getGroupById(parms.readInt("group")));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or group");
			}

		}
	},

	/*#
	 * Gets all items in VO application form.
	 *
	 * @param vo int VO ID
	 * @return List<ApplicationFormItem> All form items regardless of type
	 */
	/*#
	 * Gets items of specified type in VO application form, for initital registration or extension of account.
	 *
	 * @param vo int VO ID
	 * @param type String Application type: INITIAL or EXTENSION
	 * @return List<ApplicationFormItem> Items of specified type
	 */
	/*#
	 * Gets all items in Group application form.
	 *
	 * @param group int Group ID
	 * @return List<ApplicationFormItem> All form items regardless of type
	 */
	/*#
	 * Gets items of specified type in Group application form, for initital registration or extension of account.
	 *
	 * @param group int Group ID
	 * @param type String Application type: INITIAL or EXTENSION
	 * @return List<ApplicationFormItem> Items of specified type
	 */
	getFormItems {

		@Override
		public List<ApplicationFormItem> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("vo")) {
				if (parms.contains("type")) {
					return ac.getRegistrarManager().getFormItems(ac.getSession(),
							ac.getRegistrarManager().getFormForVo(ac.getVoById(parms.readInt("vo"))),
							AppType.valueOf(parms.readString("type")));
				} else {
					return ac.getRegistrarManager().getFormItems(ac.getSession(),
							ac.getRegistrarManager().getFormForVo(ac.getVoById(parms.readInt("vo"))));
				}
			} else if ( parms.contains("group")) {

				if (parms.contains("type")) {
					return ac.getRegistrarManager().getFormItems(ac.getSession(),
							ac.getRegistrarManager().getFormForGroup(ac.getGroupById(parms.readInt("group"))),
							AppType.valueOf(parms.readString("type")));
				} else {
					return ac.getRegistrarManager().getFormItems(ac.getSession(),
							ac.getRegistrarManager().getFormForGroup(ac.getGroupById(parms.readInt("group"))));
				}
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or group");
			}

		}
	},

	/*#
	 * Updates form items sent in list.
	 *
	 * @param vo int VO ID
	 * @param items List<ApplicationFormItem> Application form items
	 * @return int Number of updated items
	 */
	/*#
	 * Updates form items sent in list.
	 *
	 * @param group int Group ID
	 * @param items List<ApplicationFormItem> Application form items
	 * @return int Number of updated items
	 */
	updateFormItems {

		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("vo")) {
				return ac.getRegistrarManager().updateFormItems(ac.getSession(),
						ac.getRegistrarManager().getFormForVo(ac.getVoById(parms.readInt("vo"))),
						parms.readList("items",ApplicationFormItem.class));
			} else if (parms.contains("group")) {
				return ac.getRegistrarManager().updateFormItems(ac.getSession(),
						ac.getRegistrarManager().getFormForGroup(ac.getGroupById(parms.readInt("group"))),
						parms.readList("items",ApplicationFormItem.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or group");
			}

		}
	},

	/*#
	 * Updates the form attributes, not the form items.
	 * - update automatic approval style
	 * - update module_name
	 *
	 * @param form ApplicationForm Application form JSON object
	 * @return ApplicationForm Updated application form or null when update failed
	 */
	updateForm {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ApplicationForm form = parms.read("form", ApplicationForm.class);
			int result = ac.getRegistrarManager().updateForm(ac.getSession(), form);
			if (result==1) {
				return form;
			} else {
				return null;
			}
		}
	},

	/*#
	 * Gets the content for an application form for a given type of application and user.
	 * The values are prefilled from database for extension applications, and always from federation values
	 * taken from the user argument.
	 *
	 * @param vo int VO ID
	 * @param type String Application type: INITIAL or EXTENSION
	 * @return List<ApplicationFormItemWithPrefilledValue> Form items
	 */
	/*#
	 * Gets the content for an application form for a given type of application and user.
	 * The values are prefilled from database for extension applications, and always from federation values
	 * taken from the user argument.
	 *
	 * @param group int Group ID
	 * @param type String Application type: INITIAL or EXTENSION
	 * @return List<ApplicationFormItemWithPrefilledValue> Form items
	 */
	getFormItemsWithPrefilledValues {

		@Override
		public List<ApplicationFormItemWithPrefilledValue> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("vo")) {
				return ac.getRegistrarManager().getFormItemsWithPrefilledValues(
						ac.getSession(),
						Application.AppType.valueOf(parms.readString("type")),
						ac.getRegistrarManager().getFormForVo(ac.getVoById(parms.readInt("vo"))));
			} else if (parms.contains("group")) {
				return ac.getRegistrarManager().getFormItemsWithPrefilledValues(
						ac.getSession(),
						Application.AppType.valueOf(parms.readString("type")),
						ac.getRegistrarManager().getFormForGroup(ac.getGroupById(parms.readInt("group"))));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or group");
			}

		}
	},

	/*#
	 * Gets all applications for a given VO.
	 *
	 * @param vo int VO ID
	 * @return List<Application> Found applications
	 */
	/*#
	 * Gets all applications in a given state for a given VO.
	 *
	 * @param vo int VO ID
	 * @param state String State: NEW, VERIFIED, APPROVED, REJECTED
	 * @return List<Application> Found applications
	 */
	getApplicationsForVo {

		@Override
		public List<Application> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("state[]")) {
				return ac.getRegistrarManager().getApplicationsForVo(ac.getSession(), ac.getVoById(parms.readInt("vo")), parms.readList("state", String.class));
			} else {
				return ac.getRegistrarManager().getApplicationsForVo(ac.getSession(), ac.getVoById(parms.readInt("vo")), null);
			}

		}

	},

	/*#
	 * Gets all applications for a given Group.
	 *
	 * @param group int Group ID
	 * @return List<Application> Found applications
	 */
	/*#
	 * Gets all applications for a given Group.
	 *
	 * @param group int Group ID
	 * @param state String State: NEW, VERIFIED, APPROVED, REJECTED
	 * @return List<Application> Found applications
	 */
	getApplicationsForGroup {

		@Override
		public List<Application> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("state[]")) {
				return ac.getRegistrarManager().getApplicationsForGroup(ac.getSession(), ac.getGroupById(parms.readInt("group")), parms.readList("state", String.class));
			} else {
				return ac.getRegistrarManager().getApplicationsForGroup(ac.getSession(), ac.getGroupById(parms.readInt("group")), null);
			}

		}

	},

	/*#
	 * Gets all applications for the current user.
	 *
	 * @return List<Application> Found applications
	 */
	/*#
	 * Gets all applications for a specific user.
	 *
	 * @param id int User ID
	 * @return List<Application> Found applications
	 */
	getApplicationsForUser {

		@Override
		public List<Application> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("id")) {
				return ac.getRegistrarManager().getApplicationsForUser(ac.getUserById(parms.readInt("id")));
			} else {
				return ac.getRegistrarManager().getApplicationsForUser(ac.getSession());
			}
		}

	},

	/*#
	 * Gets all applications for member
	 *
	 * @param member int ID of member to get applications for
	 * @return List<Application> Found applications
	 */
	/*#
	 * Gets all applications for member
	 *
	 * @param member int ID of member to get applications for
	 * @param group int ID of group to filter applications for
	 * @return List<Application> Found applications
	 */
	getApplicationsForMember {

		@Override
		public List<Application> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("group")) {
				return ac.getRegistrarManager().getApplicationsForMember(ac.getSession(), ac.getGroupById(parms.readInt("group")), ac.getMemberById(parms.readInt("member")));
			} else {
				return ac.getRegistrarManager().getApplicationsForMember(ac.getSession(), null, ac.getMemberById(parms.readInt("member")));
			}
		}

	},

	/*#
	 * Returns application object by its ID.
	 *
	 * @param id int Application ID
	 * @return Application Found application
	 */
	getApplicationById {

		@Override
		public Application call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getRegistrarManager().getApplicationById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Returns data submitted by user in given application (by id).
	 *
	 * @param id int Application ID
	 * @return List<ApplicationFormItemData> Form data
	 */
	getApplicationDataById {

		@Override
		public List<ApplicationFormItemData> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getRegistrarManager().getApplicationDataById(ac.getSession(), parms.readInt("id"));

		}

	},

	/*#
	 * Creates a new application.
	 * The method triggers approval for VOs with auto-approved applications.
	 *
	 * @param app Application Application JSON object
	 * @param data List<ApplicationFormItemData> List of ApplicationFormItemData JSON objects
	 * @return List<ApplicationFormItemData> Stored app data
	 */
	createApplication {

		@Override
		public List<ApplicationFormItemData> call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			Application app = parms.read("app", Application.class);
			List<ApplicationFormItemData> data = parms.readList("data", ApplicationFormItemData.class);

			return ac.getRegistrarManager().createApplication(ac.getSession(), app, data);

		}

	},

	/*#
	 * Deletes an application.
	 *
	 * @param id int Application ID
	 */
	deleteApplication {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			Application app = ac.getRegistrarManager().getApplicationById(ac.getSession(), parms.readInt("id"));
			ac.getRegistrarManager().deleteApplication(ac.getSession(), app);
			return null;

		}

	},

	/*#
	 * Manually approves an application.
	 * Expected to be called as a result of direct VO administrator action in the web UI.
	 *
	 * @param id int Application ID
	 * @return Application Approved application
	 */
	approveApplication {

		@Override
		public Application call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getRegistrarManager().approveApplication(ac.getSession(), parms.readInt("id"));

		}

	},

	/*#
	 * Manually rejects an application.
	 * Expected to be called as a result of direct VO administrator action in the web UI.
	 *
	 * @param id int Application ID
	 * @return Application Rejected application
	 */
	/*#
	 * Manually rejects an application with a reason.
	 * Expected to be called as a result of direct VO administrator action in the web UI.
	 *
	 * @param id int Application ID
	 * @param reason String Reason description
	 * @return Application Rejected application
	 */
	rejectApplication {

		@Override
		public Application call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("reason")) {
				return ac.getRegistrarManager().rejectApplication(ac.getSession(), parms.readInt("id"), parms.readString("reason"));
			} else {
				return ac.getRegistrarManager().rejectApplication(ac.getSession(), parms.readInt("id"), null);
			}

		}

	},

	/*#
	 * Forcefully marks application as verified
	 * (only when application was in NEW state)
	 *
	 * @param id int Application ID
	 * @return Application Verified application
	 */
	verifyApplication {

		@Override
		public Application call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getRegistrarManager().verifyApplication(ac.getSession(), parms.readInt("id"));

		}

	},

	/*#
	 * Validates an email.
	 *
	 * This method should receive all URL parameters from a URL sent by an email to validate
	 * the email address that was provided by a user. The parameters describe the user, application, email and contain
	 * a message authentication code to prevent spoofing.
	 *
	 * @param m String Parameter m
	 * @param i String Parameter i
	 * @return bool True for validated, false for non-valid
	 */
	validateEmail {

		@Override
		public Boolean call(ApiCaller ac, Deserializer parms) throws PerunException {

			Map<String, String> validationData = new HashMap<String, String>();
			// read necessary params
			validationData.put("m", parms.readString("m"));
			validationData.put("i", parms.readString("i"));

			return ac.getRegistrarManager().validateEmailFromLink(validationData);

		}

	},

	/*#
	 * Adds a new item to a form.
	 *
	 * @param vo int VO ID
	 * @param item ApplicationFormItem ApplicationFormItem JSON object
	 * @return ApplicationFormItem Added ApplicationFormItem object
	 */
	/*#
	 * Adds a new item to a form.
	 *
	 * @param group int Group ID
	 * @param item ApplicationFormItem ApplicationFormItem JSON object
	 * @return ApplicationFormItem Added ApplicationFormItem object
	 */
	addFormItem {

		@Override
		public ApplicationFormItem call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("vo")) {
				return ac.getRegistrarManager().addFormItem(ac.getSession(),
						ac.getRegistrarManager().getFormForVo(ac.getVoById(parms.readInt("vo"))),
						parms.read("item", ApplicationFormItem.class));
			} else if (parms.contains("group")) {
				return ac.getRegistrarManager().addFormItem(ac.getSession(),
						ac.getRegistrarManager().getFormForGroup(ac.getGroupById(parms.readInt("group"))),
						parms.read("item", ApplicationFormItem.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or group");
			}

		}

	},

	/*#
	 * Removes a form item permanently.
	 * The user data associated with it remains in the database, it just loses the foreign key
	 * reference which becomes null.
	 *
	 * @param vo int VO ID
	 * @param ordnum int Item's ordnum
	 * @return Object Always null
	 */
	/*#
	 * Removes a form item permanently.
	 * The user data associated with it remains in the database, it just loses the foreign key
	 * reference which becomes null.
	 *
	 * @param group int Group ID
	 * @param ordnum int Item's ordnum
	 * @return Object Always null
	 */
	deleteFormItem {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("vo")) {
				ac.getRegistrarManager().deleteFormItem(ac.getSession(),
						ac.getRegistrarManager().getFormForVo(ac.getVoById(parms.readInt("vo"))),
						parms.readInt("ordnum"));
			} else if (parms.contains("group")) {
				ac.getRegistrarManager().deleteFormItem(ac.getSession(),
						ac.getRegistrarManager().getFormForGroup(ac.getGroupById(parms.readInt("group"))),
						parms.readInt("ordnum"));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or group");
			}

			return null;

		}

	},

	/*#
	 * Copy all form items from selected VO into another.
	 *
	 * @param fromVo int Source VO ID
	 * @param toVo int Destination VO ID
	 * @return Object Always null
	 */
		/*#
	 * Copy all form items from selected Group into another.
	 *
	 * @param fromGroup int Source Group ID
	 * @param toGroup int Destination Group ID
	 * @return Object Always null
	 */
		/*#
	 * Copy all form items from selected VO into Group.
	 *
	 * @param fromVO int Source VO ID
	 * @param toGroup int Destination Group ID
	 * @return Object Always null
	 */
		/*#
	 * Copy all form items from selected Group into VO.
	 *
	 * @param fromGroup int Source Group ID
	 * @param toVO int Destination VO ID
	 * @return Object Always null
	 */
	copyForm {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("fromVo")) {

				if (parms.contains("toVo")) {

					ac.getRegistrarManager().copyFormFromVoToVo(ac.getSession(),
							ac.getVoById(parms.readInt("fromVo")),
							ac.getVoById(parms.readInt("toVo")));

				} else if (parms.contains("toGroup")) {

					ac.getRegistrarManager().copyFormFromVoToGroup(ac.getSession(),
							ac.getVoById(parms.readInt("fromVo")),
							ac.getGroupById(parms.readInt("toGroup")), false);

				}

			} else if (parms.contains("fromGroup")) {

				if (parms.contains("toGroup")) {

					ac.getRegistrarManager().copyFormFromGroupToGroup(ac.getSession(),
							ac.getGroupById(parms.readInt("fromGroup")),
							ac.getGroupById(parms.readInt("toGroup")));

				} else if (parms.contains("toVo")) {

					ac.getRegistrarManager().copyFormFromVoToGroup(ac.getSession(),
							ac.getVoById(parms.readInt("toVo")),
							ac.getGroupById(parms.readInt("fromGroup")), true);

				}

			}

			return null;

		}

	},

	/*#
	 * Copy all e-mail notifications from selected VO into another.
	 *
	 * @param fromVo int Source VO ID
	 * @param toVo int Destination VO ID
	 * @return Object Always null
	 */
	/*#
	 * Copy all e-mail notifications from selected Group into another.
	 *
	 * @param fromGroup int Source Group ID
	 * @param toGroup int Destination Group ID
	 * @return Object Always null
	 */
		/*#
	 * Copy all e-mail notifications from selected VO into Group.
	 *
	 * @param fromVo int Source VO ID
	 * @param toGroup int Destination Group ID
	 * @return Object Always null
	 */
		/*#
	 * Copy all e-mail notifications from selected Group into VO.
	 *
	 * @param fromGroup int Source Group ID
	 * @param toVO int Destination VO ID
	 * @return Object Always null
	 */
	copyMails {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("fromVo")) {

				if (parms.contains("toVo")) {

					ac.getRegistrarManager().getMailManager().copyMailsFromVoToVo(ac.getSession(),
							ac.getVoById(parms.readInt("fromVo")),
							ac.getVoById(parms.readInt("toVo")));

				} else if (parms.contains("toGroup")) {

					ac.getRegistrarManager().getMailManager().copyMailsFromVoToGroup(ac.getSession(),
							ac.getVoById(parms.readInt("fromVo")),
							ac.getGroupById(parms.readInt("toGroup")), false);

				}

			} else if (parms.contains("fromGroup")) {

				if (parms.contains("toGroup")) {

					ac.getRegistrarManager().getMailManager().copyMailsFromGroupToGroup(ac.getSession(),
							ac.getGroupById(parms.readInt("fromGroup")),
							ac.getGroupById(parms.readInt("toGroup")));

				} else if (parms.contains("toVo")) {

					ac.getRegistrarManager().getMailManager().copyMailsFromVoToGroup(ac.getSession(),
							ac.getVoById(parms.readInt("toVo")),
							ac.getGroupById(parms.readInt("fromGroup")), true);

				}

			}

			return null;

		}

	},

	/*#
	 * Returns all mail notifications related to specific app form.
	 *
	 * @param vo int VO ID
	 * @return List<ApplicationMail> Application mails
	 */
	/*#
	 * Returns all mail notifications related to specific app form.
	 *
	 * @param group int Group ID
	 * @return List<ApplicationMail> Application mails
	 */
	getApplicationMails {

		@Override
		public List<ApplicationMail> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("vo")) {
				return ac.getRegistrarManager().getMailManager().getApplicationMails(ac.getSession(),
						ac.getRegistrarManager().getFormForVo(ac.getVoById(parms.readInt("vo"))));
			} else if (parms.contains("group")) {
				return ac.getRegistrarManager().getMailManager().getApplicationMails(ac.getSession(),
						ac.getRegistrarManager().getFormForGroup(ac.getGroupById(parms.readInt("group"))));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or group");
			}

		}

	},

	/*#
	 * Add new mail notification.
	 *
	 * @param vo int VO ID
	 * @param mail ApplicationMail ApplicationMail JSON object
	 * @return ApplicationMail Created ApplicationMail
	 */
	/*#
	 * Add new mail notification.
	 *
	 * @param group int Group ID
	 * @param mail ApplicationMail ApplicationMail JSON object
	 * @return ApplicationMail Created ApplicationMail
	 */
	addApplicationMail {

		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("vo")) {
				return ac.getRegistrarManager().getMailManager().addMail(ac.getSession(),
						ac.getRegistrarManager().getFormForVo(ac.getVoById(parms.readInt("vo"))),
						parms.read("mail", ApplicationMail.class));
			} else if (parms.contains("group")) {
				return ac.getRegistrarManager().getMailManager().addMail(ac.getSession(),
						ac.getRegistrarManager().getFormForGroup(ac.getGroupById(parms.readInt("group"))),
						parms.read("mail", ApplicationMail.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or group");
			}

		}

	},

	/*#
	 * Deletes an e-mail notification from DB based on ID property.
	 *
	 * @param vo int VO ID
	 * @param id int ApplicationMail ID
	 * @return Object Always null
	 */
	/*#
	 * Deletes an e-mail notification from DB based on ID property.
	 *
	 * @param group int Group ID
	 * @param id int ApplicationMail ID
	 * @return Object Always null
	 */
	deleteApplicationMail {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("vo")) {
				ac.getRegistrarManager().getMailManager().deleteMailById(ac.getSession(),
						ac.getRegistrarManager().getFormForVo(ac.getVoById(parms.readInt("vo"))),
						parms.readInt("id"));
			} else if (parms.contains("group")) {
				ac.getRegistrarManager().getMailManager().deleteMailById(ac.getSession(),
						ac.getRegistrarManager().getFormForGroup(ac.getGroupById(parms.readInt("group"))),
						parms.readInt("id"));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or group");
			}

			return null;
		}

	},

	/*#
	 * Updates an e-mail notification.
	 *
	 * @param mail ApplicationMail ApplicationMail JSON object
	 * @return Object Always null
	 */
	updateApplicationMail {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			ac.getRegistrarManager().getMailManager().updateMailById(ac.getSession(), parms.read("mail", ApplicationMail.class));
			return null;
		}

	},

	/*#
	 * Return mail definition including texts by ID.
	 *
	 * @param id int ApplicationMail ID
	 * @return ApplicationMail ApplicationMail object
	 */
	getApplicationMailById {

		@Override
		public ApplicationMail call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getRegistrarManager().getMailManager().getMailById(ac.getSession(), parms.readInt("id"));
		}

	},

	/*#
	 * Enable or disable sending for list of mail definitions.
	 *
	 * @param mails List<ApplicationMail> Mail definitions to update
	 * @param enabled int 1 for enabled, 0 for disabled
	 * @return Object Always null
	 */
	setSendingEnabled {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getRegistrarManager().getMailManager().setSendingEnabled(ac.getSession(),
					parms.readList("mails", ApplicationMail.class),
					parms.readInt("enabled")==1 ? true : false);

			return null;
		}

	},

	/*#
	 *	Verify Captcha answer.
	 *
	 *	@param challenge String Captcha challenge
	 *	@param response String User response
	 *	@return bool True if it is valid, False if failed
	 */
	verifyCaptcha {

		@Override
		public Boolean call(ApiCaller ac, Deserializer parms) throws PerunException {

			ReCaptchaImpl reCaptcha = new ReCaptchaImpl();

			reCaptcha.setPrivateKey(Utils.getPropertyFromConfiguration("perun.recaptcha.privatekey"));
			reCaptcha.setRecaptchaServer(ReCaptchaImpl.HTTPS_SERVER);

			// we don't need caller's address since our key is global
			String remoteAddress = "";
			ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddress, parms.readString("challenge"), parms.readString("response"));

			if (reCaptchaResponse.isValid()) {
				return true;
			} else {
				return false;
			}



		}

	},

	/*#
	 * Check if new application may belong to another user in Perun
	 * (but same person in real life).
	 *
	 * Return list of similar identities (by external identity, name or email).
	 *
	 * Returned identities contain also organization, email and external identities.
	 *
	 * @param appId int ID of application to check for
	 * @return List<Identity> List of found similar identities.
	 */
	/*#
	 * Check if new application may belong to another user in Perun
	 * (but same person in real life).
	 *
	 * IMPORTANT: This check is performed only on latest application of specified vo/group and type which belongs
	 * to logged in user/identity.
	 *
	 * Return list of similar identities (by external identity, name or email).
	 *
	 * Returned identities contain also organization, email and external identities.
	 *
	 * @param voId int Vo to get application for
	 * @param groupId int Group to get application for
	 * @param type String Application type
	 *
	 * @return List<Identity> List of found similar identities.
	 */
	checkForSimilarUsers {

		@Override
		public List<Identity> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("appId")) {
				return ac.getRegistrarManager().getConsolidatorManager().checkForSimilarUsers(ac.getSession(), parms.readInt("appId"));
			} else if (parms.contains("voId")) {
				return ac.getRegistrarManager().getConsolidatorManager().checkForSimilarUsers(ac.getSession(),
						ac.getVoById(parms.readInt("voId")),
						(parms.readInt("groupId") != 0) ? ac.getGroupById(parms.readInt("groupId")) : null,
						AppType.valueOf(parms.readString("type")) );
			} else {
				return ac.getRegistrarManager().getConsolidatorManager().checkForSimilarUsers(ac.getSession());
			}

		}

	};

}
