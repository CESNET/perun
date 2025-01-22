package cz.metacentrum.perun.registrar.model;

import cz.metacentrum.perun.core.api.PageQuery;
import cz.metacentrum.perun.core.api.SortingOrder;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Class representing a query requesting a specific page of applications.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class ApplicationsPageQuery extends PageQuery {
  private ApplicationsOrderColumn sortColumn;
  private Boolean includeGroupApplications = true;
  private Boolean getDetails = false;
  private String searchString = "";
  private List<Application.AppState> states;
  private LocalDate dateFrom = LocalDate.now().minusYears(1);
  private LocalDate dateTo = LocalDate.now();
  private Integer userId;
  private Integer groupId;

  public ApplicationsPageQuery() {
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               Boolean includeGroupApplications) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.includeGroupApplications = includeGroupApplications;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               List<Application.AppState> states, Boolean includeGroupApplications) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.includeGroupApplications = includeGroupApplications;
    this.states = states;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               String searchString, List<Application.AppState> states,
                               Boolean includeGroupApplications) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.includeGroupApplications = includeGroupApplications;
    this.searchString = searchString.trim();
    this.states = states;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               String searchString, List<Application.AppState> states, LocalDate dateFrom,
                               LocalDate dateTo, Boolean includeGroupApplications) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.includeGroupApplications = includeGroupApplications;
    this.searchString = searchString.trim();
    this.states = states;
    this.dateFrom = dateFrom;
    this.dateTo = dateTo;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               List<Application.AppState> states) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.states = states;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               List<Application.AppState> states, Integer groupId) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.states = states;
    this.groupId = groupId;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               List<Application.AppState> states, Integer userId, Integer groupId) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.states = states;
    this.userId = userId;
    this.groupId = groupId;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               List<Application.AppState> states, LocalDate dateFrom, LocalDate dateTo,
                               Integer groupId) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.states = states;
    this.dateFrom = dateFrom;
    this.dateTo = dateTo;
    this.groupId = groupId;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               String searchString, List<Application.AppState> states, Integer userId,
                               Integer groupId) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
    this.states = states;
    this.userId = userId;
    this.groupId = groupId;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               List<Application.AppState> states, LocalDate dateFrom, LocalDate dateTo, Integer userId,
                               Integer groupId) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.states = states;
    this.dateFrom = dateFrom;
    this.dateTo = dateTo;
    this.userId = userId;
    this.groupId = groupId;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               String searchString, List<Application.AppState> states, LocalDate dateFrom,
                               LocalDate dateTo, Integer groupId) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
    this.states = states;
    this.dateFrom = dateFrom;
    this.dateTo = dateTo;
    this.groupId = groupId;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               String searchString, List<Application.AppState> states, Integer groupId) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
    this.states = states;
    this.groupId = groupId;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               String searchString, List<Application.AppState> states, LocalDate dateFrom,
                               LocalDate dateTo, Integer userId, Integer groupId) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
    this.states = states;
    this.dateFrom = dateFrom;
    this.dateTo = dateTo;
    this.userId = userId;
    this.groupId = groupId;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               String searchString, List<Application.AppState> states) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
    this.states = states;
  }

  public ApplicationsPageQuery(int pageSize, int offset, SortingOrder order, ApplicationsOrderColumn sortColumn,
                               String searchString, List<Application.AppState> states, LocalDate dateFrom,
                               LocalDate dateTo) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString.trim();
    this.states = states;
    this.dateFrom = dateFrom;
    this.dateTo = dateTo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ApplicationsPageQuery)) {
      return false;
    }

    ApplicationsPageQuery that = (ApplicationsPageQuery) o;


    if (!Objects.equals(getUserId(), that.getUserId())) {
      return false;
    }
    if (!Objects.equals(getGroupId(), that.getGroupId())) {
      return false;
    }
    if (getPageSize() != that.getPageSize()) {
      return false;
    }
    if (getOffset() != that.getOffset()) {
      return false;
    }
    if (getOrder() != that.getOrder()) {
      return false;
    }
    if (getSortColumn() != that.getSortColumn()) {
      return false;
    }
    if (getIncludeGroupApplications() != that.getIncludeGroupApplications()) {
      return false;
    }
    if (getGetDetails() != that.getGetDetails()) {
      return false;
    }
    if (!getSearchString().equals(that.getSearchString())) {
      return false;
    }
    if (getStates() != that.getStates()) {
      return false;
    }
    if (getDateFrom() != that.getDateFrom()) {
      return false;
    }
    return getDateTo() == that.getDateTo();
  }

  public LocalDate getDateFrom() {
    return dateFrom;
  }

  public void setDateFrom(LocalDate dateFrom) {
    this.dateFrom = dateFrom;
  }

  public LocalDate getDateTo() {
    return dateTo;
  }

  public void setDateTo(LocalDate dateTo) {
    this.dateTo = dateTo;
  }

  public Boolean getGetDetails() {
    return getDetails;
  }

  public void setGetDetails(Boolean getDetails) {
    this.getDetails = getDetails;
  }

  public Integer getGroupId() {
    return groupId;
  }

  public void setGroupId(Integer groupId) {
    this.groupId = groupId;
  }

  public Boolean getIncludeGroupApplications() {
    return includeGroupApplications;
  }

  public void setIncludeGroupApplications(Boolean includeGroupApplications) {
    this.includeGroupApplications = includeGroupApplications;
  }

  public String getSearchString() {
    return searchString;
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString.trim();
  }

  public ApplicationsOrderColumn getSortColumn() {
    return sortColumn;
  }

  public void setSortColumn(ApplicationsOrderColumn sortColumn) {
    this.sortColumn = sortColumn;
  }

  public List<Application.AppState> getStates() {
    return states;
  }

  public void setStates(List<Application.AppState> states) {
    this.states = states;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  @Override
  public int hashCode() {
    int result = getPageSize();
    result = 31 * result + getOffset();
    result = 31 * result + (getOrder() != null ? getOrder().hashCode() : 0);
    result = 31 * result + (getSortColumn() != null ? getSortColumn().hashCode() : 0);
    result = 31 * result + (getIncludeGroupApplications() != null ? getIncludeGroupApplications().hashCode() : 0);
    result = 31 * result + (getGetDetails() != null ? getGetDetails().hashCode() : 0);
    result = 31 * result + (getSearchString() != null ? getSearchString().hashCode() : 0);
    result = 31 * result + (getStates() != null ? getStates().hashCode() : 0);
    result = 31 * result + (getUserId() != null ? getUserId().hashCode() : 0);
    result = 31 * result + (getGroupId() != null ? getGroupId().hashCode() : 0);
    result = 31 * result + (getDateFrom() != null ? getDateFrom().hashCode() : 0);
    result = 31 * result + (getDateTo() != null ? getDateTo().hashCode() : 0);
    return result;
  }
}
