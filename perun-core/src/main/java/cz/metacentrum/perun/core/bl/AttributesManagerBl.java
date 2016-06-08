/**
 *
 */
package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.ActionType;

import java.util.HashMap;
import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeRights;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichAttribute;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ActionTypeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ModuleNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongModuleTypeException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import java.util.Map;
import java.util.Set;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public interface AttributesManagerBl {


	/**
	 * Get all <b>non-empty</b> attributes associated with the facility.
	 *
	 * @param sess perun session
	 * @param facility facility to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the vo.
	 *
	 * @param sess perun session
	 * @param vo vo to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the group.
	 *
	 * @param sess perun session
	 * @param group group to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the resource.
	 *
	 * @param sess perun session
	 * @param resource resource to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Remove all non-virtual group-resource attributes assigned to resource
	 * 
	 * @param sess
	 * @param resource
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	void removeAllGroupResourceAttributes(PerunSession sess, Resource resource) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Remove all non-virtual member-resource attributes assigned to resource
	 *
	 * @param sess
	 * @param resource
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	void removeAllMemberResourceAttributes(PerunSession sess, Resource resource) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Get all virtual attributes associated with the member-resource attributes.
	 *
	 * @param sess perun session
	 * @param resource to get the attributes from
	 * @param member to get the attributes from
	 * @return list of attributes
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the member on the resource.
	 *
	 * @param sess perun session
	 * @param resource to get the attributes from
	 * @param member to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Gets all <b>non-empty</b> attributes associated with the member on the resource and if workWithUserAttributes is
	 * true, gets also all <b>non-empty</b> user, user-facility and member attributes.
	 *
	 * @param sess perun session
	 * @param resource to get the attributes from
	 * @param member to get the attributes from
	 * @param workWithUserAttributes if true returns also user-facility, user and member attributes (user is automatically get from member a facility is get from resource)
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 *
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	List<Attribute> getAttributes(PerunSession sess, Resource resource, Member member, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the member in the group.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param group group to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get all attributes (empty and virtual too) associated with the member in the group which have name in list attrNames.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param group group to get the attributes from
	 * @param attrNames list of attributes' names
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get all attributes associated with the member in the group and if workWithUserAttributes is true, gets also all <b>non-empty</b> user and member attributes.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param group group to get the attributes from
	 * @param workWithUserAttributes if true returns also user and member attributes (user is automatically get from member)
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, Group group, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the member and if workWithUserAttributes is
	 * true, get all <b>non-empty</b> attributes associated with user, who is this member.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, boolean workWithUserAttributes) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the member.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the group starts with name startPartOfName.
	 * Get only nonvirtual attributes with notNull Value.
	 *
	 *
	 * @param sess perun session
	 * @param group to get the attributes from
	 * @param startPartOfName attribute name start with this part
	 * @return list of attributes which name start with startPartOfName
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Group group, String startPartOfName) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the resource starts with name startPartOfName.
	 * Get only nonvirtual attributes with notNull value.
	 *
	 * @param sess perun session
	 * @param resource to get the attributes from
	 * @param startPartOfName attribute name start with this part
	 * @return list of attributes which name start with startPartOfName
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Resource resource, String startPartOfName) throws InternalErrorException;

	/**
	 * Get all attributes associated with the member which have name in list attrNames (empty too)
	 * Virtual attributes too.
	 *
	 * If workWithUserAttribute is true, return also all user attributes in list of attrNames (with virtual attributes too).
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param attrNames  list of attributes' names
	 * @param workWithUserAttributes if user attributes need to be return too
	 * @return list of member (and also if needed user) attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames, boolean workWithUserAttributes) throws InternalErrorException;

	/**
	 * Get all attributes associated with the member which have name in list attrNames (empty and virtual too).
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param attrNames list of attributes' names
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames) throws InternalErrorException;

    /**
	 * Get all attributes associated with the group which have name in list attrNames (empty too).
	 * Virtual attribute too.
	 *
	 * @param sess perun session
	 * @param group to get the attributes from
	 * @param attrNames list of attributes' names
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Group group, List<String> attrNames) throws InternalErrorException;

	/**
	 * Get all attributes associated with the vo which have name in list attrNames (empty and virtual too).
	 *
	 * @param sess perun session
	 * @param vo to get the attributes from
	 * @param attrNames list of attributes' names
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wraped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Vo vo, List<String> attrNames) throws InternalErrorException;

	/**
	 * Get all attributes associated with the user which have name in list attrNames (empty and virtual too).
	 *
	 * @param sess perun session
	 * @param user to get the attributes from
	 * @param attrNames list of attributes' names
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, User user, List<String> attrNames) throws InternalErrorException;


	/**
	 * Get all <b>non-empty</b> attributes associated with the user on the facility.
	 *
	 * @param sess perun session
	 * @param facility to get the attributes from
	 * @param user to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException;

	/**
	 * Returns all attributes with not-null value which fits the attributeDefinition. Can't process core or virtual attributes.
	 *
	 * @param sess
	 * @param attributeDefinition can't be core or virtual attribute
	 * @return list of attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getAttributesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get all virtual attributes associated with the user on the facility.
	 *
	 * @param sess perun session
	 * @param facility to get the attributes from
	 * @param user to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException;

	/**
	 * Get all virtual attributes associated with the user.
	 *
	 * @param sess perun session
	 * @param user to get the attributes from
	 * @return list of attributes
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the user.
	 *
	 * @param sess perun session
	 * @param user to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the host
	 * @param sess perun session
	 * @param host host to get attributes from
	 * @return list of attributes
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Host host) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the group on resource.
	 * @param sess
	 * @param resource
	 * @param group
	 * @return list of attributes
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group) throws InternalErrorException, WrongAttributeAssignmentException;

	List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, boolean workWithGroupAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get all <b>non-empty</b> member, user, member-resource and user-facility attributes.
	 *
	 * @param sess
	 * @param facility
	 * @param resource
	 * @param user
	 * @param member
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get all entityless attributes with subject equaled String key
	 *
	 * @param sess
	 * @param Key
	 * @return
	 * @throws InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, String Key) throws InternalErrorException;

	/**
	 * Get all entityless attributes with attributeName
	 * @param sess perun session
	 * @param attrName
	 * @return attribute
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getEntitylessAttributes(PerunSession sess, String attrName) throws  InternalErrorException;

	/**
	 * Returns list of Keys which fits the attributeDefinition.
	 * @param sess
	 * @param attributeDefinition
	 * @return
	 * @throws InternalErrorException
	 */
	List<String> getEntitylessKeys(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException;

	/**
	 * Returns entityless attribute by attr_id and key (subject) for update!
	 * 
	 * For update - means lock row with attr_values in DB (entityless_attr_values with specific subject and attr_id) 
	 *
	 * Not lock row in attr_names!
	 * 
	 * IMPORTANT: This method use "select for update" and locks row for transaction. Use clever.
	 *
	 * If attribute with subject=key not exists, create new one with null value and return it.
	 * 
	 * @param sess
	 * @param attrName
	 * @param key
	 * @return attr_value in string
	 * 
	 * @throws InternalErrorException if runtime error exception has been thrown
	 * @throws AttributeNotExistsException throw exception if attribute with value not exists in DB
	 */
	Attribute getEntitylessAttributeForUpdate(PerunSession sess, String key, String attrName) throws InternalErrorException, AttributeNotExistsException;
	
	/**
	 * Store the attributes associated with the facility. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * @param sess perun session
	 * @param facility facility to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Facility facility, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Just store the particular attribute associated with the facility, doesn't preform any value check. Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param facility facility to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute is not resource attribute or if it is core attribute
	 *
	 * @return true, if attribute was set in DB
	 */
	boolean setAttributeWithoutCheck(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Just store the particular attribute associated with the vo, doesn't preform any value check. Core attributes can't be set this way.
	 *
	 * @param sess
	 * @param vo
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setAttributeWithoutCheck(PerunSession sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Just store the particular attribute associated with the user-facility, doesn't preform any value check. Core attributes can't be set this way.
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setAttributeWithoutCheck(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Just store the particular attribute associated with the member-resource, doesn't preform any value check. Core attributes can't be set this way.
	 *
	 * @param sess
	 * @param resource
	 * @param member
	 * @param attribute
	 * @param workWithUserAttributes
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setAttributeWithoutCheck(PerunSession sess, Resource resource, Member member, Attribute attribute, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Just store the particular attribute associated with the member-group, doesn't preform any value check. Core attributes can't be set this way.
	 *
	 * @param sess
	 * @param member
	 * @param group
	 * @param attribute
	 * @param workWithUserAttributes
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setAttributeWithoutCheck(PerunSession sess, Member member, Group group, Attribute attribute, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Just store the particular attribute associated with the member, doesn't preform any value check. Core attributes can't be set this way.
	 *
	 * @param sess
	 * @param member
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setAttributeWithoutCheck(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Just store the particular attribute associated with the host, doesn't preform any value check. Core attributes can't be set this way.
	 *
	 * @param sess
	 * @param host
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setAttributeWithoutCheck(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Just store the particular attribute associated with the entityless, doesn't preform any value check. Core attributes can't be set this way.
	 *
	 * @param sess
	 * @param key
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setAttributeWithoutCheck(PerunSession sess, String key, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the vo. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * @param sess perun session
	 * @param vo vo to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not vo attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Vo vo, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the group. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * @param sess perun session
	 * @param group group to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not group attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the resource. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * @param sess perun session
	 * @param resource resource to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not resource attribute
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void setAttributes(PerunSession sess, Resource resource, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the resource and member combination. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * @param sess perun session
	 * @param resource resource to set on
	 * @param member member to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the resource and member combination. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 * If workWithUserAttributes is true, the method stores also the attributes associated with user, user-facility and member.
	 *
	 * @param sess perun session
	 * @param resource resource to set on
	 * @param member member to set on
	 * @param attributes attribute to set
	 * @param workWithUserAttributes method can process also user, user-facility and member attributes (user is automatically get from member a facility is get from resource)
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 *
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	void setAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the resource and member combination. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param group group to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, UserNotExistsException;

	/**
	 * Store the attributes associated with the resource and member combination. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 * If workWithUserAttributes is true, the method stores also the attributes associated with user and member.
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param group group to set on
	 * @param attributes attribute to set
	 * @param workWithUserAttributes method can process also user and member attributes (user is automatically get from member)
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, UserNotExistsException;

	/**
	 * Store the member, user, member-resource and user-facility attributes. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * @param sess perun session
	 * @param facility
	 * @param resource
	 * @param user
	 * @param member
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the resource. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the facility and user combination. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * @param sess perun session
	 * @param facility facility to set on
	 * @param user user to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the user. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * @param sess perun session
	 * @param user user to set on
	 * @param attributes attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, User user, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with the host. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 * @param sess perun session
	 * @param host host to set attributes for
	 * @param attributes attributes to be set
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException  if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not host attribute
	 */
	void setAttributes(PerunSession sess, Host host, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;
	/**
	 * Stores the group-resource attributes.
	 * @param sess
	 * @param resource
	 * @param group
	 * @param attributes
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	void setAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	void setAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes, boolean workWithGroupAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Get particular attribute for the facility.
	 *
	 * @param sess
	 * @param facility to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttribute(PerunSession sess, Facility facility, String attributeName) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the vo.
	 *
	 * @param sess
	 * @param vo to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttribute(PerunSession sess, Vo vo, String attributeName) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * This method get all similar attr_names which start with partOfAttributeName
	 *
	 * @param sess
	 * @param startingPartOfAttributeName is something like: urn:perun:user_facility:attribute-def:def:login-namespace:
	 * @return list of similar attribute names like: urn:perun:user_facility:attribute-def:def:login-namespace:cesnet etc.
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	List<String> getAllSimilarAttributeNames(PerunSession sess, String startingPartOfAttributeName) throws InternalErrorException;

	/**
	 * Get particular attribute for the group.
	 *
	 * @param sess
	 * @param group group get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttribute(PerunSession sess, Group group, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;


	/**
	 * Get particular attribute for the resource.
	 *
	 * @param sess
	 * @param resource to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttribute(PerunSession sess, Resource resource, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the member on this resource.
	 *
	 * @param sess
	 * @param resource to get attribute from
	 * @param member to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttribute(PerunSession sess, Resource resource, Member member, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the member in this group.
	 *
	 * @param sess
	 * @param member to get attribute from
	 * @param group to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttribute(PerunSession sess, Member member, Group group, String attributeName) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the member.
	 *
	 * @param sess
	 * @param member to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttribute(PerunSession sess, Member member, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the user on this facility.
	 *
	 * @param sess
	 * @param facility to get attribute from
	 * @param user to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttribute(PerunSession sess, Facility facility, User user, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the user.
	 *
	 * @param sess
	 * @param user to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttribute(PerunSession sess, User user, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the host
	 * @param sess
	 * @param host host to get attribute from
	 * @param attributeName attribute name
	 * @return attribute
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException
	 */
	Attribute getAttribute(PerunSession sess, Host host, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;
	/**
	 * Get particular group attribute on the resource
	 * @param sess
	 * @param resource
	 * @param group
	 * @param attributeName
	 * @return attribute
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException
	 */
	Attribute getAttribute(PerunSession sess, Resource resource, Group group, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular entityless attribute
	 * @param sess perun session
	 * @param key key to get attribute for
	 * @param attributeName
	 * @return attribute
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException  if the attribute doesn't exists in the underlaying data source
	 * @throws WrongAttributeAssignmentException if attribute isn't entityless attribute
	 */
	Attribute getAttribute(PerunSession sess, String key, String attributeName) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get attribute definition (attribute without defined value).
	 *
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	AttributeDefinition getAttributeDefinition(PerunSession sess, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get all attributes definition (attribute without defined value).
	 *
	 * @return List od attributes definitions
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<AttributeDefinition> getAttributesDefinition(PerunSession sess) throws InternalErrorException;

	/**
	 * Get all (for entities) attributeDefinitions which user has right to READ them and fill attribute writable (if user has also right to WRITE them).
	 * For entities means that return only those attributeDefinition which are in namespace of entities or possible combination of entities.
	 * For Example: If enityties are "member, user, resource" then return only AD in namespaces "member, user, resource and resource-member"
	 *
	 * @param sess
	 * @param entities list of perunBeans (member, user...)
	 *
	 * @return list of AttributeDefinitions with rights (writable will be filled correctly by user in session)
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	List<AttributeDefinition> getAttributesDefinitionWithRights(PerunSession sess, List<PerunBean> entities) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * From listOfAttributesNames get list of attributeDefinitions
	 *
	 * @param sess
	 * @param listOfAttributesNames
	 * @return list of AttributeDefinitions
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	List<AttributeDefinition> getAttributesDefinition(PerunSession sess, List<String> listOfAttributesNames) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get attribute definition (attribute without defined value).
	 *
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	AttributeDefinition getAttributeDefinitionById(PerunSession sess, int id) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get attributes definition (attribute without defined value) with specified namespace.
	 *
	 * @param namespace get only attributes with this namespace
	 * @return List of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<AttributeDefinition> getAttributesDefinitionByNamespace(PerunSession sess, String namespace) throws InternalErrorException;

	/**
	 * Get particular attribute for the facility.
	 *
	 * @param sess
	 * @param facility to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttributeById(PerunSession sess, Facility facility, int id) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the vo.
	 *
	 * @param sess
	 * @param vo to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttributeById(PerunSession sess, Vo vo, int id) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the resource.
	 *
	 * @param sess
	 * @param resource to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttributeById(PerunSession sess, Resource resource, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the member on this resource.
	 *
	 * @param sess
	 * @param resource to get attribute from
	 * @param member to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttributeById(PerunSession sess, Resource resource, Member member, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the member in this group. Also it can return only member or only user attribute
	 * if attr definition is not from NS_MEMBER_GROUP_ATTR but from NS_MEMBER_ATTR or NS_GROUP_ATTR
	 *
	 * @param sess perun session
	 * @param member to get attribute from
	 * @param group to get attribute from
	 * @param id attribute id
	 * @return memberGroup, member OR user attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttributeById(PerunSession sess, Member member, Group group, int id) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * Get particular attribute for the member.
	 *
	 * @param sess
	 * @param member to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttributeById(PerunSession sess, Member member, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the user on this facility.
	 *
	 * @param sess
	 * @param facility to get attribute from
	 * @param user to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttributeById(PerunSession sess, Facility facility, User user, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the user.
	 *
	 * @param sess
	 * @param user to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute getAttributeById(PerunSession sess, User user, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the host
	 * @param sess perun session
	 * @param host to get attribute from
	 * @param id id of attribute
	 * @return attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute is not host attribute
	 * @throws AttributeNotExistsException if attribute doesn't exist
	 */
	Attribute getAttributeById(PerunSession sess, Host host, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular group-resource attribute
	 * @param sess
	 * @param resource
	 * @param group
	 * @param id
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException
	 */
	Attribute getAttributeById(PerunSession sess, Resource resource, Group group, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get particular group attribute
	 * @param sess
	 * @param group
	 * @param id
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException
	 */
	Attribute getAttributeById(PerunSession sess, Group group, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get and set required attribute for member, resource, user and facility.
	 * 
	 * Procedure:
	 * 1] Get all member, member-resource, user, user-facility required attributes for member and resource.
	 * 2] Fill attributes and store those which were really filled. (value changed)
	 * 3] Set filled attributes.
	 * 4] Refresh value in all virtual attributes.
	 * 5] Check all attributes and their dependencies.
	 * 
	 * @param sess
	 * @param member
	 * @param resource
	 * @param user
	 * @param facility
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeValueException 
	 */
	void setRequiredAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeValueException;
	
	/**
	 * Get and set required attribute for member, resource, user, facility and specific service.
	 *
	 * Procedure:
	 * 1] Get all member, member-resource, user, user-facility required attributes for member, resource and specific service.
	 * 2] Fill attributes and store those which were really filled. (value changed)
	 * 3] Set filled attributes.
	 * 4] Refresh value in all virtual attributes.
	 * 5] Check all attributes and their dependencies.
	 *
	 * @param sess
	 * @param service
	 * @param facility
	 * @param resource
	 * @param user
	 * @param member
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeValueException 
	 */
	void setRequiredAttributes(PerunSession sess, Service service, Facility facility, Resource resource, User user, Member member) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeValueException;

	/**
	 * Get and set required attributes from arrayList for member, resource, user and facility.
	 *
	 * IMPORTANT: set all attrs from arrayList, set not required attrs too if they are in arrayList
	 *
	 * Procedure:
	 * 1] Get all attrs from arrayList
	 * 2] Fill attributes and store those which were really filled. (value changed)
	 * 3] Set filled attributes.
	 * 4] Refresh value in all virtual attributes.
	 * 5] Check all attributes and their dependencies.
	 * 
	 * @param sess
	 * @param facility
	 * @param resource
	 * @param user
	 * @param member
	 * @param attributes
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeValueException 
	 */
	void setRequiredAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeValueException;

	/**
	 * Store the particular attribute associated with the facility. Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param facility facility to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not facility attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular attribute associated with the vo. Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param vo vo to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not vo attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular attribute associated with the group. Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param group group to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not group attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;


	/**
	 * Store the particular attribute associated with the resource. Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param resource resource to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not resource attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Just store the particular attribute associated with the resource, doesn't preform any value check. Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param resource resource to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute is not resource attribute or if it is core attribute
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 *
	 * @return true, if attribute was set in DB
	 */
	boolean setAttributeWithoutCheck(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Just store the particular attribute associated with the group, doesn't preform any value check. Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param group
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute is not group attribute or if it is core attribute
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 *
	 * @return true, if attribute was set in DB
	 */
	boolean setAttributeWithoutCheck(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular attribute associated with the resource and member combination.  Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param resource resource to set on
	 * @param member member to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular attribute associated with the group and member combination. Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param group group to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular attribute associated with the member.  Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular attribute associated with the member. Core attributes can't be set this way.
	 *
	 * This method creates nested transaction to prevent storing value to DB if it throws any exception.
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource attribute or if it is core attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributeInNestedTransaction(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;


	/**
	 * Store the attribute associated with the facility and user combination.  Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param facility facility to set on
	 * @param user user to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attribute associated with the user. Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param user user to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Store the attribute associated with the user. Core attributes can't be set this way.
	 *
	 * This method creates nested transaction to prevent storing value to DB if it throws any exception.
	 *
	 * @param sess perun session
	 * @param user user to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributeInNestedTransaction(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;


	/**
	 * Just store the attribute associated with the user, doesn't preform any value check.  Core attributes can't be set this way.
	 *
	 * @param sess perun session
	 * @param user user to set on
	 * @param attribute attribute to set
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute is not user-facility attribute
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 *
	 * @return true, if attribute was set in DB
	 */
	boolean setAttributeWithoutCheck(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Store the attribute associated with the host. Core attributes can't be set this way.
	 * @param sess perun session
	 * @param host host to set attributes for
	 * @param attribute attribute to be set
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute is not host attribute
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 */
	void setAttribute(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Store the group-resource attribute
	 * @param sess
	 * @param resource
	 * @param group
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Just store the group-resource attribute, do not preform any value check.
	 * @param sess
	 * @param resource
	 * @param group
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException Can be raised while storing virtual attribute if another attribute which is required for set virtual attribute have wrong value
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 *
	 * @return true, if attribute was set in DB
	 */
	boolean setAttributeWithoutCheck(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Stores entityless attribute (associated with string key).
	 * @param sess perun session
	 * @param key store the attribute for this key
	 * @param attribute attribute to set
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not entityless attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttribute(PerunSession sess, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Creates an attribute, the attribute is stored into the appropriate DB table according to the namespace
	 *
	 * @param sess perun session
	 * @param attributeDefinition attribute to create
	 *
	 * @return attribute with set id
	 *
	 * @throws AttributeExistsException if attribute already exists
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	AttributeDefinition createAttribute(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, AttributeExistsException;

	/**
	 * Deletes the attribute.
	 *
	 * @param sess
	 * @param attributeDefinition
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws RelationExistsException
	 */
	void deleteAttribute(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, RelationExistsException;

	/**
	 * Delete all authz for the attribute.
	 *
	 * @param sess
	 * @param attribute the attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorExceptions
	 */
	void deleteAllAttributeAuthz(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Deletes the attribute.
	 *
	 * @param sess
	 * @param attributeDefinition attribute to delete
	 * @param force delete also all existing relation. If this parameter is true the RelationExistsException is never thrown.
	 * @throws InternalErrorException
	 * @throws RelationExistsException
	 */
	void deleteAttribute(PerunSession sess, AttributeDefinition attributeDefinition, boolean force) throws InternalErrorException, RelationExistsException;

	/**
	 * Get attributes definions required by all services assigned on the resource.
	 *
	 * @param sess perun session
	 * @param resource
	 * @return attributes definions required by all services assigned on the resource.
	 *
	 * @throws InternalErrorException
	 */
	List<AttributeDefinition> getResourceRequiredAttributesDefinition(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Get facility attributes which are required by services. Services are known from the resource.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param facility you get attributes for this facility
	 * @return list of facility attributes which are required by services which are assigned to resource
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility) throws InternalErrorException;

	/**
	 * Get resource attributes which are required by services. Services are known from the resourceToGetServicesFrom.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param resource resource from which the services are taken and for which you want to get the attributes
	 * @return list of resource attributes which are required by services which are assigned to resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource) throws InternalErrorException;

	/**
	 * Get member-resource attributes which are required by services. Services are known from the resourceToGetServicesFrom.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param resource you get attributes for this resource and the member
	 * @param member you get attributes for this member
	 * @return list of facility attributes which are required by services which are assigned to resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Member member) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get member-resource attributes which are required by services and if workWithUserAttributes is true also user, user-facility and member attributes.
	 * Services are known from the resourceToGetServicesFrom.
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
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Member member, boolean workWithUserAttributes) throws WrongAttributeAssignmentException, InternalErrorException;

	/**
	 * Get member-group attributes which are required by services. Services are known from the resourceToGetServicesFrom.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param member you get attributes for this member
	 * @param group you get attributes for this group and the member
	 * @return list of member-group's attributes which are required by services defined on specified resource
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get member-group attributes which are required by services if workWithUserAttributes is true also user and member attributes.
	 * Services are known from the resourceToGetServicesFrom.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param member you get attributes for this member
	 * @param group you get attributes for this group and the member
	 * @param workWithUserAttributes method can process also user and member attributes (user is automatically get from member)
	 * @return list of member-group's attributes which are required by services defined on specified resource
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get member, user, member-resource and user-facility attributes which are required by services which are defined on "resourceToGetServicesFrom" resource.
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
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility, Resource resource, User user, Member member) throws WrongAttributeAssignmentException, InternalErrorException;

	/**
	 * Get member attributes which are required by services defined on specified resource
	 *
	 * @param sess perun session
	 * @param member you get attributes for this member
	 * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
	 * @return list of member attributes which are required by services defined on specified resource
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member) throws InternalErrorException;


	/**
	 * Get user-facility attributes which are required by services. Services are known from the resource.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param facility facility from which the services are taken
	 * @param user you get attributes for this user
	 * @return list of user-facility attributes which are required by service
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility, User user) throws InternalErrorException;

	/**
	 * Get user attributes which are required by services. Services are known from the resource.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param user you get attributes for this user
	 * @return list of users attributes which are required by service
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, User user) throws InternalErrorException;

	/**
	 * Get the group-resource attributes which are required by services. Services are known from the resource.
	 * @param sess
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param resource
	 * @param group
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group) throws InternalErrorException, WrongAttributeAssignmentException;

	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group, boolean workWithGroupAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 *  Get the host attributes which are required by services. Services are known from the resource.
	 * @param sess
	 * @param resourceToGetServicesFrom
	 * @param host
	 * @return
	 * @throws InternalErrorException
	 */
	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Host host) throws InternalErrorException;

	List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Group group) throws InternalErrorException;

	/**
	 *  Get facility attributes which are required by all services which are connected to this facility.
	 *
	 * @param sess perun session
	 * @param facility you get attributes for this facility
	 * @return list of facility attributes which are required by all services which are connected to this facility.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Get resource attributes which are required by selected services.
	 * 
	 * @param sess perun session
	 * @param resource you get attributes for this resource
	 * @param services
	 * @return list of resource attributes which are required by selected services.
	 * @throws InternalErrorException 
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Resource resource) throws InternalErrorException;

	/**
	 * Get resource attributes which are required by services which is related to this resource.
	 *
	 * @param sess perun session
	 * @param resource resource for which you want to get the attributes
	 * @return list of resource attributes which are required by services which are assigned to resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Get member-resource attributes which are required by services which are relater to this member-resource.
	 *
	 * @param sess perun session
	 * @param resource you get attributes for this resource and the member
	 * @param member you get attributes for this member and the resource
	 * @return list of facility attributes which are required by services which are assigned to resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * If workWithUserAttribute is false => Get member-resource attributes which are required by services which are relater to this member-resource.
	 * If workWithUserAttributes is true => Get member-resource, user-facility, user and member attributes. (user is get from member and facility from resource)
	 *
	 * @param sess perun session
	 * @param resource you get attributes for this resource and the member
	 * @param member you get attributes for this member and the resource
	 * @param workWithUserAttributes method can process also user, user-facility and member attributes (user is automatically get from member a facility is get from resource)
	 * @return list of member-resource attributes or if workWithUserAttributes is true return list of member-resource, user, member and user-facility attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 *
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Member member, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException;


	/**
	 * Get user-facility attributes which are required by services which are related to this user-facility.
	 *
	 * @param sess perun session
	 * @param facility facility from which the services are taken
	 * @param user you get attributes for this user
	 * @return list of facility attributes which are required by services which are assigned to facility.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException;

	/**
	 * Get user attributes which are required by services which are relater to this user.
	 *
	 * @param sess perun session
	 * @param user
	 * @return list of user's attributes which are required by services which are related to this user
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Get member attributes which are required by services which are relater to this member and
	 * if is workWithUserAttributes = true, then also user required attributes
	 *
	 * @param sess perun session
	 * @param member you get attributes for this member
	 *
	 * @return list of member, user attributes which are required by services which are related to this member
	 * @param workWithUserAttributes method can process also user and user-facility attributes (user is automatically get from member a facility is get from resource)
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Member member, boolean workWithUserAttributes) throws InternalErrorException;


	/**
	 * Get all attributes which are required by service.
	 * Required attributes are requisite for Service to run.
	 *
	 * @param sess sess
	 * @param service service from which the attributes will be listed
	 *
	 * @return All attributes which are required by service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<AttributeDefinition> getRequiredAttributesDefinition(PerunSession sess, Service service) throws InternalErrorException;

	/**
	 * Get facility attributes which are required by the service.
	 *
	 * @param sess perun session
	 * @param facility you get attributes for this facility
	 * @param service attribute required by this service you'll get
	 * @return list of facility attributes which are required by the service
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility) throws InternalErrorException;

	/**
	 * Get vo attributes which are required by the service.
	 *
	 * @param sess perun session
	 * @param vo you get attributes for this vo
	 * @param service attribute required by this service you'll get
	 * @return list of vo attributes which are required by the service
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Vo vo) throws InternalErrorException;

	/**
	 * Get resource attributes which are required by the service.
	 *
	 * @param sess perun session
	 * @param resource resource for which you want to get the attributes
	 * @param service attribute required by this service you'll get
	 * @return list of resource attributes which are required by the service
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource) throws InternalErrorException;

	/**
	 * Get member-resource attributes which are required by the service.
	 *
	 * @param sess perun session
	 * @param resource you get attributes for this resource and the member
	 * @param member you get attributes for this member and the resource
	 * @param service attribute required by this service you'll get
	 * @return list of attributes which are required by the service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Member member) throws InternalErrorException, WrongAttributeAssignmentException;

	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Member member, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get member-resource, member, user-facility and user attributes which are required by service for each member in list of members.
	 * If workWithUserAttributes is false return only member-resource attributes.
	 * !!! Method checks if members list is not empty (returns empty HashMap)!!!
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
	HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility, Resource resource, List<Member> members, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get member-resource attributes which are required by service for each member in list of members.
	 * !!! Method checks if members list is not empty (returns empty HashMap)!!!
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param resource you get attributes for this resource and the members
	 * @param members you get attributes for this list of members and the resource
	 * @return map of member objects and his list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource, List<Member> members) throws InternalErrorException;

	/**
	 * Get member attributes which are required by service for each member in list of members.
	 * !!! Method checks if members list is not empty (returns empty HashMap)!!!
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param resource resource only to get allowed members
	 * @param members you get attributes for this list of members
	 * @return map of member objects and his list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Resource resource, Service service, List<Member> members) throws InternalErrorException;

	/**
	 * Get user-facility attributes which are required by the service for each user in list of users.
	 * !!! Method checks if members list is not empty (returns empty HashMap)!!!
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param facility you get attributes for this facility and user
	 * @param users you get attributes for this user and facility
	 * @return map of user and his list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility, List<User> users) throws InternalErrorException;

	/**
	 * Get user attributes which are required by the service for each user in list of users.
	 * !!! Method checks if members list is not empty (returns empty HashMap)!!!
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param users you get attributes for this user and facility
	 * @return map of user and his list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, List<User> users) throws InternalErrorException;

	/**
	 * Get member-group attributes which are required by the service.
	 *
	 * @param sess perun session
	 * @param member you get attributes for this member and the group
	 * @param group you get attributes for this group in which member is associated
	 * @param service attribute required by this service you'll get
	 * @return list of attributes which are required by the service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group) throws InternalErrorException, WrongAttributeAssignmentException;

	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get memner, user, member-resource, user-facility attributes which are required by the service.
	 *
	 *
	 * @param sess
	 * @param service
	 * @param facility
	 * @param resource
	 * @param user
	 * @param member
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility, Resource resource, User user, Member member) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get group-resource attributes which are required by the service.
	 *
	 * @param sess perun session
	 * @param resource
	 * @param group
	 * @param service attribute required by this service you'll get
	 * @param withGroupAttributes get also group attributes (which is required by the service) for this group
	 * @return list of attributes which are required by the service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group, boolean withGroupAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group) throws InternalErrorException, WrongAttributeAssignmentException;

	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Group group) throws InternalErrorException;

	/**
	 * Get host attributes which are required by service
	 * @param sess
	 * @param service
	 * @param host
	 * @return
	 * @throws InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Host host) throws InternalErrorException;

	/**
	 * Get member attributes which are required by the service.
	 *
	 * @param sess perun session
	 * @param member you get attributes for this member
	 * @param service attribute required by this service you'll get
	 * @return list of attributes which are required by the service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member) throws InternalErrorException;

	/**
	 * Get user attributes which are required by the service.
	 *
	 * @param sess perun session
	 * @param user you get attributes for this user
	 * @param service attribute required by this service you'll get
	 * @return list of attributes which are required by the service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, User user) throws InternalErrorException;

	/**
	 * Get user-facility attributes which are required by the service.
	 *
	 * @param sess perun session
	 * @param user you get attributes for this user and the facility
	 * @param facility you get attributes for this facility and the user
	 * @param service attribute required by this service you'll get
	 * @return list of attributes which are required by the service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility, User user) throws InternalErrorException;

	/**
	 * This method try to fill a value of the resource attribute. Value may be copied from some facility attribute.
	 *
	 * @param sess perun session
	 * @param resource resource, attribute of which you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 *  Batch version of fillAttribute. This method skips all attributes with not-null value.
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,Resource,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Resource resource, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * This method try to fill value of the member-resource attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * @param sess perun session
	 * @param resource  attribute of this resource (and member) and you want to fill
	 * @param member attribute of this member (and resource) and you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 *  Batch version of fillAttribute. This method skips all attributes with not-null value.
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,Resource,Member,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * @param workWithUserAttributes method can process also user and user-facility attributes (user is automatically get from member a facility is get from resource)
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	List<Attribute> fillAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * This method tries to fill value of the member-group attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * @param sess perun session
	 * @param member attribute of this member (and group) you want to fill
	 * @param group attribute of this group (and member) you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 *  Batch version of fillAttribute. This method skips all attributes with not-null value.
	 *  @throws WrongAttributeAssignmentException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,Member,Group,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * @param workWithUserAttributes method can process also user and memebr attributes (user is automatically get from member)
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	List<Attribute> fillAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 *
	 * This method try to fill value of the user, member, member-resource and user-facility attributes. This value is automatically generated, but not all attributes can be filled this way.
	 * This method skips all attributes with not-null value.
	 *
	 * @param sess
	 * @param facility
	 * @param resource
	 * @param user
	 * @param member
	 * @param attributes
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Attribute> fillAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 *
	 * This method try to fill value of the user, member, member-resource and user-facility attributes. This value is automatically generated, but not all attributes can be filled this way.
	 * This method skips all attributes with not-null value.
	 * 
	 * if returnOnlyAttributesWithChangedValue is true - return only attributes which changed value by filling new one
	 * If false, has the same functionality like fillAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes)
	 * 
	 * @param sess
	 * @param facility
	 * @param resource
	 * @param user
	 * @param member
	 * @param attributes
	 * @param returnOnlyAttributesWithChangedValue
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException 
	 */
	public List<Attribute> fillAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes, boolean returnOnlyAttributesWithChangedValue) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * This method try to fill value of the member attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * @param sess perun session
	 * @param member attribute of this member (and resource) and you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 *  Batch version of fillAttribute. This method skips all attributes with not-null value.
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,Resource,Member,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * This method try to fill value of the user-facility attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * @param sess perun session
	 * @param facility  attribute of this facility (and user) and you want to fill
	 * @param user attribute of this user (and facility) and you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 *  Batch version of fillAttribute. This method skips all attributes with not-null value.
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,Facility,User,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * This method try to fill value of the user attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * @param sess perun session
	 * @param user attribute of this user (and facility) and you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 *  Batch version of fillAttribute. This method skips all attributes with not-null value.
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,User,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, User user, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * This method tries to fill value of the host attribute. This value is automatically generated, but not all attributes can be filled this way
	 * @param sess
	 * @param host
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which may have filled value
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	Attribute fillAttribute(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;


	List<Attribute> fillAttributes(PerunSession sess, Group group, List<Attribute> groupReqAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Batch version of fillAttribute. This method skips all attributes with not-null value.
	 * @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession,Host,Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Host host, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * This method tries to fill value of group-resource attribute. This value is automatically generated, but not all attributes can be filled this way
	 * @param sess
	 * @param resource
	 * @param group
	 * @param attribute
	 * @return attribute which may have filled value
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Batch version of fillAttribute. This method skips all attributes with not-null value.
	 * @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Resource, cz.metacentrum.perun.core.api.Group, cz.metacentrum.perun.core.api.Attribute)
	 */
	List<Attribute> fillAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	List<Attribute> fillAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attribute, boolean workWithGroupAttributes) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Check if value of this facility attribute is valid.
	 *
	 * @param sess perun session
	 * @param facility facility for which you want to check validity of attribute
	 * @param attribute attribute to check
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't facility attribute
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 *  Batch version of checkAttributeValue
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Facility,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Facility facility, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this vo attribute is valid.
	 *
	 * @param sess perun session
	 * @param vo vo for which you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't vo attribute
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 *  Batch version of checkAttributeValue
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Vo,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Vo vo, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this group attribute is valid.
	 *
	 * @param sess perun session
	 * @param group group for which you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't group attribute
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 *  Batch version of checkAttributeValue
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Group,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;


	/**
	 * Check if value of this resource attribute is valid.
	 *
	 * @param sess perun session
	 * @param resource resource for which you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 *  Batch version of checkAttributeValue
	 *  @throws WrongAttributeValueException if any of attributes values is wrong/illegal
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Resource,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Resource resource, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this member-resource attribute is valid.
	 *
	 *
	 * @param sess perun session
	 * @param resource resource for which (and for specified member) you want to check validity of attribute
	 * @param member member for which (and for specified resource) you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 *  Batch version of fillAttribute
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Resource,Member,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Resource resource, Member member, List<Attribute> attributes) throws InternalErrorException,  WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 *  Batch version of fillAttribute
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Resource,Member,Attribute)
	 * @param workWithUserAttributes method can process also user and user-facility attributes (user is automatically get from member a facility is get from resource)
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	void checkAttributesValue(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;


	/**
	 * Check if value of this member-group attribute is valid.
	 *
	 * @param sess perun session
	 * @param group group for which (and for specified member) you want to check validity of attribute
	 * @param member member for which (and for specified group) you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 *  Batch version of fillAttribute
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Member,Group,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 *  Batch version of fillAttribute
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Member,Group,Attribute)
	 * @param workWithUserAttributes method can process also user and member attributes (user is automatically get from member)
	 * !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
	 */
	void checkAttributesValue(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of attributes is valied. Attributes can be from namespace: member, user, member-resource and user-facility.
	 *
	 * @param sess
	 * @param facility
	 * @param resource
	 * @param user
	 * @param member
	 * @param attributes
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributesValue(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this member attribute is valid.
	 *
	 *
	 * @param sess perun session
	 * @param member member for which (and for specified resource) you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 *  Batch version of fillAttribute
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Resource,Member,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this user-facility attribute is valid.
	 *
	 *
	 * @param sess perun session
	 * @param facility facility for which (and for specified user) you want to check validity of attribute
	 * @param user user for which (and for specified facility) you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 *  Batch version of checkAttributeValue
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Facility,User,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this group-resource attribute is valid
	 * @param sess perun session
	 * @param resource
	 * @param group
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't group-resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */

	void checkAttributeValue(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException,WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * batch version of checkAttributeValue
	 *@see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Resource, cz.metacentrum.perun.core.api.Group, cz.metacentrum.perun.core.api.Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Resource resource, Group group, List<Attribute> attribute) throws InternalErrorException,WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	void checkAttributesValue(PerunSession sess, Resource resource, Group group, List<Attribute> attribute, boolean workWithGroupAttribute) throws InternalErrorException,WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this user attribute is valid.
	 *
	 *
	 * @param sess perun session
	 * @param user user for which (and for specified facility) you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 *  Batch version of checkAttributeValue
	 *  @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,User,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, User user, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Check if the value of this host attribute is valid
	 * @param sess perun session
	 * @param host host which attribute validity is checked
	 * @param attribute attribute to check
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if the attribute isn't host attribute
	 */
	void checkAttributeValue(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException,WrongAttributeValueException,WrongAttributeAssignmentException;

	/**
	 * Batch version of checkAttributeValue
	 * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeValue(PerunSession,Host,Attribute)
	 */
	void checkAttributesValue(PerunSession sess, Host host, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException,WrongAttributeAssignmentException;

	/**
	 * Check if the value of this entityless attribute is valid
	 * @param sess perun session
	 * @param key check the attribute for this key
	 * @param attribute attribute to check
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if the attribute isn't entityless attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, String key, Attribute attribute) throws InternalErrorException,WrongAttributeValueException,WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this group attribute is valid no matter if attribute is required or not.
	 *
	 * @param sess perun session
	 * @param group group for which you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't group attribute
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException
	 */
	void forceCheckAttributeValue(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this resource attribute is valid no matter if attribute is required or not.
	 *
	 * @param sess perun session
	 * @param resource resource for which you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException if attribute isn't resource attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void forceCheckAttributeValue(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;


	/**
	 * Unset particular attribute for the facility. Core attributes can't be removed this way.
	 *
	 * @param sess perun session
	 * @param facility remove attribute from this facility
	 * @param attribute attribute to remove
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't facility attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Facility facility, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular entityless attribute with subject equals key.
	 *
	 * @param sess perun session
	 * @param key subject of entityless attribute
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException if the attribute isn't entityless attribute
	 * @throws WrongAttributeAssignmentException
	 */
	void removeAttribute(PerunSession sess, String key, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException,  WrongReferenceAttributeValueException;

	/**
	 * Unset the group_resource attributes. If an attribute is core attribute, then the attribute isn't unseted (it's skipped without notification).
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
	 * @param workWithGroupAttributes if true, remove also group attributes, if false, remove only group_resource attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 * @throws WrongAttributeAssignmentException if attribute is not group-resource or group attribute
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongReferenceAttributeValueException if some reference attribute has illegal value
	 */
	void removeAttributes(PerunSession sess, Resource resource, Group group, List<? extends AttributeDefinition> attributes, boolean workWithGroupAttributes) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

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
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongReferenceAttributeValueException if some reference attribute has illegal value
	 * @throws WrongAttributeAssignmentException if attribute is not group-resource or group attribute
	 */
	void removeAllAttributes(PerunSession sess, Resource resource, Group group, boolean workWithGroupAttributes) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;


	/**
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession,Facility,AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, Facility facility, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all <b>non-empty</b> attributes associated with the member and if workWithUserAttributes is
	 * true, unset all <b>non-empty</b> attributes associated with user, who is this member.
	 *
	 * @param sess perun session
	 * @param member remove attribute from this member
	 * @param workWithUserAttributes true if I want to unset all attributes associated with user, who is the member too
	 * @param attributes attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException if attribute isn't member attribute or if it is core attribute
	 */
	void removeAttributes(PerunSession sess, Member member, boolean workWithUserAttributes, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset the member, user, member-resource and user-facility attributes. If an attribute is core attribute then the attribute isn't unseted (It's skipped without any notification).
	 *
	 * @param sess perun session
	 * @param facility
	 * @param resource resource to set on
	 * @param user
	 * @param member member to set on
	 * @param attributes
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member-resource, user, member or user-facility attribute
	 * @throws WrongReferenceAttributeValueException
	 */
	void removeAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the facility.
	 *
	 * @param sess perun session
	 * @param facility remove attributes from this facility
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Facility facility) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the facility.
	 * If removeAlsoUserFacilityAttributes is true, remove all user-facility attributes of this facility and any user allowed in this facility.
	 *
	 * @param sess perun session
	 * @param facility remove attributes from this facility
	 * @param removeAlsoUserFacilityAttributes if true, remove all user-facility attributes for any user in this facility too
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void removeAllAttributes(PerunSession sess, Facility facility, boolean removeAlsoUserFacilityAttributes) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the vo. Core attributes can't be removed this way.
	 *
	 * @param sess perun session
	 * @param vo remove attribute from this vo
	 * @param attribute attribute to remove
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't vo attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Vo vo, AttributeDefinition attribute)
	 */
	void removeAttributes(PerunSession sess, Vo vo, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Store the attributes associated with member and user (which we get from this member) if workWithUserAttributes is true.
	 * If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param attributes attribute to set
	 * @param workWithUserAttributes true/false If true, we can use user attributes (get from this member) too
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is illegal
	 * @throws WrongAttributeAssignmentException if attribute is not member attribute or with workWithUserAttributes=true, if its not member or user attribute.
	 * @throws WrongReferenceAttributeValueException
	 */
	void setAttributes(PerunSession sess, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;


	/**
	 * Unset all attributes for the vo.
	 *
	 * @param sess perun session
	 * @param vo remove attributes from this vo
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Vo vo) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the group. Core attributes can't be removed this way.
	 *
	 * @param sess perun session
	 * @param group remove attribute from this group
	 * @param attribute attribute to remove
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't group attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Group group, AttributeDefinition attribute)
	 */
	void removeAttributes(PerunSession sess, Group group, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the group.
	 *
	 * @param sess perun session
	 * @param group remove attributes from this group
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Group group) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the resource. Core attributes can't be removed this way.
	 *
	 * @param sess perun session
	 * @param resource remove attribute from this resource
	 * @param attribute attribute to remove
	 *
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't resource attribute or if it is core attribute
	 */
	boolean removeAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute)
	 */
	void removeAttributes(PerunSession sess, Resource resource, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the resource.
	 *
	 * @param sess perun session
	 * @param resource remove attributes from this resource
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Resource resource) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the member on the resource. Core attributes can't be removed this way.
	 *
	 * @param sess perun session
	 * @param member remove attribute from this member
	 * @param resource remove attributes for this resource
	 * @param attribute attribute to remove
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Resource resource, Member member, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 */
	void removeAttributes(PerunSession sess, Resource resource, Member member, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the member on the resource.
	 *
	 * @param sess perun session
	 * @param member remove attributes from this member
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * Unset particular attribute for the member in the group. Core attributes can't be removed this way.
	 *
	 * @param sess perun session
	 * @param group remove attributes for this group
	 * @param member remove attribute from this member
	 * @param attribute attribute to remove
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Member, Group, AttributeDefinition)
	 */
	void removeAttributes(PerunSession sess, Member member, Group group, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	void removeAttributes(PerunSession sess, Member member, Group group, List<? extends AttributeDefinition> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the member in the group.
	 *
	 * @param sess perun session
	 * @param group remove attributes for this group
	 * @param member remove attributes from this member
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	void removeAllAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the member. Core attributes can't be removed this way.
	 *
	 * @param sess perun session
	 * @param member remove attribute from this member
	 * @param attribute attribute to remove
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Member member, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Resource resource, Member member, AttributeDefinition attribute)
	 */
	void removeAttributes(PerunSession sess, Member member, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the member.
	 *
	 * @param sess perun session
	 * @param member remove attributes from this member
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Member member) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the user on the facility. Core attributes can't be removed this way.
	 *
	 * @param sess perun session
	 * @param user remove attribute from this user
	 * @param facility remove attributes for this facility
	 * @param attribute attribute to remove
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute)
	 */
	void removeAttributes(PerunSession sess, Facility facility, User user, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes (user-facility) for the user on the facility.
	 *
	 * @param sess perun session
	 * @param facility
	 * @param user
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all <b>non-virtual</b> user-facility attributes for the user and <b>all facilities</b>
	 *
	 * @param sess perun session
	 * @param user
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void removeAllUserFacilityAttributes(PerunSession sess, User user) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the user. Core attributes can't be removed this way.
	 *
	 * @param sess perun session
	 * @param user remove attribute from this user
	 * @param attribute attribute to remove
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute or if it is core attribute
	 */
	void removeAttribute(PerunSession sess, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute)
	 */
	void removeAttributes(PerunSession sess, User user, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the user.
	 *
	 * @param sess perun session
	 * @param user remove attributes from this user
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, User user) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular attribute for the host. Core attributes can't be removed this way.
	 * @param sess
	 * @param host
	 * @param attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute is not host attribute
	 */
	void removeAttribute(PerunSession sess, Host host, AttributeDefinition attribute) throws InternalErrorException,WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this way.
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Host host, AttributeDefinition attribute)
	 */
	void removeAttributes(PerunSession sess, Host host, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all attributes for the host.
	 *
	 * @param sess perun session
	 * @param host remove attributes from this host
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Host host) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular group-resource attribute
	 * @param sess
	 * @param resource
	 * @param group
	 * @param attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	void removeAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException,WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Batch version of removeAttribute.
	 * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute)
	 */
	void removeAttributes(PerunSession sess, Resource resource, Group group, List<? extends AttributeDefinition> attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset all group-resource attributes
	 * @param sess
	 * @param resource
	 * @param group
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Resource resource, Group group) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * Unset all attributes for the key (entityless) without check of value.
	 *
	 * @param sess
	 * @param key
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws WrongAttributeAssignmentException if attribute isn't entityless attribute or if it is core attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttributeWithoutCheck(PerunSession sess, String key, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Unset all attributes for the facility without check of value.
	 *
	 * @param sess
	 * @param facility
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws WrongAttributeAssignmentException if attribute isn't facility attribute or if it is core attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttributeWithoutCheck(PerunSession sess, Facility facility, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Unset all attributes for the host without check of value.
	 *
	 * @param sess
	 * @param host
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws WrongAttributeAssignmentException if attribute isn't host attribute or if it is core attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttributeWithoutCheck(PerunSession sess, Host host, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Unset all attributes for the vo without check of value.
	 *
	 * @param sess
	 * @param vo
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws WrongAttributeAssignmentException if attribute isn't vo attribute or if it is core attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttributeWithoutCheck(PerunSession sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Unset all attributes for the group without check of value.
	 *
	 * @param sess
	 * @param group
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws WrongAttributeAssignmentException if attribute isn't group attribute or if it is core attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttributeWithoutCheck(PerunSession sess, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Unset all attributes for the resource without check of value.
	 *
	 * @param sess
	 * @param resource
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws WrongAttributeAssignmentException if attribute isn't resource attribute or if it is core attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttributeWithoutCheck(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Unset all attributes for the member-resource without check of value.
	 *
	 * @param sess
	 * @param resource
	 * @param member
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute or if it is core attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttributeWithoutCheck(PerunSession sess, Resource resource, Member member, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Unset all attributes for the member-group without check of value.
	 *
	 * @param sess
	 * @param member
	 * @param group
	 * @param attribute
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	boolean removeAttributeWithoutCheck(PerunSession sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Unset all attributes for the member without check of value.
	 *
	 * @param sess
	 * @param member
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws WrongAttributeAssignmentException if attribute isn't member attribute or if it is core attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttributeWithoutCheck(PerunSession sess, Member member, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Unset all attributes for the user-facility without check of value.
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute or if it is core attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttributeWithoutCheck(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Unset all attributes for the user without check of value.
	 *
	 * @param sess
	 * @param user
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws WrongAttributeAssignmentException if attribute isn't user attribute or if it is core attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttributeWithoutCheck(PerunSession sess, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Unset all attributes for the group-resource without check of value.
	 *
	 * @param sess
	 * @param resource
	 * @param group
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws WrongAttributeAssignmentException if attribute isn't group-resource attribute or if it is core attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttributeWithoutCheck(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	void checkAttributeExists(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException, AttributeNotExistsException;

	void checkAttributesExists(PerunSession sess, List<? extends AttributeDefinition> attributes) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Determine if attribute is core attribute.
	 *
	 * @param sess
	 * @param attribute
	 * @return true if attribute is core attribute
	 */
	boolean isCoreAttribute(PerunSession sess, AttributeDefinition attribute);

	/**
	 * Determine if attribute is defined (def) attribute.
	 *
	 * @param sess
	 * @param attribute
	 * @return true if attribute is defined attribute
	 *         false otherwise
	 */
	boolean isDefAttribute(PerunSession sess, AttributeDefinition attribute);

	/**
	 * Determine if attribute is optional (opt) attribute.
	 *
	 * @param sess
	 * @param attribute
	 * @return true if attribute is optional attribute
	 *         false otherwise
	 */
	boolean isOptAttribute(PerunSession sess, AttributeDefinition attribute);

	/**
	 * Determine if attribute is virtual (virt) attribute.
	 *
	 * @param sess
	 * @param attribute
	 * @return true if attribute is virtual attribute
	 *         false otherwise
	 */
	boolean isVirtAttribute(PerunSession sess, AttributeDefinition attribute);

	/**
	 * Determine if attribute is core-managed attribute.
	 *
	 * @param sess
	 * @param attribute
	 * @return true if attribute is core-managed
	 */
	boolean isCoreManagedAttribute(PerunSession sess, AttributeDefinition attribute);

	/**
	 * Determine if attribute is from specified namespace.
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
	 * @param attributeName
	 * @return the namespace from the attribute name
	 */
	String getNamespaceFromAttributeName(String attributeName);

	/**
	 * Gets the friendly name from the attribute name.
	 *
	 * @param attributeName
	 * @return the friendly name from the attribute name
	 */
	String getFriendlyNameFromAttributeName(String attributeName);

	/**
	 * Get all <b>non-empty</b> attributes with user's logins.
	 *
	 * @param sess
	 * @param user
	 * @return list of attributes with login
	 *
	 * @throws InternalErrorException
	 */
	List<Attribute> getLogins(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Get all values for specified attribute. Attribute can't be core, core-managed or virt.
	 *
	 * @param sess
	 * @param attributeDefinition
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException if attribute is core, core-managed or virt
	 */
	List<Object> getAllValues(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException;


	/**
	 * Check if this the attribute is truly required for the facility right now. Truly means that the nothing (member, resource...) is invalid.
	 *
	 * @param sess
	 * @param facility
	 * @param attributeDefinition
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	boolean isTrulyRequiredAttribute(PerunSession sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Check if this the attribute is truly required for the resource right now. Truly means that the nothing (member, resource...) is invalid.
	 *
	 * @param sess
	 * @param resource
	 * @param attributeDefinition
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	boolean isTrulyRequiredAttribute(PerunSession sess, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 *
	 * Check if this the attribute is truly required for the member right now. Truly means that the nothing (member, resource...) is invalid.
	 *
	 * @param sess
	 * @param member
	 * @param attributeDefinition
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	boolean isTrulyRequiredAttribute(PerunSession sess, Member member, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Check if this the attribute is truly required for the user right now. Truly means that the nothing (member, resource...) is invalid.
	 *
	 * @param sess
	 * @param user
	 * @param attributeDefinition
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	boolean isTrulyRequiredAttribute(PerunSession sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Check if this the attribute is truly required for the user and the facility right now. Truly means that the nothing (member, resource...) is invalid.
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @param attributeDefinition
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	boolean isTrulyRequiredAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Check if this the attribute is truly required for the member and the resource right now. Truly means that the nothing (member, resource...) is invalid.
	 *
	 * @param sess
	 * @param resource
	 * @param member
	 * @param attributeDefinition
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	boolean isTrulyRequiredAttribute(PerunSession sess, Resource resource, Member member, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Check if this the attribute is truly required for the member and the group right now. Truly means that the nothing (member, group...) is invalid.
	 *
	 * @param sess
	 * @param member
	 * @param group
	 * @param attributeDefinition
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	boolean isTrulyRequiredAttribute(PerunSession sess, Member member, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Same as doTheMagic(sess, member, false);
	 */
	void doTheMagic(PerunSession sess, Member member) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * This function takes all member-related attributes (member, user, member-resource, user-facility) and tries to fill them and set them.
	 * If trueMagic is set, this method can remove invalid attribute value (value which didn't pass checkAttributeValue test) and try to fill and set another. In this case, WrongReferenceAttributeValueException, WrongAttributeValueException are thrown if same attribute can't be set corraclty.
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
	void doTheMagic(PerunSession sess, Member member, boolean trueMagic) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * Converts string into the Object defined by type.
	 *
	 * @param value
	 * @param type
	 * @throws InternalErrorException
	 * @return
	 */
	public Object stringToAttributeValue(String value, String type) throws InternalErrorException;

	/**
	 * Merges attribute value if the attribute type is list or map. In other cases it only stores new value.
	 * If the type is list, new values are added to the current stored list.
	 * It the type is map, new values are added and existing are overwritten with new values, but only if there is some change.
	 *
	 * @param sess
	 * @param user
	 * @param attribute
	 * @return attribute with updated value
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	public Attribute mergeAttributeValue(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException,
				 WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * Merges attribute value if the attribute type is list or map. In other cases it only stores new value.
	 * If the type is list, new values are added to the current stored list.
	 * It the type is map, new values are added and existing are overwritten with new values, but only if there is some change.
	 *
	 * This method creates nested transaction to prevent storing value to DB if it throws any exception.
	 *
	 * @param sess
	 * @param user
	 * @param attribute
	 * @return attribute with updated value
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	public Attribute mergeAttributeValueInNestedTransaction(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException,
			WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * Merges attributes values if the attribute type is list or map. In other cases it only stores new value.
	 * If the type is list, new values are added to the current stored list.
	 * It the type is map, new values are added and existing are overwritten with new values, but only if there is some change.
	 *
	 * @param sess
	 * @param user
	 * @param attributes
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	public void mergeAttributesValues(PerunSession sess, User user, List<Attribute> attributes)  throws InternalErrorException, WrongAttributeValueException,
				 WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * This method checkValue on all possible dependent attributes for richAttr.
	 * RichAttribute is needed for useful objects which are in holders.
	 *
	 * @param sess
	 * @param richAttr
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeDependencies(PerunSession sess, RichAttribute richAttr) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * @return get map of all Dependencies
	 */
	Map<AttributeDefinition, Set<AttributeDefinition>> getAllDependencies();

	/**
	 * Method get attribute Definition attrDef and aidingAttr which only holds one or two useful objects in holders.
	 * Thanks useful objects, method find all possibly richAttributes which can be get on specific attrDef with all
	 * existing combinations of needed objects.
	 *
	 * Example: I am looking for Member attrDef and I have primaryHolder: User.
	 * So i will find all members of this user and return all richAttributes of combination attribute + specific member in primaryHolder.
	 *
	 * @param sess
	 * @param attrDef
	 * @param aidingAttr
	 * @return
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws VoNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	List<RichAttribute> getRichAttributesWithHoldersForAttributeDefinition(PerunSession sess, AttributeDefinition attrDef, RichAttribute aidingAttr) throws InternalErrorException, AttributeNotExistsException, VoNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Get All user_facility attributes for any existing user
	 *
	 * @param sess
	 * @param facility
	 * @return list of user facility attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getUserFacilityAttributesForAnyUser(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Updates AttributeDefinition.
	 *
	 * @param perunSession
	 * @param attributeDefinition
	 * @return returns updated attributeDefinition
	 * @throws InternalErrorException
	 */
	AttributeDefinition updateAttributeDefinition(PerunSession perunSession, AttributeDefinition attributeDefinition) throws InternalErrorException;

	/**
	 * Check if actionType exists in underlaying data source.
	 *
	 * @param sess perun session
	 * @param actionType actionType to check
	 * @throws InternalErrorException if unexpected error occure
	 * @throws ActionTypeNotExistsException if attribute doesn't exists
	 */
	void checkActionTypeExists(PerunSession sess, ActionType actionType) throws InternalErrorException, ActionTypeNotExistsException;

	/**
	 * Set all Attributes in list to "writable = true".
	 *
	 * @param sess
	 * @param attributes
	 * @return list of attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> setWritableTrue(PerunSession sess, List<Attribute> attributes) throws InternalErrorException;

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
	 */
	List<AttributeRights> getAttributeRights(PerunSession sess, int attributeId) throws InternalErrorException;

	/**
	 * Sets all attribute rights in the list given as a parameter.
	 * The method sets the rights for attribute and role exactly as it is given in the list of action types. That means it can
	 * remove a right, if the right is missing in the list.
	 * Info: If there is role VoAdmin in the list, use it for setting also VoObserver rights (only for read) automatic
	 *
	 * @param sess perun session
	 * @param rights list of attribute rights
	 * @throws InternalErrorException
	 */
	void setAttributeRights(PerunSession sess, List<AttributeRights> rights) throws InternalErrorException;

	/**
	 * Get user virtual attribute module by the attribute.
	 *
	 * @param sess
	 * @param attribute attribute for which you get the module
	 * @return instance of user attribute module
	 *
	 * @throws InternalErrorException
	 * @throws WrongModuleTypeException
	 * @throws ModuleNotExistsException
	 */
	public UserVirtualAttributesModuleImplApi getUserVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws ModuleNotExistsException, WrongModuleTypeException, InternalErrorException;
	
	/**
	* Method returns attribute with null value if attribute has empty string;
	* 
	* @param attributeToConvert
	* @return Attribute with original value or null value for empty string
	*/
	Attribute convertEmptyStringIntoNullInAttrValue(Attribute attributeToConvert);

	/**
	 * Method returns attribute with null value if attribute value is boolean == false
	 *
	 * @param attributeToConvert
	 * @return Attribute with original value or null value for boolean == false
	 */
	Attribute convertBooleanFalseIntoNullInAttrValue(Attribute attributeToConvert);

}
