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
import cz.metacentrum.perun.webgui.json.groupsManager.DeleteGroups;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllRichSubGroups;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.RichGroup;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Group admins page for Group Admin
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SubgroupsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading subgroups");

	/**
	 * Group
	 */
	private Group group;
	private int groupId;


	/**
	 * Creates a tab instance
	 * @param group
	 */
	public SubgroupsTabItem(Group group){
		this.group = group;
		this.groupId = group.getId();
	}

	/**
	 * Creates a tab instance
	 * @param groupId
	 */
	public SubgroupsTabItem(int groupId){
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

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName()) + ": subgroups");

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// if members group, hide
		if(group.isCoreGroup()){
			vp.add(new HTML("<h2>Members group cannot have subgroups.</h2>"));
			this.contentWidget.setWidget(vp);
			return getWidget();
		}

		// GROUP TABLE with onclick
		ArrayList<String> attrNames = new ArrayList<>();
		attrNames.add("urn:perun:group:attribute-def:def:synchronizationEnabled");
		attrNames.add("urn:perun:group:attribute-def:def:synchronizationInterval");
		attrNames.add("urn:perun:group:attribute-def:def:lastSynchronizationState");
		attrNames.add("urn:perun:group:attribute-def:def:lastSynchronizationTimestamp");
		attrNames.add("urn:perun:group:attribute-def:def:lastSuccessSynchronizationTimestamp");
		attrNames.add("urn:perun:group:attribute-def:def:authoritativeGroup");

		final GetAllRichSubGroups subgroups = new GetAllRichSubGroups(groupId, attrNames);

		// Events for reloading when group is created
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(subgroups);

		// menu
		TabMenu menu = new TabMenu();

		menu.addWidget(UiElements.getRefreshButton(this));

		CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, true, ButtonTranslation.INSTANCE.createSubGroup(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				// creates a new form
				session.getTabManager().addTabToCurrentTab(new CreateGroupTabItem(group));
			}
		});

		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) {
			createButton.setEnabled(false);
			subgroups.setCheckable(false);
		}
		menu.addWidget(createButton);

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.DELETE, ButtonTranslation.INSTANCE.deleteSubGroup());
		removeButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<RichGroup> itemsToRemove = subgroups.getTableSelectedList();
				String text = "Following groups (including all sub-groups) will be deleted.";
				UiElements.showDeleteConfirm(itemsToRemove, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						DeleteGroups request = new DeleteGroups(JsonCallbackEvents.disableButtonEvents(removeButton, events));
						request.deleteGroups(itemsToRemove);
					}
				});
			}
		});
		menu.addWidget(removeButton);

		// move button
		CustomButton moveButton = TabMenu.getPredefinedButton(ButtonType.MOVE, true, ButtonTranslation.INSTANCE.moveGroup(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<RichGroup> groupsToMove = subgroups.getTableSelectedList();
				session.getTabManager().addTabToCurrentTab(new MoveGroupsTabItem(group, groupsToMove));
			}
		});

		if (session.isPerunAdmin()) {
			// FIXME - temporary for perun admin
			if (!session.isVoAdmin(group.getVoId())) moveButton.setEnabled(false);
			menu.addWidget(moveButton);
		}

		// filter box
		menu.addFilterWidget(new ExtendedSuggestBox(subgroups.getOracle()), new PerunSearchEvent() {
			public void searchFor(String text) {
				subgroups.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterGroup());

		// add menu to the main panel
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		CellTable<RichGroup> table = subgroups.getTable(new FieldUpdater<RichGroup, String>() {
			@Override
			public void update(int arg0, RichGroup group, String arg2) {
				session.getTabManager().addTab(new GroupDetailTabItem(group.getId()));
			}
		});

		removeButton.setEnabled(false);
		if (session.isGroupAdmin(groupId) || session.isVoAdmin(group.getVoId())) JsonUtils.addTableManagedButton(subgroups, table, removeButton);
		// FIXME - temporary for perun admin
		if (session.isPerunAdmin()) {
			JsonUtils.addTableManagedButton(subgroups, table, moveButton);
			moveButton.setEnabled(false);
		}
		//if (session.isVoAdmin(group.getVoId())) JsonUtils.addTableManagedButton(subgroups, table, moveButton);

		// adds the table into the panel
		table.addStyleName("perun-table");
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
		return SmallIcons.INSTANCE.groupGoIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 857;
		int result = 1;
		result = prime * result + groupId + 546;
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

		SubgroupsTabItem create = (SubgroupsTabItem) obj;

		if (groupId != create.groupId){
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(group, "Subgroups", getUrlWithParameters());
		if(group != null){
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(group.getVoId()) || session.isVoObserver(group.getVoId()) || session.isGroupAdmin(group.getId())) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "subgps";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
	}

	static public SubgroupsTabItem load(Map<String, String> parameters) {
		int gid = Integer.parseInt(parameters.get("id"));
		return new SubgroupsTabItem(gid);
	}

}
