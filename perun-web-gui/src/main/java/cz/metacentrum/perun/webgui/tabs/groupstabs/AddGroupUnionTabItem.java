package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
import cz.metacentrum.perun.webgui.json.groupsManager.AddGroupUnion;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllGroups;
import cz.metacentrum.perun.webgui.json.groupsManager.GetGroupUnions;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides page with add Group union form
 *
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Michal Krajcovic <mkrajcovic@mail.muni.cz>
 */
public class AddGroupUnionTabItem implements TabItem {

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
	private Label titleWidget = new Label("Add Group union");

	//data
	public int groupId;
	private Group group;

	private List<Group> alreadyAddedList = new ArrayList<>();
	private SimplePanel alreadyAdded = new SimplePanel();

	public AddGroupUnionTabItem(Group group) {
		this.group = group;
		this.groupId = group.getId();
	}

	@Override
	public Widget draw() {
		titleWidget.setText("Add group union");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// menu
		TabMenu menu = new TabMenu();
		menu.addWidget(new HTML(""));

		final GetAllGroups groups = new GetAllGroups(group.getVoId());
		groups.setCoreGroupsCheckable(true);

		// remove already added union groups from offering
		JsonCallbackEvents localEvents = new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				// second callback
				final GetGroupUnions alreadyAssigned = new GetGroupUnions(group, false, new JsonCallbackEvents() {
					public void onFinished(JavaScriptObject jso) {
						JsArray<Group> esToRemove = JsonUtils.jsoAsArray(jso);
						for (int i = 0; i < esToRemove.length(); i++) {
							groups.removeFromTable(esToRemove.get(i));
						}
						// remove itself
						groups.removeFromTable(group);
					}
				});
				alreadyAssigned.retrieveData();
			}
		};
		groups.setEvents(localEvents);

		final ExtendedSuggestBox box = new ExtendedSuggestBox(groups.getOracle());

		// button
		final CustomButton assignButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedExtSource());
		final TabItem tab = this;

		assignButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<Group> groupsToAdd = groups.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(groupsToAdd)) {
					// FIXME - Should have only one callback to core
					for (int i = 0; i < groupsToAdd.size(); i++) {
						final int n = i;
						AddGroupUnion request = new AddGroupUnion(JsonCallbackEvents.disableButtonEvents(assignButton, new JsonCallbackEvents() {
							@Override
							public void onFinished(JavaScriptObject jso) {
								// unselect added person
								groups.getSelectionModel().setSelected(groupsToAdd.get(n), false);
								alreadyAddedList.add(groupsToAdd.get(n));
								rebuildAlreadyAddedWidget();
								// clear search
								box.getSuggestBox().setText("");
							}
						}));
						request.createGroupUnion(group, groupsToAdd.get(i));
					}
				}
			}
		});

		menu.addFilterWidget(box, new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				groups.filterTable(text);
			}
		}, "Filter by ext source name or type");

		menu.addWidget(assignButton);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		vp.add(alreadyAdded);

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

	/**
	 * Rebuild already added widget based on already added ext sources
	 */
	private void rebuildAlreadyAddedWidget() {

		alreadyAdded.setStyleName("alreadyAdded");
		alreadyAdded.setVisible(!alreadyAddedList.isEmpty());
		alreadyAdded.setWidget(new HTML("<strong>Already added: </strong>"));
		for (int i = 0; i < alreadyAddedList.size(); i++) {
			alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML() + ((i != 0) ? ", " : "") + SafeHtmlUtils.fromString(alreadyAddedList.get(i).getName()).asString());
		}
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

		AddGroupUnionTabItem create = (AddGroupUnionTabItem) o;
		if (groupId != create.groupId){
			return false;
		}
		return true;

	}

	@Override
	public int hashCode() {

		final int prime = 104759;
		int result = 1;
		result = prime * result + 6786786;
		return result;

	}

	@Override
	public boolean multipleInstancesEnabled() {
		return false;
	}

	@Override
	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		if (group != null) {
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);
	}

	@Override
	public boolean isAuthorized() {
		return (session.isVoAdmin(group.getVoId()) || session.isGroupAdmin(group.getId()));
	}

	@Override
	public boolean isPrepared() {
		return !(group == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return !alreadyAddedList.isEmpty();
	}

	@Override
	public void onClose() {

	}

	public final static String URL = "add-grp-union";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
	}
}
