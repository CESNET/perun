package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.exceptions.rt.WrongReferenceAttributeValueRuntimeException;

/**
 * Attribute which is reference for used attribute has illegal value. Because of it, we can't determinate if used attribute have correct value.
 *
 * @author Slavek Licehammer
 * @author Michal Prochazka
 * @version $Id$
 */
public class WrongReferenceAttributeValueException extends AttributeValueException {

    static final long serialVersionUID = 0;
    private AttributeDefinition attribute;
    private AttributeDefinition referenceAttribute;

    public WrongReferenceAttributeValueException(String message) {
        super(message);
    }

    public WrongReferenceAttributeValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongReferenceAttributeValueException(Throwable cause) {
        super(cause);
    }

    public WrongReferenceAttributeValueException(AttributeDefinition attribute) {
        super(attribute == null ? "Attribute: null" : attribute.toString());
        this.attribute = attribute;
    }

    public WrongReferenceAttributeValueException(AttributeDefinition attribute, String message) {
        super(attribute == null ? "Attribute: null" : attribute + "  " + message);
        this.attribute = attribute;
    }

    public WrongReferenceAttributeValueException(AttributeDefinition attribute, AttributeDefinition referenceAttribute) {
        super((attribute == null ? "Attribute: null" : attribute) + " reference attribute " + (referenceAttribute == null ? "null" : referenceAttribute));
        this.attribute = attribute;
        this.referenceAttribute = referenceAttribute;
    }

    public WrongReferenceAttributeValueException(AttributeDefinition attribute, AttributeDefinition referenceAttribute, Throwable cause) {
        super((attribute == null ? "Attribute: null" : attribute) + " reference attribute " + (referenceAttribute == null ? "null" : referenceAttribute) +  " cause: " + cause.getMessage(), cause);
        this.attribute = attribute;
        this.referenceAttribute = referenceAttribute;
    }

    public WrongReferenceAttributeValueException(AttributeDefinition attribute, AttributeDefinition referenceAttribute, String message) {
        super(attribute == null ? "Attribute: null" : attribute + " reference attribute " + referenceAttribute == null ? "null" : referenceAttribute + " " + message);
        this.attribute = attribute;
        this.referenceAttribute = referenceAttribute;
    }
    
    public WrongReferenceAttributeValueException(AttributeDefinition attribute, AttributeDefinition referenceAttribute, String message, Throwable cause) {
        super(attribute == null ? "Attribute: null" : attribute + " reference attribute " + referenceAttribute == null ? "null" : referenceAttribute + " " + message + " cause:" + cause.getMessage());
        this.attribute = attribute;
        this.referenceAttribute = referenceAttribute;
    }

    public WrongReferenceAttributeValueException(AttributeDefinition attribute, AttributeDefinition referenceAttribute, Object attributeHolderPrimary, Object attributeHolderSecondary, String message) {
        super((attribute == null ? "Attribute: null" : attribute) + " reference attribute: " + (referenceAttribute == null ? "null" : referenceAttribute) + " " + (attributeHolderPrimary == null ? "AttributeHolderPrimary: null" : attributeHolderPrimary) + " " + (attributeHolderSecondary == null ? "" : attributeHolderSecondary) + " " + message);
        this.attribute = attribute;
        this.referenceAttribute = referenceAttribute;
    }

    public AttributeDefinition getAttribute() {
        return attribute;
    }

    public AttributeDefinition getReferenceAttribute() {
        return referenceAttribute;
    }

    public WrongReferenceAttributeValueException(WrongReferenceAttributeValueRuntimeException rt) {
        super(rt.getMessage(), rt);
    }
}
