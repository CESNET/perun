package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.ApplicationMail;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.WhetherEnabledCell;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Returns list of VO applications
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetApplicationMails implements JsonCallback, JsonCallbackTable<ApplicationMail> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// VO / Group id
	private int id;
	// JSON URL
	static private final String JSON_URL = "registrarManager/getApplicationMails";
	// Selection model
	final MultiSelectionModel<ApplicationMail> selectionModel = new MultiSelectionModel<ApplicationMail>(
			new GeneralKeyProvider<ApplicationMail>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table data provider
	private ListDataProvider<ApplicationMail> dataProvider = new ListDataProvider<ApplicationMail>();
	// Table itself
	private PerunTable<ApplicationMail> table;
	// Table list
	private ArrayList<ApplicationMail> list = new ArrayList<ApplicationMail>();
	// Table field updater
	private FieldUpdater<ApplicationMail, String> tableFieldUpdater;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();


	private PerunEntity entity;
	private boolean checkable = true;

	/**
	 * Creates a new method instance
	 *
	 * @param entity VO or Group
	 * @param id ID of entity
	 */
	public GetApplicationMails(PerunEntity entity, int id) {
		this.entity = entity;
		this.id = id;
	}

	/**
	 * Creates a new method instance
	 *
	 * @param entity VO or Group
	 * @param id ID of entity
	 * @param events Custom events
	 */
	public GetApplicationMails(PerunEntity entity, int id, JsonCallbackEvents events) {
		this.entity = entity;
		this.id = id;
		this.events = events;
	}

	/**
	 * Returns the celltable with custom onclick
	 * @param fu Field updater
	 * @return
	 */
	public CellTable<ApplicationMail> getTable(FieldUpdater<ApplicationMail, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns just the celltable
	 * @return
	 */
	public CellTable<ApplicationMail> getTable() {

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<ApplicationMail>(list);

		// Cell table
		table = new PerunTable<ApplicationMail>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<ApplicationMail> columnSortHandler = new ListHandler<ApplicationMail>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<ApplicationMail> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			loaderImage.setEmptyResultMessage("No mail notifications found for this VO.");
		} else if (PerunEntity.GROUP.equals(entity)) {
			loaderImage.setEmptyResultMessage("No mail notifications found for this group.");
		}

		// columns
		if (checkable) {
			table.addCheckBoxColumn();
		}
		table.addIdColumn("E-mail ID", tableFieldUpdater, 90);

		// MAIL TYPE COLUMN
		Column<ApplicationMail, String> mailTypeColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<ApplicationMail, String>() {
					public String getValue(ApplicationMail object) {
						return ApplicationMail.getTranslatedMailType(object.getMailType());
					}
				}, tableFieldUpdater);

		mailTypeColumn.setSortable(true);
		columnSortHandler.setComparator(mailTypeColumn, new Comparator<ApplicationMail>(){
			public int compare(ApplicationMail arg0, ApplicationMail arg1) {
				return (ApplicationMail.getTranslatedMailType(arg0.getMailType())).compareToIgnoreCase(ApplicationMail.getTranslatedMailType(arg1.getMailType()));
			}
		});
		table.addColumn(mailTypeColumn, "E-mail type");

		// APPLICATION TYPE COLUMN
		Column<ApplicationMail, String> appTypeColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<ApplicationMail, String>() {
					public String getValue(ApplicationMail object) {
						return Application.getTranslatedType(object.getAppType());
					}
				}, tableFieldUpdater);

		appTypeColumn.setSortable(true);
		columnSortHandler.setComparator(appTypeColumn, new Comparator<ApplicationMail>(){
			public int compare(ApplicationMail arg0, ApplicationMail arg1) {
				return (Application.getTranslatedType(arg0.getAppType())).compareToIgnoreCase(Application.getTranslatedType(arg1.getAppType()));
			}
		});
		table.addColumn(appTypeColumn, "Application type");

		// ENABLED COLUMN
		Column<ApplicationMail, Boolean> enabledColumn = new Column<ApplicationMail, Boolean>(
				new WhetherEnabledCell()) {
			@Override
			public Boolean getValue(ApplicationMail object) {
				return object.isSend();
			}
		};

		enabledColumn.setSortable(true);
		columnSortHandler.setComparator(enabledColumn, new Comparator<ApplicationMail>(){
			public int compare(ApplicationMail arg0,ApplicationMail arg1) {

				if(arg0.isSend() == arg1.isSend()) return 0;
				if(arg0.isSend() == true) return -1;
				return 1;
			}
		});
		table.addColumn(enabledColumn, "Sending enabled");

		return table;

	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {

		String param = "";

		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			param = "vo="+ this.id;
		} else if (PerunEntity.GROUP.equals(entity)) {
			param = "group="+ this.id;
		}
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Sorts table by objects date
	 */
	public void sortTable() {
		list = new TableSorter<ApplicationMail>().sortById(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(ApplicationMail object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
	 */
	public void removeFromTable(ApplicationMail object) {
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
	public ArrayList<ApplicationMail> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading application mails");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading application mails started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<ApplicationMail>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Application mails loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();

	}

	public void insertToTable(int index, ApplicationMail object) {
		list.add(index, object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<ApplicationMail> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<ApplicationMail> getList() {
		return this.list;
	}

}
