package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * This interface serves as a template for checking facilities attributes.
 *
 * @author Milan Halenar <mhalenar@gmail.com>
 */
public interface GroupResourceAttributesModuleImplApi extends AttributesModuleImplApi {

  /**
   * If you need to do some further work with other modules, this method do that
   *
   * @param session   session
   * @param group     the group
   * @param resource  the resource
   * @param attribute the attribute
   */
  void changedAttributeHook(PerunSessionImpl session, Group group, Resource resource, Attribute attribute);

  /**
   * Checks if value of this group-resource attribute has valid semantics.
   *
   * @param perunSession perun session
   * @param group        group
   * @param resource     resource for which you want to check validity of attribute
   * @param attribute    attribute to check
   * @throws InternalErrorException            if an exception is raised in particular implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */

  void checkAttributeSemantics(PerunSessionImpl perunSession, Group group, Resource resource, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Checks if value of this group-resource attribute has valid syntax.
   *
   * @param perunSession perun session
   * @param group        group
   * @param resource     resource for which you want to check validity of attribute
   * @param attribute    attribute to check
   * @throws InternalErrorException       if an exception is raised in particular implementation, the exception is
   *                                      wrapped in InternalErrorException
   * @throws WrongAttributeValueException if the attribute value has wrong/illegal syntax
   */
  void checkAttributeSyntax(PerunSessionImpl perunSession, Group group, Resource resource, Attribute attribute)
      throws WrongAttributeValueException;

  /**
   * This method MAY fill an attribute at the specified resource.
   *
   * @param perunSession perun session
   * @param group        group
   * @param resource     resource for which you want to check validity of attribute
   * @param attribute    attribute to fill in
   * @return
   * @throws InternalErrorException            if an exception is raised in particular implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute fillAttribute(PerunSessionImpl perunSession, Group group, Resource resource, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException;
}
