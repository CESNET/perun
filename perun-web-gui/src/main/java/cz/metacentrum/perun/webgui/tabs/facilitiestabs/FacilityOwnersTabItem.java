package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

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
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.facilitiesManager.GetFacilityOwners;
import cz.metacentrum.perun.webgui.json.facilitiesManager.RemoveOwner;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.Owner;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab with list of owners of facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilityOwnersTabItem implements TabItem, TabItemWithUrl{

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

	/**
	 * Creates a tab instance
	 * @param facility facility to get allowed Vos from
	 */
	public FacilityOwnersTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param facilityId facility ID to get allowed Vos from
	 */
	public FacilityOwnersTabItem(int facilityId){
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

		// TITLE
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": Owners");

		// CONTENT
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// MENU
		TabMenu menu = new TabMenu();

		// CALLBACK
		final GetFacilityOwners jsonCallback = new GetFacilityOwners(facility);

		// AUTHZ
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));

		// add button
		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addNewOwners());
		addButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddFacilityOwnerTabItem(facility), true);
			}
		});

		// remove button
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeSelectedOwners());
		removeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final ArrayList<Owner> list = jsonCallback.getTableSelectedList();
				UiElements.showDeleteConfirm(list, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i=0; i<list.size(); i++) {
							if (i == list.size()-1) {
								RemoveOwner request = new RemoveOwner(JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(jsonCallback)));
								request.removeFacilityOwner(facilityId, list.get(i).getId());
							} else {
								RemoveOwner request = new RemoveOwner(JsonCallbackEvents.disableButtonEvents(removeButton));
								request.removeFacilityOwner(facilityId, list.get(i).getId());
							}
						}
					}
				});
			}
		});

		menu.addWidget(addButton);
		menu.addWidget(removeButton);

		menu.addFilterWidget(new ExtendedSuggestBox(jsonCallback.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				jsonCallback.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterOwners());

		// TABLE
		CellTable<Owner> table = jsonCallback.getTable();
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(sp);

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(jsonCallback, table, removeButton);

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
		return SmallIcons.INSTANCE.userSilhouetteIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 757;
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
		FacilityOwnersTabItem other = (FacilityOwnersTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(facility, "Owners", getUrlWithParameters());
		if (facility != null){
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

	public final static String URL = "owners";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facilityId;
	}

	static public FacilityOwnersTabItem load(Facility facility)
	{
		return new FacilityOwnersTabItem(facility);
	}

	static public FacilityOwnersTabItem load(Map<String, String> parameters)
	{
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilityOwnersTabItem(fid);
	}

}
