package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.taskslib.model.ExecService;

public class BanExecServiceOnFacility {

    private String banService;
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
    public BanExecServiceOnFacility(String banService, ExecService execService, Facility facility) {
        this.banService = banService;
        this.execService = execService;
        this.facility = facility;
    }

    public BanExecServiceOnFacility() {
    }

    public String getBanService() {
        return banService;
    }

    public void setBanService(String banService) {
        this.banService = banService;
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
        return banService + " " + execService + " on " + facility ;
    }
}
