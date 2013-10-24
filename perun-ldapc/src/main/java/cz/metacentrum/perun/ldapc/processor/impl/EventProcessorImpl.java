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
import java.util.Set;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import org.springframework.ldap.core.LdapAttribute;

public class EventProcessorImpl implements EventProcessor, Runnable {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private LdapConnector ldapConnector;

  private final static Logger log = LoggerFactory.getLogger(EventProcessorImpl.class);

  private boolean running = false;
  private AuditerConsumer auditerConsumer;
  @Autowired
  private LdapcManager ldapcManager;
  
  //These patterns are used for looking in messages
  private Pattern newGroupPattern = Pattern.compile(" created in Vo:\\[(.*)\\]");
  private Pattern subGroupPattern = Pattern.compile(" created in Vo:\\[(.*)\\] as subgroup of Group:\\[(.*)\\]");
  private Pattern deletedPattern = Pattern.compile(" deleted.$");
  private Pattern createdPattern = Pattern.compile(" created.$"); 
  private Pattern updatedPattern = Pattern.compile(" updated.$");
  private Pattern addedToPattern = Pattern.compile(" added to Group:\\[(.*)\\]");
  //private Pattern removedFromPattern = Pattern.compile(" was removed from Group:\\[(.*)\\]");
  private Pattern totallyRemovedFromPatter = Pattern.compile(" was removed from Group:\\[(.*)\\] totally");
  private Pattern setPattern = Pattern.compile(" set for User:\\[(.*)\\]");
  private Pattern removePattern = Pattern.compile(" removed for User:\\[(.*)\\]");
  private Pattern allAttrsRemovedPattern = Pattern.compile("All attributes removed for User:\\[(.*)\\]");
  private Pattern addUserExtSource = Pattern.compile("UserExtSource:\\[(.*)\\] added to User:\\[(.*)\\]");
  private Pattern removeUserExtSource = Pattern.compile("UserExtSource:\\[(.*)\\] removed from User:\\[(.*)\\]");
  private Pattern assignGroupToResource = Pattern.compile("Group:\\[(.*)\\] assigned to Resource:\\[(.*)\\]");
  private Pattern removeGroupFromResource = Pattern.compile("Group:\\[(.*)\\] removed from Resource:\\[(.*)\\]");
  
  private Pattern userUidNamespace = Pattern.compile(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace:");
  private Pattern userLoginNamespace = Pattern.compile(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:");
  
  private String pathToParseErrorFile = "./errorParseMessageForLDAP.log";
  private String pathToInsolubleParseErrorFile = "./insolubleErrorForLDAP.log";
  private String pathToNotExecutedMessages = "./notExecutedMessages.log";
  
  private Pattern validatedPattern = Pattern.compile(" validated.$");
  private Pattern otherStateOfMemberPattern = Pattern.compile("expired.$|disabled.$|invalidated.$|suspended #");

  //Using this dateFormat
  private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  
  //constants
  private static final String EINFRA = "EINFRA";
  
  public void run() {
      
    try {
      this.auditerConsumer = new AuditerConsumer(LdapcManager.CONSUMERNAME, dataSource);
      running = true;
    } catch (Exception e) {
      throw new RuntimeException("Cannot initialize AuditerConsumer.", e);
    }
   
    Integer lastProcessedIdNumber = 0;
    Pair<String, Integer> message = new Pair();
    //TODO: WaitingMessages not using anymore, but will be there for a while (for other supported methods)
    Map<String, Integer> waitingMessages = new HashMap<String, Integer>();
    List<Pair<String, Integer>> messages = new ArrayList<Pair<String, Integer>>();
    try {
      while (running) {

        messages = null;
        int sleepTime = 1000;
        do {
            try {
                messages = auditerConsumer.getMessagesForParserLikePairWithId();
            } catch (InternalErrorException ex) {
                log.error("Consumer failed due to {}. Sleeping for {} ms.",ex, sleepTime);
                Thread.sleep(sleepTime);
                sleepTime+=sleepTime;
            }
        } while(messages == null);
        Iterator<Pair<String, Integer>> messagesIter = messages.iterator();
        while(messagesIter.hasNext()) {
          message = messagesIter.next();
          messagesIter.remove();
          if(lastProcessedIdNumber > 0 && lastProcessedIdNumber < message.getRight()) {
            if((message.getRight() - lastProcessedIdNumber) > 15) log.debug("SKIP FLAG WARNING: lastProcessedIdNumber: " + lastProcessedIdNumber + " - newMessageNumber: " + message.getRight() + " = " + (lastProcessedIdNumber - message.getRight()));                     
          }    
          lastProcessedIdNumber = message.getRight();
          this.parseMessage(message.getLeft(), message.getRight());
        }
        if (Thread.interrupted()) {
          running = false;
        } else {
          Thread.sleep(5000);
        }
      }
    //If ldapc is interrupted
    } catch (InterruptedException e) {
        Date date = new Date();
        log.error("Last message has ID='" + message.getRight() + "' and was INTERRUPTED at " + dateFormat.format(date));
        saveDataToFileAfterException(message, messages, waitingMessages, pathToParseErrorFile);
        running = false;
        Thread.currentThread().interrupt();
    //If some exception is thrown, we need to save all not executed data to file
    } catch (Exception e) {
        Date date = new Date();
        log.error("Last message has ID='" + message.getRight() + "' and was bad PARSED or EXECUTE at " + dateFormat.format(date));
        saveDataToFileAfterException(message, messages, waitingMessages, pathToParseErrorFile);
        throw new RuntimeException(e);
    }
  }

  /**
   * Parse the message and decide whether it wiell be further processed.
   * @param msg
   */
  protected void parseMessage(String msg, Integer idOfMessage) throws InternalErrorException {

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
    
    //IMPORTANT
    //For 2 Groups expect first is subGroup and second is parentGroup

    Resource resource = null;
    Member member = null;
    Group group = null;
    Group parentGroup = null;
    Vo vo = null;
    User user = null;
    User serviceUser = null;
    cz.metacentrum.perun.core.api.Attribute attribute = null;
    AttributeDefinition attributeDef = null;
    UserExtSource userExtSource = null;
      
    //Try to fill expecting objects
    for(PerunBean perunBean: listOfBeans) {
        if(perunBean instanceof Group) {
            if(group == null) group = (Group) perunBean;
            else parentGroup = (Group) perunBean;
        } else if(perunBean instanceof Member) {
            if(member == null) member = (Member) perunBean;
            else throw new InternalErrorException("More than one member come to method parseMessages!");
        } else if(perunBean instanceof Vo) {
            if(vo == null) vo = (Vo) perunBean;
            else throw new InternalErrorException("More than one vo come to method parserMessages!");
        } else if(perunBean instanceof User) {
            User u = (User) perunBean;
            if(u.isServiceUser()) {
                if(serviceUser == null) serviceUser = u;
                else throw new InternalErrorException("More than one serviceUser come to method parseMessages!");
            } else {
                if(user == null) user = u;
                else throw new InternalErrorException("More than one user come to method parseMessages!");
            }
        } else if(perunBean instanceof AttributeDefinition && perunBean instanceof cz.metacentrum.perun.core.api.Attribute) {
            if(attribute == null) attribute = (cz.metacentrum.perun.core.api.Attribute) perunBean;
            else throw new InternalErrorException("More than one attribute come to method parseMessages!");
        } else if(perunBean instanceof AttributeDefinition ) {
            if(attributeDef == null) attributeDef = (AttributeDefinition) perunBean;
            else throw new InternalErrorException("More than one attribute come to method parseMessages!");
        } else if(perunBean instanceof UserExtSource) {
            if(userExtSource == null) userExtSource = (UserExtSource) perunBean;
            else throw new InternalErrorException("More than one userExtSource come to method parseMessages!");
        } else if(perunBean instanceof Resource) {
            if(resource == null) resource = (Resource) perunBean;
            else throw new InternalErrorException("More than one Resource come to method parseMessages!");
        }
    }
    
    log.debug("MessageNumber=" + idOfMessage + " -- OBJECTS: " + member + '/' + group + '/' + parentGroup + '/' + vo + '/' 
            + resource + '/' + user + '/' + attribute + '/' + attributeDef + '/' + userExtSource);
    
    //If service user is the only one user in message, so behavior will be same for him like for any other user!
    if(serviceUser != null && user == null) user = serviceUser;
    
    //IF GROUP AND MEMBER WAS FOUND, WE TRY TO WORK WITH GROUP-MEMBER SPECIFIC OPERATIONS
    if(group != null && member != null) {
        //ONLY FOR VALID MEMBER WE ADD HIM TO THE GROUP IN LDAP
        if(member.getStatus().equals(Status.VALID)) {
            Matcher addedTo = addedToPattern.matcher(msg);
            
            if(addedTo.find()) {
                if(!ldapConnector.isAlreadyMember(member, group)) ldapConnector.addMemberToGroup(member, group);
            }
        }
        //MEMBER WILL BE REMOVED FROM GROUP
        //Matcher removedFrom = removedFromPattern.matcher(msg);
        Matcher totallyRemovedFrom = totallyRemovedFromPatter.matcher(msg);
        
        if(totallyRemovedFrom.find()) {
            if(ldapConnector.isAlreadyMember(member, group)) ldapConnector.removeMemberFromGroup(member, group);
        }     
    //IF GROUP WAS FOUND, WE TRY TO WORK WITH GROUP SPECIFIC OPERATIONS
    } else if(group != null && parentGroup != null) {
        Matcher newSubGroup = subGroupPattern.matcher(msg);
        
        if(newSubGroup.find()) {
            ldapConnector.addGroupAsSubGroup(group, parentGroup);
        }
    } else if(group != null && resource != null) {
        Matcher assigned = assignGroupToResource.matcher(msg);
        Matcher removed = removeGroupFromResource.matcher(msg);
        
        if(assigned.find()) {
            updateGroupAttribute("assignedToResourceId", String.valueOf(resource.getId()), DirContext.ADD_ATTRIBUTE, group);
        } else if(removed.find()) {
            updateGroupAttribute("assignedToResourceId", String.valueOf(resource.getId()), DirContext.REMOVE_ATTRIBUTE, group);
        }
    } else if(group != null) {
        Matcher deleted = deletedPattern.matcher(msg);
        Matcher newGroup = newGroupPattern.matcher(msg);
        Matcher updated = updatedPattern.matcher(msg);
        
        //GROUP WILL BE DELTED
        if(deleted.find()){
            ldapConnector.removeGroup(group);
        //GROUP WILL BE CREATED
        } else if(newGroup.find()) {
            ldapConnector.addGroup(group);
        //GROUP WILL BE UPDATED
        } else if(updated.find()) {
            Map<Integer, List<Pair<String,String>>> attributes = new HashMap<Integer, List<Pair<String, String>>>();
            List<Pair<String,String>> replaceList = new ArrayList<Pair<String, String>>();
            replaceList.add(new Pair("cn",group.getName()));
            replaceList.add(new Pair("perunUniqueGroupName", ldapConnector.getVoShortName(group.getVoId()) + ":" + group.getName()));
            if(group.getDescription() != null) replaceList.add(new Pair("description", group.getDescription()));
            attributes.put(DirContext.REPLACE_ATTRIBUTE, replaceList);
            updateGroupAttributes(attributes, group);
        }
    //IF MEMBER WAS FOUND, TRY TO WORK WITH MEMBER SPECIFIC OPERATIONS
    } else if(member != null) {
        Matcher validated = validatedPattern.matcher(msg);
        Matcher otherStateOfMember = otherStateOfMemberPattern.matcher(msg);
        
        if(validated.find()) {
            List<Group> memberGroups = new ArrayList<Group>();
            try {
                memberGroups = Rpc.GroupsManager.getAllMemberGroups(ldapcManager.getRpcCaller(), member);
            } catch (MemberNotExistsException e) {
                //IMPORTATNT this is not problem, if member not exist, we expected that will be deleted in some message after that, in DB is deleted
            } catch (PrivilegeException e) {
                throw new InternalErrorException("There are no privilegies for getting member's groups", e);
            } catch (InternalErrorException e) {
                throw e;
            }
            for(Group g: memberGroups) {
                if(!ldapConnector.isAlreadyMember(member, g)) ldapConnector.addMemberToGroup(member, g);
            }
        //MEMBER STATE WAS CHANGED TO OTHER STATE THAN VALIDATE (USING RPC CALLING !!!)
        } else if(otherStateOfMember.find()) {
            List<Group> memberGroups = new ArrayList<Group>();
            try {
                memberGroups = Rpc.GroupsManager.getAllMemberGroups(ldapcManager.getRpcCaller(), member);
            } catch (MemberNotExistsException e) {
                //IMPORTATNT this is not problem, if member not exist, we expected that will be deleted in some message after that, in DB is deleted
            } catch (PrivilegeException e) {
                throw new InternalErrorException("There are no privilegies for getting member's groups", e);
            } catch (InternalErrorException e) {
                throw e;
            }
            for(Group g: memberGroups) {
                if(ldapConnector.isAlreadyMember(member, g)) ldapConnector.removeMemberFromGroup(member, g);
            }
        }
    //IF VO WAS FOUND, TRY TO WORK WITH VO SPECIFIC OPERATIONS
    } else if(vo != null) {
        Matcher deleted = deletedPattern.matcher(msg);
        Matcher created = createdPattern.matcher(msg);
        Matcher updated = updatedPattern.matcher(msg);
        
        //VO WILL BE DELETED
        if(deleted.find()) {
            ldapConnector.deleteVo(vo);
        //VO WILL BE CREATED
        } else if(created.find()) {
            ldapConnector.createVo(vo);
        //VO WILL BE UPDATED
        } else if(updated.find()) {
            Map<Integer, List<Pair<String,String>>> attributes = new HashMap<Integer, List<Pair<String, String>>>();
            List<Pair<String,String>> replaceList = new ArrayList<Pair<String, String>>();
            replaceList.add(new Pair("description",vo.getName()));
            attributes.put(DirContext.REPLACE_ATTRIBUTE, replaceList);
            updateVoAttributes(attributes, vo);
        }
    //IF USER AND USEREXTSOURCE WAS FOUND, TRY TO WORK WITH USER-USEREXTSOURCE SPECIFIC OPERATIONS (LIKE SET EXT LOGINS FOR IDP EXTSOURCES)
    } else if(user != null && userExtSource != null) {
        Matcher addExtSource = addUserExtSource.matcher(msg);
        Matcher removeExtSource = removeUserExtSource.matcher(msg);
        
        if(addExtSource.find()) {
            if(userExtSource.getExtSource() != null && userExtSource.getExtSource().getType() != null) {
                String extLogin;
                if(userExtSource.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
                    extLogin = userExtSource.getLogin();
                    if(extLogin == null) extLogin = "";
                    updateUserAttribute("eduPersonPrincipalNames", extLogin, DirContext.ADD_ATTRIBUTE, user);
                }
            }
        } else if(removeExtSource.find()) {
            if(userExtSource.getExtSource() != null && userExtSource.getExtSource().getType() != null) {
                String extLogin;
                if(userExtSource.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
                    extLogin = userExtSource.getLogin();
                    if(extLogin == null) extLogin = "";
                    updateUserAttribute("eduPersonPrincipalNames", extLogin, DirContext.REMOVE_ATTRIBUTE, user);
                }
            }
        }
    //IF USER AND ATTRIBUTE WAS FOUND, TRY TO WORK WITH USER-ATTR SPECIFIC OPERATIONS (LIKE SET USER ATTRIBUTES)
    } else if(user != null && attribute != null) {
        Matcher set = setPattern.matcher(msg);
        
        //SOME USER ATTRIBUTE WILL BE PROBABLY SET (IF IT IS ONE OF SPECIFIC ATTRIBUTES)
        if(set.find()) {
            Matcher uidMatcher = userUidNamespace.matcher(attribute.getName());
            Matcher loginMatcher = userLoginNamespace.matcher(attribute.getName());
            //USER PREFERREDMAIL WILL BE SET
            if(attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":preferredMail")) {
                //this mean change of attribute preferredMail in User
                if(attribute.getValue() != null) {
                    updateUserAttribute("preferredMail", (String) attribute.getValue(), DirContext.REPLACE_ATTRIBUTE, user);
                    updateUserAttribute("mail", (String) attribute.getValue(), DirContext.REPLACE_ATTRIBUTE, user);                    
                } else {
                    if(ldapConnector.userAttributeExist(user, "preferredMail")) {
                        updateUserAttribute("preferredMail", null, DirContext.REMOVE_ATTRIBUTE, user);
                    }
                    if(ldapConnector.userAttributeExist(user, "mail")) {
                        updateUserAttribute("mail", null, DirContext.REMOVE_ATTRIBUTE, user);
                    }
                }
                
              //TODO: user organization will be removed
                
            //USER ORGANIZATION WILL BE SET
            } else if(attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":organization")) {
                if(attribute.getValue() != null) {
                    updateUserAttribute("o", (String) attribute.getValue(), DirContext.REPLACE_ATTRIBUTE, user);
                } else {
                    if(ldapConnector.userAttributeExist(user, "o")) {
                        updateUserAttribute("o", null, DirContext.REMOVE_ATTRIBUTE, user);
                    }
                }
            //USER CERT DNS WILL BE SET (it is only attribute where for set is special method)
            } else if(attribute.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":userCertDNs")) {
                Map<String, String> certDNsMap = new HashMap<String, String>();
                if(attribute.getValue() != null) certDNsMap = (Map) attribute.getValue();
                else certDNsMap = null;
                
                if(certDNsMap == null || certDNsMap.isEmpty()) {
                    if(ldapConnector.userAttributeExist(user, "userCertificateSubject")) {
                        updateUserAttribute("userCertificateSubject", null, DirContext.REMOVE_ATTRIBUTE, user);
                    }
                } else {
                    Set<String> certSubjects =((Map) attribute.getValue()).keySet();
                    String[] subjectsArray = Arrays.copyOf(certSubjects.toArray(), certSubjects.toArray().length, String[].class);
                    ldapConnector.updateUsersCertSubjects(String.valueOf(user.getId()), subjectsArray);
                }
            //USER UID NUMBER WILL BE SET
            } else if(uidMatcher.find()) {
                if(attribute.getValue() != null) {
                    updateUserAttribute("uidNumber;x-ns-" + attribute.getFriendlyNameParameter(), String.valueOf((Integer) attribute.getValue()), DirContext.REPLACE_ATTRIBUTE, user);
                } else {
                    if(ldapConnector.userAttributeExist(user, "uidNumber;x-ns-" + attribute.getFriendlyNameParameter())) {
                        updateUserAttribute("uidNumber;x-ns-" + attribute.getFriendlyNameParameter(), null, DirContext.REMOVE_ATTRIBUTE, user);
                    }
                }
            //USER LOGIN WILL BE SET
            } else if(loginMatcher.find()) {
                if(attribute.getValue() != null) {
                    updateUserAttribute("login;x-ns-" + attribute.getFriendlyNameParameter(), (String) attribute.getValue(), DirContext.REPLACE_ATTRIBUTE, user);
                    //if login is from EINFRA (new value), then userPassword must be set or modified
                    if(EINFRA.toLowerCase().equals(attribute.getFriendlyNameParameter())) {
                        updateUserAttribute("userPassword", "{SASL}" + attribute.getValue() + "@" + EINFRA, DirContext.REPLACE_ATTRIBUTE, user);
                    }
                } else {
                    if(ldapConnector.userAttributeExist(user, "login;x-ns-" + attribute.getFriendlyNameParameter())) {
                        updateUserAttribute("login;x-ns-" + attribute.getFriendlyNameParameter(), null, DirContext.REMOVE_ATTRIBUTE, user);
                    }
                    if(EINFRA.toLowerCase().equals(attribute.getFriendlyNameParameter())) {
                        if(ldapConnector.userAttributeExist(user, "userPassword")) {    
                            updateUserAttribute("userPassword", null, DirContext.REMOVE_ATTRIBUTE, user);
                        }
                    }
                }
            }
        }
    //USER AND ATTRIBTUE DEFINITION WERE FOUND, SO TRY TO WORK WITH USER-ATTRDEF SPECIFIC OPERATIONS
    } else if(user != null && attributeDef != null) {
        Matcher remove = removePattern.matcher(msg);
        //REMOVE SPECIFIC USER ATTRIBUTE
        if(remove.find() &&  ldapConnector.userExist(user)) {
            Matcher uidMatcher = userUidNamespace.matcher(attributeDef.getName());
            Matcher loginMatcher = userLoginNamespace.matcher(attributeDef.getName());
            if(attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":preferredMail")) {
                if(ldapConnector.userAttributeExist(user, "preferredMail")) {
                    updateUserAttribute("preferredMail", null, DirContext.REMOVE_ATTRIBUTE, user);
                }
                if(ldapConnector.userAttributeExist(user, "mail")) {
                    updateUserAttribute("mail", null, DirContext.REMOVE_ATTRIBUTE, user);
                    
                }
                //TODO: organization (user) will not exists
                
            } else if(attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":organization")) {
                if(ldapConnector.userAttributeExist(user, "o")) {
                    updateUserAttribute("o", null, DirContext.REMOVE_ATTRIBUTE, user);
                }
            } else if(attributeDef.getName().equals(cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_VIRT + ":userCertDNs")) {
                if(ldapConnector.userAttributeExist(user, "userCertificateSubject")) {
                    updateUserAttribute("userCertificateSubject", null, DirContext.REMOVE_ATTRIBUTE, user);
                }
            } else if(uidMatcher.find()) {
                if(ldapConnector.userAttributeExist(user, "uidNumber;x-ns-" + attributeDef.getFriendlyNameParameter())) {
                    updateUserAttribute("uidNumber;x-ns-" + attributeDef.getFriendlyNameParameter(), null, DirContext.REMOVE_ATTRIBUTE, user);
                }
            } else if(loginMatcher.find()) {
                if(ldapConnector.userAttributeExist(user, "login;x-ns-" + attributeDef.getFriendlyNameParameter())) {
                    updateUserAttribute("login;x-ns-" + attributeDef.getFriendlyNameParameter(), null, DirContext.REMOVE_ATTRIBUTE, user);
                }
                if(EINFRA.toLowerCase().equals(attributeDef.getFriendlyNameParameter())) {
                        if(ldapConnector.userPasswordExists(user)) {    
                            updateUserAttribute("userPassword", null, DirContext.REMOVE_ATTRIBUTE, user);
                        }
                }
            }
        }
    //ONLY USER WAS FOUND, SO TRY TO WORK WITH USER SPECIFIC OPERATIONS
    } else if(user != null) {
        Matcher deleted = deletedPattern.matcher(msg);
        Matcher created = createdPattern.matcher(msg);
        Matcher updated = updatedPattern.matcher(msg);
        Matcher removedAllAttrs = allAttrsRemovedPattern.matcher(msg);
        //DELETE USER
        if(deleted.find()) {
            ldapConnector.deleteUser(user);
        //CREATE USER
        } else if(created.find()) {                 
            ldapConnector.createUser(user);
        //UPDATE USER
        } else if(updated.find()) {
            Map<Integer, List<Pair<String,String>>> attributes = new HashMap<Integer, List<Pair<String, String>>>();
            List<Pair<String,String>> replaceList = new ArrayList<Pair<String, String>>();
            replaceList.add(new Pair("sn",user.getLastName()));
            replaceList.add(new Pair("cn", user.getFirstName() + " " + user.getLastName()));
            replaceList.add(new Pair("givenName", user.getFirstName()));
            attributes.put(DirContext.REPLACE_ATTRIBUTE, replaceList);
            updateUserAttributes(attributes, user);
        //REMOVE ALL USER ATTRIBUTES
        } else if(removedAllAttrs.find()) {
            if(ldapConnector.userExist(user)) {
                Attributes usersAttrs = ldapConnector.getAllUsersAttributes(user);
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
                    ldapConnector.updateUser(user, items);
                }
            }
        }
    }
  }
  
  /**
   * This method try to parse again messages, which failed once or more times.
   * If there is more than 15 times fail, write these messages to file and throw MessageParsingFailException
   * 
   * -1 in id means "repeating message"
   * 
   * @param waitingMessages
   * @return 
   */
  @Deprecated //This method is not using now, its ok, but not needed anymore
  private Map<String, Integer> parseWaitingMessages(Map<String, Integer> waitingMessages) throws MessageParsingFailException {
    Map<String, Integer> repeatingErrorMessages = new HashMap<String, Integer>();  
    Set<String> keys = waitingMessages.keySet();
    for(String key: keys) {
        try{
            this.parseMessage(key, -1); 
        } catch (InternalErrorException e) {
            Integer value = waitingMessages.get(key) + 1;
            if((value) == 15) {
                log.error("INSOULUBLE ERROR of message {} failed again due to {}", key, e);
                if(!addDataToFile(key, pathToInsolubleParseErrorFile)) {
                    log.error("Failed insoluble message {} was not write to the file due to {}", key, e);
                }                
                throw new MessageParsingFailException("Message was failed 15 times.", key);
            } else {
                repeatingErrorMessages.put(key, value);
            }
        }
    }
    return repeatingErrorMessages;
  }
  
  //Add data to specific file
  private boolean addDataToFile(String data, String pathToFile) {
    File f = new File(pathToFile);
    try {
        f.createNewFile();
        PrintWriter writer;
        writer = new PrintWriter(new FileWriter(f, true)); 
        Date date = new Date();
        writer.println(dateFormat.format(date) + ": " + data);
        writer.close();
    } catch (IOException ex) {
        return false;
    }
    return true;
  }
  
  //This method try to save all data when exception arrived
  private void saveDataToFileAfterException(Pair<String, Integer> lastMsg, List<Pair<String, Integer>> restMessagesInList, Map<String, Integer> waitingMessages, String pathToFile) {
    File f = new File(pathToNotExecutedMessages);
    StringBuilder sb = new StringBuilder();

    try {
        f.createNewFile();
        PrintWriter writer;
        writer = new PrintWriter(new FileWriter(f, true));
        Date date = new Date();
        sb.append(dateFormat.format(date) + ": Bulk of Messages which was bad parsed or not parsed.\n");

        //All waiting messages first
        if(!waitingMessages.isEmpty()) {
            Set<String> keysOfWaitingMessages = waitingMessages.keySet();
            for(String s: keysOfWaitingMessages) {
                sb.append(s);
                sb.append('\n');
            }
        }
        //Last bad message second
        sb.append(lastMsg.getLeft());
        sb.append('\n');
        //Rest of message in the list of pairs, which was not executed yet
        for(Pair<String, Integer> p: restMessagesInList) {
            sb.append(p.getLeft());
            sb.append('\n');
        }
        writer.println(sb.toString());
        writer.close();
    } catch (IOException ex) {
      log.error("IMPORTANT: Try to save data to the file 'notExectutedMessages.log' but failed there! Messages: {} due to exception {}", sb, ex);
    }
  }
  
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
  
  private String getUserAttributeValue(User user, String attributeName) throws InternalErrorException {
    cz.metacentrum.perun.core.api.Attribute attribute = null;
    try {
        attribute = Rpc.AttributesManager.getAttribute(ldapcManager.getRpcCaller(), user, cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR_DEF + ":" + attributeName);
    } catch(PrivilegeException ex) {
        throw new InternalErrorException("There are no privilegies for getting this user attribute.", ex);
    } catch(AttributeNotExistsException ex) {
        throw new InternalErrorException("There is no such attribute.", ex);
    } catch(UserNotExistsException ex) {
        //If user not exist in perun now, probably will be deleted in next step so its ok. The value is null anyway.
    } catch(WrongAttributeAssignmentException ex) {
        throw new InternalErrorException("There is problem with wrong attribute assignment exception.", ex);
    }    
    if(attribute == null) return null;
    else if(attribute.getValue() == null) return null;
    else return (String) attribute.getValue();
  }
  
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
    } catch(WrongAttributeAssignmentException ex) {
        throw new InternalErrorException("There is problem with wrong attribute assignment exception.", ex);
    }    
    if(preferredMailAttr == null) return null;
    else if(preferredMailAttr.getValue() == null) return null;
    else return (String) preferredMailAttr.getValue();
  }
  
  /**
   * Update ldap attributeName for the user by value with operation
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
   * @exception InternalErrorException when error occures
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
   * Update user attributes.
   * Map<Integer, List<Pair<String, String>>> => Map<operation, List<Pair<attributeName, attributeValue>>>
   * operation can be only 1,2 or 3 (nothing else and cant be null)
   * attributeName cant be null and empty String
   * attributeValue can be null
   * 
   * Execute all operations on all attributes with (or without value) in 1 task.
   * 
   * @param mapOfAttributes map of Operation to list of pairs where left is attributeName and right is attributeValue
   * @param user cant be null
   * @throws InternalErrorException 
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
    
    if(!listOfItemsToModify.isEmpty()) {
        ModificationItem[] items = Arrays.copyOf(listOfItemsToModify.toArray(), listOfItemsToModify.toArray().length, ModificationItem[].class);
        ldapConnector.updateUser(user, items);
    }
  }

  /**
   * Update ldap attributeName for the group by value with operation
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
   * @exception InternalErrorException when error occures
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
   * Update group attributes.
   * Map<Integer, List<Pair<String, String>>> => Map<operation, List<Pair<attributeName, attributeValue>>>
   * operation can be only 1,2 or 3 (nothing else and cant be null)
   * attributeName cant be null and empty String
   * attributeValue can be null
   * 
   * Execute all operations on all attributes with (or without value) in 1 task.
   * 
   * @param mapOfAttributes map of Operation to list of pairs where left is attributeName and right is attributeValue
   * @param group cant be null
   * @throws InternalErrorException 
   */
  private void updateGroupAttributes(Map<Integer, List<Pair<String, String>>> mapOfAttributes, Group group) throws InternalErrorException {
    //User cant be null
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
    
    if(!listOfItemsToModify.isEmpty()) {
        ModificationItem[] items = Arrays.copyOf(listOfItemsToModify.toArray(), listOfItemsToModify.toArray().length, ModificationItem[].class);
        ldapConnector.updateGroup(group, items);
    }
  }
  
  /**
   * Update ldap attributeName for the vo by value with operation
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
   * @exception InternalErrorException when error occures
   * 
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
   * Update vo attributes.
   * Map<Integer, List<Pair<String, String>>> => Map<operation, List<Pair<attributeName, attributeValue>>>
   * operation can be only 1,2 or 3 (nothing else and cant be null)
   * attributeName cant be null and empty String
   * attributeValue can be null
   * 
   * Execute all operations on all attributes with (or without value) in 1 task.
   * 
   * @param mapOfAttributes map of Operation to list of pairs where left is attributeName and right is attributeValue
   * @param vo cant be null
   * @throws InternalErrorException 
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
    
    if(!listOfItemsToModify.isEmpty()) {
        ModificationItem[] items = Arrays.copyOf(listOfItemsToModify.toArray(), listOfItemsToModify.toArray().length, ModificationItem[].class);
        ldapConnector.updateVo(vo, items);
    }
  }  

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }
}