package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.cabinet.model.Authorship;

public class AuthorshipCreated {

    private Authorship authorship;

    public AuthorshipCreated(Authorship authorship) {
        this.authorship = authorship;
    }

    @Override
    public String toString() {
        return "Authorship " + authorship.serializeToString() + "created.";
    }
}
