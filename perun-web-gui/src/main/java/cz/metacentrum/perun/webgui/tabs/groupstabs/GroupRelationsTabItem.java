package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
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
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.GetGroupUnions;
import cz.metacentrum.perun.webgui.json.groupsManager.RemoveGroupUnions;
import cz.metacentrum.perun.webgui.model.Group;
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
 * @author Michal Krajcovic <mkrajcovic@mail.muni.cz>
 */
public class GroupRelationsTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading relations");

	/**
	 * Group
	 */
	private Group group;
	private int groupId;


	/**
	 * Creates a tab instance
	 *
	 * @param group
	 */
	public GroupRelationsTabItem(Group group) {
		this.group = group;
		this.groupId = group.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param groupId
	 */
	public GroupRelationsTabItem(int groupId) {
		this.groupId = groupId;
		JsonCallbackEvents events = new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso) {
				group = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.GROUP, groupId, events).retrieveData();
	}

	public final static String URL = "relations";

	@Override
	public String getUrl() {
		return URL;
	}

	@Override
	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
	}

	static public GroupRelationsTabItem load(Map<String, String> parameters) {
		int gid = Integer.parseInt(parameters.get("id"));
		return new GroupRelationsTabItem(gid);
	}

	@Override
	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName()) + ": unions");

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// if members group, hide
		if (group.isCoreGroup()) {
			vp.add(new HTML("<h2>Members group cannot have unions.</h2>"));
			this.contentWidget.setWidget(vp);
			return getWidget();
		}

		final GetGroupUnions unions = new GetGroupUnions(group, false);

		// Events for reloading when group is created
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(unions);

		// menu
		TabMenu menu = new TabMenu();

		final CheckBox subGroupsCheckBox = new CheckBox("Show sub-groups");
		final ListBox reverseDropdown = new ListBox();
		reverseDropdown.addItem("Normal");
		reverseDropdown.addItem("Reverse");

		reverseDropdown.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent changeEvent) {
				switch (reverseDropdown.getSelectedIndex()) {
					case 1:
						unions.setReverseAndRefresh(true);
						subGroupsCheckBox.setVisible(false);
						break;
					default:
						unions.setReverseAndRefresh(false);
						subGroupsCheckBox.setVisible(true);
				}
			}
		});

		menu.addWidget(UiElements.getRefreshButton(this));

		CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addGroupUnion(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				// creates a new form
				session.getTabManager().addTabToCurrentTab(new AddGroupUnionTabItem(group), true);
			}
		});

		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) {
			createButton.setEnabled(false);
			unions.setCheckable(false);
		}
		menu.addWidget(createButton);

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeGroupUnion());
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<Group> itemsToRemove = unions.getTableSelectedList();
				String text = "Following group unions will be deleted.";
				UiElements.showDeleteConfirm(itemsToRemove, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						RemoveGroupUnions request = new RemoveGroupUnions(JsonCallbackEvents.disableButtonEvents(removeButton, events));
						if (unions.isReverse()) {
							request.deleteGroupUnions(itemsToRemove, group);
						} else {
							request.deleteGroupUnions(group, itemsToRemove);
						}
					}
				});
			}
		});
		menu.addWidget(removeButton);

		// filter box
		final ExtendedSuggestBox box = new ExtendedSuggestBox(unions.getOracle());
		menu.addFilterWidget(box, new PerunSearchEvent() {
			public void searchFor(String text) {
				unions.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterGroup());


		subGroupsCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> valueChangeEvent) {
				unions.setShowSubgroupsAndRefresh(valueChangeEvent.getValue(), box.getSuggestBox().getText());
			}
		});

		menu.addWidget(new HTML("<strong>Direction: </strong>"));
		menu.addWidget(reverseDropdown);
		menu.addWidget(subGroupsCheckBox);
		// add menu to the main panel
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		CellTable<Group> table = unions.getTable(new FieldUpdater<Group, String>() {
			@Override
			public void update(int arg0, Group group, String arg2) {
				if (session.isGroupAdmin(group.getId()) || session.isVoAdmin(group.getId())) {
					session.getTabManager().addTab(new GroupDetailTabItem(group.getId()));
				} else {
					UiElements.generateInfo("Not privileged", "You are not manager of selected group or its VO.");
				}
			}
		});
		removeButton.setEnabled(false);
		if (session.isGroupAdmin(groupId) || session.isVoAdmin(group.getVoId()))
			JsonUtils.addTableManagedButton(unions, table, removeButton);

		// adds the table into the panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(sp);

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
		return SmallIcons.INSTANCE.groupLinkIcon();
	}

	@Override
	public boolean multipleInstancesEnabled() {
		return false;
	}

	@Override
	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(group, "Relations", getUrlWithParameters());
		if (group != null) {
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);
	}

	@Override
	public boolean isAuthorized() {
		return session.isVoAdmin(group.getVoId()) || session.isVoObserver(group.getVoId()) || session.isGroupAdmin(group.getId());
	}

	@Override
	public boolean isPrepared() {
		return !(group == null);
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;

		GroupRelationsTabItem create = (GroupRelationsTabItem) o;
		if (groupId != create.groupId){
			return false;
		}
		return true;

	}

	@Override
	public int hashCode() {

		final int prime = 104743;
		int result = 1;
		result = prime * result + 6786786;
		return result;

	}

}
