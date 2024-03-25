package cz.metacentrum.perun.core.api;

import java.util.Objects;

/**
 * Represents group assigned to a resource.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class AssignedGroup {
  private EnrichedGroup enrichedGroup;
  private GroupResourceStatus status;
  private Integer sourceGroupId;
  private String failureCause;
  private boolean autoAssignSubgroups;

  public AssignedGroup(EnrichedGroup enrichedGroup, GroupResourceStatus status, Integer sourceGroupId,
                       String failureCause, boolean autoAssignSubgroups) {
    this.enrichedGroup = enrichedGroup;
    this.status = status;
    this.sourceGroupId = sourceGroupId;
    this.failureCause = failureCause;
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
    AssignedGroup that = (AssignedGroup) o;
    return Objects.equals(getEnrichedGroup(), that.getEnrichedGroup()) && getStatus() == that.getStatus() &&
           Objects.equals(getSourceGroupId(), that.getSourceGroupId()) &&
           Objects.equals(getFailureCause(), that.getFailureCause()) &&
           Objects.equals(isAutoAssignSubgroups(), that.isAutoAssignSubgroups());
  }

  public EnrichedGroup getEnrichedGroup() {
    return enrichedGroup;
  }

  public void setEnrichedGroup(EnrichedGroup enrichedGroup) {
    this.enrichedGroup = enrichedGroup;
  }

  public String getFailureCause() {
    return failureCause;
  }

  public void setFailureCause(String failureCause) {
    this.failureCause = failureCause;
  }

  public Integer getSourceGroupId() {
    return sourceGroupId;
  }

  public void setSourceGroupId(Integer sourceGroupId) {
    this.sourceGroupId = sourceGroupId;
  }

  public GroupResourceStatus getStatus() {
    return status;
  }

  public void setStatus(GroupResourceStatus status) {
    this.status = status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getEnrichedGroup(), getStatus(), getSourceGroupId(), getFailureCause(),
        isAutoAssignSubgroups());
  }

  public boolean isAutoAssignSubgroups() {
    return autoAssignSubgroups;
  }

  public void setAutoAssignSubgroups(boolean autoAssignSubgroups) {
    this.autoAssignSubgroups = autoAssignSubgroups;
  }

  @Override
  public String toString() {
    return "AssignedGroup{" + "enrichedGroup=" + enrichedGroup + ", status=" + status + ", sourceGroupId=" +
           sourceGroupId + ", failureCause=" + failureCause + ", autoAssignSubgroups=" + autoAssignSubgroups + '}';
  }
}
