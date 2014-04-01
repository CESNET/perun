package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import java.util.List;
import java.util.Map;

/**
 * Searcher Class for searching objects by Map of Attributes
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public interface SearcherBl {

  /**
   * This method get Map of Attributes with searching values and try to find all users, which have specific attributes in format.
   * Better information about format below. When there are more than 1 attribute in Map, it means all must be true "looking for all of them" (AND)
   *
   * IMPORTANT: can't get CORE ATTRIBUTES
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
   *        if attributeWithSearchingValues is empty, return allUsers
   *
   * @throws AttributeNotExistsException
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  List<User> getUsers(PerunSession sess, Map<String, String> attributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;

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
   */
  List<User> getUsersForCoreAttributes(PerunSession sess, Map<String, String> coreAttributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException;
}
