package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.AbstractCellTable;

import java.util.ArrayList;

/**
 * Provides methods for handling tables from JsonCallbacks
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @param <T> type of javascript objects in table
 */

public interface JsonCallbackTable <T extends JavaScriptObject> extends JsonCallback {

	/**
	 * Clears all table content
	 * Reset all selections
	 *
	 */
	void clearTable();

	/**
	 * Inserts object to table on specified row index
	 *
	 * @param index new position of object
	 * @param object object to be inserted
	 */
	void insertToTable(int index, T object);

	/**
	 * Add object to table to the end of list
	 *
	 * @param object object to be added
	 */
	void addToTable(T object);

	/**
	 * Removes specified object from list
	 * Comparison is based on object ID
	 *
	 * @param object object to be removed
	 */
	void removeFromTable(T object);

	/**
	 * Sets table columns as editable - uses TextBox instead of text itself
	 * After this, table must be redrawn by method - redrawTable()
	 *
	 * @param editable true for showing TextBoxs instead of text, false otherwise
	 */
	void setEditable(boolean editable);

	/**
	 * Displays checkboxes in table for selecting one or more rows
	 * After this, table must be redrawn by method - redrawTable()
	 *
	 * @param checkable true to show checkboxes, false otherwise
	 */
	void setCheckable(boolean checkable);

	/**
	 * Clears 'selected' option for all objects
	 */
	void clearTableSelectedSet();

	/**
	 * Return list of selected objects from table
	 *
	 * @return list of selected objects
	 */
	ArrayList<T> getTableSelectedList();

	/**
	 * Sets new list of objects to table for showing
	 *
	 * @param list list of objects
	 */
	void setList(ArrayList<T> list);

	/**
	 * Return list of all objects in table
	 *
	 * @return list of objects from table
	 */
	ArrayList<T> getList();

	/**
	 * Returns table widget definition and do all necessary setup
	 * (connecting data provider etc.)
	 *
	 * @return abstract table widget (we can use any table - CellTable, DataGrid etc.)
	 */
	AbstractCellTable<T> getTable();

}
