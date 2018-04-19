package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.core.api.Host;

public class AllAttributesRemovedForHost {

    private Host host;
    private String name = this.getClass().getName();
    private String message;

    public AllAttributesRemovedForHost(Host host) {
        this.host = host;
    }

    public AllAttributesRemovedForHost() {
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return String.format("All attributes removed for %s.",host);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("All attributes removed for %s.",host);
    }
}
