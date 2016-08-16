/**
 *
 */
package cz.metacentrum.perun.core.implApi;

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
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongModuleTypeException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public interface AttributesManagerImplApi {

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
	 * Get all virtual attributes associated with the facility.
	 *
	 * @param sess perun session
	 * @param facility to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Get all virtual attributes associated with the member.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Get all virtual attributes associated with the vo.
	 *
	 * @param sess perun session
	 * @param vo to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Get all virtual attributes associated with the group.
	 *
	 * @param sess perun session
	 * @param group to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Get all virtual attributes associated with the host.
	 *
	 * @param sess perun session
	 * @param host to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(PerunSession sess, Host host) throws InternalErrorException;

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
	 * Get all virtual attributes associated with the resource.
	 *
	 * @param sess perun session
	 * @param resource to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the member on the resource.
	 *
	 * @param sess perun session
	 * @param resource to get the attributes from
	 * @param member to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException;

	/**
	 * Get all virtual attributes associated with the member on the resource.
	 *
	 * @param sess perun session
	 * @param resource to get the attributes from
	 * @param member to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException;

	/**
	 * Get all <b>non-empty, non-virtual</b> attributes associated with the member in the group.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param group group to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException;

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
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the member.
	 *
	 * @param sess perun session
	 * @param member member to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the group starts with name startPartOfName.
	 * Get only nonvirtual attributes with NotNull value.
	 *
	 * PRIVILEGE: Get only those attributes the principal has access to.
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
	 * PRIVILEGE: Get only those attributes the principal has access to.
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
	 * Get all attributes associated with the vo which have name in list attrNames (empty and virtual too).
	 *
	 * @param sess perun session
	 * @param vo to get the attributes from
	 * @param attrNames list of attributes' names
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Vo vo, List<String> attrNames) throws InternalErrorException;

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
	 *
	 * @param sess perun session
	 * @param group to get the attributes from
	 * @param attrNames list of attributes' names
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raises in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Group group, List<String> attrNames) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the user on the facility.
	 *
	 * @param sess perun session
	 * @param facility
	 * @param user
	 * @return list of attributes
	 *
	 * @throws InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with any user on the facility.
	 *
	 * @param sess perun session
	 * @param facility
	 * @return list of attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getUserFacilityAttributesForAnyUser(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Get all entiteless attributes with subject equaled String key
	 *
	 * @param sess
	 * @param key
	 * @return
	 * @throws InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, String key) throws InternalErrorException;

	/**
	 * Get all entityless attributes with attributeName
	 * @param sess perun session
	 * @param attrName
	 * @return attribute
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getEntitylessAttributes(PerunSession sess, String attrName) throws  InternalErrorException;

	/**
	 * Return value of entityless attribute by attr_id and key (subject).
	 * Value is in the format from DB.
	 * IMPORTANT: return only values in String (special format for Map or List)!
	 * 
	 * If value is null, return null.
	 * If attribute with subject=key not exists, create new one with null value and return null.
	 * 
	 * @param sess
	 * @param attrId
	 * @param key
	 * @return attr_value in string
	 * 
	 * @throws InternalErrorException if runtime error exception has been thrown
	 * @throws AttributeNotExistsException throw exception if attribute with value not exists in DB
	 */
	String getEntitylessAttrValueForUpdate(PerunSession sess, int attrId, String key) throws InternalErrorException, AttributeNotExistsException;
	
	/**
	 * Returns list of Keys which fits the attributeDefinition.
	 *
	 * @param sess
	 * @param attributeDefinition
	 * @return
	 * @throws InternalErrorException
	 */
	List<String> getEntitylessKeys(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException;

	/**
	 * Returns all attributes with not-null value which fits the attributeDefinition. Can't process core or virtual attributes.
	 *
	 * @param sess
	 * @param attributeDefinition
	 * @return list of attributes
	 * @throws InternalErrorException
	 */
	List<Attribute> getAttributesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException;

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
	 * Get all virtual attributes associated with the member in the group.
	 *
	 * @param sess perun session
	 * @param member to get the attributes from
	 * @param group to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the user.
	 *
	 * @param sess perun session
	 * @param user
	 * @return list of attributes
	 *
	 * @throws InternalErrorException
	 */
	List<Attribute> getAttributes(PerunSession sess, User user) throws InternalErrorException;

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
	 * Get all virtual attributes associated with the user.
	 *
	 * @param sess perun session
	 * @param user to get the attributes from
	 * @return list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getVirtualAttributes(PerunSession sess, User user) throws InternalErrorException;

	List<Attribute> getAttributes(PerunSession sess, Host host) throws InternalErrorException;

	List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group) throws InternalErrorException;

	/**
	 * Get all <b>non-empty</b> attributes associated with the user on the all facilities.
	 *
	 * @param sess perun session
	 * @param user
	 * @return list of attributes
	 *
	 * @throws InternalErrorException
	 */
	List<RichAttribute<User, Facility>> getAllUserFacilityRichAttributes(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Get particular attribute for the facility.
	 *
	 * @param attributeName attribute name defined in the particular manager
	 * @param facility to get attribute from
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttribute(PerunSession sess, Facility facility, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the vo.
	 *
	 * @param attributeName attribute name defined in the particular manager
	 * @param vo to get attribute from
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttribute(PerunSession sess, Vo vo, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the group.
	 *
	 * @param attributeName attribute name defined in the particular manager
	 * @param group to get attribute from
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttribute(PerunSession sess, Group group, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the resource.
	 *
	 * @param attributeName attribute name defined in the particular manager
	 * @param resource to get attribute from
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttribute(PerunSession sess, Resource resource, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the member on this resource.
	 *
	 * @param attributeName attribute name defined in the particular manager
	 * @param resource to get attribute from
	 * @param member to get attribute from
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttribute(PerunSession sess, Resource resource, Member member, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the member in this group.
	 *
	 * @param sess perun session
	 * @param member to get attribute from
	 * @param group to get attribute from
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttribute(PerunSession sess, Member member, Group group, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the member.
	 *
	 * @param attributeName attribute name defined in the particular manager
	 * @param member to get attribute from
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttribute(PerunSession sess, Member member, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the user on this facility.
	 *
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttribute(PerunSession sess, Facility facility, User user, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the user.
	 *
	 * @param sess
	 * @param user
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttribute(PerunSession sess, User user, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	Attribute getAttribute(PerunSession sess, Host host, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	Attribute getAttribute(PerunSession sess, Resource resource, Group group, String attributeName) throws InternalErrorException, AttributeNotExistsException;


	/**
	 * Get particular entityless attribute
	 * @param sess perun session
	 * @param key key to get attribute for
	 * @param attributeName
	 * @return attribute
	 * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException  if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttribute(PerunSession sess, String key, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get attributes definition (attribute without defined value).
	 *
	 * @param attributeName attribute name defined in the particular manager
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	AttributeDefinition getAttributeDefinition(PerunSession sess, String attributeName) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get attributes definition (attribute without defined value).
	 *
	 * @return List of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<AttributeDefinition> getAttributesDefinition(PerunSession sess) throws InternalErrorException;

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
	 * Get attibute definition (attribute without defined value).
	 *
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	AttributeDefinition getAttributeDefinitionById(PerunSession sess, int id) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the facility.
	 *
	 * @param id attribute id
	 * @param facility to get attribute from
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttributeById(PerunSession sess, Facility facility, int id) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the vo.
	 *
	 * @param id attribute id
	 * @param vo to get attribute from
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttributeById(PerunSession sess, Vo vo, int id) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the resource.
	 *
	 * @param id attribute id
	 * @param resource to get attribute from
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttributeById(PerunSession sess, Resource resource, int id) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the member on this resource.
	 *
	 * @param id attribute id
	 * @param resource to get attribute from
	 * @param member to get attribute from
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttributeById(PerunSession sess, Resource resource, Member member, int id) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the member in this group.
	 *
	 * @param sess perun session
	 * @param member to get attribute from
	 * @param group to get attribute from
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlying data source
	 */
	Attribute getAttributeById(PerunSession sess, Member member, Group group, int id) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the member.
	 *
	 * @param sess
	 * @param member
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttributeById(PerunSession sess, Member member, int id) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the user on this facility.
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttributeById(PerunSession sess, Facility facility, User user, int id) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get particular attribute for the user.
	 *
	 * @param sess
	 * @param user
	 * @param id attribute id
	 * @return attribute
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws AttributeNotExistsException if the attribute doesn't exists in the underlaying data source
	 */
	Attribute getAttributeById(PerunSession sess, User user, int id) throws InternalErrorException, AttributeNotExistsException;

	Attribute getAttributeById(PerunSession sess, Host host, int id) throws InternalErrorException, AttributeNotExistsException;

	Attribute getAttributeById(PerunSession sess, Resource resource, Group group, int id) throws InternalErrorException, AttributeNotExistsException;

	Attribute getAttributeById(PerunSession sess, Group group, int id) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Store the particular attribute associated with the given perun bean. If an attribute is core attribute then the attribute isn't stored (It's skkiped whithout any notification).
	 *
	 * @param sess perun session
	 * @param object object of setting the attribute, must be one of perunBean or string
	 * @param attribute attribute to set
	 * @return true if new value differs from old value (i.e. values changed)
	 *         false otherwise (value do not change)
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException if the namespace of the attribute does not match the perunBean
	 */
	boolean setAttribute(PerunSession sess, Object object, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Store the particular attribute associated with the bean1 and bean2. If an attribute is core attribute then the attribute isn't stored (It's skkiped whithout any notification).
	 *
	 * @param sess perun session
	 * @param bean1 first perun bean
	 * @param bean2 second perun bean
	 * @param attribute attribute to set
	 *
	 * @return true if new value differs from old value (i.e. values changed)
	 *         false otherwise (value do not change)
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean setAttribute(PerunSession sess, PerunBean bean1, PerunBean bean2, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Insert attribute value in DB.
	 * 
	 * @param sess perun session
	 * @param valueColName column, where the data will be stored, usually one of value or attr_value or attr_value_text
	 * @param attribute that will be stored in the DB
	 * @param tableName in the database in which the attribute will be inserted 
	 * @param columnNames of the database table in which the attribute will be written
	 * @param columnValues of the objects, for which the attribute will be written, corresponding to the columnNames
	 * @return true if new value differs from old value (i.e. values changed)
	 *         false otherwise (value do not change)
	 * @throws InternalErrorException 
	 */
	public boolean insertAttribute(PerunSession sess, String valueColName, Attribute attribute, String tableName, List<String> columnNames, List<Object> columnValues) throws InternalErrorException;
	
	/**
	 * Update attribute value in DB.
	 * 
	 * @param sess perun session
	 * @param valueColName column, where the data will be stored, usually one of value or attr_value or attr_value_text
	 * @param attribute that will be stored in the DB
	 * @param tableName in the database for updating 
	 * @param columnNames of the database table in which the attribute will be written
	 * @param columnValues of the objects, for which the attribute will be written, corresponding to the columnNames
	 * @return true if new value differs from old value (i.e. values changed)
	 *         false otherwise (value do not change)
	 * @throws InternalErrorException 
	 */
	public boolean updateAttribute(PerunSession sess, String valueColName, Attribute attribute, String tableName, List<String> columnNames, List<Object> columnValues) throws InternalErrorException;
	
	/**
	 * Store the particular virtual attribute associated with the facility.
	 *
	 * @param sess perun session
	 * @param facility
	 * @param attribute attribute to set
	 * @return true if attribute was really changed
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ModuleNotExistsException
	 * @throws WrongModuleTypeException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setVirtualAttribute(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular virtual attribute associated with the resource.
	 *
	 * @param sess perun session
	 * @param resource
	 * @param attribute attribute to set
	 * @return true if attribute was really changed
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ModuleNotExistsException
	 * @throws WrongModuleTypeException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setVirtualAttribute(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular virtual attribute associated with the facility and user combination.
	 *
	 * @param sess perun session
	 * @param facility
	 * @param user
	 * @param attribute attribute to set
	 * @return true if attribute was really changed
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ModuleNotExistsException
	 * @throws WrongModuleTypeException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setVirtualAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular virtual attribute associated with the resource and group combination.
	 *
	 * @param sess perun session
	 * @param resource
	 * @param group
	 * @param attribute attribute to set
	 * @return true if attribute was really changed
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ModuleNotExistsException
	 * @throws WrongModuleTypeException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setVirtualAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular virtual attribute associated with the member and group combination.
	 *
	 * @param sess perun session
	 * @param member member to set on
	 * @param group group to set on
	 * @param attribute attribute to set
	 * @return true if attribute was really changed
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ModuleNotExistsException
	 * @throws WrongModuleTypeException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setVirtualAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular virtual attribute associated with the member.
	 *
	 * @param sess perun session
	 * @param member
	 * @param attribute attribute to set
	 * @return true if attribute was really changed
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ModuleNotExistsException
	 * @throws WrongModuleTypeException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setVirtualAttribute(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException;

	/**
	 * Store the particular virtual attribute associated with the user.
	 *
	 * @param sess perun session
	 * @param user
	 * @param attribute attribute to set
	 * @return true if attribute was really changed
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws ModuleNotExistsException
	 * @throws WrongModuleTypeException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean setVirtualAttribute(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongModuleTypeException, ModuleNotExistsException, WrongReferenceAttributeValueException;

	/**
	 * Creates an attribute, the attribute is stored into the appropriate DB table according to the namespace.
	 *
	 * @param sess
	 * @param attribute attribute to create
	 *
	 * @return attribute with set id
	 *
	 * @throws AttributeExistsException if attribute already exists
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	AttributeDefinition createAttribute(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException, AttributeExistsException;

	/**
	 * Deletes the attribute. Definition and all values.
	 *
	 * @param sess
	 * @param attribute attribute to delete
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void deleteAttribute(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Delete all authz for the attribute.
	 *
	 * @param sess
	 * @param attribute the attribute
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorExceptions
	 */
	void deleteAllAttributeAuthz(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException;

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
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility) throws InternalErrorException;

	/**
	 * Get resource attributes which are required by services.
	 *
	 * @param sess perun session
	 * @param resource
	 * @param serviceIds
	 * @return list of resource attributes which are required by services which are selceted
	 * 
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, List<Integer> serviceIds) throws InternalErrorException;

	/**
	 * Get resource attributes which are required by services. Services are known from the resourceToGetServicesFrom.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param resource resource for which you want to get the attributes
	 * @return list of resource attributes which are required by services which are assigned to resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource) throws InternalErrorException;

	/**
	 * Get member attributes which are required by services. Services are known from the resourceToGetServicesFrom.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param member you get attributes for this member
	 * @return list of member attributes which are required by services which are assigned to resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member) throws InternalErrorException;

	/**
	 * Get member-resource attributes which are required by services. Services are known from the resourceToGetServicesFrom.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param resource you get attributes for this resource and the member
	 * @param member you get attributes for this member and the resource
	 * @return list of member-resource attributes which are required by services which are assigned to another resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Member member) throws InternalErrorException;

	/**
	 * Get user-facility attributes which are required by services. Services are known from the resource.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param facility facility from which the services are taken
	 * @param user you get attributes for this user
	 * @return list of member-resource attributes which are required by services which are assigned to another resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility, User user) throws InternalErrorException;

	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group) throws InternalErrorException;

	/**
	 * Get user attributes which are required by services. Services are known from the resource.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom
	 * @param user you get attributes for this user
	 * @return list of user attributes which are required by services which are assigned to resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, User user) throws InternalErrorException;

	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Host host) throws InternalErrorException;

	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Group group) throws InternalErrorException;
	/**
	 * Get all attributes which are required by service.
	 * Required attribues are requisite for Service to run.
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
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Member member) throws InternalErrorException;

	/**
	 * Get member-resource attributes which are required by service for each member in list of members.
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param resource you get attributes for this resource and the members
	 * @param members you get attributes for this list of members and the resource
	 * @return map of member and his list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource, List<Member> members) throws InternalErrorException;

	/**
	 * Get member attributes which are required by service for each member in list of members.
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param resource resource only to get allowed members
	 * @param members you get attributes for this list of members
	 * @return map of member and his list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Resource resource, Service service, List<Member> members) throws InternalErrorException;

	/**
	 * Get user-facility attributes which are required by the service for each user in list of users.
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param facility you get attributes for this facility and user
	 * @param users you get attributes for this user and facility
	 * @return map of userID and his list of attributes
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility, List<User> users) throws InternalErrorException;

	/**
	 * Get user attributes which are required by the service for each user in list of users.
	 *
	 * @param sess perun session
	 * @param service attribute required by this service
	 * @param users you get attributes for this user and facility
	 * @return map of userID and his list of attributes
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
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group) throws InternalErrorException;

	/**
	 * Get member-group attributes which are required by services. Services are known from the resourceToGetServicesFrom.
	 *
	 * @param sess perun session
	 * @param resourceToGetServicesFrom resource from which the services are taken
	 * @param group you get attributes for this group and the member
	 * @param member you get attributes for this member and the group
	 * @return list of member-group attributes which are required by services which are assigned to another resource.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group) throws InternalErrorException;

	/**
	 * Get member attributes which are required by the service.
	 *
	 * @param sess perun session
	 * @param service attribute required by this service you'll get
	 * @param member
	 * @return list of attributes which are required by the service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member) throws InternalErrorException;

	/**
	 * Get user-facility attributes which are required by the service.
	 *
	 * @param sess perun session
	 * @param service attribute required by this service you'll get
	 * @param facility
	 * @param user
	 * @return list of attributes which are required by the service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility, User user) throws InternalErrorException;

	/**
	 * Get user attributes which are required by the service.
	 *
	 * @param sess perun session
	 * @param service attribute required by this service you'll get
	 * @param user
	 * @return list of attributes which are required by the service.
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, User user) throws InternalErrorException;

	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group) throws InternalErrorException;

	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Host host) throws InternalErrorException;

	List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Group group) throws InternalErrorException;
	/**
	 * This method try to fill a value of the resource attribute. Value may be copied from some facility attribute.
	 *
	 * @param sess perun session
	 * @param resource resource, attribute of which you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be owerwriten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	Attribute fillAttribute(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException;

	/**
	 * This method try to fill value of the member-resource attribute. This value is automatically generated, but not all atrributes can be filled this way.
	 *
	 * @param sess perun session
	 * @param resource  attribute of this resource (and member) and you want to fill
	 * @param member attribute of this member (and resource) and you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be owerwriten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	Attribute fillAttribute(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException;

	/**
	 * This method tries to fill value of the member-group attribute. This value is automatically generated, but not all attributes can be filled this way.
	 *
	 * @param sess perun session
	 * @param member attribute of this member (and group) you want to fill
	 * @param group attribute of this group you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	Attribute fillAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException;

	/**
	 * This method try to fill value of the user-facility attribute. This value is automatically generated, but not all atrributes can be filled this way.
	 *
	 * @param sess perun session
	 * @param facility  attribute of this facility (and user) and you want to fill
	 * @param user attribute of this user (and facility) and you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be owerwriten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	Attribute fillAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException;

	/**
	 * This method try to fill value of the user attribute. This value is automatically generated, but not all atrributes can be filled this way.
	 *
	 * @param sess perun session
	 * @param user attribute of this user (and facility) and you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be owerwriten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	Attribute fillAttribute(PerunSession sess, User user, Attribute attribute) throws InternalErrorException;

	/**
	 * This method try to fill value of the member attribute. This value is automatically generated, but not all atrributes can be filled this way.
	 *
	 * @param sess perun session
	 * @param member attribute of this member (and facility) and you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be owerwriten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	Attribute fillAttribute(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException;

	Attribute fillAttribute(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException;

	Attribute fillAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException;

	Attribute fillAttribute(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param sess perun session
	 * @param facility
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void changedAttributeHook(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param sess perun session
	 * @param key
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void changedAttributeHook(PerunSession sess, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param sess
	 * @param vo
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void changedAttributeHook(PerunSession sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param sess
	 * @param host
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void changedAttributeHook(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param sess
	 * @param group
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void changedAttributeHook(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param sess
	 * @param user
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void changedAttributeHook(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param sess
	 * @param member
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void changedAttributeHook(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param sess
	 * @param resource
	 * @param group
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void changedAttributeHook(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param sess
	 * @param resource
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void changedAttributeHook(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param sess
	 * @param resource
	 * @param member
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void changedAttributeHook(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param sess
	 * @param member
	 * @param group
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void changedAttributeHook(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @param attribute
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void changedAttributeHook(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this facility attribute is valid.
	 *
	 * @param sess perun session
	 * @param facility facility for which you want to check validity of attribute
	 * @param attribute attribute to check
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this vo attribute is valid.
	 *
	 * @param sess perun session
	 * @param vo vo for which you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this group attribute is valid.
	 *
	 * @param sess perun session
	 * @param group group for which you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this resource attribute is valid.
	 *
	 * @param sess perun session
	 * @param resource resource for which you want to check validity of attribute
	 * @param attribute attribute to check
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

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
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

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
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

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
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException;

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
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException;

	/**
	 * Check if value of this member attribute is valid.
	 *
	 *
	 * @param sess perun session
	 * @param member member for which you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException;

	void checkAttributeValue(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException;

	void checkAttributeValue(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Check if value of this entityless attribute is valid.
	 *
	 *
	 * @param sess perun session
	 * @param key key for which you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException
	 */
	void checkAttributeValue(PerunSession sess, String key, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException;

	/**
	 * Unset particular attribute for the facility.
	 *
	 * @param sess perun session
	 * @param facility remove attribute from this facility
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttribute(PerunSession sess, Facility facility, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Unset particular entityless attribute with subject equals key.
	 *
	 * @param sess perun session
	 * @param key subject of entityless attribute
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttribute(PerunSession sess, String key, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Unset all attributes for the facility.
	 *
	 * @param sess perun session
	 * @param facility remove attributes from this facility
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Remove all non-virtual group-resource attribute on selected resource
	 * 
	 * @param sess
	 * @param resource
	 * @throws InternalErrorException 
	 */
	void removeAllGroupResourceAttributes(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Remove all non-virtual member-resource attributes assigned to resource
	 *
	 * @param sess
	 * @param resource
	 * @throws InternalErrorException
	 */
	void removeAllMemberResourceAttributes(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Unset particular attribute for the vo.
	 *
	 * @param sess perun session
	 * @param vo remove attribute from this vo
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttribute(PerunSession sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Unset all attributes for the vo.
	 *
	 * @param sess perun session
	 * @param vo remove attributes from this vo
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Unset particular attribute for the group.
	 *
	 * @param sess perun session
	 * @param group remove attribute from this group
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttribute(PerunSession sess, Group group, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Unset all attributes for the group.
	 *
	 * @param sess perun session
	 * @param group remove attributes from this group
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Group group) throws InternalErrorException;


	/**
	 * Unset particular attribute for the resource.
	 *
	 * @param sess perun session
	 * @param resource remove attribute from this resource
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Unset all attributes for the resource.
	 *
	 * @param sess perun session
	 * @param resource remove attributes from this resource
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Unset particular member-resorce attribute for the member on the resource.
	 *
	 * @param sess perun session
	 * @param resource remove attributes for this resource
	 * @param member remove attribute from this member
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttribute(PerunSession sess, Resource resource, Member member, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Unset all (member-resource) attributes for the member on the resource.
	 *
	 * @param sess perun session
	 * @param member remove attributes from this member
	 *
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException;

	/**
	 * Unset particular attribute for the member in the group. Core attributes can't be removed this way.
	 *
	 * @param sess perun session
	 * @param group remove attributes for this group
	 * @param member remove attribute from this member
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttribute(PerunSession sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Unset all attributes for the member in the group.
	 *
	 * @param sess perun session
	 * @param group remove attributes for this group
	 * @param member remove attributes from this member
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException;

	/**
	 * Unset particular member attribute
	 *
	 * @param sess perun session
	 * @param member
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttribute(PerunSession sess, Member member, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Unset all member attributes for the member.
	 *
	 * @param sess perun session
	 * @param member
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Unset particular user-facility attribute
	 *
	 * @param sess perun session
	 * @param facility
	 * @param user
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Unset all (user-facility) <b>non-virtual</b> attributes for the user on the facility.
	 *
	 * @param sess perun session
	 * @param facility
	 * @param user
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException;

	/**
	 * Unset all (user-facility) <b>non-virtual</b> attributes for any user on the facility.
	 *
	 * @param sess perun session
	 * @param facility
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllUserFacilityAttributesForAnyUser(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Unset all (user-facility) <b>non-virtual</b> attributes for the user and <b>all facilities</b>
	 *
	 * @param sess perun session
	 * @param user
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllUserFacilityAttributes(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Unset particular user-facility virtual attribute value.
	 *
	 * @param sess perun session
	 * @param facility
	 * @param user
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeVirtualAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Unset particular resource virtual attribute value.
	 *
	 * @param sess
	 * @param resource
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean removeVirtualAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Unset particular group-resource virtual attribute value.
	 *
	 * @param sess
	 * @param resource
	 * @param group
	 * @param attribute
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	boolean removeVirtualAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException;

	/**
	 * Unset particular user attribute
	 *
	 * @param sess perun session
	 * @param user
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttribute(PerunSession sess, User user, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Unset all user attributes for the user.
	 *
	 * @param sess perun session
	 * @param user
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Unset particular host attribute
	 *
	 * @param sess perun session
	 * @param host
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttribute(PerunSession sess, Host host, AttributeDefinition attribute) throws InternalErrorException;
	/**
	 * Unset all user attributes for the host.
	 *
	 * @param sess perun session
	 * @param host
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Host host) throws InternalErrorException;

	/**
	 * Unset particular group_resource attribute
	 *
	 * @param sess perun session
	 * @param resource resource
	 * @param group group
	 * @param attribute attribute to remove
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	boolean removeAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Unset all group_resource attributes
	 *
	 * @param sess perun session
	 * @param resource Resource
	 * @param group Group
	 * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAllAttributes(PerunSession sess, Resource resource, Group group) throws InternalErrorException;
	/**
	 * Check if attribute exists in underlaying data source.
	 *
	 * @param sess perun session
	 * @param attribute attribute to check
	 * @return true if attribute exists in underlaying data source, false othewise
	 *
	 * @throws InternalErrorException if unexpected error occured
	 */
	boolean attributeExists(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Check if attribute exists in underlaying data source.
	 *
	 * @param sess perun session
	 * @param attribute attribute to check
	 * @throws InternalErrorException if unexpected error occured
	 * @throws AttributeNotExistsException if attribute doesn';t exists
	 */
	void checkAttributeExists(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Check if actionType exists in underlaying data source.
	 *
	 * @param sess perun session
	 * @param actionType actionType to check
	 * @throws InternalErrorException if unexpected error occured
	 * @throws ActionTypeNotExistsException if attriobute doesn't exists
	 */
	void checkActionTypeExists(PerunSession sess, ActionType actionType) throws InternalErrorException, ActionTypeNotExistsException;

	/*
	 * @see cz.metacentrum.perun.core.implApi.AttributesManagerImplApi#checkAttributeExists(PerunSession, AttributeDefinition)
	 * @param expectedNamespace expected namespace
	 * @throws WrongAttributeAssignmentException if attribute's namespace is to equal to expected namespace
	 void checkAttributeExists(PerunSession sess, AttributeDefinition attribute, String expectedNamespace) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;
	 */

	/**
	 * Batch version of checkAttributeExists
	 */
	void checkAttributesExists(PerunSession sess, List<? extends AttributeDefinition> attributes) throws InternalErrorException, AttributeNotExistsException;

	/*
	 * @see cz.metacentrum.perun.core.implApi.AttributesManagerImplApi#checkAttributesExists(PerunSession, List)
	 * @param expectedNamespace expected namespace
	 * @throws WrongAttributeAssignmentException if any attribute's namespace is to equal to expected namespace
	 void checkAttributesExists(PerunSession sess, List<? extends AttributeDefinition> attributes, String expectedNamespace) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;
	 */

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
	 * Determine if attribute is core-managed attribute.
	 *
	 * @param sess
	 * @param attribute
	 * @return true if attribute is core-managed
	 */
	boolean isCoreManagedAttribute(PerunSession sess, AttributeDefinition attribute);

	/**
	 * Determine if attribute is virtual attribute.
	 *
	 * @param sess
	 * @param attribute
	 * @return true if attribute is virtual
	 */
	boolean isVirtAttribute(PerunSession sess, AttributeDefinition attribute);

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
	 * Determine if attribute is large (can contain value over 4kb).
	 *
	 * @param sess
	 * @param attribute
	 * @return true if the attribute is large
	 */
	boolean isLargeAttribute(PerunSession sess, AttributeDefinition attribute);

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
	 * Get all values for specified resource attribute. Atibute can't be core or virt.
	 *
	 * @param sess
	 * @param attributeDefinition attribute definition, namespace resource
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Object> getAllResourceValues(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException;

	/**
	 * Get all values for specified group-resource attribute. Atibute can't be core or virt.
	 *
	 * @param sess
	 * @param attributeDefinition attribute definition, namespace group-resource
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Object> getAllGroupResourceValues(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException;

	/**
	 * Get all values for specified group attribute. Atibute can't be core or virt.
	 *
	 * @param sess
	 * @param attributeDefinition attribute definition, namespace group
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Object> getAllGroupValues(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException;

	/**
	 * Check if this attribute is currently required on this facility. Attribute can be from any namespace.
	 *
	 * @param sess
	 * @param facility
	 * @param attributeDefinition
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	boolean isAttributeRequiredByFacility(PerunSession sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException;

        /**
	 * Check if this attribute is currently required on this vo. Attribute can be from any namespace.
	 *
	 * @param sess
	 * @param vo
	 * @param attributeDefinition
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	boolean isAttributeRequiredByVo(PerunSession sess, Vo vo, AttributeDefinition attributeDefinition) throws InternalErrorException;

	/**
	 * Check if this attribute is currently required on this group. Attribute can be from any namespace.
	 *
	 * @param sess
	 * @param group
	 * @param attributeDefinition
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	boolean isAttributeRequiredByGroup(PerunSession sess, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException;

	/**
	 * Check if this attribute is currently required on this resource. Attribute can be from any namespace.
	 *
	 * @param sess
	 * @param resource
	 * @param attributeDefinition
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	boolean isAttributeRequiredByResource(PerunSession sess, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException;

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
	 * Get the attributeModule for the attribute
	 *
	 * @param attribute get the attribute module for this attribute
	 * @see cz.metacentrum.perun.core.impl.AttributesManagerImpl#getAttributesModule(PerunSession,String)
	 */
	Object getAttributesModule(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException;

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
	 * Sets attribute right given as a parameter.
	 * The method sets the rights for attribute and role exactly as it is given in the list of action types. That means it can
	 * remove a right, if the right is missing in the list.
	 * Info: If there is role VoAdmin in the list, use it for setting also VoObserver rights (only for read) automatic
	 *
	 * @param sess perun session
	 * @param right attribute right
	 * @throws InternalErrorException
	 */
	void setAttributeRight(PerunSession sess, AttributeRights right) throws InternalErrorException;

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
}
