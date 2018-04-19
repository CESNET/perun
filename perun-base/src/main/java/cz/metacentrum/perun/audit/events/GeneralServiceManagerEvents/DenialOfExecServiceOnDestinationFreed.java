package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.taskslib.model.ExecService;

public class DenialOfExecServiceOnDestinationFreed {

    private String freeDenOfExecservice;
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
    public DenialOfExecServiceOnDestinationFreed(String freeDenOfExecservice, ExecService execService, int destinationId) {
        this.freeDenOfExecservice = freeDenOfExecservice;
        this.execService = execService;
        this.destinationId = destinationId;
    }

    public DenialOfExecServiceOnDestinationFreed() {
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
        return freeDenOfExecservice + " " + execService + " on " + destinationId;
    }
}
