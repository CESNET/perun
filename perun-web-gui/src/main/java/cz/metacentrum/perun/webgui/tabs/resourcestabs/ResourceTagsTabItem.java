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
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAllResourcesTags;
import cz.metacentrum.perun.webgui.json.resourcesManager.RemoveResourceTag;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.ResourceTag;
import cz.metacentrum.perun.webgui.tabs.ResourcesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Provides page with tags resources managements (assign / remove)
 * Used by VO administrators
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ResourceTagsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Manage assigned tags");

	// data
	private int resourceId;
	private Resource resource;

	/**
	 * Creates a tab instance
	 * @param resourceId ID of resource to get groups management for
	 */
	public ResourceTagsTabItem(int resourceId){
		this.resourceId = resourceId;
		new GetEntityById(PerunEntity.RESOURCE, resourceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				resource = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * Creates a tab instance
	 * @param resource resource to get groups management for
	 */
	public ResourceTagsTabItem(Resource resource){
		this.resource = resource;
		this.resourceId = resource.getId();
	}

	public boolean isPrepared(){
		return !(resource == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(resource.getName()) + ": manage assigned tags");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));

		final GetAllResourcesTags tags = new GetAllResourcesTags(PerunEntity.RESOURCE, resourceId);

		final JsonCallbackEvents localEvents = JsonCallbackEvents.refreshTableEvents(tags);

		CustomButton assignTagsButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.assignTagsToResource(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AssignTagTabItem(resource), true);
			}
		});

		final CustomButton removeTagsButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeSelectedTagsFromResource());
		removeTagsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<ResourceTag> tagsToRemove = tags.getTableSelectedList();
				String text = "Following groups will be removed from resource.";
				UiElements.showDeleteConfirm(tagsToRemove, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD USE ONLY ONE CALLBACK TO CORE !!
						for (int i = 0; i < tagsToRemove.size(); i++) {
							if (i == tagsToRemove.size() - 1) {
								RemoveResourceTag request = new RemoveResourceTag(resource.getId(), JsonCallbackEvents.disableButtonEvents(removeTagsButton, localEvents));
								request.removeResourceTag(tagsToRemove.get(i));
							} else {
								RemoveResourceTag request = new RemoveResourceTag(resource.getId(), JsonCallbackEvents.disableButtonEvents(removeTagsButton));
								request.removeResourceTag(tagsToRemove.get(i));
							}
						}
					}
				});
			}
		});

		if (!session.isVoAdmin(resource.getVoId())) {
			assignTagsButton.setEnabled(false);
			removeTagsButton.setEnabled(false);
			tags.setCheckable(false);
		}
		menu.addWidget(assignTagsButton);
		menu.addWidget(removeTagsButton);

		menu.addFilterWidget(new ExtendedSuggestBox(tags.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				tags.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterTags());

		// display menu to vo admin only
		vp.add(menu);
		vp.setCellHeight(menu,"30px");

		// table with field updater which leads to group detail
		CellTable<ResourceTag> table = tags.getTable();

		removeTagsButton.setEnabled(false);
		JsonUtils.addTableManagedButton(tags, table, removeTagsButton);

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
		return SmallIcons.INSTANCE.tagOrangeIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1049;
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
		ResourceTagsTabItem other = (ResourceTagsTabItem) obj;
		if (resourceId != other.resourceId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
		session.setActiveVoId(resource.getVoId());
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(resource.getVoId()) || session.isVoObserver(resource.getVoId())) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "manage-tags";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return ResourcesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + resourceId;
	}

	static public ResourceTagsTabItem load(Map<String, String> parameters) {
		int id = Integer.parseInt(parameters.get("id"));
		return new ResourceTagsTabItem(id);
	}

	static public ResourceTagsTabItem load(Resource resource) {
		return new ResourceTagsTabItem(resource);
	}

}
