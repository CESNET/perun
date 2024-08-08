package cz.metacentrum.perun.registrar.model;

import cz.metacentrum.perun.core.api.PageQuery;
import cz.metacentrum.perun.core.api.SortingOrder;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Class representing a query requesting a specific page of invitations.
 *
 * @author Sarka Palkovicova <492687@mail.muni.cz>
 */
public class InvitationsPageQuery extends PageQuery {
  private InvitationsOrderColumn sortColumn;
  private String searchString = "";
  private List<InvitationStatus> statuses;
  private LocalDate expirationFrom;
  private LocalDate expirationTo;

  public InvitationsPageQuery() {
  }

  public InvitationsPageQuery(int pageSize, int offset, SortingOrder order, InvitationsOrderColumn sortColumn) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
  }

  public InvitationsPageQuery(int pageSize, int offset, SortingOrder order, InvitationsOrderColumn sortColumn,
                              String searchString) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString;
  }

  public InvitationsPageQuery(int pageSize, int offset, SortingOrder order, InvitationsOrderColumn sortColumn,
                              List<InvitationStatus> statuses) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.statuses = statuses;
  }

  public InvitationsPageQuery(int pageSize, int offset, SortingOrder order, InvitationsOrderColumn sortColumn,
                              LocalDate expirationFrom, LocalDate expirationTo) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.expirationFrom = expirationFrom;
    this.expirationTo = expirationTo;
  }

  public InvitationsPageQuery(int pageSize, int offset, SortingOrder order, InvitationsOrderColumn sortColumn,
                              String searchString, List<InvitationStatus> statuses) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString;
    this.statuses = statuses;
  }

  public InvitationsPageQuery(int pageSize, int offset, SortingOrder order, InvitationsOrderColumn sortColumn,
                              String searchString, LocalDate expirationFrom, LocalDate expirationTo) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString;
    this.expirationFrom = expirationFrom;
    this.expirationTo = expirationTo;
  }

  public InvitationsPageQuery(int pageSize, int offset, SortingOrder order, InvitationsOrderColumn sortColumn,
                              List<InvitationStatus> statuses, LocalDate expirationFrom,
                              LocalDate expirationTo) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.statuses = statuses;
    this.expirationFrom = expirationFrom;
    this.expirationTo = expirationTo;
  }

  public InvitationsPageQuery(int pageSize, int offset, SortingOrder order, InvitationsOrderColumn sortColumn,
                              String searchString, List<InvitationStatus> statuses,
                              LocalDate expirationFrom, LocalDate expirationTo) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString;
    this.statuses = statuses;
    this.expirationFrom = expirationFrom;
    this.expirationTo = expirationTo;
  }

  public InvitationsOrderColumn getSortColumn() {
    return sortColumn;
  }

  public void setSortColumn(InvitationsOrderColumn sortColumn) {
    this.sortColumn = sortColumn;
  }

  public String getSearchString() {
    return searchString;
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString;
  }

  public List<InvitationStatus> getStatuses() {
    return statuses;
  }

  public void setStatuses(List<InvitationStatus> statuses) {
    this.statuses = statuses;
  }

  public LocalDate getExpirationFrom() {
    return expirationFrom;
  }

  public void setExpirationFrom(LocalDate expirationFrom) {
    this.expirationFrom = expirationFrom;
  }

  public LocalDate getExpirationTo() {
    return expirationTo;
  }

  public void setExpirationTo(LocalDate expirationTo) {
    this.expirationTo = expirationTo;
  }

  @Override
  public int hashCode() {
    int result = getPageSize();
    result = 31 * result + getOffset();
    result = 31 * result + (getOrder() != null ? getOrder().hashCode() : 0);
    result = 31 * result + (getSortColumn() != null ? getSortColumn().hashCode() : 0);
    result = 31 * result + (getSearchString() != null ? getSearchString().hashCode() : 0);
    result = 31 * result + (getStatuses() != null ? getStatuses().hashCode() : 0);
    result = 31 * result + (getExpirationFrom() != null ? getExpirationFrom().hashCode() : 0);
    result = 31 * result + (getExpirationTo() != null ? getExpirationTo().hashCode() : 0);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InvitationsPageQuery that)) {
      return false;
    }
    return getPageSize() == that.getPageSize() && getOffset() == that.getOffset() && getOrder() == that.getOrder() &&
               sortColumn == that.sortColumn &&
               Objects.equals(searchString, that.searchString) &&
               Objects.equals(statuses, that.statuses) && Objects.equals(expirationFrom, that.expirationFrom) &&
               Objects.equals(expirationTo, that.expirationTo);
  }

  @Override
  public String toString() {
    return "InvitationsPageQuery:[" +
               "pageSize='" + getPageSize() +
               "', offset='" + getOffset() +
               "', order='" + getOrder() +
               "', sortColumn='" + sortColumn +
               "', searchString='" + searchString +
               "', statuses='" + statuses +
               "', expirationFrom='" + expirationFrom +
               "', expirationTo='" + expirationTo +
               "']";
  }
}
