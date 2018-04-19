package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

public class ServicePropagationPlanedOnFacilityAndService {

    private String propagationPlanned;
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
    public ServicePropagationPlanedOnFacilityAndService(String propagationPlanned, Facility facility, Service service) {
        this.propagationPlanned = propagationPlanned;
        this.facility = facility;
        this.service = service;
    }

    public ServicePropagationPlanedOnFacilityAndService() {
    }

    public String getPropagationPlanned() {
        return propagationPlanned;
    }

    public void setPropagationPlanned(String propagationPlanned) {
        this.propagationPlanned = propagationPlanned;
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
        return propagationPlanned + " On "+ facility +" and " + service;
    }
}
