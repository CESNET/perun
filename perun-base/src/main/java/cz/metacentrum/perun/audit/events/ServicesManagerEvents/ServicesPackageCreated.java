package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServicesPackageCreated {

    private ServicesPackage servicesPackage;


    public ServicesPackageCreated(ServicesPackage servicesPackage) {
        this.servicesPackage = servicesPackage;
    }

    @Override
    public String toString() {
        return servicesPackage + " created.";
    }
}
