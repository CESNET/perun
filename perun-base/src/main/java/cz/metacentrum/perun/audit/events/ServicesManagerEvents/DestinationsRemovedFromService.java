package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

public class DestinationsRemovedFromService {

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

    public DestinationsRemovedFromService() {
    }

    public DestinationsRemovedFromService(Service service, Facility facility) {
        this.facility = facility;
        this.service =service;
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
        return "All destinations removed from "+ service + " and " + facility + ".";
    }
}
