package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.generalServiceManager.ListExecServicesThisExecServiceDependsOn;
import cz.metacentrum.perun.webgui.json.generalServiceManager.RemoveDependency;
import cz.metacentrum.perun.webgui.model.ExecService;
import cz.metacentrum.perun.webgui.tabs.ServicesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 *  Returns tab with exec services details and management (dependency, enable/disable, update settings)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class ViewExecServiceTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading Exec service");

	// data
	private ExecService execService;

	private int execServiceId;

	/**
	 * Creates a tab instance
	 * @param service
	 */
	public ViewExecServiceTabItem(ExecService service){
		this.execService = service;
		this.execServiceId = service.getId();

	}

	/**
	 * Creates a tab instance
	 * @param serviceId
	 */
	public ViewExecServiceTabItem(int serviceId){
		this.execServiceId = serviceId;
		new GetEntityById(PerunEntity.EXEC_SERVICE, execServiceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				execService = jso.cast();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return !(execService == null);
	}

	public Widget draw() {

		// TITLE
		this.titleWidget.setText("Exec of "+Utils.getStrippedStringWithEllipsis(execService.getService().getName()) + " ("+execService.getType()+")");

		// CONTENT
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

		AbsolutePanel dp = new AbsolutePanel();
		final FlexTable menu = new FlexTable();
		menu.setCellSpacing(5);

		// Add service information
		menu.setWidget(0, 0, new Image(LargeIcons.INSTANCE.trafficLightsIcon()));
		Label serviceName = new Label();
		serviceName.setText("Exec of "+Utils.getStrippedStringWithEllipsis(execService.getService().getName(), 40));
		serviceName.setStyleName("now-managing");
		serviceName.setTitle(execService.getService().getName());
		menu.setWidget(0, 1, serviceName);

		menu.setHTML(0, 2, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, 2, "25px");

		int column = 3;

		CustomButton cb = new CustomButton("Edit…", "Edit exec service", SmallIcons.INSTANCE.applicationFormEditIcon(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new EditExecServiceTabItem(execService, new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						execService = jso.cast();
						open();
						draw();
					}
				}));
			}
		});
		menu.setWidget(0, column, cb);
		column++;
		menu.setHTML(0, column, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, column, "25px");
		column++;

		if (JsonUtils.isExtendedInfoVisible()) {
			menu.setHTML(0, column, "<strong>ID:</strong><br/><span class=\"inputFormInlineComment\">"+execService.getId()+"</span>");
			column++;
			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;
		}

		menu.setHTML(0, column, "<strong>Type:</strong><br/><span class=\"inputFormInlineComment\">"+execService.getType()+"</span>");
		column++;
		menu.setHTML(0, column, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, column, "25px");
		column++;

		menu.setHTML(0, column, "<strong>Enabled:</strong><br/><span class=\"inputFormInlineComment\">"+execService.isEnabled()+"</span>");
		column++;
		menu.setHTML(0, column, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, column, "25px");
		column++;

		menu.setHTML(0, column, "<strong>Script path:</strong><br/><span class=\"inputFormInlineComment\">"+execService.getScriptPath()+"</span>");
		column++;
		menu.setHTML(0, column, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, column, "25px");
		column++;

		menu.setHTML(0, column, "<strong>Delay:</strong><br/><span class=\"inputFormInlineComment\">"+execService.getDefaultDelay()+"</span>");

		// TODO - waiting for time, when service will have description param
		//menu.setHTML(0, 3, "<strong>Short&nbsp;name:</strong><br/><span class=\"inputFormInlineComment\">"+service.getDescription()+"</span>");

		dp.add(menu);
		vp.add(dp);
		vp.setCellHeight(dp, "30px");

		// tab panel
		TabLayoutPanel tabPanel = new TabLayoutPanel(33, Style.Unit.PX);
		tabPanel.addStyleName("smallTabPanel");
		final TabItem tab = this;
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			public void onSelection(SelectionEvent<Integer> event) {
				UiElements.runResizeCommands(tab);
			}
		});

		final SimplePanel sp0 = new SimplePanel(); // information overview
		sp0.setWidget(getDependencyContent());

		session.getUiElements().resizeSmallTabPanel(tabPanel, 100, this);

		tabPanel.add(sp0, "Dependencies");

		session.getUiElements().resizePerunTable(tabPanel, 100, this);

		// add tabs to the main panel
		vp.add(tabPanel);

		this.contentWidget.setWidget(vp);

		return getWidget();

	}

	public void setExecService(ExecService exec) {
		this.execService = exec;
		this.execServiceId = exec.getId();
	}

	public VerticalPanel getDependencyContent() {

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// MAIN MENU
		TabMenu tabMenu = new TabMenu();
		tabMenu.addWidget(UiElements.getRefreshButton(this));

		// callback
		final ListExecServicesThisExecServiceDependsOn callback = new ListExecServicesThisExecServiceDependsOn(execServiceId);

		// refresh event
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(callback);

		// add button
		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addDependantExecService(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new AddDependencyTabItem(execService));
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeSelectedDependantExecServices());
		tabMenu.addWidget(removeButton);
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<ExecService> dependencyForDeletion = callback.getTableSelectedList();
				UiElements.showDeleteConfirm(dependencyForDeletion, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
						for (int i=0; i<dependencyForDeletion.size(); i++ ) {
							if (i == dependencyForDeletion.size()-1) {
								RemoveDependency request = new RemoveDependency(JsonCallbackEvents.disableButtonEvents(removeButton, events));
								request.removeDependency(execServiceId, dependencyForDeletion.get(i).getId());
							} else {
								RemoveDependency request = new RemoveDependency(JsonCallbackEvents.disableButtonEvents(removeButton));
								request.removeDependency(execServiceId, dependencyForDeletion.get(i).getId());
							}
						}
					}
				});
			}
		});

		vp.add(tabMenu);
		vp.setCellHeight(tabMenu, "30px");

		// get table
		CellTable<ExecService> table = callback.getTable();

		// create scroll panel for table
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		sp.setWidth("100%");

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(callback, table, removeButton);

		vp.add(sp);
		vp.setCellHeight(sp, "100%");

		session.getUiElements().resizePerunTable(sp, 250, this);

		return vp;

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return  SmallIcons.INSTANCE.trafficLightsIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1117;
		int result = 1;
		result = prime * result + execServiceId;
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
		ViewExecServiceTabItem other = (ViewExecServiceTabItem) obj;
		if (execServiceId != other.execServiceId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Services", ServicesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + ServicesTabItem.URL, execService.getService().getName(), ServicesTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+ServiceDetailTabItem.URL+"?id="+ execService.getService().getId());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "exec-view";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return ServicesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + execServiceId;
	}

	static public ViewExecServiceTabItem load(Map<String, String> parameters)
	{
		int id = Integer.parseInt(parameters.get("id"));
		return new ViewExecServiceTabItem(id);
	}

}
