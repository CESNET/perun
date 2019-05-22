package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesV2;
import cz.metacentrum.perun.webgui.json.attributesManager.GetRequiredAttributesV2;
import cz.metacentrum.perun.webgui.json.attributesManager.RemoveAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.json.servicesManager.GetFacilityAssignedServices;
import cz.metacentrum.perun.webgui.json.servicesManager.GetServices;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.attributestabs.SetNewAttributeTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tab for setting attributes for specified services on facility also:
 * FACILITY ADMIN / PERUN ADMIN - Create facility wizard - page 2
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilitySettingsTabItem implements TabItem, TabItemWithUrl {

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
	private Facility facility;
	private int facilityId;
	private boolean hide = false;

	private int lastServiceId = 0;
	private int indexInList = 1;
	private boolean lastCheckBoxValue = true;

	final VerticalPanel vp = new VerticalPanel();
	ScrollPanel sp = new ScrollPanel();
	ScrollPanel sp2 = new ScrollPanel();
	// if required table is displayed
	boolean required = true;

	/**
	 * Creates a tab instance
	 *
	 * @param facility facility to get services from
	 */
	public FacilitySettingsTabItem(Facility facility) {
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param facilityId
	 */
	public FacilitySettingsTabItem(int facilityId) {
		this.facilityId = facilityId;
		new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso) {
				facility = jso.cast();
			}
		}).retrieveData();
	}

	public boolean isPrepared() {
		return !(facility == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public void hideServicesSwitch(boolean hide) {
		this.hide = hide;
		lastCheckBoxValue = !hide;
		indexInList = 0;
	}

	public Widget draw() {

		// set title
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName()) + ": Service settings");

		// content
		vp.setSize("100%", "100%");
		vp.clear();

		// HORIZONTAL MENU
		TabMenu menu = new TabMenu();

		// Get Attributes method
		final GetRequiredAttributesV2 reqAttrs = new GetRequiredAttributesV2();
		final GetAttributesV2 attrs = new GetAttributesV2();
		attrs.getFacilityAttributes(facilityId);

		// get empty table
		final CellTable<Attribute> table = reqAttrs.getEmptyTable();
		final CellTable<Attribute> table2 = attrs.getEmptyTable();
		sp.setWidget(table);
		sp2.setWidget(table2);

		// ids to retrieve data from rpc
		final Map<String, Integer> ids = new HashMap<String, Integer>();
		ids.put("facility", facility.getId());

		// service switcher checkbox
		final CheckBox switchServicesChb = new CheckBox(WidgetTranslation.INSTANCE.offerAvailableServicesOnly(), false);
		switchServicesChb.setValue(lastCheckBoxValue); // selected by default - unselected if switch hidden
		switchServicesChb.setTitle(WidgetTranslation.INSTANCE.offerAvailableServicesOnlyTitle());
		// services listbox
		final ListBoxWithObjects<Service> servList = new ListBoxWithObjects<Service>();

		// on change of service update table
		servList.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				if (servList.getSelectedIndex() == 0) {
					// show all facility attributes
					attrs.retrieveData();
					setTable(false);
					lastServiceId = 0;
					indexInList = 0;
					return;
				}
				if (switchServicesChb.getValue() == true && servList.getSelectedIndex() == 1) {
					// show required attrs for all assigned services
					ids.remove("service");
					lastServiceId = 0;
					indexInList = 1;
				} else if ((switchServicesChb.getValue() == true && servList.getSelectedIndex() > 1)
						|| (switchServicesChb.getValue() == false && servList.getSelectedIndex() > 0)) {
					// show required attrs for selected service
					// >0 when listing all services
					// >1 when listing assigned services
					ids.put("service", servList.getSelectedObject().getId());
					lastServiceId = servList.getSelectedObject().getId();
				}
				// load req attrs
				reqAttrs.setIds(ids);
				reqAttrs.clearTable();
				reqAttrs.retrieveData();
				setTable(true);
			}
		});

		// event which fills the listbox and call getRequiredAttributes
		final JsonCallbackEvents event = new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso) {
				servList.clear();
				ArrayList<Service> srv = JsonUtils.jsoAsList(jso);
				srv = new TableSorter<Service>().sortByName(srv);
				for (int i = 0; i < srv.size(); i++) {
					servList.addItem(srv.get(i));
				}
				// no services available - load all facility attrs
				if (servList.isEmpty()) {
					servList.addNotSelectedOption();
					lastServiceId = 0;
					indexInList = 0;
					attrs.retrieveData();
					setTable(false);
					return;
				}
				// offer only available
				if (switchServicesChb.getValue() == true) {
					servList.addNotSelectedOption();
					servList.addAllOption();
					if (lastServiceId == 0) {
						if (indexInList == 1) {
							// all
							servList.setSelectedIndex(1);
						} else if (indexInList == 0) {
							// not selected - load all fac attrs
							servList.setSelectedIndex(0);
							attrs.retrieveData();
							setTable(false);
							return;
						}
					}
				} else {
					// offer all services
					servList.addNotSelectedOption();
					if (lastServiceId == 0) {
						// if no service selected, load all fac attrs
						servList.setSelectedIndex(0);
						attrs.retrieveData();
						setTable(false);
						return;
					}
				}

				// if some service was selected
				if (lastServiceId != 0) {
					ids.remove("service"); // remove service since we can't be sure, it was loaded again
					servList.setSelectedIndex(1); // either all or first service in a list
					for (Service s : servList.getAllObjects()) {
						if (s.getId() == lastServiceId) {
							// if found, select it
							servList.setSelected(s, true);
							ids.put("service", lastServiceId);
							break;
						}
					}
				}
				// get required attrs for service
				reqAttrs.clearTable();
				reqAttrs.setIds(ids);
				reqAttrs.retrieveData();
				setTable(true);
			}

			@Override
			public void onError(PerunError error) {
				servList.clear();
				if (required) {
					((AjaxLoaderImage) table.getEmptyTableWidget()).loadingError(error);
				} else {
					((AjaxLoaderImage) table2.getEmptyTableWidget()).loadingError(error);
				}
				servList.addItem("Error while loading");
			}

			@Override
			public void onLoadingStart() {
				servList.removeAllOption();
				servList.removeNotSelectedOption();
				servList.clear();
				servList.addItem("Loading...");
			}
		};
		final GetServices allServices = new GetServices(event);
		final GetFacilityAssignedServices assignedServices = new GetFacilityAssignedServices(facility.getId(), event);

		// if hide and unchecked or just unchecked
		if (!lastCheckBoxValue) {
			allServices.retrieveData();
		} else {
			assignedServices.retrieveData();
		}

		// Save changes button
		final CustomButton saveChangesButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveChangesInAttributes());
		final JsonCallbackEvents refreshEvents = JsonCallbackEvents.refreshTableEvents(reqAttrs);
		final JsonCallbackEvents refreshEvents2 = JsonCallbackEvents.refreshTableEvents(attrs);
		final JsonCallbackEvents saveChangesButtonEvent = JsonCallbackEvents.disableButtonEvents(saveChangesButton, refreshEvents);
		final JsonCallbackEvents saveChangesButtonEvent2 = JsonCallbackEvents.disableButtonEvents(saveChangesButton, refreshEvents2);
		saveChangesButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ArrayList<Attribute> list = (required) ? reqAttrs.getTableSelectedList() : attrs.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					SetAttributes request = new SetAttributes((required) ? saveChangesButtonEvent : saveChangesButtonEvent2);
					request.setAttributes(ids, list);
				}
			}
		});

		// Remove attr button
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeAttributes());
		final JsonCallbackEvents removeButtonEvent = JsonCallbackEvents.disableButtonEvents(removeButton, refreshEvents);
		final JsonCallbackEvents removeButtonEvent2 = JsonCallbackEvents.disableButtonEvents(removeButton, refreshEvents2);
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ArrayList<Attribute> list = (required) ? reqAttrs.getTableSelectedList() : attrs.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					Map<String, Integer> ids = new HashMap<String, Integer>();
					ids.put("facility", facilityId);
					RemoveAttributes request = new RemoveAttributes((required) ? removeButtonEvent : removeButtonEvent2);
					request.removeAttributes(ids, list);
				}
			}
		});

		// switch serv checkbox
		switchServicesChb.addClickHandler(new ClickHandler() {
			// load proper set of services on click
			public void onClick(ClickEvent event) {
				lastCheckBoxValue = switchServicesChb.getValue();
				if (switchServicesChb.getValue() == true) {
					assignedServices.retrieveData();
				} else {
					allServices.retrieveData();
				}
			}
		});

		// allow to set new (currently unused facility attribute)
		CustomButton setNewAttributeButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.setNewAttributes(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("facility", facility.getId());
				session.getTabManager().addTabToCurrentTab(new SetNewAttributeTabItem(ids, (required) ? reqAttrs.getList() : attrs.getList()), true);
			}
		});

		menu.addWidget(UiElements.getRefreshButton(this));
		menu.addWidget(saveChangesButton);
		menu.addWidget(setNewAttributeButton);
		menu.addWidget(removeButton);
		menu.addWidget(new HTML("<strong>Service: </strong>"));
		menu.addWidget(servList);

		if (!hide) {
			menu.addWidget(switchServicesChb);
		}

		/* TODO - not yet implemented

			 CustomButton fillDefaultButton = new CustomButton("Fill default values", SmallIcons.INSTANCE.scriptGoIcon(), new ClickHandler(){
			 public void onClick(ClickEvent event) {
			 Window.alert("not yet implemented");
			 }
			 });
			 fillDefaultButton.setTitle("Fill default values for this service/facility - nothing is saved unless you click on 'Save changes'");
			 CustomButton checkValuesButton = new CustomButton("Check values", SmallIcons.INSTANCE.scriptGearIcon(), new ClickHandler(){
			 public void onClick(ClickEvent event) {
			 Window.alert("not yet implemented");
			 }
			 });
			 checkValuesButton.setTitle("Checks inserted values against current Perun state - nothing is saved unless you click on 'Save changes'");

			 menu.addWidget(fillDefaultButton);
			 menu.addWidget(checkValuesButton);

*/

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		table.setWidth("100%");
		table2.addStyleName("perun-table");
		table2.setWidth("100%");
		sp.addStyleName("perun-tableScrollPanel");
		session.getUiElements().resizePerunTable(sp, 350, this);
		sp2.addStyleName("perun-tableScrollPanel");
		session.getUiElements().resizePerunTable(sp2, 350, this);

		// add menu and the table to the main panel
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		// default is required attributes
		setTable(true);
		this.contentWidget.setWidget(vp);

		return getWidget();
	}

	private void setTable(boolean required) {

		if (vp.getWidgetCount() == 2) {
			vp.remove(1);
		}
		if (required) {
			vp.add(sp);

		} else {
			vp.add(sp2);
		}

		this.required = required;

		Scheduler.get().scheduleDeferred(new Command() {
			@Override
			public void execute() {
				UiElements.runResizeCommandsForCurrentTab();
			}
		});

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.settingToolsIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 769;
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
		FacilitySettingsTabItem other = (FacilitySettingsTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		return true;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(facility, "Services settings", getUrlWithParameters());
		if (facility != null) {
			session.setActiveFacility(facility);
		} else {
			session.setActiveFacilityId(facilityId);
		}
	}

	public boolean isAuthorized() {

		if (session.isFacilityAdmin(facility.getId())) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "settings";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facility.getId();
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	static public FacilitySettingsTabItem load(Facility facility) {
		return new FacilitySettingsTabItem(facility);
	}

	static public FacilitySettingsTabItem load(Map<String, String> parameters) {
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilitySettingsTabItem(fid);
	}

}
