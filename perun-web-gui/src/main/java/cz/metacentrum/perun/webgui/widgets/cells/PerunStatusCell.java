package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Image;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;

/**
 * Custom GWT cell, which displays current status of the element
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class PerunStatusCell extends ClickableTextCell {

	/*
		 VALID  (0),
		 INVALID (1),    //just created object, where some information (e.g. attribute)  is missing
		 SUSPENDED (2),  //security issue
		 EXPIRED (3),
		 DISABLED (4);   //use this status instead of deleting the entity
		 */
	static private final ImageResource VALID = SmallIcons.INSTANCE.acceptIcon();
	static private final ImageResource INVALID = SmallIcons.INSTANCE.flagRedIcon();
	static private final ImageResource SUSPENDED = SmallIcons.INSTANCE.stopIcon();
	static private final ImageResource EXPIRED = SmallIcons.INSTANCE.flagYellowIcon();
	static private final ImageResource DISABLED = SmallIcons.INSTANCE.binClosedIcon();

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			String status, SafeHtmlBuilder sb) {

		// selects the image according to the status
		ImageResource ir = null;

		if (status == null) {
			sb.appendHtmlConstant("<div class=\"" + "customClickableTextCell" + "\">");
			sb.appendHtmlConstant("</div>");
			return;
		} else if(status.equalsIgnoreCase("VALID")){
			ir = VALID;
		} else if (status.equalsIgnoreCase("INVALID")){
			ir = INVALID;
		} else if (status.equalsIgnoreCase("SUSPENDED")){
			ir = SUSPENDED;
		} else if (status.equalsIgnoreCase("EXPIRED")){
			ir = EXPIRED;
		} else if (status.equalsIgnoreCase("DISABLED")){
			ir = DISABLED;
		}

		// if status not available
		if(ir == null){
			return;
		}

		// append the image
		Element imageElement = new Image(ir).getElement();
		imageElement.setTitle(status);
		SafeHtml image = SafeHtmlUtils.fromSafeConstant((imageElement.getString()));
		sb.appendHtmlConstant("<div class=\"" + "customClickableTextCell" + "\">");
		sb.append(image);
		sb.appendHtmlConstant("</div>");
	}

}
