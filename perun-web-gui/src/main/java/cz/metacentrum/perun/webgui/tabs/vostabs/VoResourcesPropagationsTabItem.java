package cz.metacentrum.perun.webgui.tabs.vostabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.propagationStatsReader.GetAllResourcesState;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab with propagation status of all facilities related to VO.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class VoResourcesPropagationsTabItem implements TabItem, TabItemWithUrl {

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();

	/**
	 * Title widget
	 */
	private Label titleWidget = new Label("All VO's resources state");

	private VirtualOrganization vo;
	private int voId;
	private int mainrow = 0;
	private int okCounter = 0;
	private int errorCounter = 0;
	private int notDeterminedCounter = 0;
	private int procesingCounter = 0;

	/**
	 * Creates a tab instance
	 * @param voId
	 */
	public VoResourcesPropagationsTabItem(int voId){
		this.voId = voId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				vo = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
	}

	/**
	 * Creates a tab instance
	 * @param vo
	 */
	public VoResourcesPropagationsTabItem(VirtualOrganization vo){
		this.voId = vo.getId();
		this.vo = vo;
	}


	public boolean isPrepared(){
		return (vo != null);
	}

	public Widget draw() {

		mainrow = 0;
		okCounter = 0;
		errorCounter = 0;
		notDeterminedCounter = 0;
		procesingCounter = 0;

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName())+": resources state");
		final TabItem tab = this;

		VerticalPanel mainTab = new VerticalPanel();
		mainTab.setWidth("100%");

		// MAIN PANEL
		final ScrollPanel firstTabPanel = new ScrollPanel();
		firstTabPanel.setSize("100%", "100%");
		firstTabPanel.setStyleName("perun-tableScrollPanel");

		final FlexTable help = new FlexTable();
		help.setCellPadding(4);
		help.setWidth("100%");

		final CustomButton cb = UiElements.getRefreshButton(this);
		help.setWidget(0, 0, cb);
		help.getFlexCellFormatter().setWidth(0, 0, "80px");
		help.setHTML(0, 1, "<strong>Color&nbsp;notation:</strong>");
		help.getFlexCellFormatter().setWidth(0, 1, "100px");
		help.setHTML(0, 2, "<strong>OK</strong>");
		help.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
		help.getFlexCellFormatter().setWidth(0, 2, "50px");
		help.getFlexCellFormatter().setStyleName(0, 2, "green");
		help.setHTML(0, 3, "<strong>Error</strong>");
		help.getFlexCellFormatter().setWidth(0, 3, "50px");
		help.getFlexCellFormatter().setStyleName(0, 3, "red");
		help.getFlexCellFormatter().setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_CENTER);
		help.setHTML(0, 4, "<strong>Not&nbsp;determined</strong>");
		help.getFlexCellFormatter().setWidth(0, 4, "50px");
		help.getFlexCellFormatter().setHorizontalAlignment(0, 4, HasHorizontalAlignment.ALIGN_CENTER);
		help.getFlexCellFormatter().setStyleName(0, 4, "notdetermined");
		/*
			 help.setHTML(0, 5, "<strong>Processing</strong>");
			 help.getFlexCellFormatter().setWidth(0, 5, "50px");
			 help.getFlexCellFormatter().setStyleName(0, 5, "yellow");
			 help.getFlexCellFormatter().setHorizontalAlignment(0, 5, HasHorizontalAlignment.ALIGN_CENTER);
			 */

		help.setHTML(0, 5, "&nbsp;");
		help.getFlexCellFormatter().setWidth(0, 6, "50%");

		mainTab.add(help);
		mainTab.add(new HTML("<hr size=\"2\" />"));
		mainTab.add(firstTabPanel);

		final FlexTable content = new FlexTable();
		content.setWidth("100%");
		content.setBorderWidth(0);
		firstTabPanel.add(content);
		content.setStyleName("propagationTable", true);
		final AjaxLoaderImage im = new AjaxLoaderImage();
		content.setWidget(0, 0, im);
		content.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);

		final GetAllResourcesState callback = new GetAllResourcesState(voId, new JsonCallbackEvents(){
			public void onLoadingStart(){
				im.loadingStart();
				cb.setProcessing(true);
			}
			public void onError(PerunError error){
				im.loadingError(error);
				cb.setProcessing(false);
			}
			public void onFinished(JavaScriptObject jso) {
				im.loadingFinished();
				cb.setProcessing(false);
				content.clear();
				content.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
				ArrayList<ResourceState> list = JsonUtils.jsoAsList(jso);
				if (list != null && !list.isEmpty()){

					list = new TableSorter<ResourceState>().sortByResourceName(list);

					// PROCESS CLUSTERS (with more than one destinations)

					for (final ResourceState state : list) {

						content.setHTML(mainrow, 0, new Image(LargeIcons.INSTANCE.serverGroupIcon())+"<span class=\"now-managing\" style=\"display: inline-block; position: relative; top: -8px;\">" + state.getResource().getName() + "</span>");

						ArrayList<Task> tasks = new TableSorter<Task>().sortByService(JsonUtils.<Task>jsoAsList(state.getTasks()));

						if (tasks == null || tasks.isEmpty()) notDeterminedCounter++;

						boolean allOk = true;
						for (Task tsk :tasks) {
							if (tsk.getStatus().equalsIgnoreCase("ERROR")) {
								errorCounter++;
								allOk = false;
								break;
							}
						}
						if (allOk && tasks != null && !tasks.isEmpty()) okCounter++;

						ListDataProvider<Task> dataProvider = new ListDataProvider<Task>();
						PerunTable<Task> table;

						// Table data provider.
						dataProvider = new ListDataProvider<Task>(tasks);

						// Cell table
						table = new PerunTable<Task>(tasks);
						table.removeRowCountChangeHandler();

						// Connect the table to the data provider.
						dataProvider.addDataDisplay(table);

						// Sorting
						ColumnSortEvent.ListHandler<Task> columnSortHandler = new ColumnSortEvent.ListHandler<Task>(dataProvider.getList());
						table.addColumnSortHandler(columnSortHandler);

						// set empty content & loader
						AjaxLoaderImage loaderImage = new AjaxLoaderImage();
						loaderImage.setEmptyResultMessage("No service configuration was propagated to this resource.");
						table.setEmptyTableWidget(loaderImage);
						loaderImage.loadingFinished();

						table.addIdColumn("Task Id");

						// Service column
						Column<Task, String> serviceColumn = JsonUtils.addColumn(
								new JsonUtils.GetValue<Task, String>() {
									public String getValue(Task task) {
										return String.valueOf(task.getExecService().getService().getName());
									}
								}, null);

						// status column
						Column<Task, String> statusColumn = JsonUtils.addColumn(
								new JsonUtils.GetValue<Task, String>() {
									public String getValue(Task task) {
										return String.valueOf(task.getStatus());
									}
								}, null);

						// start COLUMN
						TextColumn<Task> startTimeColumn = new TextColumn<Task>() {
							public String getValue(Task result) {
								return result.getStartTime();
							}
						};

						// end COLUMN
						TextColumn<Task> endTimeColumn = new TextColumn<Task>() {
							public String getValue(Task result) {
								return result.getEndTime();
							}
						};

						// schedule COLUMN
						TextColumn<Task> scheduleColumn = new TextColumn<Task>() {
							public String getValue(Task result) {
								return result.getSchedule();
							}
						};

						// Add the columns.
						table.addColumn(serviceColumn, "Service");
						table.addColumn(statusColumn, "Status");
						table.addColumn(scheduleColumn, "Scheduled");
						table.addColumn(startTimeColumn, "Started");
						table.addColumn(endTimeColumn, "Ended");

						// set row styles based on task state
						table.setRowStyles(new RowStyles<Task>(){
							public String getStyleNames(Task row, int rowIndex) {

								if (row.getStatus().equalsIgnoreCase("NONE")) {
									return "rowdarkgreen";
								}
								else if (row.getStatus().equalsIgnoreCase("DONE")){
									return "rowgreen";
								}
								else if (row.getStatus().equalsIgnoreCase("PROCESSING")){
									return "rowyellow";
								}
								else if (row.getStatus().equalsIgnoreCase("ERROR")){
									return "rowred";
								}
								return "";

							}
						});

						table.setWidth("100%");

						content.setWidget(mainrow+1, 0, table);
						content.getFlexCellFormatter().setStyleName(mainrow + 1, 0, "propagationTablePadding");

						mainrow++;
						mainrow++;

					}

				}

				// set counters
				help.setHTML(0, 2, "<strong>Ok&nbsp;("+okCounter+")</strong>");
				help.setHTML(0, 3, "<strong>Error&nbsp;("+errorCounter+")</strong>");
				help.setHTML(0, 4, "<strong>Not&nbsp;determined&nbsp;("+notDeterminedCounter+")</strong>");
				//help.setHTML(0, 5, "<strong>Processing&nbsp;(" + procesingCounter + ")</strong>");

			}
		}); // get for all facilities for VO
		callback.retrieveData();

		// resize perun table to correct size on screen
		session.getUiElements().resizePerunTable(firstTabPanel, 400, this);

		this.contentWidget.setWidget(mainTab);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.arrowRightIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1327;
		int result = 1;
		result = prime * result + voId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VoResourcesPropagationsTabItem other = (VoResourcesPropagationsTabItem) obj;
		if (voId != other.voId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(vo, "Resources state", getUrlWithParameters());
		if(vo != null){
			session.setActiveVo(vo);
			return;
		}
		session.setActiveVoId(voId);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId) || session.isVoObserver(voId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "propags";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?vo="+voId;
	}

	static public VoResourcesPropagationsTabItem load(Map<String, String> parameters) {
		int voId = Integer.parseInt(parameters.get("vo"));
		return new VoResourcesPropagationsTabItem(voId);
	}

}