package cz.metacentrum.perun.audit.events.OwnersManagerEvents;

import cz.metacentrum.perun.core.api.Owner;

public class OwnerCreated {
    private Owner owner;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OwnerCreated(Owner owner) {
        this.owner = owner;
    }

    public OwnerCreated() {
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return owner + " created.";
    }
}
