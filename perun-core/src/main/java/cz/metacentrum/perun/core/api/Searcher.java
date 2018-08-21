package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;

import java.util.Calendar;
import java.util.List;


import java.util.Map;

/**
 *
 * @author  Michal Stava
 */
public interface Searcher {

	/**
	 * This method get Map of Attributes with searching values and try to find all users, which have specific attributes in format.
	 * Better information about format below. When there are more than 1 attribute in Map, it means all must be true "looking for all of them" (AND)
	 *
	 * @param sess perun session
	 * @param attributesWithSearchingValues map of attributes names
	 *        when attribute is type String, so value is string and we are looking for total match (Partial is not supported now, will be supported later by symbol *)
	 *        when attribute is type Integer, so value is integer in String and we are looking for total match
	 *        when attribute is type List<String>, so value is String and we are looking for at least one total or partial matching element
	 *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total match of both or if is it "key" so we are looking for total match of key
	 *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
	 * @return list of users who have attributes with specific values (behaviour above)
	 *        if no user exist, return empty list of users
	 *
	 * @throws AttributeNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws WrongAttributeAssignmentException
	 */
	List<User> getUsers(PerunSession sess, Map<String, String> attributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, PrivilegeException, WrongAttributeAssignmentException;

	/**
	 * This method get Map of user Attributes with searching values and try to find all members, which have specific attributes in format for specific VO.
	 * Better information about format below. When there are more than 1 attribute in Map, it means all must be true "looking for all of them" (AND)
	 *
	 * if principal has no rights for operation, throw exception
	 * if principal has no rights for some attribute on specific user, do not return this user
	 * if attributesWithSearchingValues is null or empty, return all members from vo if principal has rights for this operation
	 *
	 * @param sess perun session
	 * @param userAttributesWithSearchingValues map of attributes names
	 *        when attribute is type String, so value is string and we are looking for total match (Partial is not supported now, will be supported later by symbol *)
	 *        when attribute is type Integer, so value is integer in String and we are looking for total match
	 *        when attribute is type List<String>, so value is String and we are looking for at least one total or partial matching element
	 *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total match of both or if is it "key" so we are looking for total match of key
	 *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
	 * @return list of users who have attributes with specific values (behaviour above)
	 *        if no user exist, return empty list of users
	 *
	 * @throws AttributeNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws WrongAttributeAssignmentException
	 * @throws VoNotExistsException
	 */
	List<Member> getMembersByUserAttributes(PerunSession sess, Vo vo,  Map<String, String> userAttributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, PrivilegeException, WrongAttributeAssignmentException, VoNotExistsException;

	/**
	 * This method take map of coreAttributes with search values and return all
	 * users who have the specific match for all of these core attributes.
	 *
	 * @param sess
	 * @param coreAttributesWithSearchingValues
	 * @return
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws PrivilegeException
	 */
	List<User> getUsersForCoreAttributes(PerunSession sess, Map<String, String> coreAttributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, PrivilegeException;

	/**
	 * Return members with expiration date set, which will expire on today +/- X days.
	 * You can specify operator for comparison (by default "=") returning exact match.
	 * So you can get all expired members (including today) using "<=" and zero days shift.
	 * or using "<" and +1 day shift.
	 *
	 * Method ignores current member state, just compares expiration date !
	 *
	 * @param sess PerunSession
	 * @param operator One of "=", "<", ">", "<=", ">=". If null, "=" is anticipated.
	 * @param days X days before/after today
	 * @return Members with expiration relative to method params.
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Member> getMembersByExpiration(PerunSession sess, String operator, int days) throws PrivilegeException, InternalErrorException;

	/**
	 * Return members with expiration date set, which will expire on specified date.
	 * You can specify operator for comparison (by default "=") returning exact match.
	 * So you can get all expired members (including today) using "<=" and current date.
	 * or using "<" and tomorrow date.
	 *
	 * Method ignores current member state, just compares expiration date !
	 *
	 * @param sess PerunSession
	 * @param operator One of "=", "<", ">", "<=", ">=". If null, "=" is anticipated.
	 * @param date Date to compare expiration with (if null, current date is used).
	 * @return Members with expiration relative to method params.
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Member> getMembersByExpiration(PerunSession sess, String operator, Calendar date) throws PrivilegeException, InternalErrorException;

	/**
	 * This method get Map of Attributes with searching values and try to find all facilities, which have specific attributes in format.
	 * Better information about format below. When there are more than 1 attribute in Map, it means all must be true "looking for all of them" (AND)
	 *
	 * @param sess perun session
	 * @param attributesWithSearchingValues map of attributes names
	 *        when attribute is type String, so value is string and we are looking for total match (Partial is not supported now, will be supported later by symbol *)
	 *        when attribute is type Integer, so value is integer in String and we are looking for total match
	 *        when attribute is type List<String>, so value is String and we are looking for at least one total or partial matching element
	 *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total match of both or if is it "key" so we are looking for total match of key
	 *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
	 * @return list of facilities that have attributes with specific values (behaviour above)
	 *        if no such facility exists, returns empty list
	 *
	 * @throws PrivilegeException insufficient permission
	 * @throws InternalErrorException internal error
	 * @throws AttributeNotExistsException when specified attribute does not exist
	 * @throws WrongAttributeAssignmentException wrong attribute assignment
	 */
	List<Facility> getFacilities(PerunSession sess, Map<String, String> attributesWithSearchingValues) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

	/**
	 * This method get Map of Attributes with searching values and try to find all resources, which have specific attributes in format.
	 * Better information about format below. When there are more than 1 attribute in Map, it means all must be true "looking for all of them" (AND)
	 *
	 * @param sess perun session
	 * @param attributesWithSearchingValues map of attributes names
	 *        when attribute is type String, so value is string and we are looking for total match (Partial is not supported now, will be supported later by symbol *)
	 *        when attribute is type Integer, so value is integer in String and we are looking for total match
	 *        when attribute is type List<String>, so value is String and we are looking for at least one total or partial matching element
	 *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total match of both or if is it "key" so we are looking for total match of key
	 *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
	 * @return list of resources that have attributes with specific values (behaviour above)
	 *        if no such resource exists, returns empty list
	 *
	 * @throws PrivilegeException insufficient permission
	 * @throws InternalErrorException internal error
	 * @throws AttributeNotExistsException when specified attribute does not exist
	 * @throws WrongAttributeAssignmentException wrong attribute assignment
	 */
	List<Resource> getResources(PerunSession sess, Map<String, String> attributesWithSearchingValues) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;
}
