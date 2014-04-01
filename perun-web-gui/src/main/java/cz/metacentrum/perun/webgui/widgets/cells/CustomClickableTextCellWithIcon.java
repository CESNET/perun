package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

/**
 * Custom GWT cell, which is clickable and looks like an anchor with icon
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CustomClickableTextCellWithIcon extends ClickableTextCell
{
	private String style;
	private Image img = null;

	/**
	 * Creates a new Clickable text cell
	 */
	public CustomClickableTextCellWithIcon(ImageResource resource)
	{
		super();
		img = new Image(resource);
		style = "customClickableTextCell";
	}

	/**
	 * Renders the widget.
	 */
	@Override
	protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
		if (value != null) {
			sb.appendHtmlConstant("<div class=\"" + style + "\">");
			img.setStyleName("customClickableCellIcon");
			sb.appendHtmlConstant(new HTML(""+img).getHTML());
			//img.getElement().setAttribute("style", "vertical-align: middle; padding: 0px 5px;");
			// sb.append(value);
			sb.appendEscapedLines(value.asString()); // for linebreaks (\n => </br>) in values
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
