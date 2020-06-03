package cz.metacentrum.perun.core.api;

import java.util.Collections;
import java.util.List;

/**
 * Host with list of all its attributes
 * @author Metodej Klang <metodej.klang@gmail.com>
 */
public class EnrichedHost {

	private Host host;
	private List<Attribute> hostAttributes;

	public EnrichedHost(){
	}

	public EnrichedHost(Host host, List<Attribute> hostAttributes) {
		this.host = host;
		this.hostAttributes = hostAttributes;
	}

	public Host getHost() {
		return host;
	}

	public List<Attribute> getHostAttributes() {
		return hostAttributes;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public void setHostAttributes(List<Attribute> hostAttributes) {
		this.hostAttributes = hostAttributes;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append("EnrichedHost:[host='").append(host.toString()).append("', hostAttributes='").append(hostAttributes).append("']").toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EnrichedHost))
			return false;
		EnrichedHost other = (EnrichedHost) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (hostAttributes == null && other.getHostAttributes() == null)
			return true;
		if (hostAttributes == null
			|| other.getHostAttributes() == null
			|| hostAttributes.size() != other.getHostAttributes().size())
			return false;
		Collections.sort(hostAttributes);
		Collections.sort(other.getHostAttributes());
		return hostAttributes.equals(other.getHostAttributes());
	}
}
