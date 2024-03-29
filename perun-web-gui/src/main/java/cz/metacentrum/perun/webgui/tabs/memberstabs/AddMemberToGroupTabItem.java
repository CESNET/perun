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
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.AddMember;
import cz.metacentrum.perun.webgui.json.membersManager.CreateMember;
import cz.metacentrum.perun.webgui.json.membersManager.GetCompleteRichMembers;
import cz.metacentrum.perun.webgui.json.registrarManager.SendInvitation;
import cz.metacentrum.perun.webgui.json.vosManager.GetCompleteCandidates;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.MemberCandidate;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.MembersTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
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

  public static final String URL = "add-to-grp";
  ScrollPanel sp = new ScrollPanel();
  ScrollPanel sp2 = new ScrollPanel();
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

  static public AddMemberToGroupTabItem load(Map<String, String> parameters) {
    int gid = Integer.parseInt(parameters.get("grp"));
    return new AddMemberToGroupTabItem(gid);
  }

  public boolean isPrepared() {
    return !(group == null);
  }

  @Override
  public boolean isRefreshParentOnClose() {
    return !alreadyAddedList.isEmpty();
  }

  @Override
  public void onClose() {

  }

  public Widget draw() {

    titleWidget.setText("Add member(s)");

    // MAIN PANEL
    final VerticalPanel firstTabPanel = new VerticalPanel();
    firstTabPanel.setSize("100%", "100%");

    boolean isMembersGroup = group.isCoreGroup();

    // if members or admins group, hide
    if (isMembersGroup) {

      firstTabPanel.add(new HTML("<p>Group \"" + group.getName() +
          "\" can't have members managed from Group admin. Please use VO admin section.</p>"));
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
    final GetCompleteRichMembers getAllMembers;         // for both
    final GetCompleteCandidates findCandidates;         // for VO/group admin differs search

    // elements handled by callback events
    final CustomButton searchButton =
        new CustomButton("Search", ButtonTranslation.INSTANCE.searchMemberInVo(), SmallIcons.INSTANCE.findIcon());
    final CustomButton listAllButton =
        new CustomButton("List all VO members", ButtonTranslation.INSTANCE.listAllMembersInVo(),
            SmallIcons.INSTANCE.userGreenIcon());

    final CheckBox disabled = new CheckBox(WidgetTranslation.INSTANCE.showDisabledMembers());

    // search through whole VO
    getAllMembers = new GetCompleteRichMembers(PerunEntity.VIRTUAL_ORGANIZATION, group.getVoId(), null);
    findCandidates = new GetCompleteCandidates(group.getVoId(), group.getId(), "", null);

    final CellTable<MemberCandidate> candidatesTable = findCandidates.getEmptyTable();
    final CellTable<RichMember> table = getAllMembers.getEmptyTable(new FieldUpdater<RichMember, RichMember>() {
      @Override
      public void update(int i, RichMember o, RichMember o2) {
        session.getTabManager().addTab(new MemberDetailTabItem(o.getId(), groupId));
      }
    });

    final CustomButton inviteButton = new CustomButton("Invite selected", SmallIcons.INSTANCE.emailIcon());
    final CustomButton addButton =
        TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedMemberToGroup());

    final ExtendedTextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
      public void searchFor(String text) {
        searchString = text;
        findCandidates.searchFor(searchString);
        search = true;
        // remove previous table
        firstTabPanel.getWidget(2).removeFromParent();
        firstTabPanel.add(sp2);
        UiElements.runResizeCommands(tab);
      }
    }, searchButton);
    searchBox.getTextBox().setText(searchString);

    // bind text box also to search globally
    searchBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
      public void onKeyUp(KeyUpEvent event) {
        if (!searchBox.getTextBox().getText().trim().isEmpty()) {
          searchButton.setEnabled(true);
          // do not trigger search on both !!
        } else {
          searchButton.setEnabled(false);
        }
      }
    });
    searchBox.getTextBox().addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        // fake some meaningless KeyUpEvent
        DomEvent.fireNativeEvent(Document.get().createKeyUpEvent(false, false, false, false, KeyCodes.KEY_DOWN),
            searchBox.getTextBox());
      }
    });
    searchBox.getTextBox().addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        // fake some meaningless KeyUpEvent
        DomEvent.fireNativeEvent(Document.get().createKeyUpEvent(false, false, false, false, KeyCodes.KEY_DOWN),
            searchBox.getTextBox());
      }
    });

    // button click triggers action
    searchButton.setEnabled(false);

    getAllMembers.setEvents(JsonCallbackEvents.mergeEvents(
        JsonCallbackEvents.disableButtonEvents(listAllButton, JsonCallbackEvents.disableCheckboxEvents(disabled)),
        new JsonCallbackEvents() {
          @Override
          public void onFinished(JavaScriptObject jso) {
            // pass data to table handling callback
            searchButton.setEnabled(true);
            searchBox.getTextBox().setEnabled(true);
          }

          @Override
          public void onError(PerunError error) {
            // pass data to table handling callback
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

    findCandidates.setEvents(JsonCallbackEvents.disableButtonEvents(searchButton, new JsonCallbackEvents() {
      @Override
      public void onFinished(JavaScriptObject jso) {
        // if found 1 item, select
        listAllButton.setEnabled(true);
        searchButton.setEnabled(true);
        searchBox.getTextBox().setEnabled(true);
        if (findCandidates.getList().size() == 1) {
          if (findCandidates.getList().get(0).getMember() == null ||
              findCandidates.getList().get(0).getMember().getSourceGroupId() == 0) {
            // select first if selectable
            findCandidates.setSelected(findCandidates.getList().get(0));
          }
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
        getAllMembers.clearTable();
        getAllMembers.retrieveData();
      }
    });

    listAllButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        search = false;
        searchString = "";
        searchBox.getTextBox().setText("");
        getAllMembers.clearTable();
        findCandidates.clearTable();
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

        if (search) {

          // searched users or candidates

          MemberCandidate candidateToBeAdded = findCandidates.getSelected();
          if (candidateToBeAdded == null) {
            UiElements.cantSaveEmptyListDialogBox(null);
          } else {

            if (candidateToBeAdded.getMember() != null) {

              // person is already member of VO

              AddMember request =
                  new AddMember(JsonCallbackEvents.disableButtonEvents(addButton, new JsonCallbackEvents() {
                    private MemberCandidate saveSelected;

                    @Override
                    public void onFinished(JavaScriptObject jso) {
                      // put names to already added
                      if (saveSelected != null) {
                        GeneralObject go = saveSelected.cast();
                        alreadyAddedList.add(go);
                      }
                      findCandidates.clearTableSelectedSet();
                      rebuildAlreadyAddedWidget();
                      // clear search
                      searchBox.getTextBox().setText("");
                    }

                    @Override
                    public void onLoadingStart() {
                      saveSelected = findCandidates.getSelected();
                    }
                  }));
              // reconstruct rich member, since call methods requires it
              RichMember mem = candidateToBeAdded.getMember().cast();
              mem.setUser(candidateToBeAdded.getRichUser());
              mem.setObjectType("RichMember");
              request.addMemberToGroup(group, mem);

            } else if (candidateToBeAdded.getCandidate() != null) {

              // person is not in Perun or candidate was found for existing user (not yet member of VO)

              CreateMember request =
                  new CreateMember(JsonCallbackEvents.disableButtonEvents(addButton, new JsonCallbackEvents() {
                    private MemberCandidate saveSelected;

                    @Override
                    public void onFinished(JavaScriptObject jso) {
                      // put names to already added
                      if (saveSelected != null) {
                        GeneralObject go = saveSelected.cast();
                        alreadyAddedList.add(go);
                      }
                      findCandidates.clearTableSelectedSet();
                      rebuildAlreadyAddedWidget();
                      // clear search
                      searchBox.getTextBox().setText("");
                    }

                    @Override
                    public void onLoadingStart() {
                      saveSelected = findCandidates.getSelected();
                    }
                  }));
              request.createMember(group.getVoId(), group, candidateToBeAdded.getCandidate());

            } else {

              // person is already user in Perun, no candidate was found

              CreateMember request =
                  new CreateMember(JsonCallbackEvents.disableButtonEvents(addButton, new JsonCallbackEvents() {
                    private User saveSelected;

                    @Override
                    public void onFinished(JavaScriptObject jso) {
                      // put names to already added
                      if (saveSelected != null) {
                        GeneralObject go = saveSelected.cast();
                        alreadyAddedList.add(go);
                        findCandidates.clearTableSelectedSet();
                        rebuildAlreadyAddedWidget();
                        // clear search
                        searchBox.getTextBox().setText("");
                      }
                    }

                    @Override
                    public void onLoadingStart() {
                      MemberCandidate cand = findCandidates.getSelected();
                      saveSelected = cand.getRichUser();
                    }
                  }));
              User user = candidateToBeAdded.getRichUser();
              request.createMember(group.getVoId(), group, user);

            }

          }

        } else {

          // searched members / all members
          final ArrayList<RichMember> membersToAdd = getAllMembers.getTableSelectedList();
          if (UiElements.cantSaveEmptyListDialogBox(membersToAdd)) {
            // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
            for (int i = 0; i < membersToAdd.size(); i++) {
              final int n = i;
              AddMember request =
                  new AddMember(JsonCallbackEvents.disableButtonEvents(addButton, new JsonCallbackEvents() {
                    private RichMember saveSelected;

                    @Override
                    public void onFinished(JavaScriptObject jso) {
                      // unselect added person
                      getAllMembers.getSelectionModel().setSelected(saveSelected, false);
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

    tabMenu.addWidget(listAllButton);
    tabMenu.addWidget(addButton);
    tabMenu.addWidget(inviteButton);

    inviteButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {

        if (search) {

          // we expect, that candidate is always single
          MemberCandidate candid = findCandidates.getSelected();
          if (candid != null) {

            if (candid.getCandidate() != null) {
              SendInvitation invite = new SendInvitation(group.getVoId(), groupId);
              invite.setEvents(JsonCallbackEvents.disableButtonEvents(inviteButton, new JsonCallbackEvents() {
                @Override
                public void onFinished(JavaScriptObject jso) {
                  findCandidates.clearTableSelectedSet();
                }
              }));
              invite.inviteUser(candid.getCandidate());
            } else {
              SendInvitation invite = new SendInvitation(group.getVoId(), groupId);
              invite.setEvents(JsonCallbackEvents.disableButtonEvents(inviteButton, new JsonCallbackEvents() {
                @Override
                public void onFinished(JavaScriptObject jso) {
                  findCandidates.clearTableSelectedSet();
                }
              }));
              User user = candid.getRichUser();
              invite.inviteUser(user);
            }
          }

        } else {

          // all members

          SendInvitation invite = new SendInvitation(group.getVoId(), groupId);
          ArrayList<RichMember> usrs = getAllMembers.getTableSelectedList();
          for (int i = 0; i < usrs.size(); i++) {
            if (i == usrs.size() - 1) {
              invite.setEvents(JsonCallbackEvents.disableButtonEvents(inviteButton, new JsonCallbackEvents() {
                @Override
                public void onFinished(JavaScriptObject jso) {
                  getAllMembers.clearTableSelectedSet();
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
        session.getTabManager().closeTab(tab, isRefreshParentOnClose());
      }
    }));

    tabMenu.addWidget(disabled);

    rebuildAlreadyAddedWidget();
    firstTabPanel.add(alreadyAdded);

    addButton.setEnabled(false);
    inviteButton.setEnabled(false);
    JsonUtils.addTableManagedButton(getAllMembers, table, addButton);
    JsonUtils.addTableManagedButton(getAllMembers, table, inviteButton);
    JsonUtils.addTableManagedButton(findCandidates, candidatesTable, addButton);
    JsonUtils.addTableManagedButton(findCandidates, candidatesTable, inviteButton);

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
      findCandidates.searchFor(searchString);
      firstTabPanel.add(sp2);
    } else {
      getAllMembers.excludeDisabled(!disabled.getValue());
      getAllMembers.clearTable();
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
      if (alreadyAddedList.get(i).getObjectType().equals("MemberCandidate")) {
        MemberCandidate c = alreadyAddedList.get(i).cast();
        if (c.getCandidate() != null) {
          alreadyAdded.getWidget().getElement().setInnerHTML(
              alreadyAdded.getWidget().getElement().getInnerHTML() + ((i != 0) ? ", " : "") +
                  SafeHtmlUtils.fromString(c.getCandidate().getFullName()).asString());
        } else {
          alreadyAdded.getWidget().getElement().setInnerHTML(
              alreadyAdded.getWidget().getElement().getInnerHTML() + ((i != 0) ? ", " : "") +
                  SafeHtmlUtils.fromString(c.getRichUser().getFullName()).asString());
        }
      } else if (alreadyAddedList.get(i).getObjectType().equals("User") ||
          alreadyAddedList.get(i).getObjectType().equals("RichUser")) {
        User u = alreadyAddedList.get(i).cast();
        alreadyAdded.getWidget().getElement().setInnerHTML(
            alreadyAdded.getWidget().getElement().getInnerHTML() + ((i != 0) ? ", " : "") +
                SafeHtmlUtils.fromString(u.getFullName()).asString());
      } else {
        RichMember m = alreadyAddedList.get(i).cast();
        alreadyAdded.getWidget().getElement().setInnerHTML(
            alreadyAdded.getWidget().getElement().getInnerHTML() + ((i != 0) ? ", " : "") +
                SafeHtmlUtils.fromString(m.getUser().getFullName()).asString());
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AddMemberToGroupTabItem other = (AddMemberToGroupTabItem) obj;
    if (groupId != other.groupId) {
      return false;
    }
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

  public String getUrl() {
    return URL;
  }

  public String getUrlWithParameters() {
    return MembersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?grp=" + groupId;
  }

}
