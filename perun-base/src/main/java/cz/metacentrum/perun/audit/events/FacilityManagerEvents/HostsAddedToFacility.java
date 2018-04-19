package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Host;

import java.util.List;

public class HostsAdded {
    private List<Host> hosts;
    private Facility facility;

    public HostsAdded(List<Host> hosts, Facility facility) {
        this.facility = facility;
        this.hosts = hosts;
    }

    @Override
    public String toString() {
        return "Hosts" + hosts + " added to cluster" + facility + ".";
    }
}
