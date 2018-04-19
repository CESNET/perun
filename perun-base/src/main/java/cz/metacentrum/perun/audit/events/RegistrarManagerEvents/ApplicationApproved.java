package cz.metacentrum.perun.audit.events.RegistrarManagerEvents;

import cz.metacentrum.perun.registrar.model.Application;

public class ApplicationApproved {

    private Application app;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ApplicationApproved() {
    }

    public ApplicationApproved(Application app) {
        this.app = app;
    }

    public Application getApp() {
        return app;
    }

    public void setApp(Application app) {
        this.app = app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Application ID=" + app.getId() + " voID=" + app.getVo().getId() + ((app.getGroup() != null) ? (" groupID=" + app.getGroup().getId()) : "") + " was approved.";
    }
}
