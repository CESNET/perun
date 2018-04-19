package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServicesPackageUpdated {
    private ServicesPackage servicesPackage;


    public ServicesPackageUpdated(ServicesPackage servicesPackage) {
        this.servicesPackage = servicesPackage;
    }

    @Override
    public String toString() {
        return servicesPackage + " updated.";
    }
}
