package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.Facility;

public class DestinationsRemovedFromAllServices {

    private Facility facility;

    public DestinationsRemovedFromAllServices(Facility facility) {
        this.facility = facility;
    }

    @Override
    public String toString() {
        return "All destinations removed from " + facility + " for all services.";
    }
}
