package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.AttributeDefinition;

public class AttributeAuthzDeleted {
    private AttributeDefinition attributeDefinition;
    public AttributeAuthzDeleted(AttributeDefinition attribute) {
        attributeDefinition = attribute;
    }

    @Override
    public String toString() {
        return "All authorization information were deleted for "+ attributeDefinition +".";
    }
}
