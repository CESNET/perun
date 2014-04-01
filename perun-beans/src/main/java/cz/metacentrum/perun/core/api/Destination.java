package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Destination where services are propagated.
 *
 * @author Slavek Licehammer
 */
public class Destination extends Auditable implements Comparable<Destination> {

    public final static String DESTINATIONHOSTTYPE = "host";
    public final static String DESTINATIONEMAILTYPE = "email";
    public final static String DESTINATIONSEMAILTYPE = "semail";
    public final static String DESTINATIONURLTYPE = "url";
    public final static String DESTINATIONUSERHOSTTYPE = "user@host";
    public final static String DESTINATIONUSERHOSTPORTTYPE = "user@host:port";

    private String destination;
    private String type;

    public Destination() {
      super();
    }

    public Destination(int id, String destination){
        super(id);
        this.destination = destination;
    }

    public Destination(int id, String destination, String type) {
        this(id, destination);
        this.type = type;
    }

    public Destination(int id, String destination, String type, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
        super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
        this.destination = destination;
        this.type = type;
    }

    /**
     * Gets the destination for this instance.
     *
     * @return The name.
     */
    public String getDestination() {
        return this.destination;
    }

    /**
     * Sets the destination for this instance.
     *
     * @param name The name.
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Gets the type for this instance.
     *
     * @return The type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Gets the hostname from destination
     * e.g. if destination is type user@host then return host
     * e.g. if destination is type user@host:port then return host
     * if destination is other type then those two, return destination without changes
     *
     * if there is no chars @ and :, return not changed type
     * if type is null, return this destination without changes
     * if destination null, return destination without changes (null)
     *
     * @return host from destination if possible to separate, in other case return destination without changes
     */
    public String getHostNameFromDestination() {
        if(this.destination == null) return this.destination;
        if(this.type == null) return this.destination;

        if(this.type.equals(DESTINATIONUSERHOSTPORTTYPE) || this.type.equals(DESTINATIONUSERHOSTTYPE)) {
            int startIndex = this.destination.indexOf('@');
            int endIndex = this.destination.indexOf(':');
            if(startIndex == -1) return this.destination;
            if(endIndex == -1) endIndex = this.destination.length();

            String hostname = this.destination.substring(startIndex, endIndex);
            return hostname;
        } else return this.destination;
    }

    /**
     * Sets the type for this instance.
     *
     * @param type The type.
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String serializeToString() {
        return this.getClass().getSimpleName() +":[" +
	"id=<" + getId() + ">" +
	", destination=<" + (getDestination() == null ? "\\0" : BeansUtils.createEscaping(getDestination())) + ">" +
	", type=<" + (getType() == null ? "\\0" : BeansUtils.createEscaping(getType())) + ">" +
	']';
    }

    public String toString() {
        return getClass().getSimpleName() + ":["
                + "id='" + getId()
                + "', destination='" + destination
                + "', type='" + type
                + "']";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + getId();
        result = prime * result + ((destination == null) ? 0 : destination.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Destination other = (Destination) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (getId() != other.getId()) {
            return false;
        }
        if (destination == null) {
            if (other.destination != null) {
                return false;
            }
        } else if (!destination.equals(other.destination)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Destination destination) {
        if (destination == null || this.destination == null) {
            throw new InternalErrorRuntimeException(new NullPointerException("Destination destination"));
        }
        return this.destination.compareTo(destination.getDestination());
    }
}
