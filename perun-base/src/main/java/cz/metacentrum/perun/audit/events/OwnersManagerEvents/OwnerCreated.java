package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Owner;

public class OwnerCreated {
    private Owner owner;

    public OwnerCreated(Owner owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return owner + " created.";
    }
}
