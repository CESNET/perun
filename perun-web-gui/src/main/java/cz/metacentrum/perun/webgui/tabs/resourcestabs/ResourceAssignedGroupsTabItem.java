package cz.metacentrum.perun.webgui.tabs.resourcestabs;

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
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedGroups;
import cz.metacentrum.perun.webgui.json.resourcesManager.RemoveGroupsFromResource;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.tabs.ResourcesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Provides page with groups on resources managements (assign / remove)
 * Used by VO administrators
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz> *
 */
public class ResourceAssignedGroupsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Manage assigned groups");

	// data
	private int resourceId;
	private Resource resource;

	/**
	 * Creates a tab instance
	 * @param resourceId ID of resource to get groups management for
	 */
	public ResourceAssignedGroupsTabItem(int resourceId){
		this.resourceId = resourceId;
		new GetEntityById(PerunEntity.RESOURCE, resourceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				resource = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * Creates a tab instance
	 * @param resource resource to get groups management for
	 */
	public ResourceAssignedGroupsTabItem(Resource resource){
		this.resource = resource;
		this.resourceId = resource.getId();
	}

	public boolean isPrepared(){
		return !(resource == null);
	}

	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(resource.getName()) + ": manage assigned groups");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));

		final GetAssignedGroups resourceGroups = new GetAssignedGroups(resourceId);


		final JsonCallbackEvents localEvents = JsonCallbackEvents.refreshTableEvents(resourceGroups);

		CustomButton assignGroupButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.assignGroupToResource(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AssignGroupTabItem(resource), true);
			}
		});

		final CustomButton removeGroupButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeGroupFromResource());
		removeGroupButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<Group> groupsForRemoving = resourceGroups.getTableSelectedList();
				String text = "Following groups will be removed from resource.";
				UiElements.showDeleteConfirm(groupsForRemoving, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						RemoveGroupsFromResource request = new RemoveGroupsFromResource(JsonCallbackEvents.disableButtonEvents(removeGroupButton, localEvents));
						request.removeGroupsFromResource(groupsForRemoving, resource);
					}
				});
			}
		});

		if (!session.isVoAdmin(resource.getVoId())) {
			resourceGroups.setCheckable(false);
			assignGroupButton.setEnabled(false);
		}

		menu.addWidget(assignGroupButton);
		menu.addWidget(removeGroupButton);

		menu.addFilterWidget(new ExtendedSuggestBox(resourceGroups.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				resourceGroups.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterGroup());

		// display menu to vo admin only
		if (session.isVoAdmin(resource.getVoId())) {
			resourceGroups.setCheckable(true);
			vp.add(menu);
			vp.setCellHeight(menu,"30px");
		} else {
			resourceGroups.setCheckable(false);
		}

		// table with field updater which leads to group detail
		CellTable<Group> table = resourceGroups.getTable(new FieldUpdater<Group, String>(){
			public void update(int index, Group object, String value) {
				session.getTabManager().addTab(new GroupDetailTabItem(object));
			}
		});

		removeGroupButton.setEnabled(false);
		if (session.isVoAdmin(resource.getVoId())) JsonUtils.addTableManagedButton(resourceGroups, table, removeGroupButton);

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
		final int prime = 1033;
		int result = 1;
		result = prime * result + resourceId;
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
		ResourceAssignedGroupsTabItem other = (ResourceAssignedGroupsTabItem) obj;
		if (resourceId != other.resourceId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
		session.setActiveVoId(resource.getVoId());
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(resource.getVoId()) || session.isVoObserver(resource.getVoId())) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "manage-groups";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return ResourcesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + resourceId;
	}

	static public ResourceAssignedGroupsTabItem load(Map<String, String> parameters) {
		int id = Integer.parseInt(parameters.get("id"));
		return new ResourceAssignedGroupsTabItem(id);
	}

	static public ResourceAssignedGroupsTabItem load(Resource resource) {
		return new ResourceAssignedGroupsTabItem(resource);
	}

}
