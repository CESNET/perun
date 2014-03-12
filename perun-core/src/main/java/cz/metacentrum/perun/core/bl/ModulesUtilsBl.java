package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.List;
import java.util.Set;

/**
 * @author Michal Stava <stavamichal@gmail.com>
 * 
 * ModuleUtils interface.
 * There are methods for help with modules.
 * 
 */
public interface ModulesUtilsBl {
    
  /**
   * If attribute "def_facility_unixGroup_namespace" is "null" return false.
   * If value of attribute "def_facility_unixGroup_namespace" is not same like "namespace", return false.
   * Else return true.
   *
   * Facility, sess and namespace can't be null, otherwise throw InternalErrorException
   * 
   * @param sess
   * @param facility
   * @param namespace
   * @return
   * @throws InternalErrorException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeAssignmentException 
   */
  boolean isNamespaceEqualsToFacilityUnixGroupNameNamespace(PerunSessionImpl sess, Facility facility, String namespace) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

  /**
   * This method get if the resource has the same attribute "attr" with the same namespace and same or different values
   * 
   * If return 0 then there exists for the resource the same attribute with the same value 
   * if return more than 0 then there exists for the resource the same attribute with different value
   * if return less than 0 then there not exists for the resource the same attribute
   *
   * @param sess
   * @param resource
   * @param attr any resource attribute with namespace which will be use for comparing
   * @return
   * @throws InternalErrorException if something is not correct
   * @throws WrongAttributeAssignmentException if attribute name is not RESOURCE attribute
   */
  int haveTheSameAttributeWithTheSameNamespace(PerunSessionImpl sess, Resource resource, Attribute attr) throws InternalErrorException, WrongAttributeAssignmentException;
  
  /**
   * This method get if the group has the same attribute "attr" with the same namespace and same or different values
   * 
   * If return 0 then there exists for the group the same attribute with the same value 
   * if return more than 0 then there exists for the group the same attribute with different value
   * if return less than 0 then there not exists for the group the same attribute
   *
   * @param sess
   * @param group
   * @param attr any group attribute with namespace which will be use for comparing
   * @return
   * @throws InternalErrorException if something is not correct
   * @throws WrongAttributeAssignmentException if attribute name is not GROUP attribute
   */
  int haveTheSameAttributeWithTheSameNamespace(PerunSessionImpl sess, Group group, Attribute attr) throws InternalErrorException, WrongAttributeAssignmentException;

  /**
   * If there are commong GID in the list of resources, get it. 
   * If there is some collision (more than one have the other GID in the same namespace) throw exception
   * 
   * @param sess
   * @param resourcesWithSameGroupNameInSameNamespace
   * @param nameOfAttribute
   * @param commonGID can be null if no commonGID exists
   * @return common GID, if no exists return null
   * 
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException 
   */
  Integer getCommonGIDOfResourcesWithSameNameInSameNamespace(PerunSessionImpl sess, List<Resource> resourcesWithSameGroupNameInSameNamespace, String nameOfAttribute, Integer commonGID) throws InternalErrorException, WrongAttributeAssignmentException;

  /**
   * If there are commong GID in the list of groups, get it. 
   * If there is some collision (more than one have the other GID in the same namespace) throw exception
   * 
   * @param sess
   * @param groupsWithSameGroupNameInSameNamespace
   * @param nameOfAttribute
   * @param commonGID can bu null if no commonGID exists
   * @return common GID, if no exists return null
   * 
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException 
   */
  Integer getCommonGIDOfGroupsWithSameNameInSameNamespace(PerunSessionImpl sess, List<Group> groupsWithSameGroupNameInSameNamespace, String nameOfAttribute, Integer commonGID) throws InternalErrorException, WrongAttributeAssignmentException;
  
  /**
   * Get free gid for resource or group.
   * 
   * @param sess
   * @param attribute group or resource unixGID-namespace attribute
   * @return if 0 there probably isn't maxGID or minGID, if null there is no free gid, other less or more than 0 gid
   * 
   * @throws InternalErrorException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeAssignmentException 
   */
  Integer getFreeGID(PerunSessionImpl sess, Attribute attribute) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;
  
  /**
   * Check if gid in arguments is free in the namespace
   * 
   * @param sess
   * @param attribute group or resource unixGID-namespace attribute with value
   * 
   * @throws InternalErrorException
   * @throws WrongReferenceAttributeValueException if minGid or maxGid is null
   * @throws WrongAttributeAssignmentException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeValueException 
   */
  void checkIfGIDIsWithinRange(PerunSessionImpl sess, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongAttributeValueException;  

  /**
   * Check if list of gids in arguments is free in the namespace
   * 
   * @param sess
   * @param attribute list of unixGIDs-namespace attribute with value
   * @param user handler of atribute
   * 
   * @throws InternalErrorException
   * @throws WrongReferenceAttributeValueException if minGid or maxGid is null
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException 
   */
  void checkIfListOfGIDIsWithinRange(PerunSessionImpl sess,User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException;  
  
  /**
   * Return true if i have right on any of groups or resources to WRITE the attribute
   * 
   * @param sess
   * @param groups lists of groups to search
   * @param resources lists of resources to search
   * @param groupAttribute AttributeDefinition for testing Write privileges for groups
   * @param resourceAttribute AttributeDefinition for testing Write privileges for resource
   * 
   * @return true if such group or resource exists, false if not
   * @throws InternalErrorException if something is not correct or attribute is null
   */
   boolean haveRightToWriteAttributeInAnyGroupOrResource(PerunSessionImpl sess, List<Group> groups, List<Resource> resources, AttributeDefinition groupAttribute, AttributeDefinition resourceAttribute) throws InternalErrorException;

   /**
    * Take list of groupGID attributes and return list of the same GID attributes only for resource (with the same original value)
    * 
    * @param sess
    * @param groupGIDs list of attributes type of Group UnixGID
    * @return list of attribute type of Resource UnixGID with same values like in original list
    * @throws InternalErrorException if something is not correct or attribute is null
    * @throws AttributeNotExistsException 
    */
   List<Attribute> getListOfResourceGIDsFromListOfGroupGIDs(PerunSessionImpl sess, List<Attribute> groupGIDs) throws InternalErrorException, AttributeNotExistsException;

   /**
    * Take list of resourceGID attributes and return list of the same GID attributes only for group (with the same original value)
    * 
    * @param sess
    * @param resourceGIDs list of attributes type of Resource UnixGID
    * @return list of attribute type of Group UnixGID with same values like in original list
    * @throws InternalErrorException if something is not correct or attribute is null
    * @throws AttributeNotExistsException 
    */
   List<Attribute> getListOfGroupGIDsFromListOfResourceGIDs(PerunSessionImpl sess, List<Attribute> resourceGIDs) throws InternalErrorException, AttributeNotExistsException;   
   
   /**
    * Get list of facilities and namespace of group or resource attribute unixGroupName-namespace and 
    * if any facility has unixGroupName-namespace with same value like this namespace of unixGroupNameNamespace attribute
    * then get unixGID-namespace of this facility and save it to the hashSet of these namespaces.
    * 
    * @param sess
    * @param facilities list of facilities 
    * @param unixGroupNameNamespace unixGroupName-namespace attribute
    * @return list of namespaces
    * @throws InternalErrorException
    * @throws WrongAttributeAssignmentException
    * @throws WrongReferenceAttributeValueException 
    */
   Set<String> getSetOfGIDNamespacesWhereFacilitiesHasTheSameGroupNameNamespace(PerunSessionImpl sess, List<Facility> facilities, Attribute unixGroupNameNamespace) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

   /**
    * Get list of facilities and namespace of group or resource attribute unixGID-namespace and 
    * if any facility has unixGID-namespace with same value like this namespace of unixGIDNamespace attribute
    * then get unixGroupName-namespace of this facility and save it to the hashSet of these namespaces.
    * 
    * @param sess
    * @param facilities list of facilities 
    * @param unixGroupNameNamespace unixGroupName-namespace attribute
    * @return list of namespaces
    * @throws InternalErrorException
    * @throws WrongAttributeAssignmentException
    * @throws WrongReferenceAttributeValueException 
    */
   Set<String> getSetOfGroupNameNamespacesWhereFacilitiesHasTheSameGIDNamespace(PerunSessionImpl sess, List<Facility> facilities, Attribute unixGroupNameNamespace) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

   /**
    * Check if value of groupName attribute is not reserved String.
    * If not, its ok.
    * If yes, throw WrongAttributeValueException.
    * If attribute is null, then its ok.
    * 
    * @param groupName attribute unixGroupName-namespace
    * @throws WrongAttributeValueException 
    */
   void checkReservedNames(Attribute groupName) throws WrongAttributeValueException;
   
   /**
    * Get value of attribute A_F_Def_unixGroupName-Namespace
    * If this value is null, throw WrongReferenceAttributeValueException
    * 
    * @param sess
    * @param resource
    * @return namespace if is not null
    * @throws InternalErrorException
    * @throws WrongReferenceAttributeValueException if value of unixGroupName-namespace attribute is null
    */
   Attribute getUnixGroupNameNamespaceAttributeWithNotNullValue(PerunSessionImpl sess, Resource resource) throws InternalErrorException, WrongReferenceAttributeValueException;
   
   /**
    * Get value of attribute A_F_Def_unixGID-Namespace
    * If this value is null, throw WrongReferenceAttributeValueException
    * 
    * @param sess
    * @param resource
    * @return
    * @throws InternalErrorException
    * @throws WrongReferenceAttributeValueException 
    */
   Attribute getUnixGIDNamespaceAttributeWithNotNullValue(PerunSessionImpl sess, Resource resource) throws InternalErrorException, WrongReferenceAttributeValueException;
   
   /**
    * This method return true if there exists some Facility (get from assigned resources) where is 
    * facility_unixGID-namespace attribute with value same like group_unixGID-namespace namespace and
    * if the group has unixGroupName-namespace with notNull value in the same namespace like value of attribute
    * facility_unixGroupName-namespace.
    * Return false if not.
    * 
    * @param sess
    * @param group the group
    * @param groupUnixGIDNamespace attribute of the group
    * @return
    * @throws InternalErrorException
    * @throws WrongReferenceAttributeValueException
    * @throws WrongAttributeAssignmentException 
    */
   boolean isGroupUnixGIDNamespaceFillable(PerunSessionImpl sess, Group group, Attribute groupUnixGIDNamespace) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;
   
   /**
    * Check if shell has the right format.
    * Use regex ^(/[-_a-zA-Z0-9]+)+$
    *
    * @param shell value of shell
    * @param attribute attribute which need to test shell (needed for right exception)
    * @throws WrongAttributeValueException if shell has bad format
    */
   void checkFormatOfShell(String shell, Attribute attribute) throws WrongAttributeValueException;

   /**
    * Checks name of an email by standard pattern and returns true, if it is valid.
    * 
    * @param sess
    * @param email name of the email
    * 
    * @return true if the name of email is valid
    */
   boolean isNameOfEmailValid(PerunSessionImpl sess, String email);
}
