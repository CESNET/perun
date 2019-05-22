package cz.metacentrum.perun.webgui.tabs.groupstabs;

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
import cz.metacentrum.perun.webgui.json.extSourcesManager.GetGroupExtSources;
import cz.metacentrum.perun.webgui.json.extSourcesManager.GetVoExtSources;
import cz.metacentrum.perun.webgui.json.extSourcesManager.RemoveExtSource;
import cz.metacentrum.perun.webgui.model.ExtSource;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.tabs.vostabs.AddVoExtSourceTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Group ext. sources management page
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GroupExtSourcesTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading group ext sources");

	// data
	private Group group;
	private int groupId;
	private int voId;

	/**
	 * Creates a tab instance
	 *
	 * @param group
	 */
	public GroupExtSourcesTabItem(Group group){
		this.group = group;
		this.groupId = group.getId();
		this.voId = group.getVoId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param groupId
	 */
	public GroupExtSourcesTabItem(int groupId){
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

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName())+": "+"ext sources");

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));

		// get VO resources
		final GetGroupExtSources extSources = new GetGroupExtSources(groupId);

		// refresh table event
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(extSources);

		// create ext source button
		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addExtSource(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddGroupExtSourceTabItem(groupId), true);
			}
		});
		if (session.isVoAdmin(voId)) {
			menu.addWidget(addButton);
		}

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeExtSource());
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<ExtSource> extSourcesToRemove = extSources.getTableSelectedList();
				String text = "Following external sources will be removed from Group. You won't be able to import members from them anymore.";
				UiElements.showDeleteConfirm(extSourcesToRemove, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i=0; i<extSourcesToRemove.size(); i++) {
							RemoveExtSource request;
							if (i == extSourcesToRemove.size()-1) {
								request = new RemoveExtSource(JsonCallbackEvents.disableButtonEvents(removeButton, events));
							} else {
								request = new RemoveExtSource(JsonCallbackEvents.disableButtonEvents(removeButton));
							}
							request.removeGroupExtSource(groupId, extSourcesToRemove.get(i).getId());
						}
					}
				});
			}
		});
		if (session.isVoAdmin(voId)) {
			menu.addWidget(removeButton);
		}

		// authorization - enable buttons for vo admin only.
		if (!session.isVoAdmin(voId)) {
			addButton.setEnabled(false);
			removeButton.setEnabled(false);
			extSources.setCheckable(false);
		}

		menu.addFilterWidget(new ExtendedSuggestBox(extSources.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				extSources.filterTable(text);
			}
		}, "Filter external sources by name or type");

		// add menu to the main panel
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		CellTable<ExtSource> table = extSources.getTable();

		if (session.isVoAdmin(voId)) {
			removeButton.setEnabled(false);
			JsonUtils.addTableManagedButton(extSources, table, removeButton);
		}

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
		return SmallIcons.INSTANCE.worldIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1601;
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
		GroupExtSourcesTabItem other = (GroupExtSourcesTabItem) obj;
		if (groupId != other.groupId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(group, "External sources", getUrlWithParameters());
		if(group != null){
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId) || session.isVoObserver(voId) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}
	}

	public final static String URL = "ext-sources";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
	}

	static public GroupExtSourcesTabItem load(Map<String, String> parameters) {
		int voId = Integer.parseInt(parameters.get("id"));
		return new GroupExtSourcesTabItem(voId);
	}

}
