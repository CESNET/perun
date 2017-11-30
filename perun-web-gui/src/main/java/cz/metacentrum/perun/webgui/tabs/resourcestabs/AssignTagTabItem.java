package cz.metacentrum.perun.webgui.tabs.resourcestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.resourcesManager.AssignResourceTag;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAllResourcesTags;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.ResourceTag;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Provides page with assign tag to resource form
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AssignTagTabItem implements TabItem {

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
	 * Create tab instance
	 * @param resourceId ID of resource to have tags assigned
	 */
	public AssignTagTabItem(int resourceId){
		this.resourceId = resourceId;
		new GetEntityById(PerunEntity.RESOURCE, resourceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				resource = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * Creates a tab instance
	 * @param resource resource to have tags assigned
	 */
	public AssignTagTabItem(Resource resource){
		this.resource = resource;
		this.resourceId = resource.getId();
	}

	public boolean isPrepared(){
		return !(resource == null);
	}

	public Widget draw() {

		titleWidget.setText("Add tag");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// menu
		TabMenu menu = new TabMenu();

		final GetAllResourcesTags tags = new GetAllResourcesTags(PerunEntity.VIRTUAL_ORGANIZATION, resource.getVoId());
		final CellTable<ResourceTag> table = tags.getTable();

		final TabItem tab = this;

		// button
		final CustomButton assignButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.assignSelectedTagsToResource());
		assignButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ArrayList<ResourceTag> tagsToAssign = tags.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(tagsToAssign)) {
					for (int i = 0; i < tagsToAssign.size(); i++) {
						if (i != tagsToAssign.size() - 1) {                     // call json normaly
							AssignResourceTag request = new AssignResourceTag(resourceId, JsonCallbackEvents.disableButtonEvents(assignButton));
							request.assignResourceTag(tagsToAssign.get(i));
						} else {                                                // last change - call json with update
							AssignResourceTag request = new AssignResourceTag(resourceId, JsonCallbackEvents.closeTabDisableButtonEvents(assignButton, tab));
							request.assignResourceTag(tagsToAssign.get(i));
						}
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

		menu.addFilterWidget(new ExtendedSuggestBox(tags.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				tags.filterTable(text);
				// if single group, select
				if (tags.getList().size() == 1) {
					table.getSelectionModel().setSelected(tags.getList().get(0), true);
				}
			}
		}, ButtonTranslation.INSTANCE.filterGroup());

		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		assignButton.setEnabled(false);
		JsonUtils.addTableManagedButton(tags, table, assignButton);

		tags.retrieveData();

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
		return SmallIcons.INSTANCE.addIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1013;
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
		AssignTagTabItem other = (AssignTagTabItem) obj;
		if (resourceId != other.resourceId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(resource.getVoId())) {
			return true;
		} else {
			return false;
		}

	}

}
