package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.BanOnResource;

public class BanUpdatedForResource {

    private BanOnResource banOnResource;
    private int memberId;
    private int resourceId;

    public BanUpdatedForResource(BanOnResource banOnResource, int memberId, int resourceId) {
        this.banOnResource = banOnResource;
        this.memberId = memberId;
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return "Ban " + banOnResource + " was updated for memberId " + memberId + " on resourceId " + resourceId + ".";
    }
}
