package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Class representing a query requesting a specific page of blocked logins.
 *
 * @author Adam Bodnar
 */
public class BlockedLoginsPageQuery extends PageQuery {
  private BlockedLoginsOrderColumn sortColumn;
  private String searchString = "";
  private List<String> namespaces;

  public BlockedLoginsPageQuery() {
  }

  public BlockedLoginsPageQuery(int pageSize, int offset, SortingOrder order, BlockedLoginsOrderColumn sortColumn) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
  }

  public BlockedLoginsPageQuery(int pageSize, int offset, SortingOrder order, BlockedLoginsOrderColumn sortColumn,
                                List<String> namespaces) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.namespaces = namespaces;
  }

  public BlockedLoginsPageQuery(int pageSize, int offset, SortingOrder order, BlockedLoginsOrderColumn sortColumn,
                                String searchString) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString;
  }

  public BlockedLoginsPageQuery(int pageSize, int offset, SortingOrder order, BlockedLoginsOrderColumn sortColumn,
                                String searchString, List<String> namespaces) {
    super(pageSize, offset, order);
    this.sortColumn = sortColumn;
    this.searchString = searchString;
    this.namespaces = namespaces;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BlockedLoginsPageQuery that = (BlockedLoginsPageQuery) o;

    if (getPageSize() != that.getPageSize()) {
      return false;
    }
    if (getOffset() != that.getOffset()) {
      return false;
    }
    if (getOrder() != that.getOrder()) {
      return false;
    }
    if (sortColumn != that.sortColumn) {
      return false;
    }
    return Objects.equals(searchString, that.searchString);
  }

  public List<String> getNamespaces() {
    return namespaces;
  }

  public void setNamespaces(List<String> namespaces) {
    this.namespaces = namespaces;
  }

  public String getSearchString() {
    return searchString;
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString;
  }

  public BlockedLoginsOrderColumn getSortColumn() {
    return sortColumn;
  }

  public void setSortColumn(BlockedLoginsOrderColumn sortColumn) {
    this.sortColumn = sortColumn;
  }

  @Override
  public int hashCode() {
    int result = getPageSize();
    result = 31 * result + getOffset();
    result = 31 * result + getOrder().hashCode();
    result = 31 * result + sortColumn.hashCode();
    result = 31 * result + (searchString != null ? searchString.hashCode() : 0);
    return result;
  }
}

