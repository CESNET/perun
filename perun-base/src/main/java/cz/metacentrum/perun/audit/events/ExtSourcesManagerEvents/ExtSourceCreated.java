package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.ExtSource;

public class ExtSourceCreated {
    private ExtSource extSource;
    public ExtSourceCreated(ExtSource extSource) {
        this.extSource = extSource;
    }
}
