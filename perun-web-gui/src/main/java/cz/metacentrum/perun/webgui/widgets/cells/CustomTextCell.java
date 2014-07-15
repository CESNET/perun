package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
/**
 * Custom GWT cell, which display simple text.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CustomTextCell extends TextCell
{
	private String style;

	/**
	 * Creates a new Clickable text cell
	 */
	public CustomTextCell()
	{
		super();
		style = "customTextCell";
	}

	/**
	 * Renders the widget.
	 */
	@Override
	public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
		if (value != null) {
			sb.appendHtmlConstant("<div class=\"" + style + "\">");
			sb.appendHtmlConstant(value.asString().replaceAll("\n", "<br>"));
			sb.appendHtmlConstant("</div>");
		}
	}

	/**
	 * Adds a class to the style
	 * @param style
	 */
	public void addStyleName(String style)
	{
		this.style += " " + style;
	}


}
