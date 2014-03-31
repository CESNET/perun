package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.servicesManager.*;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Provides tab for adding destination to facility
 *
 * !! USE AS INNER TAB ONLY !!
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddFacilityDestinationTabItem implements TabItem {

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
	private Label titleWidget = new Label("Add destination");
	
	// data
	private Facility facility;
	private int facilityId;
	
	/**
	 * Creates a tab instance
     * @param facility facility to get services from / destination to add
     */
	public AddFacilityDestinationTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}
	
	public boolean isPrepared() {
		return (facility != null);
	}
	
	public Widget draw() {
		
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": add destination");
		
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

        // prepares layout
        FlexTable layout = new FlexTable();
        layout.setStyleName("inputFormFlexTable");
        FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();
        layout.setWidth("300px");
		
		final TextBox destination = new TextBox();
		final ListBox type = new ListBox();
		type.addItem("HOST","host");
        type.addItem("USER@HOST", "user@host");
        type.addItem("USER@HOST:PORT", "user@host:port");
        type.addItem("URL","url");
		type.addItem("MAIL","email");
        type.addItem("SIGNED MAIL","semail");

        TabMenu menu = new TabMenu();
        final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addDestination());

        final ListBoxWithObjects<Service> services = new ListBoxWithObjects<Service>();
		final CheckBox useHosts = new CheckBox(WidgetTranslation.INSTANCE.useFacilityHostnames(), false);
		useHosts.setTitle(WidgetTranslation.INSTANCE.useFacilityHostnamesTitle());
		
		type.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event) {
				// if hosts - checkbox visible
				if (type.getSelectedIndex() == 0) {
					useHosts.setVisible(true);
				} else {
					useHosts.setVisible(false);
					useHosts.setValue(false);
					destination.setEnabled(true);
				}
			}
		});
		
		useHosts.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (useHosts.getValue() == true) {
					destination.setEnabled(false);
				} else {
					destination.setEnabled(true);
				}
			}
		});
		
		// fills services listbox with assigned services
		final JsonCallbackEvents fillAssignedServices = new JsonCallbackEvents(){
			public void onLoadingStart() {
                services.removeAllOption();
                services.clear();
                services.addItem("Loading...");
                addButton.setEnabled(false);
            }
            public void onFinished(JavaScriptObject jso) {
				services.removeAllOption();
				services.clear();
				ArrayList<Service> serv = JsonUtils.jsoAsList(jso);
				if (serv.size() == 0) {
					services.addItem("No service available");
				} else {
					serv = new TableSorter<Service>().sortByName(serv);
					for (int i=0; i<serv.size(); i++) {
						services.addItem(serv.get(i));
					}
					services.addAllOption();
				}
                addButton.setEnabled(true);
			}
            public void onError(PerunError error) {
                services.removeAllOption();
                services.clear();
                services.addItem("Error while loading");
                addButton.setEnabled(false);
            }
		};
		final GetFacilityAssignedServices callback = new GetFacilityAssignedServices(facility.getId(), fillAssignedServices);
		callback.retrieveData();
		
		int row = 0;
		layout.setHTML(row, 0, "Facility:");
		layout.setHTML(row, 1, facility.getName());
		row++;
		
		layout.setHTML(row, 0, "Service:");
		layout.setWidget(row, 1, services);
		row++;
		
		// display all services
		final CheckBox allServicesCheckbox = new CheckBox(WidgetTranslation.INSTANCE.displayAllServices(), false);
		allServicesCheckbox.setTitle(WidgetTranslation.INSTANCE.displayAllServicesTitle());
		allServicesCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				 
				boolean allServices = event.getValue();
				
				if(!allServices){
					GetFacilityAssignedServices callback = new GetFacilityAssignedServices(facility.getId(), fillAssignedServices);
					callback.retrieveData();
				}else{
					GetServices callback = new GetServices(fillAssignedServices);
					callback.retrieveData();
				}
				
			}
		});
		layout.setWidget(row, 1, allServicesCheckbox);
		row++;
		
		layout.setHTML(row, 0, "Destination:");
		layout.setWidget(row, 1, destination);
		row++;
		
		layout.setWidget(row, 1, useHosts);
		row++;
		
		layout.setHTML(row, 0, "Type:");
		layout.setWidget(row, 1, type);
		row++;

        for (int i=0; i<layout.getRowCount(); i++) {
            cellFormatter.addStyleName(i, 0, "itemName");
        }

        // close tab, disable button
		final JsonCallbackEvents closeTabEvents = JsonCallbackEvents.closeTabDisableButtonEvents(addButton, this);
				
		addButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				if (services.isEmpty()) {
					// no services available
					Confirm c = new Confirm("No service selected.", new Label("You must select a service to add destination for."), true);
					c.show();
				}
				if (destination.getText().equalsIgnoreCase("") && useHosts.getValue() == false) {
					Confirm c = new Confirm("Wrong value", new Label("'Destination' can't be empty."), true);
					c.show();
					return;
				}
				if (services.getSelectedIndex() == 0) {
					// selected all
					if (useHosts.getValue() == true){
						// auto by hosts
						AddDestinationsByHostsOnFacility request = new AddDestinationsByHostsOnFacility(facility, closeTabEvents);
						request.addDestinationByHosts(services.getAllObjects());
					} else {
						// default
                        AddDestination request = new AddDestination(facility, closeTabEvents);
                        request.addDestination(destination.getText().trim(), type.getValue(type.getSelectedIndex()), services.getAllObjects());
					}
				} else {
					// selected one
					if (useHosts.getValue() == true){
						// auto by hosts
						AddDestinationsByHostsOnFacility request = new AddDestinationsByHostsOnFacility(facility, closeTabEvents);
						request.addDestinationByHosts(services.getSelectedObject());
					} else {
						// default
						AddDestination request = new AddDestination(facility, closeTabEvents);
						request.addDestination(destination.getText().trim(), type.getValue(type.getSelectedIndex()), services.getSelectedObject());
					}
				}
			}
		});
        menu.addWidget(addButton);

        final TabItem tab = this;
        menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));

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
		return SmallIcons.INSTANCE.serverGoIcon(); 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + facilityId;
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
		AddFacilityDestinationTabItem other = (AddFacilityDestinationTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}
	
	public void open() {
	}
	
	public boolean isAuthorized() {

		if (session.isFacilityAdmin(facilityId)) {
			return true; 
		} else {
			return false;
		}

	}

}