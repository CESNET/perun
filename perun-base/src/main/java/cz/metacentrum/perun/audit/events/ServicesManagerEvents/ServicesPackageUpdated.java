package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServicesPackageUpdated {
    private ServicesPackage servicesPackage;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ServicesPackageUpdated() {
    }

    public ServicesPackageUpdated(ServicesPackage servicesPackage) {
        this.servicesPackage = servicesPackage;
    }

    public ServicesPackage getServicesPackage() {
        return servicesPackage;
    }

    public void setServicesPackage(ServicesPackage servicesPackage) {
        this.servicesPackage = servicesPackage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return servicesPackage + " updated.";
    }
}
