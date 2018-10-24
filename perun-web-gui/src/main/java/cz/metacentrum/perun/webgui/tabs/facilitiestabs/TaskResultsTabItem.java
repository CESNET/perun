package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.propagationStatsReader.GetRichTaskResultsByTask;
import cz.metacentrum.perun.webgui.model.Task;
import cz.metacentrum.perun.webgui.model.TaskResult;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * Return Tab with Rich Task Results loaded by RichTask
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class TaskResultsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading Task");

	// data
	private Task task;
	private int taskId;

	/**
	 * Creates a tab instance
	 * @param task RichTask
	 */
	public TaskResultsTabItem(Task task){
		this.task = task;
		this.taskId = task.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param taskId
	 */
	public TaskResultsTabItem(int taskId){
		this.taskId = taskId;
		new GetEntityById(PerunEntity.TASK, taskId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				task = jso.cast();
			}
		}).retrieveData();
	}


	public boolean isPrepared(){
		return !(task == null);
	}

	public Widget draw() {

		this.titleWidget.setText("Tasks results: "+task.getService().getName());

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		final GetRichTaskResultsByTask callback = new GetRichTaskResultsByTask(task.getId());

		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));
		menu.addFilterWidget(new ExtendedSuggestBox(callback.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				callback.filterTable(text);
			}
		}, "Filter results by destination");


		// on row click
		CellTable<TaskResult> table = callback.getTable((index, taskResult, value) -> {
			if (taskResult != null) {
				session.getTabManager().addTab(new TaskResultsForDestinationTabItem(task.getId(), taskResult.getDestination().getId()));
			}
		});

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(menu);
		vp.setCellHeight(menu, "30px");
		vp.add(sp);

		session.getUiElements().resizePerunTable(sp, 350, this);


		this.contentWidget.setWidget(vp);


		return getWidget();

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.databaseServerIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 773;
		int result = 1;
		result = prime * result + taskId;
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
		TaskResultsTabItem other = (TaskResultsTabItem) obj;
		if (taskId != other.taskId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(task.getFacility(), "Propagation results: "+task.getService().getName(), getUrlWithParameters());
		if(task != null) {
			if (task.getFacility() != null) {
				session.setActiveFacility(task.getFacility());
			}
		}
	}

	public boolean isAuthorized() {
		if (session.isFacilityAdmin(task.getFacility().getId())) {
			return true;
		} else {
			return false;
		}
	}

	public final static String URL = "taskresults";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + task.getId();
	}

	static public TaskResultsTabItem load(Task task) {
		return new TaskResultsTabItem(task);
	}

	static public TaskResultsTabItem load(Map<String, String> parameters) {
		int tid = Integer.parseInt(parameters.get("id"));
		return new TaskResultsTabItem(tid);
	}

}
