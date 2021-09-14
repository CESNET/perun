package cz.metacentrum.perun.core.api;

import java.util.Objects;

/**
 * Class representing a query requesting a specific page of users.
 *
 * @author Metodej Klang
 */
public class UsersPageQuery {
	private int pageSize;
	private int offset;
	private SortingOrder order;
	private UsersOrderColumn sortColumn;
	private String searchString = "";
	private boolean withoutVo = false;

	public UsersPageQuery() {}

	public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.order = order;
		this.sortColumn = sortColumn;
	}

	public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn, String searchString) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.order = order;
		this.sortColumn = sortColumn;
		this.searchString = searchString;
	}

	public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn, boolean withoutVo) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.order = order;
		this.sortColumn = sortColumn;
		this.withoutVo = withoutVo;
	}

	public UsersPageQuery(int pageSize, int offset, SortingOrder order, UsersOrderColumn sortColumn, String searchString, boolean withoutVo) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.order = order;
		this.sortColumn = sortColumn;
		this.searchString = searchString;
		this.withoutVo = withoutVo;
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

	public UsersOrderColumn getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(UsersOrderColumn sortColumn) {
		this.sortColumn = sortColumn;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public boolean isWithoutVo() {
		return withoutVo;
	}

	public void setWithoutVo(boolean withoutVo) {
		this.withoutVo = withoutVo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UsersPageQuery that = (UsersPageQuery) o;

		if (pageSize != that.pageSize) return false;
		if (offset != that.offset) return false;
		if (withoutVo != that.withoutVo) return false;
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
		result = 31 * result + (withoutVo ? 1 : 0);
		return result;
	}
}
