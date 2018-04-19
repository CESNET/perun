package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.registrar.model.Application;

public class ApplicationRejected {
    Application app;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ApplicationRejected() {
    }

    @Override
    public String toString() {
        return "Application ID=" + app.getId() + " voID=" + app.getVo().getId() + ((app.getGroup() != null) ? (" groupID=" + app.getGroup().getId()) : "") + " has been rejected.";
    }

    public Application getApplication() {
        return app;
    }

    public void setApplication(Application application) {
        this.app = application;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApplicationRejected(Application application) {
        this.app = application;
    }
}
