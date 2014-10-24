package cz.metacentrum.perun.webgui.json;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;

/**
 * Virtual source data provider.
 * Provides the functionality of RPC for SimplePager.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class JsonRpcSourceData implements HasRows {

	/**
	 * Default page size
	 */
	static private int defaultPageSize = 15;


	/**
	 * Number of all items
	 */
	private int rowCount;

	/**
	 * Currently visible range (number, number) - not (page, number).
	 */
	private Range visibleRange;

	/**
	 * Handler for events
	 */
	private HandlerManager handlerManager = new HandlerManager(this);

	/**
	 * The callback for data retrieving
	 */
	private JsonCallbackWithPages jsonCallback;

	private Request request = null;


	private boolean rowCountIsExact = true;

	/**
	 * New instance of the data provider
	 * @param jsonCallback The RPC request itself
	 */
	public JsonRpcSourceData(JsonCallbackWithPages jsonCallback) {
		this.jsonCallback = jsonCallback;
		this.rowCount = defaultPageSize;
		this.setVisibleRange(new Range(0, defaultPageSize));
	}

	/**
	 * Fires an event.
	 */
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}

	/**
	 * Registers a handler, which is fired when range changed
	 */
	public HandlerRegistration addRangeChangeHandler(
			RangeChangeEvent.Handler handler) {
		return handlerManager.addHandler(RangeChangeEvent.getType(), handler);
			}

	/**
	 * Registers a handler, which is fired when row count changed
	 */
	public HandlerRegistration addRowCountChangeHandler(
			RowCountChangeEvent.Handler handler) {
		return handlerManager.addHandler(RowCountChangeEvent.getType(), handler);
			}

	/**
	 * Returns a row count
	 */
	public int getRowCount() {
		return this.rowCount;
	}

	/**
	 * Returns the visible range
	 * @return range object
	 */
	public Range getVisibleRange() {
		return visibleRange;
	}

	/**
	 * Is the number exact?
	 */
	public boolean isRowCountExact() {
		return this.rowCountIsExact;
	}

	/**
	 * Sets the row count
	 *
	 * @param count the row count
	 */
	public void setRowCount(int count) {
		this.setRowCount(count, isRowCountExact());
	}

	/**
	 * Sets the row count
	 *
	 * @param count the row count
	 * @param isExact whether the value is exact
	 */
	public void setRowCount(int count, boolean isExact) {
		this.rowCountIsExact = isExact;
		if(this.rowCount != count){
			this.rowCount = count;
			RowCountChangeEvent.fire(this, getRowCount(), isExact);
		}
	}

	/**
	 * Sets the visible range
	 *
	 * @param start Number of a row, not page
	 * @param length
	 */
	public void setVisibleRange(int start, int length) {
		this.setVisibleRange(new Range(start, length));
	}

	/**
	 * Sets the visible range
	 *
	 * @param range Range object
	 */
	public void setVisibleRange(Range range) {

		// if not changed
		if(this.visibleRange != null){
			if(this.visibleRange.equals(range)){
				return;
			}
		}

		// clears the callback
		if (request != null) this.request.cancel();

		// sets the new range
		this.visibleRange = range;

		// the page
		int page = range.getStart() / range.getLength();

		//  reloads new data
		jsonCallback.clearTable();
		this.request = jsonCallback.retrieveData(range.getLength(), page);

		// triggers the change
		RangeChangeEvent.fire(this, getVisibleRange());
	}

	/**
	 * Returns the global default page size
	 * @return
	 */
	static public int getDefaultPageSize(){
		return defaultPageSize;
	}

	/**
	 * Sets the global default page size.
	 * @param pageSize New page size
	 */
	static public void setDefaultPageSize(int pageSize){
		defaultPageSize = pageSize;
	}

}
