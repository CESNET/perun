package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.ExtSource;

public class ExtSourceDeleted {
    private ExtSource extSource;

    public ExtSourceDeleted(ExtSource extSource) {
        this.extSource = extSource;
    }
}
