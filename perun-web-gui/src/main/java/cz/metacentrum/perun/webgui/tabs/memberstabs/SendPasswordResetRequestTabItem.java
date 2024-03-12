package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetLogins;
import cz.metacentrum.perun.webgui.json.membersManager.SendPasswordResetLinkEmail;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import cz.metacentrum.perun.webgui.widgets.TabPanelForTabItems;
import java.util.ArrayList;
import java.util.Map;

/**
 * Inner tab for sending password reset link email to member
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SendPasswordResetRequestTabItem implements TabItem {

  TabPanelForTabItems tabPanel;
  private RichMember member;
  private int memberId;
  private PerunWebSession session = PerunWebSession.getInstance();
  private SimplePanel contentWidget = new SimplePanel();
  private Label titleWidget = new Label("Loading member");
  private JsonCallbackEvents events = new JsonCallbackEvents();

  /**
   * Constructor
   *
   * @param member RichMember object, typically from table
   */
  public SendPasswordResetRequestTabItem(RichMember member) {
    this.member = member;
    this.memberId = member.getId();
    this.events = events;
    this.tabPanel = new TabPanelForTabItems(this);
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

    this.titleWidget.setText("Send password reset request");

    VerticalPanel vp = new VerticalPanel();
    vp.setSize("400px", "100%");

    final FlexTable layout = new FlexTable();
    layout.setSize("100%", "100%");
    layout.setStyleName("inputFormFlexTable");

    layout.setHTML(0, 0, "Select namespace:");
    layout.getFlexCellFormatter().setStyleName(0, 0, "itemName");

    layout.setHTML(1, 0, "Select language of email:");
    layout.getFlexCellFormatter().setStyleName(1, 0, "itemName");

    layout.setHTML(2, 0, "Select email attribute:");
    layout.getFlexCellFormatter().setStyleName(2, 0, "itemName");

    layout.setHTML(3, 0, "Login:");
    layout.getFlexCellFormatter().setStyleName(3, 0, "itemName");

    final ListBox namespaces = new ListBox();
    layout.setWidget(0, 1, namespaces);

    final ListBox languages = new ListBox();
    layout.setWidget(1, 1, languages);

    final ListBox emails = new ListBox();
    layout.setWidget(2, 1, emails);

    Map<String, String> urns = Utils.getResetPasswordEmails();
    for (Map.Entry<String, String> s : urns.entrySet()) {
      emails.addItem(s.getKey(), s.getValue());
    }

    // prefer native language in a list
    ArrayList<String> list = Utils.getNativeLanguage();
    if (!list.isEmpty()) {
      languages.addItem(list.get(1), list.get(0));
    }
    languages.addItem("English", "en");

    final CustomButton changeButton =
        new CustomButton("Send", "Send email with reset password link", SmallIcons.INSTANCE.emailIcon());

    GetLogins logins = new GetLogins(member.getUserId(), new JsonCallbackEvents() {
      @Override
      public void onFinished(JavaScriptObject jso) {
        namespaces.clear();
        ArrayList<Attribute> logins = JsonUtils.jsoAsList(jso);
        if (logins != null && !logins.isEmpty()) {
          changeButton.setEnabled(true);
          for (Attribute a : logins) {
            if (Utils.getSupportedPasswordNamespaces().contains(a.getFriendlyNameParameter())) {
              namespaces.addItem(a.getFriendlyNameParameter().toUpperCase(), a.getValue());
              layout.setHTML(3, 1, SafeHtmlUtils.fromString((a.getValue() != null) ? a.getValue() : "").asString());
            }
          }
        }
      }

      public void onLoadingStart() {
        changeButton.setEnabled(false);
        namespaces.clear();
        namespaces.addItem("Loading...");
      }

      public void onError(PerunError error) {
        namespaces.clear();
        namespaces.addItem("Error while loading");
      }
    });
    logins.retrieveData();

    namespaces.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        layout.setHTML(3, 1, namespaces.getValue(namespaces.getSelectedIndex()));
      }
    });


    TabMenu menu = new TabMenu();
    final TabItem tab = this;

    // by default false
    changeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        SendPasswordResetLinkEmail request =
            new SendPasswordResetLinkEmail(JsonCallbackEvents.closeTabDisableButtonEvents(changeButton, tab, true));
        request.sendEmail(member, namespaces.getItemText(namespaces.getSelectedIndex()).toLowerCase(),
            emails.getSelectedValue(), languages.getSelectedValue());
      }
    });
    menu.addWidget(changeButton);

    menu.addWidget(
        TabMenu.getPredefinedButton(ButtonType.CANCEL, ButtonTranslation.INSTANCE.cancelButton(), new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            session.getTabManager().closeTab(tab, isRefreshParentOnClose());
          }
        }));

    vp.add(layout);
    vp.add(menu);
    vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

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
    final int prime = 907;
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
    SendPasswordResetRequestTabItem other = (SendPasswordResetRequestTabItem) obj;
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

    if (session.isVoAdmin(member.getVoId())) {
      return true;
    } else {
      return false;
    }

  }

}
