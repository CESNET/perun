package cz.metacentrum.perun.ldapc.processor.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.metacentrum.perun.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.ldapc.beans.LdapOperation;
import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.AuditerConsumer;
import cz.metacentrum.perun.ldapc.beans.LdapProperties;
import cz.metacentrum.perun.ldapc.processor.EventProcessor;
import cz.metacentrum.perun.ldapc.processor.LdapConnector;
import cz.metacentrum.perun.ldapc.service.LdapcManager;
import cz.metacentrum.perun.rpclib.Rpc;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.directory.Attribute;

@org.springframework.stereotype.Service(value = "eventProcessor")
public class EventProcessorImpl implements Runnable {

	//Autowired variables
	@Autowired
	private LdapConnector ldapConnector;
	@Autowired
	private LdapcManager ldapcManager;
	@Autowired
	private LdapProperties ldapProperties;

	//Other variables
	private AuditerConsumer auditerConsumer;
	private final static Logger log = LoggerFactory.getLogger(EventProcessorImpl.class);
	private boolean running = false;

	//All parsable and useable objects
	Resource resource;
	Member member;
	Group group;
	Group parentGroup;
	Vo vo;
	User user;
	User specificUser;
	Facility facility;
	cz.metacentrum.perun.core.api.Attribute attribute;
	AttributeDefinition attributeDef;
	UserExtSource userExtSource;

	//PATTERNS (used for searching in messages)
	//Common patterns
	private Pattern deletedPattern = Pattern.compile(" deleted.$");
	private Pattern createdPattern = Pattern.compile(" created.$");
	private Pattern updatedPattern = Pattern.compile(" updated.$");
	//Resources patterns
	private Pattern deletedResourcePattern = Pattern.compile(" deleted.#Facility");
	//Groups patterns
	private Pattern newGroupPattern = Pattern.compile(" created in Vo:\\[(.*)\\]", Pattern.DOTALL);
	private Pattern subGroupPattern = Pattern.compile(" created in Vo:\\[(.*)\\] as subgroup of Group:\\[(.*)\\]", Pattern.DOTALL);
	private Pattern assignGroupToResourcePattern = Pattern.compile("^Group:\\[(.*)\\] assigned to Resource:\\[(.*)\\]", Pattern.DOTALL);
	private Pattern removeGroupFromResourcePattern = Pattern.compile("^Group:\\[(.*)\\] removed from Resource:\\[(.*)\\]", Pattern.DOTALL);
	private Pattern moveGroupPattern = Pattern.compile("^Group:\\[(.*)\\] was moved\\.$", Pattern.DOTALL);
	//Members patterns
	private Pattern addedToPattern = Pattern.compile(" added to Group:\\[(.*)\\]", Pattern.DOTALL);
	private Pattern totallyRemovedFromPatter = Pattern.compile(" was removed from Group:\\[(.*)\\] totally", Pattern.DOTALL);
	private Pattern validatedPattern = Pattern.compile(" validated.$");
	private Pattern otherStateOfMemberPattern = Pattern.compile("expired.$|disabled.$|invalidated.$|suspended #");
	//Attributes patterns
	private Pattern facilitySetPattern = Pattern.compile(" set for Facility:\\[(.*)\\]", Pattern.DOTALL);
	private Pattern facilityRemovePattern = Pattern.compile(" removed for Facility:\\[(.*)\\]", Pattern.DOTALL);
	private Pattern facilityAllAttrsRemovedPattern = Pattern.compile("^All attributes removed for Facility:\\[(.*)\\]", Pattern.DOTALL);

	private Pattern userSetPattern = Pattern.compile(" set for User:\\[(.*)\\]", Pattern.DOTALL);
	private Pattern userRemovePattern = Pattern.compile(" removed for User:\\[(.*)\\]", Pattern.DOTALL);
	private Pattern userAllAttrsRemovedPattern = Pattern.compile("^All attributes removed for User:\\[(.*)\\]", Pattern.DOTALL);

	private Pattern userUidNamespacePattern = Pattern.compile(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace:");
	private Pattern userLoginNamespacePattern = Pattern.compile(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:");
	//UserExtSources patterns
	private Pattern addUserExtSourcePattern = Pattern.compile("^UserExtSource:\\[(.*)\\] added to User:\\[(.*)\\]", Pattern.DOTALL);
	private Pattern removeUserExtSourcePattern = Pattern.compile("^UserExtSource:\\[(.*)\\] removed from User:\\[(.*)\\]", Pattern.DOTALL);

	//CONSTANTS
	private static final String LDAP_NAME = "ldap";
	private static final String PATH_TO_ERROR_FILE = "./errorParseMessageForLDAP.log";
	private static final String PATH_TO_NOT_EXECUTED_ERROR_FILE = "./notExecutedMessages.log";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	//PERUN ATTRIBUTES NAMES
	public static final String perunAttrPreferredMail = "preferredMail";
	public static final String perunAttrMail = "mail";
	public static final String perunAttrOrganization = "organization";
	public static final String perunAttrPhone = "phone";
	public static final String perunAttrUserCertDNs = "userCertDNs";
	public static final String perunAttrBonaFideStatus = "elixirBonaFideStatus";
	public static final String perunAttrSchacHomeOrganizations = "schacHomeOrganizations";
	public static final String perunAttrEduPersonScopedAffiliations = "eduPersonScopedAffiliations";
	public static final String perunAttrLibraryIDs = "libraryIDs";
	public static final String perunAttrEntityID = "entityID";
	public static final String perunAttrClientID = "OIDCClientID";
	public static final String perunAttrGroupNames = "groupNames";
	public static final String perunAttrInstitutionsCountries = "institutionsCountries";

	//LDAP ATTRIBUTES NAMES
	public static final String ldapAttrAssignedToResourceId = "assignedToResourceId";
	public static final String ldapAttrAssignedGroupId = "assignedGroupId";
	public static final String ldapAttrDescription = "description";
	public static final String ldapAttrCommonName = "cn";
	public static final String ldapAttrPerunUniqueGroupName= "perunUniqueGroupName";
	public static final String ldapAttrEduPersonPrincipalNames = "eduPersonPrincipalNames";
	public static final String ldapAttrPreferredMail = perunAttrPreferredMail;
	public static final String ldapAttrMail = perunAttrMail;
	public static final String ldapAttrOrganization = "o";
	public static final String ldapAttrTelephoneNumber = "telephoneNumber";
	public static final String ldapAttrUserCertDNs = "userCertificateSubject";
	public static final String ldapAttrBonaFideStatus = "bonaFideStatus";
	public static final String ldapAttrSchacHomeOrganizations = perunAttrSchacHomeOrganizations;
	public static final String ldapAttrEduPersonScopedAffiliations = perunAttrEduPersonScopedAffiliations;
	public static final String ldapAttrLibraryIDs = perunAttrLibraryIDs;
	public static final String ldapAttrUidNumber = "uidNumber;x-ns-";
	public static final String ldapAttrLogin = "login;x-ns-";
	public static final String ldapAttrUserPassword = "userPassword";
	public static final String ldapAttrSurname = "sn";
	public static final String ldapAttrGivenName = "givenName";
	public static final String ldapAttrEntityID = perunAttrEntityID;
	public static final String ldapAttrClientID = perunAttrClientID;
	public static final String ldapAttrObjectClass = "objectClass";
	public static final String ldapAttrPerunVoId = "perunVoId";
	public static final String ldapAttrPerunFacilityId = "perunFacilityId";
	public static final String ldapAttrPerunUserId = "perunUserId";
	public static final String ldapAttrPerunGroupId = "perunGroupId";
	public static final String ldapAttrPerunResourceId = "perunResourceId";
	public static final String ldapAttrPerunParentGroup = "perunParentGroup";
	public static final String ldapAttrPerunParentGroupId = "perunParentGroupId";
	public static final String ldapAttrMemberOf = "memberOf";
	public static final String ldapAttrUniqueMember = "uniqueMember";
	public static final String ldapAttrMemberOfPerunVo = "memberOfPerunVo";
	public static final String ldapAttrEntryStatus = "entryStatus";
	public static final String ldapAttrIsServiceUser = "isServiceUser";
	public static final String ldapAttrIsSponsoredUser = "isSponsoredUser";
	public static final String ldapAttrGroupNames = perunAttrGroupNames;
	public static final String ldapAttrInstitutionsCountries = perunAttrInstitutionsCountries;

	//LDAP OBJECT CLASSES
	public static final String objectClassTop = "top";
	public static final String objectClassPerunResource = "perunResource";
	public static final String objectClassPerunGroup = "perunGroup";
	public static final String objectClassOrganization = "organization";
	public static final String objectClassPerunVO = "perunVO";
	public static final String objectClassPerson = "person";
	public static final String objectClassOrganizationalPerson = "organizationalPerson";
	public static final String objectClassInetOrgPerson = "inetOrgPerson";
	public static final String objectClassPerunUser = "perunUser";
	public static final String objectClassTenOperEntry = "tenOperEntry";
	public static final String objectClassInetUser = "inetUser";

	//LDAP ORGANIZATION UNITS
	public static final String organizationalUnitPeople = "ou=People";

	/**
	 * This method waiting for new messages in AuditLog (using AuditerConsumer)
	 * and then call method resolveMessage or catch exceptions and log data to files.
	 *
	 * @throws InterruptedException if thread is interrupted
	 * @throws Exception if some other exception like InternalErrorException are thrown
	 *
	 */
	public void run() {

		if(!ldapProperties.propsLoaded()) throw new RuntimeException("LdapcProperties is not autowired correctly!");

		//Get instance of auditerConsumer and set runnig to true

		running = true;
		Integer lastProcessedIdNumber = 0;
		AuditMessage message = new AuditMessage(0, "Empty", null, null, null);
		List<AuditMessage> messages;

		try {
			//If running is true, then this proccess will be continously
			while (running) {

				messages = null;
				int sleepTime = 1000;
				//Waiting for new messages. If consumer failed in some internal case, waiting until it will be repaired (waiting time is increases by each attempt)
				do {
					try {
						//IMPORTANT STEP1: Get new bulk of messages
						messages = Rpc.AuditMessagesManager.pollConsumerMessagesForParser(ldapcManager.getRpcCaller(), ldapProperties.getLdapConsumerName());
					} catch (InternalErrorException ex) {
						log.error("Consumer failed due to {}. Sleeping for {} ms.",ex, sleepTime);
						Thread.sleep(sleepTime);
						sleepTime+=sleepTime;
					}

					//If there are no messages, sleep for 1 sec and then try it again
					if(messages == null) Thread.sleep(1000);
				} while(messages == null);
				//If new messages exist, resolve them all
				Iterator<AuditMessage> messagesIterator = messages.iterator();
				while(messagesIterator.hasNext()) {
					message = messagesIterator.next();
					messagesIterator.remove();
					//Warning when two consecutive messages are separated by more than 15 ids
					if(lastProcessedIdNumber > 0 && lastProcessedIdNumber < message.getId()) {
						if((message.getId() - lastProcessedIdNumber) > 15) log.debug("SKIP FLAG WARNING: lastProcessedIdNumber: " + lastProcessedIdNumber + " - newMessageNumber: " + message.getId() + " = " + (lastProcessedIdNumber - message.getId()));
					}
					lastProcessedIdNumber = message.getId();
					//IMPORTANT STEP2: Resolve next message
					this.resolveMessage(message.getMsg(), message.getId());
				}
				//After all messages has been resolved, test interrupting of thread and if its ok, wait and go for another bulk of messages
				if (Thread.interrupted()) {
					running = false;
				} else {
					Thread.sleep(5000);
				}
			}
			//If ldapc is interrupted
		} catch (InterruptedException e) {
			Date date = new Date();
			log.error("Last message has ID='" + message.getId()+ "' and was INTERRUPTED at " + DATE_FORMAT.format(date) + " due to interrupting.");
			running = false;
			Thread.currentThread().interrupt();
			//If some other exception is thrown
		} catch (Exception e) {
			Date date = new Date();
			log.error("Last message has ID='" + message.getId() + "' and was bad PARSED or EXECUTE at " + DATE_FORMAT.format(date) + " due to exception " + e.toString());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get a message and id of this message.
	 * Parse the message and decide which way will be further processed.
	 * Using patterns and objects to choose the way.
	 *
	 * Additional Information:
	 * -> For user and serviceUser there is the same behavior.
	 * -> If there is only serviceUser (not serviceUser and user) the behavior for serviceUser is the same like for user (in LDAP)
	 * -> If there are 2 groups in one message, expecting the first is subGroup and second is parentGroup
	 *
	 * Possible ways (first and only 1 possible way with the lowest number is choose):
	 * -> 1) GROUP and MEMBER exist
	 *   -> 1.1) if member status is valid => add member to group in LDAP
	 *   -> 1.2) if member was totally removed from group (totally means there is no direct or indirect existence of member in this group yet)
	 *           => remove member from this group in LDAP
	 * -> 2) GROUP and PARENT_GROUP exist
	 *   -> 2.1) if there is message with adding subgroup => add group like subgroup of parentGroup in LDAP
	 * -> 3) GROUP AND RESOURCE exist
	 *   -> 3.1) if there is message with adding group to resource => add resource to group (like attribute) in LDAP
	 *   -> 3.2) if there is message with removing group from resource => remove resource from group (like attribute) in LDAP
	 * -> 4) only RESOURCE exists (resource must be before group because of
	 *   -> 4.1) if there is message with deleting resource => delete this resource from LDAP
	 *   -> 4.2) if there is message with createing resource => create this resource in LDAP
	 *   -> 4.3) if there is message with updating resource => update this resource in LDAP
	 * -> 5) only GROUP exists
	 *   -> 5.1) if there is message with deleting group => delete this group from LDAP
	 *   -> 5.2) if there is message with creating group => create this group in LDAP
	 *   -> 5.3) if there is message with updating group => update this group in LDAP
	 *   -> 5.4) if there is message with moving group in the structure => modify existing group to the new state
	 * -> 6) only MEMBER exists (RPC CALLING used)
	 *   -> 6.1) if there is message with changing of member state to valid => add member to all groups in LDAP where he needs to be
	 *   -> 6.2) if there is message with changing of member state to other than valid => remove member from all groups in LDAP where is needed
	 * -> 7) only VO exists
	 *   -> 7.1) if there is message with deleting vo => delete this vo from LDAP
	 *   -> 7.2) if there is message with creating vo => create this vo in LDAP
	 *   -> 7.3) if there is message with updating vo => update this vo in LDAP
	 * -> 8) USER and USER_EXT_SOURCE exist
	 *   -> 8.1) if there is message with adding userExtSource (IDP) to user => create or update attribute of user in LDAP
	 *   -> 8.2) if there is message with removing userExtSource (IDP) from user => remove or update attribute of user in LDAP
	 * -> 9) USER and ATTRIBUTE exist
	 *   -> 9.1) if there is message with setting attribute to user => set Attribute to user in LDAP
	 * -> 10) USER and ATTRIBUTE_DEFINITION exist
	 *   -> 10.1) if there is message with removing attribute from user => remove Attribute from user in LDAP
	 * -> 11) only USER exists
	 *   -> 11.1) if there is message with deleting user => delete user from LDAP
	 *   -> 11.2) if there is message with creating user => create user in LDAP
	 *   -> 11.3) if there is message with updating user => update user in LDAP
	 *   -> 11.4) if there is message with removing all attribute from user => remove all attributes from user in LDAP (only removeable attributes)
	 * -> 12) FACILITY and ATTRIBUTE exist
	 *   -> 12.1) if there is message with setting attribute to facility => set Attribute to resources (assigned to facility) in LDAP
	 * -> 13) FACILITY and ATTRIBUTE_DEF exist
	 *   -> 13.1) if there is message with removing attribute from facility => remove Attribute from resources (assigned to facility) in LDAP
	 * -> 14) in all other cases
	 *   -> 14.1) always => only log some information
	 *
	 * @param msg message which need to be parse and resolve
	 * @param idOfMessage id of paring/resolving message
	 *
	 * @throws InternalErrorException when some internal error in core occurs
	 */
	protected void resolveMessage(String msg, Integer idOfMessage) throws InternalErrorException {

		List<PerunBean> listOfBeans;
		listOfBeans = AuditParser.parseLog(msg);

		//Debug information to check parsing of message.
		if(!listOfBeans.isEmpty()){
			int i=0;
			for(PerunBean p: listOfBeans) {
				i++;
				if(p!=null) log.debug("There is object number " + i + ") " + p.serializeToString());
				else log.debug("There is unknow object which is null");
			}
		}

		//Fill perunBeans
		emptyAndFillPerunBeans(listOfBeans);

		//Log debug data for looking in messages
		log.debug("MessageNumber=" + idOfMessage + " -- OBJECTS: " + this.member + '/' + this.group + '/' + this.facility + "/" + this.parentGroup + '/' + this.vo + '/'
				+ this.resource + '/' + this.user + '/' + this.attribute + '/' + this.attributeDef + '/' + this.userExtSource);

		//If specific user is the only one user in message, so behavior will be same for him like for any other user!
		if(this.specificUser != null && this.user == null) this.user = this.specificUser;

		//------------------------------------------------------------------
		//-----------------OPERATIONS ON FILLED OBJECTS---------------------
		//------------------------------------------------------------------
		//Choose first possible solution for existing objects.

		// 1) IF GROUP AND MEMBER WERE FOUND, TRY TO WORK WITH GROUP-MEMBER SPECIFIC OPERATIONS
		if(this.group != null && this.member != null) {
			// 1.1) ONLY FOR VALID MEMBER WE ADD HIM TO THE GROUP IN LDAP
			if(this.member.getStatus().equals(Status.VALID)) {
				Matcher addedTo = addedToPattern.matcher(msg);

				if(addedTo.find()) {
					if(!ldapConnector.isAlreadyMember(this.member, this.group)) ldapConnector.addMemberToGroup(this.member, this.group);
				}
			}
			// 1.2) MEMBER WILL BE REMOVED FROM GROUP
			Matcher totallyRemovedFrom = totallyRemovedFromPatter.matcher(msg);

			if(totallyRemovedFrom.find()) {
				if(ldapConnector.isAlreadyMember(this.member, this.group)) ldapConnector.removeMemberFromGroup(this.member, this.group);
			}

			// 2) IF 2 GROUPS WERE FOUND, TRY TO WORK WITH PARENT_GROUP-SUBGROUP SPECIFIC OPERATIONS
		} else if(this.group != null && this.parentGroup != null) {
			Matcher newSubGroup = subGroupPattern.matcher(msg);

			// 2.1) ADD GROUP AS SUBGROUP TO PARENT_GROUP
			if(newSubGroup.find()) {
				ldapConnector.addGroupAsSubGroup(this.group, this.parentGroup);
			}

			// 3) IF GROUP AND RESOURCE WERE FOUND, TRY TO WORK WITH GROUP-RESOURCE SPECIFIC OPERATIONS
		} else if(this.group != null && this.resource != null) {
			Matcher assigned = assignGroupToResourcePattern.matcher(msg);
			Matcher removed = removeGroupFromResourcePattern.matcher(msg);

			// 3.1) ADD NEW RESOURCE FOR GROUP IN LDAP
			if(assigned.find()) {
				updateGroupAttribute(ldapAttrAssignedToResourceId, String.valueOf(this.resource.getId()), LdapOperation.ADD_ATTRIBUTE, this.group);
				updateResourceAttribute(ldapAttrAssignedGroupId, String.valueOf(this.group.getId()), LdapOperation.ADD_ATTRIBUTE, this.resource);
				// 3.2) REMOVE RESOURCE FROM GROUP IN LDAP
			} else if(removed.find()) {
				updateGroupAttribute(ldapAttrAssignedToResourceId, String.valueOf(this.resource.getId()), LdapOperation.REMOVE_ATTRIBUTE, this.group);
				updateResourceAttribute(ldapAttrAssignedGroupId, String.valueOf(this.group.getId()), LdapOperation.REMOVE_ATTRIBUTE, this.resource);
			}
			// 4) IF ONLY RESOURCE WERE FOUND, TRY TO WORK WITH RESOURCE SPECIFIC OPERATIONS
		} else if(this.resource != null) {
			Matcher deleted = deletedResourcePattern.matcher(msg);
			Matcher created = createdPattern.matcher(msg);
			Matcher updated = updatedPattern.matcher(msg);

			// 4.1) RESOURCE WILL BE DELETED
			if(deleted.find()) {
				ldapConnector.deleteResource(resource);
				// 4.2) RESOURCE WILL BE CREATED
			} else if(created.find()) {
				ldapConnector.createResource(resource, getFacilityEntityIdValue(resource.getFacilityId()));
				// 4.3) RESOURCE WILL BE UPDATED
			} else if(updated.find()) {
				Map<LdapOperation, List<Pair<String,String>>> attributes = new HashMap<>();
				List<Pair<String,String>> replaceList = new ArrayList<>();
				replaceList.add(new Pair(ldapAttrCommonName,this.resource.getName()));
				if(this.resource.getDescription() != null && !this.resource.getDescription().isEmpty()) replaceList.add(new Pair(ldapAttrDescription, this.resource.getDescription()));
				attributes.put(LdapOperation.REPLACE_ATTRIBUTE, replaceList);
				updateResourceAttributes(attributes, this.resource);
			}
			// 5) IF ONLY GROUP WERE FOUND, TRY TO WORK WITH GROUP SPECIFIC OPERATIONS
		} else if(this.group != null) {
			Matcher deleted = deletedPattern.matcher(msg);
			Matcher newGroup = newGroupPattern.matcher(msg);
			Matcher updated = updatedPattern.matcher(msg);
			Matcher moveGroup = moveGroupPattern.matcher(msg);

			if(deleted.find()){
				// 5.1) GROUP WILL BE DELETED
				ldapConnector.removeGroup(this.group);
			} else if(newGroup.find()) {
				// 5.2) GROUP WILL BE CREATED
				ldapConnector.addGroup(this.group);
			} else if(updated.find()) {
				// 5.3) GROUP WILL BE UPDATED
				updateGroup(group);
			} else if(moveGroup.find()) {
				// 5.4) MOVE GROUP IN STRUCTURE
				moveGroup(group);
			}

			// 6) IF MEMBER WAS FOUND, TRY TO WORK WITH MEMBER SPECIFIC OPERATIONS (! RPC CALLING used there !)
		} else if(this.member != null) {
			Matcher validated = validatedPattern.matcher(msg);
			Matcher otherStateOfMember = otherStateOfMemberPattern.matcher(msg);

			// 6.1) MEMBER WAS VALIDATED, NEED TO ADD HIM TO ALL GROUPS
			if(validated.find()) {
				List<Group> memberGroups = new ArrayList<Group>();
				try {
					memberGroups = Rpc.GroupsManager.getAllMemberGroups(ldapcManager.getRpcCaller(), this.member);
				} catch (MemberNotExistsException e) {
					//IMPORTANT this is not problem, if member not exist, we expected that will be deleted in some message after that, in DB is deleted
				} catch (PrivilegeException e) {
					throw new InternalErrorException("There are no privilegies for getting member's groups", e);
				} catch (InternalErrorException e) {
					throw e;
				}
				for(Group g: memberGroups) {
					if(!ldapConnector.isAlreadyMember(this.member, g)) ldapConnector.addMemberToGroup(this.member, g);
				}
				// 6.2) MEMBER STATE WAS CHANGED TO OTHER STATE THAN VALIDATE
			} else if(otherStateOfMember.find()) {
				List<Group> memberGroups = new ArrayList<Group>();
				try {
					memberGroups = Rpc.GroupsManager.getAllMemberGroups(ldapcManager.getRpcCaller(), this.member);
				} catch (MemberNotExistsException e) {
					//IMPORTATNT this is not problem, if member not exist, we expected that will be deleted in some message after that, in DB is deleted
				} catch (PrivilegeException e) {
					throw new InternalErrorException("There are no privilegies for getting member's groups", e);
				} catch (InternalErrorException e) {
					throw e;
				}
				for(Group g: memberGroups) {
					if(ldapConnector.isAlreadyMember(this.member, g)) ldapConnector.removeMemberFromGroup(this.member, g);
				}
			}

			// 7) IF VO WAS FOUND, TRY TO WORK WITH VO SPECIFIC OPERATIONS
		} else if(this.vo != null) {
			Matcher deleted = deletedPattern.matcher(msg);
			Matcher created = createdPattern.matcher(msg);
			Matcher updated = updatedPattern.matcher(msg);

			// 7.1) VO WILL BE DELETED
			if(deleted.find()) {
				ldapConnector.deleteVo(this.vo);
				// 7.2) VO WILL BE CREATED
			} else if(created.find()) {
				ldapConnector.createVo(this.vo);
				// 7.3) VO WILL BE UPDATED
			} else if(updated.find()) {
				Map<LdapOperation, List<Pair<String,String>>> attributes = new HashMap<LdapOperation, List<Pair<String, String>>>();
				List<Pair<String,String>> replaceList = new ArrayList<Pair<String, String>>();
				replaceList.add(new Pair(ldapAttrDescription,this.vo.getName()));
				attributes.put(LdapOperation.REPLACE_ATTRIBUTE, replaceList);
				updateVoAttributes(attributes, this.vo);
			}

			// 8) IF USER AND USEREXTSOURCE WERE FOUND, TRY TO WORK WITH USER-USEREXTSOURCE SPECIFIC OPERATIONS (LIKE SET EXT LOGINS FOR IDP EXTSOURCES)
		} else if(this.user != null && this.userExtSource != null) {
			Matcher addExtSource = addUserExtSourcePattern.matcher(msg);
			Matcher removeExtSource = removeUserExtSourcePattern.matcher(msg);

			// 8.1) ADD ATTRIBUTE WITH IDP EXTSOURCE
			if(addExtSource.find()) {
				if(this.userExtSource.getExtSource() != null && this.userExtSource.getExtSource().getType() != null) {
					String extLogin;
					if(this.userExtSource.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
						extLogin = this.userExtSource.getLogin();
						if(extLogin == null) extLogin = "";
						updateUserAttribute(ldapAttrEduPersonPrincipalNames, extLogin, LdapOperation.ADD_ATTRIBUTE, user);
					}
				}
				// 8.2) REMOVE ATTRIBUTE WITH IDP EXTSOURCE
			} else if(removeExtSource.find()) {
				if(this.userExtSource.getExtSource() != null && this.userExtSource.getExtSource().getType() != null) {
					String extLogin;
					if(this.userExtSource.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
						extLogin = this.userExtSource.getLogin();
						if(extLogin == null) extLogin = "";
						updateUserAttribute(ldapAttrEduPersonPrincipalNames, extLogin, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}
				}
			}

			// 9) IF USER AND ATTRIBUTE WERE FOUND, TRY TO WORK WITH USER-ATTR SPECIFIC OPERATIONS (LIKE SET USER ATTRIBUTES)
		} else if(this.user != null && this.attribute != null) {
			Matcher set = userSetPattern.matcher(msg);

			// 9.1) SOME USER ATTRIBUTE WILL BE PROBABLY SET (IF IT IS ONE OF SPECIFIC ATTRIBUTES)
			if(set.find()) {
				Matcher uidMatcher = userUidNamespacePattern.matcher(this.attribute.getName());
				Matcher loginMatcher = userLoginNamespacePattern.matcher(this.attribute.getName());

				if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":" + perunAttrPreferredMail)) {
					//this mean change of attribute preferredMail in User
					if(this.attribute.getValue() != null) {
						updateUserAttribute(ldapAttrPreferredMail, (String) this.attribute.getValue(), LdapOperation.REPLACE_ATTRIBUTE, user);
						updateUserAttribute(ldapAttrMail, (String) this.attribute.getValue(), LdapOperation.REPLACE_ATTRIBUTE, user);
					} else {
						if(ldapConnector.userAttributeExist(this.user, ldapAttrPreferredMail)) {
							updateUserAttribute(ldapAttrPreferredMail, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
						}
						if(ldapConnector.userAttributeExist(this.user, ldapAttrMail)) {
							updateUserAttribute(ldapAttrMail, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
						}
					}

				} else if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":" + perunAttrOrganization)) {
					if(this.attribute.getValue() != null) {
						updateUserAttribute(ldapAttrOrganization, (String) attribute.getValue(), LdapOperation.REPLACE_ATTRIBUTE, this.user);
					} else {
						if(ldapConnector.userAttributeExist(this.user, ldapAttrOrganization)) {
							updateUserAttribute(ldapAttrOrganization, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
						}
					}

				} else if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":" + perunAttrPhone)) {
					if(this.attribute.getValue() != null) {
						updateUserAttribute(ldapAttrTelephoneNumber, (String) attribute.getValue(), LdapOperation.REPLACE_ATTRIBUTE, this.user);
					} else {
						if(ldapConnector.userAttributeExist(this.user, ldapAttrTelephoneNumber)) {
							updateUserAttribute(ldapAttrTelephoneNumber, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
						}
					}

				} else if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":" + perunAttrUserCertDNs)) {
					Map<String, String> certDNsMap = (this.attribute.getValue() != null) ? (Map) this.attribute.getValue() : null;

					if (certDNsMap == null || certDNsMap.isEmpty()) {
						if (ldapConnector.userAttributeExist(this.user, ldapAttrUserCertDNs)) {
							updateUserAttribute(ldapAttrUserCertDNs, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
						}
					} else {
						Set<String> certSubjectsWithPrefixes = ((Map) this.attribute.getValue()).keySet();
						Set<String> certSubjectsWithoutPrefixes = new HashSet<>();
						//remove prefixes from certificates
						for (String key : certSubjectsWithPrefixes) {
							certSubjectsWithoutPrefixes.add(key.replaceFirst("^[0-9]+[:]", ""));
						}
						String[] subjectsArray = Arrays.copyOf(certSubjectsWithoutPrefixes.toArray(), certSubjectsWithoutPrefixes.toArray().length, String[].class);
						ldapConnector.updateUsersAttributeInLDAP(String.valueOf(this.user.getId()), ldapAttrUserCertDNs, subjectsArray);
					}

				} else if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":" + perunAttrBonaFideStatus)) {
					if(this.attribute.getValue() != null) {
						updateUserAttribute(ldapAttrBonaFideStatus, (String) attribute.getValue(), LdapOperation.REPLACE_ATTRIBUTE, this.user);
					} else {
						if(ldapConnector.userAttributeExist(this.user, ldapAttrBonaFideStatus)) {
							updateUserAttribute(ldapAttrBonaFideStatus, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
						}
					}

				} else if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":" + perunAttrSchacHomeOrganizations)) {
					updateUserMultivalueAttributeInLDAP((ArrayList) this.attribute.getValue(), this.user, ldapAttrSchacHomeOrganizations);

				} else if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":" + perunAttrEduPersonScopedAffiliations)) {
					updateUserMultivalueAttributeInLDAP((ArrayList) this.attribute.getValue(), this.user, ldapAttrEduPersonScopedAffiliations);

				} else if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":" + perunAttrGroupNames)) {
					updateUserMultivalueAttributeInLDAP((ArrayList) this.attribute.getValue(), this.user, ldapAttrGroupNames);

				} else if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":" + perunAttrInstitutionsCountries)) {
					updateUserMultivalueAttributeInLDAP((ArrayList) this.attribute.getValue(), this.user, ldapAttrInstitutionsCountries);

				} else if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":" + perunAttrLibraryIDs)) {
					updateUserMultivalueAttributeInLDAP((ArrayList) this.attribute.getValue(), this.user, ldapAttrLibraryIDs);

				} else if(uidMatcher.find()) {
					if(this.attribute.getValue() != null) {
						updateUserAttribute(ldapAttrUidNumber + this.attribute.getFriendlyNameParameter(), String.valueOf((Integer) this.attribute.getValue()), LdapOperation.REPLACE_ATTRIBUTE, this.user);
					} else {
						if(ldapConnector.userAttributeExist(this.user, ldapAttrUidNumber + this.attribute.getFriendlyNameParameter())) {
							updateUserAttribute(ldapAttrUidNumber + this.attribute.getFriendlyNameParameter(), null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
						}
					}

				} else if(loginMatcher.find()) {
					if(this.attribute.getValue() != null) {
						updateUserAttribute(ldapAttrLogin + this.attribute.getFriendlyNameParameter(), (String) this.attribute.getValue(), LdapOperation.REPLACE_ATTRIBUTE, this.user);
						//if login is from loginNamespace (eg. EINFRA) (new value), then userPassword must be set or modified
						if(ldapProperties.getLdapLoginNamespace().toLowerCase().equals(this.attribute.getFriendlyNameParameter())) {
							updateUserAttribute(ldapAttrUserPassword, "{SASL}" + this.attribute.getValue() + "@" + ldapProperties.getLdapLoginNamespace(), LdapOperation.REPLACE_ATTRIBUTE, this.user);
						}
					} else {
						if(ldapConnector.userAttributeExist(this.user, ldapAttrLogin + this.attribute.getFriendlyNameParameter())) {
							updateUserAttribute(ldapAttrLogin + this.attribute.getFriendlyNameParameter(), null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
						}
						if(ldapProperties.getLdapLoginNamespace().toLowerCase().equals(this.attribute.getFriendlyNameParameter())) {
							if(ldapConnector.userAttributeExist(this.user, ldapAttrUserPassword)) {
								updateUserAttribute(ldapAttrUserPassword, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
							}
						}
					}
				}
			}

			// 10) IF USER AND ATTRIBTUE DEFINITION WERE FOUND, TRY TO WORK WITH USER-ATTRDEF SPECIFIC OPERATIONS
		} else if(this.user != null && attributeDef != null) {
			Matcher remove = userRemovePattern.matcher(msg);
			// 10.1) REMOVE SPECIFIC USER ATTRIBUTE
			if(remove.find() &&  ldapConnector.userExist(this.user)) {
				Matcher uidMatcher = userUidNamespacePattern.matcher(this.attributeDef.getName());
				Matcher loginMatcher = userLoginNamespacePattern.matcher(this.attributeDef.getName());
				if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":" + perunAttrPreferredMail)) {
					if(ldapConnector.userAttributeExist(this.user, ldapAttrPreferredMail)) {
						updateUserAttribute(ldapAttrPreferredMail, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}
					if(ldapConnector.userAttributeExist(this.user, ldapAttrMail)) {
						updateUserAttribute(ldapAttrMail, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}
					//TODO: organization (user) will not exists

				} else if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":" + perunAttrOrganization)) {
					if(ldapConnector.userAttributeExist(this.user, ldapAttrOrganization)) {
						updateUserAttribute(ldapAttrOrganization, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}

				} else if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":" + perunAttrPhone)) {
					if(ldapConnector.userAttributeExist(this.user, ldapAttrTelephoneNumber)) {
						updateUserAttribute(ldapAttrTelephoneNumber, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}

				} else if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":" + perunAttrUserCertDNs)) {
					if(ldapConnector.userAttributeExist(this.user, ldapAttrUserCertDNs)) {
						updateUserAttribute(ldapAttrUserCertDNs, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}

				} else if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":" + perunAttrEduPersonScopedAffiliations)) {
					if(ldapConnector.userAttributeExist(this.user, ldapAttrEduPersonScopedAffiliations)) {
						updateUserAttribute(ldapAttrEduPersonScopedAffiliations, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}

				} else if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":" + perunAttrGroupNames)) {
					if(ldapConnector.userAttributeExist(this.user, ldapAttrGroupNames)) {
						updateUserAttribute(ldapAttrGroupNames, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}

				} else if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":" + perunAttrInstitutionsCountries)) {
					if(ldapConnector.userAttributeExist(this.user, ldapAttrInstitutionsCountries)) {
						updateUserAttribute(ldapAttrInstitutionsCountries, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}

				} else if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":" + perunAttrBonaFideStatus)) {
					if(ldapConnector.userAttributeExist(this.user, ldapAttrBonaFideStatus)) {
						updateUserAttribute(ldapAttrBonaFideStatus, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}

				} else if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":" + perunAttrSchacHomeOrganizations)) {
					if(ldapConnector.userAttributeExist(this.user, ldapAttrSchacHomeOrganizations)) {
						updateUserAttribute(ldapAttrSchacHomeOrganizations, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}

				} else if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":" + perunAttrLibraryIDs)) {
					if(ldapConnector.userAttributeExist(this.user, ldapAttrLibraryIDs)) {
						updateUserAttribute(ldapAttrLibraryIDs, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}

				} else if(uidMatcher.find()) {
					if(ldapConnector.userAttributeExist(this.user, ldapAttrUidNumber + this.attributeDef.getFriendlyNameParameter())) {
						updateUserAttribute(ldapAttrUidNumber + this.attributeDef.getFriendlyNameParameter(), null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}

				} else if(loginMatcher.find()) {
					if(ldapConnector.userAttributeExist(this.user, ldapAttrLogin + this.attributeDef.getFriendlyNameParameter())) {
						updateUserAttribute(ldapAttrLogin + this.attributeDef.getFriendlyNameParameter(), null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}
					if(ldapProperties.getLdapLoginNamespace().toLowerCase().equals(this.attributeDef.getFriendlyNameParameter())) {
						if(ldapConnector.userPasswordExists(this.user)) {
							updateUserAttribute(ldapAttrUserPassword, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
						}
					}
				}
			}
			// 11) IF ONLY USER WAS FOUND, TRY TO WORK WITH USER SPECIFIC OPERATIONS
		} else if(this.user != null) {
			Matcher deleted = deletedPattern.matcher(msg);
			Matcher created = createdPattern.matcher(msg);
			Matcher updated = updatedPattern.matcher(msg);
			Matcher removedAllAttrs = userAllAttrsRemovedPattern.matcher(msg);
			// 11.1) DELETE USER
			if(deleted.find()) {
				ldapConnector.deleteUser(this.user);
				// 11.2) CREATE USER
			} else if(created.find()) {
				ldapConnector.createUser(this.user);
				// 11.3) UPDATE USER
			} else if(updated.find()) {
				Map<LdapOperation, List<Pair<String,String>>> attributes = new HashMap<LdapOperation, List<Pair<String, String>>>();
				List<Pair<String,String>> replaceList = new ArrayList<Pair<String, String>>();
				String firstName = this.user.getFirstName();
				String lastName = this.user.getLastName();
				String commonName = "";
				if(firstName == null || firstName.isEmpty()) firstName = "";
				else commonName+= firstName + " ";
				if(lastName == null || lastName.isEmpty()) lastName = "N/A";
				commonName+=lastName;
				replaceList.add(new Pair(ldapAttrSurname,lastName));
				replaceList.add(new Pair(ldapAttrCommonName, commonName));
				// IF firstName is empty, maybe need to be removed first
				if(firstName.isEmpty()) {
					//if first name exists and new one is empty, then remove it, else do nothing
					if(ldapConnector.userAttributeExist(this.user, ldapAttrGivenName)) {
						updateUserAttribute(ldapAttrGivenName, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
					}
				} else {
					//if first name is not empty, replace it by new first name
					replaceList.add(new Pair(ldapAttrGivenName, firstName));
				}
				attributes.put(LdapOperation.REPLACE_ATTRIBUTE, replaceList);
				updateUserAttributes(attributes, this.user);
				// 11.4) REMOVE ALL USER ATTRIBUTES
			} else if(removedAllAttrs.find()) {
				if(ldapConnector.userExist(this.user)) {
					Attributes usersAttrs = ldapConnector.getAllUsersAttributes(this.user);
					List<ModificationItem> listOfItems = new ArrayList<ModificationItem>();
					if(usersAttrs != null) {
						NamingEnumeration<? extends Attribute> attributesEnumeration;
						attributesEnumeration = usersAttrs.getAll();
						try {
							while(attributesEnumeration.hasMore()) {
								Attribute attr = attributesEnumeration.nextElement();
								if(attr != null && attr.getID() != null) {
									if(isRemovableUserAttribute(attr.getID())) {
										ModificationItem item = new ModificationItem(LdapOperation.REMOVE_ATTRIBUTE.getCode(), attr);
										listOfItems.add(item);
									}
								}
							}
						} catch (NamingException ex) {
							throw new InternalErrorException("Error at Deleting All Users Attribute, throw namingException.", ex);
						}
					}
					if(!listOfItems.isEmpty()) {
						ModificationItem[] items = Arrays.copyOf(listOfItems.toArray(), listOfItems.toArray().length, ModificationItem[].class);
						ldapConnector.updateUser(this.user, items);
					}
				}
			}

			//12) IF FACILITY AND ATTRIBUTE TO SET WAS FOUND
		} else if(this.facility != null && attribute != null) {
			Matcher set = facilitySetPattern.matcher(msg);

			// 12.1) SOME FACILITY ATTRIBUTE WILL BE PROBABLY SET (IF IT IS ONE OF SPECIFIC ATTRIBUTES)
			if(set.find()) {
				//EntityID WILL BE SET
				if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_FACILITY_ATTR_DEF + ":" + perunAttrEntityID)) {
					try {
						List<Resource> resources = Rpc.FacilitiesManager.getAssignedResources(ldapcManager.getRpcCaller(), this.facility);
						//this mean change of attribute entityID in all assigned resources
						if(this.attribute.getValue() != null) {
							for(Resource res: resources) {
								updateResourceAttribute(ldapAttrEntityID, (String) this.attribute.getValue(), LdapOperation.REPLACE_ATTRIBUTE, res);
							}
						} else {
							for(Resource res: resources) {
								if(ldapConnector.resourceAttributeExist(res, ldapAttrEntityID)) {
									updateResourceAttribute(ldapAttrEntityID, null, LdapOperation.REMOVE_ATTRIBUTE, res);
								}
							}
						}
					} catch (FacilityNotExistsException ex) {
						//this probably means that facility is already removed, so also resources are removed and we just delete them in some other message
						//so skip it just log
						log.debug("Try to get resources from facility, but facility just not exists. Skip it!");
					} catch (PrivilegeException e) {
						throw new InternalErrorException("There are no privilegies for getting all assigned resources of facility" + this.facility, e);
					}
				//OIDCClientID WILL BE SET
				} else if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_FACILITY_ATTR_DEF + ":" + perunAttrClientID)) {
					try {
						List<Resource> resources = Rpc.FacilitiesManager.getAssignedResources(ldapcManager.getRpcCaller(), this.facility);
						//this mean change of attribute clientID in all assigned resources
						if(this.attribute.getValue() != null) {
							for(Resource res: resources) {
								updateResourceAttribute(ldapAttrClientID, (String) this.attribute.getValue(), LdapOperation.REPLACE_ATTRIBUTE, res);
							}
						} else {
							for(Resource res: resources) {
								if(ldapConnector.resourceAttributeExist(res, ldapAttrClientID)) {
									updateResourceAttribute(ldapAttrClientID, null, LdapOperation.REMOVE_ATTRIBUTE, res);
								}
							}
						}
					} catch (FacilityNotExistsException ex) {
						//this probably means that facility is already removed, so also resources are removed and we just delete them in some other message
						//so skip it just log
						log.debug("Try to get resources from facility, but facility just not exists. Skip it!");
					} catch (PrivilegeException e) {
						throw new InternalErrorException("There are no privilegies for getting all assigned resources of facility" + this.facility, e);
					}
				}
			}
			//13) IF FACILITY AND ATTRIBUTE DEF TO REMOVE WAS FOUND
		} else if(this.facility != null && attributeDef != null) {
			Matcher remove = facilityRemovePattern.matcher(msg);
			// 13.1) REMOVE SPECIFIC FACILITY ATTRIBUTE
			if(remove.find()) {
				if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_FACILITY_ATTR_DEF + ":" + perunAttrEntityID)) {
					try {
						List<Resource> resources = Rpc.FacilitiesManager.getAssignedResources(ldapcManager.getRpcCaller(), this.facility);
						for(Resource res: resources) {
							if(ldapConnector.resourceAttributeExist(res, ldapAttrEntityID)) {
								updateResourceAttribute(ldapAttrEntityID, null, LdapOperation.REMOVE_ATTRIBUTE, res);
							}
						}
					} catch (FacilityNotExistsException ex) {
						//this probably means that facility is already removed, so also resources are removed and we just delete them in some other message
						//so skip it just log
						log.debug("Try to get resources from facility, but facility just not exists. Skip it!");
					} catch (PrivilegeException e) {
						throw new InternalErrorException("There are no privilegies for getting all assigned resources of facility" + this.facility, e);
					}
				} else if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_FACILITY_ATTR_DEF + ":" + perunAttrClientID)) {
					try {
						List<Resource> resources = Rpc.FacilitiesManager.getAssignedResources(ldapcManager.getRpcCaller(), this.facility);
						for (Resource res : resources) {
							if (ldapConnector.resourceAttributeExist(res, ldapAttrClientID)) {
								updateResourceAttribute(ldapAttrClientID, null, LdapOperation.REMOVE_ATTRIBUTE, res);
							}
						}
					} catch (FacilityNotExistsException ex) {
						//this probably means that facility is already removed, so also resources are removed and we just delete them in some other message
						//so skip it just log
						log.debug("Try to get resources from facility, but facility just not exists. Skip it!");
					} catch (PrivilegeException e) {
						throw new InternalErrorException("There are no privilegies for getting all assigned resources of facility" + this.facility, e);
					}
				}
			}
		  // 14) IN OTHER CASES
		} else {
			log.debug("Nothing to resolve for message with number : " + idOfMessage);
		}
	}

	/**
	 * Choose if attribute is removable or not.
	 * It means if is this attribute is required and can't be null.
	 *
	 * @param attributeName name of attribute in ldap
	 * @return true if attribute is removable, false if not
	 */
	private boolean isRemovableUserAttribute(String attributeName) {
		List<String> attributesWithoutOption = new ArrayList<String>();
		//Attributes without option like "x-ns-namespace"
		attributesWithoutOption.add(ldapAttrMail);
		attributesWithoutOption.add(ldapAttrPreferredMail);
		attributesWithoutOption.add(ldapAttrOrganization);
		attributesWithoutOption.add(ldapAttrUserCertDNs);
		attributesWithoutOption.add(ldapAttrSchacHomeOrganizations);
		attributesWithoutOption.add(ldapAttrBonaFideStatus);
		attributesWithoutOption.add(ldapAttrEduPersonScopedAffiliations);
		attributesWithoutOption.add(ldapAttrLibraryIDs);
		attributesWithoutOption.add(ldapAttrTelephoneNumber);
		attributesWithoutOption.add(ldapAttrInstitutionsCountries);
		attributesWithoutOption.add(ldapAttrGroupNames);
		attributesWithoutOption.add(ldapAttrMemberOfPerunVo);
		attributesWithoutOption.add(ldapAttrEduPersonPrincipalNames);
		if(attributesWithoutOption.contains(attributeName)) return true;

		//Attributes with option like "x-ns-namespace"
		List<String> attributesWithOption = new ArrayList<String>();
		attributesWithOption.add(ldapAttrUidNumber);
		attributesWithOption.add(ldapAttrLogin);
		attributesWithOption.add(ldapAttrUserPassword);

		for(String s: attributesWithOption) {
			if(attributeName.startsWith(s)) return true;
		}

		return false;
	}

	/**
	 * Get User preferred Mail value from perun.
	 *
	 * @param user the user
	 * @return value of user's preferredMail or null, if value is null or user not exists yet
	 * @throws InternalErrorException if some exception (except UserNotExistsException) is thrown from RPC
	 */
	private String getUserPreferredMailValue(User user) throws InternalErrorException {
		cz.metacentrum.perun.core.api.Attribute preferredMailAttr = null;
		try {
			preferredMailAttr = Rpc.AttributesManager.getAttribute(ldapcManager.getRpcCaller(), user, cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":" + perunAttrPreferredMail);
		} catch(PrivilegeException ex) {
			throw new InternalErrorException("There are no privilegies for getting user's attribute.", ex);
		} catch(AttributeNotExistsException ex) {
			throw new InternalErrorException("There is no such attribute.", ex);
		} catch(UserNotExistsException ex) {
			//If user not exist in perun now, probably will be deleted in next step so its ok. The value is null anyway.
			return null;
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException("There is problem with wrong attribute assignment exception.", ex);
		}
		if(preferredMailAttr.getValue() == null) return null;
		else return (String) preferredMailAttr.getValue();
	}

	/**
	 * Get entityID value from perun by facilityId.
	 *
	 * @param facilityId the facilityId
	 * @return value of entityID or null, if value is null or user not exists yet
	 * @throws InternalErrorException if some exception is thrown from RPC
	 */
	private String getFacilityEntityIdValue(int facilityId) throws InternalErrorException {
		Facility facility = null;
		try {
			facility = Rpc.FacilitiesManager.getFacilityById(ldapcManager.getRpcCaller(), facilityId);
		} catch (PrivilegeException ex) {
			throw new InternalErrorException("There are no privilegies for getting facility by id.", ex);
		} catch (FacilityNotExistsException ex) {
			//If facility not exist in perun now, probably will be deleted in next step so its ok. The value is null anyway.
			return null;
		}

		cz.metacentrum.perun.core.api.Attribute entityID = null;
		try {
			entityID = Rpc.AttributesManager.getAttribute(ldapcManager.getRpcCaller(), facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":" + perunAttrEntityID);
		} catch(PrivilegeException ex) {
			throw new InternalErrorException("There are no privilegies for getting facility attribute.", ex);
		} catch(AttributeNotExistsException ex) {
			throw new InternalErrorException("There is no such attribute.", ex);
		} catch(FacilityNotExistsException ex) {
			//If facility not exist in perun now, probably will be deleted in next step so its ok. The value is null anyway.
			return null;
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException("There is problem with wrong attribute assignment exception.", ex);
		}
		if(entityID.getValue() == null) return null;
		else return (String) entityID.getValue();
	}

	/**
	 * Update ldap attribute with attributeName for the user by value with operation.
	 *
	 *
	 * @param attributeName name of attribute, is mandatory, cant be null
	 * @param attributeValue value of attribute, is not mandatory, can be null
	 * @param operation add, remove or replace (can't be null)
	 * @param user cant be null
	 *
	 * @exception InternalErrorException if an error occurs
	 *
	 */
	private void updateUserAttribute(String attributeName, String attributeValue, LdapOperation operation, User user) throws InternalErrorException {
		if(operation == null) throw new InternalErrorException("Operation can't be null");
		if(attributeName == null || attributeName.equals("")) throw new InternalErrorException("Bad attribute Name in method updateUserAttribute :" + attributeName);
		if(user == null) throw new InternalErrorException("User is null in method updateUserAttribute");

		Attribute attribute;
		if(attributeValue != null) attribute = new BasicAttribute(attributeName, attributeValue);
		else attribute = new BasicAttribute(attributeName);

		ModificationItem attributeItem =  new ModificationItem(operation.getCode(), attribute);
		ldapConnector.updateUser(user, new ModificationItem[] {attributeItem});
	}

	/**
	 * Update user's ldap attributes from Map by operation in key.
	 *
	 * Map<LdapOperation, List<Pair<String, String>>> => Map<LdapOperation, List<Pair<attributeName, attributeValue>>>
	 *
	 *
	 * attributeName cant be null and empty String
	 * attributeValue can be null
	 *
	 * Execute all operations on all attributes with (or without value) in 1 task.
	 *
	 * @param mapOfAttributes map of Operation to list of pairs where left is attributeName and right is attributeValue
	 * @param user cant be null
	 * @throws InternalErrorException if an error occurs
	 */
	private void updateUserAttributes(Map<LdapOperation, List<Pair<String, String>>> mapOfAttributes, User user) throws InternalErrorException {
		//User cant be null
		if(user == null) throw new InternalErrorException("User is null in method updateUserAttributes");

		Set<LdapOperation> keys = mapOfAttributes.keySet();

		//Every Pair in List need to have "attributeName" and may have "attributeValue"
		for(LdapOperation operation: keys) {
			List<Pair<String, String>> listOfAttrs = mapOfAttributes.get(operation);
			for(Pair<String, String> pair: listOfAttrs) {
				if(pair.getLeft() == null || pair.getLeft().equals("")) throw new InternalErrorException("Some attributes in map has no name.");
			}
		}

		//If all is correct, can execute operations on attributes
		List<ModificationItem> listOfItemsToModify = getListOfModificationItems(mapOfAttributes);

		//Execute all changes on the notEmpty list of items
		if(!listOfItemsToModify.isEmpty()) {
			ModificationItem[] items = Arrays.copyOf(listOfItemsToModify.toArray(), listOfItemsToModify.toArray().length, ModificationItem[].class);
			ldapConnector.updateUser(user, items);
		}
	}

	/**
	 * Update ldap attribute with attributeName for the resource by value with operation.
	 *
	 *
	 * @param attributeName name of attribute, is mandatory, cant be null
	 * @param attributeValue value of attribute, is not mandatory, can be null
	 * @param operation add, replace or remove, can't be null
	 * @param resource cant be null
	 *
	 * @exception InternalErrorException if an error occurs
	 */
	private void updateResourceAttribute(String attributeName, String attributeValue, LdapOperation operation, Resource resource) throws InternalErrorException {
		if(operation == null) throw new InternalErrorException("Operation can't be null.");
		if(attributeName == null || attributeName.equals("")) throw new InternalErrorException("Bad attribute Name in method updateResourceAttribute :" + attributeName);
		if(resource == null) throw new InternalErrorException("Resource is null in method updateResourceAttribute");

		Attribute attribute;
		if(attributeValue != null) attribute = new BasicAttribute(attributeName, attributeValue);
		else attribute = new BasicAttribute(attributeName);

		ModificationItem attributeItem =  new ModificationItem(operation.getCode(), attribute);
		ldapConnector.updateResource(resource, new ModificationItem[] {attributeItem});
	}

	/**
	 * Update resource's ldap attributes from Map by operation in key.
	 *
	 * Map<LdapOperation, List<Pair<String, String>>> => Map<LdapOperation, List<Pair<attributeName, attributeValue>>>
	 *
	 * attributeName cant be null and empty String
	 * attributeValue can be null
	 *
	 * Execute all operations on all attributes with (or without value) in 1 task.
	 *
	 * @param mapOfAttributes map of Operation to list of pairs where left is attributeName and right is attributeValue
	 * @param resource cant be null
	 * @throws InternalErrorException if an error occurs
	 */
	private void updateResourceAttributes(Map<LdapOperation, List<Pair<String, String>>> mapOfAttributes, Resource resource) throws InternalErrorException {
		//Resource cant be null
		if(resource == null) throw new InternalErrorException("Resource is null in method updateGroupAttributes");

		Set<LdapOperation> keys = mapOfAttributes.keySet();

		//Every Pair in List need to have "attributeName" and may have "attributeValue"
		for(LdapOperation operation: keys) {
			List<Pair<String, String>> listOfAttrs = mapOfAttributes.get(operation);
			for(Pair<String, String> pair: listOfAttrs) {
				if(pair.getLeft() == null || pair.getLeft().equals("")) throw new InternalErrorException("Some attributes in map has no name.");
			}
		}

		//If all is correct, can execute operations on attributes
		List<ModificationItem> listOfItemsToModify = getListOfModificationItems(mapOfAttributes);

		//Execute all changes on the notEmpty list of items
		if(!listOfItemsToModify.isEmpty()) {
			ModificationItem[] items = Arrays.copyOf(listOfItemsToModify.toArray(), listOfItemsToModify.toArray().length, ModificationItem[].class);
			ldapConnector.updateResource(resource, items);
		}
	}

	/**
	 * Move group in hierarchical structure of it's VO.
	 *
	 * That means to change attributes in LDAP with name of group (structure is defined in name of group)
	 * and references to parentGroup if any exist.
	 *
	 * @param group group object after moving
	 *
	 * @throws InternalErrorException
	 */
	private void moveGroup(Group group) throws InternalErrorException {
		Map<LdapOperation, List<Pair<String,String>>> attributes = new HashMap<LdapOperation, List<Pair<String, String>>>();

		List<Pair<String,String>> listAttributesToBeRemoved = new ArrayList<>();
		List<Pair<String,String>> listAttributesToBeReplaced = new ArrayList<>();

		listAttributesToBeReplaced.add(new Pair(ldapAttrCommonName,group.getName()));
		listAttributesToBeReplaced.add(new Pair(ldapAttrPerunUniqueGroupName, ldapConnector.getVoShortName(group.getVoId()) + ":" + group.getName()));

		//Check if group is now as subgroup or as top group
		if(group.getName().contains(":")) {
			//group is moved as subgroup
			listAttributesToBeReplaced.add(new Pair(ldapAttrPerunParentGroup, ldapAttrPerunGroupId + "=" + group.getParentGroupId().toString() + "," + ldapAttrPerunVoId + "=" + group.getVoId() + "," + ldapProperties.getLdapBase()));
			listAttributesToBeReplaced.add(new Pair(ldapAttrPerunParentGroupId, group.getParentGroupId().toString()));
		} else {
			//group is moved as top group
			if(ldapConnector.groupAttributeExist(group, ldapAttrPerunParentGroup)) {
				listAttributesToBeRemoved.add(new Pair(ldapAttrPerunParentGroup, null));
			}
			if(ldapConnector.groupAttributeExist(group, ldapAttrPerunParentGroupId)) {
				listAttributesToBeRemoved.add(new Pair(ldapAttrPerunParentGroupId, null ));
			}
		}

		//Add all attributes which will be replaced for the group (that also mean added if not exists yet)
		attributes.put(LdapOperation.REPLACE_ATTRIBUTE, listAttributesToBeReplaced);
		//Add all attributes (if any) which will be removed for group
		if(!listAttributesToBeReplaced.isEmpty()) attributes.put(LdapOperation.REMOVE_ATTRIBUTE, listAttributesToBeRemoved);

		//update attributes in LDAP for group
		updateGroupAttributes(attributes, group);
	}

	/**
	 * Update basic group attributes (name and description) in LDAP
	 *
	 * @param group group after update
	 *
	 * @throws InternalErrorException
	 */
	private void updateGroup(Group group) throws InternalErrorException {
		Map<LdapOperation, List<Pair<String,String>>> attributes = new HashMap<>();

		List<Pair<String,String>> listAttributesToBeRemoved = new ArrayList<>();
		List<Pair<String,String>> listAttributesToBeReplaced = new ArrayList<>();

		//change name
		listAttributesToBeReplaced.add(new Pair(ldapAttrCommonName,this.group.getName()));
		listAttributesToBeReplaced.add(new Pair(ldapAttrPerunUniqueGroupName, ldapConnector.getVoShortName(this.group.getVoId()) + ":" + this.group.getName()));

		//change description (or remove it if there is none)
		if(group.getDescription() != null && !group.getDescription().isEmpty()) {
			listAttributesToBeReplaced.add(new Pair(ldapAttrDescription, this.group.getDescription()));
		} else {
			if(ldapConnector.groupAttributeExist(group, ldapAttrDescription)) {
				listAttributesToBeRemoved.add(new Pair(ldapAttrDescription, null));
			}
		}

		//Add all attributes which will be replaced for the group (that also mean added if not exists yet)
		attributes.put(LdapOperation.REPLACE_ATTRIBUTE, listAttributesToBeReplaced);
		//Add all attributes (if any) which will be removed for group
		if(!listAttributesToBeReplaced.isEmpty()) attributes.put(LdapOperation.REMOVE_ATTRIBUTE, listAttributesToBeRemoved);

		//update attributes in LDAP for group
		updateGroupAttributes(attributes, group);
	}

	/**
	 * Update ldap attribute with attributeName for the group by value with operation.
	 *
	 * @param attributeName name of attribute, is mandatory, cant be null
	 * @param attributeValue value of attribute, is not mandatory, can be null
	 * @param operation add, replace or remove, can't be null
	 * @param group cant be null
	 *
	 * @exception InternalErrorException if an error occurs
	 *
	 */
	private void updateGroupAttribute(String attributeName, String attributeValue, LdapOperation operation, Group group) throws InternalErrorException {
		if(operation == null) throw new InternalErrorException("Operation can't be null.");
		if(attributeName == null || attributeName.equals("")) throw new InternalErrorException("Bad attribute Name in method updateGroupAttribute :" + attributeName);
		if(group == null) throw new InternalErrorException("Group is null in method updateGroupAttribute");

		Attribute attribute;
		if(attributeValue != null) attribute = new BasicAttribute(attributeName, attributeValue);
		else attribute = new BasicAttribute(attributeName);

		ModificationItem attributeItem =  new ModificationItem(operation.getCode(), attribute);
		ldapConnector.updateGroup(group, new ModificationItem[] {attributeItem});
	}

	/**
	 * Update group's ldap attributes from Map by operation in key.
	 *
	 * Map<LdapOperation, List<Pair<String, String>>> => Map<LdapOperation, List<Pair<attributeName, attributeValue>>>
	 *
	 * attributeName cant be null and empty String
	 * attributeValue can be null
	 *
	 * Execute all operations on all attributes with (or without value) in 1 task.
	 *
	 * @param mapOfAttributes map of Operation to list of pairs where left is attributeName and right is attributeValue
	 * @param group cant be null
	 * @throws InternalErrorException if an error occurs
	 */
	private void updateGroupAttributes(Map<LdapOperation, List<Pair<String, String>>> mapOfAttributes, Group group) throws InternalErrorException {
		//Group cant be null
		if(group == null) throw new InternalErrorException("group is null in method updateGroupAttributes");

		Set<LdapOperation> keys = mapOfAttributes.keySet();

		//Every Pair in List need to have "attributeName" and may have "attributeValue"
		for(LdapOperation operation: keys) {
			List<Pair<String, String>> listOfAttrs = mapOfAttributes.get(operation);
			for(Pair<String, String> pair: listOfAttrs) {
				if(pair.getLeft() == null || pair.getLeft().equals("")) throw new InternalErrorException("Some attributes in map has no name.");
			}
		}

		//If all is correct, can execute operations on attributes
		List<ModificationItem> listOfItemsToModify = getListOfModificationItems(mapOfAttributes);

		//Execute all changes on the notEmpty list of items
		if(!listOfItemsToModify.isEmpty()) {
			ModificationItem[] items = Arrays.copyOf(listOfItemsToModify.toArray(), listOfItemsToModify.toArray().length, ModificationItem[].class);
			ldapConnector.updateGroup(group, items);
		}
	}

	/**
	 * Update ldap attribute with attributeName for the vo by value with operation.
	 *
	 * @param attributeName name of attribute, is mandatory, cant be null
	 * @param attributeValue value of attribute, is not mandatory, can be null
	 * @param operation add, replace or remove, can't be null
	 * @param vo cant be null
	 *
	 * @exception InternalErrorException if an error occurs
	 */
	private void updateVoAttribute(String attributeName, String attributeValue, LdapOperation operation, Vo vo) throws InternalErrorException {
		if(operation == null) throw new InternalErrorException("Operation can't be null.");
		if(attributeName == null || attributeName.equals("")) throw new InternalErrorException("Bad attribute Name in method updateVoAttribute :" + attributeName);
		if(vo == null) throw new InternalErrorException("Vo is null in method updateVoAttribute");

		Attribute attribute;
		if(attributeValue != null) attribute = new BasicAttribute(attributeName, attributeValue);
		else attribute = new BasicAttribute(attributeName);

		ModificationItem attributeItem =  new ModificationItem(operation.getCode(), attribute);
		ldapConnector.updateVo(vo, new ModificationItem[] {attributeItem});
	}

	/**
	 * Update vo's ldap attributes from Map by operation in key.
	 *
	 * Map<LdapOperation, List<Pair<String, String>>> => Map<LdapOperation, List<Pair<attributeName, attributeValue>>>
	 *
	 *
	 * attributeName cant be null and empty String
	 * attributeValue can be null
	 *
	 * Execute all operations on all attributes with (or without value) in 1 task.
	 *
	 * @param mapOfAttributes map of Operation to list of pairs where left is attributeName and right is attributeValue
	 * @param vo cant be null
	 * @throws InternalErrorException if an error occurs
	 */
	private void updateVoAttributes(Map<LdapOperation, List<Pair<String, String>>> mapOfAttributes, Vo vo) throws InternalErrorException {
		//User cant be null
		if(vo == null) throw new InternalErrorException("Vo is null in method updateVoAttributes");
		//Only 3 types of key are allowed (1,2 or 3) Modification classes
		Set<LdapOperation> keys = mapOfAttributes.keySet();

		//Every Pair in List need to have "attributeName" and may have "attributeValue"
		for(LdapOperation operation: keys) {
			List<Pair<String, String>> listOfAttrs = mapOfAttributes.get(operation);
			for(Pair<String, String> pair: listOfAttrs) {
				if(pair.getLeft() == null || pair.getLeft().equals("")) throw new InternalErrorException("Some attributes in map has no name.");
			}
		}

		//If all is correct, can execute operations on attributes
		List<ModificationItem> listOfItemsToModify = getListOfModificationItems(mapOfAttributes);

		//Execute all changes on the notEmpty list of items
		if(!listOfItemsToModify.isEmpty()) {
			ModificationItem[] items = Arrays.copyOf(listOfItemsToModify.toArray(), listOfItemsToModify.toArray().length, ModificationItem[].class);
			ldapConnector.updateVo(vo, items);
		}
	}

	/**
	 * Update multi value attribute for user in LDAP by name and values
	 *
	 * @param listOfValues to set, can be empty or null (that means no values should be set - remove existing values if any)
	 * @param user not null user to set attribute in ldap
	 * @param attributeLDAPName not null, not empty name of attribute in ldap
	 * @throws InternalErrorException if any of mandatory arguments are empty or any internal error occurred
	 */
	private void updateUserMultivalueAttributeInLDAP(List<String> listOfValues, User user, String attributeLDAPName) throws InternalErrorException {
		if(user == null) throw new InternalErrorException("User can't be null.");
		if(attributeLDAPName == null || attributeLDAPName.isEmpty()) throw new InternalErrorException("Attribute name can't be null.");

		if (listOfValues == null || listOfValues.isEmpty()) {
			if (ldapConnector.userAttributeExist(user, attributeLDAPName)) {
				updateUserAttribute(attributeLDAPName, null, LdapOperation.REMOVE_ATTRIBUTE, this.user);
			}
		} else {
			String[] subjectsArray = Arrays.copyOf(listOfValues.toArray(), listOfValues.toArray().length, String[].class);
			ldapConnector.updateUsersAttributeInLDAP(String.valueOf(this.user.getId()), attributeLDAPName, subjectsArray);
		}
	}

	/**
	 * Take map of operations and associated items to be add, replaced or removed from LDAP. Then convert this map to
	 * list of ModificationItems (Modification Item is attribute in LDAP with new value and operation which will be
	 * processed for it. Return this map.
	 *
	 * Possible Operations:
	 * 1 - ADD_ATTRIBUTE
	 * 2 - REPLACE_ATTRIBUTE
	 * 3 - REMOVE_ATTRIBUTES
	 *
	 * Skip not existing operations in map. If map is null, return empty list. If list of operations under operation is
	 * null, skipt it too.
	 *
	 * @param mapOfAttributes map of operations and associated items with new values to be processed
	 * @return list of modificationItems to be processed
	 */
	private List<ModificationItem> getListOfModificationItems(Map<LdapOperation, List<Pair<String, String>>> mapOfAttributes) {
		List<ModificationItem> listOfItemsToModify = new ArrayList<>();
		if(mapOfAttributes == null) return listOfItemsToModify;

		//For all attributes with operation ADD (1)
		if(mapOfAttributes.containsKey(LdapOperation.ADD_ATTRIBUTE)) {
			List<Pair<String,String>> listOfAttributesToAdd = mapOfAttributes.get(LdapOperation.ADD_ATTRIBUTE);
			if(listOfAttributesToAdd != null) {
				for (Pair<String, String> pair : listOfAttributesToAdd) {
					Attribute attribute;
					if (pair.getRight() != null) attribute = new BasicAttribute(pair.getLeft(), pair.getRight());
					else attribute = new BasicAttribute(pair.getLeft());
					ModificationItem attributeItem = new ModificationItem(LdapOperation.ADD_ATTRIBUTE.getCode(), attribute);
					listOfItemsToModify.add(attributeItem);
				}
			}
		}

		//For all attributes with operation REPLACE (2)
		if(mapOfAttributes.containsKey(LdapOperation.REPLACE_ATTRIBUTE)) {
			List<Pair<String,String>> listOfAttributesToReplace = mapOfAttributes.get(LdapOperation.REPLACE_ATTRIBUTE);
			if(listOfAttributesToReplace != null) {
				for (Pair<String, String> pair : listOfAttributesToReplace) {
					Attribute attribute;
					if (pair.getRight() != null) attribute = new BasicAttribute(pair.getLeft(), pair.getRight());
					else attribute = new BasicAttribute(pair.getLeft());
					ModificationItem attributeItem = new ModificationItem(LdapOperation.REPLACE_ATTRIBUTE.getCode(), attribute);
					listOfItemsToModify.add(attributeItem);
				}
			}
		}

		//For all attributes with operation REMOVE (3)
		if(mapOfAttributes.containsKey(LdapOperation.REMOVE_ATTRIBUTE)) {
			List<Pair<String,String>> listOfAttributesToRemove = mapOfAttributes.get(LdapOperation.REMOVE_ATTRIBUTE);
			if(listOfAttributesToRemove != null) {
				for (Pair<String, String> pair : listOfAttributesToRemove) {
					Attribute attribute;
					if (pair.getRight() != null) attribute = new BasicAttribute(pair.getLeft(), pair.getRight());
					else attribute = new BasicAttribute(pair.getLeft());
					ModificationItem attributeItem = new ModificationItem(LdapOperation.REMOVE_ATTRIBUTE.getCode(), attribute);
					listOfItemsToModify.add(attributeItem);
				}
			}
		}

		return listOfItemsToModify;
	}

	/**
	 * Fill objects from list of beans.
	 * If list of beans is empty or null, fill nothing.
	 *
	 * @param listOfBeans list of beans already parsed from message
	 *
	 * @throws InternalErrorException If there is some inconsistence in number of one type's objects.
	 */
	private void emptyAndFillPerunBeans(List<PerunBean> listOfBeans) throws InternalErrorException {
		//First null all usefull objects
		resource = null;
		member = null;
		group = null;
		parentGroup = null;
		vo = null;
		user = null;
		specificUser = null;
		attribute = null;
		attributeDef = null;
		userExtSource = null;
		facility = null;

		//If there is no usefull object, exit method
		if(listOfBeans == null) return;

		for(PerunBean perunBean: listOfBeans) {
			if(perunBean instanceof Group) {
				if(this.group == null) this.group = (Group) perunBean;
				else this.parentGroup = (Group) perunBean;
			} else if(perunBean instanceof Member) {
				if(this.member == null) this.member = (Member) perunBean;
				else throw new InternalErrorException("More than one member come to method parseMessages!");
			} else if(perunBean instanceof Vo) {
				if(this.vo == null) this.vo = (Vo) perunBean;
				else throw new InternalErrorException("More than one vo come to method parserMessages!");
			} else if(perunBean instanceof User) {
				User u = (User) perunBean;
				if(u.isServiceUser() || u.isSponsoredUser()) {
					if(this.specificUser == null) this.specificUser = u;
					else throw new InternalErrorException("More than one specificUser come to method parseMessages!");
				} else {
					if(this.user == null) this.user = u;
					else throw new InternalErrorException("More than one user come to method parseMessages!");
				}
			} else if(perunBean instanceof AttributeDefinition && perunBean instanceof cz.metacentrum.perun.core.api.Attribute) {
				if(this.attribute == null) this.attribute = (cz.metacentrum.perun.core.api.Attribute) perunBean;
				else throw new InternalErrorException("More than one attribute come to method parseMessages!");
			} else if(perunBean instanceof AttributeDefinition ) {
				if(this.attributeDef == null) this.attributeDef = (AttributeDefinition) perunBean;
				else throw new InternalErrorException("More than one attribute come to method parseMessages!");
			} else if(perunBean instanceof UserExtSource) {
				if(this.userExtSource == null) this.userExtSource = (UserExtSource) perunBean;
				else throw new InternalErrorException("More than one userExtSource come to method parseMessages!");
			} else if(perunBean instanceof Resource) {
				if(this.resource == null) this.resource = (Resource) perunBean;
				else throw new InternalErrorException("More than one Resource come to method parseMessages!");
			} else if(perunBean instanceof Facility) {
				if(this.facility == null) this.facility = (Facility) perunBean;
				else throw new InternalErrorException("More than one Facility come to method parseMessages!");
			}
		}
	}
}
