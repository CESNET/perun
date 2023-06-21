package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.HTML;
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
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.registrarManager.AddGroupsToAutoRegistration;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllGroups;
import cz.metacentrum.perun.webgui.json.registrarManager.GetGroupsToAutoRegistration;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AddAutoRegistrationGroupsTabItem implements TabItem {

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
	private Label titleWidget = new Label("Add group for auto registration");

	//data
	private VirtualOrganization vo;

	public AddAutoRegistrationGroupsTabItem(VirtualOrganization vo) {
		this.vo = vo;
	}

	@Override
	public Widget draw() {
		titleWidget.setText("Add group for auto registration");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// menu
		TabMenu menu = new TabMenu();
		menu.addWidget(new HTML(""));

		final GetAllGroups groups = new GetAllGroups(vo.getId());
		groups.setCoreGroupsCheckable(false);

		// remove already added union groups from offering
		JsonCallbackEvents localEvents = new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				// second callback
				final GetGroupsToAutoRegistration alreadySet = new GetGroupsToAutoRegistration(vo.getId(), new JsonCallbackEvents() {
					public void onFinished(JavaScriptObject jso) {
						JsArray<Group> esToRemove = JsonUtils.jsoAsArray(jso);
						for (int i = 0; i < esToRemove.length(); i++) {
							groups.removeFromTable(esToRemove.get(i));
						}
					}
				});
				alreadySet.retrieveData();
			}
		};
		groups.setEvents(localEvents);

		final ExtendedSuggestBox box = new ExtendedSuggestBox(groups.getOracle());

		// button
		final CustomButton assignButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedExtSource());
		final TabItem tab = this;

		assignButton.addClickHandler(event -> {
			final ArrayList<Group> availableGroups = groups.getTableSelectedList();
			if (UiElements.cantSaveEmptyListDialogBox(availableGroups)) {
				AddGroupsToAutoRegistration request = new AddGroupsToAutoRegistration(JsonCallbackEvents.disableButtonEvents(assignButton, new JsonCallbackEvents() {
					@Override
					public void onFinished(JavaScriptObject jso) {
						// clear search
						box.getSuggestBox().setText("");
						groups.retrieveData();
					}
				}));
				request.setAutoRegGroups(availableGroups, null,null);
			}
		});

		menu.addFilterWidget(box, new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				groups.filterTable(text);
			}
		}, "Filter by name");

		menu.addWidget(assignButton);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "",
				clickEvent -> session.getTabManager().closeTab(tab, isRefreshParentOnClose())));

		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		CellTable<Group> table = groups.getTable();

		assignButton.setEnabled(false);
		JsonUtils.addTableManagedButton(groups, table, assignButton);

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		vp.add(sp);

		// do not use resizePerunTable() when tab is in overlay - wrong width is calculated
		session.getUiElements().resizePerunTable(sp, 350, this);

		this.contentWidget.setWidget(vp);

		return getWidget();
	}

	@Override
	public Widget getWidget() {
		return this.contentWidget;
	}

	@Override
	public Widget getTitle() {
		return this.titleWidget;
	}

	@Override
	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.addIcon();
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;

		AddAutoRegistrationGroupsTabItem create = (AddAutoRegistrationGroupsTabItem) o;
		return this.vo.getId() == create.vo.getId();
	}

	@Override
	public int hashCode() {
		return vo != null ? vo.hashCode() : 0;
	}

	@Override
	public boolean multipleInstancesEnabled() {
		return false;
	}

	@Override
	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		if (vo != null) {
			session.setActiveVo(vo);
		}
	}

	@Override
	public boolean isAuthorized() {
		return session.isVoAdmin(vo.getId());
	}

	@Override
	public boolean isPrepared() {
		return true;
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return true;
	}

	@Override
	public void onClose() {

	}
}

