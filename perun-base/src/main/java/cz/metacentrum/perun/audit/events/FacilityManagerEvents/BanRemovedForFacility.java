package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.core.api.BanOnFacility;

public class BanRemovedForFacility {

    private BanOnFacility ban;
    private int userId;
    private int facilityId;

    private String name = this.getClass().getName();
    private String message;

    public BanRemovedForFacility(BanOnFacility ban, int userId, int facilityId) {
        this.ban = ban;
        this.userId = userId;
        this.facilityId = facilityId;
    }

    public BanRemovedForFacility() {
    }

    public BanOnFacility getBan() {
        return ban;
    }

    public void setBan(BanOnFacility ban) {
        this.ban = ban;
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
        return String.format("Ban %s was removed for userId %s on facilityId %s.",ban,userId,facilityId);
    }
}
