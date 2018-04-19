package cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents;

import cz.metacentrum.perun.core.api.ExtSource;

public class ExtSourceCreated {
    private ExtSource extSource;
    private String name = this.getClass().getName();
    private String message;

    public ExtSourceCreated(ExtSource extSource) {
        this.extSource = extSource;
    }

    public ExtSourceCreated() {
    }

    public ExtSource getExtSource() {
        return extSource;
    }

    public void setExtSource(ExtSource extSource) {
        this.extSource = extSource;
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
        return String.format("%s created.",extSource);
    }
}
