package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Class representing a query requesting a specific page of users.
 *
 * @author Metodej Klang
 */
public class UsersPageQuery extends PageQuery {
  private UsersOrderColumn sortColumn;
  private String searchString = "";
  private boolean withoutVo = false;
  private Integer voId;
  private Integer resourceId;
  private Integer facilityId;
  private Integer serviceId;
  private boolean onlyAllowed = false;
  private List<ConsentStatus> consentStatuses;

  public UsersPageQuery() {
  }

  public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
  }

  public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn,
                        String searchString) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
  }

  public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn, boolean withoutVo) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.withoutVo = withoutVo;
  }

  public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn, String searchString,
                        boolean withoutVo) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
    this.withoutVo = withoutVo;
  }

  public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn, String searchString,
                        Integer facilityId) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
    this.facilityId = facilityId;
  }

  public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn, String searchString,
                        Integer facilityId, boolean onlyAllowed) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
    this.facilityId = facilityId;
    this.onlyAllowed = onlyAllowed;
  }

  public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn, String searchString,
                        Integer facilityId, Integer voId, Integer serviceId, Integer resourceId) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
    this.facilityId = facilityId;
    this.serviceId = serviceId;
    this.voId = voId;
    this.resourceId = resourceId;
  }

  public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn, String searchString,
                        Integer facilityId, Integer voId, Integer serviceId, Integer resourceId, boolean onlyAllowed) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
    this.facilityId = facilityId;
    this.serviceId = serviceId;
    this.voId = voId;
    this.resourceId = resourceId;
    this.onlyAllowed = onlyAllowed;
  }

  public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn, String searchString,
                        Integer facilityId, Integer voId, Integer serviceId, Integer resourceId, boolean onlyAllowed,
                        List<ConsentStatus> consentStatuses) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
    this.facilityId = facilityId;
    this.serviceId = serviceId;
    this.voId = voId;
    this.resourceId = resourceId;
    this.onlyAllowed = onlyAllowed;
    this.consentStatuses = consentStatuses;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    UsersPageQuery that = (UsersPageQuery) o;

    if (getPageSize() != that.getPageSize()) {
      return false;
    }
    if (getOffset() != that.getOffset()) {
      return false;
    }
    if (withoutVo != that.withoutVo) {
      return false;
    }
    if (onlyAllowed != that.onlyAllowed) {
      return false;
    }
    if (getOrder() != that.getOrder()) {
      return false;
    }
    if (sortColumn != that.sortColumn) {
      return false;
    }
    if (!Objects.equals(searchString, that.searchString)) {
      return false;
    }
    if (!Objects.equals(voId, that.voId)) {
      return false;
    }
    if (!Objects.equals(resourceId, that.resourceId)) {
      return false;
    }
    if (!Objects.equals(serviceId, that.serviceId)) {
      return false;
    }
    if (!Objects.equals(consentStatuses, that.consentStatuses)) {
      return false;
    }
    return Objects.equals(facilityId, that.facilityId);
  }

  public List<ConsentStatus> getConsentStatuses() {
    return consentStatuses;
  }

  public void setConsentStatuses(List<ConsentStatus> consentStatuses) {
    this.consentStatuses = consentStatuses;
  }

  public Integer getFacilityId() {
    return facilityId;
  }

  public void setFacilityId(Integer facilityId) {
    this.facilityId = facilityId;
  }

  public Integer getResourceId() {
    return resourceId;
  }

  public void setResourceId(Integer resourceId) {
    this.resourceId = resourceId;
  }

  public String getSearchString() {
    return searchString;
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString.trim();
  }

  public Integer getServiceId() {
    return serviceId;
  }

  public void setServiceId(Integer serviceId) {
    this.serviceId = serviceId;
  }

  public UsersOrderColumn getSortColumn() {
    return sortColumn;
  }

  public void setSortColumn(UsersOrderColumn sortColumn) {
    this.sortColumn = sortColumn;
  }

  public Integer getVoId() {
    return voId;
  }

  public void setVoId(Integer groupId) {
    this.voId = groupId;
  }

  @Override
  public int hashCode() {
    int result = getPageSize();
    result = 31 * result + getOffset();
    result = 31 * result + getOrder().hashCode();
    result = 31 * result + sortColumn.hashCode();
    result = 31 * result + (searchString != null ? searchString.hashCode() : 0);
    result = 31 * result + (withoutVo ? 1 : 0);
    result = 31 * result + (voId != null ? voId.hashCode() : 0);
    result = 31 * result + (resourceId != null ? resourceId.hashCode() : 0);
    result = 31 * result + (facilityId != null ? facilityId.hashCode() : 0);
    result = 31 * result + (serviceId != null ? serviceId.hashCode() : 0);
    result = 31 * result + (consentStatuses != null ? consentStatuses.hashCode() : 0);
    result = 31 * result + (onlyAllowed ? 1 : 0);
    return result;
  }

  public boolean isOnlyAllowed() {
    return onlyAllowed;
  }

  public void setOnlyAllowed(boolean onlyAllowed) {
    this.onlyAllowed = onlyAllowed;
  }

  public boolean isWithoutVo() {
    return withoutVo;
  }

  public void setWithoutVo(boolean withoutVo) {
    this.withoutVo = withoutVo;
  }
}
