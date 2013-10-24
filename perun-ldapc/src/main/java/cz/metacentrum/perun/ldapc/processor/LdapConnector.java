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
  //GROUP METHODS
  void addGroup(Group group) throws InternalErrorException;

  void removeGroup(Group group) throws InternalErrorException;

  void updateGroup(Group group, ModificationItem[] modificationItems) throws InternalErrorException;
  
  void addGroupAsSubGroup(Group group, Group parentGroup) throws InternalErrorException;
  
  List<String> getAllUniqueMembersInGroup(int groupId, int voId) throws InternalErrorException;
  
  //MEMBER METHODS
  void addMemberToGroup(Member member, Group group) throws InternalErrorException;

  void removeMemberFromGroup(Member member, Group group) throws InternalErrorException;
  
  boolean isAlreadyMember(Member member, Group group) throws InternalErrorException;
  
  //USER METHODS
  void createUser(User user) throws InternalErrorException;
  
  void updateUser(User user, ModificationItem[] modificationItems) throws InternalErrorException;
  
  void updateUserWithUserId(String userId, ModificationItem[] modificationItems) throws InternalErrorException;
  
  void deleteUser(User user) throws InternalErrorException;
  
  boolean userExist(User user) throws InternalErrorException;

  void updateUsersCertSubjects(String userId, String[] certSubjects) throws InternalErrorException;
  
  boolean userAttributeExist(User user, String ldapAttributeName) throws InternalErrorException;
  
  boolean userPasswordExists(User user) throws InternalErrorException;
  
  Attributes getAllUsersAttributes(User user) throws InternalErrorException;
          
  //VO METHODS
  void createVo(Vo vo) throws InternalErrorException;

  void deleteVo(Vo vo) throws InternalErrorException;
  
  void updateVo(Vo vo, ModificationItem[] modificationItems) throws InternalErrorException;
  
  void updateVo(int voId, ModificationItem[] modificationItems) throws InternalErrorException;
  
  String getVoShortName(int voId) throws InternalErrorException;
}
