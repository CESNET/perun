package cz.metacentrum.perun.webgui.tabs.groupstabs;

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
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedRichResources;
import cz.metacentrum.perun.webgui.json.resourcesManager.RemoveGroupFromResources;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.RichResource;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.resourcestabs.ResourceDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Groups resources tab
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GroupResourcesTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading group resources");

	// data
	private Group group;
	private int groupId;

	/**
	 * Creates a tab instance
	 *
	 * @param group
	 */
	public GroupResourcesTabItem(Group group){
		this.group = group;
		this.groupId = group.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param groupId
	 */
	public GroupResourcesTabItem(int groupId){
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

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		// set title
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName())+": "+"resources");

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));

		// get VO resources
		final GetAssignedRichResources resources = new GetAssignedRichResources(groupId, PerunEntity.GROUP);
		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) resources.setCheckable(false);

		// custom events for viewResource
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(resources);

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeGroupFromSelectedResources());

		// add / remove resource from group can be done by vo / perun admin only.
		if (session.isVoAdmin(group.getVoId())) {

			menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.assignGroupToResources(), new ClickHandler() {
				public void onClick(ClickEvent event) {
					session.getTabManager().addTabToCurrentTab(new AssignGroupTabItem(group, resources.getList()), true);
				}
			}));

			removeButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					final ArrayList<RichResource> toRemove = resources.getTableSelectedList();
					String text = "Following resources will be removed from group and it's members won't have access to them anymore.";
					UiElements.showDeleteConfirm(toRemove, text, new ClickHandler() {
						@Override
						public void onClick(ClickEvent clickEvent) {
							RemoveGroupFromResources request = new RemoveGroupFromResources(JsonCallbackEvents.disableButtonEvents(removeButton, events));
							request.removeGroupFromResources(group, toRemove);
						}
					});
				}
			});
			menu.addWidget(removeButton);
		}

		// filter box
		menu.addFilterWidget(new ExtendedSuggestBox(resources.getOracle()), new PerunSearchEvent() {
			public void searchFor(String text) {
				resources.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterResources());

		// add menu to the main panel
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		CellTable<RichResource> table;

		// perun / vo admin can set attributes
		if (session.isVoAdmin(group.getVoId()) || session.isVoObserver(group.getVoId())) {
			table = resources.getTable(new FieldUpdater<RichResource, String>() {
				public void update(int index, RichResource object, String value) {
					session.getTabManager().addTab(new ResourceDetailTabItem(object, 0));
				}
			});
		} else {
			table = resources.getTable();
		}

		removeButton.setEnabled(false);
		if (session.isGroupAdmin(groupId) || session.isVoAdmin(group.getVoId())) JsonUtils.addTableManagedButton(resources, table, removeButton);

		table.addStyleName("perun-table");
		table.setWidth("100%");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(sp);
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
		return SmallIcons.INSTANCE.serverGroupIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1423;
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
		GroupResourcesTabItem other = (GroupResourcesTabItem) obj;
		if (groupId != other.groupId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(group, "Resources", getUrlWithParameters());
		if(group != null){
			session.setActiveGroup(group);
		} else {
			session.setActiveGroupId(groupId);
		}
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(group.getVoId()) || session.isVoObserver(group.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "resources";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
	}

	static public GroupResourcesTabItem load(Map<String, String> parameters) {
		int groupId = Integer.parseInt(parameters.get("id"));
		return new GroupResourcesTabItem(groupId);
	}

}
