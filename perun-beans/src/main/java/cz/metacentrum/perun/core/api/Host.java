package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
*
* Representation of the physical host.
*
* Most attributes are named accordingly to the GLUE specification:
* http://www.ogf.org/documents/GFD.147.pdf
* 
* @author  Michal Prochazka
* @author  Michal Karm Babacek
*/
public class Host extends Auditable {
	
	private String hostname;

	public Host() {
          super();
	}
	
	public Host(int id, String hostname) {
		super(id);
		this.hostname = hostname;
                
	}
        
        public Host(int id, String hostname, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid){
                super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
                this.hostname = hostname;
        }

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
        
        @Override
        public String serializeToString() {
                return this.getClass().getSimpleName() +":[" +
                "id=<" + getId() + ">" +
                ", hostname=<" + (getHostname() == null ? "\\0" : BeansUtils.createEscaping(getHostname())) + ">" +
                ']';
        }

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":[id='" + getId() + "', hostname='" + hostname + "']";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + getId();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Host other = (Host) obj;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (getId() != other.getId())
			return false;
		return true;
	}
}
