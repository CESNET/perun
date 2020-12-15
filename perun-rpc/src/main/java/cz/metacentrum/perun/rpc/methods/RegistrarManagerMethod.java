package cz.metacentrum.perun.rpc.methods;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.*;
import cz.metacentrum.perun.registrar.model.Application.AppType;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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

	initializeRegistrar {

		@Override
		public Map<String, Object> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("group")) {
				return ac.getRegistrarManager().initRegistrar(ac.getSession(), parms.readString("vo"), parms.readString("group"));
			} else {
				return ac.getRegistrarManager().initRegistrar(ac.getSession(), parms.readString("vo"), null);
			}

		}

	},

	/*#
	 * Sends invitation email to user which is not member of VO
	 *
	 * @param userId int <code>id</code> of user to send invitation to
	 * @param voId int <code>id</code> of VO to send invitation into
	 */
	/*#
	 * Sends invitation email to user which is not member of Group
	 *
	 * If user is not even member of VO, invitation link targets
	 * VO application form fist, after submission, Group application form is displayed.
	 *
	 * @param userId int <code>id</code> of user to send invitation to
	 * @param voId int <code>id</code> of VO to send invitation into
	 * @param groupId int <code>id</code> of Group to send invitation into
	 */
	/*#
	 * Sends invitation email to user which is not member of VO
	 *
	 * @param voId int <code>id</code> of VO to send invitation into
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
	 * @param voId int <code>id</code> of VO to send invitation into
	 * @param groupId int <code>id</code> of Group to send invitation into
	 * @param name String name of person used in invitation email (optional)
	 * @param email String email address to send invitation to
	 * @param language String preferred language to use
	 */
	sendInvitation {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

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
	 * Perun admin can send any notification except USER_INVITE type, see #sendInvitation() for this.
	 *
	 * @param mailType MailType type of mail notification
	 * @param appId int <code>id</code> of application to send notification for
	 * @param reason String you can specify reason for case: mailType == APP_REJECTED_USER
	 *
	 * @throws RegistrarException if notification can't be sent
	 */
	sendMessage{

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

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
	 * @param vo int VO <code>id</code>
	 * @return Object Always returned null
	 * @exampleParam vo 1
	 */
	/*#
	 * Create application form for a group.
	 *
	 * @param group int Group <code>id</code>
	 * @return Object Always returned null
	 * @exampleParam group 1
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
	 * @param vo int VO <code>id</code>
	 * @return ApplicationForm Registration form description
	 */
	/*#
	 * Gets an application form for a given Group.
	 * There is exactly one form for membership per Group, one form is used for both initial registration and annual account expansion,
	 * just the form items are marked whether the should be present in one, the other, or both types of application.
	 *
	 * @param group int Group <code>id</code>
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
	 * @param vo int VO <code>id</code>
	 * @return List<ApplicationFormItem> All form items regardless of type
	 */
	/*#
	 * Gets items of specified type in VO application form, for initital registration or extension of account.
	 *
	 * @param vo int VO <code>id</code>
	 * @param type String Application type: INITIAL or EXTENSION
	 * @return List<ApplicationFormItem> Items of specified type
	 */
	/*#
	 * Gets all items in Group application form.
	 *
	 * @param group int Group <code>id</code>
	 * @return List<ApplicationFormItem> All form items regardless of type
	 */
	/*#
	 * Gets items of specified type in Group application form, for initital registration or extension of account.
	 *
	 * @param group int Group <code>id</code>
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
	 * @param vo int VO <code>id</code>
	 * @param items List<ApplicationFormItem> Application form items
	 * @return int Number of updated items
	 */
	/*#
	 * Updates form items sent in list.
	 *
	 * @param group int Group <code>id</code>
	 * @param items List<ApplicationFormItem> Application form items
	 * @return int Number of updated items
	 */
	updateFormItems {

		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

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
			parms.stateChangingCheck();

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
	 * Update label, options, help and error message for specified form item and locale.
	 * FormItem is specified by its ID.
	 *
	 * @param formItem ApplicationFormItem Form item to update
	 * @param locale String Locale specified like: cs, en, ...
	 */
	/*#
	 * Replace label, options, help and error message for specified form item and all locales by current value.
	 * FormItem is specified by its ID.
	 *
	 * @param formItem ApplicationFormItem Form item to update
	 */
	updateFormItemTexts {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ApplicationFormItem item = parms.read("formItem", ApplicationFormItem.class);
			if (parms.contains("locale")) {
				ac.getRegistrarManager().updateFormItemTexts(ac.getSession(), item, new Locale(parms.readString("locale")));
			} else {
				ac.getRegistrarManager().updateFormItemTexts(ac.getSession(), item);
			}
			return null;
		}
	},

	/*#
	 * Return form item by its ID, you must be authorized to manipulate the form.
	 *
	 * @param id int ID of application form item
	 */
	getFormItemById {
		@Override
		public ApplicationFormItem call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getRegistrarManager().getFormItemById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Gets the content for an application form for a given type of application and user.
	 * The values are prefilled from database for extension applications, and always from federation values
	 * taken from the user argument.
	 *
	 * @param vo int VO <code>id</code>
	 * @param type String Application type: INITIAL or EXTENSION
	 * @return List<ApplicationFormItemWithPrefilledValue> Form items
	 */
	/*#
	 * Gets the content for an application form for a given type of application and user.
	 * The values are prefilled from database for extension applications, and always from federation values
	 * taken from the user argument.
	 *
	 * @param group int Group <code>id</code>
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
	 * @param vo int VO <code>id</code>
	 * @return List<Application> Found applications
	 */
	/*#
	 * Gets all applications in a given state for a given VO.
	 *
	 * @param vo int VO <code>id</code>
	 * @param state List<String> List of states: NEW, VERIFIED, APPROVED, REJECTED
	 * @return List<Application> Found applications
	 */
	/*#
	 * Gets all applications in a given state for a given VO in a given date period.
	 *
	 * @param vo int VO <code>id</code>
	 * @param state List<String> List of states: NEW, VERIFIED, APPROVED, REJECTED
	 * @param dateFrom String Earliest date for applications
	 * @param dateTo String Latest date for applications
	 * @return List<Application> Found applications
	 */
	getApplicationsForVo {

		@Override
		public List<Application> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("dateFrom") || parms.contains("dateTo")) {
					return ac.getRegistrarManager().getApplicationsForVo(
							ac.getSession(), 
							ac.getVoById(parms.readInt("vo")), 
							parms.contains("state") ? parms.readList("state", String.class) : null, 
							parms.readLocalDate("dateFrom"), 
							parms.readLocalDate("dateTo")
							);
			} else {
				return ac.getRegistrarManager().getApplicationsForVo(
						ac.getSession(), 
						ac.getVoById(parms.readInt("vo")), 
						parms.contains("state") ? parms.readList("state", String.class) : null
						);
			}

		}

	},

	/*#
	 * Gets all applications for a given Group.
	 *
	 * @param group int Group <code>id</code>
	 * @return List<Application> Found applications
	 */
	/*#
	 * Gets all applications in a given state for a given Group.
	 *
	 * @param group int Group <code>id</code>
	 * @param state List<String> List of states: NEW, VERIFIED, APPROVED, REJECTED
	 * @return List<Application> Found applications
	 */
	/*#
	 * Gets all applications in a given state for a given Group in a given date period.
	 *
	 * @param group int Group <code>id</code>
	 * @param state List<String> List of states: NEW, VERIFIED, APPROVED, REJECTED
	 * @param dateFrom String Earliest date for applications
	 * @param dateTo String Latest date for applications
	 * @return List<Application> Found applications
	 */
	getApplicationsForGroup {

		@Override
		public List<Application> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("dateFrom") || parms.contains("dateTo")) {
				return ac.getRegistrarManager().getApplicationsForGroup(
						ac.getSession(), 
						ac.getGroupById(parms.readInt("group")), 
						parms.contains("state") ? parms.readList("state", String.class) : null,
						parms.readLocalDate("dateFrom"), 
						parms.readLocalDate("dateTo")
						);
			} else {
				return ac.getRegistrarManager().getApplicationsForGroup(
						ac.getSession(), 
						ac.getGroupById(parms.readInt("group")), 
						parms.contains("state") ? parms.readList("state", String.class) : null
						);
			}

		}

	},

	/*#
	 * Gets all applications for the current user
	 * based on authz and internal user ID.
	 *
	 * @return List<Application> Found applications
	 */
	/*#
	 * Gets all applications for a specific user
	 * by user ID. Ignores session data.
	 *
	 * @param id int User <code>id</code>
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
	 * @param member int <code>id</code> of member to get applications for
	 * @return List<Application> Found applications
	 */
	/*#
	 * Gets all applications for member
	 *
	 * @param member int <code>id</code> of member to get applications for
	 * @param group int <code>id</code> of group to filter applications for
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
	 * Returns application object by its <code>id</code>.
	 *
	 * @param id int Application <code>id</code>
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
	 * @param id int Application <code>id</code>
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
	 * @deprecated See submitApplication()
	 * @param app Application Application JSON object
	 * @param data List<ApplicationFormItemData> List of ApplicationFormItemData JSON objects
	 * @return List<ApplicationFormItemData> Stored app data
	 */
	createApplication {

		@Override
		public List<ApplicationFormItemData> call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			Application app = parms.read("app", Application.class);
			List<ApplicationFormItemData> data = parms.readList("data", ApplicationFormItemData.class);

			return ac.getRegistrarManager().createApplication(ac.getSession(), app, data);

		}

	},

	/*#
	 * Creates a new application.
	 * The method triggers approval for VOs with auto-approved applications.
	 *
	 * @param app Application Application JSON object
	 * @param data List<ApplicationFormItemData> List of ApplicationFormItemData JSON objects
	 * @return Application Submitted application
	 */
	submitApplication {

		@Override
		public Application call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			Application app = parms.read("app", Application.class);
			List<ApplicationFormItemData> data = parms.readList("data", ApplicationFormItemData.class);

			return ac.getRegistrarManager().submitApplication(ac.getSession(), app, data);

		}

	},

	/*#
	 * Deletes an application.
	 *
	 * @param id int Application <code>id</code>
	 */
	deleteApplication {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			Application app = ac.getRegistrarManager().getApplicationById(ac.getSession(), parms.readInt("id"));
			ac.getRegistrarManager().deleteApplication(ac.getSession(), app);
			return null;

		}

	},

	/*#
	 * Manually approves an application.
	 * Expected to be called as a result of direct VO administrator action in the web UI.
	 *
	 * @param id int Application <code>id</code>
	 * @return Application Approved application
	 */
	approveApplication {

		@Override
		public Application call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			return ac.getRegistrarManager().approveApplication(ac.getSession(), parms.readInt("id"));

		}

	},

	/*#
	 * Check if application can be approved based on form module rules. Throws exception if not.
	 * Expected to be called from Web UI before actual approving happens, so VO admin
	 * can override this default behavior.
	 *
	 * @param id int Application <code>id</code>
	 * @throw CantBeApprovedException When application can't be approved based on form module rules.
	 */
	canBeApproved {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getRegistrarManager().canBeApproved(ac.getSession(), ac.getApplicationById(parms.readInt("id")));
			return null;
		}

	},

	/*#
	 * Manually rejects an application.
	 * Expected to be called as a result of direct VO administrator action in the web UI.
	 *
	 * @param id int Application <code>id</code>
	 * @return Application Rejected application
	 */
	/*#
	 * Manually rejects an application with a reason.
	 * Expected to be called as a result of direct VO administrator action in the web UI.
	 *
	 * @param id int Application <code>id</code>
	 * @param reason String Reason description
	 * @return Application Rejected application
	 */
	rejectApplication {

		@Override
		public Application call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

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
	 * @param id int Application <code>id</code>
	 * @return Application Verified application
	 */
	verifyApplication {

		@Override
		public Application call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

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
	 * @return boolean True for validated, false for non-valid
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
	 * @param vo int VO <code>id</code>
	 * @param item ApplicationFormItem ApplicationFormItem JSON object
	 * @return ApplicationFormItem Added ApplicationFormItem object
	 */
	/*#
	 * Adds a new item to a form.
	 *
	 * @param group int Group <code>id</code>
	 * @param item ApplicationFormItem ApplicationFormItem JSON object
	 * @return ApplicationFormItem Added ApplicationFormItem object
	 */
	addFormItem {

		@Override
		public ApplicationFormItem call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

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
	 * @param vo int VO <code>id</code>
	 * @param ordnum int Item's ordnum
	 * @return Object Always null
	 */
	/*#
	 * Removes a form item permanently.
	 * The user data associated with it remains in the database, it just loses the foreign key
	 * reference which becomes null.
	 *
	 * @param group int Group <code>id</code>
	 * @param ordnum int Item's ordnum
	 * @return Object Always null
	 */
	deleteFormItem {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

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
	 * @param fromVo int Source VO <code>id</code>
	 * @param toVo int Destination VO <code>id</code>
	 * @return Object Always null
	 */
	/*#
	 * Copy all form items from selected Group into another.
	 *
	 * @param fromGroup int Source Group <code>id</code>
	 * @param toGroup int Destination Group <code>id</code>
	 * @return Object Always null
	 */
		/*#
	 * Copy all form items from selected VO into Group.
	 *
	 * @param fromVO int Source VO <code>id</code>
	 * @param toGroup int Destination Group <code>id</code>
	 * @return Object Always null
	 */
		/*#
	 * Copy all form items from selected Group into VO.
	 *
	 * @param fromGroup int Source Group <code>id</code>
	 * @param toVO int Destination VO <code>id</code>
	 * @return Object Always null
	 */
	copyForm {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

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
	 * @param fromVo int Source VO <code>id</code>
	 * @param toVo int Destination VO <code>id</code>
	 * @return Object Always null
	 */
	/*#
	 * Copy all e-mail notifications from selected Group into another.
	 *
	 * @param fromGroup int Source Group <code>id</code>
	 * @param toGroup int Destination Group <code>id</code>
	 * @return Object Always null
	 */
		/*#
	 * Copy all e-mail notifications from selected VO into Group.
	 *
	 * @param fromVo int Source VO <code>id</code>
	 * @param toGroup int Destination Group <code>id</code>
	 * @return Object Always null
	 */
		/*#
	 * Copy all e-mail notifications from selected Group into VO.
	 *
	 * @param fromGroup int Source Group <code>id</code>
	 * @param toVO int Destination VO <code>id</code>
	 * @return Object Always null
	 */
	copyMails {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

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
	 * @param vo int VO <code>id</code>
	 * @return List<ApplicationMail> Application mails
	 */
	/*#
	 * Returns all mail notifications related to specific app form.
	 *
	 * @param group int Group <code>id</code>
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
	 * @param vo int VO <code>id</code>
	 * @param mail ApplicationMail ApplicationMail JSON object
	 * @return ApplicationMailId int
	 */
	/*#
	 * Add new mail notification.
	 *
	 * @param group int Group <code>id</code>
	 * @param mail ApplicationMail ApplicationMail JSON object
	 * @return ApplicationMailId int
	 */
	addApplicationMail {

		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

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
	 * Deletes an e-mail notification from DB based on <code>id</code> property.
	 *
	 * @param vo int VO <code>id</code>
	 * @param id int ApplicationMail <code>id</code>
	 * @return Object Always null
	 */
	/*#
	 * Deletes an e-mail notification from DB based on <code>id</code> property.
	 *
	 * @param group int Group <code>id</code>
	 * @param id int ApplicationMail <code>id</code>
	 * @return Object Always null
	 */
	deleteApplicationMail {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

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
			parms.stateChangingCheck();
			ac.getRegistrarManager().getMailManager().updateMailById(ac.getSession(), parms.read("mail", ApplicationMail.class));
			return null;
		}

	},

	/*#
	 * Return mail definition including texts by <code>id</code>.
	 *
	 * @param id int ApplicationMail <code>id</code>
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
	 * @param enabled boolean true for enabled, false for disabled
	 * @return Object Always null
	 */
	setSendingEnabled {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getRegistrarManager().getMailManager().setSendingEnabled(ac.getSession(),
					parms.readList("mails", ApplicationMail.class),
					parms.readBoolean("enabled"));

			return null;
		}

	},

	/*#
	 *	Verify Captcha answer.
	 *
	 *	@param response String User response
	 *	@return boolean True if it is valid, False if failed
	 */
	verifyCaptcha {

		@Override
		public Boolean call(ApiCaller ac, Deserializer parms) throws PerunException {

			String secret = BeansUtils.getCoreConfig().getRecaptchaPrivateKey();
			String response = parms.readString("response");

			RestTemplate restTemplate = new RestTemplate();

			final String verificationUrl = "https://www.google.com/recaptcha/api/siteverify";

			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			map.add("response", response);
			map.add("secret", secret);

			return restTemplate.postForObject(verificationUrl, map , JsonNode.class).path("success").asBoolean();
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
	 * @param appId int <code>id</code> of application to check for
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
	/*#
	 * Check for similar users by name and email in session (authz) information
	 *
	 * @return List<Identity> List of found similar identities.
	 */
	/*#
	 * Check if newly inserted form data may connect anonymous person to existing user.
	 * Return list of similar users (by identity, name or email).
	 * Returned users contain also organization and preferredMail attribute.
	 *
	 * @param formItems List<ApplicationFormItemData> List of application form items with data
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
			} else if (parms.contains("formItems")) {
				return ac.getRegistrarManager().getConsolidatorManager().checkForSimilarUsers(ac.getSession(),
						parms.readList("formItems", ApplicationFormItemData.class));
			} else {
				return ac.getRegistrarManager().getConsolidatorManager().checkForSimilarUsers(ac.getSession());
			}

		}

	},

	/*#
	 * Get time-limited token proving user identity in external source (for now 3 minutes). It can be used
	 * to join user identity with another by calling consolidateIdentityUsingToken() method
	 * and passing the token. Please note, that different authz (identity) must be used to perform both calls.
	 *
	 * @return String Token to be used for joining identities.
	 */
	getConsolidatorToken {

		@Override
		public String call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getRegistrarManager().getConsolidatorManager().getConsolidatorToken(ac.getSession());

		}

	},

	/*#
	 * Join current user identity (authz) with the one previously provided and referenced by the token.
	 *
	 * @param token String Token to be used for joining identities.
	 * @return List<UserExtSource> List of user identities know to Perun after joining.
	 * @throw IdentityUnknownException When neither current or previous identity is associated with a user in Perun.
	 * @throw IdentityIsSameException User used same identity (authz) to get token and to request joining.
	 * @throw IdentitiesAlreadyJoinedException Both identities used in a process belong to the same user in Perun (already joined).
	 * @throw IdentityAlreadyInUseException Both identities used in a process are associated with different users. In order to join two user accounts contact support.
	 */
	consolidateIdentityUsingToken {

		@Override
		public List<UserExtSource> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getRegistrarManager().getConsolidatorManager().consolidateIdentityUsingToken(ac.getSession(), parms.readString("token"));

		}

	},

	/*#
	 * Update data of specific application form item, which was originally submitted by the user.
	 * Only PerunAdmin can use this. Only applications in NEW or VERIFIED state can have form items updated.
	 * Form items of types: FROM_FEDERATION_HIDDEN, FROM_FEDERATION_SHOW, USERNAME, PASSWORD, HEADING, HTML_COMMENT,
	 * SUBMIT_BUTTON and AUTO_SUBMIT_BUTTON are not updatable by this method.
	 *
	 * @param appId int ID of Application this data belongs to.
	 * @param data ApplicationFormItemData Form item data to be updated by its ID.
	 * @deprecated Only for old GUI, see updateFormItemsData(appId, data)
	 */
	updateFormItemData {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();
			ac.getRegistrarManager().updateFormItemData(ac.getSession(), parms.readInt("appId"), parms.read("data", ApplicationFormItemData.class));
			return null;
		}

	},

	/*#
	 * Update data of application form items, which were originally submitted by the user.
	 * Only user who submitted the application can use this. Only applications in NEW or VERIFIED state can have form items updated.
	 * Form items of types: FROM_FEDERATION_HIDDEN, FROM_FEDERATION_SHOW, USERNAME, PASSWORD, HEADING, HTML_COMMENT,
	 * SUBMIT_BUTTON and AUTO_SUBMIT_BUTTON are not updatable by this method.
	 *
	 * If VALIDATED_EMAIL is changed to non-verified value, it will change application state from VERIFIED to NEW
	 * and trigger new verification and auto approval (if possible).
	 *
	 * @param appId int ID of Application this data belongs to.
	 * @param data List<ApplicationFormItemData> Form items data to be updated by their IDs.
	 */
	updateFormItemsData {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();
			ac.getRegistrarManager().updateFormItemsData(ac.getSession(), parms.readInt("appId"), parms.readList("data", ApplicationFormItemData.class));
			return null;
		}

	};

}
