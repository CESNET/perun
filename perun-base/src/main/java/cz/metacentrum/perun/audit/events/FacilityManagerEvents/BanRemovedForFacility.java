package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.BanOnFacility;

public class BanRemovedForFacility {

    private BanOnFacility ban;
    private int userId;
    private int facilityId;

    public BanRemovedForFacility(BanOnFacility ban, int userId, int facilityId) {
        this.ban = ban;
        this.userId = userId;
        this.facilityId = facilityId;
    }

    @Override
    public String toString() {
        return "Ban "+ ban + " was removed for userId "+ userId +" on facilityId "+facilityId+".";
    }
}
