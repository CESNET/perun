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
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.facilitiesManager.AssignSecurityTeam;
import cz.metacentrum.perun.webgui.json.securityTeamsManager.GetSecurityTeams;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.SecurityTeam;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Tab for adding new facility owner
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AssignSecurityTeamTabItem implements TabItem {

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
	 *
	 * @param facility Facility
	 */
	public AssignSecurityTeamTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	public boolean isPrepared(){
		return !(facility == null);
	}

	public Widget draw() {

		// TITLE
		titleWidget.setText("Add security team");

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu tabMenu = new TabMenu();

		// CALLBACK
		final GetSecurityTeams secTeams = new GetSecurityTeams();
		secTeams.setEvents(new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				if (secTeams.getList().size() == 1) {
					secTeams.getSelectionModel().setSelected(secTeams.getList().get(0), true);
				}
			}
		});
		secTeams.setForceAll(true);
		CellTable<SecurityTeam> table = secTeams.getTable();

		// ADD BUTTON
		final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.assignSecurityTeam());

		final TabItem tab = this; // tab to be closed

		addButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				// get
				ArrayList<SecurityTeam> list = secTeams.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
					for (int i=0; i<list.size(); i++) {
						if (i == list.size()-1) {
							AssignSecurityTeam request = new AssignSecurityTeam(JsonCallbackEvents.closeTabDisableButtonEvents(addButton, tab));
							request.assignSecurityTeam(facilityId, list.get(i).getId());
						} else {
							AssignSecurityTeam request = new AssignSecurityTeam(JsonCallbackEvents.disableButtonEvents(addButton));
							request.assignSecurityTeam(facilityId, list.get(i).getId());
						}
					}
				}
			}
		});

		tabMenu.addWidget(addButton);
		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, false);
			}
		}));

		tabMenu.addFilterWidget(new ExtendedSuggestBox(secTeams.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				secTeams.filterTable(text);
				if (secTeams.getList().size() == 1) {
					secTeams.getSelectionModel().setSelected(secTeams.getList().get(0), true);
				}
			}
		}, ButtonTranslation.INSTANCE.filterSecurityTeam());

		addButton.setEnabled(false);
		JsonUtils.addTableManagedButton(secTeams, table, addButton);

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		// add menu and the table to the main panel
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");
		firstTabPanel.add(sp);

		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

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
		return SmallIcons.INSTANCE.addIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 653;
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
		AssignSecurityTeamTabItem other = (AssignSecurityTeamTabItem) obj;
		if (facilityId != other.facilityId)
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

		if (session.isFacilityAdmin(facilityId)) {
			return true;
		} else {
			return false;
		}

	}

}
