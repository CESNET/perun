package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.AttributeDefinition;

public class AttributeDeleted {
    private AttributeDefinition attributeDefinition;
    public AttributeDeleted(AttributeDefinition attribute) {
        attributeDefinition = attribute;
    }

    @Override
    public String toString() {
        return attributeDefinition + " deleted.";
    }
}
