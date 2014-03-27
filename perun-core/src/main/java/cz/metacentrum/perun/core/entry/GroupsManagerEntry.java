package cz.metacentrum.perun.core.entry;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MembershipMismatchException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.NotMemberOfParentGroupException;
import cz.metacentrum.perun.core.api.exceptions.NotServiceUserExpectedException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.GroupsManagerImplApi;

/**
 * GroupsManager entry logic
 * 
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class GroupsManagerEntry implements GroupsManager {

  private GroupsManagerBl groupsManagerBl;
  private PerunBl perunBl;

  public GroupsManagerEntry(PerunBl perunBl) {
    this.perunBl = perunBl;
    this.groupsManagerBl = perunBl.getGroupsManagerBl();
  }

  public GroupsManagerEntry() {}

  //FIXME delete this method
  public GroupsManagerImplApi getGroupsManagerImpl() {
    throw new InternalErrorRuntimeException("Unsupported method!");
  }

  public Group createGroup(PerunSession sess, Vo vo, Group group) throws GroupExistsException, PrivilegeException, InternalErrorException, VoNotExistsException {
    Utils.checkPerunSession(sess);
    Utils.notNull(group, "group");
    Utils.notNull(group.getName(), "group.name");
    

    if (!group.getName().matches(GroupsManager.GROUP_SHORT_NAME_REGEXP)) {
      throw new InternalErrorException(new IllegalArgumentException("Wrong group name, group name must matches " + GroupsManager.GROUP_SHORT_NAME_REGEXP));
    }

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
      throw new PrivilegeException(sess, "createGroup");
    }

    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

    return getGroupsManagerBl().createGroup(sess, vo, group);
  }

  public Group createGroup(PerunSession sess, Group parentGroup, Group group) throws GroupNotExistsException, GroupExistsException, PrivilegeException, InternalErrorException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, parentGroup);
    Utils.notNull(group, "group");
    Utils.notNull(group.getName(), "group.name");

     
    if (!group.getName().matches(GroupsManager.GROUP_SHORT_NAME_REGEXP)) {
      throw new InternalErrorException(new IllegalArgumentException("Wrong group name, group name must matches " + GroupsManager.GROUP_SHORT_NAME_REGEXP));
    }

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, parentGroup)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, parentGroup)) {
      throw new PrivilegeException(sess, "createGroup - subGroup");
    }

    return getGroupsManagerBl().createGroup(sess, parentGroup, group);
  }

  public void deleteGroup(PerunSession sess, Group group, boolean forceDelete) throws GroupNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "deleteGroup");
    }

    getGroupsManagerBl().deleteGroup(sess, group, forceDelete);
  }

  public void deleteGroup(PerunSession sess, Group group) throws GroupNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
    this.deleteGroup(sess, group, false);
  }

  public void deleteAllGroups(PerunSession sess, Vo vo) throws VoNotExistsException, InternalErrorException, PrivilegeException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
      throw new PrivilegeException(sess, "deleteAllGroups");
    }

    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

    getGroupsManagerBl().deleteAllGroups(sess, vo);
  }

  public Group updateGroup(PerunSession sess, Group group) throws GroupNotExistsException, InternalErrorException, PrivilegeException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group);
    Utils.notNull(group, "group");
    Utils.notNull(group.getName(), "group.name");

    if (!group.getShortName().matches(GroupsManager.GROUP_SHORT_NAME_REGEXP)) {
      throw new InternalErrorException(new IllegalArgumentException("Wrong group shortName, group shortName must matches " + GroupsManager.GROUP_SHORT_NAME_REGEXP));
    }

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "updateGroup");
    }

    return getGroupsManagerBl().updateGroup(sess, group);
  }

  public Group getGroupById(PerunSession sess, int id) throws GroupNotExistsException, InternalErrorException, PrivilegeException {
    Utils.checkPerunSession(sess);

    Group group = getGroupsManagerBl().getGroupById(sess, id);
    
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
        !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
        !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
        !AuthzResolver.isAuthorized(sess, Role.RPC)) {
      throw new PrivilegeException(sess, "getGroupById");
    }

    return group;
  }

  public Group getGroupByName(PerunSession sess, Vo vo, String name) throws GroupNotExistsException, InternalErrorException, PrivilegeException, VoNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

    Group group = getGroupsManagerBl().getGroupByName(sess, vo, name);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getGroupByName");
    }

    return group;
  }

  public void addMember(PerunSession sess, Group group, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException, AlreadyMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group);
    getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

    //TODO: Really need to check if member's vo.id and vo id are the same?
    Vo vo = getGroupsManagerBl().getVo(sess, group);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "addMember");
    }

    // Check if the member and group are from the same VO
    if (member.getVoId() != (vo.getId())) {
      throw new MembershipMismatchException("Member and group are form the different VO");
    }
    
    getGroupsManagerBl().addMember(sess, group, member);
  }

  public void removeMember(PerunSession sess, Group group, Member member) throws InternalErrorException, MemberNotExistsException, NotGroupMemberException, PrivilegeException, GroupNotExistsException { 
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group);
    getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "removeMember");
    }

    getGroupsManagerBl().removeMember(sess, group, member);
  }

  public List<Member> getGroupMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group);   

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) 
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getGroupMembers");
    }

    return getGroupsManagerBl().getGroupMembers(sess, group);
  }
  
  public List<Member> getGroupMembers(PerunSession sess, Group group, Status status) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group);   

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getGroupMembers");
    }

    return getGroupsManagerBl().getGroupMembers(sess, group, status);
  }
  
  public List<RichMember> getGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group);   

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getGroupRichMembers");
    }

    return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembers(sess, group));
  }
  
  public List<RichMember> getGroupRichMembers(PerunSession sess, Group group, Status status) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group);   

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getGroupRichMembers");
    }

    return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembers(sess, group, status));
  }
  
  public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group);   

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) 
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getGroupRichMembersWithAttributes");
    }

    return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembersWithAttributes(sess, group));
  }
  
  public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group, Status status) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group);   

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getGroupRichMembersWithAttributes");
    }
    return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembersWithAttributes(sess, group, status));
  }

  public int getGroupMembersCount(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getGroupMembersCount");
    }

    return getGroupsManagerBl().getGroupMembersCount(sess, group);
  }

  public void addAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, AlreadyAdminException, PrivilegeException, GroupNotExistsException, UserNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group); 
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "addAdmin");
    }

    getGroupsManagerBl().addAdmin(sess, group, user);
  }
  
  @Override
   public void addAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, AlreadyAdminException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group); 
    getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
        
      throw new PrivilegeException(sess, "addAdmin");
    }

    getGroupsManagerBl().addAdmin(sess, group, authorizedGroup);
  }

  public void removeAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotAdminException, UserNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group); 
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "removeAdmin");
    }

    getGroupsManagerBl().removeAdmin(sess, group, user);
  }
  
  @Override
   public void removeAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, GroupNotAdminException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group); 
    getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "removeAdmin");
    }

    getGroupsManagerBl().removeAdmin(sess, group, authorizedGroup);
  }

  public List<User> getAdmins(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group); 

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getAdmins");
    }

    return getGroupsManagerBl().getAdmins(sess, group);
  }
  
  @Override
   public List<User> getDirectAdmins(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group); 

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getDirectAdmins");
    }

    return getGroupsManagerBl().getDirectAdmins(sess, group);
  }
  
  public List<Group> getAdminGroups(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getAdminGroups");
    }

    return getGroupsManagerBl().getAdminGroups(sess, group);
  }
  
  public List<RichUser> getRichAdmins(PerunSession perunSession, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException {
    Utils.checkPerunSession(perunSession);
    getGroupsManagerBl().checkGroupExists(perunSession, group); 

    // Authorization
    if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group) 
        && !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(perunSession, "getRichAdmins");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getRichAdmins(perunSession, group));  
  }
  
  public List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException {
    Utils.checkPerunSession(perunSession);
    getGroupsManagerBl().checkGroupExists(perunSession, group); 

    // Authorization
    if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(perunSession, "getRichAdminsWithAttributes");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getRichAdminsWithAttributes(perunSession, group));    
  }
  
  public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException {
    Utils.checkPerunSession(perunSession);
    getGroupsManagerBl().checkGroupExists(perunSession, group); 

    // Authorization
    if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(perunSession, "getRichAdminsWithSpecificAttributes");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getRichAdminsWithSpecificAttributes(perunSession, group, specificAttributes));    
  }  
  
  public List<Group> getAllGroups(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
      throw new PrivilegeException(sess, "getAllGroups");
    }

    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);


    List<Group> groups = getGroupsManagerBl().getAllGroups(sess, vo);
    
    // Return all groups for VOADMIN and PERUNADMIN
    if (AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
            || AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
            || AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.PERUNADMIN)) {
      return groups;
    } 
    
    // Check access rights for each group for GROUPADMIN
    if (AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.GROUPADMIN)) {
      Iterator<Group> i = groups.iterator();
      while (i.hasNext()) {
        if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, i.next())) {
          i.remove();
        }
      }
      return groups;
    }
    
    // This shouldn't happen
    throw new PrivilegeException(sess, "getAllGroups");
  }

  public Map<Group, Object> getAllGroupsWithHierarchy(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
      throw new PrivilegeException(sess, "getAllGroupsWithHierarchy");
    }

    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);


    Map<Group, Object> groups =  getGroupsManagerBl().getAllGroupsWithHierarchy(sess, vo);
    
    // Return all groups for VOADMIN and PERUNADMIN
    if (AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) 
            || AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.PERUNADMIN)
            || AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
      return groups;
    } 
    
    // Check access rights for each group for GROUPADMIN
    if (AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.GROUPADMIN)) {
      Iterator<Group> i = groups.keySet().iterator();
      while (i.hasNext()) {
        if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, i.next())) {
          i.remove();
        }
      }
      return groups;
    }
    
    // This shouldn't happen
    throw new PrivilegeException(sess, "getAllGroupsWithHierarchy");
  }

  public List<Group> getSubGroups(PerunSession sess, Group parentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, parentGroup); 

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, parentGroup)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, parentGroup)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, parentGroup)) {
      throw new PrivilegeException(sess, "getSubGroups");
    }

    return getGroupsManagerBl().getSubGroups(sess, parentGroup);
  }

  public List<Group> getAllSubGroups(PerunSession sess, Group parentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, parentGroup); 

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, parentGroup)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, parentGroup)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, parentGroup)) {
      throw new PrivilegeException(sess, "getAllSubGroups");
    }

    return getGroupsManagerBl().getAllSubGroups(sess, parentGroup);
  }
  
  public Group getParentGroup(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group); 
    
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getParentGroup");
    }

    return getGroupsManagerBl().getParentGroup(sess, group);
  }

  public List<Group> getGroups(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
        !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
      throw new PrivilegeException(sess, "getGroups");
    }

    List<Group> groups =  getGroupsManagerBl().getGroups(sess, vo);
    
    // Return all groups for VOADMIN and PERUNADMIN
    if (AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) 
            || AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.PERUNADMIN)
            || AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
      return groups;
    } 
    
    // Check access rights for each group for GROUPADMIN
    if (AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.GROUPADMIN)) {
      Iterator<Group> i = groups.iterator();
      while (i.hasNext()) {
        if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, i.next())) {
          i.remove();
        }
      }
      return groups;
    }
    
    // This shouldn't happen
    throw new PrivilegeException(sess, "getGroups");
  }

  public int getGroupsCount(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
      throw new PrivilegeException(sess, "getGroupsCount");
    }

    return getGroupsManagerBl().getGroupsCount(sess, vo);
  }

  public int getSubGroupsCount(PerunSession sess, Group parentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, parentGroup); 

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, parentGroup) 
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, parentGroup)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, parentGroup)) {
      throw new PrivilegeException(sess, "getSubGroupsCount for " + parentGroup.getName());
    }

    return getGroupsManagerBl().getSubGroupsCount(sess, parentGroup);
  }

  public Vo getVo(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group); 

    Vo vo =  getGroupsManagerBl().getVo(sess, group);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) 
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getVo");
    }

    return vo;
  }

  public List<Member> getParentGroupMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group); 

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) 
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getParentGroupMembers for " + group.getName());
    }

    return getGroupsManagerBl().getParentGroupMembers(sess, group);
  }
  
  public List<RichMember> getParentGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group); 

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) 
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getParentGroupRichMembers for " + group.getName());
    }

    return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getParentGroupRichMembers(sess, group));
  }
  
  public List<RichMember> getParentGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group); 

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) 
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
      throw new PrivilegeException(sess, "getParentGroupRichMembers for " + group.getName());
    }

    return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getParentGroupRichMembersWithAttributes(sess, group));
  }


  /**
   * Gets the groupsManagerBl for this instance.
   *
   * @return The groupsManagerBl.
   */
  public GroupsManagerBl getGroupsManagerBl() {
    return this.groupsManagerBl;
  }

  /**
   * Sets the perunBl for this instance.
   *
   * @param perunBl The perunBl.
   */
  public void setPerunBl(PerunBl perunBl)
  {
    this.perunBl = perunBl;
  }

  /**
   * Sets the groupsManagerBl for this instance.
   *
   * @param groupsManagerBl The groupsManagerBl.
   */
  public void setGroupsManagerBl(GroupsManagerBl groupsManagerBl)
  {
    this.groupsManagerBl = groupsManagerBl;
  }

  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  public void forceGroupSynchronization(PerunSession sess, Group group) throws InternalErrorException, 
    GroupNotExistsException, PrivilegeException, GroupSynchronizationAlreadyRunningException {
    Utils.checkPerunSession(sess);
    getGroupsManagerBl().checkGroupExists(sess, group); 

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.SYNCHRONIZER))  {
      throw new PrivilegeException(sess, "synchronizeGroup");
    }

    getGroupsManagerBl().forceGroupSynchronization(sess, group);
  }

  public void synchronizeGroups(PerunSession sess) throws InternalErrorException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.SYNCHRONIZER))  {
      throw new PrivilegeException(sess, "synchronizeGroups");
    }

    getGroupsManagerBl().synchronizeGroups(sess);
  }

  public List<Group> getMemberGroups(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

    Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)
        && !AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
      throw new PrivilegeException(sess, "getMemberGroups for " + member);
    }

    //Remove members and administrators groups
    List<Group> groups = getGroupsManagerBl().getMemberGroups(sess, member);
    if(!groups.isEmpty()) {
        Iterator<Group> iterator = groups.iterator();
        while(iterator.hasNext()) {
            Group g = iterator.next();
            if(g.getName().equals(VosManager.MEMBERS_GROUP)) iterator.remove();
        }
    }
    
    return groups;
  }
  
  public List<Group> getAllMemberGroups(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

    Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) 
        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
        && !AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
      throw new PrivilegeException(sess, "getAllMemberGroups for " + member);
    }

    return getGroupsManagerBl().getAllMemberGroups(sess, member);
  }  
}
