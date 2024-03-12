package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.AddMember;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllRichGroups;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.RichGroup;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import java.util.ArrayList;

/**
 * WIZARD "ADD SELECTED MEMBER TO GROUP(S)"
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MemberAddToGroupTabItem implements TabItem {

  private RichMember member;
  private int memberId;
  private PerunWebSession session = PerunWebSession.getInstance();
  private SimplePanel contentWidget = new SimplePanel();
  private Label titleWidget = new Label("Loading member details");

  private ArrayList<Group> alreadyAddedList = new ArrayList<Group>();
  private SimplePanel alreadyAdded = new SimplePanel();

  /**
   * Constructor
   *
   * @param member RichMember object, typically from table
   */
  public MemberAddToGroupTabItem(RichMember member) {
    this.member = member;
    this.memberId = member.getId();
  }

  public boolean isPrepared() {
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

    this.titleWidget.setText("Add to group(s)");

    // main widget panel
    VerticalPanel vp = new VerticalPanel();
    vp.setSize("100%", "100%");

    TabMenu menu = new TabMenu();
    vp.add(menu);
    vp.setCellHeight(menu, "30px");

    ArrayList<String> attrNames = new ArrayList<>();
    attrNames.add("urn:perun:group:attribute-def:def:synchronizationEnabled");
    attrNames.add("urn:perun:group:attribute-def:def:synchronizationInterval");
    attrNames.add("urn:perun:group:attribute-def:def:lastSynchronizationState");
    attrNames.add("urn:perun:group:attribute-def:def:lastSuccessSynchronizationTimestamp");
    attrNames.add("urn:perun:group:attribute-def:def:lastSynchronizationTimestamp");
    attrNames.add("urn:perun:group:attribute-def:def:authoritativeGroup");
    attrNames.add("urn:perun:group:attribute-def:def:groupSynchronizationTimes");
    attrNames.add("urn:perun:group:attribute-def:def:startOfLastSuccessfulSynchronization");
    attrNames.add("urn:perun:group:attribute-def:def:startOfLastSynchronization");
    attrNames.add("urn:perun:group:attribute-def:def:blockManualMemberAdding");
    final GetAllRichGroups groups = new GetAllRichGroups(member.getVoId(), attrNames, true);

    // refresh
    menu.addWidget(UiElements.getRefreshButton(this));

    menu.addFilterWidget(new ExtendedSuggestBox(groups.getOracle()), new PerunSearchEvent() {
      @Override
      public void searchFor(String text) {
        groups.filterTable(text);
        if (groups.getList().size() == 1) {
          // select if filtered result is single group
          groups.getSelectionModel().setSelected(groups.getList().get(0), true);
        }
      }
    }, "Filter groups by name");

    final TabItem tab = this;

    final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, "Add member to selected group(s)");
    addButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        ArrayList<RichGroup> list = groups.getTableSelectedList();
        if (UiElements.cantSaveEmptyListDialogBox(list)) {
          // TODO - should have only one callback to core
          for (int i = 0; i < list.size(); i++) {
            final RichGroup g = list.get(i);
            AddMember request =
                new AddMember(JsonCallbackEvents.disableButtonEvents(addButton, new JsonCallbackEvents() {
                  @Override
                  public void onFinished(JavaScriptObject jso) {
                    groups.getSelectionModel().setSelected(g, false);
                    alreadyAddedList.add(g);
                    rebuildAlreadyAddedWidget();
                  }
                }));
            request.addMemberToGroup(g, member);
          }
        }
      }
    });
    menu.addWidget(addButton);
    addButton.setEnabled(false);

    menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        session.getTabManager().closeTab(tab, isRefreshParentOnClose());
      }
    }));

    CellTable<RichGroup> table = groups.getTable();

    JsonUtils.addTableManagedButton(groups, table, addButton);

    table.addStyleName("perun-table");
    ScrollPanel sp = new ScrollPanel(table);
    sp.addStyleName("perun-tableScrollPanel");
    session.getUiElements().resizePerunTable(sp, 350, this);

    rebuildAlreadyAddedWidget();
    vp.add(alreadyAdded);

    vp.add(sp);

    this.contentWidget.setWidget(vp);

    return getWidget();

  }

  /**
   * Rebuild already added widget based on already added members
   */
  private void rebuildAlreadyAddedWidget() {

    alreadyAdded.setStyleName("alreadyAdded");
    alreadyAdded.setVisible(!alreadyAddedList.isEmpty());
    alreadyAdded.setWidget(new HTML("<strong>Already added to groups: </strong>"));
    for (int i = 0; i < alreadyAddedList.size(); i++) {
      alreadyAdded.getWidget().getElement().setInnerHTML(
          alreadyAdded.getWidget().getElement().getInnerHTML() + ((i != 0) ? ", " : "") +
              SafeHtmlUtils.fromString(alreadyAddedList.get(i).getName()).asString());
    }

  }

  public Widget getWidget() {
    return this.contentWidget;
  }

  public Widget getTitle() {
    return this.titleWidget;
  }

  public ImageResource getIcon() {
    return SmallIcons.INSTANCE.userGreenIcon();
  }

  @Override
  public int hashCode() {
    final int prime = 1439;
    int result = 1;
    result = prime * result + memberId;
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
    MemberAddToGroupTabItem other = (MemberAddToGroupTabItem) obj;
    if (memberId != other.memberId) {
      return false;
    }
    return true;
  }

  public boolean multipleInstancesEnabled() {
    return false;
  }

  public void open() {
  }

  public boolean isAuthorized() {

    if (session.isVoAdmin(member.getVoId()) || session.isGroupAdmin()) {
      return true;
    } else {
      return false;
    }

  }

}
