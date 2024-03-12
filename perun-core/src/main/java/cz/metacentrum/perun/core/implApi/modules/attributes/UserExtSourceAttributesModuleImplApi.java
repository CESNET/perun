package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * This interface serves as a template for checking and fillind in attribute
 * for a member at a specified UserExtSource.
 *
 * @author Jan Zvěřina <zverina.jan@email.cz>
 */
public interface UserExtSourceAttributesModuleImplApi extends AttributesModuleImplApi {

  /**
   * This method checks UserExtSource attribute value syntax.
   *
   * @param perunSession Perun session
   * @param ues
   * @param attribute    Attribute to be checked.
   * @throws InternalErrorException       if an exception is raised in particular
   *                                      implementation, the exception is wrapped in InternalErrorException
   * @throws WrongAttributeValueException if the attribute value has wrong/illegal syntax
   */
  void checkAttributeSyntax(PerunSessionImpl perunSession, UserExtSource ues, Attribute attribute)
      throws WrongAttributeValueException;

  /**
   * This method checks UserExtSource attribute value semantics.
   *
   * @param perunSession Perun session
   * @param ues
   * @param attribute    Attribute to be checked.
   */
  void checkAttributeSemantics(PerunSessionImpl perunSession, UserExtSource ues, Attribute attribute);

  /**
   * This method fill UserExtSource attributes.
   *
   * @param perunSession Perun session
   * @param ues
   * @param attribute    Attribute to be filled in
   * @return Attribute which MAY be filled in.
   */
  Attribute fillAttribute(PerunSessionImpl perunSession, UserExtSource ues, AttributeDefinition attribute);

  /**
   * If you need to do some further work with other modules, this method do that
   *
   * @param session   session
   * @param ues
   * @param attribute the attribute
   */
  void changedAttributeHook(PerunSessionImpl session, UserExtSource ues, Attribute attribute);
}
