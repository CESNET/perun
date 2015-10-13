package cz.metacentrum.perun.webgui.tabs.securitytabs;

import com.google.gwt.cell.client.FieldUpdater;
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
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.authzResolver.AddAdmin;
import cz.metacentrum.perun.webgui.json.securityTeamsManager.AddUserToBlacklist;
import cz.metacentrum.perun.webgui.json.usersManager.FindCompleteRichUsers;
import cz.metacentrum.perun.webgui.json.usersManager.FindUsersByIdsNotInRpc;
import cz.metacentrum.perun.webgui.model.SecurityTeam;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * !! USE AS INNER TAB ONLY !!
 *
 * Provides page with add admin to VO form
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class AddUserToBlacklistTabItem implements TabItem {

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
	private Label titleWidget = new Label("Add to blacklist");

	/**
	 * Entity ID to set
	 */
	private int securityTeamId = 0;
	private SecurityTeam securityTeam;
	private FindCompleteRichUsers users;
	private ArrayList<User> alreadyAddedList = new ArrayList<User>();
	private SimplePanel alreadyAdded = new SimplePanel();

	private String searchString = "";


	/**
	 * Creates a tab instance
	 *
	 * @param securityTeamId ID of SecurityTeam to add admin into
	 */
	public AddUserToBlacklistTabItem(int securityTeamId){
		this.securityTeamId = securityTeamId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				securityTeam = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.SECURITY_TEAM, securityTeamId, events).retrieveData();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param securityTeam SecurityTeam to add admin into
	 */
	public AddUserToBlacklistTabItem(SecurityTeam securityTeam){
		this.securityTeamId = securityTeam.getId();
		this.securityTeam = securityTeam;
	}


	public boolean isPrepared(){
		return securityTeam != null;
	}

	public Widget draw() {

		titleWidget.setText("Add to blacklist");

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

		rebuildAlreadyAddedWidget();

		final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedUsersToBlacklist());
		final TabItem tab = this;

		// search textbox
		final ExtendedTextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				startSearching(text);
				searchString = text;
			}
		}, searchButton);

		tabMenu.addWidget(addButton);

		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, !alreadyAddedList.isEmpty());
			}
		}));

		addButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final ArrayList<User> list = users.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)){

					FlexTable layout = new FlexTable();
					layout.setStyleName("inputFormFlexTable");
					final TextArea textArea = new TextArea();
					textArea.setSize("250px", "120px");

					layout.getFlexCellFormatter().addStyleName(0, 0, "itemName");
					layout.setHTML(0, 0, "Reason:");
					layout.setWidget(1, 0, textArea);
					layout.setHTML(2, 0, "Please specify why users are blacklisted.");
					layout.getFlexCellFormatter().addStyleName(2, 0, "inputFormInlineComment");

					Confirm c = new Confirm("Add user(s) to blacklist", layout, new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							for (int i=0; i<list.size(); i++) {
								// FIXME - Should have only one callback to core
								final int n = i;
								AddUserToBlacklist request = new AddUserToBlacklist(securityTeamId, JsonCallbackEvents.disableButtonEvents(addButton, new JsonCallbackEvents(){
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
								request.addUserToBlacklist(list.get(i).getId(), textArea.getText().trim());

							}
						}
					}, "Add", true);
					c.show();
				}
			}
		});

		// if some text has been searched before
		if(!searchString.equals("")) {
			searchBox.getTextBox().setText(searchString);
			startSearching(searchString);
		}

		addButton.setEnabled(false);
		JsonUtils.addTableManagedButton(users, table, addButton);

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		// add menu and the table to the main panel
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");
		firstTabPanel.add(alreadyAdded);
		firstTabPanel.add(sp);

		session.getUiElements().resizePerunTable(sp, 350, this);

		this.contentWidget.setWidget(firstTabPanel);

		return getWidget();

	}

	/**
	 * Starts the search for users
	 */
	protected void startSearching(String text){

		users.clearTable();

		// IS searched string IDs?
		if (JsonUtils.isStringWithIds(text)) {

			FindUsersByIdsNotInRpc req = new FindUsersByIdsNotInRpc(new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso){
					ArrayList<User> usersList = JsonUtils.jsoAsList(jso);
					for (User u : usersList) {
						users.addToTable(u);
					}
				}
			}, text);

			req.retrieveData();
			return;

		}

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
			alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML()+ ((i!=0) ? ", " : "") + alreadyAddedList.get(i).getFullName());
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
		final int prime = 1297;
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

		AddUserToBlacklistTabItem create = (AddUserToBlacklistTabItem) obj;
		if (securityTeamId != create.securityTeamId){
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() { }

	public boolean isAuthorized() {

		if (session.isSecurityAdmin(securityTeamId)) {
			return true;
		} else {
			return false;
		}

	}

}
