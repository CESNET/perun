package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Host;

public class HostAdded {
    private Host host;
    private Facility facility;

    public HostAdded(Host host, Facility facility) {
        this.facility = facility;
        this.host = host;
    }

    @Override
    public String toString() {
        return host+" added to " + facility + ".";
    }
}
