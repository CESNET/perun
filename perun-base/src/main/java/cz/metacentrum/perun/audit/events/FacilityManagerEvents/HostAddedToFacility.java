package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Host;

public class HostAddedToFacility {
    private Host host;
    private Facility facility;

    private String name = this.getClass().getName();
    private String message;

    public HostAddedToFacility(Host host, Facility facility) {
        this.facility = facility;
        this.host = host;
    }

    public HostAddedToFacility() {
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return host + " added to facility "+ facility;
    }
}
