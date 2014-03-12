package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.generalServiceManager.CreateDependency;
import cz.metacentrum.perun.webgui.json.generalServiceManager.ListExecServices;
import cz.metacentrum.perun.webgui.model.ExecService;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 *  Returns widget containing add Dependency form for selected exec service
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz> 
 */
public class AddDependencyTabItem implements TabItem {

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
	private Label titleWidget = new Label("loading service");

	// data
	private ExecService execService;

	protected int execServiceId;

	/**
	 * Creates a tab instance
     * @param service
     */
	public AddDependencyTabItem(ExecService service){
		this.execService = service;
		this.execServiceId = service.getId();
	}
	
	/**
	 * Creates a tab instance
     * @param serviceId
     */
	public AddDependencyTabItem(int serviceId){
		this.execServiceId = serviceId;
		new GetEntityById(PerunEntity.EXEC_SERVICE, execServiceId, new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso){
                execService = jso.cast();
            }
        }).retrieveData();
	}
	
	public boolean isPrepared() {
		return !(execService == null);
	}


	public Widget draw() {

		// TITLE
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(execService.getService().getName()) + ": add dependency");

        final VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%","100%");

        // prepares layout
        FlexTable layout = new FlexTable();
        layout.setStyleName("inputFormFlexTable");
        FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

        // close tab events
        final TabItem tab = this;

        TabMenu menu = new TabMenu();

		final ListBoxWithObjects<ExecService> listBox = new ListBoxWithObjects<ExecService>();

        final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addDependantExecService());

        // fill listbox after callback finishes
		final JsonCallbackEvents localEvents = new JsonCallbackEvents(){
			@Override
            public void onFinished(JavaScriptObject jso) {
				listBox.clear();
                ArrayList<ExecService> execs = JsonUtils.jsoAsList(jso);
				if (execs != null && !execs.isEmpty()) {
                    execs = new TableSorter<ExecService>().sortByService(execs);
                    for (int i=0; i<execs.size(); i++){
                        listBox.addItem(execs.get(i));
                    }
                    addButton.setEnabled(true);
                } else {
                    listBox.addItem("No exec service available");
                }
			}
            @Override
            public void onLoadingStart(){
                listBox.clear();
                listBox.addItem("Loading...");
                addButton.setEnabled(false);
            }
            @Override
            public void onError(PerunError error){
                listBox.addItem("Error while loading");
                addButton.setEnabled(false);
            }
		};

		// callback for all services
		ListExecServices callback = new ListExecServices(0, localEvents);
		callback.retrieveData();

        // layout

        layout.setHTML(0, 0, "ExecService:");
        layout.setHTML(1, 0, "Depend On:");

        layout.setHTML(0, 1, execService.getService().getName() + " " + execService.getType());
        layout.setWidget(1, 1, listBox);

        final JsonCallbackEvents closeTabEvents = JsonCallbackEvents.closeTabDisableButtonEvents(addButton, tab);

		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				CreateDependency request = new CreateDependency(closeTabEvents);
				request.createDependancy(execServiceId, listBox.getSelectedObject().getId());
			}
		});

        final CustomButton cancelButton = TabMenu.getPredefinedButton(ButtonType.CANCEL, "");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        });

        for (int i=0; i<layout.getRowCount(); i++) {
            cellFormatter.addStyleName(i, 0, "itemName");
        }

        menu.addWidget(addButton);
        menu.addWidget(cancelButton);

        vp.add(layout);
        vp.add(menu);
        vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

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
		return SmallIcons.INSTANCE.addIcon(); 
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) { 
			return true; 
		} else {
			return false;
		}

	}

}