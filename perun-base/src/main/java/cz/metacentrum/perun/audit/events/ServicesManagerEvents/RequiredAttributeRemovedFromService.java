package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;

public class RequiredAttributeRemovedFromService {

    private AttributeDefinition attributeDefinition;
    private Service service;

    public RequiredAttributeRemovedFromService(AttributeDefinition attribute, Service service) {
        this.attributeDefinition = attribute;
        this.service = service;
    }

    @Override
    public String toString() {
        return attributeDefinition + " removed from " + service + " as required attribute.";
    }
}
