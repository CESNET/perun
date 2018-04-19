package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;

import java.util.List;

public class RequiredAttributesRemovedFromService {

    private Service service;
    private List<? extends AttributeDefinition> attributes;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RequiredAttributesRemovedFromService() {
    }

    public RequiredAttributesRemovedFromService(List<? extends AttributeDefinition> attributes, Service service) {
        this.service = service;
        this.attributes = attributes;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public List<? extends AttributeDefinition> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<? extends AttributeDefinition> attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return attributes + " removed from "+ service + " as required attributes.";
    }
}
