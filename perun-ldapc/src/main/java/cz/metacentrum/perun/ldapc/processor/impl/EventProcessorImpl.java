package cz.metacentrum.perun.ldapc.processor.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MessageParsingFailException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.AuditerConsumer;
import cz.metacentrum.perun.core.blImpl.AuditMessagesManagerBlImpl;
import cz.metacentrum.perun.ldapc.beans.LdapProperties;
import cz.metacentrum.perun.ldapc.processor.EventProcessor;
import cz.metacentrum.perun.ldapc.processor.LdapConnector;
import cz.metacentrum.perun.ldapc.service.LdapcManager;
import cz.metacentrum.perun.rpclib.Rpc;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import java.util.Properties;
import java.util.Set;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import org.springframework.ldap.core.LdapAttribute;

public class EventProcessorImpl implements EventProcessor, Runnable {

  //Autowired variables
  @Autowired
  private DataSource dataSource;
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
  User serviceUser;
  cz.metacentrum.perun.core.api.Attribute attribute;
  AttributeDefinition attributeDef;
  UserExtSource userExtSource;
  
  //PATTERNS (used for searching in messages)
  //Common patterns
  private Pattern deletedPattern = Pattern.compile(" deleted.$");
  private Pattern createdPattern = Pattern.compile(" created.$"); 
  private Pattern updatedPattern = Pattern.compile(" updated.$");
  //Groups patterns
  private Pattern newGroupPattern = Pattern.compile(" created in Vo:\\[(.*)\\]");
  private Pattern subGroupPattern = Pattern.compile(" created in Vo:\\[(.*)\\] as subgroup of Group:\\[(.*)\\]");
  private Pattern assignGroupToResource = Pattern.compile("Group:\\[(.*)\\] assigned to Resource:\\[(.*)\\]");
  private Pattern removeGroupFromResource = Pattern.compile("Group:\\[(.*)\\] removed from Resource:\\[(.*)\\]");
  //Members patterns
  private Pattern addedToPattern = Pattern.compile(" added to Group:\\[(.*)\\]");
  private Pattern totallyRemovedFromPatter = Pattern.compile(" was removed from Group:\\[(.*)\\] totally");
  private Pattern validatedPattern = Pattern.compile(" validated.$");
  private Pattern otherStateOfMemberPattern = Pattern.compile("expired.$|disabled.$|invalidated.$|suspended #");
  //Attributes patterns
  private Pattern setPattern = Pattern.compile(" set for User:\\[(.*)\\]");
  private Pattern removePattern = Pattern.compile(" removed for User:\\[(.*)\\]");
  private Pattern allAttrsRemovedPattern = Pattern.compile("All attributes removed for User:\\[(.*)\\]");
  private Pattern userUidNamespace = Pattern.compile(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace:");
  private Pattern userLoginNamespace = Pattern.compile(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:");
  //UserExtSources patterns
  private Pattern addUserExtSource = Pattern.compile("UserExtSource:\\[(.*)\\] added to User:\\[(.*)\\]");
  private Pattern removeUserExtSource = Pattern.compile("UserExtSource:\\[(.*)\\] removed from User:\\[(.*)\\]");
  
  //CONSTANTS
  private static final String LDAP_NAME = "ldap";
  private static final String PATH_TO_ERROR_FILE = "./errorParseMessageForLDAP.log";
  private static final String PATH_TO_NOT_EXECUTED_ERROR_FILE = "./notExecutedMessages.log";
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  
  /**
   * This method waiting for new messages in AuditLog (using AuditerConsumer)
   * and then call method resolveMessage or catch exceptions and log data to files.
   * 
   * @throws InterruptedException if thread is interrupted
   * @throws Exception if some other exception like InternalErrorException are thrown
   * 
   */
  public void run() {
    
    if(ldapProperties.getLdapcProperties() == null) throw new RuntimeException("LdapcProperties is not autowired correctly!");
     
    //Get instance of auditerConsumer and set runnig to true
    try {
      this.auditerConsumer = new AuditerConsumer(ldapProperties.getLdapConsumerName(), dataSource);
      running = true;
    } catch (Exception e) {
      throw new RuntimeException("Cannot initialize AuditerConsumer.", e);
    }
   
    
    Integer lastProcessedIdNumber = 0;
    Pair<String, Integer> message = new Pair<String, Integer>();
    List<Pair<String, Integer>> messages;
    
    try {
      //If running is true, then this proccess will be continously
      while (running) {

        messages = null;
        int sleepTime = 1000;
        //Waiting for new messages. If consumer failed in some internal case, waiting until it will be repaired (waiting time is increases by each attempt)
        do {
            try {
                //IMPORTANT STEP1: Get new bulk of messages
                messages = auditerConsumer.getMessagesForParserLikePairWithId();
            } catch (InternalErrorException ex) {
                log.error("Consumer failed due to {}. Sleeping for {} ms.",ex, sleepTime);
                Thread.sleep(sleepTime);
                sleepTime+=sleepTime;
            }
        } while(messages == null);
        //If new messages exist, resolve them all
        Iterator<Pair<String, Integer>> messagesIter = messages.iterator();
        while(messagesIter.hasNext()) {
          message = messagesIter.next();
          messagesIter.remove();
          //Warning when two consecutive messages are separated by more than 15 ids 
          if(lastProcessedIdNumber > 0 && lastProcessedIdNumber < message.getRight()) {
            if((message.getRight() - lastProcessedIdNumber) > 15) log.debug("SKIP FLAG WARNING: lastProcessedIdNumber: " + lastProcessedIdNumber + " - newMessageNumber: " + message.getRight() + " = " + (lastProcessedIdNumber - message.getRight()));                     
          }    
          lastProcessedIdNumber = message.getRight();
          //IMPORTANT STEP2: Resolve next message
          this.resolveMessage(message.getLeft(), message.getRight());
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
        log.error("Last message has ID='" + message.getRight() + "' and was INTERRUPTED at " + DATE_FORMAT.format(date) + " due to interrupting.");
        running = false;
        Thread.currentThread().interrupt();
    //If some other exception is thrown
    } catch (Exception e) {
        Date date = new Date();
        log.error("Last message has ID='" + message.getRight() + "' and was bad PARSED or EXECUTE at " + DATE_FORMAT.format(date) + " due to exception " + e.toString());
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
   * -> 4) only GROUP exists
   *   -> 4.1) if there is message with deleting group => delete this group from LDAP
   *   -> 4.2) if there is message with creating group => create this group in LDAP
   *   -> 4.3) if there is message with updating group => update this group in LDAP
   * -> 5) only MEMBER exists (RPC CALLING used)
   *   -> 5.1) if there is message with changing of member state to valid => add member to all groups in LDAP where he needs to be 
   *   -> 5.2) if there is message with changing of member state to other than valid => remove member from all groups in LDAP where is needed
   * -> 6) only VO exists
   *   -> 6.1) if there is message with deleting vo => delete this vo from LDAP
   *   -> 6.2) if there is message with creating vo => create this vo in LDAP
   *   -> 6.3) if there is message with updating vo => update this vo in LDAP
   * -> 7) USER and USER_EXT_SOURCE exist
   *   -> 7.1) if there is message with adding userExtSource (IDP) to user => create or update attribute of user in LDAP
   *   -> 7.2) if there is message with removing userExtSource (IDP) from user => remove or update attribute of user in LDAP
   * -> 8) USER and ATTRIBUTE exist
   *   -> 8.1) if there is message with setting attribute to user => set Attribute to user in LDAP
   * -> 9) USER and ATTRIBUTE_DEFINITION exist
   *   -> 9.1) if there is message with removing attribute from user => remove Attribute from user in LDAP 
   * -> 10) only USER exists
   *   -> 10.1) if there is message with deleting user => delete user from LDAP
   *   -> 10.2) if there is message with creating user => create user in LDAP
   *   -> 10.3) if there is message with updating user => update user in LDAP
   *   -> 10.4) if there is message with removing all attribute from user => remove all attributes from user in LDAP (only removeable attributes)
   * -> 11) in all other cases
   *   -> 11.1) always => only log some information
   * 
   * @param msg message which need to be parse and resolve
   * @param idOfMessage id of paring/resolving message
   * 
   * @throws InternalErrorException when some internal error in core occurs
   */
  protected void resolveMessage(String msg, Integer idOfMessage) throws InternalErrorException {

    List<PerunBean> listOfBeans = new ArrayList<PerunBean>();
    listOfBeans = AuditParser.parseLog(msg);
    
    //TemporaryDebug information for controling parsing of message.
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
    log.debug("MessageNumber=" + idOfMessage + " -- OBJECTS: " + this.member + '/' + this.group + '/' + this.parentGroup + '/' + this.vo + '/' 
            + this.resource + '/' + this.user + '/' + this.attribute + '/' + this.attributeDef + '/' + this.userExtSource);
    
    //If service user is the only one user in message, so behavior will be same for him like for any other user!
    if(this.serviceUser != null && this.user == null) this.user = this.serviceUser;
    
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
        //Matcher removedFrom = removedFromPattern.matcher(msg);
        Matcher totallyRemovedFrom = totallyRemovedFromPatter.matcher(msg);
        
        if(totallyRemovedFrom.find()) {
            if(ldapConnector.isAlreadyMember(this.member, this.group)) ldapConnector.removeMemberFromGroup(this.member, this.group);
        } 
        
    // 2) IF 2 GROUPS WERE FOUND, TRY TO WORK WITH PARENTGROUP-SUBGROUP SPECIFIC OPERATIONS
    } else if(this.group != null && this.parentGroup != null) {
        Matcher newSubGroup = subGroupPattern.matcher(msg);
        
        // 2.1) ADD GROUP AS SUBGROUP TO PARENTGROUP
        if(newSubGroup.find()) {
            ldapConnector.addGroupAsSubGroup(this.group, this.parentGroup);
        }
        
    // 3) IF GROUP AND RESOURCE WERE FOUND, TRY TO WORK WITH GROUP-RESOURCE SPECIFIC OPERATIONS    
    } else if(this.group != null && this.resource != null) {
        Matcher assigned = assignGroupToResource.matcher(msg);
        Matcher removed = removeGroupFromResource.matcher(msg);
        
        // 3.1) ADD NEW RESOURCE FOR GROUP IN LDAP
        if(assigned.find()) {
            updateGroupAttribute("assignedToResourceId", String.valueOf(this.resource.getId()), DirContext.ADD_ATTRIBUTE, this.group);
        // 3.2) REMOVE RESOURCE FROM GROUP IN LDAP
        } else if(removed.find()) {
            updateGroupAttribute("assignedToResourceId", String.valueOf(this.resource.getId()), DirContext.REMOVE_ATTRIBUTE, this.group);
        }
    
    // 4) IF ONLY GROUP WERE FOUND, TRY TO WORK WITH GROUP SPECIFIC OPERATIONS
    } else if(this.group != null) {
        Matcher deleted = deletedPattern.matcher(msg);
        Matcher newGroup = newGroupPattern.matcher(msg);
        Matcher updated = updatedPattern.matcher(msg);
        
        // 4.1) GROUP WILL BE DELTED
        if(deleted.find()){
            ldapConnector.removeGroup(this.group);
        // 4.2) GROUP WILL BE CREATED
        } else if(newGroup.find()) {
            ldapConnector.addGroup(this.group);
        // 4.3) GROUP WILL BE UPDATED
        } else if(updated.find()) {
            Map<Integer, List<Pair<String,String>>> attributes = new HashMap<Integer, List<Pair<String, String>>>();
            List<Pair<String,String>> replaceList = new ArrayList<Pair<String, String>>();
            replaceList.add(new Pair("cn",this.group.getName()));
            replaceList.add(new Pair("perunUniqueGroupName", ldapConnector.getVoShortName(this.group.getVoId()) + ":" + this.group.getName()));
            if(this.group.getDescription() != null) replaceList.add(new Pair("description", this.group.getDescription()));
            attributes.put(DirContext.REPLACE_ATTRIBUTE, replaceList);
            updateGroupAttributes(attributes, this.group);
        }
        
    // 5) IF MEMBER WAS FOUND, TRY TO WORK WITH MEMBER SPECIFIC OPERATIONS (! RPC CALLING used there !)
    } else if(this.member != null) {
        Matcher validated = validatedPattern.matcher(msg);
        Matcher otherStateOfMember = otherStateOfMemberPattern.matcher(msg);
        
        // 5.1) MEMBER WAS VALIDATED, NEED TO ADD HIM TO ALL GROUPS
        if(validated.find()) {
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
                if(!ldapConnector.isAlreadyMember(this.member, g)) ldapConnector.addMemberToGroup(this.member, g);
            }
        // 5.2) MEMBER STATE WAS CHANGED TO OTHER STATE THAN VALIDATE
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
        
    // 6) IF VO WAS FOUND, TRY TO WORK WITH VO SPECIFIC OPERATIONS
    } else if(this.vo != null) {
        Matcher deleted = deletedPattern.matcher(msg);
        Matcher created = createdPattern.matcher(msg);
        Matcher updated = updatedPattern.matcher(msg);
        
        // 6.1) VO WILL BE DELETED
        if(deleted.find()) {
            ldapConnector.deleteVo(this.vo);
        // 6.2) VO WILL BE CREATED
        } else if(created.find()) {
            ldapConnector.createVo(this.vo);
        // 6.3) VO WILL BE UPDATED
        } else if(updated.find()) {
            Map<Integer, List<Pair<String,String>>> attributes = new HashMap<Integer, List<Pair<String, String>>>();
            List<Pair<String,String>> replaceList = new ArrayList<Pair<String, String>>();
            replaceList.add(new Pair("description",this.vo.getName()));
            attributes.put(DirContext.REPLACE_ATTRIBUTE, replaceList);
            updateVoAttributes(attributes, this.vo);
        }
        
    // 7) IF USER AND USEREXTSOURCE WERE FOUND, TRY TO WORK WITH USER-USEREXTSOURCE SPECIFIC OPERATIONS (LIKE SET EXT LOGINS FOR IDP EXTSOURCES)
    } else if(this.user != null && this.userExtSource != null) {
        Matcher addExtSource = addUserExtSource.matcher(msg);
        Matcher removeExtSource = removeUserExtSource.matcher(msg);
        
        // 7.1) ADD ATTRIBUTE WITH IDP EXTSOURCE
        if(addExtSource.find()) {
            if(this.userExtSource.getExtSource() != null && this.userExtSource.getExtSource().getType() != null) {
                String extLogin;
                if(this.userExtSource.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
                    extLogin = this.userExtSource.getLogin();
                    if(extLogin == null) extLogin = "";
                    updateUserAttribute("eduPersonPrincipalNames", extLogin, DirContext.ADD_ATTRIBUTE, user);
                }
            }
        // 7.2) REMOVE ATTRIBUTE WITH IDP EXTSOURCE
        } else if(removeExtSource.find()) {
            if(this.userExtSource.getExtSource() != null && this.userExtSource.getExtSource().getType() != null) {
                String extLogin;
                if(this.userExtSource.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
                    extLogin = this.userExtSource.getLogin();
                    if(extLogin == null) extLogin = "";
                    updateUserAttribute("eduPersonPrincipalNames", extLogin, DirContext.REMOVE_ATTRIBUTE, this.user);
                }
            }
        }
        
    // 8) IF USER AND ATTRIBUTE WERE FOUND, TRY TO WORK WITH USER-ATTR SPECIFIC OPERATIONS (LIKE SET USER ATTRIBUTES)
    } else if(this.user != null && this.attribute != null) {
        Matcher set = setPattern.matcher(msg);
        
        // 8.1) SOME USER ATTRIBUTE WILL BE PROBABLY SET (IF IT IS ONE OF SPECIFIC ATTRIBUTES)
        if(set.find()) {
            Matcher uidMatcher = userUidNamespace.matcher(this.attribute.getName());
            Matcher loginMatcher = userLoginNamespace.matcher(this.attribute.getName());
            //USER PREFERREDMAIL WILL BE SET
            if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":preferredMail")) {
                //this mean change of attribute preferredMail in User
                if(this.attribute.getValue() != null) {
                    updateUserAttribute("preferredMail", (String) this.attribute.getValue(), DirContext.REPLACE_ATTRIBUTE, user);
                    updateUserAttribute("mail", (String) this.attribute.getValue(), DirContext.REPLACE_ATTRIBUTE, user);                    
                } else {
                    if(ldapConnector.userAttributeExist(this.user, "preferredMail")) {
                        updateUserAttribute("preferredMail", null, DirContext.REMOVE_ATTRIBUTE, this.user);
                    }
                    if(ldapConnector.userAttributeExist(this.user, "mail")) {
                        updateUserAttribute("mail", null, DirContext.REMOVE_ATTRIBUTE, this.user);
                    }
                }           
            //USER ORGANIZATION WILL BE SET
            } else if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":organization")) {
                if(this.attribute.getValue() != null) {
                    updateUserAttribute("o", (String) attribute.getValue(), DirContext.REPLACE_ATTRIBUTE, this.user);
                } else {
                    if(ldapConnector.userAttributeExist(this.user, "o")) {
                        updateUserAttribute("o", null, DirContext.REMOVE_ATTRIBUTE, this.user);
                    }
                }
            //USER CERT DNS WILL BE SET (it is only attribute where for set is special method)
            } else if(this.attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":userCertDNs")) {
                Map<String, String> certDNsMap = new HashMap<String, String>();
                if(this.attribute.getValue() != null) certDNsMap = (Map) this.attribute.getValue();
                else certDNsMap = null;
                
                if(certDNsMap == null || certDNsMap.isEmpty()) {
                    if(ldapConnector.userAttributeExist(this.user, "userCertificateSubject")) {
                        updateUserAttribute("userCertificateSubject", null, DirContext.REMOVE_ATTRIBUTE, this.user);
                    }
                } else {
                    Set<String> certSubjects =((Map) this.attribute.getValue()).keySet();
                    String[] subjectsArray = Arrays.copyOf(certSubjects.toArray(), certSubjects.toArray().length, String[].class);
                    ldapConnector.updateUsersCertSubjects(String.valueOf(this.user.getId()), subjectsArray);
                }
            //USER UID NUMBER WILL BE SET
            } else if(uidMatcher.find()) {
                if(this.attribute.getValue() != null) {
                    updateUserAttribute("uidNumber;x-ns-" + this.attribute.getFriendlyNameParameter(), String.valueOf((Integer) this.attribute.getValue()), DirContext.REPLACE_ATTRIBUTE, this.user);
                } else {
                    if(ldapConnector.userAttributeExist(this.user, "uidNumber;x-ns-" + this.attribute.getFriendlyNameParameter())) {
                        updateUserAttribute("uidNumber;x-ns-" + this.attribute.getFriendlyNameParameter(), null, DirContext.REMOVE_ATTRIBUTE, this.user);
                    }
                }
            //USER LOGIN WILL BE SET
            } else if(loginMatcher.find()) {
                if(this.attribute.getValue() != null) {
                    updateUserAttribute("login;x-ns-" + this.attribute.getFriendlyNameParameter(), (String) this.attribute.getValue(), DirContext.REPLACE_ATTRIBUTE, this.user);
                    //if login is from loginNamespace (eg. EINFRA) (new value), then userPassword must be set or modified
                    if(ldapProperties.getLdapLoginNamespace().toLowerCase().equals(this.attribute.getFriendlyNameParameter())) {
                        updateUserAttribute("userPassword", "{SASL}" + this.attribute.getValue() + "@" + ldapProperties.getLdapLoginNamespace(), DirContext.REPLACE_ATTRIBUTE, this.user);
                    }
                } else {
                    if(ldapConnector.userAttributeExist(this.user, "login;x-ns-" + this.attribute.getFriendlyNameParameter())) {
                        updateUserAttribute("login;x-ns-" + this.attribute.getFriendlyNameParameter(), null, DirContext.REMOVE_ATTRIBUTE, this.user);
                    }
                    if(ldapProperties.getLdapLoginNamespace().toLowerCase().equals(this.attribute.getFriendlyNameParameter())) {
                        if(ldapConnector.userAttributeExist(this.user, "userPassword")) {    
                            updateUserAttribute("userPassword", null, DirContext.REMOVE_ATTRIBUTE, this.user);
                        }
                    }
                }
            }
        }
        
    // 9) IF USER AND ATTRIBTUE DEFINITION WERE FOUND, TRY TO WORK WITH USER-ATTRDEF SPECIFIC OPERATIONS
    } else if(this.user != null && attributeDef != null) {
        Matcher remove = removePattern.matcher(msg);
        // 9.1) REMOVE SPECIFIC USER ATTRIBUTE
        if(remove.find() &&  ldapConnector.userExist(this.user)) {
            Matcher uidMatcher = userUidNamespace.matcher(this.attributeDef.getName());
            Matcher loginMatcher = userLoginNamespace.matcher(this.attributeDef.getName());
            if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":preferredMail")) {
                if(ldapConnector.userAttributeExist(this.user, "preferredMail")) {
                    updateUserAttribute("preferredMail", null, DirContext.REMOVE_ATTRIBUTE, this.user);
                }
                if(ldapConnector.userAttributeExist(this.user, "mail")) {
                    updateUserAttribute("mail", null, DirContext.REMOVE_ATTRIBUTE, this.user);
                }
                //TODO: organization (user) will not exists
                
            } else if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":organization")) {
                if(ldapConnector.userAttributeExist(this.user, "o")) {
                    updateUserAttribute("o", null, DirContext.REMOVE_ATTRIBUTE, this.user);
                }
            } else if(this.attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":userCertDNs")) {
                if(ldapConnector.userAttributeExist(this.user, "userCertificateSubject")) {
                    updateUserAttribute("userCertificateSubject", null, DirContext.REMOVE_ATTRIBUTE, this.user);
                }
            } else if(uidMatcher.find()) {
                if(ldapConnector.userAttributeExist(this.user, "uidNumber;x-ns-" + this.attributeDef.getFriendlyNameParameter())) {
                    updateUserAttribute("uidNumber;x-ns-" + this.attributeDef.getFriendlyNameParameter(), null, DirContext.REMOVE_ATTRIBUTE, this.user);
                }
            } else if(loginMatcher.find()) {
                if(ldapConnector.userAttributeExist(this.user, "login;x-ns-" + this.attributeDef.getFriendlyNameParameter())) {
                    updateUserAttribute("login;x-ns-" + this.attributeDef.getFriendlyNameParameter(), null, DirContext.REMOVE_ATTRIBUTE, this.user);
                }
                if(ldapProperties.getLdapLoginNamespace().toLowerCase().equals(this.attributeDef.getFriendlyNameParameter())) {
                        if(ldapConnector.userPasswordExists(this.user)) {    
                            updateUserAttribute("userPassword", null, DirContext.REMOVE_ATTRIBUTE, this.user);
                        }
                }
            }
        }
    // 10) IF ONLY USER WAS FOUND, TRY TO WORK WITH USER SPECIFIC OPERATIONS
    } else if(this.user != null) {
        Matcher deleted = deletedPattern.matcher(msg);
        Matcher created = createdPattern.matcher(msg);
        Matcher updated = updatedPattern.matcher(msg);
        Matcher removedAllAttrs = allAttrsRemovedPattern.matcher(msg);
        // 10.1) DELETE USER
        if(deleted.find()) {
            ldapConnector.deleteUser(this.user);
        // 10.2) CREATE USER
        } else if(created.find()) {                 
            ldapConnector.createUser(this.user);
        // 10.3) UPDATE USER
        } else if(updated.find()) {
            Map<Integer, List<Pair<String,String>>> attributes = new HashMap<Integer, List<Pair<String, String>>>();
            List<Pair<String,String>> replaceList = new ArrayList<Pair<String, String>>();
            replaceList.add(new Pair("sn",this.user.getLastName()));
            replaceList.add(new Pair("cn", this.user.getFirstName() + " " + this.user.getLastName()));
            replaceList.add(new Pair("givenName", this.user.getFirstName()));
            attributes.put(DirContext.REPLACE_ATTRIBUTE, replaceList);
            updateUserAttributes(attributes, this.user);
        // 10.4) REMOVE ALL USER ATTRIBUTES
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
                                    ModificationItem item = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attr);
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
        
    // 11) IN OTHER CASES 
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
      List<String> nonOptionalAttributes = new ArrayList<String>();
      nonOptionalAttributes.add("mail");
      nonOptionalAttributes.add("preferredMail");
      nonOptionalAttributes.add("o");
      nonOptionalAttributes.add("userCertificateSubject");
      if(nonOptionalAttributes.contains(attributeName)) return true;
      
      List<String> optionalAttributes = new ArrayList<String>();
      optionalAttributes.add("uidNumber");
      optionalAttributes.add("login");
      optionalAttributes.add("userPassword");
      
      for(String s: optionalAttributes) {
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
        preferredMailAttr = Rpc.AttributesManager.getAttribute(ldapcManager.getRpcCaller(), user, cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":preferredMail");
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
   * Update ldap attribute with attributeName for the user by value with operation.
   * 
   * Operation 1 = add
   * Operation 2 = replace 
   * Operation 3 = remove
   * Operation less then 1 or more than 3 = InternalErrorException
   * 
   * @param attributeName name of attribute, is mandatory, cant be null
   * @param attributeValue value of attribute, is not mandatory, can be null
   * @param operation can be 1 or 2 or 3 nothing else, cant be null
   * @param user cant be null
   * 
   * @exception InternalErrorException if an error occurs
   * 
   */
  private void updateUserAttribute(String attributeName, String attributeValue, Integer operation, User user) throws InternalErrorException {
    if(operation == null || operation < 1 || operation > 3) throw new InternalErrorException("Bad operation for method updateUserAttribute :" + operation);
    if(attributeName == null || attributeName.equals("")) throw new InternalErrorException("Bad attribute Name in method updateUserAttribute :" + attributeName);
    if(user == null) throw new InternalErrorException("User is null in method updateUserAttribute");
    
    Attribute attribute;
    if(attributeValue != null) attribute = new BasicAttribute(attributeName, attributeValue);
    else attribute = new BasicAttribute(attributeName);
    
    ModificationItem attributeItem =  new ModificationItem(operation, attribute);
    ldapConnector.updateUser(user, new ModificationItem[] {attributeItem});
  }
  
  /**
   * Update user's ldap attributes from Map by operation in key.
   * 
   * Map<Integer, List<Pair<String, String>>> => Map<operation, List<Pair<attributeName, attributeValue>>>
   * 
   * Operation 1 = add
   * Operation 2 = replace 
   * Operation 3 = remove
   * Operation less then 1 or more than 3 = InternalErrorException
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
  private void updateUserAttributes(Map<Integer, List<Pair<String, String>>> mapOfAttributes, User user) throws InternalErrorException {
    //User cant be null
    if(user == null) throw new InternalErrorException("User is null in method updateUserAttributes");
    //Only 3 types of key are allowed (1,2 or 3) Modification classes
    Set<Integer> keys = null;
    keys = mapOfAttributes.keySet();
    if(keys.size()>3) throw new InternalErrorException("There are some not allowed operations.");
    for(Integer operation: keys) {
        if(operation == null || operation <0 || operation >3) throw new InternalErrorException("There are some not allowed operations.");
    }
    
    //Every Pair in List need to have "attributeName" and may have "attributeValue"
    for(Integer operation: keys) {
        List<Pair<String, String>> listOfAttrs = mapOfAttributes.get(operation);
        for(Pair<String, String> pair: listOfAttrs) {
            if(pair.getLeft() == null || pair.getLeft().equals("")) throw new InternalErrorException("Some attributes in map has no name.");
        }
    }
    
    //If all is correct, can execute operations on attributes
    List<ModificationItem> listOfItemsToModify = new ArrayList<ModificationItem>();
    
    //For all attributes with operation ADD (1)
    if(mapOfAttributes.containsKey(DirContext.ADD_ATTRIBUTE)) {
        List<Pair<String,String>> listOfAddingAttributes = mapOfAttributes.get(DirContext.ADD_ATTRIBUTE);
        for(Pair<String,String> pair: listOfAddingAttributes) {
            Attribute attribute;
            if(pair.getRight() != null) attribute = new BasicAttribute(pair.getLeft(), pair.getRight());
            else attribute = new BasicAttribute(pair.getRight());
            ModificationItem attributeItem =  new ModificationItem(DirContext.ADD_ATTRIBUTE, attribute);
            listOfItemsToModify.add(attributeItem);
        }
    }
    
    //For all attributes with operation REPLACE (2)
    if(mapOfAttributes.containsKey(DirContext.REPLACE_ATTRIBUTE)) {
        List<Pair<String,String>> listOfAddingAttributes = mapOfAttributes.get(DirContext.REPLACE_ATTRIBUTE);
        for(Pair<String,String> pair: listOfAddingAttributes) {
            Attribute attribute;
            if(pair.getRight() != null) attribute = new BasicAttribute(pair.getLeft(), pair.getRight());
            else attribute = new BasicAttribute(pair.getRight());
            ModificationItem attributeItem =  new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute);
            listOfItemsToModify.add(attributeItem);
        }
    }
    
    //For all attributes with operation REMOVE (3)
    if(mapOfAttributes.containsKey(DirContext.REMOVE_ATTRIBUTE)) {
        List<Pair<String,String>> listOfAddingAttributes = mapOfAttributes.get(DirContext.REMOVE_ATTRIBUTE);
        for(Pair<String,String> pair: listOfAddingAttributes) {
            Attribute attribute;
            if(pair.getRight() != null) attribute = new BasicAttribute(pair.getLeft(), pair.getRight());
            else attribute = new BasicAttribute(pair.getRight());
            ModificationItem attributeItem =  new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attribute);
            listOfItemsToModify.add(attributeItem);
        }
    }
    
    //Execute all changes on the notEmpty list of items
    if(!listOfItemsToModify.isEmpty()) {
        ModificationItem[] items = Arrays.copyOf(listOfItemsToModify.toArray(), listOfItemsToModify.toArray().length, ModificationItem[].class);
        ldapConnector.updateUser(user, items);
    }
  }

  /**
   * Update ldap attribute with attributeName for the group by value with operation.
   * 
   * Operation 1 = add
   * Operation 2 = replace 
   * Operation 3 = remove
   * Operation less then 1 or more than 3 = InternalErrorException
   * 
   * @param attributeName name of attribute, is mandatory, cant be null
   * @param attributeValue value of attribute, is not mandatory, can be null
   * @param operation can be 1 or 2 or 3 nothing else, cant be null
   * @param group cant be null
   * 
   * @exception InternalErrorException if an error occurs
   * 
   */
  private void updateGroupAttribute(String attributeName, String attributeValue, Integer operation, Group group) throws InternalErrorException {
    if(operation == null || operation < 1 || operation > 3) throw new InternalErrorException("Bad operation for method updateUserAttribute :" + operation);
    if(attributeName == null || attributeName.equals("")) throw new InternalErrorException("Bad attribute Name in method updateUserAttribute :" + attributeName);
    if(group == null) throw new InternalErrorException("Group is null in method updateGroupAttribute");
    
    Attribute attribute;
    if(attributeValue != null) attribute = new BasicAttribute(attributeName, attributeValue);
    else attribute = new BasicAttribute(attributeName);
    
    ModificationItem attributeItem =  new ModificationItem(operation, attribute);
    ldapConnector.updateGroup(group, new ModificationItem[] {attributeItem});
  }
  
  /**
   * Update group's ldap attributes from Map by operation in key.
   * 
   * Map<Integer, List<Pair<String, String>>> => Map<operation, List<Pair<attributeName, attributeValue>>>
   * 
   * Operation 1 = add
   * Operation 2 = replace 
   * Operation 3 = remove
   * Operation less then 1 or more than 3 = InternalErrorException
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
  private void updateGroupAttributes(Map<Integer, List<Pair<String, String>>> mapOfAttributes, Group group) throws InternalErrorException {
    //Group cant be null
    if(group == null) throw new InternalErrorException("group is null in method updateUserAttributes");
    //Only 3 types of key are allowed (1,2 or 3) Modification classes
    Set<Integer> keys = null;
    keys = mapOfAttributes.keySet();
    if(keys.size()>3) throw new InternalErrorException("There are some not allowed operations.");
    for(Integer operation: keys) {
        if(operation == null || operation <0 || operation >3) throw new InternalErrorException("There are some not allowed operations.");
    }
    
    //Every Pair in List need to have "attributeName" and may have "attributeValue"
    for(Integer operation: keys) {
        List<Pair<String, String>> listOfAttrs = mapOfAttributes.get(operation);
        for(Pair<String, String> pair: listOfAttrs) {
            if(pair.getLeft() == null || pair.getLeft().equals("")) throw new InternalErrorException("Some attributes in map has no name.");
        }
    }
    
    //If all is correct, can execute operations on attributes
    List<ModificationItem> listOfItemsToModify = new ArrayList<ModificationItem>();
    
    //For all attributes with operation ADD (1) 
    if(mapOfAttributes.containsKey(DirContext.ADD_ATTRIBUTE)) {
        List<Pair<String,String>> listOfAddingAttributes = mapOfAttributes.get(DirContext.ADD_ATTRIBUTE);
        for(Pair<String,String> pair: listOfAddingAttributes) {
            Attribute attribute;
            if(pair.getRight() != null) attribute = new BasicAttribute(pair.getLeft(), pair.getRight());
            else attribute = new BasicAttribute(pair.getRight());
            ModificationItem attributeItem =  new ModificationItem(DirContext.ADD_ATTRIBUTE, attribute);
            listOfItemsToModify.add(attributeItem);
        }
    }
    
    //For all attributes with operation REPLACE (2)
    if(mapOfAttributes.containsKey(DirContext.REPLACE_ATTRIBUTE)) {
        List<Pair<String,String>> listOfAddingAttributes = mapOfAttributes.get(DirContext.REPLACE_ATTRIBUTE);
        for(Pair<String,String> pair: listOfAddingAttributes) {
            Attribute attribute;
            if(pair.getRight() != null) attribute = new BasicAttribute(pair.getLeft(), pair.getRight());
            else attribute = new BasicAttribute(pair.getRight());
            ModificationItem attributeItem =  new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute);
            listOfItemsToModify.add(attributeItem);
        }
    }
    
    //For all attributes with operation REMOVE (3)
    if(mapOfAttributes.containsKey(DirContext.REMOVE_ATTRIBUTE)) {
        List<Pair<String,String>> listOfAddingAttributes = mapOfAttributes.get(DirContext.REMOVE_ATTRIBUTE);
        for(Pair<String,String> pair: listOfAddingAttributes) {
            Attribute attribute;
            if(pair.getRight() != null) attribute = new BasicAttribute(pair.getLeft(), pair.getRight());
            else attribute = new BasicAttribute(pair.getRight());
            ModificationItem attributeItem =  new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attribute);
            listOfItemsToModify.add(attributeItem);
        }
    }
    
    //Execute all changes on the notEmpty list of items
    if(!listOfItemsToModify.isEmpty()) {
        ModificationItem[] items = Arrays.copyOf(listOfItemsToModify.toArray(), listOfItemsToModify.toArray().length, ModificationItem[].class);
        ldapConnector.updateGroup(group, items);
    }
  }
  
  /**
   * Update ldap attribute with attributeName for the vo by value with operation.
   * 
   * Operation 1 = add
   * Operation 2 = replace 
   * Operation 3 = remove
   * Operation less then 1 or more than 3 = InternalErrorException
   * 
   * @param attributeName name of attribute, is mandatory, cant be null
   * @param attributeValue value of attribute, is not mandatory, can be null
   * @param operation can be 1 or 2 or 3 nothing else, cant be null
   * @param vo cant be null
   * 
   * @exception InternalErrorException if an error occurs 
   */
  private void updateVoAttribute(String attributeName, String attributeValue, Integer operation, Vo vo) throws InternalErrorException {
    if(operation == null || operation < 1 || operation > 3) throw new InternalErrorException("Bad operation for method updateUserAttribute :" + operation);
    if(attributeName == null || attributeName.equals("")) throw new InternalErrorException("Bad attribute Name in method updateUserAttribute :" + attributeName);
    if(vo == null) throw new InternalErrorException("Vo is null in method updateUserAttribute");
    
    Attribute attribute;
    if(attributeValue != null) attribute = new BasicAttribute(attributeName, attributeValue);
    else attribute = new BasicAttribute(attributeName);
    
    ModificationItem attributeItem =  new ModificationItem(operation, attribute);
    ldapConnector.updateVo(vo, new ModificationItem[] {attributeItem});
  }
  
  /**
   * Update vo's ldap attributes from Map by operation in key.
   * 
   * Map<Integer, List<Pair<String, String>>> => Map<operation, List<Pair<attributeName, attributeValue>>>
   * 
   * Operation 1 = add
   * Operation 2 = replace 
   * Operation 3 = remove
   * Operation less then 1 or more than 3 = InternalErrorException
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
  private void updateVoAttributes(Map<Integer, List<Pair<String, String>>> mapOfAttributes, Vo vo) throws InternalErrorException {
    //User cant be null
    if(vo == null) throw new InternalErrorException("Vo is null in method updateUserAttributes");
    //Only 3 types of key are allowed (1,2 or 3) Modification classes
    Set<Integer> keys = null;
    keys = mapOfAttributes.keySet();
    if(keys.size()>3) throw new InternalErrorException("There are some not allowed operations.");
    for(Integer operation: keys) {
        if(operation == null || operation <0 || operation >3) throw new InternalErrorException("There are some not allowed operations.");
    }
    
    //Every Pair in List need to have "attributeName" and may have "attributeValue"
    for(Integer operation: keys) {
        List<Pair<String, String>> listOfAttrs = mapOfAttributes.get(operation);
        for(Pair<String, String> pair: listOfAttrs) {
            if(pair.getLeft() == null || pair.getLeft().equals("")) throw new InternalErrorException("Some attributes in map has no name.");
        }
    }
    
    //If all is correct, can execute operations on attributes
    List<ModificationItem> listOfItemsToModify = new ArrayList<ModificationItem>();
    
    //For all attributes with operation ADD (1)
    if(mapOfAttributes.containsKey(DirContext.ADD_ATTRIBUTE)) {
        List<Pair<String,String>> listOfAddingAttributes = mapOfAttributes.get(DirContext.ADD_ATTRIBUTE);
        for(Pair<String,String> pair: listOfAddingAttributes) {
            Attribute attribute;
            if(pair.getRight() != null) attribute = new BasicAttribute(pair.getLeft(), pair.getRight());
            else attribute = new BasicAttribute(pair.getRight());
            ModificationItem attributeItem =  new ModificationItem(DirContext.ADD_ATTRIBUTE, attribute);
            listOfItemsToModify.add(attributeItem);
        }
    }
    
    //For all attributes with operation REPLACE (2)
    if(mapOfAttributes.containsKey(DirContext.REPLACE_ATTRIBUTE)) {
        List<Pair<String,String>> listOfAddingAttributes = mapOfAttributes.get(DirContext.REPLACE_ATTRIBUTE);
        for(Pair<String,String> pair: listOfAddingAttributes) {
            Attribute attribute;
            if(pair.getRight() != null) attribute = new BasicAttribute(pair.getLeft(), pair.getRight());
            else attribute = new BasicAttribute(pair.getRight());
            ModificationItem attributeItem =  new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute);
            listOfItemsToModify.add(attributeItem);
        }
    }
    
    //For all attributes with operation REMOVE (3)
    if(mapOfAttributes.containsKey(DirContext.REMOVE_ATTRIBUTE)) {
        List<Pair<String,String>> listOfAddingAttributes = mapOfAttributes.get(DirContext.REMOVE_ATTRIBUTE);
        for(Pair<String,String> pair: listOfAddingAttributes) {
            Attribute attribute;
            if(pair.getRight() != null) attribute = new BasicAttribute(pair.getLeft(), pair.getRight());
            else attribute = new BasicAttribute(pair.getRight());
            ModificationItem attributeItem =  new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attribute);
            listOfItemsToModify.add(attributeItem);
        }
    }
    
    //Execute all changes on the notEmpty list of items
    if(!listOfItemsToModify.isEmpty()) {
        ModificationItem[] items = Arrays.copyOf(listOfItemsToModify.toArray(), listOfItemsToModify.toArray().length, ModificationItem[].class);
        ldapConnector.updateVo(vo, items);
    }
  }
  
  /**
   * Fill objects from list of beans.
   * If list of beans is empty or null, fill nothing.
   * 
   * @param listOfBeans list of beans already parsed from message
   * @param group group in message
   * @param parentGroup parentGroup in message
   * @param vo vo in message
   * @param resource resource in message
   * @param member member in message
   * @param user user in message
   * @param serviceUser serviceUser in message
   * @param attributeDef attributeDefinition in message
   * @param attribute attribute in message
   * @param userExtSource userExtSource in message
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
    serviceUser = null;
    attribute = null;
    attributeDef = null;
    userExtSource = null;
    
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
            if(u.isServiceUser()) {
                if(this.serviceUser == null) this.serviceUser = u;
                else throw new InternalErrorException("More than one serviceUser come to method parseMessages!");
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
        }
    }    
  }

  //-----------------GETTERS AND SETTERS-------------------
  
  /**
   * Setter for dataSource
   * 
   * @param dataSource 
   */
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  } 
}