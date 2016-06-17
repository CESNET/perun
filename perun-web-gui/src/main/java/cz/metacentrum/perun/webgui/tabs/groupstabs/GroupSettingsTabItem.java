package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesV2;
import cz.metacentrum.perun.webgui.json.attributesManager.GetResourceRequiredAttributesV2;
import cz.metacentrum.perun.webgui.json.attributesManager.RemoveAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.json.groupsManager.GetGroupRichMembers;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedResources;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.attributestabs.SetNewAttributeTabItem;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tab with Group Attributes for Group Admin
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GroupSettingsTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading group settings");

	/**
	 * Group ID
	 */
	private Group group;

	private int groupId;

	/**
	 * List of resources we can use instead of assigned
	 */
	private ArrayList<Resource> resources = null;

	private int lastSelectedResourceId = 0;
	private int lastSelectedMemberId = 0;
	private boolean resCallDone = false;
	private boolean memCallDone = false;

	/**
	 * Creates a tab instance
	 * @param group
	 */
	public GroupSettingsTabItem(Group group){
		this.group = group;
		this.groupId = group.getId();
	}

	/**
	 * Creates a tab instance
	 * @param groupId
	 */
	public GroupSettingsTabItem(int groupId){
		this.groupId = groupId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				group = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.GROUP, groupId, events).retrieveData();
	}

	public boolean isPrepared(){
		return !(group == null);
	}

	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName()) + ": settings");

		// Main panel
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		final TabMenu menu = new TabMenu();

		// Adds menu to the main panel
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		// IDS
		final Map<String, Integer> ids = new HashMap<String, Integer>();
		ids.put("group", groupId);

		// define GET ATTRIBUTES callback
		final GetAttributesV2 jsonCallback = new GetAttributesV2(true);
		jsonCallback.setIds(ids);
		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) jsonCallback.setCheckable(false);
		final CellTable<Attribute> table = jsonCallback.getEmptyTable();
		table.setWidth("100%");
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		session.getUiElements().resizePerunTable(sp, 350, this);
		vp.add(sp);

		// define GET RES. REQ ATTRIBUTES callback
		final GetResourceRequiredAttributesV2 reqAttrs = new GetResourceRequiredAttributesV2(null, JsonCallbackEvents.passDataToAnotherCallback(jsonCallback));

		// GROUP RESOURCES LISTBOX
		final ListBoxWithObjects<Resource> resourceDropDown = new ListBoxWithObjects<Resource>();
		resourceDropDown.setTitle("By selecting a resource you will switch to either group-resource or member-resource setting (you must pick member too)");

		// fill table with group attributes on page load
		if (lastSelectedMemberId == 0 && lastSelectedResourceId == 0) {
			jsonCallback.retrieveData();
		} else {
			// load attrs by listbox selection after calls are done
			Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
				@Override
				public boolean execute() {
					// load proper table if something was selected
					if (resCallDone && memCallDone) {
						// trigger change event
						DomEvent.fireNativeEvent(Document.get().createChangeEvent(), resourceDropDown);
						return false;
					}
					return true;
				}
			}, 200);
		}

		// fill resources listbox
		if (resources == null) {
			// get assigned resources
			GetAssignedResources resources = new GetAssignedResources(groupId, PerunEntity.GROUP, new JsonCallbackEvents() {
				public void onFinished(JavaScriptObject jso){
					ArrayList<Resource> resources = JsonUtils.jsoAsList(jso);
					resourceDropDown.clear();
					if (resources != null && !resources.isEmpty()) {
						resources = new TableSorter<Resource>().sortByName(resources);
						resourceDropDown.addNotSelectedOption();
						for (int i=0; i<resources.size(); i++) {
							resourceDropDown.addItem(resources.get(i));
							if (lastSelectedResourceId == resources.get(i).getId()) {
								resourceDropDown.setSelected(resources.get(i), true);
							}
						}
					} else {
						resourceDropDown.addItem("No resources available");
					}
					resCallDone = true;
				}
				public void onError(PerunError error) {
					resourceDropDown.clear();
					resourceDropDown.addItem("Error while loading");
					resCallDone = true;
				}
				public void onLoadingStart() {
					resCallDone = false;
					resourceDropDown.clear();
					resourceDropDown.addItem("Loading...");
				}
			});
			resources.retrieveData();

		} else {
			// use predefined resources
			resourceDropDown.addNotSelectedOption();
			resources = new TableSorter<Resource>().sortByName(resources);
			resourceDropDown.addAllItems(resources);
		}

		// GROUP MEMBERS LISTBOX
		final ListBoxWithObjects<RichMember> membersDropDown = new ListBoxWithObjects<RichMember>();
		membersDropDown.setTitle("By selecting a member you will switch to either member or member-resource settings (you must pick resource too)");

		// fill members listbox
		GetGroupRichMembers members = new GetGroupRichMembers(groupId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				ArrayList<RichMember> mems = JsonUtils.jsoAsList(jso);
				membersDropDown.clear();
				if (mems != null && !mems.isEmpty()) {
					mems = new TableSorter<RichMember>().sortByName(mems);
					membersDropDown.addNotSelectedOption();
					for (int i=0; i<mems.size(); i++) {
						membersDropDown.addItem(mems.get(i));
						if (mems.get(i).getId() == lastSelectedMemberId) {
							membersDropDown.setSelected(mems.get(i), true);
						}
					}
				} else {
					membersDropDown.addItem("No members available");
				}
				memCallDone = true;
			}
			public void onError(PerunError error) {
				membersDropDown.clear();
				membersDropDown.addItem("Error while loading");
				memCallDone = true;
			}
			public void onLoadingStart() {
				memCallDone = false;
				membersDropDown.clear();
				membersDropDown.addItem("Loading...");
			}
		});
		members.retrieveData();

		// CHECKBOXES "FOR ALL MEMBERS"
		final CheckBox memChb = new CheckBox();
		memChb.setText("To all members");
		memChb.setVisible(false);
		memChb.setTitle("Uses this setting for all members of this group.");
		memChb.setValue(false);

		// SAVE CHANGES BUTTON
		final CustomButton saveChangesButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveChangesInAttributes());
		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) saveChangesButton.setEnabled(false);

		// click handler to save group and member-user attributes
		final ClickHandler saveAttrsClickHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<Attribute> list = jsonCallback.getTableSelectedList();

				if (UiElements.cantSaveEmptyListDialogBox(list)) {

					// refresh table and disable button a event for GetAttributes
					final JsonCallbackEvents events = JsonCallbackEvents.disableButtonEvents(saveChangesButton, JsonCallbackEvents.refreshTableEvents(jsonCallback));

					if (memChb.getValue() == true) {
						// to all members selected
						UiElements.generateInfo(WidgetTranslation.INSTANCE.saveConfirmTitle(),"Same value(s) for selected attribute(s) will be set to ALL members of group.",
								new ClickHandler() {
									public void onClick(ClickEvent event) {
										ArrayList<RichMember> memList = membersDropDown.getAllObjects();
										for (int i=0; i<memList.size(); i++){
											SetAttributes request = new SetAttributes();
											if (i == memList.size()-1) {
												request.setEvents(events);
											} else {
												request.setEvents(JsonCallbackEvents.disableButtonEvents(saveChangesButton));
											}
											Map<String, Integer> ids = new HashMap<String, Integer>();
											ids.put("member", memList.get(i).getId());
											ids.put("workWithUserAttributes", 1);
											request.setAttributes(ids, list);
										}
									}
						}
						);
					} else {
						// just one group / memeber
						SetAttributes request = new SetAttributes(events);
						request.setAttributes(jsonCallback.getIds(), list);
					}

				}
			}
		};

		// click handler to save required attributes
		final ClickHandler saveReqAttrsClickHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<Attribute> list = jsonCallback.getTableSelectedList();
				// check if not empty
				if (UiElements.cantSaveEmptyListDialogBox(list)) {

					// refresh table and disable button a event for GetResourceRequiredAttributes
					final JsonCallbackEvents events = JsonCallbackEvents.disableButtonEvents(saveChangesButton, new JsonCallbackEvents(){
						public void onFinished(JavaScriptObject jso) {
							jsonCallback.clearTable();
							// set back resourceToGetServicesFrom
							Map<String, Integer> ids =  reqAttrs.getIds();
							if (ids.containsKey("resource")) {
								ids.put("resourceToGetServicesFrom", ids.get("resource"));
							}
							if (ids.containsKey("user")) {
								ids.put("workWithUserAttributes", 1);
							}
							if (ids.containsKey("resource") && ids.containsKey("group")) {
								ids.put("workWithGroupAttributes", 1);
							}
							reqAttrs.setIds(ids);
							reqAttrs.retrieveData();
						}
					});

					final SetAttributes request = new SetAttributes();

					if (memChb.getValue() == true) {
						// to all members selected
						UiElements.generateInfo(WidgetTranslation.INSTANCE.saveConfirmTitle(),"Same value(s) for selected attribute(s) will be set to ALL members of group.", new ClickHandler() {
							public void onClick(ClickEvent event) {
								ArrayList<RichMember> memList = membersDropDown.getAllObjects();
								for (int i = 0; i < memList.size(); i++) {
									if (i == 0) {
										events.onLoadingStart();
									} // trigger disable button if first
									Map<String, Integer> ids = new HashMap<String, Integer>();
									ids.put("member", memList.get(i).getId());
									ids.put("user", memList.get(i).getUserId());
									ids.put("resource", resourceDropDown.getSelectedObject().getId());
									ids.put("facility", resourceDropDown.getSelectedObject().getFacilityId());
									if (i == memList.size() - 1) {
										request.setEvents(events);
									} // set events to last to get refresh and button enable
									request.setAttributes(ids, list);
								}
							}
						});
					} else {
						// group or member
						request.setEvents(events); // disable button events
						request.setAttributes(reqAttrs.getIds(), list);
					}

				}
			}
		};

		// REMOVE BUTTON
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeAttributes());
		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) removeButton.setEnabled(false);

		// click handler to remove group and member-user attributes
		final ClickHandler removeAttrsClickHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<Attribute> list = jsonCallback.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {

					// refresh table and disable button a event for GetAttributes
					final JsonCallbackEvents events = JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(jsonCallback));

					final RemoveAttributes request = new RemoveAttributes();

					if (memChb.getValue() == true) {
						// to all members selected
						UiElements.generateInfo(WidgetTranslation.INSTANCE.deleteConfirmTitle(),"Selected attribute(s) will be removed from ALL members of group.", new ClickHandler() {
							public void onClick(ClickEvent event) {
								ArrayList<RichMember> memList = membersDropDown.getAllObjects();
								for (int i=0; i<memList.size(); i++){
									if (i==0) { events.onLoadingStart(); } // trigger disable button if first
									Map<String, Integer> ids = new HashMap<String, Integer>();
									ids.put("member", memList.get(i).getId());
									ids.put("workWithUserAttributes", 1);
									if (i==memList.size()-1) { request.setEvents(events); } // set events to last to get refresh and button enable
									request.removeAttributes(ids, list);
								}}});
					} else {
						// just one group / memeber
						request.setEvents(events); // disable button events
						request.removeAttributes(jsonCallback.getIds(), list);
					}

				}

			}
		};

		// click handler to remove ResourceRequiredAttributes
		final ClickHandler removeReqAttrsClickHandler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<Attribute> list = jsonCallback.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {

					// refresh table and disable button a event for GetAttributes
					final JsonCallbackEvents events = JsonCallbackEvents.disableButtonEvents(removeButton, new JsonCallbackEvents(){
						public void onFinished(JavaScriptObject jso) {
							jsonCallback.clearTable();
							// set back resourceToGetServicesFrom
							Map<String, Integer> ids =  reqAttrs.getIds();
							if (ids.containsKey("resource")) {
								ids.put("resourceToGetServicesFrom", ids.get("resource"));
							}
							if (ids.containsKey("user")) {
								ids.put("workWithUserAttributes", 1);
							}
							if (ids.containsKey("resource") && ids.containsKey("group")) {
								ids.put("workWithGroupAttributes", 1);
							}
							reqAttrs.setIds(ids);
							reqAttrs.retrieveData();
						}
					});

					final RemoveAttributes request = new RemoveAttributes();

					if (memChb.getValue() == true) {
						// to all members selected
						UiElements.generateInfo(WidgetTranslation.INSTANCE.deleteConfirmTitle(), "Selected attribute(s) will be removed from ALL members of group.", new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								ArrayList<RichMember> memList = membersDropDown.getAllObjects();
								for (int i=0; i<memList.size(); i++){
									if (i==0) { events.onLoadingStart(); } // trigger disable button if first
									Map<String, Integer> ids = new HashMap<String, Integer>();
									ids.put("member", memList.get(i).getId());
									ids.put("user", memList.get(i).getUserId());
									ids.put("resource", resourceDropDown.getSelectedObject().getId());
									ids.put("facility", resourceDropDown.getSelectedObject().getFacilityId());
									if (i==memList.size()-1) { request.setEvents(events); } // set events to last to get refresh and button enable
									request.removeAttributes(ids, list);
								}
							}
						});
					} else {
						// just one group / member
						request.setEvents(events); // disable button events
						request.removeAttributes(reqAttrs.getIds(), list);
					}

				}
			}
		};

		// SET NEW ATTR BUTTON
		final CustomButton setNewAttributeButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.setNewAttributes());
		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) setNewAttributeButton.setEnabled(false);
		// click handler
		setNewAttributeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				ids.clear();
				if (resourceDropDown.getSelectedIndex() > 0) {
					if (membersDropDown.getSelectedIndex() > 0) {
						ids.put("member", membersDropDown.getSelectedObject().getId());
						ids.put("user", membersDropDown.getSelectedObject().getUserId());
						ids.put("resource", resourceDropDown.getSelectedObject().getId());
						ids.put("facility", resourceDropDown.getSelectedObject().getFacilityId());
					} else {
						ids.put("resource", resourceDropDown.getSelectedObject().getId());
						ids.put("group", groupId);
						ids.put("workWithGroupAttributes", 1);
					}
				} else {
					if (membersDropDown.getSelectedIndex() > 0) {
						ids.put("member", membersDropDown.getSelectedObject().getId());
						ids.put("user", membersDropDown.getSelectedObject().getUserId());
					} else {
						ids.put("group", groupId);
					}
				}
				session.getTabManager().addTabToCurrentTab(new SetNewAttributeTabItem(ids, jsonCallback.getList()), true);
			}
		});

		// ClickHandlersRegistration - save onClicks for GetAttributes
		final ArrayList<HandlerRegistration> clickHandlers = new ArrayList<HandlerRegistration>();
		clickHandlers.add(saveChangesButton.addClickHandler(saveAttrsClickHandler));
		clickHandlers.add(removeButton.addClickHandler(removeAttrsClickHandler));

		// DEFINE CHANGE HANDLER FOR ALL DROP DOWNS
		ChangeHandler changeHandler = new ChangeHandler(){
			public void onChange(ChangeEvent event) {

				int resIndex = resourceDropDown.getSelectedIndex();
				int memIndex = membersDropDown.getSelectedIndex();

				// no resource or member selected
				if (resIndex == 0 && memIndex == 0 ) {
					lastSelectedMemberId = 0;
					lastSelectedResourceId = 0;
					memChb.setValue(false);
					memChb.setVisible(false);
					jsonCallback.clearTable();
					ids.clear();
					ids.put("group", groupId);
					jsonCallback.setIds(ids);
					jsonCallback.retrieveData();
					for (HandlerRegistration handler : clickHandlers) { if (handler != null) {handler.removeHandler();} }
					clickHandlers.add(saveChangesButton.addClickHandler(saveAttrsClickHandler));
					clickHandlers.add(removeButton.addClickHandler(removeAttrsClickHandler));
				}
				// no resource but member selected
				if (resIndex == 0 && memIndex > 0 ) {
					lastSelectedMemberId = membersDropDown.getSelectedObject().getId();
					lastSelectedResourceId = 0;
					memChb.setValue(false);
					memChb.setVisible(true);
					jsonCallback.clearTable();
					ids.clear();
					ids.put("member", membersDropDown.getSelectedObject().getId());
					ids.put("workWithUserAttributes", 1);
					jsonCallback.setIds(ids);
					jsonCallback.retrieveData();
					for (HandlerRegistration handler : clickHandlers) { if (handler != null) {handler.removeHandler();} }
					clickHandlers.add(saveChangesButton.addClickHandler(saveAttrsClickHandler));
					clickHandlers.add(removeButton.addClickHandler(removeAttrsClickHandler));
				}
				// resource and no member selected
				if (resIndex > 0 && memIndex == 0 ) {
					lastSelectedMemberId = 0;
					lastSelectedResourceId = resourceDropDown.getSelectedObject().getId();
					memChb.setValue(false);
					memChb.setVisible(false);
					jsonCallback.clearTable();
					ids.clear();
					ids.put("group", groupId);
					ids.put("resource", resourceDropDown.getSelectedObject().getId());
					ids.put("resourceToGetServicesFrom", ids.get("resource"));
					ids.put("workWithGroupAttributes", 1);
					reqAttrs.setIds(ids);
					reqAttrs.retrieveData();
					// set ids back to make saveChanges work
					ids.remove("resourceToGetServicesFrom");
					reqAttrs.setIds(ids);
					for (HandlerRegistration handler : clickHandlers) { if (handler != null) {handler.removeHandler();} }
					clickHandlers.add(saveChangesButton.addClickHandler(saveReqAttrsClickHandler));
					clickHandlers.add(removeButton.addClickHandler(removeReqAttrsClickHandler));
				}
				// resource and member selected
				if (resIndex > 0 && memIndex > 0 ) {
					lastSelectedMemberId = membersDropDown.getSelectedObject().getId();
					lastSelectedResourceId = resourceDropDown.getSelectedObject().getId();
					memChb.setValue(false);
					memChb.setVisible(true);
					jsonCallback.clearTable();
					ids.clear();
					ids.put("resource", resourceDropDown.getSelectedObject().getId());
					ids.put("resourceToGetServicesFrom", ids.get("resource"));
					ids.put("member", membersDropDown.getSelectedObject().getId());
					ids.put("workWithUserAttributes", 1);
					reqAttrs.setIds(ids);
					reqAttrs.retrieveData();
					// set ids back to make saveChanges work
					ids.clear();
					ids.put("member", membersDropDown.getSelectedObject().getId());
					ids.put("user", membersDropDown.getSelectedObject().getUser().getId());
					ids.put("facility", resourceDropDown.getSelectedObject().getFacilityId());
					ids.put("resource", resourceDropDown.getSelectedObject().getId());
					reqAttrs.setIds(ids);
					for (HandlerRegistration handler : clickHandlers) { if (handler != null) {handler.removeHandler();} }
					clickHandlers.add(saveChangesButton.addClickHandler(saveReqAttrsClickHandler));
					clickHandlers.add(removeButton.addClickHandler(removeReqAttrsClickHandler));
				}
			}};

		// ADD CHANGE HANDLERS TO DROP-DOWN MENUS
		resourceDropDown.addChangeHandler(changeHandler);
		membersDropDown.addChangeHandler(changeHandler);

		// PUT ELEMENTS IN MENU
		menu.addWidget(UiElements.getRefreshButton(this));
		menu.addWidget(saveChangesButton);
		//menu.addWidget(resChb);
		menu.addWidget(memChb);
		menu.addWidget(setNewAttributeButton);
		menu.addWidget(removeButton);
		menu.addWidget(new HTML("<strong>Resources:</strong>"));
		menu.addWidget(resourceDropDown);
		menu.addWidget(new HTML("<strong>Members:</strong>"));
		menu.addWidget(membersDropDown);

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
		final int prime = 839;
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

		GroupSettingsTabItem create = (GroupSettingsTabItem) obj;

		if (groupId != create.groupId){
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {

		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(group, "Settings", getUrlWithParameters());
		if(group != null){
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);

	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(group.getVoId()) || session.isVoObserver(group.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "settings";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
	}

	static public GroupSettingsTabItem load(Map<String, String> parameters) {
		int id = Integer.parseInt(parameters.get("id"));
		return new GroupSettingsTabItem(id);
	}

	public void setResources(ArrayList<Resource> res) {
		this.resources = res;
	}

}
