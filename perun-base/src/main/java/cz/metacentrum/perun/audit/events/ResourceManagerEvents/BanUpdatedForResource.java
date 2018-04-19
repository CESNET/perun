package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.core.api.BanOnResource;

public class BanUpdatedForResource {

    private BanOnResource banOnResource;
    private int memberId;
    private int resourceId;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BanUpdatedForResource() {
    }

    public BanUpdatedForResource(BanOnResource banOnResource, int memberId, int resourceId) {
        this.banOnResource = banOnResource;
        this.memberId = memberId;
        this.resourceId = resourceId;
    }

    public BanOnResource getBanOnResource() {
        return banOnResource;
    }

    public void setBanOnResource(BanOnResource banOnResource) {
        this.banOnResource = banOnResource;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Ban " + banOnResource + " was updated for memberId " + memberId + " on resourceId " + resourceId + ".";
    }
}
