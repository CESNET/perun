package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.*;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedResources;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.attributestabs.SetNewAttributeTabItem;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays complex member information
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class MemberSettingsTabItem implements TabItem {

	/**
	 * member id
	 */
	private RichMember member;
	private int memberId;

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
	private Label titleWidget = new Label("Loading member's settings");

	// 0 = NO RESOURCE SELECTED
	private int lastSelectedResourceId = 0;
	private int lastSelectedFilterIndex = 1;  // 1 = required as default
	private int groupId = 0;

	/**
	 * Constructor
	 *
	 * @param member RichMember object, typically from table
	 */
	public MemberSettingsTabItem(RichMember member, int groupId){
		this.member = member;
		this.memberId = member.getId();
		this.groupId = groupId;
	}

	public boolean isPrepared(){
		return !(member == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(member.getUser().getFullNameWithTitles().trim())+ ": Settings");

		// CONTENT WIDGET
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

		// MENU
		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));

		// callbacks
		final GetAttributesV2 callback = new GetAttributesV2(true);
		if (!session.isVoAdmin(member.getVoId()) && !session.isGroupAdmin(groupId)) {
			callback.setCheckable(false);
		}
		callback.getMemberAttributes(member.getId(), 1); // member & user attrs
		final CellTable<Attribute> table = callback.getEmptyTable();

		// others callbacks
		final Map<String, Integer> ids = new HashMap<String, Integer>();
		ids.put("member", member.getId());
		ids.put("workWithUserAttributes", 1); // work with user
		if (groupId != 0) ids.put("group", groupId);
		final GetResourceRequiredAttributesV2 resourceRequired = new GetResourceRequiredAttributesV2(ids, JsonCallbackEvents.passDataToAnotherCallback(callback));
		final GetRequiredAttributes required = new GetRequiredAttributes(ids, JsonCallbackEvents.passDataToAnotherCallback(callback));

		if (lastSelectedFilterIndex == 1 || lastSelectedResourceId == 0) {
			required.retrieveData(); // load required by default
		}

		// listbox with resources
		final ListBoxWithObjects<Resource> listbox = new ListBoxWithObjects<Resource>();
		// listbox for filtering attributes callbacks
		final ListBox filter = new ListBox();
		filter.addItem("All filled attributes");
		filter.addItem("Required");
		filter.addItem("Resource required");
		filter.setSelectedIndex(lastSelectedFilterIndex); // required as default

		// change table on selection one of the listboxs
		final ChangeHandler changehandler = new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				// if resource selected
				if (listbox.getSelectedIndex() > 0) {
					// if all attributes
					if (filter.getSelectedIndex() == 0) {
						callback.clearTable();
						callback.getMemberAttributes(member.getId(), 1); // with user
						callback.retrieveData();
						callback.getMemberResourceAttributes(member.getId(), listbox.getSelectedObject().getId());
						callback.retrieveData();
						callback.getUserFacilityAttributes(listbox.getSelectedObject().getFacilityId(), member.getUser().getId());
						callback.retrieveData();
						if (groupId != 0) {
							callback.getMemberGroupAttributes(member.getId(), groupId);
							callback.retrieveData();
						}
						// if required attributes
					} else if (filter.getSelectedIndex() == 1) {
						callback.clearTable();
						ids.clear();
						UiElements.generateInfo("Not valid option", "Skipping to \"Resource required\" option.");
						filter.setSelectedIndex(2);
						lastSelectedFilterIndex = 2;
						ids.put("member", member.getId());
						ids.put("resource", listbox.getSelectedObject().getId());
						if (groupId != 0) ids.put("group", groupId);
						ids.put("resourceToGetServicesFrom", listbox.getSelectedObject().getId());
						ids.put("workWithUserAttributes",1);
						resourceRequired.retrieveData();
						// if resource required
					} else if (filter.getSelectedIndex() == 2) {
						callback.clearTable();
						ids.clear();
						ids.put("member", member.getId());
						ids.put("resource", listbox.getSelectedObject().getId());
						if (groupId != 0) ids.put("group", groupId);
						ids.put("resourceToGetServicesFrom", listbox.getSelectedObject().getId());
						ids.put("workWithUserAttributes",1);
						resourceRequired.retrieveData();
					}
					// if no resource selected
				} else {
					// if all attributes
					if (filter.getSelectedIndex() == 0) {
						callback.clearTable();
						callback.getMemberAttributes(member.getId(), 1);
						callback.retrieveData();
						if (groupId != 0) {
							callback.getMemberGroupAttributes(member.getId(), groupId);
							callback.retrieveData();
						}
						// if required attributes
					} else if (filter.getSelectedIndex() == 1) {
						callback.clearTable();
						ids.clear();
						ids.put("member", member.getId());
						ids.put("workWithUserAttributes", 1);
						if (groupId != 0) ids.put("group", groupId);
						required.retrieveData();
						// if resource required
					} else if (filter.getSelectedIndex() == 2) {
						callback.clearTable();
						((AjaxLoaderImage)table.getEmptyTableWidget()).loadingFinished();
						UiElements.generateInfo("Not valid option", "You must select resource first.");
					}
				}
			}
		};

		ChangeHandler resourceChangeHandler = new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				if (listbox.getSelectedIndex() > 0) {
					// if resource selected
					// load resource-required (by default)
					filter.setSelectedIndex(2);
					lastSelectedFilterIndex = 2;
				} else {
					// else load required by default
					filter.setSelectedIndex(1);
					lastSelectedFilterIndex = 1;
				}
				if (listbox.getSelectedObject() != null) {
					lastSelectedResourceId = listbox.getSelectedObject().getId();
				} else {
					lastSelectedResourceId = 0;
				}
				changehandler.onChange(event);
			}
		};

		ChangeHandler filterChangeHandler = new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				// if required and resource selected
				if (filter.getSelectedIndex() == 1 && listbox.getSelectedIndex() > 0) {
					// cance resource selection
					listbox.setSelectedIndex(0);
					lastSelectedResourceId = 0;
				}
				lastSelectedFilterIndex = filter.getSelectedIndex();
				changehandler.onChange(event);
			}
		};

		// refresh table on listbox change
		listbox.addChangeHandler(resourceChangeHandler);
		filter.addChangeHandler(filterChangeHandler);

		// fill listbox with member's resources
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				listbox.clear();

				ArrayList<Resource> res = JsonUtils.jsoAsList(jso);
				res = new TableSorter<Resource>().sortByName(res);

				if (res != null && !res.isEmpty()) {
					listbox.addNotSelectedOption();
				} else {
					listbox.addItem("No resource available");
				}

				for (int i=0; i<res.size(); i++) {
					listbox.addItem(res.get(i));
					// select last selected
					if (res.get(i).getId() == lastSelectedResourceId) {
						listbox.setSelected(res.get(i), true);
					}
				}
				if (lastSelectedFilterIndex != 1 && lastSelectedResourceId != 0) {
					DomEvent.fireNativeEvent(Document.get().createChangeEvent(), filter);
				}
			}
			public void onLoadingStart() {
				listbox.clear();
				listbox.addItem("Loading...");
			}
			public void onError(PerunError error) {
				listbox.clear();
				listbox.addItem("Error while loading");
			}
		};

		// get member resources
		GetAssignedResources res = new GetAssignedResources(member.getId(), PerunEntity.MEMBER, events);
		res.retrieveData();

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		session.getUiElements().resizePerunTable(sp, 350, this);

		// save changes in attributes
		final CustomButton saveChangesButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveChangesInAttributes());
		if (!session.isVoAdmin(member.getVoId()) && !session.isGroupAdmin(groupId)) saveChangesButton.setEnabled(false);
		saveChangesButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				if (UiElements.cantSaveEmptyListDialogBox(callback.getTableSelectedList())) {

					// if resource selected
					if (listbox.getSelectedIndex() > 0) {
						// if all attributes
						SetAttributes request;
						if (filter.getSelectedIndex() == 0) {
							request = new SetAttributes(JsonCallbackEvents.disableButtonEvents(saveChangesButton, new JsonCallbackEvents() {
								public void onFinished(JavaScriptObject jso) {
									callback.clearTable();
									callback.getMemberAttributes(member.getId(), 1); // with user
									callback.retrieveData();
									callback.getMemberResourceAttributes(member.getId(), listbox.getSelectedObject().getId());
									callback.retrieveData();
									callback.getUserFacilityAttributes(listbox.getSelectedObject().getFacilityId(), member.getUser().getId());
									callback.retrieveData();
									if (groupId != 0) {
										callback.getMemberGroupAttributes(member.getId(), groupId);
										callback.retrieveData();
									}
								}
							}));
						} else {
							// if required or resource-required
							request = new SetAttributes(JsonCallbackEvents.disableButtonEvents(saveChangesButton, new JsonCallbackEvents() {
								public void onFinished(JavaScriptObject jso) {
									callback.clearTable();
									ids.clear();
									ids.put("member", member.getId());
									ids.put("resource", listbox.getSelectedObject().getId());
									if (groupId != 0) ids.put("group", groupId);
									ids.put("resourceToGetServicesFrom", listbox.getSelectedObject().getId());
									ids.put("workWithUserAttributes", 1);
									resourceRequired.retrieveData();
								}
							}));
						}
						// make setAttributesCall
						ids.clear();
						ids.put("member", member.getId());
						ids.put("resource", listbox.getSelectedObject().getId());
						ids.put("facility", listbox.getSelectedObject().getFacilityId());
						ids.put("user", member.getUserId());
						if (groupId != 0) ids.put("group", groupId);
						request.setAttributes(ids, callback.getTableSelectedList());
					} else {
						// if resource not selected
						SetAttributes request;
						if (filter.getSelectedIndex() == 1) {
							// if required attr option - refresh different callback
							request = new SetAttributes(JsonCallbackEvents.disableButtonEvents(saveChangesButton, new JsonCallbackEvents() {
								public void onFinished(JavaScriptObject jso) {
									callback.clearTable();
									required.retrieveData();
								}
							}));
						} else {
							request = new SetAttributes(JsonCallbackEvents.disableButtonEvents(saveChangesButton, new JsonCallbackEvents() {
								public void onFinished(JavaScriptObject jso) {
									callback.clearTable();
									callback.getMemberAttributes(memberId, 1);
									callback.retrieveData();
									if (groupId != 0) {
										callback.getMemberGroupAttributes(memberId, groupId);
										callback.retrieveData();
									}
								}
							}));
						}
						// make setAttributes call
						ids.clear();
						ids.put("member", member.getId());
						if (groupId != 0) ids.put("group", groupId);
						ids.put("workWithUserAttributes", 1);
						request.setAttributes(ids, callback.getTableSelectedList());
					}

				}

			}
		});
		menu.addWidget(saveChangesButton);

		// buttons
		CustomButton setNewMemberAttributeButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.setNewAttributes(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				Map<String, Integer> ids = new HashMap<String,Integer>();
				ids.put("member", member.getId());
				ids.put("user", member.getUser().getId());
				if (groupId != 0) ids.put("group", groupId);
				if (listbox.getSelectedIndex() > 0) {
					ids.put("resource", listbox.getSelectedObject().getId());
					ids.put("facility", listbox.getSelectedObject().getFacilityId());
				}
				session.getTabManager().addTabToCurrentTab(new SetNewAttributeTabItem(ids), true);
			}
		});
		if (!session.isVoAdmin(member.getVoId()) && !session.isGroupAdmin(groupId)) setNewMemberAttributeButton.setEnabled(false);
		menu.addWidget(setNewMemberAttributeButton);

		// REMOVE ATTRIBUTES BUTTON
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeAttributes());
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				// if selected
				if (UiElements.cantSaveEmptyListDialogBox(callback.getTableSelectedList())) {

					// if resource selected
					if (listbox.getSelectedIndex() > 0) {
						// if all attributes
						RemoveAttributes request;
						if (filter.getSelectedIndex() == 0) {
							request = new RemoveAttributes(JsonCallbackEvents.disableButtonEvents(removeButton, new JsonCallbackEvents(){
								public void onFinished(JavaScriptObject jso) {
									callback.clearTable();
									callback.getMemberAttributes(member.getId(), 1); // with user
									callback.retrieveData();
									callback.getMemberResourceAttributes(member.getId(), listbox.getSelectedObject().getId());
									callback.retrieveData();
									callback.getUserFacilityAttributes(listbox.getSelectedObject().getFacilityId(), member.getUser().getId());
									callback.retrieveData();
									if (groupId != 0) {
										callback.getMemberGroupAttributes(member.getId(), groupId);
										callback.retrieveData();
									}
								}
							}));
						} else {
							// if required or resource-required
							request = new RemoveAttributes(JsonCallbackEvents.disableButtonEvents(removeButton, new JsonCallbackEvents(){
								public void onFinished(JavaScriptObject jso) {
									callback.clearTable();
									ids.clear();
									ids.put("member", member.getId());
									ids.put("resource", listbox.getSelectedObject().getId());
									if (groupId != 0) ids.put("group", groupId);
									ids.put("resourceToGetServicesFrom", listbox.getSelectedObject().getId());
									ids.put("workWithUserAttributes",1);
									resourceRequired.retrieveData();
								}
							}));
						}
						// make removeAttributesCall
						ids.clear();
						ids.put("member", member.getId());
						ids.put("resource", listbox.getSelectedObject().getId());
						ids.put("facility",listbox.getSelectedObject().getFacilityId());
						ids.put("user", member.getUserId());
						if (groupId != 0) ids.put("group", groupId);
						request.removeAttributes(ids, callback.getTableSelectedList());
					} else {
						// if resource not selected
						RemoveAttributes request;
						if (filter.getSelectedIndex() == 1) {
							// if required attr option - refresh different callback
							request = new RemoveAttributes(JsonCallbackEvents.disableButtonEvents(removeButton, new JsonCallbackEvents(){
								public void onFinished(JavaScriptObject jso) {
									callback.clearTable();
									required.retrieveData();
								}
							}));
						} else {
							request = new RemoveAttributes(JsonCallbackEvents.disableButtonEvents(removeButton, new JsonCallbackEvents(){
								public void onFinished(JavaScriptObject jso) {
									callback.clearTable();
									callback.getMemberAttributes(memberId, 1);
									callback.retrieveData();
									if (groupId != 0) {
										callback.getMemberGroupAttributes(memberId, groupId);
										callback.retrieveData();
									}
								}
							}));
						}
						// make removeAttributes call
						ids.clear();
						ids.put("member", member.getId());
						if (groupId != 0) ids.put("group", groupId);
						ids.put("workWithUserAttributes", 1);
						request.removeAttributes(ids, callback.getTableSelectedList());
					}

				}

			}
		});

		removeButton.setEnabled(false);
		if (session.isVoAdmin(member.getVoId()) || session.isGroupAdmin(groupId)) JsonUtils.addTableManagedButton(callback, table, removeButton);
		menu.addWidget(removeButton);

		// add listbox to menu
		menu.addWidget(new HTML("<strong>Assigned resources: </strong>"));
		menu.addWidget(listbox);
		menu.addWidget(new HTML("<strong>Filter: </strong>"));
		menu.addWidget(filter);

		// ATTRIBUTES TABLE
		vp.add(sp);
		vp.setCellHeight(sp, "100%");

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
		return SmallIcons.INSTANCE.attributesDisplayIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 883;
		int result = 1;
		result = prime * result + memberId + 1;
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
		MemberSettingsTabItem other = (MemberSettingsTabItem) obj;
		if (memberId != other.memberId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() { }

	public boolean isAuthorized() {

		if (session.isVoAdmin(member.getVoId()) || session.isVoObserver(member.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

}
