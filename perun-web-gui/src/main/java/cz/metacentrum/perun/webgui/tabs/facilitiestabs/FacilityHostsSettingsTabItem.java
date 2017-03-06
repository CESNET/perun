package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

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
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesV2;
import cz.metacentrum.perun.webgui.json.attributesManager.RemoveAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.json.facilitiesManager.GetHosts;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.Host;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.attributestabs.SetNewAttributeTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides page with hosts settings (manage hosts attributes)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilityHostsSettingsTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading facility");

	// data
	private int facilityId = 0;
	private Facility facility;
	private int lastSelectedHostId = 0;

	/**
	 * Creates a tab instance
	 * @param facilityId
	 */
	public FacilityHostsSettingsTabItem(int facilityId, int hostId){
		this.facilityId = facilityId;
		this.lastSelectedHostId = hostId;
		new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				facility = jso.cast();
			}
		}).retrieveData();
	}


	public boolean isPrepared(){
		return !(facility == null);
	}

	/**
	 * Creates a tab instance
	 * @param facility
	 */
	public FacilityHostsSettingsTabItem(Facility facility, Host host){
		this.facilityId = facility.getId();
		this.facility = facility;
		if (host != null) {
			this.lastSelectedHostId = host.getId();
		}
	}

	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": Hosts settings");

		// main panel
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%","100%");

		final GetAttributesV2 attrs = new GetAttributesV2();

		final ListBoxWithObjects<Host> listbox = new ListBoxWithObjects<Host>();
		// refresh attributes for hosts
		listbox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if (listbox.getSelectedObject() != null) {
					lastSelectedHostId = listbox.getSelectedObject().getId();
					attrs.getHostAttributes(lastSelectedHostId);
					attrs.retrieveData();
				} else {
					lastSelectedHostId = 0;
				}
			}
		});

		// retrieve hosts
		final GetHosts hosts = new GetHosts(facilityId, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				listbox.clear();
				ArrayList<Host> result = JsonUtils.jsoAsList(jso);
				if (result != null && !result.isEmpty()) {
					for (Host h : result) {
						listbox.addItem(h);
						if (h.getId() == lastSelectedHostId) {
							listbox.setSelected(h, true);
						}
					}
					if (lastSelectedHostId == 0) {
						lastSelectedHostId = listbox.getSelectedObject().getId();
					}
					attrs.getHostAttributes(lastSelectedHostId);
					attrs.retrieveData();
				}
			}
		@Override
		public void onError(PerunError error) {
			listbox.clear();
			listbox.addItem("Error while loading");
		}
		@Override
		public void onLoadingStart() {
			listbox.clear();
			listbox.addItem("Loading...");
		}
		});
		hosts.retrieveData();

		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(attrs);

		// menu
		TabMenu menu = new TabMenu();

		// Save changes button
		final CustomButton saveChangesButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveChangesInAttributes());
		final JsonCallbackEvents saveChangesButtonEvent = JsonCallbackEvents.disableButtonEvents(saveChangesButton, events);
		saveChangesButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ArrayList<Attribute> list = attrs.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					Map<String, Integer> ids = new HashMap<String, Integer>();
					ids.put("host", lastSelectedHostId);
					SetAttributes request = new SetAttributes(saveChangesButtonEvent);
					request.setAttributes(ids, list);
				}
			}
		});

		// Remove attr button
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeAttributes());
		final JsonCallbackEvents removeButtonEvent = JsonCallbackEvents.disableButtonEvents(removeButton, events);
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ArrayList<Attribute> list = attrs.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					Map<String, Integer> ids = new HashMap<String,Integer>();
					ids.put("host", lastSelectedHostId);
					RemoveAttributes request = new RemoveAttributes(removeButtonEvent);
					request.removeAttributes(ids, list);
				}
			}
		});

		menu.addWidget(UiElements.getRefreshButton(this));

		menu.addWidget(saveChangesButton);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.setNewAttributes(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("host", lastSelectedHostId);
				session.getTabManager().addTabToCurrentTab(new SetNewAttributeTabItem(ids, attrs.getList()), true);
			}
		}));

		menu.addWidget(removeButton);

		menu.addWidget(new HTML("<strong>Select host:</strong>"));
		menu.addWidget(listbox);

		// attrs table
		CellTable<Attribute> table = attrs.getEmptyTable();

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		// add menu and the table to the main panel
		firstTabPanel.add(menu);
		firstTabPanel.setCellHeight(menu, "30px");
		firstTabPanel.add(sp);

		session.getUiElements().resizePerunTable(sp, 350, this);

		this.contentWidget.setWidget(firstTabPanel);

		return getWidget();

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return  SmallIcons.INSTANCE.serverIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 739;
		int result = 1;
		result = prime * result + facilityId + lastSelectedHostId;
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
		FacilityHostsSettingsTabItem other = (FacilityHostsSettingsTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		if (lastSelectedHostId != other.lastSelectedHostId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(facility, "Hosts settings", getUrlWithParameters());
		if(facility != null) {
			session.setActiveFacility(facility);
		} else {
			session.setActiveFacilityId(facilityId);
		}
	}

	public boolean isAuthorized() {

		if (session.isFacilityAdmin(facilityId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "hostssettings";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facilityId+"&hid="+lastSelectedHostId;
	}

	static public FacilityHostsSettingsTabItem load(Map<String, String> parameters) {
		int fid = Integer.parseInt(parameters.get("id"));
		int hid = Integer.parseInt(parameters.get("hid"));
		return new FacilityHostsSettingsTabItem(fid, hid);
	}

}
