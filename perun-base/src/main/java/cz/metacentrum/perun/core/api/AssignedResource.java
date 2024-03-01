package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Represents resource assigned to a group.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class AssignedResource {
  private EnrichedResource enrichedResource;
  private GroupResourceStatus status;
  private Integer sourceGroupId;
  private String failureCause;
  private Facility facility;
  private List<ResourceTag> resourceTags;
  private boolean autoAssignSubgroups;

  public AssignedResource(EnrichedResource enrichedResource, GroupResourceStatus status, Integer sourceGroupId,
                          String failureCause, Facility facility, boolean autoAssignSubgroups) {
    this.enrichedResource = enrichedResource;
    this.status = status;
    this.sourceGroupId = sourceGroupId;
    this.failureCause = failureCause;
    this.facility = facility;
    this.autoAssignSubgroups = autoAssignSubgroups;
  }

  public EnrichedResource getEnrichedResource() {
    return enrichedResource;
  }

  public void setEnrichedResource(EnrichedResource enrichedResource) {
    this.enrichedResource = enrichedResource;
  }

  public GroupResourceStatus getStatus() {
    return status;
  }

  public void setStatus(GroupResourceStatus status) {
    this.status = status;
  }

  public Integer getSourceGroupId() {
    return sourceGroupId;
  }

  public void setSourceGroupId(Integer sourceGroupId) {
    this.sourceGroupId = sourceGroupId;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public List<ResourceTag> getResourceTags() {
    return resourceTags;
  }

  public void setResourceTags(List<ResourceTag> resourceTags) {
    this.resourceTags = resourceTags;
  }

  public String getFailureCause() {
    return this.failureCause;
  }

  public void setFailureCause(String failureCause) {
    this.failureCause = failureCause;
  }

  public boolean isAutoAssignSubgroups() {
    return autoAssignSubgroups;
  }

  public void setAutoAssignSubgroups(boolean autoAssignSubgroups) {
    this.autoAssignSubgroups = autoAssignSubgroups;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AssignedResource that = (AssignedResource) o;
    return Objects.equals(getEnrichedResource(), that.getEnrichedResource()) && getStatus() == that.getStatus()
        && Objects.equals(getSourceGroupId(), that.getSourceGroupId()) &&
        Objects.equals(getFacility(), that.getFacility())
        && Objects.equals(isAutoAssignSubgroups(), that.isAutoAssignSubgroups());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getEnrichedResource(), getStatus(), getSourceGroupId(), getFacility(), isAutoAssignSubgroups());
  }

  @Override
  public String toString() {
    return "AssignedResource{" +
        "enrichedResource=" + enrichedResource +
        ", status=" + status +
        ", sourceGroupId=" + sourceGroupId +
        ", failureCause=" + failureCause +
        ", facility=" + facility +
        ", resourceTags=" + resourceTags +
        ", autoAssignSubgroups=" + autoAssignSubgroups +
        '}';
  }
}
