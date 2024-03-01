package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Class representing a query requesting a specific page of members.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class MembersPageQuery {
  private int pageSize;
  private int offset;
  private SortingOrder order;
  private MembersOrderColumn sortColumn;
  private String searchString = "";
  private List<Status> statuses;
  private Integer groupId;
  private List<MemberGroupStatus> groupStatuses;

  public MembersPageQuery() {
  }

  public MembersPageQuery(int pageSize, int offset, SortingOrder sortingOrder, MembersOrderColumn sortColumn) {
    this.pageSize = pageSize;
    this.offset = offset;
    this.order = sortingOrder;
    this.sortColumn = sortColumn;
  }

  public MembersPageQuery(int pageSize, int offset, SortingOrder sortingOrder, MembersOrderColumn sortColumn,
                          String searchString) {
    this.pageSize = pageSize;
    this.offset = offset;
    this.order = sortingOrder;
    this.sortColumn = sortColumn;
    this.searchString = searchString;
  }

  public MembersPageQuery(int pageSize, int offset, SortingOrder sortingOrder, MembersOrderColumn sortColumn,
                          String searchString, List<Status> statuses) {
    this.pageSize = pageSize;
    this.offset = offset;
    this.order = sortingOrder;
    this.sortColumn = sortColumn;
    this.searchString = searchString;
    this.statuses = statuses;
  }

  public MembersPageQuery(int pageSize, int offset, SortingOrder sortingOrder, MembersOrderColumn sortColumn,
                          List<Status> statuses) {
    this.pageSize = pageSize;
    this.offset = offset;
    this.order = sortingOrder;
    this.sortColumn = sortColumn;
    this.statuses = statuses;
  }

  public MembersPageQuery(int pageSize, int offset, SortingOrder order, MembersOrderColumn sortColumn,
                          String searchString, List<Status> statuses, Integer groupId,
                          List<MemberGroupStatus> groupStatuses) {
    this.pageSize = pageSize;
    this.offset = offset;
    this.order = order;
    this.sortColumn = sortColumn;
    this.searchString = searchString;
    this.statuses = statuses;
    this.groupId = groupId;
    this.groupStatuses = groupStatuses;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public SortingOrder getOrder() {
    return order;
  }

  public void setOrder(SortingOrder order) {
    this.order = order;
  }

  public MembersOrderColumn getSortColumn() {
    return sortColumn;
  }

  public void setSortColumn(MembersOrderColumn sortColumn) {
    this.sortColumn = sortColumn;
  }

  public String getSearchString() {
    return searchString;
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString;
  }

  public List<Status> getStatuses() {
    return statuses;
  }

  public void setStatuses(List<Status> statuses) {
    this.statuses = statuses;
  }

  public Integer getGroupId() {
    return groupId;
  }

  public void setGroupId(Integer groupId) {
    this.groupId = groupId;
  }

  public List<MemberGroupStatus> getGroupStatuses() {
    return groupStatuses;
  }

  public void setGroupStatuses(List<MemberGroupStatus> groupStatuses) {
    this.groupStatuses = groupStatuses;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MembersPageQuery)) {
      return false;
    }

    MembersPageQuery that = (MembersPageQuery) o;

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
    if (!getSearchString().equals(that.getSearchString())) {
      return false;
    }
    if (!Objects.equals(getGroupId(), that.getGroupId())) {
      return false;
    }
    if (getGroupStatuses() != that.getGroupStatuses()) {
      return false;
    }
    return getStatuses() != that.getStatuses();
  }

  @Override
  public int hashCode() {
    int result = getPageSize();
    result = 31 * result + getOffset();
    result = 31 * result + (getOrder() != null ? getOrder().hashCode() : 0);
    result = 31 * result + (getSortColumn() != null ? getSortColumn().hashCode() : 0);
    result = 31 * result + (getSearchString() != null ? getSearchString().hashCode() : 0);
    result = 31 * result + (getStatuses() != null ? getStatuses().hashCode() : 0);
    result = 31 * result + (getGroupId() != null ? getGroupId().hashCode() : 0);
    result = 31 * result + (getGroupStatuses() != null ? getGroupStatuses().hashCode() : 0);
    return result;
  }
}
