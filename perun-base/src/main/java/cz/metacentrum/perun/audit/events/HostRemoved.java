package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Host;

public class HostRemoved {
    private Host host;
    public HostRemoved(Host host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return host +" removed.";
    }
}
