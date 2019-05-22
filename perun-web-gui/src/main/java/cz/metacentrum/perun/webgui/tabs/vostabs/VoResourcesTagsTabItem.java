package cz.metacentrum.perun.webgui.tabs.vostabs;

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
import cz.metacentrum.perun.webgui.json.resourcesManager.DeleteResourceTag;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAllResourcesTags;
import cz.metacentrum.perun.webgui.json.resourcesManager.UpdateResourceTag;
import cz.metacentrum.perun.webgui.model.ResourceTag;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab for resources tags management in VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class VoResourcesTagsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading vo resources tags");

	// data
	private VirtualOrganization vo;
	//
	private int voId;

	/**
	 * Creates a tab instance
	 *
	 * @param vo
	 */
	public VoResourcesTagsTabItem(VirtualOrganization vo){
		this.vo = vo;
		this.voId = vo.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param voId
	 */
	public VoResourcesTagsTabItem(int voId){
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

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}


	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName())+": resources tags");

		// MAIN PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu menu = new TabMenu();
		// refresh
		menu.addWidget(UiElements.getRefreshButton(this));

		// members request
		final GetAllResourcesTags resTags = new GetAllResourcesTags(PerunEntity.VIRTUAL_ORGANIZATION, voId);

		if (!session.isVoAdmin(voId)) resTags.setCheckable(false);
		if (session.isVoAdmin(voId)) resTags.setEditable(true);

		// Events for reloading when finished
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(resTags);

		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.CREATE, true, ButtonTranslation.INSTANCE.createResourceTag(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new CreateVoResourceTagTabItem(voId));
			}
		});
		menu.addWidget(addButton);
		if (!session.isVoAdmin(voId)) addButton.setEnabled(false);

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.DELETE, ButtonTranslation.INSTANCE.deleteResourceTag());
		if (!session.isVoAdmin(voId)) removeButton.setEnabled(false);
		menu.addWidget(removeButton);
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<ResourceTag> tagsToRemove = resTags.getTableSelectedList();
				String text = "Following tags will be deleted and won't be used to tag VO resources.";
				UiElements.showDeleteConfirm(tagsToRemove, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i=0; i<tagsToRemove.size(); i++ ) {
							DeleteResourceTag request;
							if(i == tagsToRemove.size() - 1) {
								request = new DeleteResourceTag(JsonCallbackEvents.disableButtonEvents(removeButton, events));
							} else {
								request = new DeleteResourceTag(JsonCallbackEvents.disableButtonEvents(removeButton));
							}
							request.deleteResourceTag(tagsToRemove.get(i));
						}
					}
				});
			}
		});

		menu.addFilterWidget(new ExtendedSuggestBox(resTags.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				resTags.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterTags());

		final CustomButton saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.updateResourceTag());
		menu.addWidget(saveButton);
		if (!session.isVoAdmin(voId)) saveButton.setEnabled(false);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<ResourceTag> tagsToUpdate = resTags.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(tagsToUpdate)) {
					// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
					for (int i=0; i<tagsToUpdate.size(); i++ ) {
						UpdateResourceTag request;
						if(i == tagsToUpdate.size() - 1) {
							request = new UpdateResourceTag(JsonCallbackEvents.disableButtonEvents(saveButton, events));
						} else {
							request = new UpdateResourceTag(JsonCallbackEvents.disableButtonEvents(saveButton));
						}
						request.updateResourceTag(tagsToUpdate.get(i));
					}
				}
			}
		});

		// get the table
		CellTable<ResourceTag> table = resTags.getTable();

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		// add menu and the table to the main panel
		firstTabPanel.add(menu);
		firstTabPanel.setCellHeight(menu, "30px");
		firstTabPanel.add(sp);

		removeButton.setEnabled(false);
		if (session.isVoAdmin(voId)) JsonUtils.addTableManagedButton(resTags, table, removeButton);
		saveButton.setEnabled(false);
		if (session.isVoAdmin(voId)) JsonUtils.addTableManagedButton(resTags, table, saveButton);


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
		return SmallIcons.INSTANCE.tagOrangeIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1361;
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
		VoResourcesTagsTabItem other = (VoResourcesTagsTabItem) obj;
		if (voId != other.voId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(vo, "Resources tags", getUrlWithParameters());
		if(vo != null){
			session.setActiveVo(vo);
			return;
		}
		session.setActiveVoId(voId);
	}


	public boolean isAuthorized() {

		if (session.isVoAdmin(voId) || session.isVoObserver(voId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "tags";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + voId;
	}

	static public VoResourcesTagsTabItem load(Map<String, String> parameters) {
		int voId = Integer.parseInt(parameters.get("id"));
		return new VoResourcesTagsTabItem(voId);
	}

}
