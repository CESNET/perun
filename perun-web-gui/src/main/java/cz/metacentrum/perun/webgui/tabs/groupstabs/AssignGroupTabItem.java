package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.resourcesManager.AssignGroupToResources;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetRichResources;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.RichResource;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Provides page with assign group to resource
 * !!! USE ONLY AS INNER TAB !!!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AssignGroupTabItem implements TabItem {

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
	private Label titleWidget = new Label("Loading group");

	/**
	 * Entity ID to set
	 */
	private Group group;
	private int groupId;
	private ArrayList<RichResource> resources;
	private ArrayList<RichResource> alreadyAddedList = new ArrayList<RichResource>();
	private SimplePanel alreadyAdded = new SimplePanel();

	/**
	 * Creates a tab instance
	 *
	 * @param group group to add admin into
	 */
	public AssignGroupTabItem(Group group, ArrayList<RichResource> resources){
		this.group = group;
		this.groupId = group.getId();
		this.resources = resources;
	}

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

	public Widget draw() {

		// set tab name
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName())+ ": assign to resource");

		// main content
		VerticalPanel mainTab = new VerticalPanel();
		mainTab.setSize("100%", "100%");

		// menu
		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));

		// callback
		final GetRichResources callback = new GetRichResources(group.getVoId());
		callback.setEvents(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				for (RichResource rr : resources) {
					callback.removeFromTable(rr);
				}
				ArrayList<RichResource> temp = new ArrayList<RichResource>();
				temp.addAll(callback.getList());
				callback.setList(temp);
			}
		});
		CellTable<RichResource> table = callback.getTable();

		// close tab event
		final TabItem tab = this;

		// filter box
		menu.addFilterWidget(new ExtendedSuggestBox(callback.getOracle()), new PerunSearchEvent() {
			public void searchFor(String text) {
				callback.filterTable(text);
				// if single group, select
				if (callback.getList().size() == 1) {
					callback.getSelectionModel().setSelected(callback.getList().get(0), true);
				}
			}
		}, ButtonTranslation.INSTANCE.filterResources());

		// button
		final CustomButton assignButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.assignGroupToSelectedResources());
		assignButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<RichResource> toAssign = callback.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(toAssign)) {
					AssignGroupToResources request = new AssignGroupToResources(JsonCallbackEvents.disableButtonEvents(assignButton, new JsonCallbackEvents(){
						@Override
						public void onFinished(JavaScriptObject jso) {
							alreadyAddedList.addAll(toAssign);
							for (RichResource r : toAssign) {
								callback.getSelectionModel().setSelected(r, false);
							}
							rebuildAlreadyAddedWidget();
						}
					}));
					request.assignGroupToResources(group, toAssign);
				}
			}
		});
		menu.addWidget(assignButton);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				// fixme - probably should be button for finish and close
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		assignButton.setEnabled(false);
		JsonUtils.addTableManagedButton(callback, table, assignButton);

		// add menu and the table to the main panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		// do not use resizePerunTable() in overlay mode - calculated width is wrong
		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

		mainTab.add(menu);
		mainTab.setCellHeight(menu, "30px");

		rebuildAlreadyAddedWidget();
		mainTab.add(alreadyAdded);

		mainTab.add(sp);

		this.contentWidget.setWidget(mainTab);

		return getWidget();
	}

	/**
	 * Rebuild already added widget based on already added members
	 */
	private void rebuildAlreadyAddedWidget() {

		alreadyAdded.setStyleName("alreadyAdded");
		alreadyAdded.setVisible(!alreadyAddedList.isEmpty());
		alreadyAdded.setWidget(new HTML("<strong>Already assigned to resources: </strong>"));
		for (int i=0; i<alreadyAddedList.size(); i++) {
			alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML()+ ((i!=0) ? ", " : "") + SafeHtmlUtils.fromString(alreadyAddedList.get(i).getName()).asString());
		}

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
		final int prime = 809;
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

		AssignGroupTabItem create = (AssignGroupTabItem) obj;
		if (groupId != create.groupId){
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		if(group != null){
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);

	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(group.getVoId())) {
			return true;
		} else {
			return false;
		}

	}

}
