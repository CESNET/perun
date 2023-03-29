package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Class representing a query requesting a specific page of blocked logins.
 *
 * @author Adam Bodnar
 */
public class BlockedLoginsPageQuery {
	private int pageSize;
	private int offset;
	private SortingOrder order;
	private BlockedLoginsOrderColumn sortColumn;
	private String searchString = "";
	private List<String> namespaces;

	public BlockedLoginsPageQuery() {}

	public BlockedLoginsPageQuery(int pageSize, int offset, SortingOrder order, BlockedLoginsOrderColumn sortColumn) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.order = order;
		this.sortColumn = sortColumn;
	}
	public BlockedLoginsPageQuery(int pageSize, int offset, SortingOrder order, BlockedLoginsOrderColumn sortColumn, List<String> namespaces) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.order = order;
		this.sortColumn = sortColumn;
		this.namespaces = namespaces;
	}

	public BlockedLoginsPageQuery(int pageSize, int offset, SortingOrder order, BlockedLoginsOrderColumn sortColumn, String searchString) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.order = order;
		this.sortColumn = sortColumn;
		this.searchString = searchString;
	}

	public BlockedLoginsPageQuery(int pageSize, int offset, SortingOrder order, BlockedLoginsOrderColumn sortColumn, String searchString, List<String> namespaces) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.order = order;
		this.sortColumn = sortColumn;
		this.searchString = searchString;
		this.namespaces = namespaces;
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

	public BlockedLoginsOrderColumn getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(BlockedLoginsOrderColumn sortColumn) {
		this.sortColumn = sortColumn;
	}

	public List<String> getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(List<String> namespaces) {
		this.namespaces = namespaces;
	}

	public SortingOrder getOrder() {
		return order;
	}

	public void setOrder(SortingOrder order) {
		this.order = order;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BlockedLoginsPageQuery that = (BlockedLoginsPageQuery) o;

		if (pageSize != that.pageSize) return false;
		if (offset != that.offset) return false;
		if (order != that.order) return false;
		if (sortColumn != that.sortColumn) return false;
		return Objects.equals(searchString, that.searchString);
	}

	@Override
	public int hashCode() {
		int result = pageSize;
		result = 31 * result + offset;
		result = 31 * result + order.hashCode();
		result = 31 * result + sortColumn.hashCode();
		result = 31 * result + (searchString != null ? searchString.hashCode() : 0);
		return result;
	}
}

