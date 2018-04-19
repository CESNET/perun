package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;

import java.util.List;

public class RequiredAttributesRemovedFromService {

    private Service service;
    private List<? extends AttributeDefinition> attributes;

    public RequiredAttributesRemovedFromService(List<? extends AttributeDefinition> attributes, Service service) {
        this.service = service;
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return attributes + " removed from "+ service + " as required attributes.";
    }
}
