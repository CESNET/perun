package cz.metacentrum.perun.audit.events;

import cz.metacentrum.perun.core.api.BanOnFacility;

public class BanUpdatedForFacility {
    private BanOnFacility banOnFacility;
    private int userId;
    private int facilityId;

    public BanUpdatedForFacility(BanOnFacility banOnFacility, int userId, int facilityId) {
        this.banOnFacility = banOnFacility;
        this.userId = userId;
        this.facilityId = facilityId;
    }

    @Override
    public String toString() {
        return "Ban "+ banOnFacility +" was updated for userId "+ userId +" on facilityId " + facilityId + ".";
    }
}
