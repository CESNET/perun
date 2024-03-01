package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.tabs.attributestabs.AttributeDefinitionsTabItem;
import cz.metacentrum.perun.webgui.tabs.entitylessattributestabs.EntitylessAttributeEditKeyTabItem;

import java.util.Map;

/**
 * Attributes tabs
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class AttributesTabs {

  static public final String URL = "attr";
  private PerunWebSession session = PerunWebSession.getInstance();

  /**
   * Creates a new instance of pages
   */
  public AttributesTabs() {
  }

  /**
   * Loads the page
   *
   * @return true on success / false otherwise
   */
  public boolean loadTab(final String tab, final Map<String, String> parameters) {

    if (tab == null) {
      return false;
    }
    // if active
    boolean open = ("1".equals(parameters.get("active")));


    if (tab.equals(AttributeDefinitionsTabItem.URL)) {
      session.getTabManager().addTab(AttributeDefinitionsTabItem.load(parameters), open);
      return true;
    }

    if (tab.equals(EntitylessAttributeEditKeyTabItem.URL)) {
      session.getTabManager().addTab(EntitylessAttributeEditKeyTabItem.load(parameters), open);
      return true;
    }

    return false;


  }

}
