package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.tabs.registrartabs.AutoRegistrationGroupsTabItem;
import cz.metacentrum.perun.webgui.tabs.registrartabs.EditMailTabItem;
import cz.metacentrum.perun.webgui.tabs.registrartabs.MailsTabItem;

import java.util.Map;

/**
 * Pages, which are for registrar
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class RegistrarTabs {

  static public final String URL = "reg";
  private PerunWebSession session = PerunWebSession.getInstance();


  /**
   * Creates a new instance of pages
   */
  public RegistrarTabs() {
  }

  /**
   * Loads the page
   *
   * @return true on success, false otherwise
   */
  public boolean loadTab(String tab, Map<String, String> parameters) {

    if (tab == null) {
      return false;
    }
    // if active
    boolean open = ("1".equals(parameters.get("active")));


    // which page?
    if (tab.equals(EditMailTabItem.URL)) {
      session.getTabManager().addTab(EditMailTabItem.load(parameters), open);
      return true;
    }

    if (tab.equals(MailsTabItem.URL)) {
      session.getTabManager().addTab(MailsTabItem.load(parameters), open);
      return true;
    }

    if (tab.equals(AutoRegistrationGroupsTabItem.URL)) {
      session.getTabManager().addTab(AutoRegistrationGroupsTabItem.load(parameters), open);
      return true;
    }


    return false;

  }

}
