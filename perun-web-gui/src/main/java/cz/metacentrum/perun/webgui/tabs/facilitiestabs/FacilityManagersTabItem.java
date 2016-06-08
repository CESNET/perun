package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.cell.client.FieldUpdater;
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
import cz.metacentrum.perun.webgui.json.authzResolver.GetAdminGroups;
import cz.metacentrum.perun.webgui.json.authzResolver.GetRichAdminsWithAttributes;
import cz.metacentrum.perun.webgui.json.authzResolver.RemoveAdmin;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab with list of administrators on facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilityManagersTabItem implements TabItem, TabItemWithUrl{

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
	private int facilityId;
	private Facility facility;
	private int selectedDropDownIndex = 0;

	/**
	 * Creates a tab instance
	 *
	 * @param facility Facility
	 */
	public FacilityManagersTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param facilityId ID of facility
	 */
	public FacilityManagersTabItem(int facilityId){
		this.facilityId = facilityId;
		new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				facility = jso.cast();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return !(facility == null);
	}

	public Widget draw() {

		// set title
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": Managers");

		// content
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// HORIZONTAL MENU
		final TabMenu menu = new TabMenu();

		final ListBox box = new ListBox();
		box.addItem("Users");
		box.addItem("Groups");
		box.setSelectedIndex(selectedDropDownIndex);

		final ScrollPanel sp = new ScrollPanel();
		sp.addStyleName("perun-tableScrollPanel");

		// request
		final GetRichAdminsWithAttributes admins = new GetRichAdminsWithAttributes(PerunEntity.FACILITY, facilityId, null);
		final GetAdminGroups adminGroups = new GetAdminGroups(PerunEntity.FACILITY, facilityId);

		box.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {

				if (box.getSelectedIndex() == 0) {
					selectedDropDownIndex = 0;
					sp.setWidget(fillContentUsers(admins, menu));
				} else {
					selectedDropDownIndex = 1;
					sp.setWidget(fillContentGroups(adminGroups, menu));
				}

			}
		});

		if (selectedDropDownIndex == 0) {
			sp.setWidget(fillContentUsers(admins, menu));
		} else {
			sp.setWidget(fillContentGroups(adminGroups, menu));
		}

		menu.addWidget(3, new HTML("<strong>Select mode: </strong>"));
		menu.addWidget(4, box);
		menu.addWidget(5, new Image(SmallIcons.INSTANCE.helpIcon()));
		menu.addWidget(6, new HTML("<strong>People with privilege to manage this facility in Perun. They aren't automatically \"roots\" on machine.</strong>"));

		session.getUiElements().resizePerunTable(sp, 350, this);

		// add menu and the table to the main panel
		vp.add(menu);
		vp.setCellHeight(menu, "30px");
		vp.add(sp);

		this.contentWidget.setWidget(vp);

		return getWidget();

	}

	private Widget fillContentUsers(final GetRichAdminsWithAttributes jsonCallback, TabMenu menu) {

		jsonCallback.clearTableSelectedSet();

		// get the table
		CellTable<User> table;
		if (session.isPerunAdmin()) {
			table = jsonCallback.getTable(new FieldUpdater<User, String>() {
				@Override
				public void update(int i, User user, String s) {
					session.getTabManager().addTab(new UserDetailTabItem(user));
				}
			});
		} else {
			table = jsonCallback.getTable();
		}

		menu.addWidget(0, UiElements.getRefreshButton(this));

		menu.addWidget(1, TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addManagerToFacility(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddFacilityManagerTabItem(facility), true);
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeManagerFromFacility());
		menu.addWidget(2, removeButton);
		removeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final ArrayList<User> list = jsonCallback.getTableSelectedList();
				String text = "Following users won't be facility managers anymore and won't be able to manage this facility in Perun.";
				UiElements.showDeleteConfirm(list, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i=0; i<list.size(); i++) {
							if (i == list.size()-1) {
								RemoveAdmin request = new RemoveAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(jsonCallback)));
								request.removeFacilityAdmin(facility, list.get(i));
							} else {
								RemoveAdmin request = new RemoveAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(removeButton));
								request.removeFacilityAdmin(facility, list.get(i));
							}
						}
					}
				});
			}
		});

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(jsonCallback, table, removeButton);

		table.addStyleName("perun-table");

		return table;

	}

	private Widget fillContentGroups(final GetAdminGroups jsonCallback, TabMenu menu) {

		jsonCallback.clearTableSelectedSet();

		// get the table
		CellTable<Group> table = jsonCallback.getTable(new FieldUpdater<Group, String>() {
			@Override
			public void update(int i, Group grp, String s) {
				session.getTabManager().addTab(new GroupDetailTabItem(grp));
			}
		});

		menu.addWidget(0, UiElements.getRefreshButton(this));

		menu.addWidget(1, TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addManagerGroupToFacility(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddFacilityManagerGroupTabItem(facility, JsonCallbackEvents.refreshTableEvents(jsonCallback)), true);
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeManagerGroupFromFacility());
		menu.addWidget(2, removeButton);
		removeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final ArrayList<Group> list = jsonCallback.getTableSelectedList();
				String text = "Members of following groups won't be facility managers anymore and won't be able to manage this facility in Perun.";
				UiElements.showDeleteConfirm(list, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i=0; i<list.size(); i++) {
							if (i == list.size()-1) {
								RemoveAdmin request = new RemoveAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(jsonCallback)));
								request.removeFacilityAdminGroup(facility, list.get(i));
							} else {
								RemoveAdmin request = new RemoveAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(removeButton));
								request.removeFacilityAdminGroup(facility, list.get(i));
							}
						}
					}
				});
			}
		});

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(jsonCallback, table, removeButton);

		table.addStyleName("perun-table");

		return table;

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.administratorIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 751;
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
		FacilityManagersTabItem other = (FacilityManagersTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(facility, "Managers", getUrlWithParameters());
		if(facility != null) {
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

	public final static String URL = "managers";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facilityId;
	}

	static public FacilityManagersTabItem load(Facility facility)
	{
		return new FacilityManagersTabItem(facility);
	}

	static public FacilityManagersTabItem load(Map<String, String> parameters)
	{
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilityManagersTabItem(fid);
	}

}
