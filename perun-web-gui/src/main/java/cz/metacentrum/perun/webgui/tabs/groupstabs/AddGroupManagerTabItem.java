package cz.metacentrum.perun.webgui.tabs.groupstabs;

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
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Provides page with add admin to Group form
 *
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class AddGroupManagerTabItem implements TabItem {

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
	private Label titleWidget = new Label("Loading group");

	/**
	 * Entity ID to set
	 */
	private Group group;
	private int groupId;

	// when searching
	private String searchString = "";
	private FindCompleteRichUsers users;
	private ArrayList<User> alreadyAddedList = new ArrayList<User>();
	private SimplePanel alreadyAdded = new SimplePanel();

	/**
	 * Creates a tab instance
	 *
	 * @param group group to add admin into
	 */
	public AddGroupManagerTabItem(Group group){
		this.group = group;
		this.groupId = group.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param groupId ID of group to add admin into
	 */
	public AddGroupManagerTabItem(int groupId){
		this.groupId = groupId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				group = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.GROUP, groupId, events).retrieveData();
	}


	public boolean isPrepared() {
		return !(group == null);
	}

	public Widget draw() {

		titleWidget.setText("Add manager");

		// MAIN PANEL
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// if members or admins group, hide
		if(group.isCoreGroup()){

			vp.add(new HTML("<p>Group \""+group.getName()+"\" can't have managers managed from Group manager section. Please use VO manager section.</p>"));

			this.contentWidget.setWidget(vp);
			return getWidget();
		}

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
		tabMenu.addWidget(UiElements.getRefreshButton(this));

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

		final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedManagersToGroup());
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
				final ArrayList<User> list = users.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					for (int i = 0; i < list.size(); i++) {
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
						request.addGroupAdmin(group, list.get(i));
					}
				}
			}
		});
		tabMenu.addWidget(addButton);

		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, !alreadyAddedList.isEmpty());
			}
		}));

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

	/**
	 * Starts the search for users
	 */
	protected void startSearching(String text){

		users.clearTable();
		users.searchFor(text);
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
		final int prime = 797;
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

		AddGroupManagerTabItem create = (AddGroupManagerTabItem) obj;
		if (groupId != create.groupId){
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() { }

	public boolean isAuthorized() {

		if (session.isVoAdmin(group.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

}
