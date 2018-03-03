package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;

import java.util.List;

public class AttributesAddedAsRequiredToService {
    private List<AttributeDefinition> attributes;
    private Service service;


    public AttributesAddedAsRequiredToService(List<? extends AttributeDefinition> attributes, Service service) {
    }

    @Override
    public String toString() {
        return attributes + " added to " + service + " as required attributes.";
    }

}
