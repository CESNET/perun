package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SelectionChangeEvent;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.authzResolver.AddAdmin;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllGroups;
import cz.metacentrum.perun.webgui.json.vosManager.GetVos;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * !! USE AS INNER TAB ONLY !!
 *
 * Provides page with add admin facility to Facility form
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddFacilityManagerGroupTabItem implements TabItem {

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
	private Label titleWidget = new Label("Add facility manager");

	/**
	 * Entity ID to set
	 */
	private int facilityId = 0;
	private Facility facility;
	private GetAllGroups getAllGroups;
	private JsonCallbackEvents refreshEvents;

	/**
	 * Creates a tab instance
	 *
	 * @param facilityId ID of Facility to add admin into
	 * @param refreshEvents events to trigger on finish
	 */
	public AddFacilityManagerGroupTabItem(int facilityId, JsonCallbackEvents refreshEvents){
		this.facilityId = facilityId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				facility = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.GROUP, facilityId, events).retrieveData();
		this.refreshEvents = refreshEvents;
	}

	/**
	 * Creates a tab instance
	 *
	 * @param facility Facility to add admin into
	 * @param refreshEvents events to trigger on finish
	 */
	public AddFacilityManagerGroupTabItem(Facility facility, JsonCallbackEvents refreshEvents){
		this.facilityId = facility.getId();
		this.facility = facility;
		this.refreshEvents = refreshEvents;
	}


	public boolean isPrepared(){
		return facility != null;
	}

	public Widget draw() {

		titleWidget.setText("Add manager group");

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// HORIZONTAL MENU
		final TabMenu tabMenu = new TabMenu();
		final ListBoxWithObjects<VirtualOrganization> box = new ListBoxWithObjects<VirtualOrganization>();

		// pass empty items to menu to ensure drawing of rest
		tabMenu.addWidget(new HTML(""));
		tabMenu.addWidget(new HTML(""));
		tabMenu.addWidget(2, new HTML("<strong>Select VO:</strong>"));
		tabMenu.addWidget(3, box);

		// get the table
		final ScrollPanel sp = new ScrollPanel();
		sp.addStyleName("perun-tableScrollPanel");

		box.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				sp.setWidget(fillGroupsContent(new GetAllGroups(box.getSelectedObject().getId()), tabMenu, box));
			}
		});

		if (box.getAllObjects().isEmpty()) {
			GetVos vos = new GetVos(new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					box.clear();
					ArrayList<VirtualOrganization> list = new TableSorter<VirtualOrganization>().sortByName(JsonUtils.<VirtualOrganization>jsoAsList(jso));
					if (list != null && !list.isEmpty()) {
						box.addAllItems(list);
						sp.setWidget(fillGroupsContent(new GetAllGroups(box.getSelectedObject().getId()), tabMenu, box));
					} else {
						box.addItem("No VOs found");
					}
				}
			@Override
			public void onError(PerunError error) {
				box.clear();
				box.addItem("Error while loading");
			}
			@Override
			public void onLoadingStart() {
				box.clear();
				box.addItem("Loading...");
			}
			});
			vos.retrieveData();
		}

		final TabItem tab = this;
		tabMenu.addWidget(1, TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				if (refreshEvents != null) refreshEvents.onFinished(null);
				session.getTabManager().closeTab(tab, false);
			}
		}));

		// add menu and the table to the main panel
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");
		firstTabPanel.add(sp);

		session.getUiElements().resizePerunTable(sp, 350, this);

		this.contentWidget.setWidget(firstTabPanel);

		return getWidget();

	}

	private Widget fillGroupsContent(GetAllGroups groups, TabMenu tabMenu, final ListBoxWithObjects<VirtualOrganization> box) {

		getAllGroups = groups;
		getAllGroups.setCoreGroupsCheckable(true);
		final CellTable<Group> table = getAllGroups.getTable();

		getAllGroups.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			private boolean found = false;
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				for (Group g : getAllGroups.getTableSelectedList()) {
					if (g.isCoreGroup()) {
						if (!found) {
							// display only once
							UiElements.generateInfo("You have selected 'all vo members' group", "If this group will be added as 'manager group', all new members of VO "+box.getSelectedObject().getName()+" will be automatically managers of your Facility and all removed members will lose management rights.");
						}
						found = true;
						return;
					}
				}
				found = false;
			}
		});

		final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedManagersGroupToGroup());
		tabMenu.addWidget(0, addButton);
		final TabItem tab = this;
		addButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				ArrayList<Group> list = getAllGroups.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)){
					for (int i=0; i<list.size(); i++) {
						if (i == list.size() - 1) {
							AddAdmin request = new AddAdmin(JsonCallbackEvents.disableButtonEvents(addButton, new JsonCallbackEvents(){
								public void onFinished(JavaScriptObject jso) {
									// close tab and refresh table
									if (refreshEvents != null) refreshEvents.onFinished(null);
									session.getTabManager().closeTab(tab, false);
								}
							}));
							request.addFacilityAdminGroup(facility, list.get(i));
						} else {
							AddAdmin request = new AddAdmin(JsonCallbackEvents.disableButtonEvents(addButton));
							request.addFacilityAdminGroup(facility, list.get(i));
						}
					}
				}
			}
		});

		addButton.setEnabled(false);
		JsonUtils.addTableManagedButton(getAllGroups, table, addButton);

		// add a class to the table and wrap it into scroll panel
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
		return SmallIcons.INSTANCE.addIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 643;
		int result = 1;
		result = prime * result + 6786786;
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
		AddFacilityManagerGroupTabItem create = (AddFacilityManagerGroupTabItem) obj;
		if (facilityId != create.facilityId){
			return false;
		}
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() { }

	public boolean isAuthorized() {

		if (session.isFacilityAdmin(facilityId)) {
			return true;
		} else {
			return false;
		}

	}

}
