package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.Owner;

public class OwnerDeleted {
    private Owner owner;

    public OwnerDeleted(Owner owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return owner + " deleted.";
    }
}
