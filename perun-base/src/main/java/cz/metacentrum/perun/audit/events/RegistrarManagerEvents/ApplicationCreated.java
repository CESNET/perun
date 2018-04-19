package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.registrar.model.Application;

public class ApplicationCreated {

    private Application app;
    private String name = this.getClass().getName();
    private String message;

    public ApplicationCreated() {
    }

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ApplicationCreated(Application app) {
        this.app = app;
    }

    public Application getApp() {
        return app;
    }

    public void setApp(Application app) {
        this.app = app;
    }

    @Override
    public String toString() {
        return String.format("New {} created.", app);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
