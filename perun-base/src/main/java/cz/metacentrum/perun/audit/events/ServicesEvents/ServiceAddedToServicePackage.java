package cz.metacentrum.perun.audit.events.ServicesEvents;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServiceAddedToServicePackage {

    private Service service;
    private ServicesPackage servicesPackage;


    public ServiceAddedToServicePackage(Service service, ServicesPackage servicesPackage) {
        this.service = service;
        this.servicesPackage = servicesPackage;
    }

    @Override
    public String toString() {
        return service + " added to " + servicesPackage +".";
    }
}
