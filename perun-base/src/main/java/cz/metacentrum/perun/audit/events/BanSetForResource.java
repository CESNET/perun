package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.BanOnResource;

public class BanSetForResource {

    private BanOnResource banOnResource;
    private int memberId;
    private int resourceId;

    public BanSetForResource(BanOnResource banOnResource, int memberId, int resourceId) {
        this.banOnResource = banOnResource;
        this.memberId = memberId;
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return "Ban " + banOnResource + " was set for memberId " + memberId + " on resourceId " + resourceId + ".";
    }
}
