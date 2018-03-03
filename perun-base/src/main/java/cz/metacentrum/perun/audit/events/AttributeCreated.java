package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;

public class AttributeCreated {
    private AttributeDefinition attribute;

    public AttributeCreated(AttributeDefinition attribute) {
        this.attribute = attribute;
    }


    @Override
    public String toString() {
        return attribute + " created.";
    }
}
