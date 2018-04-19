package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.AttributeDefinition;

public class AttributeDeleted {
    private AttributeDefinition attributeDefinition;
    private String name = this.getClass().getName();
    private String message;


    public AttributeDeleted(AttributeDefinition attribute) {
        attributeDefinition = attribute;
    }

    public AttributeDeleted() {
    }

    public AttributeDefinition getAttributeDefinition() {
        return attributeDefinition;
    }

    public void setAttributeDefinition(AttributeDefinition attributeDefinition) {
        this.attributeDefinition = attributeDefinition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("%s deleted.", attributeDefinition);
    }
}
