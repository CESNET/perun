package cz.metacentrum.perun.audit.events.AuthorshipManagementEvents;

import cz.metacentrum.perun.cabinet.model.Authorship;

public class AuthorshipCreated {

    private Authorship authorship;
    private String name = this.getClass().getName();
    private String message;


    public AuthorshipCreated(Authorship authorship) {
        this.authorship = authorship;
    }

    public AuthorshipCreated() {
    }

    public Authorship getAuthorship() {
        return authorship;
    }

    public void setAuthorship(Authorship authorship) {
        this.authorship = authorship;
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
        return String.format("Authorship %s created.", authorship.serializeToString());
    }
}
