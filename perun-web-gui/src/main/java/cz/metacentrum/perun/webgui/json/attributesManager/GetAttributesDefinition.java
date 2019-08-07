package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.AttributeDefinition;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeFriendlyNameCell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Ajax query to get all attributes definitions in Perun
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAttributesDefinition implements JsonCallback, JsonCallbackTable<AttributeDefinition>, JsonCallbackOracle<AttributeDefinition> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	private final String JSON_URL = "attributesManager/getAttributesDefinition";
	// Data provider and tables
	private ListDataProvider<AttributeDefinition> dataProvider = new ListDataProvider<AttributeDefinition>();
	private PerunTable<AttributeDefinition> table;
	private ArrayList<AttributeDefinition> list = new ArrayList<AttributeDefinition>();
	// Selection model
	final MultiSelectionModel<AttributeDefinition> selectionModel = new MultiSelectionModel<AttributeDefinition>(new GeneralKeyProvider<AttributeDefinition>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// filters
	private boolean noCore = false; // default is to show core attributes
	private boolean checkable = true;
	// oracle support
	private ArrayList<AttributeDefinition> fullBackup = new ArrayList<AttributeDefinition>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();
	private FieldUpdater<AttributeDefinition, String> tableFieldUpdater = null;
	private boolean editable = true; // editable by default

	//	private String entity = "";  // default is to show all types of entity

	// if Set empty -> show all
	private Set<String> entities = new HashSet<String>();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	/**
	 * Creates new instance of callback
	 */
	public GetAttributesDefinition() {}

	/**
	 * Creates new instance of callback
	 *
	 * @param events external events
	 */
	public GetAttributesDefinition(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Returns table widget with attributes definitions
	 *
	 * @param updater TableFieldUpdater
	 * @return table widget
	 */
	public CellTable<AttributeDefinition> getTable(FieldUpdater<AttributeDefinition, String> updater) {
		this.tableFieldUpdater = updater;
		return getTable();
	}

	/**
	 * Returns table widget with attributes definitions
	 *
	 * @return table widget
	 */
	public CellTable<AttributeDefinition> getTable() {

		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<AttributeDefinition>(list);

		// Cell table
		table = new PerunTable<AttributeDefinition>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<AttributeDefinition> columnSortHandler = new ListHandler<AttributeDefinition>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<AttributeDefinition> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("No attribute defined in Perun.");

		// checkbox column column
		if (checkable) {
			table.addCheckBoxColumn();
		}

		// ID COLUMN
		table.addIdColumn("Attr ID", tableFieldUpdater, 100);

		// FRIENDLY NAME COLUMN
		final Column<AttributeDefinition, AttributeDefinition> friendlyNameColumn = JsonUtils.addColumn(new PerunAttributeFriendlyNameCell((tableFieldUpdater != null) ? "customClickableTextCell" : ""),
				new JsonUtils.GetValue<AttributeDefinition, AttributeDefinition>() {
					public AttributeDefinition getValue(AttributeDefinition object) {
						return object;
					}
				}, (tableFieldUpdater != null) ? new FieldUpdater<AttributeDefinition, AttributeDefinition>() {
					@Override
					public void update(int index, AttributeDefinition object, AttributeDefinition value) {
						// pass field updater to original one
						if (tableFieldUpdater != null) tableFieldUpdater.update(index, object, value.getFriendlyName());
					}
				} : null);

		// ENTITY COLUMN
		final Column<AttributeDefinition, String> entityColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<AttributeDefinition, String>() {
					public String getValue(AttributeDefinition object) {
						return object.getEntity();
					}
				}, tableFieldUpdater);

		// DEFINITION COLUMN
		final Column<AttributeDefinition, String> definitionColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<AttributeDefinition, String>() {
					public String getValue(AttributeDefinition object) {
						return object.getDefinition();
					}
				}, tableFieldUpdater);

		// TYPE COLUMN
		final Column<AttributeDefinition, String> typeColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<AttributeDefinition, String>() {
					public String getValue(AttributeDefinition object) {
						if (object.getType() != null) {
							return object.getType().substring(object.getType().lastIndexOf(".")+1);
						}
						return "";
					}
				}, tableFieldUpdater);

		// UNIQUE COLUMN
		final Column<AttributeDefinition, String> uniqueColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<AttributeDefinition, String>() {
					public String getValue(AttributeDefinition object) {
						return String.valueOf(object.isUnique());
					}
				}, tableFieldUpdater);

		// DESCRIPTION COLUMN
		final Column<AttributeDefinition, String> descriptionColumn = new Column<AttributeDefinition, String>(new TextInputCell()) {
			public String getValue(AttributeDefinition attrDef) {
				if (attrDef.getDescription() == null) {
					return "";
				} else {
					return attrDef.getDescription();
				}
			}
		};

		descriptionColumn.setFieldUpdater(new FieldUpdater<AttributeDefinition, String>() {
			@Override
			public void update(int i, final AttributeDefinition attributeDefinition, final String s) {
				attributeDefinition.setDescription(s.trim());
				selectionModel.setSelected(attributeDefinition, true);
			}
		});

		// DISPLAY NAME COLUMN
		final Column<AttributeDefinition, String> displayNameColumn = new Column<AttributeDefinition, String>(new TextInputCell()) {
			public String getValue(AttributeDefinition attrDef) {
				return attrDef.getDisplayName();
			}
		};

		displayNameColumn.setFieldUpdater(new FieldUpdater<AttributeDefinition, String>() {
			@Override
			public void update(int i, final AttributeDefinition attributeDefinition, final String s) {
				attributeDefinition.setDisplayName(s.trim());
				selectionModel.setSelected(attributeDefinition, true);
			}
		});

		friendlyNameColumn.setSortable(true);
		columnSortHandler.setComparator(friendlyNameColumn, new Comparator<AttributeDefinition>() {
			public int compare(AttributeDefinition o1, AttributeDefinition o2) {
				return o1.getFriendlyName().compareToIgnoreCase(o2.getFriendlyName());
			}
		});

		entityColumn.setSortable(true);
		columnSortHandler.setComparator(entityColumn, new Comparator<AttributeDefinition>() {
			public int compare(AttributeDefinition o1, AttributeDefinition o2) {
				return o1.getEntity().compareToIgnoreCase(o2.getEntity());
			}
		});

		definitionColumn.setSortable(true);
		columnSortHandler.setComparator(definitionColumn, new Comparator<AttributeDefinition>() {
			public int compare(AttributeDefinition o1, AttributeDefinition o2) {
				return o1.getDefinition().compareToIgnoreCase(o2.getDefinition());
			}
		});

		typeColumn.setSortable(true);
		columnSortHandler.setComparator(typeColumn, new Comparator<AttributeDefinition>() {
			public int compare(AttributeDefinition o1, AttributeDefinition o2) {
				return o1.getType().compareToIgnoreCase(o2.getType());
			}
		});

		uniqueColumn.setSortable(true);
		columnSortHandler.setComparator(uniqueColumn, new Comparator<AttributeDefinition>() {
			public int compare(AttributeDefinition o1, AttributeDefinition o2) {
				return String.valueOf(o1.isUnique()).compareToIgnoreCase(String.valueOf(o2.isUnique()));
			}
		});

		// Add the column sort handler.
		table.setColumnWidth(friendlyNameColumn, 250.0, Unit.PX);
		table.setColumnWidth(entityColumn, 100.0, Unit.PX);
		table.setColumnWidth(definitionColumn, 100.0, Unit.PX);
		table.setColumnWidth(typeColumn, 100.0, Unit.PX);
		table.setColumnWidth(uniqueColumn, 100.0, Unit.PX);

		// Add the columns.
		table.addColumn(friendlyNameColumn, "Name");
		// attributesTable.addColumn(namespaceColumn, "Namespace");
		table.addColumn(entityColumn, "Entity");
		table.addColumn(definitionColumn, "Definition");
		table.addColumn(typeColumn, "Type");
		table.addColumn(uniqueColumn, "Unique");
		if (editable) {
			table.addColumn(displayNameColumn, "Display name");
			table.addColumn(descriptionColumn, "Description");
		}

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
		list = new TableSorter<AttributeDefinition>().sortByFriendlyName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object AttributeDefinition to be added as new row
	 */
	public void addToTable(AttributeDefinition object) {
		list.add(object);
		oracle.add(object.getFriendlyName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object AttributeDefinition to be removed as row
	 */
	public void removeFromTable(AttributeDefinition object) {
		list.remove(object);
		selectionModel.getSelectedSet().remove(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object AttributeDefinition to be removed as row
	 */
	public void removeFromBackupTable(AttributeDefinition object) {
		fullBackup.remove(object);
		list.remove(object);
		selectionModel.setSelected(object, false);
		oracle.clear();
		for (AttributeDefinition def : fullBackup) {
			oracle.add(def.getFriendlyName());
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
		fullBackup.clear();
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
	public ArrayList<AttributeDefinition> getTableSelectedList(){
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
		for (AttributeDefinition a : JsonUtils.<AttributeDefinition>jsoAsList(jso)) {
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

	public void insertToTable(int index, AttributeDefinition object) {
		list.add(index, object);
		oracle.add(object.getFriendlyName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<AttributeDefinition> list) {
		clearTable();
		this.list.addAll(list);
		for (AttributeDefinition a : list) {
			oracle.add(a.getFriendlyName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<AttributeDefinition> getList() {
		return this.list;
	}

	/**
	 * Change showed string in table to new one based on it's own rules
	 *
	 * @param oldString original string
	 * @return String new string (substring which starts on position 10)
	 */
	private String renameContent(String oldString){
		String newString = oldString.substring(10);
		return newString;
	}

	/**
	 * Helper method for switching show/do not show core attributes
	 */
	public void switchCore() {
		noCore = !(noCore);
	}


	/**
	 * Set entity filter on returned attributeDefinitions
	 *
	 * @param entity name of entity (member,resource,facility,user,....)
	 */
	public void setEntity(String entity) {
		this.setEntity(entity, false);
	}


	/**
	 * Set entity filter on returned attributeDefinitions
	 *
	 * @param entity name of entity (member,resource,facility,user,....)
	 * @param add true if adding
	 */
	public void setEntity(String entity, boolean add) {
		if(!add) {
			this.entities.clear();
		}
		this.entities.add(entity);
	}

	/**
	 * Set entity filter on returned attributeDefinitions
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
	public SelectionModel<AttributeDefinition> getSelectionModel(){
		return this.selectionModel;
	}

	public void filterTable(String filter) {

		// store list only for first time
		if (fullBackup.isEmpty() || fullBackup == null) {
			fullBackup.addAll(list);
		}

		// always clear selected items
		selectionModel.clear();
		list.clear();

		if (filter.equalsIgnoreCase("")) {
			list.addAll(fullBackup);
		} else {
			for (AttributeDefinition attr : fullBackup){
				// store facility by filter
				if (attr.getFriendlyName().toLowerCase().startsWith(filter.toLowerCase())) {
					list.add(attr);
				}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("No attribute definition matching '"+filter+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("No attribute defined in Perun.");
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();

	}

	public UnaccentMultiWordSuggestOracle getOracle() {
		return this.oracle;
	}

	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}
}
