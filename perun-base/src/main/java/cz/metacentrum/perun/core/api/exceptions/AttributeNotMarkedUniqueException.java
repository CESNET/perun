package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;

/**
 * Attribute is not marked as unique.
 *
 * @author Metodej Klang
 */
public class AttributeNotMarkedUniqueException extends PerunException {

  private AttributeDefinition attribute;

  /**
   * @param message             message with details about the cause
   * @param attributeDefinition attributeDefinition that has not been marked as unique
   */
  public AttributeNotMarkedUniqueException(String message, AttributeDefinition attributeDefinition) {
    super(message + " " + attributeDefinition.toString());
    this.attribute = attributeDefinition;

  }

  /**
   * Getter for the attribute that has not been marked as unique
   *
   * @return attributeDefinition that has not been marked as unique
   */
  public AttributeDefinition getAttribute() {
    return attribute;
  }

}
