package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
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
		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(execService.getService().getName()) + " ("+execService.getType()+")");

		// CONTENT
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

		// MAIN MENU
		TabMenu menu = new TabMenu();

		// ExecService info
		DecoratorPanel dp = new DecoratorPanel();
		FlexTable ft = new FlexTable();
		ft.setStyleName("inputFormFlexTable");

		ft.setHTML(0, 0, "ExecService ID: </strong>");
		ft.setHTML(1, 0, "ExecService name: </strong>");
		ft.setHTML(2, 0, "Type:");
		ft.setHTML(3, 0, "Enabled: ");
		ft.setHTML(0, 2, "Script path:");
		ft.setHTML(1, 2, "Default delay:");

        ft.getFlexCellFormatter().setStyleName(0, 0, "itemName");
        ft.getFlexCellFormatter().setStyleName(1, 0, "itemName");
        ft.getFlexCellFormatter().setStyleName(2, 0, "itemName");
        ft.getFlexCellFormatter().setStyleName(3, 0, "itemName");
        ft.getFlexCellFormatter().setStyleName(0, 2, "itemName");
        ft.getFlexCellFormatter().setStyleName(1, 2, "itemName");

		ft.setHTML(0, 1, String.valueOf(execServiceId));
		ft.setHTML(1, 1, execService.getService().getName());
		ft.setHTML(2, 1, execService.getType());
		ft.setHTML(3, 1, String.valueOf(execService.isEnabled()));
		ft.setHTML(0, 3, execService.getScriptPath());
		ft.setHTML(1, 3, String.valueOf(execService.getDefaultDelay()));

		dp.add(ft);
		vp.add(dp);

		// callback
	    final ListExecServicesThisExecServiceDependsOn callback = new ListExecServicesThisExecServiceDependsOn(execServiceId);

		// refresh event
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(callback);

        // add button
        menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addDependantExecService(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().addTabToCurrentTab(new AddDependencyTabItem(execService));
            }
        }));

        final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeSelectedDependantExecServices());
        menu.addWidget(removeButton);
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

	    HTML title = new HTML("<h3>Depends on: </h3>");
	    vp.add(title);
	    vp.add(menu);
		vp.setCellHeight(menu, "30px");
	    vp.setCellHeight(title, "20px");

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
		return  SmallIcons.INSTANCE.trafficLightsIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
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
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN);
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
