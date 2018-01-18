package cz.metacentrum.perun.webgui.tabs.vostabs;

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
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllRichGroups;
import cz.metacentrum.perun.webgui.model.RichGroup;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.tabs.groupstabs.CreateGroupTabItem;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.groupstabs.MoveGroupsTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * VO Groups page.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class VoGroupsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading vo groups");

	// data
	private VirtualOrganization vo;
	private int voId;

	/**
	 * Creates a tab instance
	 *
	 * @param vo
	 */
	public VoGroupsTabItem(VirtualOrganization vo){
		this.vo = vo;
		this.voId = vo.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param voId
	 */
	public VoGroupsTabItem(int voId){
		this.voId = voId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				vo = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
	}

	public boolean isPrepared(){
		return !(vo == null);
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName())+": "+"groups");

		// MAIN PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu menu = new TabMenu();

		// VO Groups request
		ArrayList<String> attrNames = new ArrayList<>();
		attrNames.add("urn:perun:group:attribute-def:def:synchronizationEnabled");
		attrNames.add("urn:perun:group:attribute-def:def:synchronizationInterval");
		attrNames.add("urn:perun:group:attribute-def:def:lastSynchronizationState");
		attrNames.add("urn:perun:group:attribute-def:def:lastSuccessSynchronizationTimestamp");
		attrNames.add("urn:perun:group:attribute-def:def:lastSynchronizationTimestamp");
		attrNames.add("urn:perun:group:attribute-def:def:authoritativeGroup");
		final GetAllRichGroups groups = new GetAllRichGroups(voId, attrNames);
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(groups);
		if (!session.isVoAdmin(voId)) groups.setCheckable(false);

		// refresh
		 menu.addWidget(UiElements.getRefreshButton(this));

		// add new group button
		CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, true, ButtonTranslation.INSTANCE.createGroup(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new CreateGroupTabItem(vo));
			}
		});
		if (!session.isVoAdmin(voId)) createButton.setEnabled(false);
		menu.addWidget(createButton);

		// delete selected groups button
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.DELETE, ButtonTranslation.INSTANCE.deleteGroup());
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<RichGroup> groupsToDelete = groups.getTableSelectedList();
				String text = "Following groups (including all sub-groups) will be deleted.";
				UiElements.showDeleteConfirm(groupsToDelete, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						DeleteGroups request = new DeleteGroups(JsonCallbackEvents.disableButtonEvents(removeButton, events));
						request.deleteGroups(groupsToDelete);
					}
				});
			}
		});
		menu.addWidget(removeButton);

		CustomButton moveButton = TabMenu.getPredefinedButton(ButtonType.MOVE, true, ButtonTranslation.INSTANCE.moveGroup(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<RichGroup> groupsToMove = groups.getTableSelectedList();
				session.getTabManager().addTabToCurrentTab(new MoveGroupsTabItem(vo, groupsToMove));
			}
		});
		if (session.isPerunAdmin()) {
			// FIXME - temporary for perun admin
			if (!session.isVoAdmin(voId)) moveButton.setEnabled(false);
			menu.addWidget(moveButton);
		}

		// filter box
		menu.addFilterWidget(new ExtendedSuggestBox(groups.getOracle()), new PerunSearchEvent() {
			public void searchFor(String text) {
				groups.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterGroup());

		// add a table with a onclick
		CellTable<RichGroup> table = groups.getTable(new FieldUpdater<RichGroup, String>() {
			public void update(int index, RichGroup group, String value) {
				session.getTabManager().addTab(new GroupDetailTabItem(group.getId()));
			}
		});

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		// add menu and the table to the main panel
		firstTabPanel.add(menu);
		firstTabPanel.setCellHeight(menu, "30px");
		firstTabPanel.add(sp);

		removeButton.setEnabled(false);
		if (session.isVoAdmin(voId)) JsonUtils.addTableManagedButton(groups, table, removeButton);
		// fixme - temporary only for perun admin
		if (session.isPerunAdmin()) {
			JsonUtils.addTableManagedButton(groups, table, moveButton);
			moveButton.setEnabled(false);
		}

		session.getUiElements().resizePerunTable(sp, 350, this);


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
		return SmallIcons.INSTANCE.groupIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1607;
		int result = 1;
		result = prime * result + voId;
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
		VoGroupsTabItem other = (VoGroupsTabItem) obj;
		if (voId != other.voId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(vo, "Groups", getUrlWithParameters());
		if(vo != null){
			session.setActiveVo(vo);
			return;
		}
		session.setActiveVoId(voId);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "groups";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + voId;
	}

	static public VoGroupsTabItem load(Map<String, String> parameters) {
		int voId = Integer.parseInt(parameters.get("id"));
		return new VoGroupsTabItem(voId);
	}

}
