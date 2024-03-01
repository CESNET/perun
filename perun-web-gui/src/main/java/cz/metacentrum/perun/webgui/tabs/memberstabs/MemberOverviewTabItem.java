package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetListOfAttributes;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.MembershipExpirationWidget;
import cz.metacentrum.perun.webgui.widgets.PerunStatusWidget;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays members overview
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MemberOverviewTabItem implements TabItem {

  private RichMember member;
  private int memberId;
  private PerunWebSession session = PerunWebSession.getInstance();
  private SimplePanel contentWidget = new SimplePanel();
  private Label titleWidget = new Label("Loading member details");
  private int groupId = 0;
  private TabItem tabItem = this;

  /**
   * Constructor
   *
   * @param member RichMember object, typically from table
   */
  public MemberOverviewTabItem(RichMember member, int groupId) {
    this.member = member;
    this.memberId = member.getId();
    this.groupId = groupId;
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

    this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(member.getUser().getFullNameWithTitles().trim()));

    // main widget panel
    ScrollPanel vp = new ScrollPanel();
    vp.setSize("100%", "100%");

    VerticalPanel innerVp = new VerticalPanel();
    innerVp.setSize("100%", "100%");
    vp.add(innerVp);

    TabMenu menu = new TabMenu();
    innerVp.add(menu);
    innerVp.setCellHeight(menu, "30px");

    menu.addWidget(UiElements.getRefreshButton(this));

    session.getUiElements().resizeSmallTabPanel(vp, 400);

    FlexTable layout = new FlexTable();
    layout.setSize("100%", "100%");

    innerVp.add(layout);

    layout.setHTML(0, 0, "<p>Personal:");
    layout.setHTML(0, 1, "<p>Membership:");
    layout.getFlexCellFormatter().setWidth(0, 0, "50%");
    layout.getFlexCellFormatter().setWidth(0, 1, "50%");

    layout.getFlexCellFormatter().setStyleName(0, 0, "subsection-heading");
    layout.getFlexCellFormatter().setStyleName(0, 1, "subsection-heading");

    // if attribute not set
    final String notSet = "<i>N/A</i>";

    final FlexTable personalLayout = new FlexTable();
    layout.setWidget(1, 0, personalLayout);
    personalLayout.setStyleName("inputFormFlexTableDark");

    personalLayout.setHTML(0, 0, "Organization:");
    personalLayout.setHTML(1, 0, "Workplace:");
    personalLayout.setHTML(2, 0, "Research group:");
    personalLayout.setHTML(3, 0, "Preferred mail:");
    personalLayout.setHTML(4, 0, "Mail:");
    personalLayout.setHTML(5, 0, "Phone:");
    personalLayout.setHTML(6, 0, "Address:");
    personalLayout.setHTML(7, 0, "Preferred language:");
    personalLayout.setHTML(8, 0, "LoA:");

    // one empty cell to create empty column
    personalLayout.setHTML(0, 1, "&nbsp;");
    personalLayout.getFlexCellFormatter().setWidth(0, 1, "70%");

    // style personal table
    for (int i = 0; i < personalLayout.getRowCount(); i++) {
      personalLayout.getFlexCellFormatter().addStyleName(i, 0, "itemName");
    }

    // Membership table
    final FlexTable memberLayout = new FlexTable();
    layout.setWidget(1, 1, memberLayout);
    layout.getFlexCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
    memberLayout.setStyleName("inputFormFlexTableDark");

    memberLayout.setHTML(0, 0, "Status:");
    final PerunStatusWidget<RichMember> statusWidget;
    if (session.isVoAdmin(member.getVoId())) {
      JsonCallbackEvents event = new JsonCallbackEvents() {
        @Override
        public void onFinished(JavaScriptObject jso) {
          // UPDATE OBJECT
          if (jso != null) {
            // fixme - since we pass this event to more tabs and update expiration (set attributes)
            //  passed object might not be relevant for this action
            Member m = jso.cast();
            member.setStatus(m.getStatus());
          }
        }
      };
      statusWidget = new PerunStatusWidget<RichMember>(member, member.getUser().getFullName(), event, tabItem);
    } else {
      statusWidget = new PerunStatusWidget<RichMember>(member, member.getUser().getFullName(), null, tabItem);
    }
    memberLayout.setWidget(0, 1, statusWidget);
    memberLayout.getFlexCellFormatter().setRowSpan(0, 0, 2);

    if (member.getStatus().equalsIgnoreCase("VALID")) {
      memberLayout.setHTML(1, 0, "Member is properly configured and have access on provided resources.");
    } else if (member.getStatus().equalsIgnoreCase("INVALID")) {
      memberLayout.setHTML(1, 0,
          "Member have configuration error and DON'T have access on provided resources. You can check what is wrong by changing member's status to VALID. If possible, procedure will configure all necessary settings by itself.");
    } else if (member.getStatus().equalsIgnoreCase("EXPIRED")) {
      memberLayout.setHTML(1, 0, "Member didn't extend membership and DON'T have access on provided resources.");
    } else if (member.getStatus().equalsIgnoreCase("DISABLED")) {
      memberLayout.setHTML(1, 0,
          "Member didn't extend membership long time ago or was manually disabled and DON'T have access on provided resources.");
    }
    memberLayout.getFlexCellFormatter().setStyleName(1, 0, "inputFormInlineComment");

    memberLayout.setHTML(2, 0, "Expiration:");
    memberLayout.setHTML(3, 0, "Member type:");
    if (member.getUser().isServiceUser()) {
      memberLayout.setHTML(3, 1, "Service");
    } else if (member.getUser().isSponsoredUser()) {
      memberLayout.setHTML(3, 1, "Sponsored");
    } else {
      memberLayout.setHTML(3, 1, "Person");
    }
    memberLayout.setHTML(4, 0, "Sponsored by:");
    memberLayout.setHTML(5, 0, "Member ID:");
    memberLayout.setHTML(5, 1, member.getId() + "");
    memberLayout.setHTML(6, 0, "User ID:");
    memberLayout.setHTML(6, 1, member.getUser().getId() + "");

    if (session.isVoAdmin(member.getVoId())) {

      CustomButton resetButton =
          new CustomButton("Send password reset request…", "", SmallIcons.INSTANCE.keyIcon(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              session.getTabManager().addTabToCurrentTab(new SendPasswordResetRequestTabItem(member));
            }
          });

      memberLayout.setHTML(7, 0, "Password reset");
      memberLayout.setWidget(7, 1, resetButton);

    }

    // style member table
    for (int i = 0; i < memberLayout.getRowCount(); i++) {
      if (i != 1) {
        memberLayout.getFlexCellFormatter().addStyleName(i, 0, "itemName");
      }
    }

    // attributes to load
    ArrayList<String> attrs = new ArrayList<String>();
    // TODO - WE WILL HAVE THIS CONFIGURABLE SOON
    attrs.add("urn:perun:user:attribute-def:def:organization");
    attrs.add("urn:perun:user:attribute-def:def:workplace");
    attrs.add("urn:perun:user:attribute-def:opt:researchGroup");
    attrs.add("urn:perun:member:attribute-def:def:mail");
    attrs.add("urn:perun:user:attribute-def:def:preferredMail");
    attrs.add("urn:perun:user:attribute-def:def:phone");
    attrs.add("urn:perun:user:attribute-def:def:address");
    attrs.add("urn:perun:user:attribute-def:def:preferredLanguage");
    attrs.add("urn:perun:user:attribute-def:virt:loa");
    attrs.add("urn:perun:member:attribute-def:def:membershipExpiration");
    attrs.add("urn:perun:member:attribute-def:def:sponzoredMember");

    HashMap<String, Integer> ids = new HashMap<String, Integer>();
    ids.put("member", memberId);
    ids.put("workWithUserAttributes", 1);

    GetListOfAttributes attrsCall = new GetListOfAttributes();

    attrsCall.setEvents(new JsonCallbackEvents() {
      @Override
      public void onFinished(JavaScriptObject jso) {

        ArrayList<Attribute> list = JsonUtils.jsoAsList(jso);
        Map<String, Attribute> attrMap = new HashMap<>();
        for (Attribute a : list) {
          attrMap.put(a.getName(), a);
        }

        setValueForLayout(personalLayout, 0, 1, attrMap.get("urn:perun:user:attribute-def:def:organization"), null);
        setValueForLayout(personalLayout, 1, 1, attrMap.get("urn:perun:user:attribute-def:def:workplace"), null);
        setValueForLayout(personalLayout, 2, 1, attrMap.get("urn:perun:user:attribute-def:opt:researchGroup"), null);
        setValueForLayout(personalLayout, 3, 1, attrMap.get("urn:perun:user:attribute-def:def:preferredMail"), null);
        setValueForLayout(personalLayout, 4, 1, attrMap.get("urn:perun:member:attribute-def:def:mail"), null);
        setValueForLayout(personalLayout, 5, 1, attrMap.get("urn:perun:user:attribute-def:def:phone"), null);
        setValueForLayout(personalLayout, 6, 1, attrMap.get("urn:perun:user:attribute-def:def:address"), null);
        setValueForLayout(personalLayout, 7, 1, attrMap.get("urn:perun:user:attribute-def:def:preferredLanguage"),
            null);

        String appendLoa = null;
        if (attrMap.get("urn:perun:user:attribute-def:virt:loa") != null) {
          Attribute attr = attrMap.get("urn:perun:user:attribute-def:virt:loa");
          if ("0".equals(attr.getValue())) {
            appendLoa = " (not verified = default)";
          } else if ("1".equals(attr.getValue())) {
            appendLoa = " (verified email)";
          } else if ("2".equals(attr.getValue())) {
            appendLoa = " (verified identity)";
          } else if ("3".equals(attr.getValue())) {
            appendLoa = " (verified identity, strict password strength)";
          }
        }
        setValueForLayout(personalLayout, 8, 1, attrMap.get("urn:perun:user:attribute-def:virt:loa"), appendLoa);

        setValueForLayout(memberLayout, 4, 1, attrMap.get("urn:perun:member:attribute-def:def:sponzoredMember"),
            " (ID of RT ticket with explanation)");
        if (attrMap.containsKey("urn:perun:member:attribute-def:def:membershipExpiration")) {
          // set attribute inside member
          member.setAttribute(attrMap.get("urn:perun:member:attribute-def:def:membershipExpiration"));
          memberLayout.setWidget(2, 1, new MembershipExpirationWidget(member, tabItem));
        }

      }

      private void setValueForLayout(FlexTable layout, int row, int column, Attribute attribute, String append) {
        if (attribute != null) {
          String value =
              SafeHtmlUtils.fromString((attribute.getValue() != null) ? attribute.getValue() : "").asString();
          if (!"null".equals(value)) {
            layout.setHTML(row, column, (append != null) ? (value + append) : value);
            return;
          }
        }
        layout.setHTML(row, column, notSet);
      }

      @Override
      public void onError(PerunError error) {
        String text = "<span style=\"color: red\">Error while loading";
        for (int i = 0; i < personalLayout.getRowCount(); i++) {
          personalLayout.setHTML(i, 1, text);
        }
        memberLayout.setHTML(2, 1, text);
        memberLayout.setHTML(4, 1, text);
      }

      @Override
      public void onLoadingStart() {
        for (int i = 0; i < personalLayout.getRowCount(); i++) {
          personalLayout.setWidget(i, 1, new Image(AjaxLoaderImage.SMALL_IMAGE_URL));
        }
        memberLayout.setWidget(2, 1, new Image(AjaxLoaderImage.SMALL_IMAGE_URL));
        memberLayout.setWidget(4, 1, new Image(AjaxLoaderImage.SMALL_IMAGE_URL));
      }
    });

    attrsCall.getListOfAttributes(ids, attrs);

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
    return SmallIcons.INSTANCE.userGreenIcon();
  }

  @Override
  public int hashCode() {
    final int prime = 1453;
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
    MemberOverviewTabItem other = (MemberOverviewTabItem) obj;
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

    if (session.isVoAdmin(member.getVoId()) || session.isVoObserver(member.getVoId()) ||
        session.isGroupAdmin(groupId)) {
      return true;
    } else {
      return false;
    }

  }

}
