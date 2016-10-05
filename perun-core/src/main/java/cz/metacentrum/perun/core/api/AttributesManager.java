package cz.metacentrum.perun.core.api;

import java.util.HashMap;
import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.*;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public interface AttributesManager {

	public static final String NS_FACILITY_ATTR= "urn:perun:facility:attribute-def";
	public static final String NS_FACILITY_ATTR_DEF= "urn:perun:facility:attribute-def:def";
	public static final String NS_FACILITY_ATTR_OPT = "urn:perun:facility:attribute-def:opt";
	public static final String NS_FACILITY_ATTR_CORE = "urn:perun:facility:attribute-def:core";
	public static final String NS_FACILITY_ATTR_VIRT = "urn:perun:facility:attribute-def:virt";

	public static final String NS_FACILITY_CLUSTER_ATTR_DEF= "urn:perun:facility:cluster:attribute-def:def";
	public static final String NS_FACILITY_CLUSTER_ATTR_OPT = "urn:perun:facility:cluster:attribute-def:opt";
	public static final String NS_FACILITY_CLUSTER_ATTR_CORE = "urn:perun:facility:cluster:attribute-def:core";
	public static final String NS_FACILITY_CLUSTER_ATTR_VIRT = "urn:perun:facility:cluster:attribute-def:virt";

	public static final String NS_RESOURCE_ATTR = "urn:perun:resource:attribute-def";
	public static final String NS_RESOURCE_ATTR_DEF = "urn:perun:resource:attribute-def:def";
	public static final String NS_RESOURCE_ATTR_OPT = "urn:perun:resource:attribute-def:opt";
	public static final String NS_RESOURCE_ATTR_CORE = "urn:perun:resource:attribute-def:core";
	public static final String NS_RESOURCE_ATTR_VIRT = "urn:perun:resource:attribute-def:virt";

	public static final String NS_MEMBER_RESOURCE_ATTR = "urn:perun:member_resource:attribute-def";
	public static final String NS_MEMBER_RESOURCE_ATTR_DEF = "urn:perun:member_resource:attribute-def:def";
	public static final String NS_MEMBER_RESOURCE_ATTR_OPT = "urn:perun:member_resource:attribute-def:opt";
	public static final String NS_MEMBER_RESOURCE_ATTR_VIRT = "urn:perun:member_resource:attribute-def:virt";

	public static final String NS_MEMBER_GROUP_ATTR = "urn:perun:member_group:attribute-def";
	public static final String NS_MEMBER_GROUP_ATTR_DEF = "urn:perun:member_group:attribute-def:def";
	public static final String NS_MEMBER_GROUP_ATTR_OPT = "urn:perun:member_group:attribute-def:opt";
	public static final String NS_MEMBER_GROUP_ATTR_VIRT = "urn:perun:member_group:attribute-def:virt";

	public static final String NS_MEMBER_ATTR_CORE = "urn:perun:member:attribute-def:core";
	public static final String NS_MEMBER_ATTR = "urn:perun:member:attribute-def";
	public static final String NS_MEMBER_ATTR_DEF = "urn:perun:member:attribute-def:def";
	public static final String NS_MEMBER_ATTR_OPT = "urn:perun:member:attribute-def:opt";
	public static final String NS_MEMBER_ATTR_VIRT = "urn:perun:member:attribute-def:virt";

	public static final String NS_USER_FACILITY_ATTR = "urn:perun:user_facility:attribute-def";
	public static final String NS_USER_FACILITY_ATTR_DEF = "urn:perun:user_facility:attribute-def:def";
	public static final String NS_USER_FACILITY_ATTR_OPT = "urn:perun:user_facility:attribute-def:opt";
	public static final String NS_USER_FACILITY_ATTR_VIRT = "urn:perun:user_facility:attribute-def:virt";

	public static final String NS_USER_ATTR = "urn:perun:user:attribute-def";
	public static final String NS_USER_ATTR_CORE = "urn:perun:user:attribute-def:core";
	public static final String NS_USER_ATTR_DEF = "urn:perun:user:attribute-def:def";
	public static final String NS_USER_ATTR_OPT = "urn:perun:user:attribute-def:opt";
	public static final String NS_USER_ATTR_VIRT = "urn:perun:user:attribute-def:virt";

	public static final String NS_VO_ATTR= "urn:perun:vo:attribute-def";
	public static final String NS_VO_ATTR_DEF= "urn:perun:vo:attribute-def:def";
	public static final String NS_VO_ATTR_OPT = "urn:perun:vo:attribute-def:opt";
	public static final String NS_VO_ATTR_CORE = "urn:perun:vo:attribute-def:core";
	public static final String NS_VO_ATTR_VIRT = "urn:perun:vo:attribute-def:virt";

	public static final String NS_GROUP_ATTR= "urn:perun:group:attribute-def";
	public static final String NS_GROUP_ATTR_DEF= "urn:perun:group:attribute-def:def";
	public static final String NS_GROUP_ATTR_OPT = "urn:perun:group:attribute-def:opt";
	public static final String NS_GROUP_ATTR_CORE = "urn:perun:group:attribute-def:core";
	public static final String NS_GROUP_ATTR_VIRT = "urn:perun:group:attribute-def:virt";

	public static final String NS_HOST_ATTR = "urn:perun:host:attribute-def";
	public static final String NS_HOST_ATTR_CORE = "urn:perun:host:attribute-def:core";
	public static final String NS_HOST_ATTR_DEF = "urn:perun:host:attribute-def:def";
	public static final String NS_HOST_ATTR_OPT = "urn:perun:host:attribute-def:opt";
	public static final String NS_HOST_ATTR_VIRT = "urn:perun:host:attribute-def:virt";

	public static final String NS_GROUP_RESOURCE_ATTR = "urn:perun:group_resource:attribute-def";
	public static final String NS_GROUP_RESOURCE_ATTR_DEF = "urn:perun:group_resource:attribute-def:def";
	public static final String NS_GROUP_RESOURCE_ATTR_OPT = "urn:perun:group_resource:attribute-def:opt";
	public static final String NS_GROUP_RESOURCE_ATTR_VIRT = "urn:perun:group_resource:attribute-def:virt";

	public static final String NS_ENTITYLESS_ATTR = "urn:perun:entityless:attribute-def";
	public static final String NS_ENTITYLESS_ATTR_DEF = "urn:perun:entityless:attribute-def:def";
	public static final String NS_ENTITYLESS_ATTR_OPT = "urn:perun:entityless:attribute-def:opt";

	public static final String NS_UES_ATTR = "urn:perun:ues:attribute-def";
	public static final String NS_UES_ATTR_CORE = "urn:perun:ues:attribute-def:core";
	public static final String NS_UES_ATTR_DEF = "urn:perun:ues:attribute-def:def";
	public static final String NS_UES_ATTR_OPT = "urn:perun:ues:attribute-def:opt";
	public static final String NS_UES_ATTR_VIRT = "urn:perun:ues:attribute-def:virt";

	public static final String ENTITY_MEMBER = "member";
	public static final String ENTITY_USER = "user";

	public static final String LOGIN_NAMESPACE = "login-namespace";

	public static final String ATTRIBUTES_REGEXP = "^[-a-zA-Z0-9.]+([:][-a-zA-Z0-9.]+)?$";

	//public static final String NS_ATTR_DEF_FACILITY= NS_ATTR_DEF + ":facility";

	/**
	 * Get all <b>non-empty</b> attributes associated with the facility.
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param facility facility to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in underlying data source
	 */
	List<Attribute> getAttributes(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException, InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the vo.
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param vo vo to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws VoNotExistsException if the vo doesn't exists in underlying data source
	 */
	List<Attribute> getAttributes(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException, InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the group.
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param group group to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the group doesn't exists in underlying data source
	 */
	List<Attribute> getAttributes(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException, InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the resource.
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param resource resource to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 */
	List<Attribute> getAttributes(PerunSession sess, Resource resource) throws PrivilegeException, ResourceNotExistsException, InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the member on the resource.
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param resource to get the attributes from
	 * @param member to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getAttributes(PerunSession sess, Resource resource, Member member) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Gets all <b>non-empty</b> attributes associated with the member on the resource and if workWithUserAttributes is
	 * true, gets also all <b>non-empty</b> user, user-facility and member attributes.
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param resource to get the attributes from
	 * @param member to get the attributes from
	 * @param workWithUserAttributes if true returns also user-facility, user and member attributes (user is automatically get from member a facility is get from resource)
	 * @return list of attributes
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 * @throws WrongAttributeAssignmentException
	 *
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	List<Attribute> getAttributes(PerunSession sess, Resource resource, Member member, boolean workWithUserAttributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the member in the group.
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param group group to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the group doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, Group group) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get all attributes (empty and virtual too)associated with the member in the group which have name in list attrNames.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param group group to get the attributes from
	 * @param attrNames list of attributes' names
	 * @return list of attributes
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the group doesn't exists in underlying data source
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get all attributes associated with the member in the group which have name in list attrNames (empty too).
	 * If workWithUserAttribute is true, return also all user attributes in list of attrNames (with virtual attributes too).
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param group group to get the attributes from
	 * @param attrNames list of attributes' names
	 * @param workWithUserAttributes if true returns also user and member attributes (user is automatically get from member)
	 * @return list of attributes
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the group doesn't exists in underlying data source
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames, boolean workWithUserAttributes) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get all entityless attributes with subject equaled String key
	 *
	 * PRIVILEGE: Only PerunAdmin can get Entityless attributes.
	 *
	 * @param sess perun session
	 * @param key string of subject of entityless attributes
	 * @return
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, String key) throws PrivilegeException, InternalErrorException;

	/**
	 * Get all entityless attributes with attributeName
	 *
	 * PRIVILEGE: Only PerunAdmin has access to entityless attributes.
	 *
	 * @param sess perun session
	 * @param attrName
	 * @return attribute
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getEntitylessAttributes(PerunSession sess, String attrName) throws PrivilegeException, InternalErrorException;

	/**
	 * Get all <b>non-empty</b> member, user, member-resource and user-facility attributes.
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess
	 * @param facility
	 * @param resource
	 * @param user
	 * @param member
	 * @return
	 *
	 * @throws PrivilegeException
	 * @throws ResourceNotExistsException
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws ResourceNotExistsException
	 */
	List<Attribute> getAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the member.
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member) throws PrivilegeException, InternalErrorException, MemberNotExistsException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the group and resource.
	 * Virtual attribute too.
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * If workWithGroupAttributes is true, return also all group attributes in list of attrNames (with virtual attributes too).
	 *
	 * @param sess perun session
	 * @param resource to get the attributes from
	 * @param group to get the attributes from
	 * @param workWithGroupAttributes if group attributes need to be return too
	 * @return list of attributes
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ResourceNotExistsException if the resource doesn't exist
	 * @throws GroupNotExistsException if the group doesn't exist
	 * @throws GroupResourceMismatchException if group and resource are from the same vo
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, boolean workWithGroupAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException;


	/**
	 * Get all <b>non-empty</b> attributes associated with the group starts with name startPartOfName.
	 * Get only nonvirtual attributes with notNull value.
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param group to get the attributes from
	 * @param startPartOfName attribute name start with this part
	 * @return list of attributes which name start with startPartOfName
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws GroupNotExistsException if the group doesn't exist
	 */
	List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Group group, String startPartOfName) throws PrivilegeException, GroupNotExistsException, InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the resource starts with name startPartOfName.
	 * Get only nonvirtual attributes with notNull value.
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param resource to get the attributes from
	 * @param startPartOfName attribute name start with this part
	 * @return list of attributes which name start with startPartOfName
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ResourceNotExistsException if the resource doesn't exist
	 */
	List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Resource resource, String startPartOfName) throws PrivilegeException, ResourceNotExistsException, InternalErrorException;

	/**
	 * Get all attributes associated with the member which have name in list attrNames (empty too).
	 * Virtual attribute too.
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param attrNames list of attributes' names
	 * @return list of attributes
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames) throws PrivilegeException, InternalErrorException, MemberNotExistsException;

	/**
	 * Get all attributes associated with the group which have name in list attrNames (empty too).
	 * Virtual attribute too.
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param group to get the attributes from
	 * @param attrNames list of attributes' names
	 * @return list of attributes
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws GroupNotExistsException if the group doesn't have access to this resource
	 */
	List<Attribute> getAttributes(PerunSession sess, Group group, List<String> attrNames) throws PrivilegeException, InternalErrorException, GroupNotExistsException;

	/**
	 * Get all attributes associated with the member which have name in list attrNames (empty too)
	 * Virtual attributes too.
	 *
	 * If workWithUserAttribute is true, return also all user attributes in list of attrNames (with virtual attributes too).
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param attrNames  list of attributes' names
	 * @param workWithUserAttributes if user attributes need to be returned too
	 * @return list of member (and also if needed user) attributes
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException;

	/**
	 * Get all attributes associated with the vo which have name in list attrNames (empty too).
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param vo to get the attributes from
	 * @param attrNames list of attributes' names
	 * @return list of attributes
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws VoNotExistsException if the vo doesn't have access to this resource
	 */
	List<Attribute> getAttributes(PerunSession sess, Vo vo, List<String> attrNames) throws PrivilegeException, InternalErrorException, VoNotExistsException;

	/**
	 * Get all attributes associated with the user which have name in list attrNames (empty too).
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param user to get the attributes from
	 * @param attrNames list of attributes' names
	 * @return list of attributes
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws UserNotExistsException if the user doesn't exists
	 */
	List<Attribute> getAttributes(PerunSession sess, User user, List<String> attrNames) throws PrivilegeException, InternalErrorException, UserNotExistsException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the user on the facility.
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param facility to get the attributes from
	 * @param user to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in underlying data source
	 * @throws UserNotExistsException if the user doesn't have access to this facility
	 */
	List<Attribute> getAttributes(PerunSession sess, Facility facility, User user) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, UserNotExistsException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the user.
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param user to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserNotExistsException if the user doesn't have access to this facility
	 */
	List<Attribute> getAttributes(PerunSession sess, User user) throws PrivilegeException, InternalErrorException, UserNotExistsException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the host
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param host host to get attributes from
	 * @return list of attributes
	 * @throws InternalErrorException if an exception raises in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws HostNotExistsException if the host doesn't exist in underlying data source
	 */
	List<Attribute> getAttributes(PerunSession sess, Host host) throws PrivilegeException, InternalErrorException, HostNotExistsException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the group on resource
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param resource
	 * @param group
	 * @return list of group_resource attributes
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raises in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ResourceNotExistsException if the resource doesn't exist in underlying data source
	 * @throws GroupNotExistsException if the group doesn't exist in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException,GroupResourceMismatchException, WrongAttributeAssignmentException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the member and if workWithUserAttributes is
	 * true, get all <b>non-empty</b> attributes associated with user, who is this member.
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param workWithUserAttributes if true returns also user attributes
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException;

	/**
	 * Returns list of Keys which fits the attributeDefinition.
	 *
	 * PRIVILEGE: Only PerunAdmin has access to entityless keys.
	 *
	 * @param sess perun session
	 * @param attributeDefinition
	 * @return
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException if attribute is not facility attribute
	 */
	List<String> getEntitylessKeys(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the UserExtSource.
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
	 *
	 * @param sess perun session
	 * @param ues to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserExtSourceNotExistsException if the user external source doesn't exists
	 */
	List<Attribute> getAttributes(PerunSession sess, UserExtSource ues) throws PrivilegeException, InternalErrorException, UserExtSourceNotExistsException;

	/**
	 * Returns all attributes with not-null value which fits the attributeDefinition. Can't proscess core or virtual attributes.
	 *
	 * PRIVILEGE: Only PerunAdmin has access to get Attributes by AttributeDefinition
	 *
	 * @param sess
	 * @param attributeDefinition
	 * @return list of attributes
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	List<Attribute> getAttributesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Store the attributes associated with the facility. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param facility facility to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Facility facility, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the vo. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param vo vo to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws VoNotExistsException if the vo doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not vo attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Vo vo, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the group. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param group group to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the group doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not group attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the resource. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param resource resource to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not resource attribute
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void setAttributes(PerunSession sess, Resource resource, List<Attribute> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, WrongAttributeValueException;

	/**
	 * Store the attributes associated with the resource and member combination. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param resource resource to set on
	 * @param member member to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the resource and member combination. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 * If workWithUserAttributes is true, the method stores also the attributes associated with user, user-facility and member.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param resource resource to set on
	 * @param member member to set on
	 * @param attributes attribute to set
	 * @param workWithUserAttributes method can process also user, user-facility and member attributes (user is automatically get from member a facility is get from resource)
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 *
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	void setAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the member and group combination. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param group group to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, UserNotExistsException;

	/**
	 * Store the attributes associated with the member and group combination. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 * If workWithUserAttributes is true, the method stores also the attributes associated with user and member.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param group group to set on
	 * @param attributes attribute to set
	 * @param workWithUserAttributes method can process also user and member attributes (user is automatically get from member)
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, UserNotExistsException;

	/**
	 * Store the attributes associated with member and user (which we get from this member) if workWithUserAttributes is true.
	 * If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param attributes attribute to set
	 * @param workWithUserAttributes true/false If true, we can use user attributes (get from this member) too
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws UserNotExistsException if the user (get from this member after workWithUserAttributes=true) doesn't exists in the underlying data source
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member attribute or with workWithUserAttributes=true, if its not member or user attribute.
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, MemberNotExistsException, UserNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the member, user, member-resource and user-facility attributes. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param facility
	 * @param resource resource to set on
	 * @param user
	 * @param member member to set on
	 * @param attributes
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute
	 * @throws UserNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the resource. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the facility and user combination. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param facility facility to set on
	 * @param user user to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in the underlying data source
	 * @throws UserNotExistsException if the user doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the user. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param user user to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserNotExistsException if the user doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not user attribute
	 */
	void setAttributes(PerunSession sess, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the host. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perunsession
	 * @param host host to set attributes set
	 * @throws PrivilegeException if privileges are not given
	 * @throws HostNotExistsException if the host doesn't exist in the underlying data source
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException  if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException  if attribute is not host attribute
	 */
	void setAttributes(PerunSession sess, Host host, List<Attribute> attributes) throws PrivilegeException,HostNotExistsException,InternalErrorException,AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * Store the attributes associated with the group on resource.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param resource
	 * @param group
	 * @param attributes
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raises in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ResourceNotExistsException if resource doesn't exist in underlying data source
	 * @throws GroupNotExistsException if group doesn't exist in underlying data source
	 * @throws WrongReferenceAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not group-resource attribute
	 */
	void setAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException,GroupResourceMismatchException,AttributeNotExistsException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with group and resource if workWithUserAttributes is true then also from group itself.
	 * If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param group group to set on
	 * @param resource resource to set on
	 * @param attributes attribute to set
	 * @param workWithUserAttributes true/false If true, we can use group attributes too
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the group doesn't exists in the underlying data source
	 * @throws ResourceNotExistsException if the resource (get from this member after workWithUserAttributes=true) doesn't exists in the underlying data source
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member attribute or with workWithUserAttributes=true, if its not member or user attribute.
	 * @throws GroupResourceMismatchException if group and resource are from the same vo
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes, boolean workWithGroupAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException,GroupResourceMismatchException,AttributeNotExistsException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the user external source. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param ues user external source to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserExtSourceNotExistsException if the user external source doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not user external source attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, UserExtSource ues, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, UserExtSourceNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Get particular attribute for the facility.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param facility to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttribute(PerunSession sess, Facility facility, String attributeName) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the vo.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param vo to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws VoNotExistsException if the vo doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttribute(PerunSession sess, Vo vo, String attributeName) throws PrivilegeException, InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the group.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param group group get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the group doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttribute(PerunSession sess, Group group, String attributeName) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the resource.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param resource to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttribute(PerunSession sess, Resource resource, String attributeName) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the member on this resource.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param resource to get attribute from
	 * @param member to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttribute(PerunSession sess, Resource resource, Member member, String attributeName) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the member in this group.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param member to get attribute from
	 * @param group to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttribute(PerunSession sess, Member member, Group group, String attributeName) throws PrivilegeException, GroupNotExistsException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the member.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param member to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttribute(PerunSession sess, Member member, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the user on this facility.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param facility to get attribute from
	 * @param user to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in the underlying data source
	 * @throws UserNotExistsException if the user doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttribute(PerunSession sess, Facility facility, User user, String attributeName) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the user.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param user to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserNotExistsException if the user doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttribute(PerunSession sess, User user, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the host
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess perun session
	 * @param host to get attribute from
	 * @param attributeName attribute name defined in host manager
	 * @return attribute
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws HostNotExistsException if the host doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException if attribute isn't host attribute
	 */
	Attribute getAttribute(PerunSession sess, Host host, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, HostNotExistsException,WrongAttributeAssignmentException;

	/**
	 * Get particular group attribute on resource
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess perun session
	 * @param resource resource to get attributes from
	 * @param group group to get attributes for
	 * @param attributeName
	 * @return attribute
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException  if the attribute doesn't exists in the underlying data source
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 * @throws WrongAttributeAssignmentException if attribute isn't group-resource attribute
	 */
	Attribute getAttribute(PerunSession sess, Resource resource, Group group, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException;

	/**
	 * Get particular entityless attribute
	 *
	 * PRIVILEGE: Only PerunAdmin can access to entitylessAttributes.
	 *
	 * @param sess perun session
	 * @param key key to get attribute for
	 * @param attributeName
	 * @return attribute
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException  if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException if attribute isn't entityless attribute
	 */
	Attribute getAttribute(PerunSession sess, String key, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the user external source.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param ues to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserExtSourceNotExistsException if the user external source doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttribute(PerunSession sess, UserExtSource ues, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, UserExtSourceNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get attribute definition (attribute without defined value).
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	AttributeDefinition getAttributeDefinition(PerunSession sess, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException;

	/**
	 * Get all attributes definition (attribute without defined value).
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @return List of attributes definitions
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 */
	List<AttributeDefinition> getAttributesDefinition(PerunSession sess) throws PrivilegeException, InternalErrorException, AttributeNotExistsException;

	/**
	 * Get all (for entities) attributeDefinitions which user has right to READ them and fill attribute writable (if user has also right to WRITE them).
	 * For entities means that return only those attributeDefinition which are in namespace of entities or possible combination of entities.
	 * For Example: If entities are "member, user, resource" then return only AD in namespaces "member, user, resource and resource-member"
	 *
	 * @param sess
	 * @param entities list of perunBeans (member, user...)
	 *
	 * @return list of AttributeDefinitions with rights (writable will be filled correctly by user in session)
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	List<AttributeDefinition> getAttributesDefinitionWithRights(PerunSession sess, List<PerunBean> entities) throws PrivilegeException, InternalErrorException, AttributeNotExistsException;

	/**
	 * From listOfAttributesNames get list of attributeDefinitions
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param sess
	 * @param listOfAttributesNames
	 * @return list of AttributeDefinitions
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	List<AttributeDefinition> getAttributesDefinition(PerunSession sess, List<String> listOfAttributesNames) throws PrivilegeException, InternalErrorException, AttributeNotExistsException;

	/**
	 * Get attribute definition (attribute without defined value).
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	AttributeDefinition getAttributeDefinitionById(PerunSession sess, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException;

	/**
	 * Get attributes definition (attribute without defined value) with specified namespace.
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param namespace get only attributes with this namespace
	 * @return List of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 */
	List<AttributeDefinition> getAttributesDefinitionByNamespace(PerunSession sess, String namespace) throws PrivilegeException, InternalErrorException;

	/**
	 * Get particular attribute for the facility.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param facility to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttributeById(PerunSession sess, Facility facility, int id) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the vo.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param vo to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws VoNotExistsException if the vo doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttributeById(PerunSession sess, Vo vo, int id) throws PrivilegeException, InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the resource.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param resource to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttributeById(PerunSession sess, Resource resource, int id) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the member on this resource.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param resource to get attribute from
	 * @param member to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttributeById(PerunSession sess, Resource resource, Member member, int id) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the member in this group.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess perun session
	 * @param member to get attribute from
	 * @param group to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttributeById(PerunSession sess, Member member, Group group, int id) throws PrivilegeException, GroupNotExistsException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the member.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param member to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttributeById(PerunSession sess, Member member, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the user on this facility.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param facility to get attribute from
	 * @param user to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in the underlying data source
	 * @throws UserNotExistsException if the user doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttributeById(PerunSession sess, Facility facility, User user, int id) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the user.
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param user to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserNotExistsException if the user doesn't exists in the underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttributeById(PerunSession sess, User user, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the host
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess perun session
	 * @param host
	 * @param id
	 * @return
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws HostNotExistsException if the host doesn't exist in the underlying data source
	 * @throws WrongAttributeAssignmentException if the attribute isn't host attribute
	 */
	Attribute getAttributeById(PerunSession sess, Host host, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, HostNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular group attribute on this resource
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param resource
	 * @param group
	 * @param id
	 * @return
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttributeById(PerunSession sess, Resource resource, Group group, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ResourceNotExistsException,GroupNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException;

	/**
	 * Get particular attribute for group
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param group
	 * @param id
	 * @return
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws GroupNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttributeById(PerunSession sess, Group group, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException,GroupNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException;

	/**
	 * Get particular attribute for user external source
	 *
	 * PRIVILEGE: Principal need to have access to attribute which wants to get.
	 *
	 * @param sess
	 * @param ues
	 * @param id
	 * @return
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws UserExtSourceNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttributeById(PerunSession sess, UserExtSource ues, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException,UserExtSourceNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Store the particular attribute associated with the facility. Core attributes can't be set this way.
	 *
	 * PRIVILEGE: Principal need to have access to all attribute which wants to set.
	 *
	 * @param sess perun session
	 * @param facility facility to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not facility attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Facility facility, Attribute attribute) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular attribute associated with the vo. Core attributes can't be set this way.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param vo vo to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws VoNotExistsException if the vo doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not vo attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Vo vo, Attribute attribute) throws PrivilegeException, InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular attribute associated with the group. Core attributes can't be set this way.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param group group to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the group doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not group attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular attribute associated with the resource. Core attributes can't be set this way.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param resource resource to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not resource attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Resource resource, Attribute attribute) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular attribute associated with the resource and member combination.  Core attributes can't be set this way.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param resource resource to set on
	 * @param member member to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Resource resource, Member member, Attribute attribute) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular attribute associated with the group and member combination. Core attributes can't be set this way.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param group group to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular attribute associated with the member.  Core attributes can't be set this way.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attribute associated with the facility and user combination.  Core attributes can't be set this way.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param facility facility to set on
	 * @param user user to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in the underlying data source
	 * @throws UserNotExistsException if the user doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attribute associated with the user.  Core attributes can't be set this way.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param user user to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserNotExistsException if the user doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attribute associated with the host.  Core attributes can't be set this way.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perunsession
	 * @param host host to set attributes on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserNotExistsException if the host doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not host attribute

*/
	void setAttribute(PerunSession sess, Host host, Attribute attribute) throws PrivilegeException, InternalErrorException, HostNotExistsException,AttributeNotExistsException,WrongAttributeValueException,WrongAttributeAssignmentException;

	/**
	 * Stores attribute associated with group resource combination.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param resource to set attributes on
	 * @param group
	 * @param attribute attribute to set
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not group resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Resource resource, Group group,  Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException,GroupResourceMismatchException, WrongReferenceAttributeValueException;

	/**
	 * Stores entityless attribute (associateed witk string key).
	 *
	 * PRIVILEGE: Only PerunAdmin can get entityless attributes.
	 *
	 * @param sess perun session
	 * @param key stopre the attribute for this key
	 * @param attribute attribute to set
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not entityless attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, String key, Attribute attribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attribute associated with the user external source.
	 *
	 * PRIVILEGE: Principal need to have access to all attributes which he wants to set.
	 *
	 * @param sess perun session
	 * @param ues user external source to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserExtSourceNotExistsException if the user external source doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not user external source attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, UserExtSource ues, Attribute attribute) throws PrivilegeException, InternalErrorException, UserExtSourceNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Creates an attribute, the attribute is stored into the appropriate DB table according to the namespace
	 *
	 * PRIVILEGE: Only PerunAdmin can create new attribute.
	 *
	 * @param sess perun session
	 * @param attributeDefinition attribute to create
	 *
	 * @return attribute with set id
	 *
	 * @throws AttributeExistsException if attribute already exists
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 */
	AttributeDefinition createAttribute(PerunSession sess, AttributeDefinition attributeDefinition) throws PrivilegeException, InternalErrorException, AttributeExistsException;

	/**
	 * Deletes the attribute.
	 *
	 * PRIVILEGE: Only PerunAdmin can delete existing attribute.
	 *
	 * @param sess
	 * @param attributeDefinition
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws RelationExistsException
	 * @throws AttributeNotExistsException
	 * @throws PrivilegeException if privileges are not given
	 */
	void deleteAttribute(PerunSession sess, AttributeDefinition attributeDefinition) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, RelationExistsException;

	/**
	 * Deletes the attribute.
	 *
	 * PRIVILEGE: Only PerunAdmin can delete existing attribute.
	 *
	 * @param sess
	 * @param attributeDefinition attribute to delete
	 * @param force delete also all existing relation. If this parameter is true the RelationExistsException is never thrown.
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws RelationExistsException
	 * @throws AttributeNotExistsException
	 */
	void deleteAttribute(PerunSession sess, AttributeDefinition attributeDefinition, boolean force) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, RelationExistsException;

	/**
	 * Get facility attributes which are required by all services which are related to this facility.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param facility you get attributes for this facility
	 * @return list of facility attributes which are required by services
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility wasn't created from this resource
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException;

	/**
	 * Get resource attributes which are required by services which is relatod to this resource.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param resource resource for which you want to get the attributes
	 * @return list of resource attributes which are required by services which are assigned to resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource) throws PrivilegeException, InternalErrorException, ResourceNotExistsException;

	/**
	 * Get member-resource attributes which are required by services which are defined on "resourceToGetServicesFrom" resource.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
	 * @param resource you get attributes for this resource and the member
	 * @param member you get attributes for this member and the resource
	 * @return list of member-resource attributes which are required by services which are assigned to specified resource
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Member member) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get member-resource attributes which are required by services and if workWithUserAttributes is true also user, user-facility and member attributes.
	 * Services are known from the resourceToGetServicesFrom.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
	 * @param resource you get attributes for this resource and the member
	 * @param member you get attributes for this member and the resource
	 * @param workWithUserAttributes method can process also user, user-facility and member attributes (user is automatically get from member a facility is get from resource)
	 * @return list of member-resource attributes (if workWithUserAttributes is true also user, user-facility and member attributes) which are required by services which are assigned to another resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 *
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Member member, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get member-group attributes which are required by services defined on specified resource
	 * Services are known from the resourceToGetServicesFrom.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
	 * @param member you get attributes for this member and the group
	 * @param group you get attributes for this group and the member
	 * @return list of group-resource's attributes which are required by services defined on specified resource
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws MemberNotExistsException
	 * @throws GroupNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group) throws PrivilegeException, InternalErrorException, WrongAttributeAssignmentException, ResourceNotExistsException, MemberNotExistsException, GroupNotExistsException;

	/**
	 * Get member-group attributes which are required by services defined on specified resource and if workWithUserAttributes is true also user and member attributes.
	 * Services are known from the resourceToGetServicesFrom.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
	 * @param member you get attributes for this member and the group
	 * @param group you get attributes for this group and the member
	 * @param workWithUserAttributes method can process also user and member attributes (user is automatically get from member)
	 * @return list of group-resource's attributes which are required by services defined on specified resource
	 *
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws MemberNotExistsException
	 * @throws GroupNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, WrongAttributeAssignmentException, ResourceNotExistsException, MemberNotExistsException, GroupNotExistsException;

	/**
	 * Get member, user, member-resource and user-facility attributes which are required by services which are defined on "resourceToGetServicesFrom" resource.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
	 * @param facility
	 * @param resource you get attributes for this resource and the member
	 * @param user
	 * @param member you get attributes for this member and the resource
	 * @return list of member-resource attributes which are required by services which are assigned to specified resource
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws UserNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility, Resource resource, User user, Member member) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get user-facility attributes which are required by services which are defined on specified resource
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
	 * @param facility facility from which the services are taken
	 * @param user you get attributes for this user
	 * @return list of user-facility attributes which are required by services defined on specified resource
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws UserNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility, User user) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, FacilityNotExistsException, UserNotExistsException;

	/**
	 * Get member attributes which are required by services defined on specified resource
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param member you get attributes for this member
	 * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
	 * @return list of member attributes which are required by services defined on specified resource
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member) throws PrivilegeException, InternalErrorException, MemberNotExistsException, ResourceNotExistsException;

	/**
	 * Get user attributes which are required by services defined on specified resource
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param user
	 * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
	 * @return list of user's attributes which are required by services defined on specified resource
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserNotExistsException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, User user) throws PrivilegeException, InternalErrorException, UserNotExistsException, ResourceNotExistsException;

	/**
	 * Get host attributes which are required by services defined on specified resource
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess
	 * @param resourceToGetServicesFrom  getRequired attributes from services which are assigned on this resource
	 * @param host
	 * @return list of host's attributes which are required by services defined on specified resource
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws HostNotExistsException
	 * @throws ResourceNotExistsException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Host host) throws PrivilegeException, InternalErrorException, HostNotExistsException, ResourceNotExistsException;

	/**
	 * Get group-resource attributes which are required by services defined on specified resource
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess
	 * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
	 * @param resource
	 * @param group
	 * @return list of group-resource's attributes which are required by services defined on specified resource
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group) throws PrivilegeException, InternalErrorException, WrongAttributeAssignmentException, ResourceNotExistsException, GroupNotExistsException;

	/**
	 * Get group attributes which are required by services defined on specified resource
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess
	 * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
	 * @param group
	 * @return list of group's attributes which are required by services defined on specified resource
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Group group) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException;

	/**
	 * Get group-resource attributes which are required by services defined on specified resource
	 * Get also group attributes, if workWithGroupAttributes is true.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess
	 * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
	 * @param group
	 * @param resource
	 * @param workWithGroupAttributes if true, get also group required attributes
	 *
	 * @return list of group_resource and (if workWithGroupAttributes is true) group required attributes
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ConsistencyErrorException
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 * @throws GroupResourceMismatchException
	 * @throws WrongAttributeAssignmentException
	 *
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group, boolean workWithGroupAttributes) throws WrongAttributeAssignmentException, PrivilegeException, InternalErrorException, ConsistencyErrorException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException;

	/**
	 * Get member-resource attributes which are required by services which are relater to this member-resource.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param resource you get attributes for this resource and the member
	 * @param member you get attributes for this member and the resource
	 * @return list of member-resource attributes which are required by services which are assigned to resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Member member) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * If workWithUserAttribute is false => Get member-resource attributes which are required by services which are relater to this member-resource.
	 * If workWithUserAttributes is true => Get member-resource, user-facility, user and member attributes. (user is get from member and facility from resource)
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param resource you get attributes for this resource and the member
	 * @param member you get attributes for this member and the resource
	 * @param workWithUserAttributes method can process also user, user-facility and member attributes (user is automatically get from member a facility is get from resource)
	 * @return list of member-resource attributes or if workWithUserAttributes is true return list of member-resource, user, member and user-facility attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException
	 * @throws ResourceNotExistsException
	 * @throws MemberNotExistsException
	 * @throws WrongAttributeAssignmentException
	 *
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Member member, boolean workWithUserAttributes) throws PrivilegeException, WrongAttributeAssignmentException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException;

	/**
	 * Get user-facility attributes which are required by services which are related to this user-facility.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param facility facility from which the services are taken
	 * @param user you get attributes for this user
	 * @return list of user-facility attributes which are required by services which are assigned to facility.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws UserNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, FacilityNotExistsException, UserNotExistsException;

	/**
	 * Get member attributes which are required by services which are relater to this member.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param member you get attributes for this member
	 * @param workWithUserAttributes method can process also user and user-facility attributes (user is automatically get from member a facility is get from resource)
	 * @return list of member and user attributes which are required by services which are related to this member
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Member member, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException;

	/**
	 * Get user attributes which are required by services which are relater to this user.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param user
	 * @return list of user's attributes which are required by services which are related to this user
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserNotExistsException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, User user) throws PrivilegeException, InternalErrorException, UserNotExistsException;

	/**
	 * Get all attributes which are required by service.
	 * Required attribues are requisite for Service to run.
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param sess sess
	 * @param service service from which the attributes will be listed
	 *
	 * @return All attributes which are required by service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 */
	List<AttributeDefinition> getRequiredAttributesDefinition(PerunSession sess, Service service) throws PrivilegeException, InternalErrorException, ServiceNotExistsException;

	/*
	 *  Get facility attributes which are required by all services which are connected to this facility.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param facility you get attributes for this facility
	 * @return list of facility attributes which are required by all services which are connected to this facility.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in underlying data source
	 List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException;
	 */

	/**
	 * Get facility attributes which are required by the service.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param facility you get attributes for this facility
	 * @param service attribute required by this servis you'll get
	 * @return list of facility attributes which are required by the service
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility wasn't created from this resource
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, ServiceNotExistsException;

	/**
	 * Get vo attributes which are required by the service.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param vo you get attributes for this vo
	 * @param service attribute required by this service you'll get
	 * @return list of vo attributes which are required by the service
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws VoNotExistsException if the vo wasn't created from this resource
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Vo vo) throws PrivilegeException, InternalErrorException, VoNotExistsException, ServiceNotExistsException;
	
	/**
	 * Get facility attributes which are required by the services.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param facility you get attributes for this facility
	 * @param services attributes required by this services you'll get
	 * @return list of facility attributes which are required by the service
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility wasn't created from this resource
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, ServiceNotExistsException;

	/**
	 * Get resource attributes which are required by the service.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param resource resource for which you want to get the attributes
	 * @param service attributes required by this service you'll get
	 * @return list of resource attributes which are required by the service
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException;

	/**
	 * Get resource attributes which are required by the services.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param resource you get attributes for this resource
	 * @param services attributes required by this services you'll get
	 * @return list of facility attributes which are required by the service
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Resource resource) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException;

	/**
	 * Get member-resource attributes which are required by the service.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param resource you get attributes for this resource and the member
	 * @param member you get attributes for this member and the resource
	 * @param service attributes required by this services you'll get
	 * @return list of attributes which are required by the service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in underlying data source
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Member member) throws PrivilegeException, WrongAttributeAssignmentException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException;

	/**
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Member member, boolean workWithUserAttributes) throws PrivilegeException, WrongAttributeAssignmentException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException;

	/**
	 * Get member-resource, member, user-facility and user attributes which are required by service for each member in list of members.
	 * If workWithUserAttributes is false return only member-resource attributes.
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param resource you get attributes for this resource
	 * @param members you get attributes for this list of members
	 * @param workWithUserAttributes if true method can process also user, user-facility and member attributes
	 * @return map of member objects and his list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if methods checkMemberIsFromTheSameVoLikeResource finds that user is not from same vo like resource
	 */
	HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource, List<Member> members, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException, ServiceNotExistsException, ResourceNotExistsException, MemberNotExistsException, FacilityNotExistsException;

	/**
	 * Get member-resource attributes which are required by service for each member in list of members.
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param resource you get attributes for this resource and the members
	 * @param members you get attributes for this list of members and the resource
	 * @return map of memberID and his list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 */
	HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource, List<Member> members) throws InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException;

	/**
	 * Get member attributes which are required by service for each member in list of members.
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param resource resource only to get allowed members
	 * @param members you get attributes for this list of members
	 * @return map of memberID and his list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 */
	HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Resource resource, Service service, List<Member> members) throws InternalErrorException, ServiceNotExistsException, ResourceNotExistsException, MemberNotExistsException;


	/**
	 * Get user-facility attributes which are required by the service for each user in list of users.
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param facility you get attributes for this facility and user
	 * @param users you get attributes for this user and facility
	 * @return map of user and his list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 * @throws FacilityNotExistsException if the facility wasn't created from this resource
	 * @throws UserNotExistsException if the host doesn't exists in the underlying data source
	 */
	HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility, List<User> users) throws InternalErrorException, ServiceNotExistsException, FacilityNotExistsException, UserNotExistsException;

	/**
	 * Get user attributes which are required by the service for each user in list of users.
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param users you get attributes for this user and facility
	 * @return map of user and his list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 * @throws UserNotExistsException
	 */
	HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, List<User> users) throws InternalErrorException, ServiceNotExistsException, UserNotExistsException;

	/**
	 * Get member-group attributes which are required by the service.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param member you get attributes for this member and the resource
	 * @param group you get attributes for this group in which member is associated
	 * @param service attribute required by this service you'll get
	 * @return list of attributes which are required by the service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if the member doesn't exists in underlying data source
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, MemberNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException;

	/**
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group, boolean workWithUserAttributes) throws PrivilegeException, WrongAttributeAssignmentException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException, GroupNotExistsException;

	/**
	 * Get member attributes which are required by the service.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param member you get attributes for this member and the resource
	 * @param service attribute required by this servis you'll get
	 * @return list of attributes which are required by the service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if the member doesn't exists in underlying data source
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't have access to this resource
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, MemberNotExistsException;

	/**
	 * Get group-resource attributes required for the service.
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess perun session
	 * @param service you will get attributes required by this service
	 * @param resource
	 * @param group
	 * @param workWithGroupAttributes get also group attributes (which is required by the service) for this group
	 * @return
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ServiceNotExistsException if the service doesn't exists in underlying data source
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group, boolean workWithGroupAttributes) throws PrivilegeException, WrongAttributeAssignmentException, InternalErrorException, ServiceNotExistsException, ResourceNotExistsException, GroupNotExistsException;

	/**
	 * Get group-resource attributes required for the service.
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess
	 * @param service
	 * @param resource
	 * @param group
	 * @return
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 * @throws GroupResourceMismatchException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group) throws PrivilegeException, InternalErrorException, WrongAttributeAssignmentException, ServiceNotExistsException, ResourceNotExistsException, GroupNotExistsException,GroupResourceMismatchException;

	/**
	 * Get host required attributes for the service
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess
	 * @param service
	 * @param host
	 * @return
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws HostNotExistsException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Host host) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, HostNotExistsException;

	/**
	 * Get group required attributes for the service
	 *
	 * PRIVILEGE: Get only those required attributes principal has access to.
	 *
	 * @param sess
	 * @param service
	 * @param group
	 * @return
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ServiceNotExistsException
	 * @throws GroupNotExistsException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Group group) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, GroupNotExistsException;

	/**
	 * This method tries to fill a value of the resource attribute. Value may be copied from some facility attribute.
	 *
	 * PRIVILEGE: Fill attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param resource resource, attribute of which you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 */
	Attribute fillAttribute(PerunSession sess, Resource resource, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 *  Batch version of fillAttribute. This method skips all attributes with not-null value.
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,Resource,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Resource resource, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * This method tries to fill value of the member-resource attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * PRIVILEGE: Fill attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param resource  resource which attribute you want to fill
	 * @param member member which attribute you want to fill
	 * @param attribute attribute to fill. If attribute already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws MemberNotExistsException if member doesn't exists in underlying data source or he doesn't have access to this resource
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 */
	Attribute fillAttribute(PerunSession sess, Resource resource, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 *  Batch version of fillAttribute. This method skips all attributes with not-null value.
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,Resource,Member,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 * @param workWithUserAttributes method can process also user and user-facility attributes (user is automatically get from member a facility is get from resource)
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	List<Attribute> fillAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * This method tries to fill value of the member-group attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * PRIVILEGE: Fill attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param member attribute of this member (and resource) you want to fill
	 * @param group attribute of this group you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if member doesn't exists in underlying data source or he doesn't have access to this resource
	 * @throws GroupNotExistsException if group doesn't exists
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 */
	Attribute fillAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, MemberNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 *  Batch version of fillAttribute. This method skips all attributes with not-null value.
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,Member,Group,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 * @param workWithUserAttributes method can process also user and member attributes (user is automatically get from member)
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	List<Attribute> fillAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupNotExistsException;

	/**
	 * This method tries to fill value of the user, member, member-resource and user-facility attributes. This value is automatically generated, but not all attributes can be filled this way.
	 * This method skips all attributes with not-null value.
	 *
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 * @param sess
	 * @param facility
	 * @param resource
	 * @param user
	 * @param member
	 * @param attributes
	 * @return
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws MemberNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws UserNotExistsException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> fillAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * This method tries to fill value of the member attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * PRIVILEGE: Fill attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param member attribute of this member (and resource) and you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if member doesn't exists in underlying data source or he doesn't have access to this resource
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 */
	Attribute fillAttribute(PerunSession sess, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 *  Batch version of fillAttribute. This method skips all attributes with not-null value.
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,Resource,Member,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * This method tries to fill value of the user-facility attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * PRIVILEGE: Fill attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param facility  attribute of this facility (and user) and you want to fill
	 * @param user attribute of this user (and facility) and you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in underlying data source
	 * @throws UserNotExistsException if user doesn't exists in underlying data source or he doesn't have access to this facility
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 */
	Attribute fillAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 *  Batch version of fillAttribute. This method skips all attributes with not-null value.
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,Facility,User,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * This method tries to fill value of the user attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * PRIVILEGE: Fill attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param user attribute of this user (and facility) and you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserNotExistsException if user doesn't exists in underlying data source or he doesn't have access to this facility
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 */
	Attribute fillAttribute(PerunSession sess, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 *  Batch version of fillAttribute. This method skips all attributes with not-null value.
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,Facility,User,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * This method tries to fill group-resource attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * PRIVILEGE: Fill attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param resource resource which attr you want to fill
	 * @param group group which attr you want to fill
	 * @param attribute attribute to be filled
	 * @return attribute which MAY have filled value
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException if attribute isn't group-resource attribute
	 */
	Attribute fillAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException,ResourceNotExistsException, GroupNotExistsException,AttributeNotExistsException,WrongAttributeAssignmentException,GroupResourceMismatchException;

	/**
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 * batch version of fillAttribute, this method skips attributes with non-null value
	 * @see AttributesManager#fillAttribute(PerunSession, Resource, Group, Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException,GroupResourceMismatchException;

	/**
	 * This method tries to fill host attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * PRIVILEGE: Fill attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param host host for which you want have attribute filled
	 * @param attribute attr which you want to fill
	 * @return attribute which MAY have filled value
	 * @throws PrivilegeException if privileges are not given
	 * @throws WrongAttributeAssignmentException if attribute isn't group-resource attribute
	 * @throws HostNotExistsException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSession sess, Host host, Attribute attribute) throws PrivilegeException,InternalErrorException, HostNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 * Batch version of fillAttribute
	 * @see AttributesManager#fillAttribute(PerunSession, Host, Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Host host, List<Attribute> attributes) throws PrivilegeException,InternalErrorException, HostNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * This method tries to fill group attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * PRIVILEGE: Fill attribute only when principal has access to write on it.
	 *
	 * @param sess
	 * @param group group which attribute you want to fill
	 * @param attribute attribute which you want to fill
	 * @return attribute which may have filled value
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSession sess, Group group, Attribute attribute) throws PrivilegeException,InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 * Batch version of fillAttribute
	 * @see AttributesManager#fillAttribute(PerunSession, Group, Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * This method tries to fill value of the user external source attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * PRIVILEGE: Fill attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param ues user external source which will be filled with attribute
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which may have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserExtSourceNotExistsException if user external source doesn't exists in underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 */
	Attribute fillAttribute(PerunSession sess, UserExtSource ues, Attribute attribute) throws PrivilegeException, InternalErrorException, UserExtSourceNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * PRIVILEGE: Fill attributes only when principal has access to write on them.
	 *
	 * Batch version of fillAttribute.
	 * @see AttributesManager#fillAttribute(PerunSession, UserExtSouce, Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, UserExtSource ues, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, UserExtSourceNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Check if value of this facility attribute is valid.
	 *
	 * PRIVILEGE: Check attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param facility facility for which you want to check validity of attribute
	 * @param attribute attribute to check
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in underlying data source
	 * @throws WrongAttributeAssignmentException if attribute isn't facility attribute
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws AttributeNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Facility facility, Attribute attribute) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;


	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them.
	 *
	 *  Batch version of checkAttributeValue
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Facility,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Facility facility, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * Check if value of this vo attribute is valid.
	 *
	 * PRIVILEGE: Check attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param vo vo for which you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws VoNotExistsException if the vo doesn't exists in underlying data source
	 * @throws WrongAttributeAssignmentException if attribute isn't vo attribute
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException
	 * @throws AttributeNotExistsException
	 */
	void checkAttributeValue(PerunSession sess, Vo vo, Attribute attribute) throws PrivilegeException, InternalErrorException, VoNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them.
	 *
	 *  Batch version of checkAttributeValue
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Vo,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Vo vo, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, VoNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * Check if value of this resource attribute is valid.
	 *
	 * @param sess perun session
	 * @param resource resource for which you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * PRIVILEGE: Check attribute only when principal has access to write on it.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws MemberNotExistsException if member doesn't exists in underlying data source or he doesn't have access to this resource
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't resource attribute
	 * @throws WrongReferenceAttributeValueException
	 * @throws AttributeNotExistsException
	 */
	void checkAttributeValue(PerunSession sess, Resource resource, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them.
	 *
	 *  Batch version of checkAttributeValue
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Resource,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Resource resource, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * Check if value of this member-resource attribute is valid.
	 *
	 * PRIVILEGE: Check attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param resource resource for which (and for specified member) you want to check validity of attribute
	 * @param member member for which (and for specified resource) you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 * @throws AttributeNotExistsException
	 */
	void checkAttributeValue(PerunSession sess, Resource resource, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them.
	 *
	 *  Batch version of fillAttribute
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Resource,Member,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Resource resource, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them.
	 *
	 *  Batch version of fillAttribute
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Resource,Member,Attribute)
	 * @param workWithUserAttributes method can process also user and user-facility attributes (user is automatically get from member a facility is get from resource)
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	void checkAttributesValue(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * Check if value of this member-group attribute is valid.
	 *
	 * PRIVILEGE: Check attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param group group for which (and for specified member) you want to check validity of attribute
	 * @param member member for which (and for specified group) you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 * @throws AttributeNotExistsException
	 */
	void checkAttributeValue(PerunSession sess, Member member, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, GroupNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them.
	 *
	 * Batch version of fillAttribute
	 * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Member,Group,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, GroupNotExistsException;

	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them.
	 *
	 * Batch version of fillAttribute
	 * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Member,Group,Attribute)
	 * @param workWithUserAttributes method can process also user and member attributes (user is automatically get from member)
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	void checkAttributesValue(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, GroupNotExistsException;

	/**
	 * Check if value of attributes is valid. Attributes can be from namespace: member, user, member-resource and user-facility.
	 *
	 * PRIVILEGE: Check attributes only when principal has access to write on them.
	 *
	 * @param sess
	 * @param facility
	 * @param resource
	 * @param user
	 * @param member
	 * @param attributes
	 *
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws MemberNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws UserNotExistsException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AttributeNotExistsException
	 */
	void checkAttributesValue(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * Check if value of this member attribute is valid.
	 *
	 * PRIVILEGE: Check attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param member member for which (and for specified resource) you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 * @throws AttributeNotExistsException
	 */
	void checkAttributeValue(PerunSession sess, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them
	 *
	 *  Batch version of fillAttribute
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Resource,Member,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * Check if value of this user-facility attribute is valid.
	 *
	 * PRIVILEGE: Check attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param facility facility for which (and for specified user) you want to check validity of attribute
	 * @param user user for which (and for specified facility) you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws FacilityNotExistsException if the facility doesn't exists in underlying data source
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 * @throws AttributeNotExistsException
	 */
	void checkAttributeValue(PerunSession sess, Facility facility, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them
	 *
	 *  Batch version of checkAttributeValue
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Facility,User,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * Check if value of this user attribute is valid.
	 *
	 * PRIVILEGE: Check attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param user user for which (and for specified facility) you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 * @throws AttributeNotExistsException
	 */
	void checkAttributeValue(PerunSession sess, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them.
	 *
	 *  Batch version of checkAttributeValue
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,User,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException;

	/**
	 * Checks if value of this host attribute is valid
	 *
	 * PRIVILEGE: Check attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param host host for which attribute validity is checked
	 * @param attribute
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if the attribute isn't host attribute
	 * @throws AttributeNotExistsException if given attribute doesn't exist
	 * @throws HostNotExistsException if specified host doesn't exist
	 */
	void checkAttributeValue(PerunSession sess, Host host, Attribute attribute) throws PrivilegeException, InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException,AttributeNotExistsException, HostNotExistsException;

	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them.
	 *
	 * batch version of checkAttributeValue
	 * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession, Host, Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Host host, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, HostNotExistsException, WrongAttributeValueException,WrongAttributeAssignmentException;

	/**
	 * Checks if value of this group attribute is valid
	 *
	 * PRIVILEGE: Check attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param group group for which attribute validity is checked
	 * @param attribute
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if the attribute isn't group attribute
	 * @throws WrongReferenceAttributeValueException if value of referenced attribute (if any) is not valid
	 * @throws AttributeNotExistsException if given attribute doesn't exist
	 * @throws GroupNotExistsException if specified group doesn't exist
	 */
	void checkAttributeValue(PerunSession sess, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, GroupNotExistsException;

	/**
	 * Checks if value of this group-resource attribute is valid
	 *
	 * PRIVILEGE: Check attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param resource resource which attr you want check
	 * @param group group which attr you want to check
	 * @param attribute attribute to be checked
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Resource resource, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException,GroupResourceMismatchException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them.
	 *
	 * Batch version of checkAttributeValue
	 * @see AttributesManager#checkAttributeValue(PerunSession, Resource, Group, Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException,GroupResourceMismatchException, WrongReferenceAttributeValueException;
	
	/**
	 * Checks if value of this user external source attribute is valid
	 *
	 * PRIVILEGE: Check attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param ues user external source for which attribute validity is checked
	 * @param attribute
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if the attribute isn't UserExtSource attribute
	 * @throws AttributeNotExistsException if given attribute doesn't exist
	 * @throws UserExtSourceNotExistsException if specified user external source doesn't exist
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, UserExtSource ues, Attribute attribute) throws PrivilegeException, InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException,AttributeNotExistsException, UserExtSourceNotExistsException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Check attributes only when principal has access to write on them.
	 *
	 * batch version of checkAttributeValue
	 * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession, UserExtSource, Attribute)
	 */
	void checkAttributesValue(PerunSession sess, UserExtSource ues, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, UserExtSourceNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException,  WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the facility. Core attributes can't be removed this way.
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param facility remove attribute from this facility
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws FacilityNotExistsException if the facility doesn't exists in underlying data source
	 * @throws WrongAttributeAssignmentException if attribute isn't facility attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException if the attribute isn't entityless attribute
	 * @throws WrongAttributeValueException
	 * @throws AttributeNotExistsException
	 */
	void removeAttribute(PerunSession sess, Facility facility, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Facility, AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, Facility facility, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset the group-resource attributes. If an attribute is core attribute, then the attribute isn't unset (it's skipped without notification).
	 * If workWithGroupAttributes is true, unset also group attributes.
	 *
	 * Remove only attributes which are in list of attributes.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param group group to set on
	 * @param resource resource to set on
	 * @param attributes attributes which will be used to removing
	 * @param workWithGroupAttributes if true, remove also group attributes, if false, remove only group-resource attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws GroupNotExistsException if group not exists in perun
	 * @throws ResourceNotExistsException if resource not exists in perun
	 * @throws GroupResourceMismatchException if group and resource has not the same vo
	 * @throws WrongAttributeAssignmentException if attribute is not group-resource or group attribute
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongReferenceAttributeValueException if some reference attribute has illegal value
	 */
	void removeAttributes(PerunSession sess, Resource resource, Group group, List<? extends AttributeDefinition> attributes, boolean workWithGroupAttributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, GroupNotExistsException, ResourceNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the group and resource.
	 * If workWithGroupAttributes is true, remove also all group attributes.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param group group to set on
	 * @param resource resource to set on
	 * @param workWithGroupAttributes if true, remove also group attributes, if false, remove only group_resource attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws GroupNotExistsException if group not exists in perun
	 * @throws ResourceNotExistsException if resource not exists in perun
	 * @throws GroupResourceMismatchException if group and resource has not the same vo
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongReferenceAttributeValueException if some reference attribute has illegal value
	 * @throws WrongAttributeAssignmentException if attribute is not group-resource or group attribute
	 */
	void removeAllAttributes(PerunSession sess, Resource resource, Group group, boolean workWithGroupAttributes) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, GroupResourceMismatchException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * Unset particular entityless attribute with subject equals key.
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param key subject of entityless attribute
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	void removeAttribute(PerunSession sess, String key, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, PrivilegeException, AttributeNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the facility.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param facility remove attributes from this facility
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws FacilityNotExistsException if the facility doesn't exists in underlying data source
	 */
	void removeAllAttributes(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the facility.
	 * If removeAlsoUserFacilityAttributes is true, remove all user-facility attributes of this facility and any user allowed in this facility.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param facility remove attributes from this facility
	 * @param removeAlsoUserFacilityAttributes if true, remove all user-facility attributes for any user in this facility too
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws FacilityNotExistsException if the facility doesn't exists in underlying data source
	 */
	void removeAllAttributes(PerunSession sess, Facility facility, boolean removeAlsoUserFacilityAttributes) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the vo. Core attributes can't be removed this way.
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param vo remove attribute from this vo
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws VoNotExistsException if the vo doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException if attribute isn't vo attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, VoNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Vo, AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, Vo vo, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, VoNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the vo.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param vo remove attributes from this vo
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws VoNotExistsException if the vo doesn't exists in underlying data source
	 */
	void removeAllAttributes(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the group. Core attributes can't be removed this way.
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param group remove attribute from this group
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws GroupNotExistsException if the group doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException if attribute isn't group attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Group group, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Group, AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, Group group, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the group.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param group remove attributes from this group
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws GroupNotExistsException if the group doesn't exists in underlying data source
	 */
	void removeAllAttributes(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the resource. Core attributes can't be removed this way.
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param resource remove attribute from this resource
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException if attribute isn't resource attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Resource, AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, Resource resource, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset the member, user, member-resource and user-facility attributes. If an attribute is core attribute then the attribute isn't unset (It's skipped without any notification).
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param facility
	 * @param resource resource to set on
	 * @param user
	 * @param member member to set on
	 * @param attributes
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws ResourceNotExistsException if the resource doesn't exists in the underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in the underlying data source
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource, user, member or user-facility attribute
	 * @throws UserNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 */
	void removeAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<? extends AttributeDefinition> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;


	/**
	 * Unset all attributes for the resource.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param resource remove attributes from this resource
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 */
	void removeAllAttributes(PerunSession sess, Resource resource) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the member on the resource. Core attributes can't be removed this way.
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param resource remove attributes for this resource
	 * @param member remove attribute from this member
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in underlying data source
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Resource resource, Member member, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Resource, Member, AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, Resource resource, Member member, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the member on the resource.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param resource
	 * @param member remove attributes from this member
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if the member doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws ResourceNotExistsException if the resource doesn't exists in underlying data source
	 * @throws WrongAttributeAssignmentException
	 */
	void removeAllAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException, WrongAttributeAssignmentException, PrivilegeException, MemberNotExistsException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the member in the group. Core attributes can't be removed this way.
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param group remove attributes for this group
	 * @param member remove attribute from this member
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in underlying data source
	 * @throws GroupNotExistsException if the resource doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Member, Group, AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, Member member, Group group, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the member in the group.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param group remove attributes for this group
	 * @param member remove attributes from this member
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws MemberNotExistsException if the member doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws GroupNotExistsException if the resource doesn't exists in underlying data source
	 * @throws WrongAttributeAssignmentException
	 */
	void removeAllAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException, WrongAttributeAssignmentException, PrivilegeException, MemberNotExistsException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the member. Core attributes can't be removed this way.
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param member remove attribute from this member
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException if attribute isn't member attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Member member, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all <b>non-empty</b> attributes associated with the member and if workWithUserAttributes is
	 * true, unset all <b>non-empty</b> attributes associated with user, who is this member.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param member remove attribute from this member
	 * @param workWithUserAttributes true if I want to unset all attributes associated with user, who is the member too
	 * @param attributes attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws MemberNotExistsException if the member doesn't exists in underlying data source
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException if attribute isn't member attribute or if it is core attribute
	 */
	void removeAttributes(PerunSession sess, Member member, boolean workWithUserAttributes, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Member, AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, Member member, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the member.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param member remove attributes from this member
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws MemberNotExistsException if the member doesn't exists in underlying data source
	 */
	void removeAllAttributes(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the user on the facility. Core attributes can't be removed this way.
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param facility remove attributes for this facility
	 * @param user remove attribute from this user
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws UserNotExistsException if the user doesn't exists in underlying data source
	 * @throws FacilityNotExistsException if the facility doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Facility, User, AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, Facility facility, User user, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the user on the facility.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param facility
	 * @param user remove attributes from this user
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws UserNotExistsException if the user doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws FacilityNotExistsException if the facility doesn't exists in underlying data source
	 */
	void removeAllAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException, FacilityNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the user. Core attributes can't be removed this way.
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param user remove attribute from this user
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws UserNotExistsException if the user doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, User user, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, User, AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, User user, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the user.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param user remove attributes from this user
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws UserNotExistsException if the user doesn't exists in underlying data source
	 */
	void removeAllAttributes(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the host. Core attributes can't be removed this way.
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perunSession
	 * @param host remove attributes from this host
	 * @param attribute to remove
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws HostNotExistsException if the host doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException if attribute isn't host attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Host host, AttributeDefinition attribute) throws PrivilegeException,InternalErrorException,AttributeNotExistsException,HostNotExistsException,WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Host, AttributeDefinition)
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws HostNotExistsException if the host doesn't exists in underlying data source
	 * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
	 * @throws WrongAttributeAssignmentException if any of attributes isn't host attribute
	 */
	void removeAttributes(PerunSession sess, Host host, List<? extends AttributeDefinition> attributes) throws PrivilegeException,InternalErrorException,AttributeNotExistsException,HostNotExistsException,WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the host
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perunsession
	 * @param host remove attributes from this host
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws HostNotExistsException if the host doesn't exists in underlying data source
	 * @throws WrongAttributeAssignmentException
	 */
	void removeAllAttributes(PerunSession sess, Host host) throws PrivilegeException, WrongAttributeAssignmentException, InternalErrorException,HostNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular group attribute on the resource
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param resource remove attribute for this resource
	 * @param group remove attribute for this group
	 * @param attribute to be removed
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws GroupResourceMismatchException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException if attribute is not group-resource attribute
	 */
	void removeAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws PrivilegeException,InternalErrorException, ResourceNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException,GroupResourceMismatchException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * Batch version of removeAttribute
	 * @see AttributesManager#removeAttribute(PerunSession, Resource, Group, AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, Resource resource, Group group, List<? extends AttributeDefinition> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException,AttributeNotExistsException, WrongAttributeAssignmentException,GroupResourceMismatchException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Remove all attributes for group on resource
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param resource resource to have attributes removed
	 * @param group group to have attribute removed
	 * @throws PrivilegeException if privileges are not given
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws GroupResourceMismatchException
	 * @throws GroupNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	void removeAllAttributes(PerunSession sess, Resource resource, Group group) throws PrivilegeException, WrongAttributeAssignmentException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException,GroupResourceMismatchException, WrongAttributeValueException, WrongReferenceAttributeValueException;
	
	/**
	 * Unset particular attribute for the user external source.
	 *
	 * PRIVILEGE: Remove attribute only when principal has access to write on it.
	 *
	 * @param sess perun session
	 * @param ues remove attribute from this user external source
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws AttributeNotExistsException if the attribute doesn't exists in underlying data source
	 * @throws UserExtSourceNotExistsException if the user external source doesn't exists in underlying data source
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException if attribute isn't user external source attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, UserExtSource ues, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserExtSourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, UserExtSource, AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, UserExtSource ues, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserExtSourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the user external source.
	 *
	 * PRIVILEGE: Remove attributes only when principal has access to write on them.
	 *
	 * @param sess perun session
	 * @param ues remove attributes from this user external source
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws PrivilegeException if privileges are not given
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws UserExtSourceNotExistsException if the user external source doesn't exists in underlying data source
	 */
	void removeAllAttributes(PerunSession sess, UserExtSource ues) throws InternalErrorException, PrivilegeException, UserExtSourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Determine if attribute is core attribute.
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param sess
	 * @param attribute
	 * @return true if attribute is core attribute
	 */
	boolean isCoreAttribute(PerunSession sess, AttributeDefinition attribute);

	/**
	 * Determine if attribute is optional (opt) attribute.
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param sess
	 * @param attribute
	 * @return true if attribute is optional attribute
	 *         false otherwise
	 */
	boolean isOptAttribute(PerunSession sess, AttributeDefinition attribute);

	/**
	 * Determine if attribute is core-managed attribute.
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param sess
	 * @param attribute
	 * @return true if attribute is core-managed
	 */
	boolean isCoreManagedAttribute(PerunSession sess, AttributeDefinition attribute);

	/**
	 * Determine if attribute is from specified namespace.
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param sess
	 * @param attribute
	 * @param namespace
	 * @return true if the attribute is from specified namespace false otherwise
	 */
	boolean isFromNamespace(PerunSession sess, AttributeDefinition attribute, String namespace);

	/**
	 * Determine if attribute is from specified namespace.
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param sess
	 * @param attribute
	 * @param namespace
	 *
	 * @throws WrongAttributeAssignmentException if the attribute isn't from specified namespace
	 */
	void checkNamespace(PerunSession sess, AttributeDefinition attribute, String namespace) throws WrongAttributeAssignmentException;

	/**
	 * Determine if attributes are from specified namespace.
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param sess
	 * @param attributes
	 * @param namespace
	 *
	 * @throws WrongAttributeAssignmentException if any of the attribute isn't from specified namespace
	 */
	void checkNamespace(PerunSession sess, List<? extends AttributeDefinition> attributes, String namespace) throws WrongAttributeAssignmentException;

	/**
	 * Gets the namespace from the attribute name.
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param attributeName
	 * @return the namespace from the attribute name
	 * @throws InternalErrorException
	 */
	String getNamespaceFromAttributeName(String attributeName) throws InternalErrorException;

	/**
	 * Gets the friendly name from the attribute name.
	 *
	 * PRIVILEGE: No access needed.
	 *
	 * @param attributeName
	 * @return the friendly name from the attribute name
	 * @throws InternalErrorException
	 */
	String getFriendlyNameFromAttributeName(String attributeName) throws InternalErrorException;

	/**
	 * Get all attributes with user's logins.
	 *
	 * PRIVILEGE: Get only those logins principal has access to.
	 *
	 * @param sess
	 * @param user
	 * @return list of attributes with login
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 */
	List<Attribute> getLogins(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException;

	/**
	 * PRIVILEGE: Only for PerunAdmin.
	 *
	 * Same as doTheMagic(sess, member, false);
	 */
	void doTheMagic(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * This function takes all member-related attributes (member, user, member-resource, user-facility) and tries to fill them and set them.
	 * If trueMagic is set, this method can remove invalid attribute value (value which didn't pass checkAttributeValue test) and try to fill and set another. In this case, WrongReferenceAttributeValueException, WrongAttributeValueException are thrown if same attribute can't be set correctly.
	 *
	 * PRIVILEGE: Only for PerunAdmin.
	 *
	 * @param sess
	 * @param member
	 * @param trueMagic
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	void doTheMagic(PerunSession sess, Member member, boolean trueMagic) throws InternalErrorException, PrivilegeException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Updates AttributeDefinition.
	 * PRIVILEGE: only PerunAdmin
	 *
	 * @param perunSession
	 * @param attributeDefinition
	 * @return returns updated attributeDefinition
	 * @throws AttributeNotExistsException
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 */
	AttributeDefinition updateAttributeDefinition(PerunSession perunSession, AttributeDefinition attributeDefinition) throws AttributeNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Gets attribute rights of an attribute with id given as a parameter.
	 * If the attribute has no rights for a role, it returns empty list. That means the returned list has always 4 items
	 * for each of the roles VOADMIN, FACILITYADMIN, GROUPADMIN, SELF.
	 * Info: not return rights for role VoObserver (could be same like read rights for VoAdmin)
	 *
	 * @param sess perun session
	 * @param attributeId id of the attribute
	 * @return all rights of the attribute
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws AttributeNotExistsException
	 */
	List<AttributeRights> getAttributeRights(PerunSession sess, int attributeId) throws InternalErrorException, PrivilegeException, AttributeNotExistsException;

	/**
	 * Sets all attribute rights in the list given as a parameter.
	 * The method sets the rights for attribute and role exactly as it is given in the list of action types. That means it can
	 * remove a right, if the right is missing in the list.
	 * Info: If there is role VoAdmin in the list, use it for setting also VoObserver rights (only for read) automatic
	 *
	 * @param sess perun session
	 * @param rights list of attribute rights
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws AttributeNotExistsException when attribute IDs in rights don't refer to existing attributes
	 */
	void setAttributeRights(PerunSession sess, List<AttributeRights> rights) throws InternalErrorException, PrivilegeException, AttributeNotExistsException;
}
