package cz.metacentrum.perun.audit.events;

import com.google.gwt.thirdparty.json.JSONObject;
import cz.metacentrum.perun.cabinet.model.Authorship;
import jdk.nashorn.api.scripting.JSObject;
import org.codehaus.jackson.map.util.JSONPObject;

public class AuthorshipDeleted {
    private Authorship authorship;

    public AuthorshipDeleted(Authorship authorship) {
        this.authorship = authorship;
    }

    @Override
    public String toString() {
        return "Authorship "+ authorship.serializeToString() + "deleted.";
    }
}
