package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

public class DenialsOnDestinationFreed {

    private String freeAllDen;
    private int destinationId;
    private String name = this.getClass().getName();
    private String message;

    public String getMessage() {
        return toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public DenialsOnDestinationFreed(String freeAllDen, int destinationId) {
        this.freeAllDen = freeAllDen;
        this.destinationId = destinationId;
    }

    public DenialsOnDestinationFreed() {
    }

    public String getFreeAllDen() {
        return freeAllDen;
    }

    public void setFreeAllDen(String freeAllDen) {
        this.freeAllDen = freeAllDen;
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
        return freeAllDen + " on " + destinationId;
    }
}
