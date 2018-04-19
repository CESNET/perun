package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.BanOnFacility;

public class BanUpdatedForFacility {
    private BanOnFacility banOnFacility;
    private int userId;
    private int facilityId;

    private String name = this.getClass().getName();
    private String message;

    public BanUpdatedForFacility(BanOnFacility banOnFacility, int userId, int facilityId) {
        this.banOnFacility = banOnFacility;
        this.userId = userId;
        this.facilityId = facilityId;
    }

    public BanUpdatedForFacility() {
    }

    public BanOnFacility getBanOnFacility() {
        return banOnFacility;
    }

    public void setBanOnFacility(BanOnFacility banOnFacility) {
        this.banOnFacility = banOnFacility;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

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

    @Override
    public String toString() {
        return String.format("Ban %s was updated for userId %s on facilityId %s.",banOnFacility,userId,facilityId);
    }
}
