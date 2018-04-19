package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

public class ForcedServicePropagationOnFacilityAndService {


    private String forcePropagation;
    private Facility facility;
    private Service service;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public ForcedServicePropagationOnFacilityAndService(String forcePropagation, Facility facility, Service service) {
        this.forcePropagation = forcePropagation;
        this.facility = facility;
        this.service = service;
    }

    public ForcedServicePropagationOnFacilityAndService() {
    }

    public String getForcePropagation() {
        return forcePropagation;
    }

    public void setForcePropagation(String forcePropagation) {
        this.forcePropagation = forcePropagation;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
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
        return forcePropagation + "On " + facility +" and " + service;
    }
}
