package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;

/**
 * Widget which is displayed when user is not authorized to view some tab.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class NotAuthorizedWidget extends Composite {

	/**
	 * Creates a new instance of NotAuthorizeWidget
	 */
	public NotAuthorizedWidget() {

		// widget inside
		VerticalPanel widget = new VerticalPanel();

		// format widget
		widget.setSize("100%", "100%");
		HTML content = new HTML(new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>You don't have permission to view this page.</h2>");
		widget.add(content);
		widget.setCellHorizontalAlignment(content, HasHorizontalAlignment.ALIGN_CENTER);
		widget.setCellVerticalAlignment(content, HasVerticalAlignment.ALIGN_MIDDLE);

		this.initWidget(widget);

	}

}
