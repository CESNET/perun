package cz.metacentrum.perun.core.api;

/**
 * Destination where services are propagated.
 *
 * @author Slavek Licehammer
 */
public class Destination extends Auditable implements Comparable<PerunBean> {

	public final static String DESTINATIONHOSTTYPE = "host";
	public final static String DESTINATIONEMAILTYPE = "email";
	public final static String DESTINATIONSEMAILTYPE = "semail";
	public final static String DESTINATIONURLTYPE = "url";
	public final static String DESTINATIONUSERHOSTTYPE = "user@host";
	public final static String DESTINATIONUSERHOSTPORTTYPE = "user@host:port";
	public final static String DESTINATIONSERVICESPECIFICTYPE = "service-specific";
	public final static String DESTINATIONWINDOWS = "user@host-windows";
	public final static String DESTINATIONWINDOWSPROXY = "host-windows-proxy";

	public static final String PROPAGATIONTYPE_PARALLEL = "PARALLEL";
	public static final String PROPAGATIONTYPE_SERIAL = "SERIAL";
	public static final String PROPAGATIONTYPE_DUMMY = "DUMMY";

	private String destination;
	private String type;
	private String propagationType = PROPAGATIONTYPE_PARALLEL;

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

	public Destination(int id, String destination, String type, String propagationType) {
		this(id, destination, type);
		this.propagationType = propagationType;
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
	 * @param destination The destination.
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
	 * 	Gets the propagation type for this instance.
	 *
	 *  @return The propagation type, either "PARALLEL", "SERIAL" or "DUMMY"
	 */
	public String getPropagationType() {
		return this.propagationType;
	}

	/**
	 * Gets the hostname from destination
	 * e.g. if destination is type user@host then return host
	 * e.g. if destination is type user@host:port then return host
	 * e.g. if destination is type user@host-windows then return host-windows
	 * if destination is other type then these three, return destination without changes
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

		if(this.type.equals(DESTINATIONUSERHOSTPORTTYPE) || this.type.equals(DESTINATIONUSERHOSTTYPE) || this.type.equals(DESTINATIONWINDOWS)) {
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

	/**
	 * Sets the propagation type for this instance.
	 *
	 * @param type The propagation type.
	 */
	public void setPropagationType(String type) {
		this.propagationType = type;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", destination=<").append(getDestination() == null ? "\\0" : BeansUtils.createEscaping(getDestination())).append(">").append(
			", type=<").append(getType() == null ? "\\0" : BeansUtils.createEscaping(getType())).append(">").append(
			", propagationtype=<").append(getPropagationType() == null ? "\\0" : BeansUtils.createEscaping(getPropagationType())).append(">").append(
			']').toString();
	}

	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":["
			).append("id='").append(getId()
			).append("', destination='").append(destination
			).append("', type='").append(type
			).append("', propagationtype='").append(propagationType
			).append("']").toString();
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
	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof Destination) {
			Destination destination = (Destination) perunBean;
			if (this.getDestination() == null && destination.getDestination() != null) return -1;
			if (destination.getDestination() == null && this.getDestination() != null) return 1;
			if (this.getDestination() == null && destination.getDestination() == null) return 0;
			return this.getDestination().compareToIgnoreCase(destination.getDestination());
		} else {
			return (this.getId() - perunBean.getId());
		}
	}
}
