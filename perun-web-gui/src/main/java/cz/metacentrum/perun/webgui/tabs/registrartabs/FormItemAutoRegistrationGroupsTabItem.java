package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.registrarManager.GetGroupsToAutoRegistration;
import cz.metacentrum.perun.webgui.json.registrarManager.RemoveGroupsFromAutoRegistration;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.tabs.RegistrarTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class FormItemAutoRegistrationGroupsTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Manage registration groups - form item");

	public static final String URL = "auto-reg-groups-item";

	private PerunEntity entity;
	private int id;
	private int formItem;
	private List<Group> groups;

	public FormItemAutoRegistrationGroupsTabItem(PerunEntity entity, int id, int formItem) {
		this.entity = entity;
		this.id = id;
		this.formItem = formItem;
		groups = new ArrayList<>();
	}

	@Override
	public Widget draw() {
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));

		final GetGroupsToAutoRegistration groups = new GetGroupsToAutoRegistration(entity, id, formItem);
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(groups);

		if ((PerunEntity.VIRTUAL_ORGANIZATION.equals(entity) && !session.isVoAdmin(id)) ||
			(PerunEntity.GROUP.equals(entity) && !session.isGroupAdmin(id))) {
			groups.setCheckable(false);
		}

		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addGroupToAutoReg(),
				event -> session.getTabManager().addTabToCurrentTab(new FormItemAddAutoRegistrationGroupsTabItem(entity, id, formItem), true));
		if ((PerunEntity.VIRTUAL_ORGANIZATION.equals(entity) && !session.isVoAdmin(id)) ||
			(PerunEntity.GROUP.equals(entity) && !session.isGroupAdmin(id))) {
			addButton.setEnabled(false);
		}
		menu.addWidget(addButton);

		// remove selected groups button
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeGroupFromAutoReg());
		removeButton.addClickHandler(event -> {
			final ArrayList<Group> groupsToRemove = groups.getTableSelectedList();
			String text = "Following groups will be removed from the auto registration.";
			UiElements.showDeleteConfirm(groupsToRemove, text, clickEvent -> {
				RemoveGroupsFromAutoRegistration request = new RemoveGroupsFromAutoRegistration(JsonCallbackEvents.disableButtonEvents(removeButton, events));
				request.deleteGroups(groupsToRemove, PerunEntity.GROUP.equals(entity) ? id : null, formItem);
			});
		});
		menu.addWidget(removeButton);

		// filter box
		menu.addFilterWidget(new ExtendedSuggestBox(groups.getOracle()), new PerunSearchEvent() {
			public void searchFor(String text) {
				groups.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterGroup());

		// add a table with a onclick
		CellTable<Group> table = groups.getTable();

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(table);


		contentWidget.setWidget(vp);
		return getWidget();
	}

	@Override
	public Widget getWidget() {
		return contentWidget;
	}

	@Override
	public Widget getTitle() {
		return titleWidget;
	}

	@Override
	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.groupIcon();
	}

	@Override
	public boolean multipleInstancesEnabled() {
		return false;
	}

	@Override
	public void open() {
		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
			session.getUiElements().getBreadcrumbs().setLocation(id, "Auto registration groups", getUrlWithParameters());
			session.setActiveVoId(id);
		} else {
			session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
			session.getUiElements().getBreadcrumbs().setLocation(id, "Auto registration groups", getUrlWithParameters());
			session.setActiveGroupId(id);
		}
	}

	@Override
	public boolean isAuthorized() {
		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			return session.isVoAdmin(id);
		} else {
			return session.isGroupAdmin(id);
		}
	}

	@Override
	public boolean isPrepared() {
		return entity != null && groups != null;
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	@Override
	public String getUrl() {
		return URL;
	}

	@Override
	public String getUrlWithParameters() {
		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			return RegistrarTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?vo=" + id + "&formItem=" + formItem;
		} else {
			return RegistrarTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?group=" + "&formItem=" + formItem;
		}
	}

	public static FormItemAutoRegistrationGroupsTabItem load(Map<String, String> parameters) {
		int id;
		PerunEntity entity;
		if (parameters.containsKey("vo")) {
			id = Integer.parseInt(parameters.get("vo"));
			entity = PerunEntity.VIRTUAL_ORGANIZATION;
		} else {
			id = Integer.parseInt(parameters.get("group"));
			entity = PerunEntity.GROUP;
		}
		int formItem = Integer.parseInt(parameters.get("formItem"));
		return new FormItemAutoRegistrationGroupsTabItem(entity, id, formItem);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FormItemAutoRegistrationGroupsTabItem that = (FormItemAutoRegistrationGroupsTabItem) o;
		return id == that.id && formItem == that.formItem && entity == that.entity;
	}

	@Override
	public int hashCode() {
		return Objects.hash(entity, id, formItem);
	}
}
