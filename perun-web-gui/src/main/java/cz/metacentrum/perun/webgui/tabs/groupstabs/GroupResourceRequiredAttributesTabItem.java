package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Window;
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
import cz.metacentrum.perun.webgui.json.attributesManager.GetResourceRequiredAttributesV2;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.json.groupsManager.GetGroupRichMembers;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetFacility;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeValueCell;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides page with attributes management for specified group on specified resource and its members
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 *
 */
public class GroupResourceRequiredAttributesTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading settings");


	/**
	 * Resource ID to set
	 */
	private int resourceId = 0;

	/**
	 * Group ID to set
	 */
	private int groupId = 0;
	private Group group;
	private Resource resource;
	private int columnId = 3;

	/**
	 * Creates a tab instance
	 *
	 * @param resourceId ID of resource to get services from
	 * @param groupId ID of group to get attributes for
	 */
	public GroupResourceRequiredAttributesTabItem(int resourceId, int groupId){
		this.groupId= groupId;
		this.resourceId = resourceId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				group = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.GROUP, groupId, events).retrieveData();
		JsonCallbackEvents events2 = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				resource = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.RESOURCE, resourceId, events2).retrieveData();
	}


	public boolean isPrepared(){
		return !(group == null || resource == null);
	}


	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName()) + ": settings for " + Utils.getStrippedStringWithEllipsis(resource.getName()));

		if (JsonUtils.isExtendedInfoVisible()) {
			columnId = 3;
		} else {
			columnId = 2;
		}

		final VerticalPanel mainTab = new VerticalPanel();
		mainTab.setSize("100%","100%");

		TabMenu menu = new TabMenu();

		final Label facilityId = new Label();

		JsonCallbackEvents events = new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso) {
				Facility fac = (Facility)jso;
				facilityId.setText(String.valueOf(fac.getId()));
			}
		};
		GetFacility facility = new GetFacility(resourceId, events);
		facility.retrieveData();

		mainTab.add(new HTML("<hr size=\"2px\" />"));

		// set attributes type to group_resource
		final Map<String, Integer> ids = new HashMap<String, Integer>();
		ids.put("resourceToGetServicesFrom", resourceId);
		ids.put("group", groupId);

		// gets all required group attributes for specified group and resource
		final GetResourceRequiredAttributesV2 reqAttrs = new GetResourceRequiredAttributesV2(ids);
		final CellTable<Attribute> reqAttrsTable = reqAttrs.getTable();
		reqAttrsTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

		// get all required group_resource attributes too
		ids.put("resource", resourceId);
		reqAttrs.setIds(ids);
		reqAttrs.retrieveData();

		final ListBoxWithObjects<RichMember> listBox = new ListBoxWithObjects<RichMember>();
		listBox.setTitle(WidgetTranslation.INSTANCE.selectingMember());

		// local event fills the listBox
		JsonCallbackEvents localEvents = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				ArrayList<RichMember> mems = JsonUtils.jsoAsList(jso);
				mems = new TableSorter<RichMember>().sortByName(mems);
				listBox.addNotSelectedOption();
				for (int i = 0; i<mems.size(); i++) {
					listBox.addItem(mems.get(i));
				}
				listBox.addAllOption();
			}};
		final GetGroupRichMembers getGroupRichMembers = new GetGroupRichMembers(groupId, localEvents);
		getGroupRichMembers.retrieveData();

		reqAttrsTable.addStyleName("perun-table");
		final ScrollPanel sp = new ScrollPanel(reqAttrsTable);
		sp.addStyleName("perun-tableScrollPanel");

		// store for column with values
		final Column<Attribute, ?> columnStore = reqAttrsTable.getColumn(columnId);

		listBox.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event) {
				int selectedIndex = listBox.getSelectedIndex();
				// no member selected
				if (selectedIndex == 0) {
					// offer just group and group resource attributes
					reqAttrs.clearTable();
					ids.clear();
					ids.put("resourceToGetServicesFrom", resourceId);
					// get groups attributes
					ids.put("group", groupId);
					reqAttrs.setIds(ids);
					reqAttrs.retrieveData();
					// get group_resource attributes
					ids.put("resource", resourceId);
					reqAttrs.setIds(ids);
					reqAttrs.retrieveData();
					reqAttrs.sortTable();
					// some member is selected
				} else {
					reqAttrs.clearTable();
					ids.clear();
					ids.put("resourceToGetServicesFrom", resourceId);
					// get member, member-resource, user, user-facility  attributes
					ids.put("member", listBox.getSelectedObject().getId());
					ids.put("resource", resourceId);
					ids.put("workWithUserAttributes", 1); // to get user, user-facility attrs
					reqAttrs.setIds(ids);
					reqAttrs.retrieveData();
					// all members are selected
					if (selectedIndex == 1) {

						// remove value column
						reqAttrsTable.removeColumn(columnId);

						// create own value column
						// Value column
						Column<Attribute, Attribute> valueColumn = JsonUtils.addColumn(
								new PerunAttributeValueCell(), "Value",
								new JsonUtils.GetValue<Attribute, Attribute>() {
									public Attribute getValue(Attribute attribute) {
										attribute.setValueAsJso(null);
										return attribute;
									}
								}, new FieldUpdater<Attribute, Attribute>() {

									public void update(int index, Attribute object,
										Attribute value) {

										object = value;
										reqAttrsTable.getSelectionModel().setSelected(object, object.isAttributeValid());

									}
								});

						// add to table
						reqAttrsTable.insertColumn(columnId, valueColumn, "Value");
					} else {
						// member selected
						// return original column
						reqAttrsTable.removeColumn(columnId);
						reqAttrsTable.insertColumn(columnId, columnStore, "Value");
					}
				}
			}
		});

		CustomButton saveChangesButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveChangesInAttributes(), new ClickHandler() {
			public void onClick(ClickEvent event) {

				ArrayList<Attribute> list = reqAttrs.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {

					// send lists
					ArrayList<Attribute> groupList = new ArrayList<Attribute>();
					ArrayList<Attribute> groupResourceList = new ArrayList<Attribute>();
					ArrayList<Attribute> memberList = new ArrayList<Attribute>();
					ArrayList<Attribute> memberResourceList = new ArrayList<Attribute>();
					ArrayList<Attribute> userList = new ArrayList<Attribute>();
					ArrayList<Attribute> userFacilityList = new ArrayList<Attribute>();
					SetAttributes request = new SetAttributes();
					int selectedIndex = listBox.getSelectedIndex();

					// if all selected
					if (selectedIndex == 1) {
						// TODO - USE NEW CONFIRM DESIGN
						if (!Window.confirm("Same values for selected attributes will be set to all members in group." +
								"\n\nDo you want to continue ?")) {
							return;
								}
					}

					// get different attributes
					for (Attribute attr : list) {
						if (attr.getNamespace().contains("urn:perun:group:")) {
							groupList.add(attr);
						} else if (attr.getNamespace().contains("urn:perun:group_resource:")) {
							groupResourceList.add(attr);
						} else if (attr.getNamespace().contains("urn:perun:member:")) {
							memberList.add(attr);
						} else if (attr.getNamespace().contains("urn:perun:member_resource:")) {
							memberResourceList.add(attr);
						} else if (attr.getNamespace().contains("urn:perun:user:")) {
							userList.add(attr);
						} else if (attr.getNamespace().contains("urn:perun:user_facility:")) {
							userFacilityList.add(attr);
						}
					}
					// if not empty, send request
					if (!(groupList.isEmpty())) {
						ids.clear();
						ids.put("group", groupId);
						request.setAttributes(ids, groupList);
					}
					if (!(groupResourceList.isEmpty())) {
						ids.clear();
						ids.put("group", groupId);
						ids.put("resource", resourceId);
						request.setAttributes(ids, groupResourceList);
					}
					if (!(memberList.isEmpty())) {
						if (selectedIndex == 1) {
							// for all members
							for (int i = 0; i < listBox.getItemCount(); i++) {
								ids.clear();
								ids.put("member", (listBox.getObjectAt(i)).getId());
								request.setAttributes(ids, memberList);
							}
						} else {
							// for one member
							ids.clear();
							ids.put("member", listBox.getSelectedObject().getId());
							request.setAttributes(ids, memberList);
						}
					}
					if (!(memberResourceList.isEmpty())) {
						if (selectedIndex == 1) {
							// for all members
							for (int i = 0; i < listBox.getItemCount(); i++) {
								ids.clear();
								ids.put("resource", resourceId);
								ids.put("member", (listBox.getObjectAt(i)).getId());
								request.setAttributes(ids, memberResourceList);
							}
						} else {
							// for one member
							ids.clear();
							ids.put("resource", resourceId);
							ids.put("member", listBox.getSelectedObject().getId());
							request.setAttributes(ids, memberResourceList);
						}
					}
					if (!(userList.isEmpty())) {
						if (selectedIndex == 1) {
							// for all members
							for (int i = 0; i < listBox.getItemCount(); i++) {
								ids.clear();
								ids.put("user", (listBox.getObjectAt(i)).getUser().getId());
								request.setAttributes(ids, userList);
							}
						} else {
							// for one member
							ids.clear();
							ids.put("user", listBox.getSelectedObject().getUser().getId());
							request.setAttributes(ids, userList);
						}
					}
					if (!(userFacilityList.isEmpty())) {
						if (selectedIndex == 1) {
							// for all members
							for (int i = 0; i < listBox.getItemCount(); i++) {
								ids.clear();
								ids.put("user", listBox.getObjectAt(i).getUser().getId());
								ids.put("facility", Integer.parseInt(facilityId.getText()));
								request.setAttributes(ids, userFacilityList);
							}
						} else {
							// for one member
							ids.clear();
							ids.put("user", listBox.getSelectedObject().getUser().getId());
							ids.put("facility", Integer.parseInt(facilityId.getText()));
							request.setAttributes(ids, userFacilityList);
						}
					}
					reqAttrs.clearTableSelectedSet();
				}

			}
		});

		menu.addWidget(UiElements.getRefreshButton(this));
		menu.addWidget(saveChangesButton);
		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) saveChangesButton.setEnabled(false);
		menu.addWidget(new HTML("<strong>Group members:</strong>"));
		menu.addWidget(listBox);

		// table content
		session.getUiElements().resizePerunTable(sp, 350, this);

		mainTab.add(menu);
		mainTab.add(sp);
		mainTab.setCellHeight(sp, "100%");
		mainTab.setCellHeight(menu, "30px");

		this.contentWidget.setWidget(mainTab);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.attributesDisplayIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 829;
		int result = 1;
		result = prime * result * groupId + resourceId;
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

		GroupResourceRequiredAttributesTabItem o = (GroupResourceRequiredAttributesTabItem) obj;
		if (groupId != o.groupId){
			return false;
		}
		if (resourceId != o.resourceId){
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		session.setActiveGroupId(groupId);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(resource.getVoId())) {
			return true;
		} else {
			return false;
		}
	}

	public final static String URL = "res-req-attrs";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId + "&res=" + resourceId;
	}

	static public GroupResourceRequiredAttributesTabItem load(Map<String, String> parameters) {
		int gid = Integer.parseInt(parameters.get("id"));
		int rid = Integer.parseInt(parameters.get("res"));
		return new GroupResourceRequiredAttributesTabItem(rid, gid);
	}

}
