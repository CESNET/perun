package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.facilitiesManager.GetAssignedFacilities;
import cz.metacentrum.perun.webgui.json.servicesManager.BlockUnblockServiceOnDestination;
import cz.metacentrum.perun.webgui.json.servicesManager.GetAllRichDestinations;
import cz.metacentrum.perun.webgui.json.servicesManager.GetDestinations;
import cz.metacentrum.perun.webgui.json.servicesManager.GetRichDestinations;
import cz.metacentrum.perun.webgui.json.servicesManager.RemoveDestination;
import cz.metacentrum.perun.webgui.model.Destination;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.ServicesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.AddFacilityDestinationTabItem;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.Map;

/**
 * Provides page with service's destinations management
 *
 * You can select a facility on page or view destinations for all facilities
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class ServiceDestinationsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Manage destinations");

	// data
	private Service service;
	private int serviceId;
	private int lastSelectedFacilityId = 0;

	/**
	 * Provides page with service's destinations management
	 *
	 * You can select a facility on page or view destinations for all facilities
	 *
	 * @param service Service to get destinations for
	 */
	public ServiceDestinationsTabItem(Service service){
		this.service = service;
		this.serviceId = service.getId();
	}

	/**
	 * Provides page with service's destinations management
	 *
	 * You can select a facility on page or view destinations for all facilities
	 *
	 * @param serviceId Service to get destinations for
	 */
	public ServiceDestinationsTabItem(int serviceId){
		this.serviceId = service.getId();
		new GetEntityById(PerunEntity.SERVICE, serviceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				service = jso.cast();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return !(service == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		this.titleWidget.setText("Service destinations");

		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));

		// Adds menu to the main panel
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		// buttons
		final CustomButton addDestButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addDestination());
		final CustomButton removeDestButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeSelectedDestinations());

		menu.addWidget(addDestButton);
		menu.addWidget(removeDestButton);

		final CustomButton blockButton = new CustomButton(ButtonTranslation.INSTANCE.blockPropagationButton(), ButtonTranslation.INSTANCE.blockServicesOnFacility(), SmallIcons.INSTANCE.stopIcon());
		final CustomButton allowButton = new CustomButton(ButtonTranslation.INSTANCE.allowPropagationButton(), ButtonTranslation.INSTANCE.allowServicesOnFacility(), SmallIcons.INSTANCE.acceptIcon());

		menu.addWidget(allowButton);
		menu.addWidget(blockButton);
		menu.addWidget(new HTML("<strong>Selected facility:</strong>"));

		// listbox with facilities
		final ListBoxWithObjects<Facility> ls = new ListBoxWithObjects<Facility>();
		menu.addWidget(ls);

		// get empty table used for destinations
		final GetAllRichDestinations callback = new GetAllRichDestinations(null, service);
		callback.showFacilityColumn(true);
		callback.showServiceColumn(false);
		final CellTable<Destination> table = callback.getEmptyTable(); // do not make callback yet
		if (lastSelectedFacilityId == 0) {
			callback.retrieveData();
		}

		// refresh events called when selection changes or callback ends
		final JsonCallbackEvents refreshEvents = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				if (ls.getSelectedIndex() == 0) {
					// fills table with destinations of all facilities
					callback.clearTable();
					callback.retrieveData();
				} else {
					callback.clearTable();
					((AjaxLoaderImage)table.getEmptyTableWidget()).loadingStart();
					// fills table with destinations of selected facility
					JsonCallbackEvents localEvents = new JsonCallbackEvents(){
						@Override
						public void onFinished(JavaScriptObject jso){
							ArrayList<Destination> dest = JsonUtils.jsoAsList(jso);
							for (Destination d : dest) {
								//d.setFacility(ls.getSelectedObject());
								//d.setService(service);
								callback.addToTable(d);
							}
							((AjaxLoaderImage)table.getEmptyTableWidget()).loadingFinished();
						}
						@Override
						public void onError(PerunError error){
							((AjaxLoaderImage)table.getEmptyTableWidget()).loadingError(error);
						}
					};
					final GetRichDestinations callback = new GetRichDestinations(ls.getSelectedObject(), service, localEvents);
					callback.retrieveData();
				}
			}
		};

		// fills listbox and table with dest. for all service facilities
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				ls.clear();
				ArrayList<Facility> facs = JsonUtils.jsoAsList(jso);
				facs = new TableSorter<Facility>().sortByName(facs);
				// if no facility
				if (facs.size() == 0) {
					ls.addItem("No facility available");
					return;
				}
				for (int i=0; i<facs.size(); i++){
					ls.addItem(facs.get(i));
					if (facs.get(i).getId() == lastSelectedFacilityId) {
						ls.setSelected(facs.get(i), true);
					}
				}
				ls.addAllOption();
				if (lastSelectedFacilityId == 0) {
					// select all
					ls.setItemSelected(0, true);
				} else {
					// was selected
					addDestButton.setEnabled(true);
					refreshEvents.onFinished(null);
				}
			}
			public void onError(PerunError error){
				ls.addItem("Error while loading");
				addDestButton.setEnabled(false);
			}
			public void onLoadingStart(){
				ls.clear();
				ls.addItem("Loading...");
				addDestButton.setEnabled(false);
			}
		};
		final GetAssignedFacilities assignedFacilities = new GetAssignedFacilities(PerunEntity.SERVICE, serviceId, events);
		assignedFacilities.retrieveData();



		ls.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				if (ls.getSelectedIndex() > 0) {
					// store last selected facility id
					addDestButton.setEnabled(true);
					lastSelectedFacilityId = ls.getSelectedObject().getId();
				} else {
					addDestButton.setEnabled(false);
					lastSelectedFacilityId = 0;
				}
				refreshEvents.onFinished(null);
			}
		});

		// CLICK HANDLERS FOR BUTTONS

		addDestButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddFacilityDestinationTabItem(ls.getSelectedObject()));
			}
		});

		removeDestButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				// get
				final ArrayList<Destination> destsToRemove = callback.getTableSelectedList();
				UiElements.showDeleteConfirm(destsToRemove, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
						for (int i = 0; i < destsToRemove.size(); i++) {
							if (i == destsToRemove.size() - 1) {
								RemoveDestination request = new RemoveDestination(destsToRemove.get(i).getFacility().getId(), service.getId(), JsonCallbackEvents.disableButtonEvents(removeDestButton, refreshEvents));
								request.removeDestination(destsToRemove.get(i).getDestination(), destsToRemove.get(i).getType());
							} else {
								RemoveDestination request = new RemoveDestination(destsToRemove.get(i).getFacility().getId(), service.getId(), JsonCallbackEvents.disableButtonEvents(removeDestButton));
								request.removeDestination(destsToRemove.get(i).getDestination(), destsToRemove.get(i).getType());
							}
						}
					}
				});
			}
		});


		allowButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				final ArrayList<Destination> destForBlockUnblock = callback.getTableSelectedList();
				// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
				for (int i=0; i<destForBlockUnblock.size(); i++ ) {
					if (i == destForBlockUnblock.size()-1) {
						BlockUnblockServiceOnDestination request = new BlockUnblockServiceOnDestination(JsonCallbackEvents.disableButtonEvents(allowButton, refreshEvents));
						request.unblockServiceOnDestination(destForBlockUnblock.get(i).getService().getId(), destForBlockUnblock.get(i).getId());
					} else {
						BlockUnblockServiceOnDestination request = new BlockUnblockServiceOnDestination(JsonCallbackEvents.disableButtonEvents(allowButton));
						request.unblockServiceOnDestination(destForBlockUnblock.get(i).getService().getId(), destForBlockUnblock.get(i).getId());
					}
				}
			}
		});
		blockButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				final ArrayList<Destination> destForBlockUnblock = callback.getTableSelectedList();
				// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
				for (int i=0; i<destForBlockUnblock.size(); i++ ) {
					if (i == destForBlockUnblock.size()-1) {
						BlockUnblockServiceOnDestination request = new BlockUnblockServiceOnDestination(JsonCallbackEvents.disableButtonEvents(blockButton, refreshEvents));
						request.blockServiceOnDestination(destForBlockUnblock.get(i).getService().getId(), destForBlockUnblock.get(i).getId());
					} else {
						BlockUnblockServiceOnDestination request = new BlockUnblockServiceOnDestination(JsonCallbackEvents.disableButtonEvents(blockButton));
						request.blockServiceOnDestination(destForBlockUnblock.get(i).getService().getId(), destForBlockUnblock.get(i).getId());
					}
				}
			}
		});

		// filter box
		menu.addFilterWidget(new ExtendedSuggestBox(callback.getOracle()), new PerunSearchEvent() {
			public void searchFor(String text) {
				callback.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterDestinationByFacility());

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(sp);
		session.getUiElements().resizePerunTable(sp, 350, this);

		blockButton.setEnabled(false);
		allowButton.setEnabled(false);
		removeDestButton.setEnabled(false);
		JsonUtils.addTableManagedButton(callback, table, blockButton);
		JsonUtils.addTableManagedButton(callback, table, allowButton);
		JsonUtils.addTableManagedButton(callback, table, removeDestButton);

		// add tabs to the main panel
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
		final int prime = 1069;
		int result = 1;
		result = prime * result + serviceId;
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
		ServiceDestinationsTabItem other = (ServiceDestinationsTabItem) obj;
		if (serviceId != other.serviceId)
			return false;
		return true;
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

	public final static String URL = "srv-dest";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return ServicesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + serviceId;
	}

	static public ServiceDestinationsTabItem load(Map<String, String> parameters)
	{
		int id = Integer.parseInt(parameters.get("id"));
		return new ServiceDestinationsTabItem(id);
	}

}
