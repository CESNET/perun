package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * This interface serves as a template for checking and fillind in attribute for a member at a specified Resource.
 *
 * @author Lukáš Pravda <luky.pravda@gmail.com>
 */
public interface MemberResourceAttributesModuleImplApi extends AttributesModuleImplApi {

  /**
   * If you need to do some further work with other modules, this method do that
   *
   * @param session   session
   * @param member    the member
   * @param resource  the resource
   * @param attribute the attribute
   * @throws InternalErrorException                if there is any internal error
   * @throws WrongReferenceAttributeValueException if there is problem to process the change hook because of the value
   *                                               of referenced attribute
   */
  void changedAttributeHook(PerunSessionImpl session, Member member, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException;

  /**
   * This method checks semantics of Member's attribute value at a specified resource.
   *
   * @param perunSession Perun session
   * @param member       Member
   * @param resource     Resource
   * @param attribute    Attribute to be checked.
   * @throws InternalErrorException                if an exception is raised in particular implementation, the exception
   *                                               is wrapped in InternalErrorException
   * @throws WrongReferenceAttributeValueException if an referenced attribute against the parameter is to be compared is
   *                                               not available
   * @throws WrongAttributeAssignmentException
   */
  void checkAttributeSemantics(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

  /**
   * This method checks syntax of Member's attribute value at a specified resource.
   *
   * @param perunSession Perun session
   * @param member       Member
   * @param resource     Resource
   * @param attribute    Attribute to be checked.
   * @throws InternalErrorException       if an exception is raised in particular implementation, the exception is
   *                                      wrapped in InternalErrorException
   * @throws WrongAttributeValueException if the attribute value has wrong/illegal syntax
   */
  void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute)
      throws WrongAttributeValueException;

  /**
   * This method MAY fill Member's attributes at a specified resource.
   *
   * @param perunSession Perun session
   * @param member       Member
   * @param resource     Resource
   * @param attribute    Attribute to be filled in
   * @return Attribute which MAY be filled in.
   */
  Attribute fillAttribute(PerunSessionImpl perunSession, Member member, Resource resource,
                          AttributeDefinition attribute);
}
