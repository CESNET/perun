package cz.metacentrum.perun.webgui.client.resources;

/**
 * Event called when user clicks on search in tab menu.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public abstract class PerunSearchEvent {

  /**
   * Text to search for.
   * The text is always not-empty.
   *
   * @param text
   */
  public abstract void searchFor(String text);
}
