package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Searcher Class for searching objects by Map of Attributes
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public interface SearcherBl {
  /**
   * This method get Map of Attributes with searching values and try to find all facilities, which have specific
   * attributes in format. Better information about format below. When there are more than 1 attribute in Map, it means
   * all must be true "looking for all of them" (AND)
   *
   * @param sess                          perun session
   * @param attributesWithSearchingValues map of attributes names when attribute is type String, so value is string and
   *                                      we are looking for total match (Partial is not supported now, will be
   *                                      supported later by symbol *) when attribute is type Integer, so value is
   *                                      integer in String and we are looking for total match when attribute is type
   *                                      List<String>, so value is String and we are looking for at least one total or
   *                                      partial matching element when attribute is type Map<String> so value is String
   *                                      in format "key=value" and we are looking total match of both or if is it "key"
   *                                      so we are looking for total match of key IMPORTANT: In map there is not
   *                                      allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
   * @return list of facilities that have attributes with specific values (behaviour above) if no such facility exists,
   * returns empty list
   * @throws InternalErrorException            internal error
   * @throws AttributeNotExistsException       when specified attribute does not exist
   * @throws WrongAttributeAssignmentException wrong attribute assignment
   * @throws WrongAttributeValueException      wrong attribute value
   */
  List<Facility> getFacilities(PerunSession sess, Map<String, String> attributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * Filter output from getGroups by vo_id.
   *
   * @see #getGroups(PerunSession, Map)
   */
  List<Group> getGroups(PerunSession sess, Vo vo, Map<String, String> attributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeValueException;

  /**
   * This method get Map of Attributes with searching values and try to find all groups, which have specific attributes
   * in format. Better information about format below. When there are more than 1 attribute in Map, it means all must be
   * true "looking for all of them" (AND)
   * <p>
   * IMPORTANT: can't get CORE ATTRIBUTES, it will skip any core attribute in map without information about it
   *
   * @param sess                          perun session
   * @param attributesWithSearchingValues map of attributes names when attribute is type String, so value is string and
   *                                      we are looking for total match (Partial is not supported now, will be
   *                                      supported later by symbol *) when attribute is type Integer, so value is
   *                                      integer in String and we are looking for total match when attribute is type
   *                                      List<String>, so value is String and we are looking for at least one total or
   *                                      partial matching element when attribute is type Map<String> so value is String
   *                                      in format "key=value" and we are looking total match of both or if is it "key"
   *                                      so we are looking for total match of key IMPORTANT: In map there is not
   *                                      allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
   * @return list of groups who have attributes with specific values (behavior above) if no group exist, return empty
   * list of groups if empty map, return all groups
   * @throws AttributeNotExistsException
   * @throws WrongAttributeValueException wrong attribute value
   */
  List<Group> getGroups(PerunSession sess, Map<String, String> attributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeValueException;

  /**
   * Return all groups assigned to any resource with following conditions: 1] resource has set "resourceAttribute"
   * attribute with same value 2] group and resource has set "groupResourceAttribute" attribute with same value
   * Attribute values can't be empty. If there is no such group, return empty array.
   *
   * @param sess
   * @param groupResourceAttribute expected attribute set between a group and a resource (group need to be assigned to
   *                               the resource)
   * @param resourceAttribute      expected attribute set for assigned resource
   * @return list of groups with following conditions
   * @throws InternalErrorException if any of attributes is null or has empty value, if any of attributes is not in
   *                                expected namespace
   */
  List<Group> getGroupsByGroupResourceSetting(PerunSession sess, Attribute groupResourceAttribute,
                                              Attribute resourceAttribute);

  /**
   * Gets groups ids for potential application auto rejection.
   *
   * @return groups ids
   */
  List<Integer> getGroupsIdsForAppAutoRejection();

  /**
   * Return members with expiration date set, which will expire on today +/- X days. You can specify operator for
   * comparison (by default "=") returning exact match. So you can get all expired members (including today) using "<="
   * and zero days shift. or using "<" and +1 day shift.
   * <p>
   * Method ignores current member state, just compares expiration date !
   *
   * @param sess     PerunSession
   * @param operator One of "=", "<", ">", "<=", ">=". If null, "=" is anticipated.
   * @param days     X days before/after today
   * @return Members with expiration relative to method params.
   * @throws InternalErrorException
   */
  List<Member> getMembersByExpiration(PerunSession sess, String operator, int days);

  /**
   * Return members with expiration date set, which will expire on specified date. You can specify operator for
   * comparison (by default "=") returning exact match. So you can get all expired members (including today) using "<="
   * and today date. or using "<" and tomorrow date.
   * <p>
   * Method ignores current member state, just compares expiration date !
   *
   * @param sess     PerunSession
   * @param operator One of "=", "<", ">", "<=", ">=". If null, "=" is anticipated.
   * @param date     Date to compare expiration with (if null, current date is used).
   * @return Members with expiration relative to method params.
   * @throws InternalErrorException
   */
  List<Member> getMembersByExpiration(PerunSession sess, String operator, LocalDate date);

  /**
   * Return members with group expiration date set, which will expire on specified date in given group. You can specify
   * operator for comparison (by default "=") returning exact match. So you can get all expired members (including
   * today) using "<=" and today date. or using "<" and tomorrow date.
   * <p>
   * Method returns members with its expiration status for given group. Method ignores current member state, just
   * compares expiration date!
   * <p>
   * Method returns also indirect members of group with expiration set (by accident probably), but we manage status only
   * for direct members !!
   *
   * @param sess     Perun session
   * @param operator One of "=", "<", ">", "<=", ">=". If null, "=" is anticipated.
   * @param date     Date to compare expiration with (if null, current date is used).
   * @return Members with expiration relative to method params.
   * @throws InternalErrorException internal error
   */
  List<Member> getMembersByGroupExpiration(PerunSession sess, Group group, String operator, LocalDate date);

  /**
   * This method get Map of Attributes with searching values and try to find all resources, which have specific
   * attributes in format. Better information about format below. When there are more than 1 attribute in Map, it means
   * all must be true "looking for all of them" (AND)
   *
   * @param sess                          perun session
   * @param attributesWithSearchingValues map of attributes names when attribute is type String, so value is string and
   *                                      we are looking for exact or partial match based by parameter
   *                                      'allowPartialMatchForString' when attribute is type Integer, so value is
   *                                      integer in String and we are looking for total match when attribute is type
   *                                      List<String>, so value is String and we are looking for at least one total or
   *                                      partial matching element when attribute is type Map<String> so value is String
   *                                      in format "key=value" and we are looking total match of both or if is it "key"
   *                                      so we are looking for total match of key IMPORTANT: In map there is not
   *                                      allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
   * @param allowPartialMatchForString    if true, we are looking for partial match, if false, we are looking only for
   *                                      exact match (only for STRING type attributes)
   * @return list of resources that have attributes with specific values (behaviour above) if no such resource exists,
   * returns empty list
   * @throws InternalErrorException            internal error
   * @throws AttributeNotExistsException       when specified attribute does not exist
   * @throws WrongAttributeAssignmentException wrong attribute assignment
   * @throws WrongAttributeValueException      wrong attribute value
   */
  List<Resource> getResources(PerunSession sess, Map<String, String> attributesWithSearchingValues,
                              boolean allowPartialMatchForString)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * This method get Map of Attributes with searching values and try to find all users, which have specific attributes
   * in format. Better information about format below. When there are more than 1 attribute in Map, it means all must be
   * true "looking for all of them" (AND)
   *
   * @param sess                          perun session
   * @param attributesWithSearchingValues map of attributes names when attribute is type String, so value is string and
   *                                      we are looking for total match (Partial is not supported now, will be
   *                                      supported later by symbol *) when attribute is type Integer, so value is
   *                                      integer in String and we are looking for total match when attribute is type
   *                                      List<String>, so value is String and we are looking for at least one total or
   *                                      partial matching element when attribute is type Map<String> so value is String
   *                                      in format "key=value" and we are looking total match of both or if is it "key"
   *                                      so we are looking for total match of key IMPORTANT: In map there is not
   *                                      allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
   * @return list of users who have attributes with specific values (behavior above) if no user exist, return empty list
   * of users
   * @throws AttributeNotExistsException
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   */
  List<User> getUsers(PerunSession sess, Map<String, String> attributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * This method takes Map of Attributes (both member and user) with searching values and tries to find all users,
   * with specific values in the given attributes
   * More information about format below. An AND condition is implicit between the attributes we are searching by.
   *
   * @param sess                          perun session
   * @param attributesWithSearchingValues map of attributes names when attribute is type String, so value is string and
   *                                      we are looking for total match, when attribute is type Integer, so value is
   *                                      integer in String and we are looking for total match when attribute is type
   *                                      List<String>, so value is String and we are looking for at least one total or
   *                                      partial matching element when attribute is type Map<String> so value is String
   *                                      in format "key=value" and we are looking total match of both or if is it "key"
   *                                      so we are looking for total match of key IMPORTANT: In map there is not
   *                                      allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
   * @return list of members who have attributes with specific values (behavior above) if no member exist,
   * return empty list
   * @throws AttributeNotExistsException
   * @throws WrongAttributeAssignmentException wrong attribute value
   */
  List<Member> getMembers(PerunSession sess, Vo vo, Map<String, String> attributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * This method take map of coreAttributes with search values and return all users who have the specific match for all
   * of these core attributes.
   *
   * @param sess
   * @param coreAttributesWithSearchingValues
   * @return
   * @throws InternalErrorException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   */
  List<User> getUsersForCoreAttributes(PerunSession sess, Map<String, String> coreAttributesWithSearchingValues)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * Gets VOs ids for potential application auto rejection.
   *
   * @return VOs ids
   */
  List<Integer> getVosIdsForAppAutoRejection();

  /**
   * Similarity substring search in VOs, groups and facilities.
   * The amount of results is limited, based on the globalSearchLimit CoreConfig property
   *
   * @param sess session
   * @param searchString string to search for
   * @return map with lists of matched entities. The keys are `users`, `vos`, `groups`, `facilities`
   *      and values are lists containing the matched objects
   */
  Map<String, List<PerunBean>> globalSearch(PerunSession sess, String searchString);

  /**
   * Performs exact match on ID for user, VO, group and facility.
   * Use this method only when the search string is shorter than 3 digits, use other globalSearch methods otherwise.
   * The amount of results is limited, based on the globalSearchLimit CoreConfig property
   * @param sess session
   * @param searchId id to search for
   * @return map with lists of matched entities. The keys are `users`, `vos`, `groups`, `facilities`
   *       and values are lists containing the matched objects
   */
  Map<String, List<PerunBean>> globalSearchIDOnly(PerunSession sess, int searchId);

  /**
   * Similarity substring search in users, VOs, groups and facilities. Performs also an exact match for IDs should the
   * search string be a number.
   * The amount of results is limited, based on the globalSearchLimit CoreConfig property
   *
   * @param sess session
   * @param searchString string to search for
   * @return map with lists of matched entities. The keys are `users`, `vos`, `groups`, `facilities`
   *    and values are lists containing the matched objects
   */
  Map<String, List<PerunBean>> globalSearchPerunAdmin(PerunSession sess, String searchString);
}
