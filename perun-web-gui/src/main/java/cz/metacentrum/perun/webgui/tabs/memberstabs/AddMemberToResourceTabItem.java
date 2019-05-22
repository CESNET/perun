package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.AddMember;
import cz.metacentrum.perun.webgui.json.groupsManager.CreateGroup;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllGroups;
import cz.metacentrum.perun.webgui.json.membersManager.FindCompleteRichMembers;
import cz.metacentrum.perun.webgui.json.resourcesManager.*;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Dialog for adding member to specific resource.
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddMemberToResourceTabItem implements TabItem  {

	private PerunWebSession session = PerunWebSession.getInstance();
	private VirtualOrganization vo;
	private int voId;
	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Loading VO");

	/* Define in which state is tab currently loaded */
	private enum State{
		SELECT_MEMBERS,
			SELECT_RESOURCE_OR_FACILITY,
			SELECT_GROUP,
			CREATE_GROUP
	}
	// default state is first
	private State state = State.SELECT_MEMBERS;
	// list of selected members to handle
	private AddRemoveItemsTable<RichMember> selectedMembers = new AddRemoveItemsTable<RichMember>(true);

	private ListBoxWithObjects<Facility> facilitiesListbox = new ListBoxWithObjects<Facility>();
	private ListBoxWithObjects<Service> servicesListbox = new ListBoxWithObjects<Service>();
	private ArrayList<RichResource> allResources = new ArrayList<RichResource>();
	private ListBoxWithObjects<RichResource> resourcesListbox = new ListBoxWithObjects<RichResource>();
	private Label description = new Label();
	private HTML services = new HTML();
	private boolean startedAtTwo = false;

	// TODO - save indicator if selected resource or facility and finally of selected resource

	/**
	 * Create tab instance
	 * @param vo - VO to filter available resources for
	 */
	public AddMemberToResourceTabItem(VirtualOrganization vo) {
		this.vo = vo;
		this.voId = vo.getId();
	}

	/**
	 * Create tab instance
	 * @param voId - VO to filter available resources for
	 */
	public AddMemberToResourceTabItem(int voId) {
		this.voId = voId;
		new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				vo = jso.cast();
			}
		}).retrieveData();
	}

	@Override
	public boolean isPrepared() {
		return (vo != null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	/**
	 * Start tab at stage two with 1 member selected for adding
	 *
	 * @param rm  RichMember to add to resource
	 */
	public void startAtStageTwo(RichMember rm) {
		this.startedAtTwo = true;
		this.state = State.SELECT_RESOURCE_OR_FACILITY;
		this.selectedMembers.addItem(rm);
	}

	@Override
	public Widget draw() {

		this.titleWidget.setText("Add member(s) to resource");

		final TabItem tab = this;

		if (state == State.SELECT_MEMBERS) {

			this.contentWidget.clear();

			session.getTabManager().changeStyleOfInnerTab(true);

			// main tab
			FlexTable hp = new FlexTable();
			hp.setSize("100%","100%");
			hp.setCellPadding(5);

			// members list
			VerticalPanel membersListPanel = new VerticalPanel();
			membersListPanel.setSize("100%", "100%");
			TabMenu membersListMenu = new TabMenu();
			membersListPanel.add(membersListMenu);
			membersListPanel.setCellHeight(membersListMenu, "30px");

			// members list callback
			final FindCompleteRichMembers findMembers = new FindCompleteRichMembers(PerunEntity.VIRTUAL_ORGANIZATION, voId, "", null);

			membersListMenu.addSearchWidget(new PerunSearchEvent() {
				@Override
				public void searchFor(String text) {
					if (UiElements.searchStingCantBeEmpty(text)) {
						findMembers.searchFor(text);
					}
				}
			}, ButtonTranslation.INSTANCE.searchMemberInVo());

			final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, "Add selected members to list", new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					ArrayList<RichMember> list = findMembers.getTableSelectedList();
					if (UiElements.cantSaveEmptyListDialogBox(list)) {
						selectedMembers.addItems(list);
						findMembers.clearTableSelectedSet();
					}
				}
			});
			membersListMenu.addWidget(addButton);

			membersListMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					session.getTabManager().closeTab(tab);
				}
			}));

			final CustomButton continueButton = TabMenu.getPredefinedButton(ButtonType.CONTINUE, "Continue to resource selection", new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					// change state
					state = State.SELECT_RESOURCE_OR_FACILITY;
					// redraw the page
					tab.draw();
				}
			});
			membersListMenu.addWidget(continueButton);
			// if no member selected, disable
			if (selectedMembers.getList().size() == 0) {
				continueButton.setEnabled(false);
			}

			// selection of some member will enable continue button
			selectedMembers.setEvents(new AddRemoveItemsTable.HandleItemsAction<RichMember>() {
				@Override
				public void onAdd(RichMember object) {
					continueButton.setEnabled(true);
				}
				@Override
				public void onRemove(RichMember object) {
					if (selectedMembers.getList().size() == 0) {
						continueButton.setEnabled(false);
					}
				}
			});

			// put table in content
			ScrollPanel tableWrapper = new ScrollPanel();
			tableWrapper.setWidth("100%");

			CellTable<RichMember> table = findMembers.getEmptyTable(new FieldUpdater<RichMember, RichMember>() {
				// when user click on a row -> open new tab
				public void update(int index, RichMember object, RichMember value) {
					session.getTabManager().addTab(new MemberDetailTabItem(object.getId(), 0));
				}
			});

			addButton.setEnabled(false);
			JsonUtils.addTableManagedButton(findMembers, table, addButton);

			table.addStyleName("perun-table");
			table.setWidth("100%");
			tableWrapper.setWidget(table);
			tableWrapper.addStyleName("perun-tableScrollPanel");

			session.getUiElements().resizeSmallTabPanel(tableWrapper, 350, this);

			membersListPanel.add(tableWrapper);

			// selected members
			VerticalPanel selectedMembersPanel = new VerticalPanel();
			selectedMembersPanel.setSize("100%", "100%");
			TabMenu selectedMembersMenu = new TabMenu();
			selectedMembersPanel.add(selectedMembersMenu);
			selectedMembersPanel.setCellHeight(selectedMembersMenu, "30px");

			selectedMembersMenu.addWidget(new HTML("<h3>Selected&nbsp;members:</h3>"));

			selectedMembersPanel.add(selectedMembers);

			hp.setWidget(0, 0, membersListPanel);
			hp.setWidget(0, 1, selectedMembersPanel);
			hp.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);
			hp.getFlexCellFormatter().setWidth(0, 0, "80%");
			hp.getFlexCellFormatter().setWidth(0, 1,"20%");

			this.contentWidget.add(hp);

		} else if (state == State.SELECT_RESOURCE_OR_FACILITY) {

			this.contentWidget.clear();

			session.getTabManager().changeStyleOfInnerTab(false);

			FlexTable layout = new FlexTable();
			layout.setSize("100%", "100%");

			TabMenu menu = new TabMenu();

			if (!startedAtTwo) {
				menu.addWidget(TabMenu.getPredefinedButton(ButtonType.BACK, "Back to members selection", new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// get back and reload
						state = State.SELECT_MEMBERS;
						tab.draw();
					}
				}));
			} else {
				this.titleWidget.setText("Add member to resource");
			}

			menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CONTINUE, "", new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					state = State.SELECT_GROUP;
					tab.draw();
				}
			}));

			// RESOURCE LAYOUT
			FlexTable resourceLayout = new FlexTable();
			resourceLayout.setStyleName("inputFormFlexTable");

			// by default do not choose facility or service
			facilitiesListbox.addNotSelectedOption();
			servicesListbox.addNotSelectedOption();

			// TODO
			servicesListbox.setEnabled(false);

			// fill services event
			final JsonCallbackEvents fillServEvent = new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso){
					services.setHTML("");
					ArrayList<Service> servList = JsonUtils.jsoAsList(jso);
					servList = new TableSorter<Service>().sortByName(servList);
					for (Service s : servList){
						services.setHTML(services.getHTML().concat(SafeHtmlUtils.fromString(s.getName()).asString() + "</br>"));
					}
				}
				@Override
				public void onError(PerunError errro) {
					services.setHTML("Error while loading available services.");
				}
			};

			// fill resources event
			final JsonCallbackEvents fillResEvent = new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso){
					// list of VO facilities filled by loaded resources
					ArrayList<Facility> facs = new ArrayList<Facility>();
					ArrayList<RichResource> resList = JsonUtils.jsoAsList(jso);
					resList = new TableSorter<RichResource>().sortByName(resList);
					Set<String> unificator = new HashSet<String>();
					for (RichResource r : resList){
						allResources.add(r);
						resourcesListbox.addItem(r);
						if (r.getFacility() != null && !unificator.contains(r.getFacility().getName())) {
							unificator.add(r.getFacility().getName());
							facs.add(r.getFacility());
						}
					}
					// sort and fill facilities
					facs = new TableSorter<Facility>().sortByName(facs);
					facilitiesListbox.addAllItems(facs);
					// initial setup
					// refresh rest
					description.setText(resourcesListbox.getSelectedObject().getDescription());
					GetAssignedServices servCall = new GetAssignedServices(resourcesListbox.getSelectedObject().getId(), fillServEvent);
					servCall.retrieveData();
				}
				@Override
				public void onLoadingStart(){
					resourcesListbox.clear();
				}
				@Override
				public void onError(PerunError error){
					resourcesListbox.addItem("Error while loading");
				}
			};

			resourcesListbox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent changeEvent) {
					description.setText(resourcesListbox.getSelectedObject().getDescription());
					// fill information about services on resource
					GetAssignedServices servCall = new GetAssignedServices(resourcesListbox.getSelectedObject().getId(), fillServEvent);
					servCall.retrieveData();
				}
			});

			facilitiesListbox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent changeEvent) {
					if (facilitiesListbox.getSelectedIndex() > 0) {
						// fill only resources on facility
						resourcesListbox.clear();
						for (RichResource r : allResources) {
							if (r.getFacility().getId() == facilitiesListbox.getSelectedObject().getId()){
								resourcesListbox.addItem(r);
							}
						}
					} else {
						// fill all resources
						resourcesListbox.clear();
						resourcesListbox.addAllItems(allResources);
					}
					// refresh rest
					description.setText(resourcesListbox.getSelectedObject().getDescription());
					GetAssignedServices servCall = new GetAssignedServices(resourcesListbox.getSelectedObject().getId(), fillServEvent);
					servCall.retrieveData();

				}
			});


			GetRichResources resCall = new GetRichResources(voId, fillResEvent);
			if (allResources.isEmpty()) {
				resCall.retrieveData();
			}

			resourceLayout.setHTML(0, 0, "Filter by Facility:");
			resourceLayout.setWidget(0, 1, facilitiesListbox);

			//resourceLayout.setHTML(1, 0, "Filter by Service:");
			//resourceLayout.setWidget(1, 1, servicesListbox);

			resourceLayout.setHTML(1, 0, "Selected resource:");
			resourceLayout.setWidget(1, 1, resourcesListbox);

			resourceLayout.setHTML(2, 0, "Description:");
			resourceLayout.setWidget(2, 1, description);

			resourceLayout.setHTML(3, 0, "Services on Resource:");
			resourceLayout.setWidget(3, 1, services);

			for (int i=0; i<resourceLayout.getRowCount(); i++) {
				resourceLayout.getFlexCellFormatter().addStyleName(i, 0, "itemName");
			}

			// set layout
			layout.setWidget(0, 0, menu);
			layout.setWidget(1, 0, resourceLayout);

			this.contentWidget.add(layout);

		} else if (state == State.SELECT_GROUP) {

			this.contentWidget.clear();

			session.getTabManager().changeStyleOfInnerTab(true);

			final GetAssignedGroups assGroupCall = new GetAssignedGroups(resourcesListbox.getSelectedObject().getId());
			assGroupCall.setSingleSelection(true);
			assGroupCall.setCoreGroupsCheckable(false);

			VerticalPanel vp = new VerticalPanel();
			vp.setSize("100%","100%");

			TabMenu menu = new TabMenu();

			menu.addWidget(TabMenu.getPredefinedButton(ButtonType.BACK, "Back to resource selection", new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					// get back and reload
					state = State.SELECT_RESOURCE_OR_FACILITY;
					tab.draw();
				}
			}));

			final CustomButton addToGroupButton = new CustomButton("Add to Group", "Add members to selected Group", SmallIcons.INSTANCE.addIcon());
			menu.addWidget(addToGroupButton);
			addToGroupButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {

					if (UiElements.cantSaveEmptyListDialogBox(assGroupCall.getTableSelectedList())) {

						final Group grp = assGroupCall.getTableSelectedList().get(0);
						final FlexTable content = new FlexTable();

						GetAssignedResources getRes = new GetAssignedResources(grp.getId(), PerunEntity.GROUP, new JsonCallbackEvents(){
							@Override
							public void onFinished(JavaScriptObject jso){
								ArrayList<Resource> rr = JsonUtils.jsoAsList(jso);
								String items = new String("<ul>");
								for (Resource r : rr) {
									items += "<li>"+r.getName()+"</li>";
								}
								items += "</ul>";
								ScrollPanel sp = new ScrollPanel();
								sp.add(new HTML(items));
								sp.setSize("300px", "150px");
								content.setWidget(1, 0, sp);
							}
						public void onError(PerunError error) {
							content.setWidget(1, 0, new AjaxLoaderImage().loadingError(error));
						}
						});
						getRes.retrieveData();

						content.setHTML(0, 0, "<p><strong>By adding member into this group, he/she will gain access to following resources:</strong>");
						content.setWidget(1, 0, new AjaxLoaderImage().loadingStart());

						Confirm c = new Confirm("Confirm add action", content, new ClickHandler() {
							@Override
							public void onClick(ClickEvent clickEvent) {
								AddMember request = new AddMember(JsonCallbackEvents.disableButtonEvents(addToGroupButton));
								for (RichMember m : selectedMembers.getList()) {
									request.addMemberToGroup(grp, m);
								}
							}
						}, true);
						c.setNonScrollable(true);
						c.show();
					}
				}
			});

			menu.addWidget(new CustomButton("Create group…", "Create new empty group", SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					// get back and reload
					state = State.CREATE_GROUP;
					tab.draw();
				}
			}));

			CustomButton finish = TabMenu.getPredefinedButton(ButtonType.FINISH, "Close the tab", new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					session.getTabManager().closeTab(tab, isRefreshParentOnClose());
				}
			});
			finish.setImageAlign(true);
			menu.addWidget(finish);

			menu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
			menu.addWidget(new HTML("<strong>Please select group in which you wish to add members to."));

			CellTable<Group> table = assGroupCall.getTable();

			addToGroupButton.setEnabled(false);
			JsonUtils.addTableManagedButton(assGroupCall, table, addToGroupButton);

			// add a class to the table and wrap it into scroll panel
			table.addStyleName("perun-table");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			session.getUiElements().resizeSmallTabPanel(sp, 350, this);

			vp.add(menu);
			vp.add(sp);

			this.contentWidget.add(vp);

		} else if (state == State.CREATE_GROUP) {

			session.getTabManager().changeStyleOfInnerTab(false);

			VerticalPanel vp = new VerticalPanel();

			// form inputs
			final ExtendedTextBox groupNameTextBox = new ExtendedTextBox();
			final TextBox groupDescriptionTextBox  = new TextBox();
			final ListBoxWithObjects<Group> vosGroups = new ListBoxWithObjects<Group>();
			vosGroups.setVisible(false);
			final CheckBox asSubGroup = new CheckBox("", false);
			cz.metacentrum.perun.webgui.widgets.TabMenu menu = new cz.metacentrum.perun.webgui.widgets.TabMenu();
			final CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, "");
			final CustomButton cancelButton = TabMenu.getPredefinedButton(ButtonType.CANCEL, "");
			final HTML parentGroupText = new HTML("Parent group:");
			parentGroupText.setVisible(false);

			final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
				@Override
				public boolean validateTextBox() {
					if (groupNameTextBox.getTextBox().getText().trim().isEmpty()) {
						groupNameTextBox.setError("Name can't be empty.");
					} else if (!groupNameTextBox.getTextBox().getText().trim().matches(Utils.GROUP_SHORT_NAME_MATCHER)) {
						groupNameTextBox.setError("Name can contain only letters, numbers, spaces, dots, '_' and '-'.");
					} else {
						groupNameTextBox.setOk();
						return true;
					}
					return false;
				}
			};
			groupNameTextBox.setValidator(validator);

			final GetAllGroups groupsCall = new GetAllGroups(voId, new JsonCallbackEvents(){
				public void onFinished(JavaScriptObject jso){
					vosGroups.clear();
					ArrayList<Group> retGroups = JsonUtils.jsoAsList(jso);
					retGroups = new TableSorter<Group>().sortByName(retGroups);
					for (Group g : retGroups) {
						if (g.isCoreGroup()) {
							// SKIP CORE GROUPS !!
							continue;
						}
						vosGroups.addItem(g);
					}
					createButton.setEnabled(true);
				}
				public void onLoadingStart(){
					vosGroups.clear();
					vosGroups.addItem("Loading...");
					createButton.setEnabled(false);
				}
				public void onError(PerunError error) {
					vosGroups.clear();
					vosGroups.addItem("Error while loading");
					if (asSubGroup.getValue()) {
						createButton.setEnabled(false);
					} else {
						createButton.setEnabled(true);
					}
				}
			});

			asSubGroup.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
					if (booleanValueChangeEvent.getValue() == true) {
						// set title
						vosGroups.setVisible(true);
						parentGroupText.setVisible(true);
						groupsCall.retrieveData();
						createButton.setTitle(ButtonTranslation.INSTANCE.createSubGroup());
					} else {
						createButton.setEnabled(true);
						vosGroups.setVisible(false);
						parentGroupText.setVisible(false);
						createButton.setTitle(ButtonTranslation.INSTANCE.createGroup());
					}
				}
			});

			// layout
			FlexTable layout = new FlexTable();
			layout.setStyleName("inputFormFlexTable");
			FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

			// send button
			createButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {

					if (!validator.validateTextBox()) return;

					// redirect event
					final JsonCallbackEvents redirectEvent = new JsonCallbackEvents(){
						@Override
						public void onFinished(JavaScriptObject jso){
							state = State.SELECT_GROUP;
							session.getTabManager().changeStyleOfInnerTab(true);
							tab.draw();
						}
					};

					// assign group after creation
					final JsonCallbackEvents assignEvent = new JsonCallbackEvents(){
						@Override
						public void onFinished(JavaScriptObject jso){
							Group g = jso.cast();
							if (g != null) {
								AssignGroupToResources assign = new AssignGroupToResources(JsonCallbackEvents.disableButtonEvents(createButton, redirectEvent));
								assign.assignGroupToResources(g, JsonUtils.<RichResource>toList(resourcesListbox.getSelectedObject()));
							}
						}
					};

					// creates a new request
					CreateGroup cg = new CreateGroup(JsonCallbackEvents.disableButtonEvents(createButton, assignEvent));
					if (asSubGroup.getValue()) {
						if (vosGroups.getSelectedObject() != null) {
							cg.createGroupInGroup(vosGroups.getSelectedObject().getId(), groupNameTextBox.getTextBox().getText().trim(), groupDescriptionTextBox.getText());
						} else {
							UiElements.generateInfo("No parent group selected", "You checked create this group as sub-group, but no parent group is selected. Please select parent group.");
						}
					} else {
						cg.createGroupInVo(voId, groupNameTextBox.getTextBox().getText().trim(), groupDescriptionTextBox.getText());
					}
				}
			});
			// cancel button
			cancelButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {

					// redirect back to select group
					state = State.SELECT_GROUP;
					session.getTabManager().changeStyleOfInnerTab(true);
					tab.draw();

				}
			});

			// Add some standard form options
			layout.setHTML(0, 0, "Name:");
			layout.setWidget(0, 1, groupNameTextBox);
			layout.setHTML(1, 0, "Description:");
			layout.setWidget(1, 1, groupDescriptionTextBox);
			layout.setHTML(2, 0, "As sub-group:");
			layout.setWidget(2, 1, asSubGroup);
			layout.setWidget(3, 0, parentGroupText);
			layout.setWidget(3, 1, vosGroups);

			for (int i=0; i<layout.getRowCount(); i++) {
				cellFormatter.addStyleName(i, 0, "itemName");
			}

			// button align
			menu.addWidget(createButton);
			menu.addWidget(cancelButton);

			vp.add(layout);
			vp.add(menu);
			vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

			this.contentWidget.setWidget(vp);

		}

		return getWidget();

	}

	@Override
	public Widget getWidget() {
		return contentWidget;
	}

	@Override
	public Widget getTitle() {
		return titleWidget;
	}

	@Override
	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.addIcon();
	}

	@Override
	public boolean multipleInstancesEnabled() {
		return true;
	}

	@Override
	public void open() {

	}

	@Override
	public boolean isAuthorized() {
		return session.isVoAdmin(voId);
	}

	@Override
	public int hashCode() {
		final int prime = 1433;
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
		AddMemberToResourceTabItem other = (AddMemberToResourceTabItem) obj;
		if (voId != other.voId)
			return false;
		return true;
	}

}
