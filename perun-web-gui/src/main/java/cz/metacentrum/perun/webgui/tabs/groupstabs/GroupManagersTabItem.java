package cz.metacentrum.perun.webgui.tabs.groupstabs;

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
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoManagersTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Group admins page for Group Admin
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GroupManagersTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading group managers");

	// data
	private Group group;
	private int groupId;
	private int selectedDropDownIndex = 0;

	/**
	 * Creates a tab instance
	 * @param group
	 */
	public GroupManagersTabItem(Group group){
		this.group = group;
		this.groupId = group.getId();
	}

	/**
	 * Creates a tab instance
	 * @param groupId
	 */
	public GroupManagersTabItem(int groupId){
		this.groupId = groupId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				group = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.GROUP, groupId, events).retrieveData();
	}


	public boolean isPrepared(){
		return !(group == null);
	}


	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName()) + ": managers");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		final ListBox box = new ListBox();
		box.addItem("Users");
		box.addItem("Groups");
		box.setSelectedIndex(selectedDropDownIndex);

		final ScrollPanel sp = new ScrollPanel();
		sp.addStyleName("perun-tableScrollPanel");

		// menu
		final TabMenu menu = new TabMenu();

		// group members
		final GetRichAdminsWithAttributes admins = new GetRichAdminsWithAttributes(PerunEntity.GROUP, groupId, null);
		final GetAdminGroups adminGroups = new GetAdminGroups(PerunEntity.GROUP, groupId);

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

		session.getUiElements().resizePerunTable(sp, 350, this);

		vp.add(menu);
		vp.setCellHeight(menu, "30px");
		vp.add(sp);
		sp.setStyleName("perun-tableScrollPanel");

		this.contentWidget.setWidget(vp);

		return getWidget();

	}

	private Widget fillContentUsers(final GetRichAdminsWithAttributes admins, TabMenu menu) {

		admins.clearTableSelectedSet();

		boolean isMembersGroup = group.isCoreGroup();

		menu.addWidget(0, UiElements.getRefreshButton(this));

		// Events for reloading when finished
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(admins);

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeManagerFromGroup());

		if(!isMembersGroup){

			CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addManagerToGroup(), new ClickHandler() {
				public void onClick(ClickEvent event) {
					session.getTabManager().addTabToCurrentTab(new AddGroupManagerTabItem(group), true);
				}
			});
			if (!session.isVoAdmin(group.getVoId()) && !session.isGroupAdmin(group.getId())) addButton.setEnabled(false);
			menu.addWidget(1, addButton);

			removeButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					final ArrayList<User> itemsToRemove = admins.getTableSelectedList();
					String text = "Following users won't be group managers anymore.";
					UiElements.showDeleteConfirm(itemsToRemove, text, new ClickHandler() {
						@Override
						public void onClick(ClickEvent clickEvent) {
							// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
							for (int i=0; i<itemsToRemove.size(); i++ ) {
								RemoveAdmin request;
								if (i == itemsToRemove.size() - 1) {
									request = new RemoveAdmin(PerunEntity.GROUP, JsonCallbackEvents.disableButtonEvents(removeButton, events));
								} else {
									request = new RemoveAdmin(PerunEntity.GROUP, JsonCallbackEvents.disableButtonEvents(removeButton));
								}
								request.removeGroupAdmin(group, itemsToRemove.get(i));
							}
						}
					});
				}
			});
			menu.addWidget(2, removeButton);

		} else {

			// is core group
			menu.addWidget(1, new Image(SmallIcons.INSTANCE.helpIcon()));
			Anchor a = new Anchor("<strong>To edit VO managers use VO manager section in menu.</strong>", true);
			a.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					session.getTabManager().addTab(new VoManagersTabItem(group.getVoId()));
				}
			});
			menu.addWidget(2, a);

		}

		// get the table
		CellTable<User> table;
		if (session.isPerunAdmin()) {
			table = admins.getTable(new FieldUpdater<User, String>() {
				public void update(int i, User user, String s) {
					session.getTabManager().addTab(new UserDetailTabItem(user));
				}
			});
		} else {
			table = admins.getTable();
		}

		removeButton.setEnabled(false);
		if (session.isVoAdmin(group.getVoId()) || session.isGroupAdmin(group.getId())) {
			JsonUtils.addTableManagedButton(admins, table, removeButton);
		}

		table.setStyleName("perun-table");

		return table;

	}

	private Widget fillContentGroups(final GetAdminGroups admins, TabMenu menu) {

		admins.clearTableSelectedSet();

		boolean isMembersGroup = group.isCoreGroup();

		menu.addWidget(0, UiElements.getRefreshButton(this));

		// Events for reloading when finished
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(admins);

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeManagerGroupFromGroup());

		if(!isMembersGroup){

			CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addManagerGroupToGroup(), new ClickHandler() {
				public void onClick(ClickEvent event) {
					session.getTabManager().addTabToCurrentTab(new AddGroupManagerGroupTabItem(group, events), true);
				}
			});
			if (!session.isVoAdmin(group.getVoId()) && !session.isGroupAdmin(group.getId())) addButton.setEnabled(false);
			menu.addWidget(1, addButton);

			removeButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					final ArrayList<Group> itemsToRemove = admins.getTableSelectedList();
					String text = "Members of following groups won't be group managers anymore.";
					UiElements.showDeleteConfirm(itemsToRemove, text, new ClickHandler() {
						@Override
						public void onClick(ClickEvent clickEvent) {
							// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
							for (int i=0; i<itemsToRemove.size(); i++ ) {
								RemoveAdmin request;
								if (i == itemsToRemove.size() - 1) {
									request = new RemoveAdmin(PerunEntity.GROUP, JsonCallbackEvents.disableButtonEvents(removeButton, events));
								} else {
									request = new RemoveAdmin(PerunEntity.GROUP, JsonCallbackEvents.disableButtonEvents(removeButton));
								}
								request.removeGroupAdminGroup(group, itemsToRemove.get(i));
							}
						}
					});
				}
			});
			menu.addWidget(2, removeButton);

		} else {

			// is core group
			menu.addWidget(1, new Image(SmallIcons.INSTANCE.helpIcon()));
			Anchor a = new Anchor("<strong>To edit VO managers use VO manager section in menu.</strong>", true);
			a.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					session.getTabManager().addTab(new VoManagersTabItem(group.getVoId()));
				}
			});
			menu.addWidget(2, a);

		}

		// get the table
		CellTable<Group> table = admins.getTable(new FieldUpdater<Group, String>() {
			public void update(int i, Group grp, String s) {
				session.getTabManager().addTab(new GroupDetailTabItem(grp));
			}
		});

		removeButton.setEnabled(false);
		if (session.isVoAdmin(group.getVoId()) || session.isGroupAdmin(group.getId())) {
			JsonUtils.addTableManagedButton(admins, table, removeButton);
		}

		table.setStyleName("perun-table");

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
		final int prime = 823;
		int result = 1;
		result = prime * result + groupId;
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

		GroupManagersTabItem create = (GroupManagersTabItem) obj;

		if (group != create.group){
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(group, "Managers", getUrlWithParameters());
		if(group != null){
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);

	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(group.getVoId()) || session.isVoObserver(group.getVoId()) || session.isGroupAdmin(groupId)) {
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

	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
	}

	static public GroupManagersTabItem load(Map<String, String> parameters) {
		int id = Integer.parseInt(parameters.get("id"));
		return new GroupManagersTabItem(id);
	}

}
