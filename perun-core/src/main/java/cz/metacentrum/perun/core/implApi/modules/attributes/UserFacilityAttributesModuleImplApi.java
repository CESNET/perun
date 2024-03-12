package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * @author Lukáš Pravda <luky.pravda@gmail.com>
 */
public interface UserFacilityAttributesModuleImplApi extends AttributesModuleImplApi {

  /**
   * Checks if value of assigned attribute in relationship between those two
   * entities has a correct syntax.
   *
   * @param session   Perun session
   * @param user      User of the facility.
   * @param facility  Facility to be used by a user.
   * @param attribute Attribute in relationship between facility and user.
   * @throws InternalErrorException       if an exception is raised in particular
   *                                      implementation, the exception is wrapped in InternalErrorException
   * @throws WrongAttributeValueException if the attribute value has wrong/illegal syntax
   */
  void checkAttributeSyntax(PerunSessionImpl session, User user, Facility facility, Attribute attribute)
      throws WrongAttributeValueException;

  /**
   * Checks if value of assigned attribute in relationship between those two
   * entities has a correct semantics.
   *
   * @param session   Perun session
   * @param user      User of the facility.
   * @param facility  Facility to be used by a user.
   * @param attribute Attribute in relationship between facility and user.
   * @throws InternalErrorException                if an exception is raised in particular
   *                                               implementation, the exception is wrapped in InternalErrorException
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   * @throws WrongAttributeAssignmentException
   */
  void checkAttributeSemantics(PerunSessionImpl session, User user, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Tries to fill an attribute in the relationship between a facility and
   * user
   *
   * @param session   Perun Session
   * @param user      User of the facility
   * @param facility  Facility to be used by user.
   * @param attribute Attribute in relationship between facility and user to be filled in.
   * @return Attribute which MAY be filled in
   * @throws InternalErrorException            if an exception is raised in particular
   *                                           implementation, the exception is wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute fillAttribute(PerunSessionImpl session, User user, Facility facility, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException;

  /**
   * If you need to do some further work with other modules, this method do that
   *
   * @param session   session
   * @param user      the user
   * @param facility  the facility
   * @param attribute the attribute
   */
  void changedAttributeHook(PerunSessionImpl session, User user, Facility facility, Attribute attribute);
}
