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
 * Custom GWT cell, which displays application status (NEW, VERIFIED, APPROVED, REJECTED)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PerunAppStateCell extends ClickableTextCell {

	static private final ImageResource NEW = SmallIcons.INSTANCE.addIcon();
	static private final ImageResource VERIFIED = SmallIcons.INSTANCE.hourglassIcon();
	static private final ImageResource APPROVED = SmallIcons.INSTANCE.acceptIcon();
	static private final ImageResource REJECTED = SmallIcons.INSTANCE.cancelIcon();

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			String status, SafeHtmlBuilder sb) {

		// selects the image according to the status
		ImageResource ir = null;

		if (status.equalsIgnoreCase("NEW")) {
			ir = NEW;
		} else if (status.equalsIgnoreCase("VERIFIED")) {
			ir = VERIFIED;
		} else if (status.equalsIgnoreCase("APPROVED")) {
			ir = APPROVED;
		} else if (status.equalsIgnoreCase("REJECTED")) {
			ir = REJECTED;
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
