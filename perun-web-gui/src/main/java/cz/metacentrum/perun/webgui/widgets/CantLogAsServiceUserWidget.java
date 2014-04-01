package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;

/**
 * Widget which is displayed when user logged to perun
 * is Service user.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CantLogAsServiceUserWidget extends Composite {

	/**
	 * Creates a new instance of widget
	 */
	public CantLogAsServiceUserWidget() {

		FlexTable layout = new FlexTable();

		String text = "<h2>Logging to Perun using service identity is not allowed!</h2><h4>To manage your service identity please log-in with your personal credentials.</h4>";

		layout.setWidget(0, 0, new HTML(new Image(LargeIcons.INSTANCE.errorIcon())+text));

		layout.setSize("100%", "100%");
		layout.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		this.initWidget(layout);

	}

}
