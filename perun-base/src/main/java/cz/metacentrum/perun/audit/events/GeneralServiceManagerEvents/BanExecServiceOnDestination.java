package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.taskslib.model.ExecService;

public class BanExecServiceOnDestination {

    private String banService;
    private ExecService execService;
    private int destinationId;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public BanExecServiceOnDestination(String banService, ExecService execService, int destinationId) {
        this.banService = banService;
        this.execService = execService;
        this.destinationId = destinationId;
    }

    public BanExecServiceOnDestination() {
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

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return banService + " " + execService + " on " + destinationId ;
    }
}
