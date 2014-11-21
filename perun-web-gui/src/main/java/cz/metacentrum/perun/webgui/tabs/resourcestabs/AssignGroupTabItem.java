package cz.metacentrum.perun.webgui.tabs.resourcestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllGroups;
import cz.metacentrum.perun.webgui.json.resourcesManager.AssignGroupsToResource;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedGroups;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Provides page with assign group to resource form
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
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
	private Label titleWidget = new Label("Loading resource");

	//data
	private int resourceId;
	private Resource resource;

	/**
	 * @param resourceId ID of resource to have group assigned
	 */
	public AssignGroupTabItem(int resourceId){
		this.resourceId = resourceId;
		new GetEntityById(PerunEntity.RESOURCE, resourceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				resource = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * Creates a tab instance
	 * @param resource ID of resource
	 */
	public AssignGroupTabItem(Resource resource){
		this.resource = resource;
		this.resourceId = resource.getId();
	}

	public boolean isPrepared(){
		return !(resource == null);
	}

	public Widget draw() {

		titleWidget.setText("Assign group");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// menu
		TabMenu menu = new TabMenu();

		final GetAllGroups voGroups = new GetAllGroups(resource.getVoId());
		voGroups.setCoreGroupsCheckable(true);
		final CellTable<Group> table = voGroups.getEmptyTable(new FieldUpdater<Group, String>() {
			public void update(int index, Group object, String value) {
				session.getTabManager().addTab(new GroupDetailTabItem(object));
			}
		});

		// remove already assigned groups from offering
		voGroups.setEvents(new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso){
				final GetAssignedGroups alreadyAssigned = new GetAssignedGroups(resourceId, new JsonCallbackEvents() {
					public void onFinished(JavaScriptObject jso){
						JsArray<Group> groupsToRemove = JsonUtils.jsoAsArray(jso);
						for (int i=0; i<groupsToRemove.length(); i++) {
							voGroups.removeFromTable(groupsToRemove.get(i));
						}
						// if single group, select
						if (voGroups.getList().size() == 1) {
							table.getSelectionModel().setSelected(voGroups.getList().get(0), true);
						}
					}
				});
				alreadyAssigned.retrieveData();
			}
		});

		// checbox is selected by default
		final CheckBox chb = new CheckBox();
		chb.setText(WidgetTranslation.INSTANCE.configureGroupBeforeAssign());
		chb.setTitle(WidgetTranslation.INSTANCE.configureGroupBeforeAssignTitle());
		chb.setValue(false);

		final TabItem tab = this;

		// button
		final CustomButton assignButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.assignSelectedGroupsToResource());
		assignButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ArrayList<Group> groupsToAssign = voGroups.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(groupsToAssign)) {
					if (chb.getValue() == false) {
						AssignGroupsToResource request = new AssignGroupsToResource(JsonCallbackEvents.closeTabDisableButtonEvents(assignButton, tab));
						request.assignGroupsToResource(groupsToAssign, resource);
					}
					if (chb.getValue() == true){
						session.getTabManager().addTabToCurrentTab(new ManageGroupsBeforeAssigning(resource, groupsToAssign), true);
					}
				}
			}
		});

		menu.addWidget(assignButton);
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, false);
			}
		}));

		menu.addFilterWidget(new ExtendedSuggestBox(voGroups.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				voGroups.filterTable(text);
				// if single group, select
				if (voGroups.getList().size() == 1) {
					table.getSelectionModel().setSelected(voGroups.getList().get(0), true);
				}
			}
		}, ButtonTranslation.INSTANCE.filterGroup());

		if (session.isPerunAdmin()) {
			// TODO - now is for testing so perun admin only
			menu.addWidget(chb);
		}
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		assignButton.setEnabled(false);
		JsonUtils.addTableManagedButton(voGroups, table, assignButton);

		voGroups.retrieveData();

		table.addStyleName("perun-table");
		table.setWidth("100%");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		vp.add(sp);

		session.getUiElements().resizeSmallTabPanel(sp, 350, this);
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
		final int prime = 1009;
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
		AssignGroupTabItem other = (AssignGroupTabItem) obj;
		if (resourceId != other.resourceId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {}

	public boolean isAuthorized() {

		if (session.isVoAdmin(resource.getVoId())) {
			return true;
		} else {
			return false;
		}

	}

}
