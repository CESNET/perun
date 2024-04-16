package cz.metacentrum.perun.core.api;

import java.util.Objects;

public class PageQuery {
  private int pageSize;
  private int offset;
  private SortingOrder order;

  public PageQuery() {
  }

  public PageQuery(int pageSize, int offset, SortingOrder order) {
    this.pageSize = pageSize;
    this.offset = offset;
    this.order = order;
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

  /**
   * This method recalculates sets the correct offset if the offset requested from the client is bigger than the total
   * count of the found entities. In this case we will want to change the offset to display the last found page
   * (considering the given page size).
   *
   * @param filteredCount total count of filtered entities
   */
  public void recalculateOffset(Integer filteredCount) {
    if (filteredCount == null || filteredCount == 0) {
      offset = 0;
    } else if (filteredCount <= offset) {
      int totalPages = (int) Math.ceil((double) filteredCount / pageSize);
      offset = (totalPages - 1) * pageSize;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PageQuery pageQuery = (PageQuery) o;
    return pageSize == pageQuery.pageSize && offset == pageQuery.offset && order == pageQuery.order;
  }

  @Override
  public int hashCode() {
    return Objects.hash(pageSize, offset, order);
  }
}
