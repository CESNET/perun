package cz.metacentrum.perun.audit.events.AuthorshipManagementEvents;

import com.google.gwt.thirdparty.json.JSONObject;
import cz.metacentrum.perun.cabinet.model.Authorship;
import jdk.nashorn.api.scripting.JSObject;
import org.codehaus.jackson.map.util.JSONPObject;

public class AuthorshipDeleted {
    private Authorship authorship;
    private String name = this.getClass().getName();
    private String message;

    public AuthorshipDeleted(Authorship authorship) {
        this.authorship = authorship;
    }

    public AuthorshipDeleted() {
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
        return String.format("Authorship %s deleted.", authorship.serializeToString());
    }
}
