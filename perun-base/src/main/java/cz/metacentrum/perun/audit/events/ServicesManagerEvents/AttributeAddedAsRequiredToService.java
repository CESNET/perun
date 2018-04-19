package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;

public class AttributeAddedAsRequiredToService {
    private AttributeDefinition attribute;
    private Service service;


    public AttributeAddedAsRequiredToService(AttributeDefinition attribute, Service service) {
        this.attribute =attribute;
        this.service = service;
    }

    @Override
    public String toString() {
        return attribute + " added to " + service + " as required attribute.";
    }
}
