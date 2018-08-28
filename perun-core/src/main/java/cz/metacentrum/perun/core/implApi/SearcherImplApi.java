package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Searcher Class for searching objects by Map of Attributes
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public interface SearcherImplApi {

	/**
	 * This method get Map of Attributes with searching values and try to find all users, which have specific attributes in format.
	 * Better information about format below. When there are more than 1 attribute in Map, it means all must be true "looking for all of them" (AND)
	 *
	 * IMPORTANT: can't get CORE ATTRIBUTES
	 *
	 * @param sess perun session
	 * @param attributesWithSearchingValues map of attributes
	 *        when attribute is type String, so value is string and we are looking for total match (Partial is not supported now, will be supported later by symbol *)
	 *        when attribute is type Integer, so value is integer in String and we are looking for total match
	 *        when attribute is type List<String>, so value is String and we are looking for at least one total or partial matching element
	 *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total match of both or if is it "key" so we are looking for total match of key
	 *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
	 * @return list of users who have attributes with specific values (behavior above)
	 *        if no user exist, return empty list of users
	 *        if attributeWithSearchingValues is empty, return allUsers
	 *
	 * @throws InternalErrorException
	 */
	List<User> getUsers(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues) throws InternalErrorException;

	/**
	 * Return members with expiration date set, which will expire on date +/- X days.
	 * You can specify operator for comparison (by default "=") returning exact match.
	 * So you can get all expired members (including today) using "<=" and zero days shift.
	 * or using "<" and +1 day shift.
	 *
	 * Method ignores current member state, just compares expiration date !
	 *
	 * @param sess PerunSession
	 * @param operator One of "=", "<", ">", "<=", ">=". If null, "=" is anticipated.
	 * @param date Date to compare expiration with (if null, current date is used).
	 * @param days X days before/after today.
	 * @return Members with expiration relative to method params.
	 * @throws InternalErrorException
	 */
	List<Member> getMembersByExpiration(PerunSession sess, String operator, Calendar date, int days) throws InternalErrorException;

	/**
	 * Return all groups assigned to any resource with following conditions:
	 * 1] resource has set "resourceAttribute" attribute with same value
	 * 2] group and resource has set "groupResourceAttribute" attribute with same value
	 * Attribute values can't be empty.
	 * If there is no such group, return empty array.
	 *
	 * @param sess
	 * @param groupResourceAttribute expected attribute set between a group and a resource (group need to be assigned to the resource)
	 * @param resourceAttribute expected attribute set for assigned resource
	 * @return list of groups with following conditions
	 * @throws InternalErrorException if any of attributes is null or has empty value, if any of attributes is not in expected namespace
	 */
	List<Group> getGroupsByGroupResourceSetting(PerunSession sess, Attribute groupResourceAttribute, Attribute resourceAttribute) throws InternalErrorException;

	/**
	 * This method get Map of Attributes with searching values and try to find all facilities, which have specific attributes in format.
	 * Better information about format below. When there are more than 1 attribute in Map, it means all must be true "looking for all of them" (AND)
	 *
	 * IMPORTANT: can't get CORE ATTRIBUTES
	 *
	 * @param sess perun session
	 * @param attributesWithSearchingValues map of attributes
	 *        when attribute is type String, so value is string and we are looking for total match (Partial is not supported now, will be supported later by symbol *)
	 *        when attribute is type Integer, so value is integer in String and we are looking for total match
	 *        when attribute is type List<String>, so value is String and we are looking for at least one total or partial matching element
	 *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total match of both or if is it "key" so we are looking for total match of key
	 *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
	 * @return list of facilities that have attributes with specific values (behavior above)
	 *        if no such facility exists, return empty list
	 *        if attributeWithSearchingValues is empty, return all facilities
	 *
	 * @throws InternalErrorException internal error
	 */
	List<Facility> getFacilities(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues) throws InternalErrorException;

	/**
	 * This method get Map of Attributes with searching values and try to find all resources, which have specific attributes in format.
	 * Better information about format below. When there are more than 1 attribute in Map, it means all must be true "looking for all of them" (AND)
	 *
	 * IMPORTANT: can't get CORE ATTRIBUTES
	 *
	 * @param sess perun session
	 * @param attributesWithSearchingValues map of attributes
	 *        when attribute is type String, so value is string and we are looking for total match (Partial is not supported now, will be supported later by symbol *)
	 *        when attribute is type Integer, so value is integer in String and we are looking for total match
	 *        when attribute is type List<String>, so value is String and we are looking for at least one total or partial matching element
	 *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total match of both or if is it "key" so we are looking for total match of key
	 *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
	 * @return list of resources that have attributes with specific values (behavior above)
	 *        if no such resource exists, return empty list
	 *        if attributeWithSearchingValues is empty, return all resources
	 *
	 * @throws InternalErrorException internal error
	 */
	List<Resource> getResources(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues) throws InternalErrorException;
}
