package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;

import java.util.List;

public class AttributesAddedAsRequiredToService {
    private List<AttributeDefinition> attributes;
    private Service service;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AttributesAddedAsRequiredToService() {
    }

    public AttributesAddedAsRequiredToService(List<? extends AttributeDefinition> attributes, Service service) {
    }

    public List<AttributeDefinition> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDefinition> attributes) {
        this.attributes = attributes;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return attributes + " added to " + service + " as required attributes.";
    }

}
