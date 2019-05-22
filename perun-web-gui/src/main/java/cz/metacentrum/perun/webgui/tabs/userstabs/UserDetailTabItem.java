package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesV2;
import cz.metacentrum.perun.webgui.json.attributesManager.GetListOfAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.RemoveAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.json.facilitiesManager.GetAssignedFacilities;
import cz.metacentrum.perun.webgui.json.groupsManager.GetMemberGroups;
import cz.metacentrum.perun.webgui.json.membersManager.GetMemberByUser;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedResources;
import cz.metacentrum.perun.webgui.json.usersManager.*;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.tabs.attributestabs.SetNewAttributeTabItem;
import cz.metacentrum.perun.webgui.tabs.cabinettabs.UsersPublicationsTabItem;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.FacilityDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.ChangeStatusTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.MemberDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.resourcestabs.ResourceDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * View user details
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class UserDetailTabItem implements TabItem, TabItemWithUrl {

	/**
	 * User id to display info for
	 */
	private User user;

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
	private Label titleWidget = new Label("Loading user");

	private int lastTabId = 0;

	private TabLayoutPanel tabPanel;

	private int userId;

	private ArrayList<Attribute> userLoginAttrs = new ArrayList<Attribute>();

	// downloadable file tag counter, otherwise browser returns same document.
	private static int counter = 0;

	/**
	 * Creates a new view user class
	 *
	 * @param user
	 */
	public UserDetailTabItem(User user){
		this.user = user;
		this.userId = user.getId();
	}

	/**
	 * Creates a new view user class
	 *
	 * @param userId
	 */
	public UserDetailTabItem(int userId){
		this.userId = userId;
		new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso)
		{
			user = jso.cast();
		}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return !(user == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}


	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()) + ": Full details");

		// main widget panel
		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("100%");

		// tab panel
		tabPanel = new TabLayoutPanel(33, Unit.PX);
		tabPanel.addStyleName("smallTabPanel");
		final TabItem tab = this;
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			public void onSelection(SelectionEvent<Integer> event) {
				UiElements.runResizeCommands(tab);
			}
		});

		final SimplePanel sp0 = new SimplePanel(); // information overview
		final SimplePanel sp1 = new SimplePanel(); // VOs / Groups / active accounts
		final SimplePanel sp2 = new SimplePanel(); // Resources
		final SimplePanel sp3 = new SimplePanel(); // Facilities
		final SimplePanel sp4 = new SimplePanel(); // Ext identities
		final SimplePanel sp5 = new SimplePanel(); // Publications
		final SimplePanel sp6 = new SimplePanel(); // Certificates, logins, passwords
		final SimplePanel sp7 = new SimplePanel(); // Service identities
		final SimplePanel sp8 = new SimplePanel(); // Sponsored users

		session.getUiElements().resizeSmallTabPanel(tabPanel, 100, this);

		tabPanel.add(sp0, "Information overview");
		tabPanel.add(sp1, "Vos, Groups, Accounts");
		tabPanel.add(sp2, "Resources");
		tabPanel.add(sp3, "Facilities");
		tabPanel.add(sp4, "External identity");
		tabPanel.add(sp5, "Publications");
		tabPanel.add(sp6, "Certificates, Logins, Passwords");

		if (user.isServiceUser()) {
			tabPanel.add(sp7, "Associated users");
		} else {
			tabPanel.add(sp7, "Service identities");
		}

		if (user.isSponsoredUser()) {
			tabPanel.add(sp8, "Sponsors");
		} else if (!user.isServiceUser()) {
			tabPanel.add(sp8, "Sponsored users");
		}

		sp0.setWidget(loadInformationOverview());

		final TabItem publications = new UsersPublicationsTabItem(user);

		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {

			public void onSelection(SelectionEvent<Integer> event) {
				UiElements.runResizeCommands(tab);
				setLastTabId(event.getSelectedItem());
				if (0 == event.getSelectedItem()) {
					if (sp0.getWidget() == null) {
						sp0.setWidget(loadInformationOverview());
					}
				} else if (1 == event.getSelectedItem()) {
					if (sp1.getWidget() == null) {
						sp1.setWidget(loadVosGroupsAccounts());
					}
				} else if (2 == event.getSelectedItem()) {
					if (sp2.getWidget() == null) {
						sp2.setWidget(loadResources());
					}
				} else if (3 == event.getSelectedItem()) {
					if (sp3.getWidget() == null) {
						sp3.setWidget(loadFacilities());
					}
				} else if (4 == event.getSelectedItem()) {
					if (sp4.getWidget() == null) {
						sp4.setWidget(loadExternalIdentities());
					}
				} else if (5 == event.getSelectedItem()) {
					if (sp5.getWidget() == null) {
						sp5.setWidget(publications.draw());
					}
				} else if (6 == event.getSelectedItem()) {
					if (sp6.getWidget() == null) {
						sp6.setWidget(loadCertificatesLoginsPasswords());
					}
				} else if (7 == event.getSelectedItem()) {
					if (sp7.getWidget() == null) {
						sp7.setWidget(loadServiceIdentities());
					}
				} else if (8 == event.getSelectedItem()) {
					if (sp8.getWidget() == null) {
						sp8.setWidget(loadSponsoredUsers());
					}
				};
			}
		});

		// TODO remove after replacement to TabPanelForTabItems
		UiElements.addResizeCommand(new Command() {

			public void execute() {
				UiElements.runResizeCommands(publications);
			}
		}, this);


		tabPanel.selectTab(getLastTabId(), true);  // select and trigger onSelect event

		session.getUiElements().resizePerunTable(tabPanel, 100, this);

		// add tabs to the main panel
		vp.add(tabPanel);
		this.contentWidget.setWidget(vp);

		return getWidget();
	}

	// FIXME and TODO - all private methods should be separate TabItems !!! Connect them with user menu etc. ?

	private Widget loadInformationOverview(){

		// content
		ScrollPanel scroll = new ScrollPanel();
		VerticalPanel extendedInfoVp = new VerticalPanel();
		extendedInfoVp.setStyleName("perun-table");
		scroll.setWidget(extendedInfoVp);
		scroll.setStyleName("perun-tableScrollPanel");

		session.getUiElements().resizeSmallTabPanel(scroll, 350, this);

		extendedInfoVp.setWidth("100%");

		// detail header
		Widget userHeader = new HTML("<h2>" + "User details" + "</h2>");
		extendedInfoVp.add(userHeader);
		extendedInfoVp.setCellHeight(userHeader, "30px");

		final TabItem tab = this;
		final JsonCallbackEvents events = new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				user = jso.cast();
				tab.draw();
			}
		};
		CustomButton change = new CustomButton("", "Edit user", SmallIcons.INSTANCE.applicationFormEditIcon());
		change.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new EditUserDetailsTabItem(user, events));
			}
		});

		// detail content
		FlexTable layout = new FlexTable();
		layout.setCellSpacing(6);
		// Add some standard form options
		layout.setHTML(0, 0, "<strong>Full&nbsp;name:</strong>");
		layout.setHTML(0, 1, SafeHtmlUtils.fromString((user.getFullNameWithTitles() != null) ? user.getFullNameWithTitles() : "").asString());
		layout.setWidget(0, 2, change);
		layout.setHTML(0, 3, "<strong>User&nbsp;ID:</strong>");
		layout.setHTML(0, 4, String.valueOf(user.getId()));
		layout.setHTML(0, 5, "<strong>User&nbsp;type:</strong>");
		if (user.isServiceUser()) {
			layout.setHTML(0, 6, "Service");
		} else if (user.isSponsoredUser()) {
			layout.setHTML(0, 6, "Sponsored");
		} else {
			layout.setHTML(0, 6, "Person");
		}

		// wrap the content in a DecoratorPanel
		DecoratorPanel decPanel = new DecoratorPanel();
		decPanel.setWidget(layout);
		extendedInfoVp.add(decPanel);

		// user attributes

		final GetAttributesV2 attributes = new GetAttributesV2();
		attributes.getUserAttributes(user.getId());

		CellTable<Attribute> tableAttributes = attributes.getTable();
		tableAttributes.addStyleName("perun-table");
		tableAttributes.setWidth("100%");

		Widget attributesHeader = new HTML("<h2>" + "User attributes" + "</h2>");
		extendedInfoVp.add(attributesHeader);
		extendedInfoVp.setCellHeight(attributesHeader, "30px");

		TabMenu menu = new TabMenu();

		final CustomButton saveAttrButton = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save changes in attributes for user");
		saveAttrButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				ArrayList<Attribute> list = attributes.getTableSelectedList();
				if (list == null || list.isEmpty()) {
					Confirm c = new Confirm("No changes to save", new Label("You must select some attributes to save."), true);
					c.show();
					return;
				}

				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("user", userId);

				SetAttributes request = new SetAttributes(JsonCallbackEvents.disableButtonEvents(saveAttrButton, JsonCallbackEvents.refreshTableEvents(attributes)));
				request.setAttributes(ids, list);

			}
		});
		menu.addWidget(saveAttrButton);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, "Set new attributes for user", new ClickHandler() {
			public void onClick(ClickEvent event) {

				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("user", userId);
				session.getTabManager().addTabToCurrentTab(new SetNewAttributeTabItem(ids, attributes.getList()), true);

			}
		}));

		final CustomButton removeAttrButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove attributes from user");
		removeAttrButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				ArrayList<Attribute> list = attributes.getTableSelectedList();
				if (list == null || list.isEmpty()) {
					Confirm c = new Confirm("No changes to save", new Label("You must select some attributes to save."), true);
					c.show();
					return;
				}

				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("user", userId);

				RemoveAttributes request = new RemoveAttributes(JsonCallbackEvents.disableButtonEvents(removeAttrButton, JsonCallbackEvents.refreshTableEvents(attributes)));
				request.removeAttributes(ids, list);

			}
		});
		menu.addWidget(removeAttrButton);

		extendedInfoVp.add(menu);
		extendedInfoVp.add(tableAttributes);

		// VOS
		GetVosWhereUserIsMember vos = new GetVosWhereUserIsMember(user.getId());
		vos.setCheckable(false);

		// get the table with custom onclick
		CellTable<VirtualOrganization> simpeVosTable = vos.getTable(new FieldUpdater<VirtualOrganization, VirtualOrganization>() {
			public void update(int index, VirtualOrganization object, VirtualOrganization value) {
				session.getTabManager().addTab(new VoDetailTabItem(object));
			}
		});

		// format the table
		simpeVosTable.addStyleName("perun-table");
		simpeVosTable.setWidth("100%");

		// simple table
		Widget vosHeader = new HTML("<h2>" + "Virtual organizations" + "</h2>");
		extendedInfoVp.add(vosHeader);
		extendedInfoVp.setCellHeight(vosHeader, "30px");
		extendedInfoVp.add(simpeVosTable);

		return scroll;

	}

	private Widget loadExternalIdentities() {

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();

		final GetUserExtSources extSources = new GetUserExtSources(user.getId());

		// reload tab events
		final TabItem tab = this;
		final JsonCallbackEvents reloadTabEvents = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				session.getTabManager().reloadTab(tab);
			}
		};

		// buttons
		CustomButton addUserExtSourceButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, "Add new external identity to user", new ClickHandler() {
			public void onClick(ClickEvent event) {
				// when user click on add ext source btn
				session.getTabManager().addTabToCurrentTab(new AddUserExtSourceTabItem(user.getId()));
			}
		});

		final CustomButton removeUserExtSourceButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Removes selected external identities from this user");
		removeUserExtSourceButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<UserExtSource> list = extSources.getTableSelectedList();
				UiElements.showDeleteConfirm(list, "Following external identities will be removed from user.", new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						for (int i=0; i<list.size(); i++ ) {
							// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
							if (i == list.size()-1) {
								RemoveUserExtSource request = new RemoveUserExtSource(JsonCallbackEvents.disableButtonEvents(removeUserExtSourceButton, reloadTabEvents));
								request.removeUserExtSource(user.getId(), list.get(i).getId());
							} else {
								RemoveUserExtSource request = new RemoveUserExtSource(JsonCallbackEvents.disableButtonEvents(removeUserExtSourceButton));
								request.removeUserExtSource(user.getId(), list.get(i).getId());
							}
						}
					}
				});
			}
		});


		menu.addWidget(addUserExtSourceButton);
		menu.addWidget(removeUserExtSourceButton);
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		CellTable<UserExtSource> table = extSources.getTable(new FieldUpdater<UserExtSource, String>() {
			@Override
			public void update(int index, UserExtSource object, String value) {
				session.getTabManager().addTab(new UserExtSourceDetailTabItem(object));
			}
		});
		table.addStyleName("perun-table");
		table.setWidth("100%");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		vp.add(sp);
		vp.setCellHeight(sp, "100%");

		session.getUiElements().resizeSmallTabPanel(sp, 320, this);

		removeUserExtSourceButton.setEnabled(false);
		JsonUtils.addTableManagedButton(extSources, table, removeUserExtSourceButton);

		return vp;

	}

	private Widget loadResources(){

		cz.metacentrum.perun.webgui.json.usersManager.GetAssignedRichResources resources = new cz.metacentrum.perun.webgui.json.usersManager.GetAssignedRichResources(user.getId(), PerunEntity.USER);
		resources.setCheckable(false);
		CellTable<RichResource> table = resources.getTable(new FieldUpdater<RichResource, String>() {
			@Override
			public void update(int index, RichResource object, String value) {
				// show as vo admin
				session.getTabManager().addTab(new ResourceDetailTabItem(object, 0));
			}
		});

		table.addStyleName("perun-table");
		table.setWidth("100%");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");
		vp.add(sp);
		session.getUiElements().resizeSmallTabPanel(sp, 320, this);
		vp.setCellHeight(sp, "100%");

		return vp;

	}

	private Widget loadVosGroupsAccounts() {

		// whole content
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// menu
		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		// final VO name widget
		final Hyperlink voLabel = new Hyperlink();

		final ListBoxWithObjects<VirtualOrganization> listbox = new ListBoxWithObjects<VirtualOrganization>();
		menu.addWidget(new HTML("<strong>Select&nbsp;user's&nbsp;VO:</strong>"));
		menu.addWidget(listbox);
		menu.addWidget(voLabel);

		// sub content
		final ScrollPanel subContent = new ScrollPanel();
		subContent.setSize("100%", "100%");
		vp.add(subContent);
		subContent.setStyleName("perun-tableScrollPanel");

		session.getUiElements().resizeSmallTabPanel(subContent, 400, this);

		JsonCallbackEvents events = new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				listbox.clear();
				ArrayList<VirtualOrganization> list = JsonUtils.jsoAsList(jso);
				list = new TableSorter<VirtualOrganization>().sortByShortName(list);
				for (int i=0; i<list.size(); i++){
					listbox.addItem(list.get(i));
				}
				if (!listbox.isEmpty()) {
					loadMemberSubContent(subContent, voLabel, listbox);
				};
			}
			@Override
			public void onLoadingStart() {
				listbox.clear();
				listbox.addItem("Loading...");
			}
			@Override
			public void onError(PerunError error) {
				listbox.clear();
				listbox.addItem("Error while loading");
			}

		};

		GetVosWhereUserIsMember vosCall = new GetVosWhereUserIsMember(user.getId(), events);
		vosCall.retrieveData();

		// change handler
		listbox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				loadMemberSubContent(subContent, voLabel, listbox);
			}
		});

		return vp;

	}

	private void loadMemberSubContent(final SimplePanel subContent, final Hyperlink voLabel, final ListBoxWithObjects<VirtualOrganization> listbox){

		subContent.setWidget(new AjaxLoaderImage());

		final GetMemberByUser gmbu = new GetMemberByUser(listbox.getSelectedObject().getId(), user.getId());

		JsonCallbackEvents loadEvent = new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				// get member
				final Member member = jso.cast();
				// create content panel and put it on page
				final VerticalPanel entryPanel = new VerticalPanel();
				entryPanel.setStyleName("perun-table");
				entryPanel.setSize("100%", "100%");
				subContent.setWidget(entryPanel);

				voLabel.setHTML(SafeHtmlUtils.fromSafeConstant("<h2>" + SafeHtmlUtils.fromString((listbox.getSelectedObject().getName() != null) ? listbox.getSelectedObject().getName() : "").asString() + "</h2>"));
				voLabel.setTargetHistoryToken(session.getTabManager().getLinkForTab(new VoDetailTabItem(listbox.getSelectedObject())));

				// detail header
				Widget memberHeader = new HTML("<h2>" + "Member details" + "</h2>");
				entryPanel.add(memberHeader);
				entryPanel.setCellHeight(memberHeader, "30px");

				// detail content
				FlexTable layout = new FlexTable();
				layout.setCellSpacing(6);
				// Add some standard form options
				layout.setHTML(0, 0, "<strong>Member&nbsp;ID:</strong>");
				layout.setHTML(0, 1, String.valueOf(member.getId()));

				ImageResource ir = null;

				// member status
				if(member.getStatus().equalsIgnoreCase("VALID")){
					ir = SmallIcons.INSTANCE.acceptIcon();
				} else if (member.getStatus().equalsIgnoreCase("INVALID")){
					ir = SmallIcons.INSTANCE.flagRedIcon();
				} else if (member.getStatus().equalsIgnoreCase("SUSPENDED")){
					ir = SmallIcons.INSTANCE.stopIcon();
				} else if (member.getStatus().equalsIgnoreCase("EXPIRED")){
					ir = SmallIcons.INSTANCE.flagYellowIcon();
				} else if (member.getStatus().equalsIgnoreCase("DISABLED")){
					ir = SmallIcons.INSTANCE.binClosedIcon();
				}

				HTML status = new HTML("<a>" + member.getStatus() + " " + new Image(ir) + "</a>");
				layout.setHTML(1, 0, "<strong>Member status: </strong>");
				layout.setWidget(1, 1, status);

				// member status - on click action
				status.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						PerunWebSession.getInstance().getTabManager().addTabToCurrentTab(
							new ChangeStatusTabItem(member, new JsonCallbackEvents(){
								@Override
								public void onFinished(JavaScriptObject jso) {
									loadMemberSubContent(subContent, voLabel, listbox);
								}
							}));
					}
				});

				final ListBoxWithObjects<Resource> resList = new ListBoxWithObjects<Resource>();

				GetAssignedResources res = new GetAssignedResources(member.getId(), PerunEntity.MEMBER, new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						resList.clear();
						ArrayList<Resource> list = JsonUtils.jsoAsList(jso);
						if (list == null || list.isEmpty()){
							resList.addItem("No resources found");
						} else {
							list = new TableSorter<Resource>().sortByName(list);
							resList.addNotSelectedOption();
							resList.addAllItems(list);
						}
					}
				@Override
				public void onError(PerunError error) {
					resList.clear();
					resList.addItem("Error while loading");
				}
				@Override
				public void onLoadingStart() {
					resList.clear();
					resList.addItem("Loading...");
				}
				});
				res.retrieveData();

				// link to member's detail
				Hyperlink link = new Hyperlink();
				link.setText("View detail");
				layout.setHTML(2, 0, "<strong>Member's detail page:</strong>");
				layout.setWidget(2, 1, link);
				link.setTargetHistoryToken(session.getTabManager().getLinkForTab(new MemberDetailTabItem(member.getId(), 0)));

				// wrap the content in a DecoratorPanel
				DecoratorPanel decPanel = new DecoratorPanel();
				decPanel.setWidget(layout);
				entryPanel.add(decPanel);
				entryPanel.setCellHeight(decPanel, "50px");

				// tables

				// detail header
				Widget groupHeader = new HTML("<h2>" + "Member groups" + "</h2>");
				entryPanel.add(groupHeader);
				entryPanel.setCellHeight(groupHeader, "30px");

				final GetMemberGroups groups = new GetMemberGroups(member.getId());
				groups.setCheckable(false);
				groups.setEditable(false);

				CellTable<Group> table = groups.getTable();
				table.addStyleName("perun-table");
				table.setWidth("100%");
				entryPanel.add(table);

				// detail header
				Widget attrHeader = new HTML("<h2>" + "Member / Member-resource attributes" + "</h2>");
				entryPanel.add(attrHeader);
				entryPanel.setCellHeight(attrHeader, "30px");

				final GetAttributesV2 attributes = new GetAttributesV2();
				attributes.getMemberAttributes(member.getId());

				resList.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						if (resList.getSelectedIndex() == 0) {
							attributes.getMemberAttributes(member.getId());
							attributes.retrieveData();
						} else {
							attributes.getMemberResourceAttributes(member.getId(), resList.getSelectedObject().getId());
							attributes.retrieveData();
						}
					}
				});

				TabMenu menu = new TabMenu();

				final CustomButton saveAttrButton = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save changes in attributes for member");
				saveAttrButton.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {

						ArrayList<Attribute> list = attributes.getTableSelectedList();
						if (UiElements.cantSaveEmptyListDialogBox(list)) {

							Map<String, Integer> ids = new HashMap<String,Integer>();
							ids.put("member", member.getId());
							if (resList.getSelectedIndex() > 0) {
								ids.put("resource", resList.getSelectedObject().getId());
							}

							SetAttributes request = new SetAttributes(JsonCallbackEvents.disableButtonEvents(saveAttrButton, JsonCallbackEvents.refreshTableEvents(attributes)));
							request.setAttributes(ids, list);


						}

					}
				});
				menu.addWidget(saveAttrButton);

				menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, "Set new attributes for member", new ClickHandler() {
					public void onClick(ClickEvent event) {

						Map<String, Integer> ids = new HashMap<String,Integer>();
						ids.put("member", member.getId());
						if (resList.getSelectedIndex() > 0) {
							ids.put("resource", resList.getSelectedObject().getId());
						}
						session.getTabManager().addTabToCurrentTab(new SetNewAttributeTabItem(ids, attributes.getList()), true);

					}
				}));

				final CustomButton removeAttrButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove attributes from member");
				removeAttrButton.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						ArrayList<Attribute> list = attributes.getTableSelectedList();
						if (UiElements.cantSaveEmptyListDialogBox(list)) {
							Map<String, Integer> ids = new HashMap<String, Integer>();
							ids.put("member", member.getId());
							if (resList.getSelectedIndex() > 0) {
								ids.put("resource", resList.getSelectedObject().getId());
							}
							RemoveAttributes request = new RemoveAttributes(JsonCallbackEvents.disableButtonEvents(removeAttrButton, JsonCallbackEvents.refreshTableEvents(attributes)));
							request.removeAttributes(ids, list);

						}
					}
				});
				menu.addWidget(removeAttrButton);

				menu.addWidget(new HTML("<strong>Resource:</strong>"));
				menu.addWidget(resList);

				entryPanel.add(menu);

				CellTable<Attribute> attrTable = attributes.getTable();
				attrTable.addStyleName("perun-table");
				attrTable.setWidth("100%");
				entryPanel.add(attrTable);

			}
			@Override
			public void onError(PerunError error) {
				subContent.setWidget(new AjaxLoaderImage().loadingError(error));
			}

		};

		// set events & load data
		gmbu.setEvents(loadEvent);
		gmbu.retrieveData();

	}

	private Widget loadCertificatesLoginsPasswords()
	{
		// set content

		final VerticalPanel attributesTable = new VerticalPanel();

		final TabMenu menu = new TabMenu();
		attributesTable.add(menu);

		attributesTable.setWidth("100%");

		final ScrollPanel sp = new ScrollPanel();

		sp.setStyleName("perun-tableScrollPanel");
		session.getUiElements().resizeSmallTabPanel(sp, 350, this);
		sp.setWidth("100%");

		//attributesTable.setStyleName("userDetailTable");

		// clear logins when page refreshed
		userLoginAttrs.clear();

		// ids used for attribute handling
		Map<String, Integer> ids = new HashMap<String, Integer>();
		ids.put("user", userId);

		// attribute table
		final PerunAttributeTableWidget table = new PerunAttributeTableWidget(ids);
		table.setDescriptionShown(true);
		table.setDisplaySaveButton(false);

		// load data
		GetListOfAttributes attributes = new GetListOfAttributes(new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {

				userLoginAttrs.addAll(JsonUtils.<Attribute>jsoAsList(jso));

				userLoginAttrs = new TableSorter<Attribute>().sortByAttrNameTranslation(userLoginAttrs);
				for (Attribute a : userLoginAttrs) {
					if (a.getFriendlyName().equalsIgnoreCase("userCertificates")) {
						table.add(a);
					} else if (a.getFriendlyName().equalsIgnoreCase("sshPublicKey")) {
						table.add(a);
					} else if (a.getFriendlyName().equalsIgnoreCase("kerberosAdminPrincipal")) {
						table.add(a);
					}  else if (a.getFriendlyName().equalsIgnoreCase("sshPublicAdminKey")) {
						table.add(a);
					}
				}
				// build attr table
				table.build();

				menu.addWidget(table.getSaveButton());

				// content
				VerticalPanel vpContent = new VerticalPanel();
				vpContent.add(table.asWidget());

				FlexTable innerTable = new FlexTable();
				innerTable.setCellPadding(5);
				vpContent.add(innerTable);

				// set content to page
				sp.add(vpContent);
				attributesTable.add(sp);

				int rowCount = 0;

				for (final Attribute a : userLoginAttrs) {
					if (a.getBaseFriendlyName().equalsIgnoreCase("login-namespace")) {
						if (a.getValueAsObject() != null) {
							// name
							innerTable.setHTML(rowCount, 0, "<strong>"+SafeHtmlUtils.fromString(a.getDisplayName()).asString()+"</strong>");
							// value
							innerTable.setHTML(rowCount, 1, SafeHtmlUtils.fromString(a.getValue()).asString());
							// change password
							if (Utils.getSupportedPasswordNamespaces().contains(a.getFriendlyNameParameter())) {

								CustomButton cb = new CustomButton("Change password…", SmallIcons.INSTANCE.keyIcon(), new ClickHandler(){
									public void onClick(ClickEvent event) {
										session.getTabManager().addTabToCurrentTab(new SelfPasswordTabItem(user, a.getFriendlyNameParameter(), a.getValue(), SelfPasswordTabItem.Actions.CHANGE));
									}
								});

								innerTable.setWidget(rowCount, 2, cb);

								CustomButton cb2 = new CustomButton("Reset to randomly generated password", SmallIcons.INSTANCE.keyIcon(), new ClickHandler(){
									public void onClick(ClickEvent event) {

										String response = Utils.getBinaryResource(PerunWebSession.getInstance().getRpcUrl()+"usersManager/changePasswordRandom",
												"{ \"userId\": " + userId + " , \"loginNamespace\": \"" + a.getFriendlyNameParameter() + "\" }");

										counter++;
										Utils.convertBinaryToDownloadableFile(counter,"PasswordReset"+Random.nextInt(10000)+".pdf", "application/pdf", response);

									}
								});
								innerTable.setWidget(rowCount, 3, cb2);

							}
							rowCount++;
						}
					}
				}
			}
		});

		HashSet<String> set = new HashSet<String>();
		set.add("urn:perun:user:attribute-def:def:userCertificates");
		set.add("urn:perun:user:attribute-def:def:kerberosAdminPrincipal");
		set.add("urn:perun:user:attribute-def:def:sshPublicKey");
		set.add("urn:perun:user:attribute-def:def:sshPublicAdminKey");
		set.add("urn:perun:user:attribute-def:def:login-namespace:mu");
		set.add("urn:perun:user:attribute-def:def:login-namespace:einfra");
		set.add("urn:perun:user:attribute-def:def:login-namespace:sitola");
		set.add("urn:perun:user:attribute-def:def:login-namespace:egi-ui");
		set.add("urn:perun:user:attribute-def:def:login-namespace:cesnet");
		set.add("urn:perun:user:attribute-def:def:login-namespace:meta");
		set.add("urn:perun:user:attribute-def:def:login-namespace:einfra-services");
		set.add("urn:perun:user:attribute-def:def:login-namespace:shongo");
		// TODO - FIXME - must be dynamic !!
		for (String namespace : Utils.getSupportedPasswordNamespaces()) {
			set.add("urn:perun:user:attribute-def:def:login-namespace:"+namespace);
		}

		// TODO - remove SHONGO
		ArrayList<String> list = JsonUtils.setToList(set);
		attributes.getListOfAttributes(ids, list);

		return attributesTable;
	}

	private Widget loadFacilities() {

		// whole content
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// menu
		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		final ListBoxWithObjects<Facility> listbox = new ListBoxWithObjects<Facility>();
		menu.addWidget(new HTML("<strong>Select&nbsp;user's&nbsp;Facility:</strong>"));
		menu.addWidget(listbox);

		// sub content
		final SimplePanel subContent = new SimplePanel();
		subContent.setSize("100%", "100%");
		vp.add(subContent);
		vp.setCellHeight(subContent, "100%");

		final Hyperlink facilityLabel = new Hyperlink();
		menu.addWidget(facilityLabel);

		JsonCallbackEvents events = new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				listbox.clear();
				ArrayList<Facility> list = JsonUtils.jsoAsList(jso);
				list = new TableSorter<Facility>().sortByName(list);
				for (int i=0; i<list.size(); i++){
					listbox.addItem(list.get(i));
				}
				if (!listbox.isEmpty()){
					loadFacilitySubContent(subContent, facilityLabel, listbox);
				}
			}
			@Override
			public void onLoadingStart() {
				listbox.clear();
				listbox.addItem("Loading...");
			}
			@Override
			public void onError(PerunError error) {
				listbox.clear();
				listbox.addItem("Error while loading");
			}
		};

		GetAssignedFacilities facCall = new GetAssignedFacilities(PerunEntity.USER, user.getId(), events);
		facCall.retrieveData();

		// change handler
		listbox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				loadFacilitySubContent(subContent, facilityLabel, listbox);
			}
		});

		return vp;

	}

	private void loadFacilitySubContent(final SimplePanel subContent, final Hyperlink facilityLabel, final ListBoxWithObjects<Facility> listbox){

		final VerticalPanel entryPanel = new VerticalPanel();
		entryPanel.setSize("100%", "100%");
		subContent.setWidget(entryPanel);

		facilityLabel.setHTML(SafeHtmlUtils.fromSafeConstant("<h2>" + SafeHtmlUtils.fromString((listbox.getSelectedObject().getName() != null) ? listbox.getSelectedObject().getName() : "").asString() + "</h2>"));
		facilityLabel.setTargetHistoryToken(session.getTabManager().getLinkForTab(new FacilityDetailTabItem(listbox.getSelectedObject())));

		final GetAttributesV2 attributes = new GetAttributesV2();
		attributes.getUserFacilityAttributes(listbox.getSelectedObject().getId(), user.getId());

		TabMenu menu = new TabMenu();

		final CustomButton saveAttrButton = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save changes in attributes");
		saveAttrButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				ArrayList<Attribute> list = attributes.getTableSelectedList();
				if (list == null || list.isEmpty()) {
					Confirm c = new Confirm("No changes to save", new Label("You must select some attributes to save."), true);
					c.show();
					return;
				}

				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("user", userId);
				ids.put("facility", listbox.getSelectedObject().getId());

				SetAttributes request = new SetAttributes(JsonCallbackEvents.disableButtonEvents(saveAttrButton, JsonCallbackEvents.refreshTableEvents(attributes)));
				request.setAttributes(ids, list);

			}
		});
		menu.addWidget(saveAttrButton);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, "Set new attributes", new ClickHandler() {
			public void onClick(ClickEvent event) {

				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("user", userId);
				ids.put("facility", listbox.getSelectedObject().getId());
				session.getTabManager().addTabToCurrentTab(new SetNewAttributeTabItem(ids, attributes.getList()), true);

			}
		}));

		final CustomButton removeAttrButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove attributes");
		removeAttrButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				ArrayList<Attribute> list = attributes.getTableSelectedList();
				if (list == null || list.isEmpty()) {
					Confirm c = new Confirm("No changes to save", new Label("You must select some attributes to save."), true);
					c.show();
					return;
				}

				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("user", userId);
				ids.put("facility", listbox.getSelectedObject().getId());

				RemoveAttributes request = new RemoveAttributes(JsonCallbackEvents.disableButtonEvents(removeAttrButton, JsonCallbackEvents.refreshTableEvents(attributes)));
				request.removeAttributes(ids, list);

			}
		});
		menu.addWidget(removeAttrButton);

		entryPanel.add(menu);

		CellTable<Attribute> table = attributes.getTable();
		table.addStyleName("perun-table");
		table.setWidth("100%");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		entryPanel.add(sp);

		// better format
		entryPanel.add(new SimplePanel());
		entryPanel.setCellHeight(entryPanel.getWidget(entryPanel.getWidgetCount()-1), "100%");

		session.getUiElements().resizeSmallTabPanel(sp, 320, this);

	}

	public VerticalPanel loadServiceIdentities(){

		// Content
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		if (user.isServiceUser()) {

			// SERVICE TYPE user

			// request
			final GetUsersBySpecificUser request = new GetUsersBySpecificUser(userId);

			// menu
			TabMenu menu = new TabMenu();
			vp.add(menu);
			vp.setCellHeight(menu, "30px");

			// buttons
			menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, "Add new user to service identity: "+user.getLastName(), new ClickHandler() {
				public void onClick(ClickEvent clickEvent) {
					session.getTabManager().addTabToCurrentTab(new ConnectServiceIdentityTabItem(user), true);
				}
			}));

			final CustomButton removeUserButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove user from service identity "+user.getLastName());
			menu.addWidget(removeUserButton);
			removeUserButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent clickEvent) {

					final ArrayList<User> list = request.getTableSelectedList();
					final ArrayList<User> fullList = request.getList();

					if (fullList.size() == list.size()) {

						UiElements.generateAlert("Remove warning", "<strong><span class=\"serverResponseLabelError\">If you remove all users from service identity you won't be able to use it in the future.</br></br>Please consider keeping at least one user, e.g. add someone else before you remove yourself.</span></strong><p><strong>Do you wish to continue anyway ?</strong>", new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {

								UiElements.showDeleteConfirm(list, "Following users will be removed from service identity and they will lose all access to it. Only users associated with service identity can add other users again. If you remove all users connected to the service identity, you won't be able to use it in future!", new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										for (int i = 0; i < list.size(); i++) {
											// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
											RemoveSpecificUserOwner req;
											if (i == list.size() - 1) {
												req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton, JsonCallbackEvents.refreshTableEvents(request)));
											} else {
												req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton));
											}
											req.removeServiceUser(list.get(i), user);

											// TODO - consider fixing authz in session ?

										}
									}
								});

							}
						});

					} else {

						// if not selected myself, continue same way
						UiElements.showDeleteConfirm(list, "Following users will be removed from service identity and they will lose any access to it. Only users associated with service identity can add other users again. If you remove all users connected to the service identity, it will be deleted too!", new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								for (int i = 0; i < list.size(); i++) {
									// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
									RemoveSpecificUserOwner req;
									if (i == list.size() - 1) {
										req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton, JsonCallbackEvents.refreshTableEvents(request)));
									} else {
										req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton));
									}
									req.removeServiceUser(list.get(i), user);

									// TODO - consider fixing authz in session ?

								}
							}
						});

					}

				}
			});

			// table
			CellTable<User> table = request.getTable(new FieldUpdater<User, String>() {
				public void update(int i, User user, String s) {
					session.getTabManager().addTab(new UserDetailTabItem(user));
				}
			});

			removeUserButton.setEnabled(false);
			JsonUtils.addTableManagedButton(request, table, removeUserButton);

			table.addStyleName("perun-table");
			table.setWidth("100%");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			vp.add(sp);

		} else {

			// PERSON TYPE user

			// request
			final GetSpecificUsersByUser request = new GetSpecificUsersByUser(userId);
			request.setHideSponsored(true);

			// menu
			TabMenu menu = new TabMenu();
			vp.add(menu);
			vp.setCellHeight(menu, "30px");

			// buttons
			menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, "Add new service identity to "+user.getFullName(), new ClickHandler() {
				public void onClick(ClickEvent clickEvent) {
					session.getTabManager().addTabToCurrentTab(new ConnectServiceIdentityTabItem(user), true);
				}
			}));

			final CustomButton removeUserButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove service identity from "+user.getFullName());
			menu.addWidget(removeUserButton);
			removeUserButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					final ArrayList<User> list = request.getTableSelectedList();
					UiElements.showDeleteConfirm(list, "Following service identities will be removed from user.", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
							for (int i=0; i<list.size(); i++ ) {
								RemoveSpecificUserOwner req;
								// if last, refresh
								if(i == list.size() - 1) {
									req = new RemoveSpecificUserOwner(JsonCallbackEvents.refreshTableEvents(request));
								} else {
									req = new RemoveSpecificUserOwner();
								}
								req.removeServiceUser(user, list.get(i));
							}
						}
					});
				}
			});

			// table
			CellTable<User> table = request.getTable(new FieldUpdater<User, String>() {
				public void update(int i, User user, String s) {
					session.getTabManager().addTab(new UserDetailTabItem(user));
				}
			});

			removeUserButton.setEnabled(false);
			JsonUtils.addTableManagedButton(request, table, removeUserButton);

			table.addStyleName("perun-table");
			table.setWidth("100%");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			vp.add(sp);

		}

		return vp;

	}

	public VerticalPanel loadSponsoredUsers(){

		// Content
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		if (user.isSponsoredUser()) {

			// SPONSORED TYPE user

			// request
			final GetUsersBySpecificUser request = new GetUsersBySpecificUser(userId);

			// menu
			TabMenu menu = new TabMenu();
			vp.add(menu);
			vp.setCellHeight(menu, "30px");

			// buttons
			menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, "Add new sponsor to "+user.getLastName(), new ClickHandler() {
				public void onClick(ClickEvent clickEvent) {
					session.getTabManager().addTabToCurrentTab(new ConnectServiceIdentityTabItem(user), true);
				}
			}));

			final CustomButton removeUserButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove sponsors from "+user.getLastName());
			menu.addWidget(removeUserButton);
			removeUserButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent clickEvent) {

					final ArrayList<User> list = request.getTableSelectedList();
					final ArrayList<User> fullList = request.getList();

					if (fullList.size() == list.size()) {

						UiElements.generateAlert("Remove warning", "<strong><span class=\"serverResponseLabelError\">If you remove all users from sponsored user you won't be able to use it in the future.</br></br>Please consider keeping at least one user, e.g. add someone else before you remove yourself.</span></strong><p><strong>Do you wish to continue anyway ?</strong>", new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {

								UiElements.showDeleteConfirm(list, "Following users will be removed from sponsored user and they will lose all access to it. Only users associated with sponsored user can add other users again. If you remove all users connected to the sponsored user, you won't be able to use it in future!", new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										for (int i = 0; i < list.size(); i++) {
											// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
											RemoveSpecificUserOwner req;
											if (i == list.size() - 1) {
												req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton, JsonCallbackEvents.refreshTableEvents(request)));
											} else {
												req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton));
											}
											req.removeServiceUser(list.get(i), user);

											// TODO - consider fixing authz in session ?

										}
									}
								});

							}
						});

					} else {

						// if not selected myself, continue same way
						UiElements.showDeleteConfirm(list, "Following users will be removed from sponsored user and they will lose any access to it. Only users associated with sponsored user can add other users again. If you remove all users connected to the sponsored user, it will be deleted too!", new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								for (int i = 0; i < list.size(); i++) {
									// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
									RemoveSpecificUserOwner req;
									if (i == list.size() - 1) {
										req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton, JsonCallbackEvents.refreshTableEvents(request)));
									} else {
										req = new RemoveSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(removeUserButton));
									}
									req.removeServiceUser(list.get(i), user);

									// TODO - consider fixing authz in session ?

								}
							}
						});

					}

				}
			});

			// table
			CellTable<User> table = request.getTable(new FieldUpdater<User, String>() {
				public void update(int i, User user, String s) {
					session.getTabManager().addTab(new UserDetailTabItem(user));
				}
			});

			removeUserButton.setEnabled(false);
			JsonUtils.addTableManagedButton(request, table, removeUserButton);

			table.addStyleName("perun-table");
			table.setWidth("100%");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			vp.add(sp);

		} else {

			// PERSON TYPE user

			// request
			final GetSpecificUsersByUser request = new GetSpecificUsersByUser(userId);
			request.setHideService(true);

			// menu
			TabMenu menu = new TabMenu();
			vp.add(menu);
			vp.setCellHeight(menu, "30px");

			// buttons
			menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, "Add new sponsored user to "+user.getFullName(), new ClickHandler() {
				public void onClick(ClickEvent clickEvent) {
					session.getTabManager().addTabToCurrentTab(new ConnectServiceIdentityTabItem(user), true);
				}
			}));

			final CustomButton removeUserButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove sponsored user from "+user.getFullName());
			menu.addWidget(removeUserButton);
			removeUserButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					final ArrayList<User> list = request.getTableSelectedList();
					UiElements.showDeleteConfirm(list, "Following sponsored users will be removed from user.", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
							for (int i=0; i<list.size(); i++ ) {
								RemoveSpecificUserOwner req;
								// if last, refresh
								if(i == list.size() - 1) {
									req = new RemoveSpecificUserOwner(JsonCallbackEvents.refreshTableEvents(request));
								} else {
									req = new RemoveSpecificUserOwner();
								}
								req.removeServiceUser(user, list.get(i));
							}
						}
					});
				}
			});

			// table
			CellTable<User> table = request.getTable(new FieldUpdater<User, String>() {
				public void update(int i, User user, String s) {
					session.getTabManager().addTab(new UserDetailTabItem(user));
				}
			});

			removeUserButton.setEnabled(false);
			JsonUtils.addTableManagedButton(request, table, removeUserButton);

			table.addStyleName("perun-table");
			table.setWidth("100%");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			vp.add(sp);

		}

		return vp;

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.userGrayIcon();
	}

	/**
	 * Returns ID of last selected subtab in this page
	 *
	 * @return ID of subtab
	 */
	private int getLastTabId(){
		return this.lastTabId;
	}

	/**
	 * Sets ID of subtab as last selected
	 *
	 * @param id
	 */
	private void setLastTabId(int id){
		this.lastTabId = id;
	}

	@Override
	public int hashCode() {
		final int prime = 1279;
		int result = 1;
		result = prime * result + userId;
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
		UserDetailTabItem other = (UserDetailTabItem) obj;
		if (userId != other.userId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Users", PerunAdminTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"users", user.getFullNameWithTitles(), getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "detail";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId;
	}

	static public UserDetailTabItem load(Map<String, String> parameters) {
		int uid = Integer.parseInt(parameters.get("id"));
		return new UserDetailTabItem(uid);
	}
}
