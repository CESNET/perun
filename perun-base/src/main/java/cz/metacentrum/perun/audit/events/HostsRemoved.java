package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Host;

import java.util.List;

public class HostsRemoved {

    private List<Host> hosts;
    private Facility facility;

    public HostsRemoved(List<Host> hosts, Facility facility) {
        this.facility = facility;
        this.hosts = hosts;
    }

    @Override
    public String toString() {
        return "Hosts "+ hosts +" removed from cluster "+facility;
    }
}
