package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

public class DestinationRemovedFromService {

    private Destination destination;
    private Service service;
    private Facility facility;


    public DestinationRemovedFromService(Destination destination, Service service, Facility facility) {
        this.destination = destination;
        this.facility = facility;
        this.service = service;
    }

    @Override
    public String toString() {
        return destination + " removed from " + service + " and " + facility + ".";
    }
}
