package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum SearcherMethod implements ManagerMethod {

  /*#
   * This method get Map of Attributes with searching values and try to find all users, which have specific
   * attributes in format.
   * Better information about format below. When there are more than 1 attribute in Map, it means all must be true
   * "looking for all of them" (AND)
   *
   * @param attributesWithSearchingValues Map<String,String> map of attributes names with values
   *        when attribute is type String, so value is string and we are looking for total match (Partial is not
   * supported now, will be supported later by symbol *)
   *        when attribute is type Integer, so value is integer in String and we are looking for total match
   *        when attribute is type List<String>, so value is String and we are looking for at least one total or
   * partial matching element
   *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total
   * match of both or if is it "key" so we are looking for total match of key
   *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
   * @return List<User> list of users who have attributes with specific values (behaviour above)
   *        if no user exist, return empty list of users
   *        if attributeWithSearchingValues is empty, return allUsers
   */
  getUsers {
    @Override
    public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      return ac.getSearcher()
          .getUsers(ac.getSession(), parms.read("attributesWithSearchingValues", LinkedHashMap.class));
    }
  },

  /*#
   * This method get Map of user Attributes with searching values and try to find all members, which have specific
   * attributes in format for specific VO.
   * Better information about format below. When there are more than 1 attribute in Map, it means all must be true
   * "looking for all of them" (AND)
   *
   * if principal has no rights for operation, throw exception
   * if principal has no rights for some attribute on specific user, do not return this user
   * if attributesWithSearchingValues is null or empty, return all members from vo if principal has rights for this
   * operation
   *
   * @param userAttributesWithSearchingValues Map<String,String> map of attributes names with values
   *        when attribute is type String, so value is string and we are looking for total match (Partial is not
   * supported now, will be supported later by symbol *)
   *        when attribute is type Integer, so value is integer in String and we are looking for total match
   *        when attribute is type List<String>, so value is String and we are looking for at least one total or
   * partial matching element
   *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total
   * match of both or if is it "key" so we are looking for total match of key
   *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
   * @param vo VO
   * @return List<Member> list of members who have attributes with specific values (behaviour above)
   *        if no user exist, return empty list of users
   *        if attributeWithSearchingValues is empty, return allUsers
   */
  getMembersByUserAttributes {
    @Override
    public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      return ac.getSearcher().getMembersByUserAttributes(ac.getSession(), ac.getVoById(parms.readInt("vo")),
          parms.read("userAttributesWithSearchingValues", LinkedHashMap.class));
    }
  },

  /*#
   * This method get Map of user Attributes with searching values and try to find all members, which have specific
   * attributes in format for specific VO. Better information about format below. When there are more than 1 attribute
   * in Map, it means all must be true "looking for all of them" (AND)
   * <p>
   * if principal has no rights for operation, throw exception if principal has no rights for some attribute on specific
   * user, do not return this user if attributesWithSearchingValues is null or empty, return all members from vo if
   * principal has rights for this operation
   *
   * @param userAttributesWithSearchingValues map of attributes names when attribute is type String, so value is string
   *                                          and we are looking for total match (Partial is not supported now, will be
   *                                          supported later by symbol *) when attribute is type Integer, so value is
   *                                          integer in String and we are looking for total match when attribute is
   *                                          type List<String>, so value is String and we are looking for at least one
   *                                          total or partial matching element when attribute is type Map<String> so
   *                                          value is String in format "key=value" and we are looking total match of
   *                                          both or if is it "key" so we are looking for total match of key IMPORTANT:
   *                                          In map there is not allowed char '=' in key. First char '=' is delimiter
   *                                          in MAP item key=value!!!
   * @return list of members who have attributes with specific values (behaviour above) if no member exist, return empty
   * list of members
   * @throws AttributeNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws WrongAttributeAssignmentException
   * @throws VoNotExistsException
   */
  getMembers {
    @Override
    public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      return ac.getSearcher().getMembers(ac.getSession(), ac.getVoById(parms.readInt("vo")),
          parms.read("attributesWithSearchingValues", LinkedHashMap.class));
    }
  },

  /*#
   * This method get Map of Attributes with searching values and try to find all facilities, which have specific
   * attributes in format.
   * Better information about format below. When there are more than 1 attribute in Map, it means all must be true
   * "looking for all of them" (AND)
   *
   * @param attributesWithSearchingValues map of attributes names
   *        when attribute is type String, so value is string and we are looking for total match (Partial is not
   * supported now, will be supported later by symbol *)
   *        when attribute is type Integer, so value is integer in String and we are looking for total match
   *        when attribute is type List<String>, so value is String and we are looking for at least one total or
   * partial matching element
   *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total
   * match of both or if is it "key" so we are looking for total match of key
   *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
   * @return list of facilities that have attributes with specific values (behaviour above)
   *        if no such facility exists, returns empty list
   */
  getFacilities {
    @Override
    public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      return ac.getSearcher()
          .getFacilities(ac.getSession(), parms.read("attributesWithSearchingValues", LinkedHashMap.class));
    }
  },

  /*#
   * This method get Map of Attributes with searching values and try to find all resources, which have specific
   * attributes in format.
   * Better information about format below. When there are more than 1 attribute in Map, it means all must be true
   * "looking for all of them" (AND)
   *
   * @param attributesWithSearchingValues map of attributes names
   *        when attribute is type String, so value is string and we are looking for exact match
   *        when attribute is type Integer, so value is integer in String and we are looking for total match
   *        when attribute is type List<String>, so value is String and we are looking for at least one total or
   * partial matching element
   *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total
   * match of both or if is it "key" so we are looking for total match of key
   *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
   * @return list of resources that have attributes with specific values (behaviour above)
   *        if no such resource exists, returns empty list
   */
  /*#
   * This method get Map of Attributes with searching values and try to find all resources, which have specific
   * attributes in format.
   * Better information about format below. When there are more than 1 attribute in Map, it means all must be true
   * "looking for all of them" (AND)
   *
   * @param attributesWithSearchingValues map of attributes names
   *        when attribute is type String, so value is string and we are looking for exact or partial match based by
   * parameter 'allowPartialMatchForString'
   *        when attribute is type Integer, so value is integer in String and we are looking for total match
   *        when attribute is type List<String>, so value is String and we are looking for at least one total or
   * partial matching element
   *        when attribute is type Map<String> so value is String in format "key=value" and we are looking total
   * match of both or if is it "key" so we are looking for total match of key
   *        IMPORTANT: In map there is not allowed char '=' in key. First char '=' is delimiter in MAP item key=value!!!
   * @param allowPartialMatchForString if true, we are looking for partial match, if false, we are looking only for
   * exact match (only for STRING type attributes)
   * @return list of resources that have attributes with specific values (behaviour above)
   *        if no such resource exists, returns empty list
   */
  getResources {
    @Override
    public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("allowPartialMatchForString")) {
        return ac.getSearcher()
            .getResources(ac.getSession(), parms.read("attributesWithSearchingValues", LinkedHashMap.class),
                parms.readBoolean("allowPartialMatchForString"));
      } else {
        return ac.getSearcher()
            .getResources(ac.getSession(), parms.read("attributesWithSearchingValues", LinkedHashMap.class), false);
      }
    }
  },

  /*#
   * Similarity substring search in users, VOs, groups and facilities.
   * The amount of results is limited bt the globalSearchLimit CoreConfig property
   * Searches in the following priority by the following fields:
   *    Users - fullname, logins   PERUNADMIN ONLY
   *    Vos - shortname, name
   *    Groups - name, description
   *    Facilities - name, descriptin
   *
   * If called by PERUNADMIN, also searches by id in each entity. The ids are exact matched
   * Should the string be shorter than 3 characters and is a number solely an ID search will be executed
   *
   * @param searchString Text to search by
   * @return map with lists of matched entities. The keys are `users`, `vos`, `groups`, `facilities`
   *      and values are lists containing the matched objects
   */
  globalSearch {
    @Override
    public Map<String, List<PerunBean>> call(ApiCaller ac, Deserializer parms) throws PerunException {
      String searchString = parms.readString("searchString");
      if (searchString.length() < 3) {
        try {
          return ac.getSearcher().globalSearchIDOnly(ac.getSession(), Integer.parseInt(searchString));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("searchString has to be an integer when searching under 3 characters");
        }
      }
      return ac.getSearcher().globalSearch(ac.getSession(), parms.readString("searchString"));
    }
  };
}
