package cz.metacentrum.perun.audit.events;

public abstract class AbstractEvent {

    protected String name;
    protected String message;

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
}
