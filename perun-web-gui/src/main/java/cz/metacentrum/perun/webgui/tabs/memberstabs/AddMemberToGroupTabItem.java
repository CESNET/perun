package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
import cz.metacentrum.perun.webgui.json.groupsManager.AddMember;
import cz.metacentrum.perun.webgui.json.membersManager.CreateMember;
import cz.metacentrum.perun.webgui.json.membersManager.FindCompleteRichMembers;
import cz.metacentrum.perun.webgui.json.membersManager.GetCompleteRichMembers;
import cz.metacentrum.perun.webgui.json.registrarManager.SendInvitation;
import cz.metacentrum.perun.webgui.json.vosManager.FindCandidatesOrUsersToAddToVo;
import cz.metacentrum.perun.webgui.model.Candidate;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.MembersTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Add member to group page
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddMemberToGroupTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading group");

	// data
	private Group group;

	// when searching
	private String searchString = "";

	private int groupId;
	private ArrayList<GeneralObject> alreadyAddedList = new ArrayList<GeneralObject>();
	private SimplePanel alreadyAdded = new SimplePanel();

	private boolean search = true;
	private boolean searchCandidates = false;

	ScrollPanel sp = new ScrollPanel();
	ScrollPanel sp2 = new ScrollPanel();

	/**
	 * Creates a tab instance
	 *
	 * @param group
	 */
	public AddMemberToGroupTabItem(Group group) {
		this.group = group;
		this.groupId = group.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param groupId
	 */
	public AddMemberToGroupTabItem(int groupId) {
		this.groupId = groupId;
		JsonCallbackEvents events = new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso) {
				group = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.GROUP, groupId, events).retrieveData();
	}


	public boolean isPrepared() {
		return !(group == null);
	}

	public Widget draw() {

		titleWidget.setText("Add member(s)");

		// MAIN PANEL
		final VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		boolean isMembersGroup = group.isCoreGroup();

		// if members or admins group, hide
		if (isMembersGroup) {

			firstTabPanel.add(new HTML("<p>Group \"" + group.getName() + "\" can't have members managed from Group admin. Please use VO admin section.</p>"));
			this.contentWidget.setWidget(firstTabPanel);
			return getWidget();

		}

		// MENU
		TabMenu tabMenu = new TabMenu();
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");

		// for resize
		final TabItem tab = this;

		// CALLBACKS
		final FindCompleteRichMembers findMembers;                            // for both
		final GetCompleteRichMembers getAllMembers;                           // for both
		final FindCandidatesOrUsersToAddToVo findCandidatesOrUsersToAddToVo;  // for VO/group admin differs search

		// elements handled by callback events
		final CustomButton searchButton = new CustomButton("Search in VO", ButtonTranslation.INSTANCE.searchMemberInVo(), SmallIcons.INSTANCE.findIcon());
		final CustomButton searchGloballyButton = new CustomButton("Search globally", ButtonTranslation.INSTANCE.searchForMembersInExtSources(), SmallIcons.INSTANCE.findIcon());
		final CustomButton listAllButton = new CustomButton("List all VO members", ButtonTranslation.INSTANCE.listAllMembersInVo(), SmallIcons.INSTANCE.userGreenIcon());

		final CheckBox disabled = new CheckBox(WidgetTranslation.INSTANCE.showDisabledMembers());

		// search through whole VO
		findMembers = new FindCompleteRichMembers(PerunEntity.VIRTUAL_ORGANIZATION, group.getVoId(), "", null);
		getAllMembers = new GetCompleteRichMembers(PerunEntity.VIRTUAL_ORGANIZATION, group.getVoId(), null);

		if (session.isVoAdmin(group.getVoId())) {
			// will search vo ext sources and users
			findCandidatesOrUsersToAddToVo = new FindCandidatesOrUsersToAddToVo(group.getVoId(), 0, "", null);
		} else {
			// will search group ext sources only
			findCandidatesOrUsersToAddToVo = new FindCandidatesOrUsersToAddToVo(group.getVoId(), group.getId(), "", null);
		}

		final CellTable<Candidate> candidatesTable = findCandidatesOrUsersToAddToVo.getEmptyTable();

		final CellTable<RichMember> table = findMembers.getEmptyTable(new FieldUpdater<RichMember, RichMember>() {
			// when user click on a row -> open new tab
			public void update(int index, RichMember object, RichMember value) {
				session.getTabManager().addTab(new MemberDetailTabItem(object.getId(), groupId));
			}
		});

		final CustomButton inviteButton = new CustomButton("Invite selected", SmallIcons.INSTANCE.emailIcon());
		final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedMemberToGroup());

		final ExtendedTextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
			public void searchFor(String text) {
				searchString = text;
				findMembers.searchFor(searchString);
				search = true;
				searchCandidates = false;
				// remove previous table
				firstTabPanel.getWidget(2).removeFromParent();
				firstTabPanel.add(sp);
				UiElements.runResizeCommands(tab);
			}
		}, searchButton);
		searchBox.getTextBox().setText(searchString);

		// bind text box also to search globally
		searchBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (!searchBox.getTextBox().getText().trim().isEmpty()) {
					searchGloballyButton.setEnabled(true);
					// do not trigger search on both !!
				} else {
					searchGloballyButton.setEnabled(false);
				}
			}
		});
		searchBox.getTextBox().addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				// fake some meaningless KeyUpEvent
				DomEvent.fireNativeEvent(Document.get().createKeyUpEvent(false, false, false, false, KeyCodes.KEY_DOWN), searchBox.getTextBox());
			}
		});
		searchBox.getTextBox().addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				// fake some meaningless KeyUpEvent
				DomEvent.fireNativeEvent(Document.get().createKeyUpEvent(false, false, false, false, KeyCodes.KEY_DOWN), searchBox.getTextBox());
			}
		});
		// button click triggers action
		searchGloballyButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				if(UiElements.searchStingCantBeEmpty(searchBox.getTextBox().getText().trim())){
					new PerunSearchEvent() {
						public void searchFor(String text) {
							searchString = text;
							findCandidatesOrUsersToAddToVo.searchFor(searchString);
							searchCandidates = true;
							search = false;
							// remove previous table
							firstTabPanel.getWidget(2).removeFromParent();
							firstTabPanel.add(sp2);
							UiElements.runResizeCommands(tab);
						}
					}.searchFor(searchBox.getTextBox().getText().trim());
				}
			}
		});
		searchGloballyButton.setEnabled(false);

		findMembers.setEvents(JsonCallbackEvents.disableButtonEvents(searchButton, new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				// if found 1 item, select
				listAllButton.setEnabled(true);
				searchGloballyButton.setEnabled(true);
				searchBox.getTextBox().setEnabled(true);
				ArrayList<RichMember> list = JsonUtils.jsoAsList(jso);
				if (list != null && list.size() == 1) {
					findMembers.getSelectionModel().setSelected(list.get(0), true);
				}
			}

			@Override
			public void onError(PerunError error) {
				listAllButton.setEnabled(true);
				searchGloballyButton.setEnabled(true);
				searchBox.getTextBox().setEnabled(true);
			}

			@Override
			public void onLoadingStart() {
				listAllButton.setEnabled(false);
				searchGloballyButton.setEnabled(false);
				disabled.setVisible(false);
				searchBox.getTextBox().setEnabled(false);
				addButton.setEnabled(false);
				inviteButton.setEnabled(false);
			}
		}));

		getAllMembers.setEvents(JsonCallbackEvents.mergeEvents(JsonCallbackEvents.disableButtonEvents(listAllButton, JsonCallbackEvents.disableCheckboxEvents(disabled)),
				new JsonCallbackEvents() {
					@Override
					public void onFinished(JavaScriptObject jso) {
						// pass data to table handling callback
						findMembers.onFinished(jso);
						((AjaxLoaderImage) table.getEmptyTableWidget()).setEmptyResultMessage("VO has no members.");
						searchButton.setEnabled(true);
						searchBox.getTextBox().setEnabled(true);
					}

					@Override
					public void onError(PerunError error) {
						// pass data to table handling callback
						findMembers.onError(error);
						searchButton.setEnabled(true);
						searchBox.getTextBox().setEnabled(true);
					}

					@Override
					public void onLoadingStart() {
						searchButton.setEnabled(false);
						disabled.setVisible(true);
						searchBox.getTextBox().setEnabled(false);
						addButton.setEnabled(false);
						inviteButton.setEnabled(false);
					}
				}));

		findCandidatesOrUsersToAddToVo.setEvents(JsonCallbackEvents.disableButtonEvents(searchGloballyButton, new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				// if found 1 item, select
				listAllButton.setEnabled(true);
				searchButton.setEnabled(true);
				searchBox.getTextBox().setEnabled(true);
				ArrayList<Candidate> list = JsonUtils.jsoAsList(jso);
				if (list != null && list.size() == 1) {
					findCandidatesOrUsersToAddToVo.setSelected(list.get(0));
				}
			}

			@Override
			public void onError(PerunError error) {
				listAllButton.setEnabled(true);
				searchButton.setEnabled(true);
				searchBox.getTextBox().setEnabled(true);
			}

			@Override
			public void onLoadingStart() {
				disabled.setVisible(false);
				listAllButton.setEnabled(false);
				searchButton.setEnabled(false);
				searchBox.getTextBox().setEnabled(false);
				addButton.setEnabled(false);
				inviteButton.setEnabled(false);
			}
		}));

		// DISABLED CHECKBOX
		disabled.setTitle(WidgetTranslation.INSTANCE.showDisabledMembersTitle());
		disabled.setVisible(false);

		// checkbox click handler
		disabled.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				getAllMembers.excludeDisabled(!disabled.getValue());
				findMembers.clearTable();
				getAllMembers.retrieveData();
			}
		});

		listAllButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				search = false;
				searchCandidates = false;
				searchString = "";
				searchBox.getTextBox().setText("");
				findMembers.clearTable();
				findCandidatesOrUsersToAddToVo.clearTable();
				getAllMembers.retrieveData();
				// remove previous table
				firstTabPanel.getWidget(2).removeFromParent();
				firstTabPanel.add(sp);
				UiElements.runResizeCommands(tab);
			}
		});

		// click handler
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				if (searchCandidates) {

					// searched users or candidates

					Candidate candidateToBeAdded = findCandidatesOrUsersToAddToVo.getSelected();
					if (candidateToBeAdded == null) {
						UiElements.cantSaveEmptyListDialogBox(null);
					} else {
						if (candidateToBeAdded.getObjectType().equalsIgnoreCase("Candidate")) {

							CreateMember request = new CreateMember(JsonCallbackEvents.disableButtonEvents(addButton, new JsonCallbackEvents() {
								private Candidate saveSelected;
								@Override
								public void onFinished(JavaScriptObject jso) {
									// put names to already added
									if (saveSelected != null) {
										GeneralObject go = saveSelected.cast();
										alreadyAddedList.add(go);
									}
									findCandidatesOrUsersToAddToVo.clearTableSelectedSet();
									rebuildAlreadyAddedWidget();
									// clear search
									searchBox.getTextBox().setText("");
								}
								@Override
								public void onLoadingStart() {
									saveSelected = findCandidatesOrUsersToAddToVo.getSelected();
								}
							}));
							request.createMember(group.getVoId(), group, candidateToBeAdded);

						} else {

							CreateMember request = new CreateMember(JsonCallbackEvents.disableButtonEvents(addButton, new JsonCallbackEvents(){
								private User saveSelected;
								@Override
								public void onFinished(JavaScriptObject jso) {
									// put names to already added
									if (saveSelected != null) {
										GeneralObject go = saveSelected.cast();
										alreadyAddedList.add(go);
										findCandidatesOrUsersToAddToVo.clearTableSelectedSet();
										rebuildAlreadyAddedWidget();
										// clear search
										searchBox.getTextBox().setText("");
									}
								}
								@Override
								public void onLoadingStart(){
									Candidate cand = findCandidatesOrUsersToAddToVo.getSelected();
									saveSelected = cand.cast();
								}
							}));
							User user = candidateToBeAdded.cast();
							request.createMember(group.getVoId(), group, user);

						}

					}


				} else {

					// searched members / all members
					final ArrayList<RichMember> membersToAdd = findMembers.getTableSelectedList();
					if (UiElements.cantSaveEmptyListDialogBox(membersToAdd)) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i = 0; i < membersToAdd.size(); i++) {
							final int n = i;
							AddMember request = new AddMember(JsonCallbackEvents.disableButtonEvents(addButton, new JsonCallbackEvents() {
								private RichMember saveSelected;
								@Override
								public void onFinished(JavaScriptObject jso) {
									// unselect added person
									findMembers.getSelectionModel().setSelected(saveSelected, false);
									// put names to already added
									GeneralObject go = saveSelected.cast();
									alreadyAddedList.add(go);
									rebuildAlreadyAddedWidget();
									// clear search
									searchBox.getTextBox().setText("");
								}
								@Override
								public void onLoadingStart() {
									saveSelected = membersToAdd.get(n);
								}
							}));
							request.addMemberToGroup(group, membersToAdd.get(i));
						}
					}

				}
			}
		});

		tabMenu.addWidget(searchGloballyButton);
		tabMenu.addWidget(listAllButton);
		tabMenu.addWidget(addButton);
		tabMenu.addWidget(inviteButton);

		inviteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				if (searchCandidates) {

					// we expect, that candidate is always single
					Candidate candid = findCandidatesOrUsersToAddToVo.getSelected();
					if (candid != null) {

						if (candid.getObjectType().equalsIgnoreCase("Candidate")) {
							SendInvitation invite = new SendInvitation(group.getVoId(), groupId);
							invite.setEvents(JsonCallbackEvents.disableButtonEvents(inviteButton, new JsonCallbackEvents() {
								@Override
								public void onFinished(JavaScriptObject jso) {
									findCandidatesOrUsersToAddToVo.clearTableSelectedSet();
								}
							}));
							invite.inviteUser(candid);
						} else {
							SendInvitation invite = new SendInvitation(group.getVoId(), groupId);
							invite.setEvents(JsonCallbackEvents.disableButtonEvents(inviteButton, new JsonCallbackEvents() {
								@Override
								public void onFinished(JavaScriptObject jso) {
									findCandidatesOrUsersToAddToVo.clearTableSelectedSet();
								}
							}));
							User user = candid.cast();
							invite.inviteUser(user);
						}
					}

				} else {

					// members / all members

					SendInvitation invite = new SendInvitation(group.getVoId(), groupId);
					ArrayList<RichMember> usrs = findMembers.getTableSelectedList();
					for (int i = 0; i < usrs.size(); i++) {
						if (i == usrs.size() - 1) {
							invite.setEvents(JsonCallbackEvents.disableButtonEvents(inviteButton, new JsonCallbackEvents() {
								@Override
								public void onFinished(JavaScriptObject jso) {
									findMembers.clearTableSelectedSet();
								}
							}));
						} else {
							invite.setEvents(JsonCallbackEvents.disableButtonEvents(inviteButton));
						}
						invite.inviteUser(usrs.get(i).getUser());
					}

				}
			}
		});

		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				// with refresh if somebody was added
				session.getTabManager().closeTab(tab, !alreadyAddedList.isEmpty());
			}
		}));

		tabMenu.addWidget(disabled);

		rebuildAlreadyAddedWidget();
		firstTabPanel.add(alreadyAdded);

		addButton.setEnabled(false);
		JsonUtils.addTableManagedButton(findMembers, table, addButton);
		inviteButton.setEnabled(false);
		JsonUtils.addTableManagedButton(findMembers, table, inviteButton);
		JsonUtils.addTableManagedButton(findCandidatesOrUsersToAddToVo, candidatesTable, addButton);
		JsonUtils.addTableManagedButton(findCandidatesOrUsersToAddToVo, candidatesTable, inviteButton);

		table.addStyleName("perun-table");
		sp.setWidget(table);
		sp.addStyleName("perun-tableScrollPanel");

		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

		candidatesTable.addStyleName("perun-table");
		sp2.setWidget(candidatesTable);
		sp2.addStyleName("perun-tableScrollPanel");

		session.getUiElements().resizeSmallTabPanel(sp2, 350, this);

		// if not empty - start searching
		if (search) {
			findMembers.searchFor(searchString);
			firstTabPanel.add(sp);
		} else if (searchCandidates) {
			findCandidatesOrUsersToAddToVo.searchFor(searchString);
			firstTabPanel.add(sp2);
		} else {
			getAllMembers.excludeDisabled(!disabled.getValue());
			getAllMembers.retrieveData();
			firstTabPanel.add(sp);
		}

		this.contentWidget.setWidget(firstTabPanel);
		return getWidget();

	}

	/**
	 * Rebuild already added widget based on already added members
	 */
	private void rebuildAlreadyAddedWidget() {

		alreadyAdded.setStyleName("alreadyAdded");
		alreadyAdded.setVisible(!alreadyAddedList.isEmpty());
		alreadyAdded.setWidget(new HTML("<strong>Already added: </strong>"));
		for (int i = 0; i < alreadyAddedList.size(); i++) {
			if (alreadyAddedList.get(i).getObjectType().equals("Candidate")) {
				Candidate c = alreadyAddedList.get(i).cast();
				alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML()+ ((i!=0) ? ", " : "") + c.getFullName());
			} else if (alreadyAddedList.get(i).getObjectType().equals("User") || alreadyAddedList.get(i).getObjectType().equals("RichUser")) {
				User u = alreadyAddedList.get(i).cast();
				alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML() + ((i != 0) ? ", " : "") + u.getFullName());
			} else {
				RichMember m = alreadyAddedList.get(i).cast();
				alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML() + ((i != 0) ? ", " : "") + m.getUser().getFullName());
			}
		}
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
		final int prime = 1429;
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
		AddMemberToGroupTabItem other = (AddMemberToGroupTabItem) obj;
		if (groupId != other.groupId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		if (group != null) {
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(group.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "add-to-grp";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return MembersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?grp=" + groupId;
	}

	static public AddMemberToGroupTabItem load(Map<String, String> parameters) {
		int gid = Integer.parseInt(parameters.get("grp"));
		return new AddMemberToGroupTabItem(gid);
	}

}