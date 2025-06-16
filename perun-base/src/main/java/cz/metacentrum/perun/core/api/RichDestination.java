package cz.metacentrum.perun.core.api;

import java.sql.Timestamp;

/**
 * Destination where services are propagated.
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class RichDestination extends Destination implements Comparable<PerunBean> {
  private Service service;
  private Facility facility;
  private boolean blocked;
  private Timestamp lastSuccessfulPropagation;
  private Timestamp lastAttemptedPropagation;

  public RichDestination() {
  }

  public RichDestination(Destination destination, Facility facility, Service service) {
    this(destination, facility, service, false);
  }

  public RichDestination(Destination destination, Facility facility, Service service, boolean blocked) {
    super(destination.getId(), destination.getDestination(), destination.getType(), destination.getCreatedAt(),
        destination.getCreatedBy(), destination.getModifiedAt(), destination.getModifiedBy(),
        destination.getCreatedByUid(), destination.getModifiedByUid());
    setPropagationType(destination.getPropagationType());
    this.service = service;
    this.facility = facility;
    this.blocked = blocked;
  }

  public RichDestination(Destination destination, Facility facility, Service service, boolean blocked,
                         Timestamp lastSuccessfulPropagation) {
    super(destination.getId(), destination.getDestination(), destination.getType(), destination.getCreatedAt(),
        destination.getCreatedBy(), destination.getModifiedAt(), destination.getModifiedBy(),
        destination.getCreatedByUid(), destination.getModifiedByUid());
    setPropagationType(destination.getPropagationType());
    this.service = service;
    this.facility = facility;
    this.blocked = blocked;
    this.lastSuccessfulPropagation = lastSuccessfulPropagation;
  }

  public RichDestination(Destination destination, Facility facility, Service service, boolean blocked,
                         Timestamp lastSuccessfulPropagation, Timestamp lastAttemptedPropagation) {
    super(destination.getId(), destination.getDestination(), destination.getType(), destination.getCreatedAt(),
            destination.getCreatedBy(), destination.getModifiedAt(), destination.getModifiedBy(),
            destination.getCreatedByUid(), destination.getModifiedByUid());
    setPropagationType(destination.getPropagationType());
    this.service = service;
    this.facility = facility;
    this.blocked = blocked;
    this.lastSuccessfulPropagation = lastSuccessfulPropagation;
    this.lastAttemptedPropagation = lastAttemptedPropagation;
  }

  /*public RichDestination(User user, Member member, List<UserExtSource> userExtSources, List<Attribute> userAttributes,
                         List<Attribute> memberAttributes) {
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

  public boolean isBlocked() {
    return blocked;
  }

  public void setBlocked(boolean blocked) {
    this.blocked = blocked;
  }

  public Timestamp getLastSuccessfulPropagation() {
    return lastSuccessfulPropagation;
  }

  public Timestamp getLastAttemptedPropagation() {
    return lastAttemptedPropagation;
  }

  public void setLastSuccessfulPropagation(Timestamp lastSuccessfulPropagation) {
    this.lastSuccessfulPropagation = lastSuccessfulPropagation;
  }

  public void setLastAttemptedPropagation(Timestamp lastAttemptedPropagation) {
    this.lastAttemptedPropagation = lastAttemptedPropagation;
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

    return str.append(this.getClass().getSimpleName()).append(":[").append("id=<").append(getId()).append(">")
        .append(", destination=<")
        .append(super.getDestination() == null ? "\\0" : BeansUtils.createEscaping(super.getDestination())).append(">")
        .append(", type=<").append(super.getType() == null ? "\\0" : BeansUtils.createEscaping(super.getType()))
        .append(">").append(", facility=<").append(getFacility() == null ? "\\0" : getFacility().serializeToString())
        .append(">").append(", service=<").append(getService() == null ? "\\0" : getService().serializeToString())
        .append(">").append(", blocked=<").append(isBlocked()).append(">").append(", lastSuccessfulPropagation=<")
        .append(getLastSuccessfulPropagation()).append(">").append(", lastAttemptedPropagation=<")
        .append(getLastAttemptedPropagation()).append(">").append(']').toString();
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();

    return str.append(getClass().getSimpleName()).append(":[").append("id='").append(getId()).append("', destination='")
        .append(super.getDestination()).append("', type='").append(super.getType()).append("', facility='")
        .append(getFacility()).append("', service='").append(getService()).append("', blocked='").append(isBlocked())
        .append("', lastSuccessfulPropagation='").append(getLastSuccessfulPropagation())
        .append("', lastAttemptedPropagation='").append(getLastAttemptedPropagation()).append("']").toString();
  }
}
