package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.core.api.Service;

public class AllRequiredAttributesRemovedFromService {

    private Service service;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AllRequiredAttributesRemovedFromService() {
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

    public AllRequiredAttributesRemovedFromService(Service service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return "All required attributes removed from " + service + ".";
    }
}
