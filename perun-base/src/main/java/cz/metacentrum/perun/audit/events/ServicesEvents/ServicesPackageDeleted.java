package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServicesPackageDeleted {
    private ServicesPackage servicesPackage;


    public ServicesPackageDeleted(ServicesPackage servicesPackage) {
        this.servicesPackage = servicesPackage;
    }

    @Override
    public String toString() {
        return servicesPackage + " deleted.";
    }
}
