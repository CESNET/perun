package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Class representing a query requesting a specific page of audit messages.
 *
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class MessagesPageQuery {
	private int pageSize;
	private int offset;
	private SortingOrder order;
	private List<String> selectedEvents;

	public MessagesPageQuery() { }
	public MessagesPageQuery(int pageSize, int offset, SortingOrder order, List<String> selectedEvents) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.order  = order;
		this.selectedEvents = selectedEvents;
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
		return this.order;
	}

	public void setOrder(SortingOrder order) {
		this.order = order;
	}

	public List<String> getSelectedEvents() {
		return this.selectedEvents;
	}

	public void setSelectedEvents(List<String>  selectedEvents) {
		this.selectedEvents = selectedEvents;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MessagesPageQuery that)) return false;

		return (getPageSize() == that.getPageSize())
			&& (getOffset() == that.getOffset())
			&& (getOrder() == that.getOrder())
			&& (Objects.equals(getSelectedEvents(), that.getSelectedEvents()));
	}

	@Override
	public int hashCode() {
		int result = getPageSize();
		result = 31 * result + getOffset();
		result = 31 * result + getOrder().hashCode();
		result = 31 * result + getSelectedEvents().hashCode();
		return result;
	}
}
