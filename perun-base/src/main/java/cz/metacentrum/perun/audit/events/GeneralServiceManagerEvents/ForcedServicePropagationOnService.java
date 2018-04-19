package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.core.api.Service;

public class ForcedServicePropagationOnService {

    private String forcePropagation;
    private Service service;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public ForcedServicePropagationOnService(String forcePropagation, Service service) {
        this.forcePropagation = forcePropagation;
        this.service = service;
    }

    public ForcedServicePropagationOnService() {
    }

    public String getForcePropagation() {
        return forcePropagation;
    }

    public void setForcePropagation(String forcePropagation) {
        this.forcePropagation = forcePropagation;
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
        return forcePropagation + "On " + service + ".";
    }
}
