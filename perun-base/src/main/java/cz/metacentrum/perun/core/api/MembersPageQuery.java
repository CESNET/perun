package cz.metacentrum.perun.core.api;

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

	public MembersPageQuery() { }
	public MembersPageQuery(int pageSize, int offset, SortingOrder sortingOrder, MembersOrderColumn sortColumn) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.order = sortingOrder;
		this.sortColumn = sortColumn;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MembersPageQuery)) return false;

		MembersPageQuery that = (MembersPageQuery) o;

		if (getPageSize() != that.getPageSize()) return false;
		if (getOffset() != that.getOffset()) return false;
		if (getOrder() != that.getOrder()) return false;
		return getSortColumn() == that.getSortColumn();
	}

	@Override
	public int hashCode() {
		int result = getPageSize();
		result = 31 * result + getOffset();
		result = 31 * result + (getOrder() != null ? getOrder().hashCode() : 0);
		result = 31 * result + (getSortColumn() != null ? getSortColumn().hashCode() : 0);
		return result;
	}
}
