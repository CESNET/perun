package cz.metacentrum.perun.webgui.widgets.cells;

import com.google.gwt.cell.client.AbstractCell;
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
public class WhetherEnabledCell extends AbstractCell<Boolean> {

	static private final ImageResource ENABLED = SmallIcons.INSTANCE.acceptIcon();
	static private final ImageResource DISABLED = SmallIcons.INSTANCE.crossIcon();

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, Boolean enabled, SafeHtmlBuilder sb) {

		// selects the image according to the status
		ImageResource ir = null;

		if(enabled){
			ir = ENABLED;
		}else{
			ir = DISABLED;
		}

		// if status not available
		if(ir == null){
			return;
		}

		// append the image
		Element imageElement = new Image(ir).getElement();
		imageElement.setTitle(String.valueOf(enabled));
		SafeHtml image = SafeHtmlUtils.fromSafeConstant((imageElement.getString()));
		sb.append(image);
	}

}
