package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AnonymizationNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Interface for checking and filling in user's attributes.
 *
 * @author Lukáš Pravda <luky.pravda@gmail.com>
 */
public interface UserAttributesModuleImplApi extends AttributesModuleImplApi {

  /**
   * Checks if assigned attribute value to the user has valid syntax.
   *
   * @param perunSession PerunSession
   * @param user         User
   * @param attribute    Attribute of the user.
   * @throws InternalErrorException       if an exception is raised in particular
   *                                      implementation, the exception is wrapped in InternalErrorException
   * @throws WrongAttributeValueException if the attribute value has wrong/illegal syntax
   */
  void checkAttributeSyntax(PerunSessionImpl perunSession, User user, Attribute attribute)
      throws WrongAttributeValueException;

  /**
   * Checks if value of assigned attribute to the user has valid semantics.
   *
   * @param perunSession PerunSession
   * @param user         User
   * @param attribute    Attribute of the user.
   * @throws InternalErrorException                if an exception is raised in particular
   *                                               implementation, the exception is wrapped in InternalErrorException
   * @throws WrongReferenceAttributeValueException if an referenced attribute against
   *                                               the parameter is to be compared is not available
   * @throws WrongAttributeAssignmentException     if attribute does not belong to appropriate entity
   */
  void checkAttributeSemantics(PerunSessionImpl perunSession, User user, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Tries to fill an attribute to the specified user.
   *
   * @param perunSession PerunSession
   * @param user         User
   * @param attribute    Attribute in relationship between facility and user to be filled in.
   * @return Attribute which MAY be filled in
   * @throws InternalErrorException            if an exception is raised in particular
   *                                           implementation, the exception is wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException;

  /**
   * If you need to do some further work with other modules, this method do that
   *
   * @param session   session
   * @param user      the user
   * @param attribute the attribute
   */
  void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException;

  /**
   * Gets anonymized value of the attribute.
   *
   * @param perunSession PerunSession
   * @param user         User
   * @param attribute    Attribute of the user.
   * @return Attribute with anonymized value
   * @throws InternalErrorException             if an exception is raised in particular
   *                                            implementation, the exception is wrapped in InternalErrorException
   * @throws AnonymizationNotSupportedException if the module doesn't implement this method
   */
  Attribute getAnonymizedValue(PerunSessionImpl perunSession, User user, Attribute attribute)
      throws AnonymizationNotSupportedException;
}
