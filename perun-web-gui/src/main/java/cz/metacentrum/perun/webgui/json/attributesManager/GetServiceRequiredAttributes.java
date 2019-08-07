package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
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

/**
 * Ajax query to get required attributes for specified service
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 *
 */
public class GetServiceRequiredAttributes implements JsonCallback, JsonCallbackTable<AttributeDefinition>, JsonCallbackOracle<AttributeDefinition> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	private int serviceId;
	private final String JSON_URL = "attributesManager/getRequiredAttributesDefinition";
	// Data provider and tables
	private ListDataProvider<AttributeDefinition> dataProvider = new ListDataProvider<AttributeDefinition>();
	private PerunTable<AttributeDefinition> table;
	private ArrayList<AttributeDefinition> list = new ArrayList<AttributeDefinition>();
	// Selection model
	final MultiSelectionModel<AttributeDefinition> selectionModel = new MultiSelectionModel<AttributeDefinition>(new GeneralKeyProvider<AttributeDefinition>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Loader
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// oracle support
	private ArrayList<AttributeDefinition> fullBackup = new ArrayList<AttributeDefinition>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();

	/**
	 * Creates a new callback
	 *
	 * @param serviceId ID of service to get required attributes for
	 */
	public GetServiceRequiredAttributes(int serviceId) {
		this.serviceId = serviceId;
	}

	/**
	 * Creates a new callback
	 *
	 * @param serviceId ID of service to get required attributes for
	 * @param events external events
	 */
	public GetServiceRequiredAttributes(int serviceId, JsonCallbackEvents events) {
		this.events = events;
		this.serviceId = serviceId;
	}

	/**
	 * Returns table widget with required attributes for service
	 *
	 * @return table widget
	 */
	public CellTable<AttributeDefinition> getTable() {

		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<AttributeDefinition>(list);

		// Cell table
		table = new PerunTable<AttributeDefinition>(list);
		table.removeRowCountChangeHandler(); // remove row count change handler

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<AttributeDefinition> columnSortHandler = new ListHandler<AttributeDefinition>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<AttributeDefinition> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("Service has no required attribute.");

		// checkbox column column
		table.addCheckBoxColumn();

		// ID COLUMN
		table.addIdColumn("Attribute ID");

		// FRIENDLY NAME COLUMN
		final Column<AttributeDefinition, AttributeDefinition> friendlyNameColumn = JsonUtils.addColumn(new PerunAttributeFriendlyNameCell(),
				new JsonUtils.GetValue<AttributeDefinition, AttributeDefinition>() {
					public AttributeDefinition getValue(AttributeDefinition object) {
						return object;
					}
				},null);

		/*
		// NAMESPACE COLUMN
		TextColumn<AttributeDefinition> namespaceColumn = new TextColumn<AttributeDefinition>() {
		public String getValue(AttributeDefinition attrDef) {
		return String.valueOf(attrDef.getNamespace());
		}
		};
		*/

		// ENTITY COLUMN
		TextColumn<AttributeDefinition> entityColumn = new TextColumn<AttributeDefinition>() {
			public String getValue(AttributeDefinition attrDef) {
				return attrDef.getEntity();
			}
		};

		// DEFINITION COLUMN
		TextColumn<AttributeDefinition> definitionColumn = new TextColumn<AttributeDefinition>() {
			public String getValue(AttributeDefinition attrDef) {
				return attrDef.getDefinition();
			}
		};

		// TYPE COLUMN
		TextColumn<AttributeDefinition> typeColumn = new TextColumn<AttributeDefinition>() {
			public String getValue(AttributeDefinition attrDef) {
				return String.valueOf(renameContent(attrDef.getType()));
			}
		};


		// SORTING

		/*
			 namespaceColumn.setSortable(true);
			 columnSortHandler.setComparator(namespaceColumn, new Comparator<AttributeDefinition>() {
			 public int compare(AttributeDefinition o1, AttributeDefinition o2) {
			 return o1.getNamespace().compareToIgnoreCase(o2.getNamespace());
			 }
			 });
			 */

		friendlyNameColumn.setSortable(true);
		columnSortHandler.setComparator(friendlyNameColumn, new Comparator<AttributeDefinition>() {
			public int compare(AttributeDefinition o1, AttributeDefinition o2) {
				return o1.getFriendlyName().compareToIgnoreCase(o2.getFriendlyName());
			}
		});

		// Sorting value column
		entityColumn.setSortable(true);
		columnSortHandler.setComparator(entityColumn,
				new Comparator<AttributeDefinition>() {
					public int compare(AttributeDefinition o1, AttributeDefinition o2) {
						return o1.getEntity().compareToIgnoreCase(o2.getEntity());
					}
				});

		// Sorting value column
		definitionColumn.setSortable(true);
		columnSortHandler.setComparator(definitionColumn,
				new Comparator<AttributeDefinition>() {
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

		table.setColumnWidth(friendlyNameColumn, 250.0, Unit.PX);
		table.setColumnWidth(entityColumn, 120.0, Unit.PX);
		table.setColumnWidth(definitionColumn, 120.0, Unit.PX);
		table.setColumnWidth(typeColumn, 120.0, Unit.PX);

		// Add the columns.
		table.addColumn(friendlyNameColumn, "Name");
		//attributesTable.addColumn(namespaceColumn, "Namespace");
		table.addColumn(entityColumn, "Entity");
		table.addColumn(definitionColumn, "Definition");
		table.addColumn(typeColumn, "Value type");

		table.addDescriptionColumn();

		return table;

	}

	/**
	 * Starts RPC call
	 */
	public void retrieveData(){
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, "service="+serviceId, this);
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
	 * Clear all table content
	 */
	public void clearTable(){
		loaderImage.loadingStart();
		list.clear();
		fullBackup.clear();
		selectionModel.clear();
		oracle.clear();
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
		session.getUiElements().setLogErrorText("Error while loading required attributes.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading required attributes started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<AttributeDefinition>jsoAsList(jso));
		sortTable();
		loaderImage.loadingFinished();
		session.getUiElements().setLogText("Required attributes loaded: " + list.size());
		events.onFinished(jso);
	}

	public void insertToTable(int index, AttributeDefinition object) {
		list.add(index, object);
		oracle.add(object.getFriendlyName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		// TODO Auto-generated method stub
	}

	public void setList(ArrayList<AttributeDefinition> list) {
		clearTable();
		this.list.addAll(list);
		for (AttributeDefinition def : list) {
			oracle.add(def.getFriendlyName());
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
	 * @param oldString original text
	 * @return String new string (substring which starts on position 10)
	 */
	private String renameContent(String oldString){
		String newString = oldString.substring(10);
		return newString;
	}

	@Override
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
			loaderImage.setEmptyResultMessage("No required attribute matching '"+filter+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("No required attribute found.");
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();

	}

	@Override
	public UnaccentMultiWordSuggestOracle getOracle() {
		return this.oracle;
	}

	@Override
	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}
}
