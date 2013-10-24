package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.authzResolver.Logout;
import cz.metacentrum.perun.webgui.model.PerunError;

import java.util.Collection;

/**
 * Logout button with image
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class LogoutButton extends Composite {

    static final String SHIBBOLETH_COOKIE_FORMAT = "^_shib.+$";

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

        Logout call = new Logout(new JsonCallbackEvents(){
            @Override
            public void onFinished(JavaScriptObject jso){

                // retrieves all the cookies
                Collection<String> cookies = Cookies.getCookieNames();

                // regexp
                RegExp regExp = RegExp.compile(SHIBBOLETH_COOKIE_FORMAT);

                for(String cookie : cookies)
                {
                    // shibboleth cookie?
                    MatchResult matcher = regExp.exec(cookie);
                    boolean matchFound = (matcher != null); // equivalent to regExp.test(inputStr);
                    if(matchFound){
                        // remove it
                        Cookies.removeCookieNative(cookie, "/");
                    }
                }

                button.setProcessing(false);
                RootLayoutPanel.get().clear();
                RootLayoutPanel.get().add(new LogoutWidget());
            }
            @Override
            public void onError(PerunError error){
                button.setProcessing(false);
            }
            @Override
            public void onLoadingStart() {
                button.setProcessing(true);
            }
        });
        // do the logout
        call.retrieveData();

    }

}