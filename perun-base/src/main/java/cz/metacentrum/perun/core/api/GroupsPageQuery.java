package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class representing a query requesting a specific page of groups.
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class GroupsPageQuery {
	private int pageSize;
	private int offset;
	private SortingOrder order;
	private GroupsOrderColumn sortColumn;
	private String searchString = "";
	private Integer memberId;
	private List<String> roles = new ArrayList<>();
	private List<RoleAssignmentType> types = new ArrayList<>();

	public GroupsPageQuery() {}

	public GroupsPageQuery(int pageSize, int offset, SortingOrder order, GroupsOrderColumn sortColumn) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.order = order;
		this.sortColumn = sortColumn;
	}

	public GroupsPageQuery(int pageSize, int offset, SortingOrder order, GroupsOrderColumn sortColumn, String searchString) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.order = order;
		this.sortColumn = sortColumn;
		this.searchString = searchString;
	}

	public GroupsPageQuery(int pageSize, int offset, SortingOrder order, GroupsOrderColumn sortColumn, String searchString, Integer memberId) {
		this(pageSize, offset, order, sortColumn, searchString);
		this.memberId = memberId;
	}

	public GroupsPageQuery(int pageSize, int offset, SortingOrder order, GroupsOrderColumn sortColumn, String searchString, Integer memberId, List<String> roles, List<RoleAssignmentType> types) {
		this(pageSize, offset, order, sortColumn, searchString, memberId);
		this.roles = roles;
		this.types = types;
	}

	public GroupsPageQuery(int pageSize, int offset, SortingOrder order, GroupsOrderColumn sortColumn, String searchString, List<String> roles, List<RoleAssignmentType> types) {
		this(pageSize, offset, order, sortColumn, searchString);
		this.roles = roles;
		this.types = types;
	}

	public GroupsPageQuery(int pageSize, int offset, SortingOrder order, GroupsOrderColumn sortColumn, List<String> roles, List<RoleAssignmentType> types) {
		this(pageSize, offset, order, sortColumn);
		this.roles = roles;
		this.types = types;
	}

	public GroupsPageQuery(int pageSize, int offset, SortingOrder order, GroupsOrderColumn sortColumn, Integer memberId, List<String> roles, List<RoleAssignmentType> types) {
		this(pageSize, offset, order, sortColumn);
		this.memberId = memberId;
		this.roles = roles;
		this.types = types;
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

	public SortingOrder getOrder() { return order; }

	public void setOrder(SortingOrder order) {
		this.order = order;
	}

	public GroupsOrderColumn getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(GroupsOrderColumn sortColumn) {
		this.sortColumn = sortColumn;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public List<RoleAssignmentType> getTypes() {
		return types;
	}

	public void setTypes(List<RoleAssignmentType> types) {
		this.types = types;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GroupsPageQuery)) return false;

		GroupsPageQuery that = (GroupsPageQuery) o;

		if (getPageSize() != that.getPageSize()) return false;
		if (getOffset() != that.getOffset()) return false;
		if (getOrder() != that.getOrder()) return false;
		if (getSortColumn() != that.getSortColumn()) return false;
		if (!Objects.equals(getMemberId(), that.getMemberId())) return false;
		if (getRoles() != that.getRoles()) return false;
		if (getTypes() != that.getTypes()) return false;
		return getSearchString().equals(that.getSearchString());
	}

	@Override
	public int hashCode() {
		int result = getPageSize();
		result = 31 * result + getOffset();
		result = 31 * result + (getOrder() != null ? getOrder().hashCode() : 0);
		result = 31 * result + (getSortColumn() != null ? getSortColumn().hashCode() : 0);
		result = 31 * result + (getSearchString() != null ? getSearchString().hashCode() : 0);
		result = 31 * result + (getMemberId() != null ? getMemberId().hashCode() : 0);
		result = 31 * result + (getRoles() != null ? getRoles().hashCode() : 0);
		result = 31 * result + (getTypes() != null ? getTypes().hashCode() : 0);
		return result;
	}
}
