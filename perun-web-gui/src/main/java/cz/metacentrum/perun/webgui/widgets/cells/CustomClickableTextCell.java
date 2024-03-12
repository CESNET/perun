package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;

/**
 * Custom GWT cell, which is clickable and looks like an anchor.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CustomClickableTextCell extends ClickableTextCell {
  private String style;

  /**
   * Creates a new Clickable text cell
   */
  public CustomClickableTextCell() {
    super();
    style = "customClickableTextCell";
  }

  /**
   * Renders the widget.
   */
  @Override
  protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.appendHtmlConstant("<div class=\"" + style + "\">");
      sb.appendHtmlConstant(value.asString().replaceAll("\n", "<br>"));
      sb.appendHtmlConstant("</div>");
    }
  }

  /**
   * Adds a class to the style
   *
   * @param style
   */
  public void addStyleName(String style) {
    this.style += " " + style;
  }


}
