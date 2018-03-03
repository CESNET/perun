package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.Destination;

public class DestinationCreated {

    private Destination destination;

    public DestinationCreated(Destination destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return destination + " created.";
    }
}
