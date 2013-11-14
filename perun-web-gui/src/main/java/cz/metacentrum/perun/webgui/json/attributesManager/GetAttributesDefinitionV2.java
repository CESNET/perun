package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.comparators.AttributeComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeValueCell;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeNameCell;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeDescriptionCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Ajax query to get all attributes definitions in Perun but stores them as attributes
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class GetAttributesDefinitionV2 implements JsonCallback, JsonCallbackTable<Attribute>, JsonCallbackOracle<Attribute> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	private final String JSON_URL = "attributesManager/getAttributesDefinition";
	// Data provider and tables
	private ListDataProvider<Attribute> dataProvider = new ListDataProvider<Attribute>();
	private PerunTable<Attribute> table;
	private ArrayList<Attribute> list = new ArrayList<Attribute>();
	// Selection model
	final MultiSelectionModel<Attribute> selectionModel = new MultiSelectionModel<Attribute>(new GeneralKeyProvider<Attribute>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// filters
	private boolean noCore = false; // default is to show core attributes
	private boolean checkable = true;
	// oracle support
	private ArrayList<Attribute> fullBackup = new ArrayList<Attribute>();
	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();

	//	private String entity = "";  // default is to show all types of entity

	// if Set empty -> show all
	private Set<String> entities = new HashSet<String>();

	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	/**
	 * Creates new instance of callback
	 *
     */
	public GetAttributesDefinitionV2() {}

	/**
	 * Creates new instance of callback
	 *
     * @param events external events
     */
	public GetAttributesDefinitionV2(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Returns table widget with attributes definitions
	 * 
	 * @return table widget
	 */
	public CellTable<Attribute> getTable() {

		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Attribute>(list);

		// Cell table
		table = new PerunTable<Attribute>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Attribute> columnSortHandler = new ListHandler<Attribute>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Attribute> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// checkbox column column
		if (checkable) {
			table.addCheckBoxColumn();
		}
		
		// Create ID column.
		table.addIdColumn("Attr ID", null, 90);

		// Name column
		Column<Attribute, Attribute> nameColumn = JsonUtils.addColumn(new PerunAttributeNameCell());

		// Description column
		Column<Attribute, Attribute> descriptionColumn = JsonUtils.addColumn(new PerunAttributeDescriptionCell());

        // Value column
        Column<Attribute, Attribute> valueColumn = JsonUtils.addColumn(new PerunAttributeValueCell());
        valueColumn.setFieldUpdater(new FieldUpdater<Attribute, Attribute>() {
            public void update(int index, Attribute object, Attribute value) {
                object = value;
                selectionModel.setSelected(object, object.isAttributeValid());
            }
        });

		// updates the columns size
		this.table.setColumnWidth(nameColumn, 200.0, Unit.PX);
		
		// Sorting name column
		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new AttributeComparator<Attribute>(AttributeComparator.Column.TRANSLATED_NAME));
		
		// Sorting description column
		descriptionColumn.setSortable(true);
		columnSortHandler.setComparator(descriptionColumn, new AttributeComparator<Attribute>(AttributeComparator.Column.TRANSLATED_DESCRIPTION));

		// Add sorting
		this.table.addColumnSortHandler(columnSortHandler);

		// Add the columns.
		this.table.addColumn(nameColumn, "Name");
		this.table.addColumn(valueColumn, "Value");
		this.table.addColumn(descriptionColumn, "Description");
		
		return table;

	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData(){
		loaderImage.loadingStart();
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, this);
	}

    /**
     * Sorts table by objects Name
     */
    public void sortTable() {
        list = new TableSorter<Attribute>().sortByAttrNameTranslation(getList());
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Add object as new row to table
     *
     * @param object Attribute to be added as new row
     */
    public void addToTable(Attribute object) {
        list.add(object);
        oracle.add(object.getFriendlyName());
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Removes object as row from table
     *
     * @param object Attribute to be removed as row
     */
    public void removeFromTable(Attribute object) {
        selectionModel.getSelectedSet().remove(object);
        Iterator<Attribute> it = list.iterator();
        while(it.hasNext()) {
            Attribute a = it.next();
            if (a.getId() == object.getId()){
                it.remove();
            }
        }
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Clear all table content
     */
    public void clearTable(){
        loaderImage.loadingStart();
        list.clear();
        oracle.clear();
        selectionModel.clear();
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Clears list of selected items
     */
    public void clearTableSelectedSet(){
        selectionModel.clear();
    }

    /**
     * Return selected items from list
     *
     * @return return list of checked items
     */
    public ArrayList<Attribute> getTableSelectedList(){
        return JsonUtils.setToList(selectionModel.getSelectedSet());
    }

    /**
     * Called, when an error occurs
     */
    public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Error while loading attribute definitions.");
        loaderImage.loadingError(error);
        events.onError(error);
    }

    /**
     * Called, when loading starts
     */
    public void onLoadingStart() {
        session.getUiElements().setLogText("Loading attribute definitions started.");
        events.onLoadingStart();
    }

    /**
     * Called, when operation finishes successfully.
     */
    public void onFinished(JavaScriptObject jso) {
        clearTable();
        for (Attribute a : JsonUtils.<Attribute>jsoAsList(jso)) {
            // check namespace for core
            if (noCore && a.getDefinition().equals("core")) {
                // do not add anything
            } else {
                // check namespace for entity
                // if not empty, proceed to check
                if (!entities.isEmpty()) {
                    if (entities.contains(a.getEntity())) {
                        // add
                        addToTable(a);
                    }
                } else {
                    addToTable(a);
                }
            }
        }
        sortTable();
        loaderImage.loadingFinished();
        session.getUiElements().setLogText("Attribute definitions loaded: " + list.size());
        events.onFinished(jso);
    }

    public void insertToTable(int index, Attribute object) {
        list.add(index, object);
        oracle.add(object.getFriendlyName());
        dataProvider.flush();
        dataProvider.refresh();
    }

    public void setEditable(boolean editable) {
        //this.editable = editable;
    }

    public void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }

    public void setList(ArrayList<Attribute> list) {
        clearTable();
        this.list.addAll(list);
        for (Attribute a : list) {
            oracle.add(a.getFriendlyName());
        }
        dataProvider.flush();
        dataProvider.refresh();
    }

    public ArrayList<Attribute> getList() {
        return this.list;
    }

	/**
	 * Helper method for switching show/do not show core attributes
	 */
	public void switchCore() {
		noCore = !(noCore);
	}


	/**
	 * Set entity filter on returned Attributes
	 * 
	 * @param entity name of entity (member,resource,facility,user,....)
	 */
	public void setEntity(String entity) {
		this.setEntity(entity, false);
	}


	/**
	 * Set entity filter on returned Attributes
	 * 
	 * @param entity name of entity (member,resource,facility,user,....)
	 * @param add true if adding
	 */
	public void setEntity(String entity, boolean add) {
		if(!add)
		{
			this.entities.clear();
		}
		this.entities.add(entity);
	}

	/**
	 * Set entity filter on returned Attributes
	 * 
	 * @param entities set of the entities
	 */
	public void setEntities(Set<String> entities) {
		this.entities = entities;
	}

	/**
	 * Sets external events to callback after it's creation
	 * 
	 * @param externalEvents external events
	 */
	public void setEvents(JsonCallbackEvents externalEvents) {
		events = externalEvents;
	}

	/**
	 * Returns table selection model
	 * 
	 * @return selection model
	 */
	public SelectionModel<Attribute> getSelectionModel(){
		return this.selectionModel;
	}

	public void filterTable(String filter) {
		
		// always clear selected items
		selectionModel.clear();
		
		// store list only for first time
		if (fullBackup.isEmpty() || fullBackup == null) {
			for (Attribute attr : getList()){
				fullBackup.add(attr);
			}	
		}
		if (filter.equalsIgnoreCase("")) {
			setList(fullBackup);
		} else {
			getList().clear();
			for (Attribute attr : fullBackup){
				// store facility by filter
				if (attr.getFriendlyName().toLowerCase().startsWith(filter.toLowerCase())) {
					addToTable(attr);
				}
			}
			if (getList().isEmpty()) {
				loaderImage.loadingFinished();
			}
            dataProvider.flush();
            dataProvider.refresh();
		}

	}

	public MultiWordSuggestOracle getOracle() {
		return oracle;
	}

	public void setOracle(MultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}
}