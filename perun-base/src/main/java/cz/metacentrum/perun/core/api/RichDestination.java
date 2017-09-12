package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Service;
import java.util.List;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Destination where services are propagated.
 * @author Michal Stava stavamichal@gmail.com
 */
public class RichDestination extends Destination implements Comparable<PerunBean> {
	private Service service;
	private Facility facility;

	public RichDestination(){
	}

	public RichDestination(Destination destination, Facility facility, Service service) {
		super(destination.getId(), destination.getDestination(), destination.getType(), destination.getCreatedAt(),
				destination.getCreatedBy(), destination.getModifiedAt(), destination.getModifiedBy(),
				destination.getCreatedByUid(), destination.getModifiedByUid());
		setPropagationType(destination.getPropagationType());
		this.service = service;
		this.facility = facility;
	}

	/*public RichDestination(User user, Member member, List<UserExtSource> userExtSources, List<Attribute> userAttributes, List<Attribute> memberAttributes) {
		this(user, member, userExtSources);
		this.userAttributes = userAttributes;
		this.memberAttributes = memberAttributes;
		}*/

	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((facility == null) ? 0 : facility.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
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
		RichDestination other = (RichDestination) obj;
		if (facility == null) {
			if (other.facility != null) {
				return false;
			}
		} else if (!facility.equals(other.facility)) {
			return false;
		}
		if (service == null) {
			if (other.service != null) {
				return false;
			}
		} else if (!service.equals(other.service)) {
			return false;
		}
		return true;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", destination=<").append(super.getDestination() == null ? "\\0" : BeansUtils.createEscaping(super.getDestination())).append(">").append(
			", type=<").append(super.getType() == null ? "\\0" : BeansUtils.createEscaping(super.getType())).append(">").append(
			", facility=<").append(getFacility() == null ? "\\0" : getFacility().serializeToString()).append(">").append(
			", service=<").append(getService() == null ? "\\0" : getService().serializeToString()).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":["
			).append("id='").append(getId()
			).append("', destination='").append(super.getDestination()
			).append("', type='").append(super.getType()
			).append("', facility='").append(getFacility()
			).append("', service='").append(getService()
			).append("']").toString();
	}
}
