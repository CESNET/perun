package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.taskslib.model.ExecService;

public class DenialOfExecServiceOnFacilityFreed {

    private String freeDenOfExecservice;
    private ExecService execService;
    private Facility facility;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public DenialOfExecServiceOnFacilityFreed(String freeDenOfExecservice, ExecService execService, Facility facility) {
        this.freeDenOfExecservice = freeDenOfExecservice;
        this.execService = execService;
        this.facility = facility;
    }

    public DenialOfExecServiceOnFacilityFreed() {
    }

    public String getFreeDenOfExecservice() {
        return freeDenOfExecservice;
    }

    public void setFreeDenOfExecservice(String freeDenOfExecservice) {
        this.freeDenOfExecservice = freeDenOfExecservice;
    }

    public ExecService getExecService() {
        return execService;
    }

    public void setExecService(ExecService execService) {
        this.execService = execService;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return freeDenOfExecservice + " " + execService + " on " + facility;
    }
}
