package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServiceRemovedFromServicesPackage {

    private Service service;
    private ServicesPackage servicesPackage;


    public ServiceRemovedFromServicesPackage(Service service, ServicesPackage servicesPackage) {
        this.service = service;
        this.servicesPackage = servicesPackage;
    }

    @Override
    public String toString() {
        return service + " removed from " + servicesPackage + ".";
    }
}
