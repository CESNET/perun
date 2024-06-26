package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Searcher Class for searching objects by Map of Attributes
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public interface SearcherImplApi {

  /**
   * This method get Map of Attributes with searching values and try to find all facilities, which have specific
   * attributes in format. Better information about format below. When there are more than 1 attribute in Map, it means
   * all must be true "looking for all of them" (AND)
   * <p>
   * IMPORTANT: can't get CORE ATTRIBUTES
   *
   * @param sess                          perun session
   * @param attributesWithSearchingValues map of attributes when attribute is type String, so value is string and we are
   *                                      looking for total match (Partial is not supported now, will be supported later
   *                                      by symbol *) when attribute is type Integer, so value is integer in String and
   *                                      we are looking for total match when attribute is type List<String>, so value
   *                                      is String and we are looking for at least one total or partial matching
   *                                      element when attribute is type Map<String> so value is String in format
   *                                      "key=value" and we are looking total match of both or if is it "key" so we are
   *                                      looking for total match of key IMPORTANT: In map there is not allowed char '='
   *                                      in key. First char '=' is delimiter in MAP item key=value!!!
   * @return list of facilities that have attributes with specific values (behavior above) if no such facility exists,
   * return empty list if attributeWithSearchingValues is empty, return all facilities
   * @throws InternalErrorException internal error
   * @throws WrongAttributeValueException wrong attribute value
   */
  List<Facility> getFacilities(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues) throws
      WrongAttributeValueException;

  /**
   * This method get Map of Attributes with searching values and try to find all groups, which have specific attributes
   * in format. Better information about format below. When there are more than 1 attribute in Map, it means all must be
   * true "looking for all of them" (AND)
   * <p>
   * IMPORTANT: can't get CORE ATTRIBUTES
   *
   * @param sess                          perun session
   * @param attributesWithSearchingValues map of attributes when attribute is type String, so value is string and we are
   *                                      looking for total match (Partial is not supported now, will be supported later
   *                                      by symbol *) when attribute is type Integer, so value is integer in String and
   *                                      we are looking for total match when attribute is type List<String>, so value
   *                                      is String and we are looking for at least one total or partial matching
   *                                      element when attribute is type Map<String> so value is String in format
   *                                      "key=value" and we are looking total match of both or if is it "key" so we are
   *                                      looking for total match of key IMPORTANT: In map there is not allowed char '='
   *                                      in key. First char '=' is delimiter in MAP item key=value!!!
   * @return list of groups who have attributes with specific values (behavior above) if no group exist, return empty
   * list of groups if attributeWithSearchingValues is empty, return all groups
   *
   * @throws WrongAttributeValueException wrong attribute value
   */
  List<Group> getGroups(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues)
      throws WrongAttributeValueException;

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
   * Return members with expiration date set, which will expire on date +/- X days. You can specify operator for
   * comparison (by default "=") returning exact match. So you can get all expired members (including today) using "<="
   * and zero days shift. or using "<" and +1 day shift.
   * <p>
   * Method ignores current member state, just compares expiration date !
   *
   * @param sess     PerunSession
   * @param operator One of "=", "<", ">", "<=", ">=". If null, "=" is anticipated.
   * @param date     Date to compare expiration with (if null, current date is used).
   * @param days     X days before/after today.
   * @return Members with expiration relative to method params.
   * @throws InternalErrorException
   */
  List<Member> getMembersByExpiration(PerunSession sess, String operator, LocalDate date, int days);

  /**
   * Return members who should expire in given group and with expiration date set, which will expire on date +/- X days.
   * You can specify operator for comparison (by default "=") returning exact match. So you can get all expired members
   * (including today) using "<=" and zero days shift. or using "<" and +1 day shift.
   * <p>
   * Method returns members with its expiration status for given group. Method ignores current member state, just
   * compares expiration date !
   *
   * @param sess     PerunSession
   * @param group    Group where members are searched in
   * @param operator One of "=", "<", ">", "<=", ">=". If null, "=" is anticipated.
   * @param date     Date to compare expiration with (if null, current date is used).
   * @param days     X days before/after today.
   * @return Members with expiration relative to method params.
   * @throws InternalErrorException internal error
   */
  List<Member> getMembersByGroupExpiration(PerunSession sess, Group group, String operator, LocalDate date, int days);

  /**
   * This method get Map of Attributes with searching values and try to find all resources, which have specific
   * attributes in format. Better information about format below. When there are more than 1 attribute in Map, it means
   * all must be true "looking for all of them" (AND)
   * <p>
   * IMPORTANT: can't get CORE ATTRIBUTES
   *
   * @param sess                          perun session
   * @param attributesWithSearchingValues map of attributes when attribute is type String, so value is string and we are
   *                                      looking for exact or partial match based by parameter
   *                                      'allowPartialMatchForString' when attribute is type Integer, so value is
   *                                      integer in String and we are looking for total match when attribute is type
   *                                      List<String>, so value is String and we are looking for at least one total or
   *                                      partial matching element when attribute is type Map<String> so value is String
   *                                      in format "key=value" and we are looking total match of both or if is it "key"
   *                                      so we are looking for total match of key IMPORTANT: In map there is not
   *                                      allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
   * @param allowPartialMatchForString    if true, we are looking for partial match, if false, we are looking only for
   *                                      exact match (only for STRING type attributes)
   * @return list of resources that have attributes with specific values (behavior above) if no such resource exists,
   * return empty list if attributeWithSearchingValues is empty, return all resources
   * @throws InternalErrorException internal error
   * @throws WrongAttributeValueException wrong attribute value
   */
  List<Resource> getResources(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues,
                              boolean allowPartialMatchForString) throws WrongAttributeValueException;

  /**
   * This method get Map of Attributes with searching values and try to find all users, which have specific attributes
   * in format. Better information about format below. When there are more than 1 attribute in Map, it means all must be
   * true "looking for all of them" (AND)
   * <p>
   * IMPORTANT: can't get CORE ATTRIBUTES
   *
   * @param sess                          perun session
   * @param attributesWithSearchingValues map of attributes when attribute is type String, so value is string and we are
   *                                      looking for total match (Partial is not supported now, will be supported later
   *                                      by symbol *) when attribute is type Integer, so value is integer in String and
   *                                      we are looking for total match when attribute is type List<String>, so value
   *                                      is String and we are looking for at least one total or partial matching
   *                                      element when attribute is type Map<String> so value is String in format
   *                                      "key=value" and we are looking total match of both or if is it "key" so we are
   *                                      looking for total match of key IMPORTANT: In map there is not allowed char '='
   *                                      in key. First char '=' is delimiter in MAP item key=value!!!
   * @return list of users who have attributes with specific values (behavior above) if no user exist, return empty list
   * of users if attributeWithSearchingValues is empty, return allUsers
   * @throws InternalErrorException
   * @throws WrongAttributeValueException wrong attribute value
   */
  List<User> getUsers(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues)
      throws WrongAttributeValueException;

  /**
   * This method takes a map of entity (member or user) to maps of Attributes with values to
   * search by, for the members in the given vo.
   * <pre>
   * E.g.: member -> Attribute(urn:perun:member:attribute-def:def:memAttr1...) -> val1
   *                 Attribute(urn:perun:member:attribute-def:def:memAttr2...) -> val2
   *       user ->   Attribute(urn:perun:user:attribute-def:def:userAttr1...) -> val3
   *                 Attribute(urn:perun:user:attribute-def:def:userAttr2...) -> val4
   * </pre>
   * A member would need to have matching values in both of the member attributes above and its associated user in both
   * the user attributes above in order to be returned.
   * <p>
   * IMPORTANT: can't get CORE and VIRTUAL ATTRIBUTES
   *
   * @param sess                              perun session
   * @param mapOfEntityToMapOfAttrsWithValues map of member and user keys to map of attributes with values to search by
   *                                      in those namespaces. The matching of the attributes by the values works as
   *                                      follows: when attribute is type String, so value is string and we are
   *                                      looking for total match (Partial is not supported now, will be supported later
   *                                      by symbol *) when attribute is type Integer, so value is integer in String and
   *                                      we are looking for total match when attribute is type List<String>, so value
   *                                      is String and we are looking for at least one total or partial matching
   *                                      element when attribute is type Map<String> so value is String in format
   *                                      "key=value" and we are looking total match of both or if is it "key" so we are
   *                                      looking for total match of key IMPORTANT: In map there is not allowed char '='
   *                                      in key. First char '=' is delimiter in MAP item key=value!!!
   * @return list of users who have attributes with specific values (behavior above) if no user exist, return empty list
   * of users if attributeWithSearchingValues is empty, return allUsers
   * @throws WrongAttributeValueException wrong attribute value
   */
  List<Member> getMembers(PerunSession sess,
                          Vo vo, Map<String, Map<Attribute, String>> mapOfEntityToMapOfAttrsWithValues)
      throws WrongAttributeValueException;

  /**
   * Gets VOs ids for potential application auto rejection.
   *
   * @return VOs ids
   */
  List<Integer> getVosIdsForAppAutoRejection();
}
