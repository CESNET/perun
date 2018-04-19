package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.BanOnResource;

public class BanRemovedForResource {
    private BanOnResource banOnResource;
    private int memberId;
    private int resourceId;

    public BanRemovedForResource(BanOnResource ban, int memberId, int resourceId) {
        this.banOnResource = ban;
        this.memberId = memberId;
        this. resourceId = resourceId;
    }

    @Override
    public String toString() {
        return "Ban " + banOnResource + " was removed for memberId " + memberId + " on resourceId " + resourceId + ".";
    }
}
