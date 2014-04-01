package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.authzResolver.Logout;

/**
 * Logout button with image
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class LogoutButton extends Composite {

	private CustomButton button;

	/**
	 * Creates a new button
	 */
	public LogoutButton() {

		// construct the button with image
		button = new CustomButton(ButtonTranslation.INSTANCE.logoutButton(), ButtonTranslation.INSTANCE.logout(), SmallIcons.INSTANCE.doorOutIcon(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				logout();
			}
		});

		this.initWidget(button);

	}

	/**
	 * Logout method, erases the cookies and calls RPC logout to invalidate session.
	 */
	private void logout() {

		Logout call = new Logout(JsonCallbackEvents.disableButtonEvents(button, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso){

				Utils.clearFederationCookies();

				History.newItem("logout");

				RootLayoutPanel.get().clear();
				RootLayoutPanel.get().add(new LogoutWidget());
			}
		}));
		// do the logout
		call.retrieveData();

	}

}
