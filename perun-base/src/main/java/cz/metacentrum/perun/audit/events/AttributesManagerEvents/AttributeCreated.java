package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;

public class AttributeCreated {

    private AttributeDefinition attribute;
    private String name = this.getClass().getName();
    private String message;


    public AttributeCreated(AttributeDefinition attribute) {
        this.attribute = attribute;
    }

    public AttributeCreated() {
    }

    public AttributeDefinition getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeDefinition attribute) {
        this.attribute = attribute;
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
        return attribute + " created.";
    }
}
