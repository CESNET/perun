package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.core.client.JavaScriptObject;
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
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.groupsManager.DeleteGroups;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAutoRegistrationGroups;
import cz.metacentrum.perun.webgui.json.groupsManager.RemoveGroupsFromAutoRegistration;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
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

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AutoRegistrationGroupsTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Auto registration groups");

	public static final String URL = "auto-reg-groups";

	private int voId;
	private VirtualOrganization vo;
	private List<Group> groups;

	public AutoRegistrationGroupsTabItem(int voId) {
		this.voId = voId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				vo = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();

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

		final GetAutoRegistrationGroups groups = new GetAutoRegistrationGroups(voId);
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(groups);

		if (!session.isVoAdmin(voId)) {
			groups.setCheckable(false);
		}

		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addGroupToAutoReg(),
				event -> session.getTabManager().addTabToCurrentTab(new AddAutoRegGroupTabItem(vo), true));
		if (!session.isVoAdmin(voId)) addButton.setEnabled(false);
		menu.addWidget(addButton);

		// remove selected groups button
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeGroupFromAutoReg());
		removeButton.addClickHandler(event -> {
			final ArrayList<Group> groupsToRemove = groups.getTableSelectedList();
			String text = "Following groups will be removed from the auto registration.";
			UiElements.showDeleteConfirm(groupsToRemove, text, clickEvent -> {
				RemoveGroupsFromAutoRegistration request = new RemoveGroupsFromAutoRegistration(JsonCallbackEvents.disableButtonEvents(removeButton, events));
				request.deleteGroups(groupsToRemove);
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
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(vo, "Auto registration groups", getUrlWithParameters());

		if (vo != null) {
			session.setActiveVo(vo);
			return;
		}
		session.setActiveVoId(voId);
	}

	@Override
	public boolean isAuthorized() {
		return session.isVoAdmin(voId);
	}

	@Override
	public boolean isPrepared() {
		return vo != null && groups != null;
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
		return RegistrarTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?vo=" + voId;
	}

	public static AutoRegistrationGroupsTabItem load(Map<String, String> parameters) {
		int voId = Integer.parseInt(parameters.get("vo"));
		return new AutoRegistrationGroupsTabItem(voId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AutoRegistrationGroupsTabItem)) return false;

		AutoRegistrationGroupsTabItem that = (AutoRegistrationGroupsTabItem) o;

		return voId == that.voId;
	}

	@Override
	public int hashCode() {
		final int prime = 977;
		return 31 * prime + voId;
	}
}
