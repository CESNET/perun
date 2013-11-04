package cz.metacentrum.perun.ldapc.processor;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.util.List;
import java.util.Map;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

public interface LdapConnector {
  
  //---------------------GROUP METHODS------------------------------------------  
  /**
   * Add group to LDAP.
   * 
   * @param group group from Perun
   * 
   * @throws InternalErrorException if NameNotFoundException is thrown
   */
  public void addGroup(Group group) throws InternalErrorException;

  /**
   * Remove group from LDAP
   * 
   * @param group group from Perun 
   * @throws InternalErrorException if NameNotFoundException is thrown
   */
  public void removeGroup(Group group) throws InternalErrorException;

  /**
   * Update group in LDAP
   * 
   * @param group new state of group in perun
   * @param modificationItems attributes of groups which need to be modified
   */
  public void updateGroup(Group group, ModificationItem[] modificationItems);
  
  /**
   * The same behavior like method 'addGroup'.
   * Only call method 'addGroup' with group
   * 
   * @param group group from Perun (then call addGroup(group) )
   * @param parentGroup (is not used now, can be null) IMPORTANT
   * 
   * @throws InternalErrorException if NameNotFoundException is thrown
   */
  public void addGroupAsSubGroup(Group group, Group parentGroup) throws InternalErrorException;
  
  //-----------------------------MEMBER METHODS---------------------------------
  /**
   * Add member to group in LDAP. 
   * It means add attribute to member and add attribute to group. 
   * If this group is 'members' group, add member attribute to the vo (of group) too.
   * 
   * @param member the member
   * @param group the group
   * 
   * @throws InternalErrorException if NameNotFoundException is thrown
   */
  public void addMemberToGroup(Member member, Group group) throws InternalErrorException;

  /**
   * Remove member from group in LDAP.
   * It means remove attribute from member and remove attribute from group.
   * If this group is 'member' group, remove member attribute for vo (of group) too.
   * 
   * @param member
   * @param group
   * @throws InternalErrorException if NameNotFoundException is thrown
   */
  public void removeMemberFromGroup(Member member, Group group) throws InternalErrorException;
  
  /**
   * Return true if member has already attribute 'memberOf' for this group in LDAP
   * 
   * @param member the member
   * @param group the group
   * @return true if attribute 'memberOf' exists for this group, false if not
   */
  public boolean isAlreadyMember(Member member, Group group);
  
  /**
   * Get all 'uniqueMember' values of group in LDAP.
   * 
   * @param groupId group Id
   * @param voId vo Id
   * @return list of uniqueMember values
   */
  public List<String> getAllUniqueMembersInGroup(int groupId, int voId);
  
  //-----------------------------VO METHODS-------------------------------------
  /**
   * Create vo in LDAP.
   * 
   * @param vo the vo
   * @throws InternalErrorException if NameNotFoundException is thrown
   */
  public void createVo(Vo vo) throws InternalErrorException;

  /**
   * Delete existing vo in LDAP.
   * 
   * @param vo the vo
   * @throws InternalErrorException if NameNotFoundException is thrown
   */
  public void deleteVo(Vo vo) throws InternalErrorException;
  
  /**
   * Update existing vo in LDAP. 
   * Get id from object vo.
   * 
   * @param vo the vo
   * @param modificationItems list of attribute which need to be modified
   */
  public void updateVo(Vo vo, ModificationItem[] modificationItems);
  
  /**
   * Update existing vo in LDAP.
   * Use id instead of whole object.
   * 
   * @param voId vo id
   * @param modificationItems list of attributes which need to be modified
   */
  public void updateVo(int voId, ModificationItem[] modificationItems);
  
  /**
   * Find Vo in LDAP and return shortName of this Vo.
   * 
   * @param voId vo id
   * 
   * @return shortName of vo with vo id
   * @throws InternalErrorException if shortName has not right format (null, not exists, 0 length, more than 1 shortName exist)
   */
  public String getVoShortName(int voId) throws InternalErrorException;
  
  //----------------------------USER METHODS------------------------------------
  /**
   * Create user in ldap.
   * 
   * @param user user from perun
   * @throws InternalErrorException if NameNotFoundException occurs 
   */
  public void createUser(User user) throws InternalErrorException;
  
  /**
   * Update existing user in ldap.
   * 
   * @param user user from perun
   * @param modificationItems list of attributes which need to be modified
   */
  public void updateUser(User user, ModificationItem[] modificationItems);
  
  /**
   * Update existing user in ldap.
   * 
   * @param userId use id instead of whole object user
   * @param modificationItems list of attributes which need to be modified
   */
  public void updateUserWithUserId(String userId, ModificationItem[] modificationItems);
  
  /**
   * Delete existing user from ldap.
   * IMPORTANT Don't need delete members of deleting user from groups, it will depend on messages removeFrom Group
   * 
   * @param user
   * @throws InternalErrorException 
   */
  public void deleteUser(User user) throws InternalErrorException;
  
  /**
   * Update all values of user cert subject in ldap.
   * 
   * @param userId user id
   * @param certSubjects values of cert subjects
   */
  public void updateUsersCertSubjects(String userId, String[] certSubjects);
  
  /**
   * Return true if user already exists in ldap.
   * 
   * @param user user in perun
   * @return true if user already exists in ldap, false if not
   */
  public boolean userExist(User user);
  
  /**
   * Return true if user attribute with ldapAttributeName in ldap exists.
   * 
   * @param user user in perun
   * @param ldapAttributeName name of user ldap attribute
   * @return true if attribute in ldap exists, false if not
   * @throws InternalErrorException if ldapAttributeName is null
   */
  public boolean userAttributeExist(User user, String ldapAttributeName) throws InternalErrorException;
  
  /**
   * Return true if user attribute 'password' in ldap already exists.
   * 
   * @param user user in perun
   * @return true if password in ldap exists for user, false if note
   */
  public boolean userPasswordExists(User user);
  
  /**
   * Get all ldapAttributes of user
   * 
   * @param user user in perun
   * @return all attribute of user in ldap
   * @throws InternalErrorException 
   */
  public Attributes getAllUsersAttributes(User user);
}
