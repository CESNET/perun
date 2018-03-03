package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

public class DestinationsRemovedFromService {

    private Facility facility;
    private Service service;


    public DestinationsRemovedFromService(Service service, Facility facility) {
        this.facility = facility;
        this.service =service;
    }

    @Override
    public String toString() {
        return "All destinations removed from "+ service + " and " + facility + ".";
    }
}
