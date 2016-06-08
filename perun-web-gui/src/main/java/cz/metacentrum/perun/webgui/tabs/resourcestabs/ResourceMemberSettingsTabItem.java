package cz.metacentrum.perun.webgui.tabs.resourcestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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
import cz.metacentrum.perun.webgui.json.attributesManager.*;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedRichMembers;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedServices;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.ResourcesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.attributestabs.SetNewAttributeTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides page with member, member-resource, user, user-facility attributes settings
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ResourceMemberSettingsTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("member settings");

	//data
	private int resourceId;
	private RichResource resource;

	private int lastSelectedService = 0;
	private int lastSelectedMember = 0;

	private boolean memberCallDone = false;
	private boolean servCallDone = false;

	/**
	 * @param resourceId ID of resource to get attributes for
	 */
	public ResourceMemberSettingsTabItem(final int resourceId){
		this.resourceId = resourceId;
		new GetEntityById(PerunEntity.RICH_RESOURCE, resourceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				resource = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * @param resource resource to get attributes for
	 */
	public ResourceMemberSettingsTabItem(RichResource resource){
		this.resource = resource;
		this.resourceId = resource.getId();
	}

	public boolean isPrepared(){
		return !(resource == null);
	}

	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(resource.getName()) + ": member settings");

		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// menu
		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));

		final ListBoxWithObjects<RichMember> memberListBox = new ListBoxWithObjects<>();
		final ListBoxWithObjects<Service> serviceListBox = new ListBoxWithObjects<>();

		// load
		final Map<String,Integer> ids = new HashMap<String,Integer>();

		// to get attributes for all services
		final GetResourceRequiredAttributesV2 resReqAttrs = new GetResourceRequiredAttributesV2();
		final GetRequiredAttributesV2 reqAttrs = new GetRequiredAttributesV2(ids, JsonCallbackEvents.passDataToAnotherCallback(resReqAttrs));

		// puts first table
		final CellTable<Attribute> table = resReqAttrs.getEmptyTable();

		// get assigned members
		final GetAssignedRichMembers members = new GetAssignedRichMembers(resourceId, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso){
				memberListBox.removeAllOption();
				memberListBox.clear();
				ArrayList<RichMember> grp = JsonUtils.jsoAsList(jso);
				grp = new TableSorter<RichMember>().sortByName(grp);
				if (grp == null || grp.isEmpty()) {
					memberListBox.addItem("No members assigned");
					lastSelectedMember = 0;
					((AjaxLoaderImage)table.getEmptyTableWidget()).setEmptyResultMessage("No group or service assigned. Please assign them first to see member-resource setting.");
					((AjaxLoaderImage)table.getEmptyTableWidget()).loadingFinished();
					return;
				}
				memberListBox.addAllItems(grp);
				for (RichMember g : memberListBox.getAllObjects()) {
					if (lastSelectedMember != 0 && g.getId() == lastSelectedMember) memberListBox.setSelected(g, true);
				}
				memberCallDone = true;

				if (lastSelectedService == 0) {
					// load resource-required
					if (servCallDone) {
						Map<String, Integer> ids = new HashMap<String, Integer>();
						ids.put("resource", resourceId);
						ids.put("resourceToGetServicesFrom", resourceId);
						ids.put("member", memberListBox.getSelectedObject().getId());
						ids.put("workWithUserAttributes", 1);
						resReqAttrs.setIds(ids);
						resReqAttrs.retrieveData();
					}
				} else {
					// load resource-service-member
					if (servCallDone) {
						Map<String, Integer> ids = new HashMap<String, Integer>();
						ids.put("resource", resourceId);
						ids.put("service", lastSelectedService);
						ids.put("member", memberListBox.getSelectedObject().getId());
						ids.put("workWithUserAttributes", 1);
						reqAttrs.setIds(ids);
						reqAttrs.retrieveData();
					}
				}

			}
			@Override
			public void onError(PerunError error){
				memberListBox.removeAllOption();
				memberListBox.clear();
				memberListBox.addItem("Error while loading");
				memberCallDone = true;
			}
			@Override
			public void onLoadingStart(){
				memberListBox.removeAllOption();
				memberListBox.clear();
				memberListBox.addItem("Loading...");
				memberCallDone = false;
			}
		});
		members.retrieveData();

		GetAssignedServices services = new GetAssignedServices(resourceId, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso){

				serviceListBox.removeAllOption();
				serviceListBox.clear();
				ArrayList<Service> srv = JsonUtils.jsoAsList(jso);
				srv = new TableSorter<Service>().sortByName(srv);
				if (srv == null || srv.isEmpty()) {
					serviceListBox.addItem("No services assigned");
					lastSelectedService = 0;
					((AjaxLoaderImage)table.getEmptyTableWidget()).setEmptyResultMessage("No group or service assigned. Please assign them first to see member-resource setting.");
					((AjaxLoaderImage)table.getEmptyTableWidget()).loadingFinished();
					return;
				}
				serviceListBox.addAllItems(srv);
				serviceListBox.addAllOption();
				if (lastSelectedService == 0) serviceListBox.setSelectedIndex(0);
				for (Service s : serviceListBox.getAllObjects()) {
					if (lastSelectedService != 0 && s.getId() == lastSelectedService) serviceListBox.setSelected(s, true);
				}
				servCallDone = true;

				if (lastSelectedService == 0) {
					if (memberCallDone) {
						// load resource-required
						Map<String, Integer> ids = new HashMap<String, Integer>();
						ids.put("resource", resourceId);
						ids.put("resourceToGetServicesFrom", resourceId);
						ids.put("member", memberListBox.getSelectedObject().getId());
						ids.put("workWithUserAttributes", 1);
						resReqAttrs.setIds(ids);
						resReqAttrs.retrieveData();
					}
				} else {
					if (memberCallDone) {
						// load resource-service-member
						Map<String, Integer> ids = new HashMap<String, Integer>();
						ids.put("resource", resourceId);
						ids.put("service", lastSelectedService);
						ids.put("member", memberListBox.getSelectedObject().getId());
						ids.put("workWithUserAttributes", 1);
						reqAttrs.setIds(ids);
						reqAttrs.retrieveData();
					}
				}

			}
			@Override
			public void onError(PerunError error){
				serviceListBox.removeAllOption();
				serviceListBox.clear();
				serviceListBox.addItem("Error while loading");
				servCallDone = true;
			}
			@Override
			public void onLoadingStart(){
				serviceListBox.removeAllOption();
				serviceListBox.clear();
				serviceListBox.addItem("Loading...");
				servCallDone = false;
			}


		});
		services.retrieveData();


		ChangeHandler changeHandler = new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {

				if (memberCallDone && servCallDone) {

					if (memberListBox.getSelectedObject() != null) {
						lastSelectedMember = memberListBox.getSelectedObject().getId();
					} else {
						lastSelectedMember = 0;
					}

					if (serviceListBox.getSelectedIndex() > 0) {
						lastSelectedService = serviceListBox.getSelectedObject().getId();
					} else {
						lastSelectedService = 0;
					}

					if (lastSelectedService == 0) {
						// load resource-required
						Map<String, Integer> ids = new HashMap<String, Integer>();
						ids.put("resource", resourceId);
						ids.put("resourceToGetServicesFrom", resourceId);
						ids.put("member", memberListBox.getSelectedObject().getId());
						ids.put("workWithUserAttributes", 1);
						resReqAttrs.setIds(ids);
						resReqAttrs.retrieveData();
					} else {
						// load resource-service-member
						Map<String, Integer> ids = new HashMap<String, Integer>();
						ids.put("resource", resourceId);
						ids.put("service", lastSelectedService);
						ids.put("member", memberListBox.getSelectedObject().getId());
						ids.put("workWithUserAttributes", 1);
						reqAttrs.setIds(ids);
						reqAttrs.retrieveData();
					}

				}

			}
		};
		memberListBox.addChangeHandler(changeHandler);
		serviceListBox.addChangeHandler(changeHandler);

		final JsonCallbackEvents refreshTable = JsonCallbackEvents.refreshTableEvents(resReqAttrs);

		if (!session.isVoAdmin(resource.getVoId()) && !session.isFacilityAdmin(resource.getFacilityId())) resReqAttrs.setCheckable(false);

		// add save changes to menu
		final CustomButton saveChangesButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveChangesInAttributes());
		menu.addWidget(saveChangesButton);

		// set button disable event
		final JsonCallbackEvents saveChangesButtonEvent = JsonCallbackEvents.disableButtonEvents(saveChangesButton, refreshTable);
		if (!session.isVoAdmin(resource.getVoId()) && !session.isFacilityAdmin(resource.getFacilityId())) saveChangesButton.setEnabled(false);
		saveChangesButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				ArrayList<Attribute> list = resReqAttrs.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					Map<String, Integer> ids = new HashMap<String,Integer>();
					ids.put("resource", resourceId);
					ids.put("member", memberListBox.getSelectedObject().getId());
					ids.put("workWithUserAttributes", 1);
					SetAttributes request = new SetAttributes(saveChangesButtonEvent);
					request.setAttributes(ids, list);
				}
			}
		});

		// add set new to menu
		CustomButton setNewAttributeButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.setNewAttributes(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("resource", resourceId);
				ids.put("member", memberListBox.getSelectedObject().getId());
				ids.put("user", memberListBox.getSelectedObject().getUserId());
				ids.put("facility", resource.getFacilityId());
				session.getTabManager().addTabToCurrentTab(new SetNewAttributeTabItem(ids, resReqAttrs.getList()), true);
			}
		});
		if (!session.isVoAdmin(resource.getVoId()) && !session.isFacilityAdmin(resource.getFacilityId())) setNewAttributeButton.setEnabled(false);
		menu.addWidget(setNewAttributeButton);

		// remove attr button
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeAttributes());

		// remove button event
		final JsonCallbackEvents removeButtonEvent = JsonCallbackEvents.disableButtonEvents(removeButton, refreshTable);
		if (!session.isVoAdmin(resource.getVoId()) && !session.isFacilityAdmin(resource.getFacilityId())) removeButton.setEnabled(false);
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				ArrayList<Attribute> list = resReqAttrs.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					Map<String, Integer> ids = new HashMap<String, Integer>();
					ids.put("resource", resourceId);
					ids.put("member", memberListBox.getSelectedObject().getId());
					ids.put("facility", resource.getFacilityId());
					ids.put("user", memberListBox.getSelectedObject().getUserId());
					RemoveAttributes request = new RemoveAttributes(removeButtonEvent);
					request.removeAttributes(ids, list);
				}

			}
		});

		menu.addWidget(removeButton);

		// add member selection to menu
		menu.addWidget(new HTML("<strong>Selected&nbsp;member: </strong>"));
		menu.addWidget(memberListBox);

		// add service selection to menu
		menu.addWidget(new HTML("<strong>Selected&nbsp;service: </strong>"));
		menu.addWidget(serviceListBox);

		table.addStyleName("perun-table");
		table.setWidth("100%");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		session.getUiElements().resizePerunTable(sp, 350, this);
		vp.add(sp);

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
		return SmallIcons.INSTANCE.settingToolsIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1531;
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
		ResourceMemberSettingsTabItem other = (ResourceMemberSettingsTabItem) obj;
		if (resourceId != other.resourceId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.setActiveVoId(resource.getVoId());
		session.setActiveFacilityId(resource.getFacilityId());
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(resource.getVoId()) || session.isFacilityAdmin(resource.getFacilityId())) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "memsettings";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return ResourcesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + resourceId;
	}

	static public ResourceGroupSettingsTabItem load(Map<String, String> parameters) {
		int id = Integer.parseInt(parameters.get("id"));
		return new ResourceGroupSettingsTabItem(id);
	}

}