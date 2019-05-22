package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.authzResolver.AddAdmin;
import cz.metacentrum.perun.webgui.json.usersManager.FindCompleteRichUsers;
import cz.metacentrum.perun.webgui.json.usersManager.FindUsersByIdsNotInRpc;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Tab for adding new facility administrator
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddFacilityManagerTabItem implements TabItem {

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

	/**
	 * Search string
	 */
	private String searchString = "";
	private FindCompleteRichUsers users;

	// data
	private int facilityId;
	private Facility facility;
	private ArrayList<User> alreadyAddedList = new ArrayList<User>();
	private SimplePanel alreadyAdded = new SimplePanel();

	/**
	 * Creates a tab instance
	 *
	 * @param facility Facility
	 */
	public AddFacilityManagerTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param facilityId ID of facility
	 */
	public AddFacilityManagerTabItem(int facilityId){
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

	@Override
	public boolean isRefreshParentOnClose() {
		return !alreadyAddedList.isEmpty();
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		titleWidget.setText("Add manager");

		final CustomButton searchButton = new CustomButton("Search", ButtonTranslation.INSTANCE.searchUsers(), SmallIcons.INSTANCE.findIcon());
		this.users = new FindCompleteRichUsers("", null, JsonCallbackEvents.disableButtonEvents(searchButton, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				// if found 1 item, select
				ArrayList<User> list = JsonUtils.jsoAsList(jso);
				if (list != null && list.size() == 1) {
					users.getSelectionModel().setSelected(list.get(0), true);
				}
			}
		}));

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu tabMenu = new TabMenu();

		// get the table
		final CellTable<User> table;
		if (session.isPerunAdmin()) {
			table = users.getTable(new FieldUpdater<User, String>() {
				public void update(int i, User user, String s) {
					session.getTabManager().addTab(new UserDetailTabItem(user));
				}
			});
		} else {
			table = users.getTable();
		}

		final TabItem tab = this;

		// already added
		rebuildAlreadyAddedWidget();

		// search textbox
		final ExtendedTextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				startSearching(text);
				searchString = text;
			}
		}, searchButton);

		final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedManagersToFacility());

		addButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final ArrayList<User> list = users.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)){
					// proceed
					for (int i=0; i<list.size(); i++) {
						final int n = i;
						AddAdmin request = new AddAdmin(JsonCallbackEvents.disableButtonEvents(addButton, new JsonCallbackEvents(){
							@Override
							public void onFinished(JavaScriptObject jso) {
								// put names to already added
								alreadyAddedList.add(list.get(n));
								rebuildAlreadyAddedWidget();
								// unselect added person
								users.getSelectionModel().setSelected(list.get(n), false);
								// clear search
								searchBox.getTextBox().setText("");
							}
						}));
						request.addFacilityAdmin(facility, list.get(i));
					}
				}
			}
		});

		tabMenu.addWidget(addButton);

		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		// if some text has been searched before
		if(!searchString.equals("")) {
			searchBox.getTextBox().setText(searchString);
			startSearching(searchString);
		}

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		// add menu and the table to the main panel
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");
		firstTabPanel.add(alreadyAdded);
		firstTabPanel.add(sp);

		addButton.setEnabled(false);
		JsonUtils.addTableManagedButton(users, table, addButton);

		session.getUiElements().resizePerunTable(sp, 350, this);

		this.contentWidget.setWidget(firstTabPanel);

		return getWidget();

	}

	/**
	 * Starts the search for users
	 */
	protected void startSearching(String text){

		users.clearTable();
		users.searchFor(text);

	}

	/**
	 * Rebuild already added widget based on already added admins
	 */
	private void rebuildAlreadyAddedWidget() {

		alreadyAdded.setStyleName("alreadyAdded");
		alreadyAdded.setVisible(!alreadyAddedList.isEmpty());
		alreadyAdded.setWidget(new HTML("<strong>Already added: </strong>"));
		for (int i=0; i<alreadyAddedList.size(); i++) {
			alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML()+ ((i!=0) ? ", " : "") + SafeHtmlUtils.fromString(alreadyAddedList.get(i).getFullName()).asString());
		}

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
		final int prime = 647;
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
		AddFacilityManagerTabItem other = (AddFacilityManagerTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
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
