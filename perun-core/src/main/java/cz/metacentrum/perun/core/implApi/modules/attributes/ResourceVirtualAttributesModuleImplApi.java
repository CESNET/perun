package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * This interface serves as a template for virtual attributes.
 *
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public interface ResourceVirtualAttributesModuleImplApi
    extends ResourceAttributesModuleImplApi, VirtualAttributesModuleImplApi {

  /**
   * This method will return computed value.
   *
   * @param sess      PerunSession
   * @param resource  resource which is needed for computing the value
   * @param attribute attribute to operate on
   * @return
   * @throws InternalErrorException if an exception is raised in particular implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  Attribute getAttributeValue(PerunSessionImpl sess, Resource resource, AttributeDefinition attribute);

  /**
   * Currently do nothing.
   *
   * @param sess      PerunSession
   * @param resource  resource which is needed for computing the value
   * @param attribute attribute to operate on
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws InternalErrorException                if an exception is raised in particular implementation, the exception
   *                                               is wrapped in InternalErrorException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeValueException
   */
  boolean removeAttributeValue(PerunSessionImpl sess, Resource resource, AttributeDefinition attribute)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Method sets attributes' values which are dependent on this virtual attribute.
   *
   * @param sess      PerunSession
   * @param resource  resource which is needed for computing the value
   * @param attribute attribute to operate on
   * @return true if attribute was really changed
   * @throws InternalErrorException if an exception is raised in particular implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  boolean setAttributeValue(PerunSessionImpl sess, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException;
}
