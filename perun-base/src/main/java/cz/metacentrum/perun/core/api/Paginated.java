package cz.metacentrum.perun.core.api;

import java.util.List;

/**
 * This class represents paginated data.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class Paginated<T> {

  private List<T> data;
  private int offset;
  private int pageSize;
  private int totalCount;

  public Paginated(List<T> data, int offset, int pageSize, int totalCount) {
    this.data = data;
    this.offset = offset;
    this.pageSize = pageSize;
    this.totalCount = totalCount;
  }

  public List<T> getData() {
    return data;
  }

  public void setData(List<T> data) {
    this.data = data;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Paginated)) {
      return false;
    }

    Paginated<?> paginated = (Paginated<?>) o;

    if (getOffset() != paginated.getOffset()) {
      return false;
    }
    if (getPageSize() != paginated.getPageSize()) {
      return false;
    }
    if (getTotalCount() != paginated.getTotalCount()) {
      return false;
    }
    return getData() != null ? getData().equals(paginated.getData()) : paginated.getData() == null;
  }

  @Override
  public int hashCode() {
    int result = getData() != null ? getData().hashCode() : 0;
    result = 31 * result + getOffset();
    result = 31 * result + getPageSize();
    result = 31 * result + getTotalCount();
    return result;
  }
}
