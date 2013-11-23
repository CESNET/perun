package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.widgets.cells.HyperlinkCell;

import java.util.ArrayList;

/**
 * Custom implementation of the CellTable
 * Contains some of the prepared columns
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id: a0e8489e10c074eae0d9a8f9f57426109fed0e98 $
 */
public class PerunTable<T extends JavaScriptObject> extends CellTable<T>
{
	/**
	 * The list table uses
	 */
	private ArrayList<T> list;

	/**
	 * Column sort handler
	 */
	private ListHandler<T> columnSortHandler;

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Whether automatically show hyperlinks for base entities
	 */
	private boolean hyperlinksAllowed = true;
	
	/**
	 * RowCountChangeHandler registration
	 */
	private HandlerRegistration rowCountChangeHandler;

    /**
     * Widget containing info about rows
     */
    private HTML rowCountWidget = new HTML();

    /**
	 * Creates a new cell table, in which you can use prepared columns
	 *
     * @param list Table source list
     *
     */
	public PerunTable(ArrayList<T> list) {
		super();
		this.list = list;
		this.setVisibleRange(0, 1000);

		final PerunTable<T> table = this;

        rowCountWidget.addStyleName("GPBYFDEFD");
        rowCountWidget.getElement().getStyle().setPosition(Position.ABSOLUTE);
        rowCountWidget.getElement().getStyle().setTop(0, Unit.PX);
        rowCountWidget.getElement().getStyle().setRight(0, Unit.PX);
        rowCountWidget.getElement().getStyle().setBackgroundColor("#fff");
        rowCountWidget.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        rowCountWidget.getElement().getStyle().setBorderWidth(0, Unit.PX);
        table.getElement().appendChild(rowCountWidget.getElement());

		// when row count changes, appends the row count
		rowCountChangeHandler = this.addRowCountChangeHandler(new RowCountChangeEvent.Handler() {
			public void onRowCountChange(RowCountChangeEvent event) {
                // change content of row count handler widget
                rowCountWidget.setHTML("Count: " + String.valueOf(event.getNewRowCount()));
			}
		});

	}

	/**
	 * Whether are hyperlinks allowed
	 * Default: true
	 * 
	 * @return
	 */
	public boolean isHyperlinksAllowed() {
		return hyperlinksAllowed;
	}

	/**
	 * Whether to allow hyperlinks in the table
	 * Default: true
	 * 
	 * @param hyperlinksAllowed
	 */
	public void setHyperlinksAllowed(boolean hyperlinksAllowed) {
		this.hyperlinksAllowed = hyperlinksAllowed;
	}

	/**
	 * Adds the checkbox column to the table
	 */
	public void addCheckBoxColumn()
	{
		final SelectionModel<? super T> selectionModel = this.getSelectionModel();

		Column<T, Boolean> checkBoxColumn = new Column<T, Boolean>(
				new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(T object) {
				// Get the value from the selection model.
				return selectionModel.isSelected(object);
			}
		};


		// Checkbox column header
		CheckboxCell cb = new CheckboxCell();
		Header<Boolean> checkBoxHeader = new Header<Boolean>(cb) {
			public Boolean getValue() {
				return false;//return true to see a checked checkbox.
			}
		};
		checkBoxHeader.setUpdater(new ValueUpdater<Boolean>() {	
			public void update(Boolean value) {
				// sets selected to all, if value = true, unselect otherwise
				for(T obj : list){
					selectionModel.setSelected(obj, value);
				}
			}
		});

		this.insertColumn(0, checkBoxColumn, checkBoxHeader);
		this.setColumnWidth(checkBoxColumn, 60.0, Unit.PX);

	}

	/**
	 * Adds the default ID column to the table
	 * The default width is 150px
	 * 
	 * @param title Use the element name width ID: VO ID, Attribute ID, ...
	 */
	public void addIdColumn(String title)
	{
		this.addIdColumn(title, null, 150);
	}

	/**
	 * Adds the default ID column to the table
	 * The default width is 150px
	 * 
	 * @param title Use the element name width ID: VO ID, Attribute ID, ...
	 * @param tableFieldUpdater
	 */
	public void addIdColumn(String title, FieldUpdater<T, String> tableFieldUpdater)
	{
		this.addIdColumn(title, tableFieldUpdater, 150);
	}

	/**
	 * Adds the default ID column to the table
	 * 
	 * @param title Use the element name width ID: VO ID, Attribute ID, ...
	 * @param tableFieldUpdater
	 * @param width Width of the column in PX
	 */
	public void addIdColumn(String title, FieldUpdater<T, String> tableFieldUpdater, int width)
	{
		// if not to show
		if(!JsonUtils.isExtendedInfoVisible()){
			return;
		}

		/* TEXT COLUMN */

		if(tableFieldUpdater == null)
		{
			TextColumn<T> idColumn = new TextColumn<T>() {
				@Override
				public String getValue(T obj) {
					GeneralObject go = obj.cast();
					return String.valueOf(go.getId());
				}
			};

			// sort type column
			idColumn.setSortable(true);
			this.columnSortHandler.setComparator(idColumn, new GeneralComparator<T>(GeneralComparator.Column.ID));

			// adding columns
			this.addColumn(idColumn, title);

			// custom width
			this.setColumnWidth(idColumn, width, Unit.PX);

			return;
		}

		/* CLICKABLE */
		if(hyperlinksAllowed)
		{
			Column<T, T> idColumn = JsonUtils.addCustomCellColumn(new HyperlinkCell<T>("id"), tableFieldUpdater);

			idColumn.setSortable(true);

			// adding columns
			this.addColumn(idColumn, title);


			// comparator
			this.columnSortHandler.setComparator(idColumn, new GeneralComparator<T>(GeneralComparator.Column.ID));

			// custom width
			this.setColumnWidth(idColumn, width, Unit.PX);

			return;
		}

		// hyperlinks not allowed
		Column<T, String> idColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<T, String>() {
					public String getValue(T object) {
						GeneralObject go = object.cast();
						return String.valueOf(go.getId());
					}
				}, tableFieldUpdater);

		idColumn.setSortable(true);

		// adding columns
		this.addColumn(idColumn, title);

		// comparator
		this.columnSortHandler.setComparator(idColumn, new GeneralComparator<T>(GeneralComparator.Column.ID));

		// custom width
		this.setColumnWidth(idColumn, width, Unit.PX);
	}

	/**
	 * Adds the default NAME column to the table
	 * 
	 * @param tableFieldUpdater
	 */
	public void addNameColumn(FieldUpdater<T, String> tableFieldUpdater)
	{
		this.addNameColumn(tableFieldUpdater, 0);
	}

	/**
	 * Adds the default NAME column to the table
	 * 
	 * @param tableFieldUpdater
	 * @param width Column width in pixels
	 */
	public void addNameColumn(FieldUpdater<T, String> tableFieldUpdater, int width)
	{
		/* TEXT COLUMN */

		if(tableFieldUpdater == null)
		{
			TextColumn<T> nameColumn = new TextColumn<T>() {
				@Override
				public String getValue(T obj) {
					GeneralObject go = obj.cast();
					return go.getName();
				}
			};

			// sort type column
			nameColumn.setSortable(true);
			this.columnSortHandler.setComparator(nameColumn, new GeneralComparator<T>(GeneralComparator.Column.NAME));

			// adding columns
			this.addColumn(nameColumn, "Name");

			// width
			if(width != 0){
				this.setColumnWidth(nameColumn, width, Unit.PX);			
			}
			return;
		}

		/* CLICKABLE */

		// Create name column.
		if(hyperlinksAllowed)
		{
			Column<T, T> nameColumn = JsonUtils.addCustomCellColumn(new HyperlinkCell<T>("name"), tableFieldUpdater);

			nameColumn.setSortable(true);

			// adding columns
			this.addColumn(nameColumn, "Name");

			// comparator
			this.columnSortHandler.setComparator(nameColumn, new GeneralComparator<T>(GeneralComparator.Column.NAME));

			// width
			if(width != 0){
				this.setColumnWidth(nameColumn, width, Unit.PX);			
			}

			return;
		}

		// hyperlinks not allowed
		Column<T, String> nameColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<T, String>() {
					public String getValue(T object) {
						GeneralObject go = object.cast();
						return go.getName();
					}
				}, tableFieldUpdater);


		nameColumn.setSortable(true);

		// adding columns
		this.addColumn(nameColumn, "Name");

		// comparator
		this.columnSortHandler.setComparator(nameColumn, new GeneralComparator<T>(GeneralComparator.Column.NAME));

		// width
		if(width != 0){
			this.setColumnWidth(nameColumn, width, Unit.PX);			
		}


	}

	/**
	 * Adds the default DESCRIPTION column to the table
	 *
	 */
	public void addDescriptionColumn()
	{
		this.addDescriptionColumn(null, 0);		
	}

	/**
	 * Adds the default DESCRIPTION column to the table
	 * 
	 * @param tableFieldUpdater
	 */
	public void addDescriptionColumn(FieldUpdater<T, String> tableFieldUpdater)
	{
		this.addDescriptionColumn(tableFieldUpdater, 0);		
	}

	/**
	 * Adds the default DESCRIPTION column to the table
	 * 
	 * @param tableFieldUpdater
	 * @param width Column width in pixels
	 */
	public void addDescriptionColumn(FieldUpdater<T, String> tableFieldUpdater, int width)
	{
		// Create description column.

		/* TEXT COLUMN */

		if(tableFieldUpdater == null)
		{
			TextColumn<T> descriptionColumn = new TextColumn<T>() {
				@Override
				public String getValue(T obj) {
					GeneralObject go = obj.cast();
					return go.getDescription();
				}
			};

			// sort type column
			descriptionColumn.setSortable(true);
			this.columnSortHandler.setComparator(descriptionColumn, new GeneralComparator<T>(GeneralComparator.Column.DESCRIPTION));

			// adding columns
			this.addColumn(descriptionColumn, "Description");

			// width
			if(width != 0){
				this.setColumnWidth(descriptionColumn, width, Unit.PX);			
			}
			return;
		}

		/* CLICKABLE */

		if(hyperlinksAllowed) {
			Column<T, T> descriptionColumn = JsonUtils.addCustomCellColumn(new HyperlinkCell<T>("description"), tableFieldUpdater);

			descriptionColumn.setSortable(true);

			// adding columns
			this.addColumn(descriptionColumn, "Description");

			// comparator
			this.columnSortHandler.setComparator(descriptionColumn, new GeneralComparator<T>(GeneralComparator.Column.DESCRIPTION));

			// width
			if(width != 0){
				this.setColumnWidth(descriptionColumn, width, Unit.PX);			
			}

		} else {

			Column<T, String> descriptionColumn = JsonUtils.addColumn(
					new JsonUtils.GetValue<T, String>() {
						public String getValue(T object) {
							GeneralObject go = object.cast();
							return go.getDescription();
						}
					}, tableFieldUpdater);

			descriptionColumn.setSortable(true);

			// adding columns
			this.addColumn(descriptionColumn, "Description");

			// comparator
			this.columnSortHandler.setComparator(descriptionColumn, new GeneralComparator<T>(GeneralComparator.Column.DESCRIPTION));

			// width
			if(width != 0){
				this.setColumnWidth(descriptionColumn, width, Unit.PX);			
			}

		}

	}

	/**
	 * Add a handler to handle {@link ColumnSortEvent}s.
	 * 
	 * @param columnSortHandler  the {@link ColumnSortEvent.Handler} to add
	 * @return a {@link HandlerRegistration} to remove the handler
	 */
	public HandlerRegistration addColumnSortHandler(ListHandler<T> columnSortHandler){
		this.columnSortHandler = columnSortHandler;
		return super.addColumnSortHandler(columnSortHandler);
	}

    /**
     * Return sort handler associated with this table
     *
     * @return ColumnSortHandler
     */
    public ListHandler<T> getColumnSortHandler() {
        return this.columnSortHandler;
    }
	
	/**
	 * Removes rowCountChangeHandler which shows count of rows in table in top right corner.
	 */
	public void removeRowCountChangeHandler(){
		if (rowCountChangeHandler != null) {
            rowCountWidget.removeFromParent();
			rowCountChangeHandler.removeHandler();			
		}
	}

}