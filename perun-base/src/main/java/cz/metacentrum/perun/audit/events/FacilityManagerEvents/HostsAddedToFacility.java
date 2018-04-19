package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Host;

import java.util.List;

public class HostsAddedToFacility {
    private List<Host> hosts;
    private Facility facility;

    private String name = this.getClass().getName();
    private String message;


    public HostsAddedToFacility(List<Host> hosts, Facility facility) {
        this.facility = facility;
        this.hosts = hosts;
    }

    public HostsAddedToFacility() {
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
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
        return String.format("Hosts %s added to cluster %s.",hosts,facility);
    }
}
