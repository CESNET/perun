package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;

/**
 * Widget which is displayed when user logs out of Perun.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class LogoutWidget extends Composite {

	PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Creates a new instance of LogoutWidget
	 */
	public LogoutWidget() {

		// widget inside
		VerticalPanel widget = new VerticalPanel();

		this.initWidget(widget);

		try {

			widget.setSize("100%", "100%");
			FlexTable content = new FlexTable();
			content.setCellSpacing(5);
			widget.add(content);
			widget.setCellHorizontalAlignment(content, HasHorizontalAlignment.ALIGN_CENTER);
			widget.setCellVerticalAlignment(content, HasVerticalAlignment.ALIGN_MIDDLE);

			String server = session.getRpcServer();
			if (server.equalsIgnoreCase("fed")) {

				content.setWidget(0, 0, new Image(LargeIcons.INSTANCE.acceptIcon()));
				content.setHTML(0, 1, "<h2>Log-out from Perun was successful.</h2>");

			} else {

				content.setWidget(0, 0, new Image(LargeIcons.INSTANCE.errorIcon()));
				content.setHTML(0, 1, "<h2>You must close browser to successfully log-out from Perun.</h2>");

			}

			FlexTable links = new FlexTable();
			links.setCellSpacing(10);

			CustomButton loginButton = new CustomButton("Log-in back to Perun", SmallIcons.INSTANCE.arrowLeftIcon(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					History.back();
					Window.Location.reload();
				}
			});
			CustomButton perunWebButton = new CustomButton("Go to Perun web", SmallIcons.INSTANCE.arrowRightIcon(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					Window.Location.replace("https://perun-aai.org");
				}
			});
			perunWebButton.setImageAlign(true);

			links.setWidget(0, 0, loginButton);
			links.setWidget(0, 1, perunWebButton);

			content.setWidget(1, 0, links);
			content.getFlexCellFormatter().setColSpan(1, 0, 2);
			content.getFlexCellFormatter().setAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);

		} catch (Exception ex) {
			GWT.log(""+ex.toString());
		}
	}

}
